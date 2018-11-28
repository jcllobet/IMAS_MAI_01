package cat.urv.imas.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Move{
    UP,
    LEFT,
    RIGHT,
    DOWN;

    private static final List<Move> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Move randomMove() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

    public static AgentPosition newPos(AgentPosition pos, Move move) {
        switch(move){
            case UP:    return new AgentPosition(pos.getAgent(), pos.getRow()-1, pos.getColumn());
            case DOWN:  return new AgentPosition(pos.getAgent(), pos.getRow()+1, pos.getColumn());
            case RIGHT: return new AgentPosition(pos.getAgent(), pos.getRow(), pos.getColumn()+1);
            case LEFT:  return new AgentPosition(pos.getAgent(), pos.getRow(), pos.getColumn()-1);
            default:    return null;
        }
    }
}
