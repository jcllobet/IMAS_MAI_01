package cat.urv.imas.utils;

import java.util.Random;

public enum Move {
    UP, DOWN, LEFT, RIGHT;

    private static final Random RANDOM = new Random();
    public static Move getRandom() {
        int x = RANDOM.nextInt(Move.class.getEnumConstants().length);
        return Move.class.getEnumConstants()[x];
    }
}
