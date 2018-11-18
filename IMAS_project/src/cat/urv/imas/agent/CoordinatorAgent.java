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

import cat.urv.imas.behaviour.coordinator.RequestResponseBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviour;
import cat.urv.imas.ontology.MessageContent;
import jade.core.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    private Map<AID, Boolean> newPositionReceived;
    private boolean mapRequestInProgress;

    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID systemAgent;
    private AID searcherCoordinator;
    private AID cleanerCoordinator;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        this.mapRequestInProgress = false;
        this.newPositionReceived = new HashMap<>();

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // search SystemAgent (is a blocking method, so we will obtain always a correct AID)
        this.systemAgent = UtilsAgents.searchAgentType(this, AgentType.SYSTEM);
        this.searcherCoordinator = UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR);
        this.cleanerCoordinator = UtilsAgents.searchAgentType(this, AgentType.CLEANER_COORDINATOR);

        /* ********************************************************************/
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        ACLMessage initialRequest = generateMsg(ACLMessage.REQUEST, systemAgent, InteractionProtocol.FIPA_REQUEST, MessageContent.GET_MAP);

        this.addBehaviour(new RequesterBehaviour(this, initialRequest));
        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }

    public AID getSystemAgent() {
        return systemAgent;
    }

    public AID getSearcherCoordinator() {
        return searcherCoordinator;
    }

    public AID getCleanerCoordinator() {
        return cleanerCoordinator;
    }

    public Map<AID, Boolean> getNewPositionReceived() {
        return newPositionReceived;
    }

    public void setNewPositionReceived(Map<AID, Boolean> newPositionReceived) {
        this.newPositionReceived = newPositionReceived;
    }

    public boolean isMapRequestInProgress() {
        return mapRequestInProgress;
    }

    public void setMapRequestInProgress(boolean mapRequestInProgress) {
        this.mapRequestInProgress = mapRequestInProgress;
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

}
