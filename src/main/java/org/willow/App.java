package org.willow;

import com.hsbc.cranker.connector.CrankerConnector;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        App app = new App();
        Willow platform = new Willow(app.getComponentName());
        List<WillowResource> resources = List.of(
                new WillowResource("/willow", "/home/vagrant/gitRepos/WillowHouse/docroot/modules", "willow.js"),
                new WillowResource("/KartRacer", "/home/vagrant/gitRepos/KartRacer/www"),
                new WillowResource("/dist", "/data/dist"),
                new WillowResource("/vue", "/data/dist", "vue.js"),
                new WillowResource("/apps", "/home/vagrant/gitRepos/VirtualHosts/provision/installedApps")
        );
        app.start(platform, resources);
    }

    public String getComponentName() {
        return "willow-static";
    }

    public void start(Willow application, List<WillowResource> resources) {
        LOGGER.info("Starting " + application);

        // Create server
        MuServerBuilder builder = application.serverBuilder();
        resources.forEach(rsrc -> {
            builder.addHandler(rsrc.handler());
        });
        MuServer server = builder.start();

        // Register to cranker
        List<CrankerConnector> connectors = resources
                .stream()
                .map(rsrc -> application.connectorBuilder(server.uri(), rsrc.route()).start())
                .toList();

        // Prepare for shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info(application + " shutting down...");
            connectors.forEach(con -> con.stop(2L, TimeUnit.SECONDS));
            server.stop();
            LOGGER.info("Shut down complete.");
        }));

        application.writePort(server.address().getPort());
        application.writePid(ProcessHandle.current().pid());

        LOGGER.info("{} server started at {}", application, server.uri());
    }
}
