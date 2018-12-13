package cat.urv.imas.utils;
import cat.urv.imas.utils.Move;

import java.util.Random;

public class Movement {
    private static final Random RANDOM = new Random();

    public static Position random(Position pos) {
        Move movement = Move.getRandom();
        switch(movement) {
            default:
            case UP:    return new Position(pos.getRow() - 1, pos.getColumn());
            case DOWN:  return new Position(pos.getRow() + 1, pos.getColumn());
            case RIGHT: return new Position(pos.getRow(), pos.getColumn() + 1);
            case LEFT:  return new Position(pos.getRow(), pos.getColumn() - 1);
        }
    }
    
    public static Position givenMove(Move movement, Position pos) {
        switch(movement) {
            default:
            case UP:    return new Position(pos.getRow() - 1, pos.getColumn());
            case DOWN:  return new Position(pos.getRow() + 1, pos.getColumn());
            case RIGHT: return new Position(pos.getRow(), pos.getColumn() + 1);
            case LEFT:  return new Position(pos.getRow(), pos.getColumn() - 1);
        }
    }

    public static Position advance(Position position, GarbagePosition assigned) {
        int dx = assigned.getColumn() - position.getColumn();
        int dy = assigned.getRow() - position.getRow();

        if (dx >= +1) dx = +1;
        if (dx <= -1) dx = -1;
        if (dy >= +1) dy = +1;
        if (dy <= -1) dy = -1;

        if (dx != 0 && dy != 0) {
            if (RANDOM.nextBoolean()) {
                dx = 0;
            } else {
                dy = 0;
            }
        }

        return new Position(position.getRow() + dy, position.getColumn() + dx);
    }
}
