/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.behaviour.BaseListenerBehavoir;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import cat.urv.imas.utils.MovementMsg;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alarca_94
 */
public class ListenerBehaviour extends BaseListenerBehavoir {
    public ListenerBehaviour(SystemAgent agent) {
        super(agent);
    }

    @Override
    protected void onRequest() {
        SystemAgent agent = (SystemAgent) getBaseAgent();
        ACLMessage msg = getMsg();

        if (msg.getContent().equals(MessageContent.GET_MAP)){
            // If map has been requested
            if (!agent.isMapUpdated()){
                agent.resetNewPositions();
                agent.log("Request received from " + msg.getSender().getLocalName() + " but game is not updated");
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.REFUSE);
                agent.send(msg);
            }
            // Agent requesting map when map is updated
            else {
                agent.log("Request received from " + msg.getSender().getLocalName() + " and the map is updated");
                msg.createReply().setPerformative(ACLMessage.AGREE);

                agent.addElementsForThisSimulationStep();
                agent.sendMap(msg.getSender());
            }
        }
    }

    @Override
    protected void onInform() {
        SystemAgent agent = (SystemAgent) getBaseAgent();
        ACLMessage msg = getMsg();
        agent.log("INFORM message received from " + msg.getSender().getLocalName());
        try {
            List<MovementMsg> movements = Arrays.asList((MovementMsg[])msg.getContentObject());
            agent.updateMap(movements);
        } catch (UnreadableException ex) {
            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
