package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleaner.ListenerBehaviour;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.GarbagePosition;
import cat.urv.imas.utils.Movement;
import cat.urv.imas.utils.Position;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CleanerAgent extends BaseWorkerAgent {
    private static final int MAX_STUCK = 2;
    private int cleanerCapacity;
    private GarbagePosition assigned;
    private HashMap<WasteType, Integer> storage;
    private int stuck;
    private boolean pickingUp;
    private int pickUpTurns;

    private int PICK_UP_TIME = 3;
    private int MUNICIPAL = 1;
    private int INDUSTRIAL = 3;

    public CleanerAgent() {
        super(AgentType.CLEANER, CellType.RECYCLING_POINT_CENTER);
        assigned = null;
        stuck = 0;
        storage = new HashMap<>();
        pickUpTurns = PICK_UP_TIME;
        pickingUp = false;
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

    private int freeStorage() {
        int amount = 0;
        Iterator it = storage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            amount = (Integer)pair.getValue();
            it.remove(); // avoids a ConcurrentModificationException
        }
        return cleanerCapacity - amount;
    }

    @Override
    protected void onParametersUpdate(GameSettings game) {
        if (assigned != null) {
            int dy = Math.abs(getPosition().getRow() - assigned.getRow());
            int dx = Math.abs(getPosition().getColumn() - assigned.getColumn());

            try {
                if (dx <= 1 && dy <= 1) {
                    pickingUp = true;
                    if (pickUpTurns == 0) {
                        int storageNeeded = assigned.getType().equals(WasteType.MUNICIPAL) ? MUNICIPAL : INDUSTRIAL;
                        if (freeStorage() >= storageNeeded) {
                            Integer ocuppied = storage.get(assigned.getType());
                            storage.put(assigned.getType(), (ocuppied != null ? ocuppied : 0) + storageNeeded);
                            ACLMessage msg = generateInformMsg(getParent(), FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.REMOVED_GARBAGE);
                            msg.setContentObject(assigned);
                            assigned = null;
                            send(msg);
                        } else {
                            System.out.println("Not enough space!");
                            // TODO say it to the cleaner coord
                        }
                        pickUpTurns = PICK_UP_TIME;
                        pickingUp = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void computeNewPos() {
        Position newPos = null;

        if (pickingUp) {
            pickUpTurns--;
            sendNewPosToParent(getPosition());
            return;
        }

        if (getPrevious() != null && getPrevious().equals(getPosition())) {
            stuck++;
        }

        if (assigned == null || stuck > MAX_STUCK) {
            newPos = Movement.random(getPosition());
            stuck = 0;
        }
        else {
            List<Position> positions = getPath(new Position(assigned.getRow(), assigned.getColumn()));

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
