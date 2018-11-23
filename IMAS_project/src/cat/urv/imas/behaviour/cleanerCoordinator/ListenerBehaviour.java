/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.cleanerCoordinator;

import cat.urv.imas.agent.CleanerCoordinatorAgent;
import cat.urv.imas.agent.CoordinatorAgent;
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
    
    public ListenerBehaviour(CleanerCoordinatorAgent agent){
        super(agent);
    }
    
    @Override
    public void action() {
        CleanerCoordinatorAgent cleanerCoordAgent = (CleanerCoordinatorAgent)this.getAgent();
        ACLMessage msg = cleanerCoordAgent.receive();
        
        //if a message is available and a listener is available
        if (msg != null){
            switch(msg.getPerformative()){  
                case ACLMessage.REQUEST:
                    if (msg.getContent().equals(MessageContent.GET_MAP)){
                        // If map is not updated and not updating...
                        cleanerCoordAgent.setNewPositionReceived(msg.getSender(), false);
                        // If map has been requested
                        if (cleanerCoordAgent.isMapRequestInProgress()){
                            cleanerCoordAgent.log("Request received but waiting for the map");
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.REFUSE);
                            cleanerCoordAgent.send(msg);
                        } else if (cleanerCoordAgent.isMapUpdated() == false){
                            cleanerCoordAgent.log("Request received but game is not updated");
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.REFUSE);
                            cleanerCoordAgent.send(msg);
                            
                            cleanerCoordAgent.send(cleanerCoordAgent.getReqMapMsg());
                            cleanerCoordAgent.setMapRequestInProgress(true);
                        }
                        // Agent requesting map when map is updated
                        else {
                            cleanerCoordAgent.log("Request received and the map is updated");
                            msg.createReply().setPerformative(ACLMessage.AGREE);
                            cleanerCoordAgent.sendMap(msg.getSender());
                        }
                        // Set updating map to true
                        // Send request map to System Agent
                        // Refuse petition from sender msg.getSender()
                        
                        // Else if map is updated
                        // Agree Request and inform the map
                        
                    }
                    break;
                case ACLMessage.PROPOSE:
                    // Auction logic
                    break;
                case ACLMessage.AGREE:
                    // Look who sends it: System Agent maybe sends the inform with the map back
                    break;
                case ACLMessage.REFUSE:
                    if (cleanerCoordAgent.isMapRequestInProgress()){
                        System.out.println("cleanerCoordAgent Action refused. Retrying in " + RETRY_TIME_MS + "...");
                        //cleanerCoordAgent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
                        try {
                            Thread.sleep(RETRY_TIME_MS);
                            cleanerCoordAgent.send(cleanerCoordAgent.getReqMapMsg());
                        } catch (InterruptedException e) {
                            cleanerCoordAgent.log("Failed retry");
                        }
                    }
                    break;
                case ACLMessage.INFORM:
                    cleanerCoordAgent.log("INFORM message received");
                    if (msg.getSender().equals(cleanerCoordAgent.getCoordinatorAgent())){
                        try {
                            cleanerCoordAgent.setGame((GameSettings) msg.getContentObject());
                            cleanerCoordAgent.log("Map received");
                        } catch (UnreadableException ex) {
                            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        cleanerCoordAgent.setNewPositionReceived(msg.getSender(), true);
                    }
                    break;
                default:
                    break;
            }
        } else {
                block();
        }
    }
    
}
