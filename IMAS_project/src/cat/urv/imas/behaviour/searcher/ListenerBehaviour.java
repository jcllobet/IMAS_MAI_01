/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.searcher;

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
    
    public ListenerBehaviour(SearcherAgent agent){
        super(agent);
    }
    
    @Override
    public void action() {
        SearcherAgent searcherAgent = (SearcherAgent)this.getAgent();
        ACLMessage msg = searcherAgent.receive();
        
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
                    // Wait for two seconds to resend the message
                    if (searcherAgent.isWaitingForMap()){
                        searcherAgent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
                        try {
                            Thread.sleep(RETRY_TIME_MS);
                            searcherAgent.send(searcherAgent.getReqMapMsg());
                        } catch (InterruptedException e) {
                            searcherAgent.log("Failed retry");
                        }
                    }
                    break;
                case ACLMessage.INFORM:
                    try {
                        if (msg.getContentObject() instanceof GameSettings){
                            searcherAgent.setParameters((GameSettings) msg.getContentObject());
                            searcherAgent.computeNewPosition();
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
