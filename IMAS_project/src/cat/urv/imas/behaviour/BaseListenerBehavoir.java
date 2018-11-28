package cat.urv.imas.behaviour;

import cat.urv.imas.agent.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class BaseListenerBehavoir extends CyclicBehaviour {
    public static final Integer RETRY_TIME_MS = 2000;

    private ACLMessage msg;
    private BaseAgent baseAgent;

    public BaseListenerBehavoir(BaseAgent agent){
        super(agent);
        msg = null;
        agent = null;
    }

    protected void onRequest() {}

    protected void onPropose() {}

    protected void onAgree() {}

    protected void onRefuse() {
        BaseAgent agent = getBaseAgent();
        if (agent.isWaitingForMap()) {
            agent.log("Action refused. Retrying in " + RETRY_TIME_MS + "...");
            try {
                Thread.sleep(RETRY_TIME_MS);
                agent.sendMapRequestToParent();
            } catch (InterruptedException e) {
                agent.log("Failed retry");
            }
        }
    }

    protected void onInform() {}

    @Override
    public void action() {
        setBaseAgent((BaseAgent)getAgent());
        setMsg(getBaseAgent().receive());

        // If a message is available and a listener is available
        if (getMsg() != null){
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST:
                    onRequest();
                    break;
                case ACLMessage.PROPOSE:
                    onPropose();
                    break;
                case ACLMessage.AGREE:
                    onAgree();
                    break;
                case ACLMessage.REFUSE:
                    onRefuse();
                    break;
                case ACLMessage.INFORM:
                    onInform();
                    break;
                default:
                    break;
            }
        } else {
            block();
        }
    }

    public ACLMessage getMsg() {
        return msg;
    }

    public void setMsg(ACLMessage msg) {
        this.msg = msg;
    }

    public BaseAgent getBaseAgent() {
        return baseAgent;
    }

    public void setBaseAgent(BaseAgent agent) {
        this.baseAgent = agent;
    }
}
