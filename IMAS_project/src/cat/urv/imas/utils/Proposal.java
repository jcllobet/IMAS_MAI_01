package cat.urv.imas.utils;

import jade.core.AID;
import java.io.Serializable;

public class Proposal implements Serializable {
    private Integer distance;
    private AID agent;

    public Proposal(AID agent, Integer distance) {
        this.agent = agent;
        this.distance = distance;
    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return agent.getLocalName() + ":" + distance;
    }
}
