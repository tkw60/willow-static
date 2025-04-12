package manual;

import org.willow.App;
import org.willow.WillowResource;

import java.util.List;

public class RunLocal {

    public static void main(String[] args) {
        new App().start(
                List.of(
                        new WillowResource("/willow", "../WillowHouse/docroot/modules", "willow.js"),
                        new WillowResource("/KartRacer", "../KartRacer/www"),
                        new WillowResource("/dist", "D:/Shared/dist"),
                        new WillowResource("/vue", "D:/Shared/dist", "vue.js"),
                        new WillowResource("/apps", "../VirtualHosts/provision/installedApps")
                ));
    }
}
