/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleaner;

import cat.urv.imas.agent.CleanerAgent;
import cat.urv.imas.behaviour.BaseListenerBehavior;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.GarbagePosition;
import cat.urv.imas.utils.InformMsg;
import cat.urv.imas.utils.LogCode;
import cat.urv.imas.utils.PathHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alarca_94
 */
public class ListenerBehaviour extends BaseListenerBehavior {
    
    public ListenerBehaviour(CleanerAgent agent){
        super(agent);
    }

    @Override
    protected void onAcceptProposal() {
        CleanerAgent agent = (CleanerAgent) getBaseAgent();
        ACLMessage msg = getMsg();

        try {
            GarbagePosition garbage = (GarbagePosition)msg.getContentObject();
            ACLMessage response = msg.createReply();
            agent.log(LogCode.ACCEPT_PROP, "from " + msg.getSender().getLocalName() + " for garbage " + garbage);
            agent.accept(garbage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCFP() {
        CleanerAgent agent = (CleanerAgent) getBaseAgent();
        ACLMessage msg = getMsg();
        try {
            GarbagePosition garbage = (GarbagePosition)msg.getContentObject();
            ACLMessage response = msg.createReply();

            Integer storageNeeded = (garbage.getType().equals(WasteType.MUNICIPAL) ? 1 : 3) * garbage.getAmount();

            if (/*agent.getAssigned() != null || */(storageNeeded > agent.freeStorage())) {
                getBaseAgent().log(LogCode.CFP, "CFP for " + garbage + " from " + msg.getSender().getLocalName() + " but already assigned");
                response.setPerformative(ACLMessage.REFUSE);
            } else {
                getBaseAgent().log(LogCode.CFP, "CFP for " + garbage + " from " + msg.getSender().getLocalName());
                response.setPerformative(ACLMessage.PROPOSE);
                response.setContentObject(PathHelper.pathSize(agent.getPosition(), garbage.getPosition()));
            }
            agent.send(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected  void onInform(InformMsg msg) {
        CleanerAgent agent = (CleanerAgent) getBaseAgent();

        try {
            if (msg.getType().equals(MessageContent.MAP_UPDATED)) {
                agent.sendMapRequestToParent();
            }
            else if (msg.getType().equals(MessageContent.NEW_MAP)) {
                agent.setParameters((GameSettings) msg.getContentObject());
                if (!agent.updateBusy()) {
                    agent.computeNewPos();
                } else {
                    agent.sendNewPosToParent(agent.getPosition());
                }
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
