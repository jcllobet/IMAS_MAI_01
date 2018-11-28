package cat.urv.imas.utils;
import cat.urv.imas.utils.Move;

public class Movement {
    public static AgentPosition random(AgentPosition pos, Move movement) {
        switch(movement) {
            default:
            case UP:    return new AgentPosition(pos.getAgent(), pos.getRow()-1, pos.getColumn());
            case DOWN:  return new AgentPosition(pos.getAgent(), pos.getRow()+1, pos.getColumn());
            case RIGHT: return new AgentPosition(pos.getAgent(), pos.getRow(), pos.getColumn()+1);
            case LEFT:  return new AgentPosition(pos.getAgent(), pos.getRow(), pos.getColumn()-1);
        }
    }
}
