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
package cat.urv.imas.behaviour.cleaner;

import cat.urv.imas.agent.CleanerAgent;
import cat.urv.imas.agent.CleanerAgent;
import cat.urv.imas.ontology.GameSettings;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour for the Cleaner agent to deal with AGREE messages.
 * The Cleaner agent sends a REQUEST for the
 * information of the game settings. The System Agent sends an AGREE and 
 * then it informs of this information which is stored by the Cleaner agent.
 * 
 * NOTE: The game is processed by another behaviour that we add after the 
 * INFORM has been processed.
 */
public class RequesterBehaviour extends AchieveREInitiator {

    Integer RETRY_TIME_MS = 2000;

    public RequesterBehaviour(CleanerAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
        agent.log("Started behaviour to deal with AGREEs");
    }

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        CleanerAgent agent = (CleanerAgent) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
        CleanerAgent agent = (CleanerAgent) this.getAgent();
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
        try {
            GameSettings game = (GameSettings) msg.getContentObject();
            agent.setParameters(game);
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }

    /**
     * Handle NOT-UNDERSTOOD messages
     *
     * @param msg Message
     */
    @Override
    protected void handleNotUnderstood(ACLMessage msg) {
        CleanerAgent agent = (CleanerAgent) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
        CleanerAgent agent = (CleanerAgent) this.getAgent();
        agent.log("The action has failed.");

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
        CleanerAgent agent = (CleanerAgent) this.getAgent();
        agent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
        try {
            Thread.sleep(RETRY_TIME_MS);
            agent.launchInitialRequest();
        } catch (InterruptedException e) {
            agent.log("Failed retry");
        }
    }

}
