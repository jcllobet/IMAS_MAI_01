package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleaner.ListenerBehaviour;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.*;

public class CleanerAgent extends BaseWorkerAgent {
    private static final int MAX_STUCK = 2;
    private int cleanerCapacity;
    private HashMap<GarbagePosition, Integer> assignedGarbages;
    private GarbagePosition assigned;
    private Position recycling;
    private HashMap<WasteType, Integer> storage;
    private int stuck;

    private int PICK_UP_TIME = 3;
    private int MUNICIPAL = 1;
    private int INDUSTRIAL = 3;

    public CleanerAgent() {
        super(AgentType.CLEANER, CellType.RECYCLING_POINT_CENTER);
        assigned = null;
        stuck = 0;
        storage = new HashMap<>();
        assignedGarbages = new HashMap<>();
        recycling = null;
    }

    public GarbagePosition getAssigned() {
        return assigned;
    }

    @Override
    protected void setup() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Initialize
        setEnabledO2ACommunication(true, 1);
        registerToDF();

        // Set parent and message
        setParent(UtilsAgents.searchAgentType(this, AgentType.CLEANER_COORDINATOR));

        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestToParent();
    }

    @Override
    protected void onNewParameters(GameSettings game) {
        cleanerCapacity = game.getCleanerCapacity();
    }

    public int freeStorage() {
        int amount = 0;
        Iterator it = storage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            amount = (Integer)pair.getValue();
        }
        return cleanerCapacity - amount;
    }

    @Override
    protected void onParametersUpdate(GameSettings game) {
        if (recycling != null) {
            int dy = Math.abs(getPosition().getRow() - recycling.getRow());
            int dx = Math.abs(getPosition().getColumn() - recycling.getColumn());

            if (dx <= 1 && dy <= 1) {
                storage.clear();
                log("Waste dumped");
                // setBusy(1); just in case we want to wait for the dump
                recycling = null;
                recalculateTarget();
            }
        }
        else if (assigned != null) {
            int dy = Math.abs(getPosition().getRow() - assigned.getRow());
            int dx = Math.abs(getPosition().getColumn() - assigned.getColumn());

            try {
                if (dx <= 1 && dy <= 1) {
                    int storageNeeded = assigned.getType().equals(WasteType.MUNICIPAL) ? MUNICIPAL : INDUSTRIAL;
                    storageNeeded *= assigned.getAmount();
                    if (freeStorage() >= storageNeeded) {
                        Integer ocuppied = storage.get(assigned.getType());
                        storage.put(assigned.getType(), (ocuppied != null ? ocuppied : 0) + storageNeeded);
                        ACLMessage msg = generateInformMsg(getParent(), FIPANames.InteractionProtocol.FIPA_REQUEST, MessageContent.REMOVED_GARBAGE);
                        msg.setContentObject(assigned);
                        assignedGarbages.remove(assigned);
                        recalculateTarget();
                        send(msg);
                    } else {
                         log(LogCode.FATAL, "Not enough space!");
                    }
                    if (freeStorage() == 0) {
                        recycling = nearestRecyclingPoint();
                    }
                    setBusy(PICK_UP_TIME);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Position nearestRecyclingPoint() {
        Integer min = Integer.MAX_VALUE;
        Position best = null;

        for (Position pos : getPointsOfInterest()) {
            Integer size = PathHelper.pathSize(getPosition(), pos);
            if (size < min) {
                min = size;
                best = pos;
            }
        }

        return best;
    }

    @Override
    public void computeNewPos() {
        Position newPos = null;

        if (getPrevious() != null && getPrevious().equals(getPosition())) {
            stuck++;
        }

        if ((assigned == null && recycling == null) || stuck > MAX_STUCK) {
            newPos = Movement.random(getPosition());
            stuck = 0;
        }
        else if (recycling == null) {
            newPos = PathHelper.nextPath(getPosition(), assigned.getPosition());
        } else {
            newPos = PathHelper.nextPath(getPosition(), recycling);
        }

        sendNewPosToParent(newPos);
    }

    public void recalculateTarget() {
        int storageNeeded = 0;
        if (assignedGarbages.size() > 0) {
            assigned = shortestGarbage(assignedGarbages);
            storageNeeded = (assigned.getType().equals(WasteType.MUNICIPAL) ? MUNICIPAL : INDUSTRIAL) * assigned.getAmount();
        } else {
            assigned = null;
        }

        if (freeStorage() <= storageNeeded) {
            recycling = nearestRecyclingPoint();
            assigned = null;
        }
    }

    public void accept(GarbagePosition garbage) {
        //assigned = garbage;
        assignedGarbages.put(garbage, PathHelper.pathSize(getPosition(), garbage.getPosition()));
        recalculateTarget();
    }

    private GarbagePosition shortestGarbage(HashMap<GarbagePosition, Integer> assignedGarbages) {
        updateDistances(assignedGarbages);

        int minDistance = Integer.MAX_VALUE;
        GarbagePosition closest = null;
        for (Map.Entry<GarbagePosition, Integer> entry : assignedGarbages.entrySet()) {
            if (entry.getValue() < minDistance){
                minDistance = entry.getValue();
                closest = entry.getKey();
            }
        }

        return closest;
    }

    private void updateDistances(HashMap<GarbagePosition, Integer> assignedGarbages) {
        for (Map.Entry<GarbagePosition, Integer> entry : assignedGarbages.entrySet()) {
            assignedGarbages.replace(entry.getKey(), PathHelper.pathSize(getPosition(), entry.getKey().getPosition()));
        }
    }
}
