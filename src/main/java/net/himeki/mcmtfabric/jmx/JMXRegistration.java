package net.himeki.mcmtfabric.jmx;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class JMXRegistration {

    public static void register() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName debugName = new ObjectName("org.jmt.mcmt:type=MCMTDebug");
            MCMTDebug debugBean = new MCMTDebug();
            mbs.registerMBean(debugBean, debugName);
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
