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

import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

/**
 * Agent abstraction used in this practical work.
 * It gathers common attributes and functionality from all agents.
 */
public abstract class BaseAgent extends Agent {

    private AID parent;
    private boolean waitingForMap;
    
    /**
     * Type of this agent.
     */
    protected AgentType type;
    
    /**
     * Agents' owner.
     */
    public static final String OWNER = "urv";
    /**
     * Language used for communication.
     */
    public static final String LANGUAGE = "serialized-object";
    /**
     * Onthology used in the communication.
     */
    public static final String ONTOLOGY = "serialized-object";
    
    /**
     * Creates the agent.
     * @param type type of agent to set.
     */
    public BaseAgent(AgentType type) {
        super();
        this.type = type;
        this.waitingForMap = false;
        this.parent = null;
    }

    public AID getParent() {
        return parent;
    }

    public void setParent(AID parent) {
        this.parent = parent;
    }

    public void sendMapRequestToParent() {
        sendMapRequestTo(parent);
    }

    public void sendMapRequestTo(AID receiver) {
        ACLMessage requestMapMsg = generateMsg(ACLMessage.REQUEST,
                                                receiver,
                                                FIPANames.InteractionProtocol.FIPA_REQUEST,
                                                MessageContent.GET_MAP);
        this.send(requestMapMsg);
        this.waitingForMap = true;
    }

    public void clearWaitingForMap() {
        waitingForMap = false;
    }

    public boolean isWaitingForMap() {
        return waitingForMap;
    }
    
    /**
     * Informs the type of agent.
     * @return the type of agent.
     */
    public AgentType getType() {
        return this.type;
    }
    
    /**
     * Add a new message to the log.
     *
     * @param str message to show
     */
    public void log(String str) {
        System.out.println(getLocalName() + ": " + str);
    }
    
    /**
     * Add a new message to the error log.
     *
     * @param str message to show
     */
    public void errorLog(String str) {
        System.err.println(getLocalName() + ": " + str);
    }

    protected void registerToDF() {
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(type.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
    }

    public ACLMessage generateMsg(int performative, AID receiver, String protocol, String content) {
        ACLMessage msg = new ACLMessage(performative);
        msg.clearAllReceiver();
        msg.addReceiver(receiver);
        msg.setProtocol(protocol);
        log("Request message to agent " + receiver.getName());
        try {
            msg.setContent(content);
            log("Request message content:" + msg.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }
}
