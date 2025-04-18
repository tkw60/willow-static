package org.willow;

import org.willow.platform.Platform;

import java.net.UnknownHostException;
import java.util.List;

public class App {

    public static void main(String[] args) throws UnknownHostException {
        Platform platform = Platform.builder()
                .useVmGuestRouterIfLocalNotUp()
                .build("willow-static");
        new App().start(platform);
    }

    public void start(Platform platform) {
        List<WillowResource> resources = platform.isWindows()
                ? List.of
                (
                        new WillowResource("/willow", "../WillowHouse/docroot/modules", "willow.js"),
                        new WillowResource("/KartRacer", "../KartRacer/www"),
                        new WillowResource("/dist", "D:/Shared/dist"),
                        new WillowResource("/vue", "D:/Shared/dist", "vue.js"),
                        new WillowResource("/apps", "../VirtualHosts/provision/installedApps")
                )
                : List.of
                (
                        new WillowResource("/willow", "/home/vagrant/gitRepos/WillowHouse/docroot/modules", "willow.js"),
                        new WillowResource("/KartRacer", "/home/vagrant/gitRepos/KartRacer/www"),
                        new WillowResource("/dist", "/data/dist"),
                        new WillowResource("/vue", "/data/dist", "vue.js"),
                        new WillowResource("/apps", "/home/vagrant/gitRepos/VirtualHosts/provision/installedApps")
                );
        start(platform, resources);
    }

    public void start(Platform platform, List<WillowResource> resources) {
        platform.serverBuilder()
                .addHandlers(resources.stream().map(WillowResource::handler).toList())
                .addResponseCompleteListener("AccessLog")
                .registerToRouter(resources.stream().map(WillowResource::route).toList())
                .addShutdownHooks()
                .writeProcessFiles()
                .start();
    }
}
