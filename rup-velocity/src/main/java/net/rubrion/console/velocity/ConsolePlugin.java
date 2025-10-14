package net.rubrion.console.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import java.nio.file.Path;

@Plugin(id = "template", name = "Template", version = "${version}", authors = "${authors}")
public class ConsolePlugin {

    private final Path dataDirectory;

    @Inject
    public ConsolePlugin(@DataDirectory Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Inject
    public void onEnable() {

    }

    @Inject
    public void onDisable() {

    }

}
