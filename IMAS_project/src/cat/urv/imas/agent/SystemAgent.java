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
import cat.urv.imas.ontology.InitialGameSettings;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import jade.core.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends BaseCoordinator {

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
        setNumChildren(1); // TODO constant
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

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public void addNewPosition(AgentPosition newPos){
        getNewPositions().add(newPos);
        log("All positions received");
        if (getNewPositions().size() == getNumChildren()) {
            informToAllChildren(MessageContent.MAP_UPDATED);
            setMapUpdated(false);
            resetNewPositions();
        }
    }*/

    public void updateGUI() {
        this.gui.updateGame();
    }

    public void updateMap(List<AgentPosition> positions) {

    }
}
