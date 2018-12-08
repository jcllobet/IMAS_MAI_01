package cat.urv.imas.agent;

import cat.urv.imas.behaviour.cleanerCoordinator.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.GarbagePosition;

import cat.urv.imas.utils.Proposal;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleanerCoordinatorAgent extends BaseCoordinatorAgent {
    List<GarbagePosition> unassignedGarbage;
    List<GarbagePosition> pendingGarbage;
    List<GarbagePosition> assignedGarbage;
    List<Proposal> proposals;
    GarbagePosition allocatingGarbage;
    private int responses;
    private boolean contractNetInProgress;

    public CleanerCoordinatorAgent() {
        super(AgentType.CLEANER_COORDINATOR);
        this.responses = 0;
        this.allocatingGarbage = null;
        this.contractNetInProgress = false;
        this.unassignedGarbage = new ArrayList<>();
        this.pendingGarbage = new ArrayList<>();
        this.assignedGarbage = new ArrayList<>();
        this.proposals = new ArrayList<>();
    }

    @Override
    protected void setup() {
        this.setEnabledO2ACommunication(true, 1);
        registerToDF();

        setParent(UtilsAgents.searchAgentType(this, AgentType.COORDINATOR));

        informNewPosMsg.addReceiver(getParent());
        addBehaviour(new ListenerBehaviour(this));
    }

    public void onNewGarbage(List<GarbagePosition> locatedGarbage) {
        for (GarbagePosition newGarbage : locatedGarbage) {
            if (!unassignedGarbage.contains(newGarbage) && !assignedGarbage.contains(newGarbage)) {
                unassignedGarbage.add(newGarbage);
            }
        }
        tryAssignGarbage();
    }

    public void tryAssignGarbage() {
        if (!contractNetInProgress) {
            for (GarbagePosition garbage : unassignedGarbage) {
                if (!pendingGarbage.contains(garbage)) {
                    pendingGarbage.add(garbage);
                    contractNet(garbage);
                    return;
                }
            }
        }
    }

    public void contractNet(GarbagePosition garbage) {
        contractNetInProgress = true;
        allocatingGarbage = garbage;
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        responses = 0;

        for (AID child : getChildren()) {
            msg.addReceiver(child);
        }
        try {
            msg.setContentObject(garbage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(msg);
    }

    public void acceptedProposal(Proposal proposal) {
        proposals.add(proposal);
        responseReceived();
    }

    public void responseReceived() {
        responses++;

        if (responses == getNumChildren()) {
            if (!proposals.isEmpty()) {
                Proposal best = proposals.get(0);
                for (Proposal proposal : proposals) {
                    if (proposal.getDistance() < best.getDistance()) {
                        best = proposal;
                    }
                }
                proposals.remove(best);
                acceptProposal(best);
                unassignedGarbage.remove(allocatingGarbage);
                pendingGarbage.remove(allocatingGarbage);
                assignedGarbage.add(allocatingGarbage);
                rejectProposals();
            }

            responses = 0;
            proposals.clear();
            allocatingGarbage = null;
            contractNetInProgress = false;
            tryAssignGarbage();
        }
    }

    private void rejectProposals() {
        for (Proposal proposal : proposals) {
            ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
            msg.addReceiver(proposal.getAgent());
            send(msg);
        }
    }

    private void acceptProposal(Proposal best) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            msg.setContentObject(allocatingGarbage);
            msg.addReceiver(best.getAgent());
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GarbagePosition getAllocatingGarbage() {
        return allocatingGarbage;
    }
}
