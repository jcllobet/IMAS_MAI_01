package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searchCoordinator.ListenerBehaviour;
import cat.urv.imas.behaviour.searchCoordinator.RequestResponseBehaviour;
import cat.urv.imas.behaviour.searchCoordinator.RequesterBehaviour;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearcherCoordinatorAgent extends ImasAgent {

    private Map<AID, Boolean> newPositionReceived;
    private boolean mapRequestInProgress;
    private boolean mapUpdated;

    private AID coordinatorAgent;
    private GameSettings game;
    
    // Messages
    private ACLMessage requestMapMsg;

    public SearcherCoordinatorAgent() {
        super(AgentType.ESEARCHER_COORDINATOR);
    }

    @Override
    protected void setup() {
        this.mapRequestInProgress = false;
        this.mapUpdated = false;
        this.newPositionReceived = new HashMap<>();

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
        
        this.addBehaviour(new ListenerBehaviour(this));
        //this.addBehaviour(new RequestResponseBehaviour(this, mt));
        //this.send(requestMapMsg);
        //this.mapRequestInProgress = true;
    }

    public void launchInitialRequest() {
        ACLMessage initialRequest = generateMsg(ACLMessage.REQUEST, coordinatorAgent, FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.GET_MAP);
        addBehaviour(new RequesterBehaviour(this, initialRequest));
    }

    public Map<AID, Boolean> getNewPositionReceived() {
        return newPositionReceived;
    }

    public void setNewPositionReceived(Map<AID, Boolean> newPositionReceived) {
        this.newPositionReceived = newPositionReceived;
    }

    public boolean isMapRequestInProgress() {
        return mapRequestInProgress;
    }

    public void setMapRequestInProgress(boolean mapRequestInProgress) {
        this.mapRequestInProgress = mapRequestInProgress;
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
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
    
    public ACLMessage getReqMapMsg() {
        return requestMapMsg;
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
