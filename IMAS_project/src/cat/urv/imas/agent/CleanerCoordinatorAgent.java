package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleanerCoordinator.RequesterBehaviour;
import cat.urv.imas.behaviour.cleanerCoordinator.RequestResponseBehaviour;
import cat.urv.imas.behaviour.cleanerCoordinator.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CleanerCoordinatorAgent extends ImasAgent {

    private Map<AID, Boolean> newPositionReceived;
    private List<AgentPosition> newPositions;
    private boolean mapRequestInProgress;
    private boolean mapUpdated;

    private AID coordinatorAgent;
    private GameSettings game;
    
    private boolean firstTurn;
    private int numCleaners;
    
    // Messages
    private ACLMessage requestMapMsg;
    private ACLMessage informNewPosMsg;

    public CleanerCoordinatorAgent() {
        super(AgentType.CLEANER_COORDINATOR);
    }

    @Override
    protected void setup() {
        this.mapRequestInProgress = false;
        this.mapUpdated = false;
        this.newPositionReceived = new HashMap<>();
        this.newPositions = new ArrayList<>();

        this.firstTurn = true;
        this.numCleaners = 0;
        
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.coordinatorAgent = UtilsAgents.searchAgentType(this, AgentType.COORDINATOR);

        /* ********************************************************************/
        //launchInitialRequest();
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        requestMapMsg = generateMsg(ACLMessage.REQUEST, 
                                    coordinatorAgent, 
                                    FIPANames.InteractionProtocol.FIPA_REQUEST, 
                                    MessageContent.GET_MAP);

        informNewPosMsg = new ACLMessage(ACLMessage.INFORM);
        informNewPosMsg.addReceiver(coordinatorAgent);
        
        this.addBehaviour(new ListenerBehaviour(this));
        //this.addBehaviour(new RequestResponseBehaviour(this, mt));
        //this.send(requestMapMsg);
        //this.mapRequestInProgress = true;
    }

    public void launchInitialRequest() {
        ACLMessage initialRequest = generateMsg(ACLMessage.REQUEST, coordinatorAgent, FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.GET_MAP);
        addBehaviour(new RequesterBehaviour(this, initialRequest));
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        if (firstTurn){
            for (Map.Entry<AgentType, List<Cell>> entry : this.getGame().getAgentList().entrySet()) {
                if (entry.getKey().name().equals(AgentType.CLEANER.toString())) {
                    numCleaners = entry.getValue().size();
                }
            }
            firstTurn = false;
        }
        
        this.game = game;
        this.setMapRequestInProgress(false);
        this.setMapUpdated(true);
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    public Map<AID, Boolean> getNewPositionReceived() {
        return newPositionReceived;
    }

    public void setNewPositionReceived(AID agent, boolean received) {
        this.newPositionReceived.put(agent, received);
    }
    
    public void resetNewPosition(){
        this.newPositions.removeAll(newPositions);
    }
    
    public void addNewPosition(AgentPosition newPos){
        this.newPositions.add(newPos);
        
        if (!this.newPositionReceived.values().contains(false)){
            try {
                informNewPosMsg.setContentObject(newPositions.toArray());
                this.send(informNewPosMsg);
            } catch (IOException ex) {
                Logger.getLogger(CleanerCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.send(informNewPosMsg);
        }
    }
    
    public ACLMessage getReqMapMsg() {
        return requestMapMsg;
    }
    
    public boolean isMapRequestInProgress() {
        return mapRequestInProgress;
    }

    public void setMapRequestInProgress(boolean mapRequestInProgress) {
        this.mapRequestInProgress = mapRequestInProgress;
    }
    
    public boolean isMapUpdated() {
        return mapUpdated;
    }

    public void setMapUpdated(boolean mapUpdated) {
        this.mapUpdated = mapUpdated;
    }
    
    public void sendMap(AID sender) {
        ACLMessage sendMapMsg = new ACLMessage(ACLMessage.INFORM);
        sendMapMsg.addReceiver(sender);
        try {
            sendMapMsg.setContentObject(this.game);
            this.send(sendMapMsg);
        } catch (IOException ex) {
            Logger.getLogger(SearcherCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
