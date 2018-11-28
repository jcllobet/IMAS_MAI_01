package cat.urv.imas.behaviour;

import cat.urv.imas.agent.BaseCoordinatorAgent;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.LogCode;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class BaseCoordinatorListenerBehavior extends BaseListenerBehavior {

    public BaseCoordinatorListenerBehavior(BaseCoordinatorAgent agent) {
        super(agent);
    }

    @Override
    protected void onRequest() {
        BaseCoordinatorAgent agent = (BaseCoordinatorAgent) getBaseAgent();
        ACLMessage msg = getMsg();
        if (msg.getContent().equals(MessageContent.GET_MAP)){
            // If map has been requested
            if (agent.isWaitingForMap()){
                agent.log(LogCode.REQUEST, "from " + msg.getSender().getLocalName() + " but waiting for the map");
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.REFUSE);
                agent.send(msg);
            } else if (!agent.isMapUpdated()){
                agent.log(LogCode.REQUEST, "from " + msg.getSender().getLocalName() + " but map is not updated");
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.REFUSE);
                agent.send(msg);

                agent.sendMapRequestToParent();
            }
            // Agent requesting map when map is updated
            else {
                agent.log(LogCode.REQUEST, "from " + msg.getSender().getLocalName() + " and the map is updated");
                AID sender = msg.getSender();
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.AGREE);
                agent.send(msg); // Send agree
                agent.sendMap(sender); // Send map
            }
            // Set updating map to true
            // Send request map to System Agent
            // Refuse petition from sender msg.getSender()

            // Else if map is updated
            // Agree Request and inform the map
        }
    }

}
