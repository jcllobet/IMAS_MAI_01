/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleaner;

import cat.urv.imas.agent.CleanerAgent;
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
public class ListenerBehaviour extends CyclicBehaviour{
    Integer RETRY_TIME_MS = 2000;
    
    public ListenerBehaviour(CleanerAgent agent){
        super(agent);
    }
    
    @Override
    public void action() {
        CleanerAgent cleanerAgent = (CleanerAgent)this.getAgent();
        ACLMessage msg = cleanerAgent.receive();
        
        //if a message is available and a listener is available
        if (msg != null){
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST:
                    break;
                case ACLMessage.PROPOSE:
                    // Auction logic
                    break;
                case ACLMessage.AGREE:
                    // Look who sends it: System Agent maybe sends the inform with the map back
                    break;
                case ACLMessage.REFUSE:
                    if (cleanerAgent.isWaitingForMap()){
                        cleanerAgent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
                        try {
                            Thread.sleep(RETRY_TIME_MS);
                            cleanerAgent.send(cleanerAgent.getReqMapMsg());
                        } catch (InterruptedException e) {
                            cleanerAgent.log("Failed retry");
                        }
                    }
                    break;
                case ACLMessage.INFORM:
                    try {
                        if (msg.getContentObject() instanceof GameSettings){
                            cleanerAgent.setParameters((GameSettings) msg.getContentObject());
                            cleanerAgent.computeNewPosition();
                        }
                        break;
                    } catch (UnreadableException ex) {
                        Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                    }
                default:
                    break;
            }
        } else {
                block();
        }
    }
    
}
