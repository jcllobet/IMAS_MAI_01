package cat.urv.imas.utils;

import cat.urv.imas.agent.AgentType;

import java.io.Serializable;

public class MovementMsg implements Serializable {
    private Position from;
    private Position to;
    private AgentType type;

    public MovementMsg(Position from, Position to, AgentType type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public AgentType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " wants to move from " + from + " to " + to;
    }
}
