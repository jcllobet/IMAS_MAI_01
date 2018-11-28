/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleaner;

import cat.urv.imas.agent.CleanerAgent;
import cat.urv.imas.behaviour.BaseListenerBehavior;
import cat.urv.imas.ontology.GameSettings;
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
    protected  void onInform() {
        CleanerAgent agent = (CleanerAgent) getBaseAgent();
        ACLMessage msg = getMsg();
        try {
            if (msg.getContentObject() instanceof GameSettings){
                agent.setParameters((GameSettings) msg.getContentObject());
                agent.computeNewPos();
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
