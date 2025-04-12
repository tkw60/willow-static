package manual;

import org.willow.App;
import org.willow.Willow;
import org.willow.WillowResource;

import java.util.List;

public class RunWithLocalRouter {

    public static void main(String[] args) {
        App app = new App();
        Willow platform = new Willow(app.getComponentName(), 23000);
        List<WillowResource> resources = List.of(
                new WillowResource("/willow", "../WillowHouse/docroot/modules", "willow.js"),
                new WillowResource("/KartRacer", "../KartRacer/www"),
                new WillowResource("/dist", "D:/Shared/dist"),
                new WillowResource("/vue", "D:/Shared/dist", "vue.js"),
                new WillowResource("/apps", "../VirtualHosts/provision/installedApps")
        );
        app.start(platform, resources);
    }
}
