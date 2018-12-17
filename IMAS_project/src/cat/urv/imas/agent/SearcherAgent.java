package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import javafx.geometry.Pos;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearcherAgent extends BaseWorkerAgent {
    private List<GarbagePosition> locatedGarbage;
    private int ALPHA = 10;
    private int batterySize;
    private int battery;
    private Move nextMove;
    private Position startPoint;
    private Move fixedMove;
    private boolean tripStarted;
    private Position assigned;

    private int PICK_UP_TIME = 5;

    public SearcherAgent() {
        super(AgentType.SEARCHER, CellType.BATTERIES_CHARGE_POINT);
        // Array of the eSearcher surrounding Cells
        this.locatedGarbage = new ArrayList<>();
        this.batterySize = 0;
        this.battery = batterySize;
        this.assigned = null;
    }


    @Override
    protected void onNewParameters(GameSettings game) {
        batterySize = game.geteSearcherMaxSteps();
        battery = batterySize;
        startPoint = null;
        tripStarted = false;
    }

    @Override
    protected void setup() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setEnabledO2ACommunication(true, 1);
        registerToDF();

        setParent(UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR));

        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestToParent();
    }

    @Override
    protected void onParametersUpdate(GameSettings game) {
        updateVision(game);
        if (battery > 0 && !getPrevious().equals(getPosition())) {
            battery--;
        }
        if (assigned != null) {
            int dy = Math.abs(getPosition().getRow() - assigned.getRow());
            int dx = Math.abs(getPosition().getColumn() - assigned.getColumn());
            if (dx <= 1 && dy <= 1) {
                battery = batterySize;
                setBusy(PICK_UP_TIME);
                assigned = null;
                log("Recharged with " + batterySize + " units");
            }
        }
    }

    protected void updateVision(GameSettings game) {
        // Obtain the 8 surrounding cells at this turn
        locatedGarbage.clear();
        Position currPos = new Position();
        nextMove = null;

        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
               if (y !=0 || x != 0) {
                    currPos.set(getPosition().getRow() + y, getPosition().getColumn() + x);
                    Cell cell = game.getMap()[currPos.getRow()][currPos.getColumn()];
                    if (cell.getCellType().equals(CellType.FIELD)) {
                        FieldCell field = (FieldCell) cell;
                        Map<WasteType, Integer> waste = field.detectWaste();
                        for (Map.Entry<WasteType, Integer> entry : waste.entrySet()) {
                            locatedGarbage.add(new GarbagePosition(entry.getKey(), entry.getValue(), currPos));
                        }
                    }
                    
                    if (!cell.getCellType().equals(CellType.PATH) && nextMove == null){
                        if (startPoint == null && fixedMove == null){ // Starting turns until attached
                            nextMove = followPath(cell, game);
                            if (nextMove != null){
                                startPoint = new Position(getPosition());
                            }
                        } else if (getPosition().equals(startPoint) &&
                                tripStarted) { // We reach initial postion --> Detach
                            Move simulated = followPath(cell, game);
                            if (simulated != null){
                                fixedMove = detachPos(simulated);
                                nextMove = fixedMove;
                                tripStarted = false;
                                startPoint = null;
                            }
                        } else if (fixedMove != null){ // Looking for another wall
                            // Check whether we arrived to another wall
                            if (getWalls().contains(Movement.givenMove(fixedMove, getPosition()))){
                                startPoint = new Position(getPosition());
                                fixedMove = null;
                                nextMove = followPath(cell, game);
                            // If no wall is in front, keep the same movement
                            } else {
                                nextMove = fixedMove;
                            }
                        } else { // Regular movement around a block
                            nextMove = followPath(cell, game);
                            tripStarted = true;
                        }
                    }
                }
            }
        }

        if (!locatedGarbage.isEmpty()) {
            sendGarbageListToParent(locatedGarbage);
        }
    }

    @Override
    public void computeNewPos() {

        if (battery <= 0) {
            sendNewPosToParent(getPosition());
            return;
        }
        assigned = isBatteryNeeded();

        Position newPos = null;

        if (assigned != null) {
            startPoint = null;
            newPos = PathHelper.nextPath(getPosition(), assigned);
        }
        else if (nextMove != null) {
            newPos = Movement.givenMove(nextMove, getPosition());
        }
        else {
            newPos = Movement.random(getPosition());
        }

        sendNewPosToParent(newPos);
    }

    public Position isBatteryNeeded() {
        Integer min = Integer.MAX_VALUE;
        Position best = null;

        for (Position pos : getPointsOfInterest()) {
            Integer size = PathHelper.pathSize(getPosition(), pos);
            if (size < min) {
                min = size;
                best = pos;
            }
        }

        return ((min + ALPHA) < battery) ? null : best;
    }

    private Move detachPos(Move move) {
        return Move.rotate90(move);
    }

    private Move followPath(Cell cell, GameSettings game) {
        Move move = null;
        if (move == null){
            // Check position of the wall
            if (cell.getRow() - getPosition().getRow() == -1 &&
                    cell.getCol() - getPosition().getColumn() == +1){ // The wall is up-right
                move = Move.UP;
            } else if (cell.getRow() - getPosition().getRow() == -1){ // The wall is up-middle/left
                // Check the cell at your left position
                if (game.getMap()[getPosition().getRow()][getPosition().getColumn() - 1].getCellType().equals(CellType.PATH)){
                    move = Move.LEFT;
                // Check the cell below your position
                } else if (game.getMap()[getPosition().getRow() + 1][getPosition().getColumn()].getCellType().equals(CellType.PATH)){
                    move = Move.DOWN;
                } else {
                    move = Move.RIGHT;
                }
            } else if (cell.getRow() - getPosition().getRow() == +1 &&
                    cell.getCol() - getPosition().getColumn() == -1){ // The wall is down-left
                // Check the cell below your position
                if (game.getMap()[getPosition().getRow() + 1][getPosition().getColumn()].getCellType().equals(CellType.PATH)){
                    move = Move.DOWN;
                // Check the cell at your right position
                } else if (!(game.getMap()[getPosition().getRow()][getPosition().getColumn() + 1].getCellType().equals(CellType.PATH))){
                    move = Move.UP;
                } else {
                    move = Move.RIGHT;
                }
            } else if (cell.getRow() - getPosition().getRow() == +1){ // The wall is down-middle/right
                // Check the cell at your right position
                if (!(game.getMap()[getPosition().getRow()][getPosition().getColumn() + 1] instanceof PathCell)){
                    move = Move.UP;
                } else {
                    move = Move.RIGHT;
                }
            }
        }
        
        return move;
    }
}
