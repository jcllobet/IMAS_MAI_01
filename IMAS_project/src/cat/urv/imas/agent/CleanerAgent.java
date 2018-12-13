package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleaner.ListenerBehaviour;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.GarbagePosition;
import cat.urv.imas.utils.Move;
import cat.urv.imas.utils.Movement;
import cat.urv.imas.utils.Position;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.List;

public class CleanerAgent extends BaseWorkerAgent {
    private static final int MAX_STUCK = 2;
    private int cleanerCapacity;
    private GarbagePosition assigned;
    private int stuck;

    public CleanerAgent() {
        super(AgentType.CLEANER, CellType.RECYCLING_POINT_CENTER);
        assigned = null;
        stuck = 0;
    }

    public GarbagePosition getAssigned() {
        return assigned;
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
    protected void onParametersUpdate(GameSettings game) {
        if (assigned != null) {
            int dy = Math.abs(getPosition().getRow() - assigned.getRow());
            int dx = Math.abs(getPosition().getColumn() - assigned.getColumn());

            try {
                if (dx <= 1 && dy <= 1) {
                    ACLMessage msg = generateInformMsg(getParent(), FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.REMOVED_GARBAGE);
                    msg.setContentObject(assigned);
                    assigned = null;
                    send(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void computeNewPos() {
        Position newPos = null;

        if (getPrevious() != null && getPrevious().equals(getPosition())) {
            stuck++;
        }

        if (assigned == null || stuck > MAX_STUCK) {
            newPos = Movement.random(getPosition());
            stuck = 0;
        }
        else {
            List<Position> positions = GetPath(new Position(assigned.getRow(), assigned.getColumn()));

            if(positions.size()>0){
                if (positions.size() == 1) //TODO: Remove it ....Assigned position is already current position
                {
                    newPos = positions.get(0);
                    System.out.println("!!!!!!!!!!!!!!!!!!!" + getLocalName() + " " + getPosition() + " is already at destination " + newPos);
                }
                else{
                    newPos = positions.get(1);
                    System.out.println("!!!!!!!!!!!!!!!!!!!" + getLocalName() + " " + getPosition() + " going to " + newPos);
                }

            }
            else{
                System.err.println("Couldn't calculate next step for agent " + getLocalName());
            }
        }

        sendNewPosToParent(newPos);
    }

    public void accept(GarbagePosition garbage) {
        assigned = garbage;
    }
}
