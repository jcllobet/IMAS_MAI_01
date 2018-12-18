package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.InfoAgent;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.*;
import jade.lang.acl.ACLMessage;

import java.util.Collections;
import java.util.LinkedList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseWorkerAgent extends BaseAgent {

    private Position position;
    private Position previous;
    private Position maxBounds;
    private Position minBounds;
    private List<Position> walls;
    private List<Position> pointsOfInterest;
    private CellType interestType;
    private int busy;
    private PathHelper pathing;


    public BaseWorkerAgent(AgentType type, CellType interestType) {
        super(type);
        this.interestType     = interestType;
        this.position         = null;
        this.previous         = null;
        this.walls            = new ArrayList<>();
        this.pointsOfInterest = new ArrayList<>();
        this.maxBounds        = new Position();
        this.minBounds        = new Position();
        this.busy             = 0;
    }

    protected void onNewParameters(GameSettings game) {
        // Additional setting of parameters
    }

    protected void onParametersUpdate(GameSettings game) {
        // Additional setting of parameters
    }

    public PathHelper getPathing() {
        return pathing;
    }

    public abstract void computeNewPos();

    public Position getPrevious() {
        return previous;
    }

    public void setParameters(GameSettings game) {
        Cell[][] map = game.getMap();
        assert(map[0].length > 0);

        previous = (position == null) ? null : new Position(position);
        if (position == null) {
            position = new Position();
            if (!findInMap(map)) {
                position = null;
            } else {
                pathing = game.getPathing();
                maxBounds.set(map.length - 1, map[0].length - 1);
                log("Position set " + position);
                onNewParameters(game);
            }
        } else {
            findInMap(map); // Returns false if not found
            onParametersUpdate(game);
        }
    }

    private boolean findInMap(Cell[][] map) {
        boolean agentFound = false;
        for (Cell[] row : map) {
            for (Cell cell : row) {
                // Is path (not a wall)
                if (cell.getCellType().equals(CellType.PATH)) {
                    if (!cell.isEmpty())
                    {
                        PathCell pathCell = (PathCell)cell;
                        InfoAgent cellAgent = null;
                        try {
                            cellAgent = pathCell.getAgents().getFirst();
                        } catch (Exception e) { /* Cell is empty, this should never happen  */ }
                        if (cellAgent.getAID().equals(getAID())) {
                            // Agent found
                            position.setRow(cell.getRow());
                            position.setColumn(cell.getCol());
                            agentFound = true;
                        }
                    }
                }
                // Is wall
                else {
                    walls.add(new Position(cell.getRow(), cell.getCol()));

                    if (cell.getCellType().equals(interestType)) {
                        pointsOfInterest.add(new Position(cell.getRow(), cell.getCol()));
                    }
                }
            }
        }
        return agentFound;
    }

    public void sendNewPosToParent(Position newPos) {
        ACLMessage msg = new InformMsg(MessageContent.NEW_POS);
        msg.addReceiver(getParent());
        try {
            msg.setContentObject(new MovementMsg(position, newPos, getType()));
            send(msg);
        } catch (IOException ex) {
            Logger.getLogger(CleanerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(AgentPosition position) {
        this.position = position;
    }

    public List<Position> getWalls() {
        return walls;
    }

    public void setWalls(List<Position> walls) {
        this.walls = walls;
    }

    public List<Position> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public void setPointsOfInterest(List<Position> pointsOfInterest) {
        this.pointsOfInterest = pointsOfInterest;
    }

    public void setBusy(int turns) {
        busy = turns;
    }

    public boolean updateBusy() {
        if (busy > 0) {
            busy--;
            return true;
        }
        return false;
    }
}