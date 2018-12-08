package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.Move;
import cat.urv.imas.utils.Movement;
import cat.urv.imas.utils.Position;

public class SearcherAgent extends BaseWorkerAgent {
    private final static int VISION_SIZE = 8;
    private Cell[] surroundingCells;

    public SearcherAgent() {
        super(AgentType.SEARCHER, CellType.BATTERIES_CHARGE_POINT);
        // Array of the eSearcher surrounding Cells
        this.surroundingCells = new Cell[VISION_SIZE];
    }

    @Override
    protected void setup() {
        setEnabledO2ACommunication(true, 1);
        registerToDF();

        setParent(UtilsAgents.searchAgentType(this, AgentType.ESEARCHER_COORDINATOR));

        addBehaviour(new ListenerBehaviour(this));
        sendMapRequestToParent();
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
        Position newPos = null;
        //do {
            newPos = Movement.random(getPosition(), Move.getRandom());
        //} while (!isValidPos(newPos));

        sendNewPosToParent(newPos);
    }
}
