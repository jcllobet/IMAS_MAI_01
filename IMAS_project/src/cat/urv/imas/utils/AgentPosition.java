/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.utils;

import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author alarca_94
 */
public class AgentPosition extends Position implements Serializable{
    private AID agent;
    
    public AgentPosition(AID agent) {        
        this.agent = agent;
    }
    
    public AgentPosition(AID agent, int row, int column) {
        super(row, column);
        
        this.agent = agent;
    }

    public AID getAgent() {
        return agent;
    }
    
    
    
}
