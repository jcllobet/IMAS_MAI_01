package cat.urv.imas.utils;

import cat.urv.imas.ontology.MessageContent;
import jade.lang.acl.ACLMessage;

public class InformMsg extends ACLMessage {
    String informType;

    public InformMsg(String informType) {
        super(ACLMessage.INFORM);
        this.informType = informType;
    }

    public String getType() {
        return informType;
    }
}
