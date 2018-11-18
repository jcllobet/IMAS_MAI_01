package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.RequesterBehaviour;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class SearcherAgent extends ImasAgent {

    private AID searcherCoordinator;
    private GameSettings game;

    public SearcherAgent() {
        super(AgentType.SEARCHER);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        this.searcherCoordinator = UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR);

        /* ********************************************************************/
        launchInitialRequest();
    }

    public void launchInitialRequest() {
        ACLMessage initialRequest = generateMsg(ACLMessage.REQUEST, searcherCoordinator, FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.GET_MAP);
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
