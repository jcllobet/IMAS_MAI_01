package cat.urv.imas.utils;
import cat.urv.imas.utils.Move;

public class Movement {
    public static Position random(Position pos, Move movement) {
        switch(movement) {
            default:
            case UP:    return new Position(pos.getRow() - 1, pos.getColumn());
            case DOWN:  return new Position(pos.getRow() + 1, pos.getColumn());
            case RIGHT: return new Position(pos.getRow(), pos.getColumn() + 1);
            case LEFT:  return new Position(pos.getRow(), pos.getColumn() - 1);
        }
    }
}
