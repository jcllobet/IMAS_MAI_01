package cat.urv.imas.utils;

import java.util.Random;

public enum Move {
    UP, DOWN, LEFT, RIGHT;

    private static final Random RANDOM = new Random();
    public static Move getRandom() {
        double random = RANDOM.nextDouble();
        if (random < 0.25) return Move.UP;
        if (random < 0.50) return Move.DOWN;
        if (random < 0.75) return Move.LEFT;
        return Move.RIGHT;
    }
}
