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

import cat.urv.imas.behaviour.coordinator.ListenerBehaviour;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.AgentPosition;
import jade.core.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends BaseCoordinator {

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
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // search SystemAgent (is a blocking method, so we will obtain always a correct AID)
        setParent(UtilsAgents.searchAgentType(this, AgentType.SYSTEM));
        this.searcherCoordinator = UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR);
        this.cleanerCoordinator = UtilsAgents.searchAgentType(this, AgentType.CLEANER_COORDINATOR);

        /* ********************************************************************/
        this.informNewPosMsg.addReceiver(getParent());
        
        this.addBehaviour(new ListenerBehaviour(this));
    }

    @Override
    public void setGame(GameSettings game) {
        super.setGame(game);
        setNumChildren(2); // TODO constant
    }

    public AID getSearcherCoordinator() {
        return searcherCoordinator;
    }

    public AID getCleanerCoordinator() {
        return cleanerCoordinator;
    }    
}
