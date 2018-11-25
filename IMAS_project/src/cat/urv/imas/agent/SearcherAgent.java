package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import cat.urv.imas.utils.Position;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearcherAgent extends ImasAgent {

    private AID searcherCoordinator;
    private AgentPosition position;
    private AgentPosition newPos;
    private Position maxRowColum;
    private List<int[]> chargingPoints;
    private List<int[]> walls;
    private Cell[] surroundingCells;
    private boolean startingTurn;
    
    private boolean waitingForMap;

    // Messages
    private ACLMessage requestMapMsg;
    private ACLMessage informNewPosMsg;
    
    public SearcherAgent() {
        super(AgentType.SEARCHER);
    }

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

        // Position of the Battery Charging points
        chargingPoints = new ArrayList<>();

        // Position of the walls points
        walls = new ArrayList<>();

        // Boolean for location initialization
        startingTurn = true;
        
        waitingForMap = false;

        // Array of the eSearcher surrounding Cells
        surroundingCells = new Cell[8];

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.searcherCoordinator = UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR);
        setParent(searcherCoordinator);
        
        requestMapMsg = generateMsg(ACLMessage.REQUEST, 
                                    searcherCoordinator, 
                                    FIPANames.InteractionProtocol.FIPA_REQUEST, 
                                    MessageContent.GET_MAP);
        informNewPosMsg = new ACLMessage(ACLMessage.INFORM);
        informNewPosMsg.addReceiver(searcherCoordinator);

        /* ********************************************************************/
        //launchInitialRequest();
        
        this.addBehaviour(new ListenerBehaviour(this));
        this.send(requestMapMsg);
        this.waitingForMap = true;
    }

    /**
     * Update the agent settings.
     *
     * @param game current game settings.
     */
    public void setParameters(GameSettings game) {
        Cell[][] map = game.getMap();
        PathCell curr_cell;
        int counter = 0;

        // TODO: update map at the Agents creation with proper AIDs
        // If it is the first turn, it needs to obtain its location, the Walls location and the Battery Charging Points
        if (startingTurn) {
            // Once it is located, it will no longer look for an agent in PathCells
            boolean located = false;

            startingTurn = false;

            int searcherIdx = Integer.parseInt(this.getLocalName().substring(
                    this.getLocalName().length() - 1, this.getLocalName().length()));

            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    // PathCells can contain an agent
                    if (map[i][j].getCellType().equals(CellType.PATH)) {
                        if (!located && !map[i][j].isEmpty()) {
                            curr_cell = (PathCell) map[i][j];
                            try {
                                // Check the Agent Type that resides in this Cell
                                if (curr_cell.getAgents().getFirst().getType().equals(AgentType.SEARCHER)) {
                                    if (counter == searcherIdx) {
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
                        walls.add(new int[]{i, j});

                        if (map[i][j].getCellType().equals(CellType.BATTERIES_CHARGE_POINT)) {
                            this.chargingPoints.add(new int[]{i, j});
                        }
                    }
                }
            }
            
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
                            if (curr_cell.getAgents().getFirst().getType().equals(AgentType.SEARCHER) &&
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

        // Obtain the 8 surrounding cells at this turn
        counter = 0;
        for (int i = position.getRow() - 1; i <= position.getRow() + 1; i++){
            for (int j = position.getColumn() - 1; j <= position.getColumn() + 1; j++){
                if ((i != this.position.getRow() || j != this.position.getColumn()) 
                        && i >= 0 && j >= 0 && i <= maxRowColum.getRow() && j <= maxRowColum.getColumn()) {
                    surroundingCells[counter] = game.getMap()[i][j];
                    counter++;
                }
            }
        }
    }

    public boolean isWaitingForMap() {
        return waitingForMap;
    }

    public void setWaitingForMap(boolean waitingForMap) {
        this.waitingForMap = waitingForMap;
    }
    
    public ACLMessage getReqMapMsg() {
        return requestMapMsg;
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
        return !(pos.getRow()    < 0 ||
                 pos.getColumn() < 0 ||
                 pos.getRow()    > maxRowColum.getRow() ||
                 pos.getColumn() > maxRowColum.getColumn());
    }
    
    private enum Move{
        UP,
        LEFT,
        RIGHT,
        DOWN;
        
        private static final List<Move> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = VALUES.size();
        private static final Random RANDOM = new Random();

        public static Move randomMove() {
            return VALUES.get(RANDOM.nextInt(SIZE));
        }
        
        public static AgentPosition newPos(AgentPosition pos, Move move) {
            switch(move){
                case UP:    return new AgentPosition(pos.getAgent(), pos.getRow()-1, pos.getColumn());
                case DOWN:  return new AgentPosition(pos.getAgent(), pos.getRow()+1, pos.getColumn());
                case RIGHT: return new AgentPosition(pos.getAgent(), pos.getRow(), pos.getColumn()+1);
                case LEFT:  return new AgentPosition(pos.getAgent(), pos.getRow(), pos.getColumn()-1);
                default:    return null;
            }
        }
    }
}
