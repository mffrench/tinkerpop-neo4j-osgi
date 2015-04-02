package net.echinopsii.demo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class TinkerpopNeo4JActivator implements BundleActivator {

    private final static Logger log = LoggerFactory.getLogger(TinkerpopNeo4JManagedService.class);
    private ServiceRegistration tinkerpopNeo4jServiceRegistration;
    private TinkerpopNeo4JManagedService tinkerpopNeo4jManagedService = new TinkerpopNeo4JManagedService();

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Dictionary props = new Hashtable();
        log.debug("Starting net.echinopsii.demo.TinkerpopNeo4JManagedService");
        props.put("service.pid", "net.echinopsii.demo.TinkerpopNeo4JManagedService");
        tinkerpopNeo4jServiceRegistration = bundleContext.registerService(ManagedService.class.getName(), tinkerpopNeo4jManagedService, props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (tinkerpopNeo4jServiceRegistration !=null) {
            tinkerpopNeo4jManagedService.stop();
            tinkerpopNeo4jManagedService =null;
            tinkerpopNeo4jServiceRegistration.unregister();
            tinkerpopNeo4jServiceRegistration =null;
        }
    }

}
