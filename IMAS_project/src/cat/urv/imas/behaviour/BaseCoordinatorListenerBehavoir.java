package cat.urv.imas.behaviour;

import cat.urv.imas.agent.BaseCoordinatorAgent;
import cat.urv.imas.ontology.MessageContent;
import jade.lang.acl.ACLMessage;

public class BaseCoordinatorListenerBehavoir extends BaseListenerBehavoir {

    public BaseCoordinatorListenerBehavoir(BaseCoordinatorAgent agent) {
        super(agent);
    }

    @Override
    protected void onRequest() {
        BaseCoordinatorAgent agent = (BaseCoordinatorAgent) getBaseAgent();
        ACLMessage msg = getMsg();
        if (msg.getContent().equals(MessageContent.GET_MAP)){
            // If map has been requested
            if (agent.isWaitingForMap()){
                agent.log("Request received from " + msg.getSender().getLocalName() + " but waiting for the map");
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.REFUSE);
                agent.send(msg);
            } else if (!agent.isMapUpdated()){
                agent.log("Request received from " + msg.getSender().getLocalName() + "but game is not updated");
                msg = msg.createReply();
                msg.setPerformative(ACLMessage.REFUSE);
                agent.send(msg);

                agent.sendMapRequestToParent();
            }
            // Agent requesting map when map is updated
            else {
                agent.log("Request received from " + msg.getSender().getLocalName() + " and the map is updated");
                msg.createReply().setPerformative(ACLMessage.AGREE);
                agent.sendMap(msg.getSender());
            }
            // Set updating map to true
            // Send request map to System Agent
            // Refuse petition from sender msg.getSender()

            // Else if map is updated
            // Agree Request and inform the map
        }
    }

}
