/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.behaviour.BaseCoordinatorListenerBehavior;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
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
public class ListenerBehaviour extends BaseCoordinatorListenerBehavior {
    
    public ListenerBehaviour(CoordinatorAgent agent){
        super(agent);
    }

    @Override
    protected void onInform() {
        CoordinatorAgent agent = (CoordinatorAgent) getBaseAgent();
        ACLMessage msg = getMsg();
        if (msg.getContent().equals(MessageContent.MAP_UPDATED)) {
            agent.informToAllChildren(msg);
        } else if (msg.getSender().equals(agent.getParent())){
            try {
                agent.setGame((GameSettings) msg.getContentObject());
                agent.log("Map received");
            } catch (UnreadableException ex) {
                Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                List<MovementMsg> movements = Arrays.asList((MovementMsg[])msg.getContentObject());
                agent.addMovementsMsg(movements);
                agent.incrementMovementMsgCount();
                agent.sendMovements();
            } catch (UnreadableException ex) {
                Logger.getLogger(cat.urv.imas.behaviour.searchCoordinator.ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
