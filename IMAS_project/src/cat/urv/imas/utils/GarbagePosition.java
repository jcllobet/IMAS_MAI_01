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

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Position))
            return false;

        GarbagePosition other = (GarbagePosition)obj;
        return getRow() == other.getRow() && getColumn() == other.getColumn() && type.equals(other.getType());
    }

    @Override
    public String toString() {
        return type.getShortString() + "/" + amount + ":" + super.toString();
    }
}
