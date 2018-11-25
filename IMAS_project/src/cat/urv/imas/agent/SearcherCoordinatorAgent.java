package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searchCoordinator.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;
import java.util.Map;

public class SearcherCoordinatorAgent extends BaseCoordinator {

    public SearcherCoordinatorAgent() {
        super(AgentType.ESEARCHER_COORDINATOR);
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
        this.informNewPosMsg.addReceiver(getParent());
        
        this.addBehaviour(new ListenerBehaviour(this));
    }

    @Override
    public void setGame(GameSettings game) {
        super.setGame(game);
        if (getNumChildren() == null && getGame() != null) {
            for (Map.Entry<AgentType, List<Cell>> entry : this.getGame().getAgentList().entrySet()) {
                if (entry.getKey().name().equals(AgentType.SEARCHER.toString())) {
                    setNumChildren(entry.getValue().size());
                    break;
                }
            }
        }
    }
}
