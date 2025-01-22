package org.tahoma.frostguards;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FrostLogs {

    private final FrostGuard plugin;
    private final File logFile;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FrostLogs(FrostGuard plugin) {
        this.plugin = plugin;
        File directory = new File(plugin.getDataFolder(), "logs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        this.logFile = new File(directory, "logs.txt");
    }

    public void logAction(CommandSender sender, String action, String targetName, long time) {
        String senderName = (sender instanceof Player) ? sender.getName() : "Консоль";
        String timeInfo = (time == -1) ? "безлимит" : time + " сек";

        String message = String.format("[%s] %s %s %s (время: %s)",
                sdf.format(new Date()), senderName, action, targetName, timeInfo);
        logToFile(message);

        // Опциональный лог в консоль
        boolean logToConsole = plugin.getConfig().getBoolean("log-to-console", true);
        if (logToConsole) {
            plugin.getLogger().info(message);
        }
    }

    public void logToFile(String text) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println("[" + sdf.format(new Date()) + "] " + text);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить лог в файл: " + e.getMessage());
        }
    }
}