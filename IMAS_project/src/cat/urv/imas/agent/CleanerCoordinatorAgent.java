package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleanerCoordinator.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;

import java.util.List;
import java.util.Map;

public class CleanerCoordinatorAgent extends BaseCoordinatorAgent {

    public CleanerCoordinatorAgent() {
        super(AgentType.CLEANER_COORDINATOR);
    }

    @Override
    protected void setup() {
        this.setEnabledO2ACommunication(true, 1);
        registerToDF();

        setParent(UtilsAgents.searchAgentType(this, AgentType.COORDINATOR));

        informNewPosMsg.addReceiver(getParent());
        addBehaviour(new ListenerBehaviour(this));
    }

    @Override
    public void setGame(GameSettings game) {
        super.setGame(game);
        // ...
    }
}
