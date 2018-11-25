/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleanerCoordinator;

import cat.urv.imas.agent.CleanerCoordinatorAgent;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.behaviour.BaseCoordinatorListenerBehavoir;
import cat.urv.imas.behaviour.BaseListenerBehavoir;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alarca_94
 */
public class ListenerBehaviour extends BaseCoordinatorListenerBehavoir {

    public ListenerBehaviour(CleanerCoordinatorAgent agent) {
        super(agent);
    }

    @Override
    protected void onInform() {
        CleanerCoordinatorAgent agent = (CleanerCoordinatorAgent) getImasAgent();
        ACLMessage msg = getMsg();
        agent.log("INFORM message received from " + msg.getSender().getLocalName());
        if (msg.getSender().equals(agent.getParent())){
            try {
                agent.setGame((GameSettings) msg.getContentObject());
                agent.log("Map received");
            } catch (UnreadableException ex) {
                Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                agent.addNewPosition((AgentPosition)msg.getContentObject());
                agent.addNewPosMsgCount();
                agent.sendNewPositions();
            } catch (UnreadableException ex) {
                Logger.getLogger(cat.urv.imas.behaviour.searchCoordinator.ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
