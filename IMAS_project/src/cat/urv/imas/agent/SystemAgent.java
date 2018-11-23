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

import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.InitialGameSettings;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.RequestResponseBehaviour;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private InitialGameSettings game;
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
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public void addElementsForThisSimulationStep() {
        this.game.addElementsForThisSimulationStep();
    }

    private void loadGUI() {
        try {
            this.gui = new GraphicInterface(game);
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
        this.game = InitialGameSettings.load("game.settings");
        log("Initial configuration settings loaded");

        // 3. Load GUI
        loadGUI();

        // 4. Create other agents
        createAgents();

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.coordinatorAgent = UtilsAgents.searchAgentType(this, AgentType.COORDINATOR);

        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), 
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
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
            for (int i = 0; i < numSearchers; ++i) {
                AgentController searcher = cc.createNewAgent("Searcher-" + i,
                        "cat.urv.imas.agent.SearcherAgent", null);
                searcher.start();
            }
            // Create cleaners
            for (int i = 0; i < numCleaners; ++i) {
                AgentController searcher = cc.createNewAgent("Cleaner-" + i,
                        "cat.urv.imas.agent.CleanerAgent", null);
                searcher.start();
            }

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void updateGUI() {
        this.gui.updateGame();
    }

    public void setGame(InitialGameSettings game) {
        this.game = game;
    }
}
