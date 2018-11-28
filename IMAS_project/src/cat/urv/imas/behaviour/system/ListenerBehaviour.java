/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.behaviour.BaseListenerBehavior;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.LogCode;
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
public class ListenerBehaviour extends BaseListenerBehavior {
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
                agent.log(LogCode.REQUEST, "from " + msg.getSender().getLocalName() + " but map is not updated");
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.REFUSE);
                agent.send(msg);
            }
            // Agent requesting map when map is updated
            else {
                agent.log(LogCode.REQUEST, "from " + msg.getSender().getLocalName() + " and the map is updated");
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
        try {
            List<MovementMsg> movements = Arrays.asList((MovementMsg[])msg.getContentObject());
            agent.updateMap(movements);
        } catch (UnreadableException ex) {
            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
