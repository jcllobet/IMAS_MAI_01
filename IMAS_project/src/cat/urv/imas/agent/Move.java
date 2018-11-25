package cat.urv.imas.agent;

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

    public static int[] newPos(int[] pos, Move move) {
        switch(move){
            case UP: return new int[]{pos[0]-1, pos[1]};
            case DOWN: return new int[]{pos[0]+1, pos[1]};
            case RIGHT: return new int[]{pos[0], pos[1]+1};
            case LEFT: return new int[]{pos[0], pos[1]-1};
            default: return null;
        }
    }
}
