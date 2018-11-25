package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleaner.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.AgentPosition;
import cat.urv.imas.utils.Position;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CleanerAgent extends ImasAgent {

    public CleanerAgent() {
        super(AgentType.CLEANER);
    }

    private AID cleanerCoordinator;
    private AgentPosition position;
    private AgentPosition newPos;
    private Position maxRowColum;
    private List<Position> recyclingPoints;
    private List<Position> walls;
    private boolean startingTurn;
    private int cleanerCapacity;
    
    // Messages
    private ACLMessage informNewPosMsg;

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // Array [row, col]
        position    = new AgentPosition(this.getAID());
        newPos      = new AgentPosition(this.getAID());
        maxRowColum = new Position();

        // Position of the recycling points
        recyclingPoints = new ArrayList<>();

        // Position of the walls points
        walls = new ArrayList<>();

        // Boolean for location initialization
        startingTurn    = true;
        
        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        cleanerCoordinator = UtilsAgents.searchAgentType(this, AgentType.CLEANER_COORDINATOR);
        setParent(cleanerCoordinator);

        informNewPosMsg = new ACLMessage(ACLMessage.INFORM);
        informNewPosMsg.addReceiver(cleanerCoordinator);

        /* ********************************************************************/
        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestTo(cleanerCoordinator);
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setParameters(GameSettings game) {
        Cell[][] map = game.getMap();
        PathCell curr_cell;

        // TODO: update map at the Agents creation with proper AIDs
        // If it is the first turn, it needs to obtain its location, the Walls location and the Battery Charging Points
        if (startingTurn){
            // Once it is located, it will no longer look for an agent in PathCells
            boolean located = false;

            startingTurn = false;

            int cleanerIdx = Integer.parseInt(this.getLocalName().substring(
                    this.getLocalName().length()-1, this.getLocalName().length()));
            int counter = 0;

            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    // PathCells can contain an agent
                    if (map[i][j].getCellType().equals(CellType.PATH)) {
                        if (!located && !map[i][j].isEmpty()) {
                            curr_cell = (PathCell) map[i][j];
                            try {
                                // Check the Agent Type that resides in this Cell
                                if (curr_cell.getAgents().getFirst().getType().equals(AgentType.CLEANER)) {
                                    if (counter == cleanerIdx) {
                                        // Initial set of the Cell Agent AID
                                        curr_cell.getAgents().getFirst().setAID(this.getAID());
                                        position.setRow(i);
                                        position.setColumn(j);
                                        located = true;
                                    } else {
                                        counter++;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    // All other Cells will be a Wall (Some of them Recycling Points and some other Charging Points)
                    } else {
                        walls.add(new Position(i, j));

                        if (map[i][j].getCellType().equals(CellType.RECYCLING_POINT_CENTER)) {
                            this.recyclingPoints.add(new Position(i, j));
                        }
                    }
                }
            }

            // Set Maximum Waste Capacity
            cleanerCapacity = game.getCleanerCapacity();
            
            // Set limits of the map
            maxRowColum.setRow(map.length - 1);
            maxRowColum.setColumn(map[0].length - 1);

            System.out.println(this.getLocalName() + " starts at row " + String.valueOf(position.getRow()) + " and column " +
                    String.valueOf(position.getColumn()));

        } else {

            // This loop will be stopped once the agent finds itself
            firstLoop:
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (map[i][j].getCellType().equals(CellType.PATH) && !map[i][j].isEmpty()) {
                        curr_cell = (PathCell) map[i][j];
                        try {
                            if (curr_cell.getAgents().getFirst().getType().equals(AgentType.CLEANER) &&
                                    curr_cell.getAgents().getFirst().getAID().equals(this.getAID())) {
                                position.setRow(i);
                                position.setColumn(j);
                                break firstLoop;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void computeNewPosition() {
        boolean valid = false;
        while (!valid){
            newPos = Move.newPos(position, Move.randomMove());
            if (isValidPos(newPos)){
                valid = true;
            }
        }
        
        try {
            informNewPosMsg.setContentObject(newPos);
            this.send(informNewPosMsg);
        } catch (IOException ex) {
            Logger.getLogger(CleanerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean isValidPos(AgentPosition pos){
        if (pos.getRow() < 0 || 
                pos.getColumn() < 0 || 
                pos.getRow() > maxRowColum.getRow() || 
                pos.getColumn() > maxRowColum.getColumn()){
            return false;
        }
        return true;
    }
}
