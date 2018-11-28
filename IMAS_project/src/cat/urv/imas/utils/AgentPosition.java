/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils;

import cat.urv.imas.agent.BaseAgent;
import jade.core.AID;
import java.io.Serializable;

public class AgentPosition extends Position implements Serializable {
    private BaseAgent agent;
    
    public AgentPosition(BaseAgent agent) {
        this.agent = agent;
    }
    
    public AgentPosition(BaseAgent agent, int row, int column) {
        super(row, column);
        this.agent = agent;
    }

    public AID getAID() {
        return agent.getAID();
    }

    public BaseAgent getAgent() {
        return agent;
    }

    @Override
    public String toString() {
        return (agent != null ? agent.getLocalName() : "??") + ": " + super.toString();
    }
}
