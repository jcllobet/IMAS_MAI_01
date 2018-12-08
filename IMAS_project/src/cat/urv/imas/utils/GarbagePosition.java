package cat.urv.imas.utils;

import cat.urv.imas.agent.BaseAgent;
import cat.urv.imas.ontology.WasteType;
import jade.core.AID;

import java.io.Serializable;

public class GarbagePosition extends Position implements Serializable {
    private WasteType type;
    private Integer amount;

    public GarbagePosition(WasteType type, int amount, Position position) {
        super(position);
        this.type = type;
        this.amount = amount;
    }

    public WasteType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.getShortString() + ": " + super.toString();
    }
}
