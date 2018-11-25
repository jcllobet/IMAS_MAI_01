/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.searchCoordinator;

import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.SearcherCoordinatorAgent;
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
public class ListenerBehaviour extends CyclicBehaviour{
    
    Integer RETRY_TIME_MS = 2000;
    
    public ListenerBehaviour(SearcherCoordinatorAgent agent){
        super(agent);
    }
    
    @Override
    public void action() {
        SearcherCoordinatorAgent searchCoordAgent = (SearcherCoordinatorAgent)this.getAgent();
        ACLMessage msg = searchCoordAgent.receive();
        
        //if a message is available and a listener is available
        if (msg != null){
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST:
                    if (msg.getContent().equals(MessageContent.GET_MAP)){
                        // If map is not updated and not updating...
                        searchCoordAgent.getNewPositionReceived().put(msg.getSender(), false);
                        // If map has been requested
                        if (searchCoordAgent.isMapRequestInProgress()){
                            searchCoordAgent.log("Request received  " + msg.getSender().getLocalName() + 
                                    " but waiting for the map");
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.REFUSE);
                            searchCoordAgent.send(msg);
                        } else if (searchCoordAgent.isMapUpdated() == false){
                            searchCoordAgent.log("Request received from " + msg.getSender().getLocalName() + 
                                    " but game is not updated");
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.REFUSE);
                            searchCoordAgent.send(msg);
                            
                            searchCoordAgent.send(searchCoordAgent.getReqMapMsg());
                            searchCoordAgent.setMapRequestInProgress(true);
                        }
                        // Agent requesting map when map is updated
                        else {
                            searchCoordAgent.log("Request received from " + msg.getSender().getLocalName() + 
                                    " and the map is updated");
                            msg.createReply().setPerformative(ACLMessage.AGREE);
                            searchCoordAgent.sendMap(msg.getSender());
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
                    if (searchCoordAgent.isMapRequestInProgress()){
                        searchCoordAgent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
                        try {
                            Thread.sleep(RETRY_TIME_MS);
                            searchCoordAgent.send(searchCoordAgent.getReqMapMsg());
                        } catch (InterruptedException e) {
                            searchCoordAgent.log("Failed retry");
                        }
                    }
                    break;
                case ACLMessage.INFORM:
                    searchCoordAgent.log("INFORM message received from " + msg.getSender().getLocalName());
                    if (msg.getSender().equals(searchCoordAgent.getCoordinatorAgent())){
                        try {
                            searchCoordAgent.setGame((GameSettings) msg.getContentObject());
                            searchCoordAgent.log("Map received");
                        } catch (UnreadableException ex) {
                            Logger.getLogger(ListenerBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        searchCoordAgent.setNewPositionReceived(msg.getSender(), true);
                        try {
                            searchCoordAgent.addNewPosition((AgentPosition)msg.getContentObject());
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
