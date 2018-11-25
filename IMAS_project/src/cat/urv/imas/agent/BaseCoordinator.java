package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.ontology.GameSettings;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.AgentPosition;
import jade.core.AID;
import jade.core.Agent;
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
    private List<AgentPosition> newPositions;
    private Integer numChildren;
    private int newPosMsgCount;
    protected ACLMessage informNewPosMsg;

    public BaseCoordinator(AgentType type) {
        super(type);
        this.mapUpdated = false;
        this.newPositions = new ArrayList<>();
        this.numChildren = null;
        this.newPosMsgCount = 0;
        this.informNewPosMsg = new ACLMessage(ACLMessage.INFORM);
    }

    public void setGame(GameSettings game) {
        if (game != null) {
            this.game = game;
            this.mapUpdated = true;
            clearWaitingForMap();
        }
    }

    public Integer getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(Integer numChildren) {
        this.numChildren = numChildren;
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
        Boolean isNull = game == null;
        System.out.println("SENDING MAP TO " + sender.getLocalName() + " FROM " + getLocalName() + " AND IS NULL: " + isNull);
        try {
            sendMapMsg.setContentObject(game);
            this.send(sendMapMsg);
        } catch (IOException ex) {
            Logger.getLogger(SearcherCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setNewPositions(List<AgentPosition> positions) {
        newPositions = positions;
    }

    public void resetNewPositions(){
        this.newPositions.removeAll(newPositions);
    }

    public void addNewPosition(AgentPosition newPos){
        newPositions.add(newPos);
    }

    public void addNewPositions(List<AgentPosition> positions) {
        for (AgentPosition agentPosition : positions) {
            addNewPosition(agentPosition);
        }
    }
    
    public void sendNewPositions(){
        if (newPosMsgCount == numChildren && getParent() != null) {
            try {
                informNewPosMsg.setContentObject(newPositions.toArray(new AgentPosition[newPositions.size()]));
                this.send(informNewPosMsg);
            } catch (IOException ex) {
                Logger.getLogger(CleanerCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            setMapUpdated(false);
            resetNewPositions();
            resetNewPosMsgCount();
        }
    }

    public void informToAllChildren(String messageContent) {
        for (AgentPosition agentPosition : newPositions) {
            ACLMessage informUpdatedMsg = new ACLMessage(ACLMessage.INFORM);
            informUpdatedMsg.addReceiver(agentPosition.getAgent());
            informUpdatedMsg.setContent(messageContent);
            send(informUpdatedMsg);
        }
    }

    public List<AgentPosition> getNewPositions() {
        return newPositions;
    }

    private void resetNewPosMsgCount() {
        newPosMsgCount = 0;
    }
    
    public void addNewPosMsgCount() {
        newPosMsgCount++;
    }
}
