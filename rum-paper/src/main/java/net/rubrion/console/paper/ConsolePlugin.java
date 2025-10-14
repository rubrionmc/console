package net.rubrion.console.paper;

import net.rubrion.console.common.ConsoleBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public class ConsolePlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        ConsoleBootstrap bootstrap = new ConsoleBootstrap();
        bootstrap.init();
    }

    @Override
    public void onEnable() {

    }


    @Override
    public void onDisable() {

    }

}
