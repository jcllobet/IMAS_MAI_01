/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.system.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.InfoAgent;
import cat.urv.imas.ontology.InitialGameSettings;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.*;
import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cat.urv.imas.agent.UtilsAgents.searchAgentsType;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends BaseCoordinatorAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;

    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public void addElementsForThisSimulationStep() {
        InitialGameSettings iniGame = (InitialGameSettings)getGame();
        iniGame.addElementsForThisSimulationStep();
    }

    private void loadGUI() {
        try {
            this.gui = new GraphicInterface(getGame());
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        registerToDF();

        // 2. Load game settings.
        setGame(InitialGameSettings.load("game.settings"));
        log("Initial configuration settings loaded");
        setMapUpdated(true);
        PathHelper.calculateAllPaths(getGame());

        // 3. Load GUI
        loadGUI();

        // 4. Create other agents
        createAgents();

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.coordinatorAgent = UtilsAgents.searchAgentType(this, AgentType.COORDINATOR);

        this.addBehaviour(new ListenerBehaviour(this));
    }

    @Override
    public void setGame(GameSettings game) {
        super.setGame(game);
    }

    private void createAgents() {
        ContainerController cc = this.getContainerController();
        try {
            AgentController coordinatorAgent = cc.createNewAgent("CoordinatorAgent",
                    "cat.urv.imas.agent.CoordinatorAgent", null);
            AgentController cleanerCoordinatorAgent = cc.createNewAgent("CleanerCoordinatorAgent",
                    "cat.urv.imas.agent.CleanerCoordinatorAgent", null);
            AgentController searchCoordinatorAgent = cc.createNewAgent("SearcherCoordinatorAgent",
                    "cat.urv.imas.agent.SearcherCoordinatorAgent", null);

            coordinatorAgent.start();
            searchCoordinatorAgent.start();
            cleanerCoordinatorAgent.start();

            int numSearchers = 0;
            int numCleaners = 0;
            for (Map.Entry<AgentType, List<Cell>> entry : this.getGame().getAgentList().entrySet()) {
                if (entry.getKey().name().equals(AgentType.SEARCHER.toString())) {
                    numSearchers = entry.getValue().size();
                }
                if (entry.getKey().name().equals(AgentType.CLEANER.toString())) {
                    numCleaners = entry.getValue().size();
                }
            }
            // Create searchers
            Integer[] args = new Integer[1];
            for (int i = 0; i < numSearchers; ++i) {
                args[0] = i;
                AgentController searcher = cc.createNewAgent("Searcher-" + i,
                        "cat.urv.imas.agent.SearcherAgent", null);
                searcher.start();
            }
            // Create cleaners
            for (int i = 0; i < numCleaners; ++i) {
                args[0] = i;
                AgentController searcher = cc.createNewAgent("Cleaner-" + i,
                        "cat.urv.imas.agent.CleanerAgent", null);
                searcher.start();
            }
            setWorkersAIDtoCells(AgentType.SEARCHER, numSearchers);
            setWorkersAIDtoCells(AgentType.CLEANER, numCleaners);

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void setWorkersAIDtoCells(AgentType type, int num) {
        List<AID> AIDs = UtilsAgents.searchAgentsType(this, type, num);
        Cell[][] map = getGame().getMap();

        for (int i = 0; i < AIDs.size(); ++i) {
            boolean found = false;
            int currentSearcher = 0;

            for (int j = 0; j < map.length && !found; ++j) {
                for (int k = 0; k < map[0].length && !found; ++k) {

                    Cell cell = map[j][k];
                    if (cell.getCellType().equals(CellType.PATH) && !cell.isEmpty()) {
                        PathCell pathCell = (PathCell) cell;
                        InfoAgent cellAgent = null;
                        try {
                            cellAgent = pathCell.getAgents().getFirst();
                        } catch (Exception e) { /* Cell is empty, this should never happen  */ }
                        if (cellAgent.getType().equals(type)) {
                            if (currentSearcher == i) {
                                // Agent found
                                cellAgent.setAID(AIDs.get(i));
                                System.out.println("Setting " + AIDs.get(i).getLocalName() + " to " + j + ", " + k);
                                found = true;
                            }
                            currentSearcher++;
                        }
                    }
                }
            }
        }
    }

    /*@Override
    public void addMovementMsg(AgentPosition newPos){
        getMovements().add(newPos);
        log("All positions received");
        if (getMovements().size() == getNumChildren()) {
            informToAllChildren(MessageContent.MAP_UPDATED);
            setMapUpdated(false);
            resetNewPositions();
        }
    }*/

    public void updateGUI() {
        this.gui.updateGame();
    }

    public void updateMap(List<MovementMsg> movements) {
        Cell[][] map = getGame().getMap();
        List<MovementMsg> newMovements = new ArrayList<>();
        List<MovementMsg> oldMovements = new ArrayList<>();
        oldMovements.addAll(movements);

        // Victor sabe como va esto
        for (MovementMsg msg : movements) {
            Cell cellTo = null;
            try {
                cellTo = map[msg.getTo().getRow()][msg.getTo().getColumn()];
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean alreadyOnMovements = false;
            boolean alreadyOnNewMovements = false;
            boolean isNotWall = cellTo.getCellType().equals(CellType.PATH);

            for (MovementMsg msg1 : oldMovements) {
                if (msg1.getFrom().equals(msg.getTo())) {
                    alreadyOnMovements = true;
                    break;
                }
            }

            for (MovementMsg msg1 : newMovements) {
                if (msg1.getTo().equals(msg.getTo())) {
                    alreadyOnNewMovements = true;
                    break;
                }
            }

            if (!alreadyOnMovements && !alreadyOnNewMovements && isNotWall) {
                newMovements.add(msg);
                oldMovements.remove(msg);
            }
        }

        for (MovementMsg msg : newMovements) {
            move(msg);
        }

        updateGUI();
        ACLMessage updateMsg = new InformMsg(MessageContent.MAP_UPDATED);
        informToAllChildren(updateMsg);
    }

    private void move(MovementMsg msg) {
        Cell[][] map = getGame().getMap();

        try {
            PathCell cellFrom = (PathCell) map[msg.getFrom().getRow()][msg.getFrom().getColumn()];
            Cell cellTo = map[msg.getTo().getRow()][msg.getTo().getColumn()];

            InfoAgent iAgent = cellFrom.getAgents().getFirst();
            ((PathCell) cellTo).addAgent(iAgent);
            cellFrom.removeAgent(iAgent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removedGarbage(GarbagePosition garbage) {
        Cell[][] map = getGame().getMap();

        // Remove waste from map
        FieldCell field = (FieldCell)map[garbage.getRow()][garbage.getColumn()];
        field.detectWaste();
        field.removeWaste();
    }
}
