package cat.urv.imas.agent;

import cat.urv.imas.ontology.GameSettings;
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
    private List<int[]> newPositions;

    public BaseCoordinator(AgentType type) {
        super(type);
        this.mapUpdated = false;
        this.newPositionReceived = new HashMap<>();
        this.newPositions = new ArrayList<>();
    }

    public void setGame(GameSettings game) {
        this.game = game;
        this.mapUpdated = true;
        clearWaitingForMap();
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

    public void addNewPosition(int[] newPos){
        this.newPositions.add(newPos);
    }
}
