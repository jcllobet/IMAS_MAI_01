package cat.urv.imas.agent;

import cat.urv.imas.behaviour.searcher.ListenerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.WasteType;
import cat.urv.imas.utils.*;
import javafx.geometry.Pos;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearcherAgent extends BaseWorkerAgent {
    private List<GarbagePosition> locatedGarbage;
    private int ALPHA = 10;
    private int batterySize;
    private int battery;

    public SearcherAgent() {
        super(AgentType.SEARCHER, CellType.BATTERIES_CHARGE_POINT);
        // Array of the eSearcher surrounding Cells
        this.locatedGarbage = new ArrayList<>();
        this.batterySize = 0;
        this.battery = batterySize;
    }


    @Override
    protected void onNewParameters(GameSettings game) {
        batterySize = game.geteSearcherMaxSteps();
        battery = batterySize;
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
        updateVision(game);
        if (battery > 0 && !getPrevious().equals(getPosition())) {
            battery--;
        }
    }

    protected void updateVision(GameSettings game) {
        // Obtain the 8 surrounding cells at this turn
        locatedGarbage.clear();
        Position currPos = new Position();
        int current = 0;

        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
               if (y !=0 || x != 0) {
                    currPos.set(getPosition().getRow() + y, getPosition().getColumn() + x);
                    // TODO Dont look out of the map
                    Cell cell = null;
                    try {
                        cell = game.getMap()[currPos.getRow()][currPos.getColumn()];
                    } catch (Exception e) {
                        System.out.println("Hello");
                    }
                    if (cell.getCellType().equals(CellType.FIELD)) {
                        FieldCell field = (FieldCell) cell;
                        Map<WasteType, Integer> waste = field.detectWaste();
                        for (Map.Entry<WasteType, Integer> entry : waste.entrySet()) {
                            locatedGarbage.add(new GarbagePosition(entry.getKey(), entry.getValue(), currPos));
                        }
                    }
                }
            }
        }

        if (!locatedGarbage.isEmpty()) {
            sendGarbageListToParent(locatedGarbage);
        }
    }

    @Override
    public void computeNewPos() {

        if (battery <= 0) {
            sendNewPosToParent(getPosition());
            return;
        }

        Position newPos = null;

        if (isBatteryNeeded()) {
            newPos = Movement.random(getPosition());
        } else {

        }

        sendNewPosToParent(newPos);
    }

    public boolean isBatteryNeeded() {
        Integer min = Integer.MAX_VALUE;

        for (Position pos : getPointsOfInterest()) {
            Integer size = PathHelper.pathSize(getPosition(), pos);
            if (size < min) {
                min = size;
            }
        }

        return (min + ALPHA) < battery;
    }
}
