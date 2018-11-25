package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.AgentPosition;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseCoordinator extends ImasAgent {

    private GameSettings game;
    private boolean mapUpdated;
    private Map<AID, Boolean> newPositionReceived;
    private List<AgentPosition> newPositions;
    private Integer numChildren;

    public BaseCoordinator(AgentType type) {
        super(type);
        this.mapUpdated = false;
        this.newPositionReceived = new HashMap<>();
        this.newPositions = new ArrayList<>();
        this.numChildren = 0;
    }

    public void setGame(GameSettings game) {
        this.game = game;
        this.mapUpdated = true;
        clearWaitingForMap();
        if (numChildren == null){
            for (Map.Entry<AgentType, List<Cell>> entry : this.getGame().getAgentList().entrySet()) {
                if (entry.getKey().name().equals(AgentType.CLEANER.toString())) {
                    numChildren = entry.getValue().size();
                }
            }
        }
    }

    public boolean isMapUpdated() {
        return mapUpdated;
    }

    public GameSettings getGame() {
        return game;
    }

    public void sendMap(AID sender) {
        ACLMessage sendMapMsg = new ACLMessage(ACLMessage.INFORM);
        sendMapMsg.addReceiver(sender);
        try {
            sendMapMsg.setContentObject(game);
            this.send(sendMapMsg);
        } catch (IOException ex) {
            Logger.getLogger(SearcherCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Map<AID, Boolean> getNewPositionReceived() {
        return newPositionReceived;
    }

    public void setNewPositionReceived(AID agent, boolean received) {
        this.newPositionReceived.put(agent, received);
    }

    public void resetNewPositions(){
        this.newPositions.removeAll(newPositions);
    }

    public void addNewPosition(AgentPosition newPos){
        ACLMessage informNewPosMsg = new ACLMessage(ACLMessage.INFORM);
        informNewPosMsg.addReceiver(getParent());

        if (!this.newPositionReceived.values().contains(false)){
            try {
                informNewPosMsg.setContentObject(newPositions.toArray());
                this.send(informNewPosMsg);
            } catch (IOException ex) {
                Logger.getLogger(CleanerCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.send(informNewPosMsg);
        }
    }
}
