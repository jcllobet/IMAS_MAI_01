package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.InfoAgent;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import cat.urv.imas.utils.InformMsg;
import cat.urv.imas.utils.MovementMsg;
import cat.urv.imas.utils.Position;
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

    public BaseWorkerAgent(AgentType type, CellType interestType) {
        super(type);
        this.interestType     = interestType;
        this.position         = null;
        this.previous         = null;
        this.walls            = new ArrayList<>();
        this.pointsOfInterest = new ArrayList<>();
        this.maxBounds        = new Position();
        this.minBounds        = new Position();
    }

    protected boolean isValidPos(Position pos){
        return   pos.getRow()    > minBounds.getRow()    &&
                pos.getColumn() > minBounds.getColumn() &&
                pos.getRow()    < maxBounds.getRow()    &&
                pos.getColumn() < maxBounds.getColumn() && !walls.contains(pos);
    }

    protected void onNewParameters(GameSettings game) {
        // Additional setting of parameters
    }

    protected void onParametersUpdate(GameSettings game) {
        // Additional setting of parameters
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

    protected void sendNewPosToParent(Position newPos) {
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

    public List<Position> GetPath(Position endPosition) {
        List<Position> visitedPoints = new ArrayList<>();
        LinkedList<Position> nextToVisit = new LinkedList<>();
        int[][] manhattan_dir  = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 }};

        int[][] allsides_dir  = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 },  //Sides
                                  { 1, 1 }, { 1, -1 }, { -1, -1 }, { -1, 1 }}; //Diagonal

        //Finding the nearest path cell
        //in case the destination is a wall and a corner
        if(this.walls.contains(endPosition)){
            boolean findNearPath = false;

            for (int[] direction : allsides_dir) {
                Position newEndPos = new Position(endPosition.getRow() + direction[0], endPosition.getColumn() + direction[1]);

                if(isValidPos(newEndPos)){
                    endPosition = newEndPos;
                    findNearPath = true;
                    break;
                }
            }

            if (!findNearPath)
                return Collections.emptyList();
        }

        Position start = this.position;
        nextToVisit.add(start);

        while (!nextToVisit.isEmpty()) {
            Position cur = nextToVisit.remove();

            if (!isValidPos(cur) || visitedPoints.contains(cur)) {
                continue;
            }

            if (cur.equals(endPosition)) {
                return backtrackPath(cur);
            }

            for (int[] direction : manhattan_dir) {
                Position coordinate = new Position(cur.getRow() + direction[0], cur.getColumn() + direction[1], cur);

                nextToVisit.add(coordinate);

            }

            visitedPoints.add(cur);
        }
        return Collections.emptyList();
    }

    private List<Position> backtrackPath(
            Position cur) {
        List<Position> path = new ArrayList<>();
        Position iter = cur;

        while (iter != null) {
            path.add(iter);
            iter = iter.getParent();
        }

        Collections.reverse(path);
        return path;
    }
}