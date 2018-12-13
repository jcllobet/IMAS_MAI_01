/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleanerCoordinator;

import cat.urv.imas.agent.BaseAgent;
import cat.urv.imas.agent.CleanerCoordinatorAgent;
import cat.urv.imas.behaviour.BaseCoordinatorListenerBehavior;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.*;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.lang.acl.UnreadableException;

import java.util.Arrays;
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
    protected void onPropose() {
        CleanerCoordinatorAgent agent = (CleanerCoordinatorAgent) getBaseAgent();
        ACLMessage msg = getMsg();

        try {
            Integer distance = (Integer)msg.getContentObject();
            agent.log(LogCode.PROPOSE, "from " + msg.getSender().getLocalName() + " with distance: " + distance);
            agent.acceptedProposal(new Proposal(msg.getSender(), distance));
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRefuse() {
        CleanerCoordinatorAgent agent = (CleanerCoordinatorAgent) getBaseAgent();
        ACLMessage msg = getMsg();

        if (msg.getSender().equals(agent.getParent())) { // Refuse from parent (Map not updated)
            super.onRefuse();
        } else { // Refuse from child (Proposal rejected)
            agent.log(LogCode.REFUSE, "from " + msg.getSender().getLocalName() + " in proposal from " + agent.getAllocatingGarbage());
            agent.responseReceived();
        }
    }

    @Override
    protected void onInform(InformMsg msg) {
        CleanerCoordinatorAgent agent = (CleanerCoordinatorAgent) getBaseAgent();

        try {
            if (msg.getType().equals(MessageContent.REMOVED_GARBAGE)) {
                GarbagePosition garbage = (GarbagePosition)msg.getContentObject();
                msg.removeReceiver(agent.getAID());
                msg.addReceiver(agent.getParent());
                agent.send(msg);
                // Remove waste from pending
                agent.removeAssignedGarbage(garbage);
                // TODO mandar hasta el system agent
                // agent.removedGarbage(garbage);
            }
            else if (msg.getType().equals(MessageContent.NEW_GARBAGE)) {
                GarbagePosition[] garbage = (GarbagePosition[])msg.getContentObject();
                agent.onNewGarbage(Arrays.asList(garbage));
            }
            else if (msg.getType().equals(MessageContent.MAP_UPDATED)) {
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
