/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
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
  
    private ACLMessage msg;
    
    public ListenerBehaviour(CoordinatorAgent agent){
        super(agent);
    }
    
    @Override
    public void action() {
        CoordinatorAgent coordAgent = (CoordinatorAgent)this.getAgent();
        this.msg = coordAgent.receive();
        
        //if a message is available and a listener is available
        if (msg != null){
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST:
                    if (msg.getContent().equals(MessageContent.GET_MAP)){
                        // If map is not updated and not updating...
                        coordAgent.getNewPositionReceived().put(msg.getSender(), false);
                        // If map has been requested
                        if (coordAgent.isMapRequestInProgress()){
                            coordAgent.log("Request received but waiting for the map");
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.REFUSE);
                            coordAgent.send(msg);
                        } else if (coordAgent.isMapUpdated() == false){
                            coordAgent.log("Request received but game is not updated");
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.REFUSE);
                            coordAgent.send(msg);
                            
                            coordAgent.send(coordAgent.getReqMapMsg());
                            coordAgent.setMapRequestInProgress(true);
                        }
                        // Agent requesting map when map is updated
                        else {
                            coordAgent.log("Request received and the map is updated");
                            msg.createReply().setPerformative(ACLMessage.AGREE);
                            coordAgent.sendMap(msg.getSender());
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
                    if (coordAgent.isMapRequestInProgress()){
                        coordAgent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
                        try {
                            Thread.sleep(RETRY_TIME_MS);
                            coordAgent.send(coordAgent.getReqMapMsg());
                        } catch (InterruptedException e) {
                            coordAgent.log("Failed retry");
                        }
                    }
                    break;
                case ACLMessage.INFORM:
                    coordAgent.log("INFORM message received");
                    if (msg.getSender().equals(coordAgent.getSystemAgent())){
                        try {
                            coordAgent.setGame((GameSettings) msg.getContentObject());
                            coordAgent.log("Map received");
                        } catch (UnreadableException ex) {
                            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                        }
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
