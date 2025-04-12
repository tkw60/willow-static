package org.willow;

import io.muserver.ContextHandlerBuilder;
import io.muserver.MuHandler;
import io.muserver.handlers.ResourceHandlerBuilder;

public record WillowResource(String webPath, String path, String defaultPath) {

    public WillowResource(String webPath, String path) {
        this(webPath, path, null);
    }

    public MuHandler handler() {
        ResourceHandlerBuilder builder = ResourceHandlerBuilder.fileOrClasspath(path, webPath);
        if (defaultPath != null) {
            builder.withDefaultFile(defaultPath);
        }
        return ContextHandlerBuilder.context(webPath).addHandler(builder).build();
    }

    public String route() {
        return webPath.substring(1);
    }
}
