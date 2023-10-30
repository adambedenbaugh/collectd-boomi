package com.boomi.jmx;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class JMXConnection {
    JMXConnector connection;
    static String host;
    static int port;
    private final Logger logger = Logger.getLogger("GLOBAL");


    public JMXConnection(String host, int port) throws IOException {
        JMXConnection.host = host;
        JMXConnection.port = port;
        connection = JMXConnectorFactory
                .newJMXConnector(createConnectionURL(), new HashMap<>());
    }

    private JMXServiceURL createConnectionURL() throws MalformedURLException {
        return new JMXServiceURL("rmi", "", 0, "/jndi/rmi://" + host + ":" + port + "/jmxrmi");
    }

    public void connect() throws IOException {
        connection.connect();
    }

//    private CompositeData setJmxCompositeDataAttribute(String attributeName, String attributeValue) throws MalformedObjectNameException, IOException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
//        ObjectName objectName = new ObjectName(attributeName);
//        return (CompositeData) connection.getMBeanServerConnection().getAttribute(objectName, attributeValue);
//    }

    private Object getJmxObjectAttribute(String attributeName, String attributeValue) throws MalformedObjectNameException, IOException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
        ObjectName objectName = new ObjectName(attributeName);
        return connection.getMBeanServerConnection().getAttribute(objectName, attributeValue);
    }

    private void printCollectdPutVal(String plugin, String typeInstance, String value) {
        String putVal = "PUTVAL " + host + "/jmx-" + plugin + "/gauge-" + typeInstance + " N:" + value;
        System.out.println(putVal);
        logger.info(putVal);
    }

    public void getAtomStatus()
            throws ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
            MBeanException, IOException {
        try {
            String atomStatus = getJmxObjectAttribute("com.boomi.container.services:type=Config", "Status").toString();
            Map<String, String> atomStatusMap = getStringStringMap();
            String defaultStatus = "9";
            String atomStatusMetric = atomStatusMap.getOrDefault(atomStatus, defaultStatus);
            printCollectdPutVal("Config", "Status", atomStatusMetric);
        } catch ( InstanceNotFoundException e) {
            logger.warning("Atom Status InstanceNowFoundException: " + e);
        }
    }

    private static Map<String, String> getStringStringMap() {
        Map<String, String> atomStatusMap = new HashMap<>();
        atomStatusMap.put("RUNNING", "0");
        atomStatusMap.put("INITIALIZING", "1");
        atomStatusMap.put("INIT_UPDATING", "2");
        atomStatusMap.put("PAUSING", "3");
        atomStatusMap.put("PAUSED", "4");
        atomStatusMap.put("PAUSING_FOR_STOP", "5");
        atomStatusMap.put("PAUSED_FOR_STOP", "6");
        atomStatusMap.put("STOPPING", "7");
        atomStatusMap.put("STOPPED", "8");
        return atomStatusMap;
    }

    /*
     * 0 = Good
     * 1 = Warning
     * If the queue server is not running, the attribute will not be found and the script will exit with code 0
     */
    public void getQueueHealthStatus()
            throws MalformedObjectNameException, IOException, ReflectionException, AttributeNotFoundException,
            MBeanException {
        try {
            String healthStatusMetric;
            String healthStatus = getJmxObjectAttribute("com.boomi.container.services:type=PluginService,plugin=QUEUE_SERVER", "HealthStatus").toString();
            logger.info("Queue Health Status: " + healthStatus);
            if (healthStatus.equals("Good")) {
                healthStatusMetric = "0";
            } else {
                healthStatusMetric = "1";
            }
            printCollectdPutVal("QUEUE_SERVER", "HealthStatus", healthStatusMetric);
        } catch ( InstanceNotFoundException e) {
            logger.warning("Queue Health Status InstanceNowFoundException: " + e);
        }
    }

    public void getRestarting ()
            throws ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
            MBeanException, IOException {
        try {
            Boolean restartingStatus = (Boolean) getJmxObjectAttribute("com.boomi.container.services:type=ContainerController", "Restarting");
            String restartingStatusMetric = restartingStatus ? "1" : "0";
            printCollectdPutVal("ContainerController", "Restarting", restartingStatusMetric);
        } catch ( InstanceNotFoundException e) {
            logger.warning("Restarting InstanceNowFoundException: " + e);
        }
    }

    public void getLowMemoryMode ()
            throws ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
            MBeanException, IOException {
        try {
            Boolean lowMemoryStatus = (Boolean) getJmxObjectAttribute("com.boomi.container.services:type=ResourceManager", "LowMemory");
            String lowMemoryStatusMetric = lowMemoryStatus ? "1" : "0";
            printCollectdPutVal("ResourceManager", "LowMemory", lowMemoryStatusMetric);
        } catch ( InstanceNotFoundException e) {
            logger.warning("LowMemory InstanceNowFoundException: " + e);
        }
    }

    public Boolean getClustered ()
            throws ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
            InstanceNotFoundException, MBeanException, IOException {
        return (Boolean) getJmxObjectAttribute( "com.boomi.container.services:type=Config", "Clustered");
    }
    public void getClusterProblem ()
            throws ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
            MBeanException, IOException {
        try {
            if (getClustered()) {
                String healthStatus = (String) getJmxObjectAttribute("com.boomi.container.services:type=ContainerController", "ClusterProblem");
                String healthStatusMetric = healthStatus == null ? "0" : "1";
                printCollectdPutVal("ContainerController", "ClusterProblem", healthStatusMetric);
            }
        } catch ( InstanceNotFoundException e) {
            logger.warning("ClusterProblem InstanceNowFoundException: " + e);
        }
    }

}
