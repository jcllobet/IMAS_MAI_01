package cat.urv.imas.behaviour;

import cat.urv.imas.agent.BaseAgent;
import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.InformMsg;
import cat.urv.imas.utils.LogCode;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class BaseListenerBehavior extends CyclicBehaviour {
    public static final Integer RETRY_TIME_MS = 50;

    private ACLMessage msg;
    private BaseAgent baseAgent;

    public BaseListenerBehavior(BaseAgent baseAgent){
        super(baseAgent);
        this.msg = null;
        this.baseAgent = null;
    }

    protected void onCFP() {
        getBaseAgent().log(LogCode.CFP, "from " + msg.getSender().getLocalName());
    }

    protected void onRequest() {
        getBaseAgent().log(LogCode.REQUEST, "from " + msg.getSender().getLocalName());
    }

    protected void onPropose() {
        getBaseAgent().log(LogCode.PROPOSE, "from " + msg.getSender().getLocalName());
    }

    protected void onAcceptProposal() {
        getBaseAgent().log(LogCode.ACCEPT_PROP, "from " + msg.getSender().getLocalName());
    }

    protected void onRejectProposal() {
        getBaseAgent().log(LogCode.REJECT_PROP, "from " + msg.getSender().getLocalName());
    }

    protected void onAgree() {
        getBaseAgent().log(LogCode.AGREE, "from " + msg.getSender().getLocalName());
    }

    protected void onRefuse() {
        BaseAgent agent = getBaseAgent();
        if (agent.isWaitingForMap()) {
            getBaseAgent().log(LogCode.REFUSE, "from " + msg.getSender().getLocalName() + ". Retrying in " + RETRY_TIME_MS + "...");
            try {
                Thread.sleep(RETRY_TIME_MS);
                agent.sendMapRequestToParent();
            } catch (InterruptedException e) {
                agent.log("Failed retry");
            }
        }
    }

    protected void onInform(InformMsg msg) {}

    @Override
    public void action() {
        setBaseAgent((BaseAgent)getAgent());
        setMsg(getBaseAgent().receive());

        // If a message is available and a listener is available
        if (getMsg() != null){
            switch (msg.getPerformative()) {
                case ACLMessage.ACCEPT_PROPOSAL:
                    onAcceptProposal();
                    break;
                case ACLMessage.REJECT_PROPOSAL:
                    onRejectProposal();
                    break;
                case ACLMessage.REQUEST:
                    onRequest();
                    break;
                case ACLMessage.PROPOSE:
                    onPropose();
                    break;
                case ACLMessage.AGREE:
                    onAgree();
                    break;
                case ACLMessage.CFP:
                    onCFP();
                    break;
                case ACLMessage.REFUSE:
                    onRefuse();
                    break;
                case ACLMessage.INFORM:
                    try {
                        InformMsg infMsg = (InformMsg) msg;
                        getBaseAgent().log(LogCode.INFORM, "from " + msg.getSender().getLocalName());
                        if (infMsg.getType().equals(MessageContent.CHILD_REQUEST)) {
                            getBaseAgent().addChild(msg.getSender());
                            getBaseAgent().log("Added " + msg.getSender().getLocalName() + " as child");
                        } else {
                            onInform(infMsg);
                        }
                    } catch (ClassCastException e) {
                        // This only happens if a non custom inform message was received
                        System.out.println(msg.getContent());
                    }
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
