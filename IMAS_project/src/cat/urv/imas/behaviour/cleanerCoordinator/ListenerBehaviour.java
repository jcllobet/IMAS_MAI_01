/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleanerCoordinator;

import cat.urv.imas.agent.CleanerCoordinatorAgent;
import cat.urv.imas.behaviour.BaseCoordinatorListenerBehavior;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.InformMsg;
import cat.urv.imas.utils.MovementMsg;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alarca_94
 */
public class ListenerBehaviour extends BaseCoordinatorListenerBehavior {

    public ListenerBehaviour(CleanerCoordinatorAgent agent) {
        super(agent);
    }

    @Override
    protected void onInform(InformMsg msg) {
        CleanerCoordinatorAgent agent = (CleanerCoordinatorAgent) getBaseAgent();

        try {
            if (msg.getType().equals(MessageContent.MAP_UPDATED)) {
                agent.informToAllChildren(msg);
            }
            else if (msg.getType().equals(MessageContent.NEW_MAP)){ // Map received from parent
                agent.setGame((GameSettings) msg.getContentObject());
                agent.log("Map received");
            }
            else if (msg.getType().equals(MessageContent.NEW_POS)) {
                agent.addMovementMsg((MovementMsg)msg.getContentObject());
                agent.incrementMovementMsgCount();
                agent.sendMovements();
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
