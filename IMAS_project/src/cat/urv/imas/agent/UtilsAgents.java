/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.ontology.MessageContent;
import cat.urv.imas.utils.Position;
import jade.core.Agent;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for agents.
 */
public class UtilsAgents {

    /**
     * After each search of an agent, we will wait for 2 seconds before retrying.
     */
    private static final long DELAY = 2000;
    
    /**
     * To prevent being instanced.
     */
    private UtilsAgents() {
    }

    public static AID searchAgentType(Agent parent, AgentType type) {
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(type.toString());
        return UtilsAgents.searchAgent(parent, searchCriterion);
    }

    /**
     * To search an agent of a certain type
     *
     * @param parent Agent
     * @param sd ServiceDescription search criterion
     * @return AID of the agent if it is foun, it is a *blocking* method
     */
    public static AID searchAgent(Agent parent, ServiceDescription sd) {
        List<AID> result = searchAgents(parent, sd, null);
        return (result.size() == 0) ? null : result.get(0);
    }

    public static Integer manhattanDist(Position A, Position B) {
        return Math.abs(A.getRow() - B.getRow()) + Math.abs(A.getColumn() - B.getColumn());
    }

    public static List<AID> searchAgentsType(Agent parent, AgentType type, int num) {
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(type.toString());
        return UtilsAgents.searchAgents(parent, searchCriterion, num);
    }

    public static List<AID> searchAgentsType(Agent parent, AgentType type) {
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(type.toString());
        return UtilsAgents.searchAgents(parent, searchCriterion, null);
    }

    public static List<AID> searchAgents(Agent parent, ServiceDescription sd, Integer num) {
        /**
         * Searching an agent of the specified type
         */
        List<AID> found = new ArrayList<>();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = new DFAgentDescription[0];
            while ((result.length == 0 && num == null) || (num != null && result.length < num)) {
                SearchConstraints c = new SearchConstraints();
                c.setMaxResults(new Long(-1));
                result = DFService.search(parent, dfd, c);
                for (DFAgentDescription description : result) {
                    found.add(description.getName());
                }
                Thread.sleep(DELAY);
            }
        } catch (Exception fe) {
            System.err.println("ERROR: Cannot search the expected agent from parent " + parent.getLocalName());
            fe.printStackTrace();
            parent.doDelete();
        }
        return found;
    }

    /**
     * To create an agent in a given container
     *
     * @param container AgentContainer
     * @param agentName String Agent name
     * @param className String Agent class
     * @param arguments Object[] Arguments; null, if they are not needed
     */
    public static void createAgent(AgentContainer container, String agentName, String className, Object[] arguments) {
        try {
            AgentController controller = container.createNewAgent(agentName, className, arguments);
            controller.start();
        } catch (StaleProxyException e) {
            System.err.println("ERROR: Cannot create agent " + agentName + " of class " + className);
            e.printStackTrace();
        }
    }

    /**
     * To create the agent and the container together. You can specify a
     * container and reuse it.
     *
     * @param agentName String Agent name
     * @param className String Class
     * @param arguments Object[] Arguments
     */
    public static void createAgent(String agentName, String className, Object[] arguments) {
        try {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            AgentContainer container = rt.createAgentContainer(p);

            AgentController controller = container.createNewAgent(agentName, className, arguments);
            controller.start();
        } catch (Exception e) {
            System.err.println("ERROR: Cannot create agent " + agentName + " of class " + className);
            e.printStackTrace();
        }
    }

    /**
     * To create the agent and the container together, returning the container.
     *
     * @param agentName String Agent name
     * @param className String Class
     * @param arguments Object[] Arguments
     * @return AgentContainer created
     */
    public static AgentContainer createAgentGetContainer(String agentName, String className, Object[] arguments) {
        AgentContainer container = null;
        try {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            container = rt.createAgentContainer(p);

            AgentController controller = container.createNewAgent(agentName, className, arguments);
            controller.start();
        } catch (Exception e) {
            System.err.println("ERROR: Cannot create agent " + agentName + " of class " + className);
            e.printStackTrace();
        }
        return container;
    }

}
