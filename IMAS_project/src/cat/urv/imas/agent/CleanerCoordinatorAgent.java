package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleanerCoordinator.ListenerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CleanerCoordinatorAgent extends BaseCoordinator {

    public CleanerCoordinatorAgent() {
        super(AgentType.CLEANER_COORDINATOR);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        registerToDF();

        // search CoordinatorAgent (is a blocking method, so we will obtain always a correct AID)
        setParent(UtilsAgents.searchAgentType(this, AgentType.COORDINATOR));

        /* ********************************************************************/
        //launchInitialRequest();
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new ListenerBehaviour(this));
        //this.addBehaviour(new RequestResponseBehaviour(this, mt));
        //this.send(requestMapMsg);
        //this.mapRequestInProgress = true;
    }
}
