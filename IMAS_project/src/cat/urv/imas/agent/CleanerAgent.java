package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleaner.ListenerBehaviour;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import jade.lang.acl.ACLMessage;

public class CleanerAgent extends BaseWorker {
    private static int mapID = 0;
    private int cleanerCapacity;

    public CleanerAgent() {
        super(AgentType.CLEANER, mapID, CellType.RECYCLING_POINT_CENTER);
    }

    @Override
    protected void setup() {
        // Initialize
        setEnabledO2ACommunication(true, 1);
        registerToDF();

        // Set parent and message
        setParent(UtilsAgents.searchAgentType(this, AgentType.CLEANER_COORDINATOR));

        // Behaviors
        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestTo(getParent());
    }

    @Override
    protected void onNewParameters(GameSettings game) {
        cleanerCapacity = game.getCleanerCapacity();
    }

    @Override
    public void computeNewPos() {
        do {
            setNewPos(Move.newPos(getPosition(), Move.randomMove()));
        } while (!isValidPos(getNewPos()));

        sendNewPosToParent();
    }
}
