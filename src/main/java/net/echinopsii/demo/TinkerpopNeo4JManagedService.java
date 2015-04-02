package net.echinopsii.demo;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import org.neo4j.kernel.logging.BufferingConsoleLogger;
import org.neo4j.kernel.logging.DefaultLogging;
import org.neo4j.kernel.logging.Logging;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.NeoServer;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.PropertyFileConfigurator;
import org.neo4j.server.configuration.validation.DatabaseLocationMustBeSpecifiedRule;
import org.neo4j.server.configuration.validation.Validator;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;

public class TinkerpopNeo4JManagedService implements ManagedService {

    private final static Logger log = LoggerFactory.getLogger(TinkerpopNeo4JManagedService.class);
    private static final String NEO4J_CONFIG_FILE_PATH_PROPS = "neo4j.configfile";

    private Bootstrapper bootstrapper = Bootstrapper.loadMostDerivedBootstrapper();
    private Configurator configurator ;
    private NeoServer    server ;
    private Thread       shutdownHook ;

    public void stop() {
        if ( server != null )
            server.stop();
    }

    @Override
    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("updated : {}", new Object[]{(dictionary==null)?"null conf":dictionary.toString()});
        if (dictionary!=null) {
            String configFilePath = (String) dictionary.get(NEO4J_CONFIG_FILE_PATH_PROPS);
            log.debug("Neo4J server config file path: {}", new Object[]{configFilePath});
            File configFile = new File(configFilePath);
            log.debug("Create configuration from {}", configFilePath);
            BufferingConsoleLogger console = new BufferingConsoleLogger();
            configurator = new PropertyFileConfigurator(new Validator(new DatabaseLocationMustBeSpecifiedRule()), configFile, console);
            Logging logging = DefaultLogging.createDefaultLogging(configurator.getDatabaseTuningProperties());
            log.debug("Create neo4j server");
            server = new CommunityNeoServer(configurator, logging);
            log.debug("Start neo4j server");
            server.start();

            shutdownHook = new Thread() {
                @Override
                public void run() {
                    log.info("Neo4j Server shutdown initiated by request");
                    if (server != null)
                        server.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            Graph ccgraph = new Neo4j2Graph(server.getDatabase().getGraph());

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
