package cat.urv.imas.main;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import jade.core.AID;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.agent.UtilsAgents;
import jade.core.Profile;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import jade.Boot;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        /* Without GUI
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.PLATFORM_ID, "Platform Name");
        profile.setParameter(Profile.CONTAINER_NAME, "Container Name");
        ContainerController cc = rt.createMainContainer(profile);
        */

        // With gui, first launch the jade.Boot and then connect to it
        String[] param = new String[1];
        param[0] = "-gui";
        Boot.main(param);

        ProfileImpl p = new ProfileImpl(); // "From JADE: configuration-dependent classes and boot parameter"
        p.setParameter(Profile.CONTAINER_NAME, "IMAS-Container");
        p.setParameter(Profile.LOCAL_HOST, "127.0.0.1");
        p.setParameter(Profile.GUI, "true");
        ContainerController cc = Runtime.instance().createAgentContainer(p);  // createMainContainer

        // Add the system agent
        try {
            AgentController systemAgent = cc.createNewAgent("SystemAgent", 
            												"cat.urv.imas.agent.SystemAgent", null);
            systemAgent.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
