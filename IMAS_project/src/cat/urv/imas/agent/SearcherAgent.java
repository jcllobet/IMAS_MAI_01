package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.Position;

public class SearcherAgent extends BaseWorker {
    private final static int VISION_SIZE = 8;
    private static int mapID = 0;
    private Cell[] surroundingCells;

    public SearcherAgent() {
        super(AgentType.SEARCHER, mapID++, CellType.BATTERIES_CHARGE_POINT);
        // Array of the eSearcher surrounding Cells
        this.surroundingCells = new Cell[VISION_SIZE];
    }

    @Override
    protected void setup() {
        setEnabledO2ACommunication(true, 1);
        registerToDF();

        setParent(UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR));

        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestTo(getParent());
    }

    @Override
    protected void onParametersUpdate(GameSettings game) {
        // Obtain the 8 surrounding cells at this turn
        int current = 0;
        Position currPos = new Position();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                if (y !=0 || x != 0) {
                    currPos.set(getPosition().getRow() + y, getPosition().getColumn() + x);
                    if (isValidPos(currPos)) {
                        surroundingCells[current++] = game.getMap()[currPos.getRow()][currPos.getColumn()];
                    }
                }
            }
        }
    }

    @Override
    public void computeNewPos() {
        do {
            setNewPos(Move.newPos(getPosition(), Move.randomMove()));
        } while (!isValidPos(getNewPos()));

        sendNewPosToParent();
    }
}
