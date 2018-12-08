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

    public Integer getDistance() {
        return distance;
    }

    public AID getAgent() {
        return agent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof  Proposal))
            return false;
        Proposal other = (Proposal)obj;
        return other.getDistance().equals(distance) && other.getAgent().equals(agent);
    }

    @Override
    public String toString() {
        return agent.getLocalName() + ":" + distance;
    }
}
