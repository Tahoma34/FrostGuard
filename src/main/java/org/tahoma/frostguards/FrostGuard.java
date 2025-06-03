package org.tahoma.frostguards;

import org.bukkit.plugin.java.JavaPlugin;

public final class FrostGuard extends JavaPlugin {

    private static FrostGuard instance;
    private FrostListener frostListener;
    private FrostLogs frostLogs;

    @Override
    public void onEnable() {
        instance = this;


        saveDefaultConfig();

        getConfig().options().copyDefaults(true);
        saveConfig();

        this.frostLogs = new FrostLogs(this);
        frostListener = new FrostListener(this);
        getServer().getPluginManager().registerEvents(frostListener, this);

        getCommand("fg").setExecutor(new Commands(this));

        getLogger().info("FrostGuard плагин активирован!");
        frostLogs.logToFile("Плагин FrostGuard активирован");
    }

    @Override
    public void onDisable() {
        getLogger().info("FrostGuard плагин выключен.");
        frostLogs.logToFile("Плагин FrostGuard выключен");
    }

    public static FrostGuard getInstance() {
        return instance;
    }

    public FrostListener getFrostListener() {
        return frostListener;
    }

    public FrostLogs getFrostLogs() {
        return frostLogs;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        getFrostLogs().logToFile("Конфигурация плагина перезагружена");
        getLogger().info("Конфигурация плагина перезагружена");
    }
}
