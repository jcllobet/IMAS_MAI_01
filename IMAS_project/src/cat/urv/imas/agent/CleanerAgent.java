package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleaner.ListenerBehaviour;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.Move;
import cat.urv.imas.utils.Movement;
import cat.urv.imas.utils.Position;

public class CleanerAgent extends BaseWorkerAgent {
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
        sendMapRequestToParent();
    }

    @Override
    protected void onNewParameters(GameSettings game) {
        cleanerCapacity = game.getCleanerCapacity();
    }

    @Override
    public void computeNewPos() {
        Position newPos = null;
        do {
            newPos = Movement.random(getPosition(), Move.getRandom());
        } while (!isValidPos(newPos));

        sendNewPosToParent(newPos);
    }
}
