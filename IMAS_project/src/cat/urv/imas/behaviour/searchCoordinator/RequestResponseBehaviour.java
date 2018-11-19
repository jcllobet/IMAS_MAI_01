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
package cat.urv.imas.behaviour.searchCoordinator;

import cat.urv.imas.agent.SearcherCoordinatorAgent;
import cat.urv.imas.ontology.MessageContent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 * A request-responder behaviour for System agent, answering to queries
 * from the Search Coordinator agent. The Search Coordinator agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class RequestResponseBehaviour extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public RequestResponseBehaviour(SearcherCoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting REQUESTs from authorized agents");
    }

    /**
     * When System Agent receives a REQUEST message, it agrees. Only if
     * message content is GET_MAP, method prepareResultNotification() will be invoked.
     *
     * @param msg message received.
     * @return AGREE message when all was ok, or FAILURE otherwise.
     */
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    protected ACLMessage handleRequest(ACLMessage msg) {
        SearcherCoordinatorAgent agent = (SearcherCoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            Object content = msg.getContent();
            // MESSAGE GET_MAP
            if (content.equals(MessageContent.GET_MAP)) {
                // Case 1: We dont have the game yet
                agent.getNewPositionReceived().put(msg.getSender(), false);
                if (agent.getGame() == null) {
                    agent.log("Request received but game is null");
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                // Case 2: Agent requesting map has a 1 in newPositionReceived[AID]
                else if (agent.getNewPositionReceived().get(msg.getSender())) {
                    agent.log("Agent sent new position before, but the map is not updated yet");
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                // Case 3: otherwise agree
                else {
                    agent.log("Agent sent new position before (or it's the first time), and the map is updated");
                    reply.setPerformative(ACLMessage.AGREE);
                }
            }
            // MESSAGE NEW_POS
            if (content.equals(MessageContent.NEW_POS)) {
                // TODO
                // TODO set getNewPositionReceived().replace(AID, true);
            }
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.getMessage());
            e.printStackTrace();
        }
        agent.log("Response being prepared");
        return reply;
    }

    /**
     * After sending an AGREE message on prepareResponse(), this behaviour
     * sends an INFORM message with the whole game settings.
     *
     * NOTE: This method is called after the response has been sent and only when one
     * of the following two cases arise: the response was an agree message OR no
     * response message was sent.
     *
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {

        // it is important to make the createReply in order to keep the same context of
        // the conversation
        SearcherCoordinatorAgent agent = (SearcherCoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);

        try {
            reply.setContentObject(agent.getGame());
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.toString());
            e.printStackTrace();
        }
        agent.log("Game settings sent");
        return reply;

    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}
