package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searchCoordinator.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.GarbagePosition;

import java.util.List;
import java.util.Map;

public class SearcherCoordinatorAgent extends BaseCoordinatorAgent {

    public SearcherCoordinatorAgent() {
        super(AgentType.ESEARCHER_COORDINATOR);
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
    public void onNewGarbage(List<GarbagePosition> garbagePositions) {
        //sendGarbageListToParent(garbagePositions);
    }

    @Override
    public void setGame(GameSettings game) {
        super.setGame(game);
        // ...
    }
}
