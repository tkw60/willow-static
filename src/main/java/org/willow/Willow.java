package org.willow;

import com.hsbc.cranker.connector.CrankerConnectorBuilder;
import com.hsbc.cranker.connector.RouterEventListener;
import com.hsbc.cranker.connector.RouterRegistration;
import io.muserver.MuServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Willow {

    public static boolean isProduction() {
        try {
            String thisHost = InetAddress.getLocalHost().getHostName();
            if (thisHost == null) {
                return false;
            } else {
                String productionHost = "cedar";
                return thisHost.toLowerCase().startsWith(productionHost)
                        && (thisHost.length() == productionHost.length() || thisHost.charAt(productionHost.length()) == '.');
            }
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private final String component;
    private final int    routerWebPort;

    public Willow(String component) {
        this(component, 22000);
    }

    public Willow(String component, int routerWebPort) {
        this.component = component;
        this.routerWebPort = routerWebPort;
    }

    @Override
    public String toString() {
        return component;
    }

    public String route() {
        String prefix = "willow-";
        return component.startsWith(prefix) ? component.substring(prefix.length()) : component;
    }

    public String contextPath() {
        return "/" + route();
    }

    protected int getRouterWebPort() {
        return routerWebPort;
    }

    protected int getRouterRegistrationPort() {
        return getRouterWebPort() + 1;
    }

    protected int getPriorityRegistrationPort() {
        return getRouterRegistrationPort() + 1;
    }

    public MuServerBuilder routerWebServerBuilder() {
        return serverBuilder(getRouterWebPort());
    }

    public MuServerBuilder routerRegistrationServerBuilder() {
        return serverBuilder(getRouterRegistrationPort());
    }

    public MuServerBuilder routerPriorityRegistrationServerBuilder() {
        return serverBuilder(getPriorityRegistrationPort());
    }

    public MuServerBuilder serverBuilder() {
        return serverBuilder(0);
    }

    private MuServerBuilder serverBuilder(int port) {
        final Logger logger = LoggerFactory.getLogger("AccessLog");
        return MuServerBuilder.muServer()
                .withHttpPort(port)
                .addResponseCompleteListener(info -> {
                    logger.info("Sent {} / {}s for {} {}{}",
                            info.response().status(),
                            String.format("%6.3f", info.duration() / 1000.0),
                            info.request().method(),
                            info.request().uri().toString(),
                            Set.of(302,301).contains(info.response().status()) ? " ->" : ""
                    );
                });
    }

    public CrankerConnectorBuilder connectorBuilder(URI uri) {
        return connectorBuilder(uri, route());
    }

    public CrankerConnectorBuilder connectorBuilder(URI uri, String route) {
        final Logger logger = LoggerFactory.getLogger("ConnectorLog");
        return CrankerConnectorBuilder.connector()
                .withRouterLookupByDNS(URI.create("ws://localhost:" + getRouterRegistrationPort()))
                .withComponentName(component)
                .withRoute(route)
                .withTarget(uri)
                .withRouterRegistrationListener(new RouterEventListener() {
                    public void onRegistrationChanged(ChangeData data) {
                        logger.info("Router registration changed: " + data);
                    }
                    public void onSocketConnectionError(RouterRegistration router, Throwable exception) {
                        logger.warn("Error connecting to " + router);
                    }
                });
    }

    public void writePort(int port) {
        ProcessFiles.PORT_FILE.write(component, port);
    }

    public void writePid(Long pid) {
        ProcessFiles.PID_FILE.write(component, pid);
    }

    private enum ProcessFiles {
        PORT_FILE,
        PID_FILE;

        public void write(String component, long value) {
            if (System.getenv().containsKey(toString())) {
                try {
                    Path path = Paths.get(System.getenv(toString()));
                    Files.writeString(path, Long.toString(value));
                } catch (Throwable t) {
                    LoggerFactory.getLogger(component).error("Exiting - failed to write " + this);
                    System.exit(1);
                }
            }
        }
    }
}
