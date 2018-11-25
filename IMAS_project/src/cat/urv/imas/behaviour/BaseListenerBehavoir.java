package cat.urv.imas.behaviour;

import cat.urv.imas.agent.CleanerAgent;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.ImasAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class BaseListenerBehavoir extends CyclicBehaviour {
    public static final Integer RETRY_TIME_MS = 2000;

    private ACLMessage msg;
    private ImasAgent imasAgent;

    public BaseListenerBehavoir(ImasAgent agent){
        super(agent);
        msg = null;
        agent = null;
    }

    protected void onRequest() {}

    protected void onPropose() {}

    protected void onAgree() {}

    protected void onRefuse() {
        ImasAgent agent = getImasAgent();
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
        setImasAgent((ImasAgent)getAgent());
        setMsg(getImasAgent().receive());

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

    public ImasAgent getImasAgent() {
        return imasAgent;
    }

    public void setImasAgent(ImasAgent agent) {
        this.imasAgent = agent;
    }
}
