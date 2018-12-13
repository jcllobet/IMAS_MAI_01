package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.GarbagePosition;
import cat.urv.imas.utils.Move;
import cat.urv.imas.utils.Movement;
import cat.urv.imas.utils.Position;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearcherAgent extends BaseWorkerAgent {
    private List<GarbagePosition> locatedGarbage;
    private Move nextMove;
    private Position startPoint;
    private Move fixedMove;
    private boolean tripStarted;

    public SearcherAgent() {
        super(AgentType.SEARCHER, CellType.BATTERIES_CHARGE_POINT);
        // Array of the eSearcher surrounding Cells
        this.locatedGarbage = new ArrayList<>();
        startPoint = null;
        tripStarted = false;
    }

    @Override
    protected void setup() {
        setEnabledO2ACommunication(true, 1);
        registerToDF();

        setParent(UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR));

        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestToParent();
    }

    @Override
    protected void onParametersUpdate(GameSettings game) {
        updateVision(game);
    }

    protected void updateVision(GameSettings game) {
        // Obtain the 8 surrounding cells at this turn
        locatedGarbage.clear();
        Position currPos = new Position();
        int current = 0;
        nextMove = null;

        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                if (y !=0 || x != 0) {
                    currPos.set(getPosition().getRow() + y, getPosition().getColumn() + x);
                    // TODO Dont look out of the map
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
        Position newPos = null;
        //newPos = Movement.random(getPosition());
        if (nextMove != null){
            newPos = Movement.givenMove(nextMove, getPosition());
        } else {
            newPos = Movement.random(getPosition());
        }

        sendNewPosToParent(newPos);
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
            }// TODO: Check all walls surrounding it
        }
        
        return move;
    }
}
