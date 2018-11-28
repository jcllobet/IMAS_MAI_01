package cat.urv.imas.agent;

import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.utils.AgentPosition;
import cat.urv.imas.utils.MovementMsg;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseCoordinatorAgent extends BaseAgent {

    private GameSettings game;
    private boolean mapUpdated;
    private List<MovementMsg> movements;
    private int movementMsgCount;
    protected ACLMessage informNewPosMsg;

    public BaseCoordinatorAgent(AgentType type) {
        super(type);
        this.mapUpdated = false;
        this.movements = new ArrayList<>();
        this.movementMsgCount = 0;
        this.informNewPosMsg = new ACLMessage(ACLMessage.INFORM);
    }

    public void setGame(GameSettings game) {
        if (game != null) {
            this.game = game;
            this.mapUpdated = true;
            clearWaitingForMap();
        }
    }

    public int getNumChildren() {
        return getChildren().size();
    }

    public boolean isMapUpdated() {
        return mapUpdated;
    }

    public void setMapUpdated(boolean mapUpdated) {
        this.mapUpdated = mapUpdated;
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

    public void setMovements(List<MovementMsg> positions) {
        movements = positions;
    }

    public void resetNewPositions(){
        movements.removeAll(movements);
    }

    public void addMovementMsg(MovementMsg msg){
        movements.add(msg);
    }

    public void addMovementsMsg(List<MovementMsg> movements) {
        for (MovementMsg msg : movements) {
            addMovementMsg(msg);
        }
    }
    
    public void sendMovements(){
        if (movementMsgCount == getNumChildren() && getParent() != null) {
            try {
                informNewPosMsg.setContentObject(movements.toArray(new MovementMsg[movements.size()]));
                this.send(informNewPosMsg);
            } catch (IOException ex) {
                Logger.getLogger(CleanerCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            setMapUpdated(false);
            resetNewPositions();
            resetNewPosMsgCount();
        }
    }

    public List<MovementMsg> getMovements() {
        return movements;
    }

    private void resetNewPosMsgCount() {
        movementMsgCount = 0;
    }
    
    public void incrementMovementMsgCount() {
        movementMsgCount++;
    }
}
