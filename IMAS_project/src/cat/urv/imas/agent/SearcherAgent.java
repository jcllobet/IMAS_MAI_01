package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.RequesterBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class SearcherAgent extends ImasAgent {

    private AID searcherCoordinator;
    private int[] position;
    private List<int[]> chargingPoints;
    private List<int[]> walls;
    private Cell[] surroundingCells;
    private boolean startingTurn;

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
        position = new int[2];

        // Position of the Battery Charging points
        chargingPoints = new ArrayList<>();

        // Position of the walls points
        walls = new ArrayList<>();

        // Boolean for location initialization
        startingTurn = true;

        // Array of the eSearcher surrounding Cells
        surroundingCells = new Cell[8];

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.searcherCoordinator = UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR);

        /* ********************************************************************/
        launchInitialRequest();
    }

    public void launchInitialRequest() {
        ACLMessage initialRequest = generateMsg(ACLMessage.REQUEST, searcherCoordinator, FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.GET_MAP);
        addBehaviour(new RequesterBehaviour(this, initialRequest));
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
                                        position[0] = i;
                                        position[1] = j;
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

            System.out.println(this.getLocalName() + " starts at row " + String.valueOf(position[0]) + " and column " +
                    String.valueOf(position[1]));

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
                                position[0] = i;
                                position[1] = j;
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
        for (int i = position[0] - 1; i <= position[0] + 1; i++){
            for (int j = position[1] - 1; j <= position[1] + 1; j++){
                if (i != j) {
                    surroundingCells[counter] = game.getMap()[i][j];
                    counter++;
                }
            }
        }
    }
}
