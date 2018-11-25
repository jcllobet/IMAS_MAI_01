/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleaner;

import cat.urv.imas.agent.CleanerAgent;
import cat.urv.imas.behaviour.BaseListenerBehavoir;
import cat.urv.imas.behaviour.searcher.*;
import cat.urv.imas.behaviour.searchCoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.SearcherAgent;
import cat.urv.imas.agent.SearcherCoordinatorAgent;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alarca_94
 */
public class ListenerBehaviour extends BaseListenerBehavoir {
    
    public ListenerBehaviour(CleanerAgent agent){
        super(agent);
    }

    @Override
    protected  void onInform() {
        CleanerAgent agent = (CleanerAgent)getImasAgent();
        ACLMessage msg = getMsg();
        try {
            if (msg.getContentObject() instanceof GameSettings){
                agent.setParameters((GameSettings) msg.getContentObject());
                agent.computeNewPosition();
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
