package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.InfoAgent;
import cat.urv.imas.utils.AgentPosition;
import cat.urv.imas.utils.Position;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseWorkerAgent extends BaseAgent {

    private AgentPosition position;
    private AgentPosition newPos;
    private Position maxBounds;
    private Position minBounds;
    private List<Position> walls;
    private List<Position> pointsOfInterest;
    private CellType interestType;

    private int mapID;

    public BaseWorkerAgent(AgentType type, int mapID, CellType interestType) {
        super(type);
        this.interestType     = interestType;
        this.mapID            = mapID;
        this.position         = null;
        this.newPos           = null;
        this.walls            = new ArrayList<>();
        this.pointsOfInterest = new ArrayList<>();
        this.maxBounds        = new Position();
        this.minBounds        = new Position();
    }

    protected boolean isValidPos(Position pos){
        return !(pos.getRow()    < minBounds.getRow()    ||
                 pos.getColumn() < minBounds.getColumn() ||
                 pos.getRow()    > maxBounds.getRow()    ||
                 pos.getColumn() > maxBounds.getColumn());
    }

    protected void onNewParameters(GameSettings game) {
        // Additional setting of parameters
    }

    protected void onParametersUpdate(GameSettings game) {
        // Additional setting of parameters
    }

    public abstract void computeNewPos();

    public void setParameters(GameSettings game) {
        Cell[][] map = game.getMap();
        assert(map[0].length > 0);

        if (position == null) {
            position = new AgentPosition(getAID());
            newPos = new AgentPosition(getAID());
            findInMap(map); // Returns false if not found
            onNewParameters(game);
            maxBounds.set(map.length - 1, map[0].length - 1);
            log("Position set " + position);
        } else {
            findInMap(map); // Returns false if not found
            onParametersUpdate(game);
        }
    }

    private boolean findInMap(Cell[][] map) {
        int currMapID = 0;

        for (Cell[] row : map) {
            for (Cell cell : row) {
                // Is path (not a wall)
                if (cell.getCellType().equals(CellType.PATH) && !cell.isEmpty()) {
                    PathCell pathCell = (PathCell)cell;
                    InfoAgent cellAgent = null;
                    try {
                        cellAgent = pathCell.getAgents().getFirst();
                    } catch (Exception e) { /* Cell is empty, this should never happen  */ }
                    if (cellAgent != null && currMapID == mapID && cellAgent.getType().equals(getType())) {
                        // Agent found
                        cellAgent.setAID(getAID());
                        position.setRow(cell.getRow());
                        position.setColumn(cell.getCol());
                        return true;
                    }
                    currMapID++;
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
        return false;
    }

    protected void sendNewPosToParent() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(getParent());
        try {
            msg.setContentObject(newPos);
            send(msg);
        } catch (IOException ex) {
            Logger.getLogger(CleanerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AgentPosition getPosition() {
        return position;
    }

    public void setPosition(AgentPosition position) {
        this.position = position;
    }

    public AgentPosition getNewPos() {
        return newPos;
    }

    public void setNewPos(AgentPosition newPos) {
        this.newPos = newPos;
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
}
