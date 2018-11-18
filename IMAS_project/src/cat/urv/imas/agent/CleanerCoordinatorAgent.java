package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleanerCoordinator.RequesterBehaviour;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

public class CleanerCoordinatorAgent extends ImasAgent {

    private Map<AID, Boolean> newPositionReceived;
    private boolean mapRequestInProgress;

    private AID coordinatorAgent;
    private GameSettings game;

    public CleanerCoordinatorAgent() {
        super(AgentType.CLEANER_COORDINATOR);
    }

    @Override
    protected void setup() {
        this.mapRequestInProgress = false;
        this.newPositionReceived = new HashMap<>();

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.coordinatorAgent = UtilsAgents.searchAgentType(this, AgentType.COORDINATOR);

        /* ********************************************************************/
        launchInitialRequest();
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
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
}
