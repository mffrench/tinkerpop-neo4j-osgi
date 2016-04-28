package net.echinopsii.demo;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import org.neo4j.helpers.Pair;
import org.neo4j.server.CommunityBootstrapper;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Dictionary;

public class TinkerpopNeo4JManagedService implements ManagedService {

    private final static Logger log = LoggerFactory.getLogger(TinkerpopNeo4JManagedService.class);
    private static final String NEO4J_HOME = "neo4j.home";
    private static final String NEO4J_CONFIG_FILE_PATH_PROPS = "neo4j.configfile";

    private CommunityBootstrapper communityBootstrapper;
    private Thread shutdownHook ;

    public void stop() {
        if ( communityBootstrapper != null )
            communityBootstrapper.stop();
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("updated : {}", new Object[]{(dictionary==null)?"null conf":dictionary.toString()});
        if (dictionary!=null) {
            String neo4jHome = (String) dictionary.get(NEO4J_HOME);
            String configFilePath = neo4jHome + File.separator + dictionary.get(NEO4J_CONFIG_FILE_PATH_PROPS);
            configFilePath = configFilePath.replace(" ", "");
            System.setProperty("org.neo4j.server.properties", configFilePath);
            log.debug("Neo4J server config file path: {}", new Object[]{configFilePath});

            log.debug("Create neo4j server");
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }
            communityBootstrapper = new CommunityBootstrapper();
            communityBootstrapper.start(new File(configFilePath), (Pair<String, String>[]) new ArrayList().toArray(new Pair[0]));

            shutdownHook = new Thread() {
                @Override
                public void run() {
                    log.info("Neo4j Server shutdown initiated by request");
                    if (communityBootstrapper != null)
                        communityBootstrapper.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            Graph ccgraph = new Neo4j2Graph(communityBootstrapper.getServer().getDatabase().getGraph());

            Vertex echinopsii = ccgraph.addVertex(null);
            echinopsii.setProperty("name", "echinopsii");
            echinopsii.setProperty("type", "SAS");
            echinopsii.setProperty("description", "a free open source holacracy");

            Vertex lbernail = ccgraph.addVertex(null);
            lbernail.setProperty("name", "Laurent Bernaille");
            lbernail.setProperty("type", "human");
            lbernail.setProperty("description", "echinopsii co-founder");

            Vertex mffrench = ccgraph.addVertex(null);
            mffrench.setProperty("name", "Mathilde Ffrench");
            mffrench.setProperty("type", "human");
            mffrench.setProperty("description", "echinopsii co-founder");

            Vertex rscheibert = ccgraph.addVertex(null);
            rscheibert.setProperty("name", "Romain Scheibert");
            rscheibert.setProperty("type", "human");
            rscheibert.setProperty("description", "echinopsii co-founder");

            Vertex sghuge = ccgraph.addVertex(null);
            sghuge.setProperty("name", "Sagar Ghuge");
            sghuge.setProperty("type", "human");
            sghuge.setProperty("description", "ariane contributor");

            ccgraph.addEdge(null, mffrench, echinopsii, "ceo");
            ccgraph.addEdge(null, lbernail, echinopsii, "cto");
            ccgraph.addEdge(null, rscheibert, echinopsii, "cmo");
            ccgraph.addEdge(null, sghuge, echinopsii, "os-contributor");

            ((TransactionalGraph) ccgraph).commit();
        }
    }
}
