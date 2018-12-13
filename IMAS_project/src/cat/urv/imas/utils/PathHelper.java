package cat.urv.imas.utils;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.InfoAgent;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PathHelper {
    static int mapWidth = 0;
    static Position[][] movements = null;
    static List<Position> walls;
    static Position maxBounds = null;

    private static Position nearest(Position wall) {
        int[][] allsides_dir  = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 },  //Sides
                { 1, 1 }, { 1, -1 }, { -1, -1 }, { -1, 1 }}; //Diagonal

        if (walls.contains(wall)) {
            for (int[] direction : allsides_dir) {
                Position newEndPos = new Position(wall.getRow() + direction[0], wall.getColumn() + direction[1]);
                if (isValidPos(newEndPos, walls, maxBounds)){
                    return newEndPos;
                }
            }
        }
        return wall;
    }

    public static Integer pathSize(Position start, Position end) {
        end = nearest(end);
        Integer size = 0;
        while (start != null && end != null && !start.equals(end)) {
            start = nextPath(start, end);
            size++;
        }
        if (start == null) return Integer.MAX_VALUE;
        if (end == null) return Integer.MAX_VALUE;
        return size;
    }

    public static Position nextPath(Position start, Position end) {
        end = nearest(end);
        return movements[getIndex(start.getRow(), start.getColumn(), mapWidth)]
                        [getIndex(end.getRow(), end.getColumn(), mapWidth)];
    }

    public static  void calculateAllPaths(GameSettings game) {
        Cell[][] map = game.getMap();
        assert(map[0].length > 0);

        maxBounds = new Position(map.length, map[0].length);
        walls = new ArrayList<>();
        int max = maxBounds.getRow() * maxBounds.getColumn();
        movements = new Position[max][max];
        mapWidth = maxBounds.getColumn();
        boolean[] isPath = new boolean[max];

        int k = 0;
        for (Cell[] row : map) {
            for (Cell cell : row) {
                isPath[k] = cell.getCellType().equals(CellType.PATH);
                if (!isPath[k]) {
                    walls.add(new Position(cell.getRow(), cell.getCol()));
                }
                k++;
            }
        }

        System.out.println();
        int completed = 0;
        int total = max * max;
        for (int i = 0; i < max; ++i) {
            for (int j = 0; j < max; ++j) {
                if (completed++ % 10 == 0) {
                    double percentage = completed * 100.0 / total;
                    System.out.print("\rCalculating path ... " + completed + " of " + total + " (" + percentage + "%)");
                }
                if (i != j) {
                    List<Position> path = null;
                    if (isPath[i] && isPath[j]) {
                        Position start = new Position(getRow(i, mapWidth), getCol(i, mapWidth));
                        Position end = new Position(getRow(j, mapWidth), getCol(j, mapWidth));
                        path = calculatePath(start, end, walls, maxBounds);
                    }
                    if (path != null && !path.isEmpty()){
                        movements[i][j] = path.get(1);
                    } else {
                        movements[i][j] = null;
                    }
                } else {
                    movements[i][j] = isPath[i] ? new Position(getRow(i, mapWidth), getCol(i, mapWidth)) : null;
                }
            }
        }
        System.out.println();
    }

    private static int getCol(int index, int width) {
        return index - getRow(index, width) * width;
    }

    private static int getRow(int index, int width) {
        return index / width;
    }

    private static int getIndex(int row, int col, int width) {
        return row * width + col;
    }

    private static boolean isValidPos(Position pos, List<Position> walls, Position maxBounds){
        return  pos.getRow()    > 0    &&
                pos.getColumn() > 0 &&
                pos.getRow()    < maxBounds.getRow()    &&
                pos.getColumn() < maxBounds.getColumn() && !walls.contains(pos);
    }

    private static List<Position> calculatePath(Position start, Position endPosition, List<Position> walls, Position maxBounds) {
        List<Position> visitedPoints = new ArrayList<>();
        LinkedList<Position> nextToVisit = new LinkedList<>();
        int[][] manhattan_dir  = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 }};

        nextToVisit.add(start);

        while (!nextToVisit.isEmpty()) {
            Position cur = nextToVisit.remove();

            if (!isValidPos(cur, walls, maxBounds) || visitedPoints.contains(cur)) {
                continue;
            }

            if (cur.equals(endPosition)) {
                return backtrackPath(cur);
            }

            for (int[] direction : manhattan_dir) {
                Position coordinate = new Position(cur.getRow() + direction[0], cur.getColumn() + direction[1], cur);

                nextToVisit.add(coordinate);
            }

            visitedPoints.add(cur);
        }
        return Collections.emptyList();
    }

    private static List<Position> backtrackPath(Position cur) {
        List<Position> path = new ArrayList<>();
        Position iter = cur;

        while (iter != null) {
            path.add(iter);
            iter = iter.getParent();
        }

        Collections.reverse(path);
        return path;
    }
}
