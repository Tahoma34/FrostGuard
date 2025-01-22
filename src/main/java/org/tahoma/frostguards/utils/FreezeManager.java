package org.tahoma.frostguards.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.tahoma.frostguards.FrostGuard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager {

    private static final Map<UUID, Long> frozenPlayers = new ConcurrentHashMap<>();

    public static void freezePlayer(Player player, long durationSeconds) {
        long endTime;
        if (durationSeconds == -1) {
            endTime = -1;
        } else {
            endTime = System.currentTimeMillis() + durationSeconds * 1000;
        }
        frozenPlayers.put(player.getUniqueId(), endTime);

        if (durationSeconds > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isFrozen(player)) {
                        long storedEndTime = frozenPlayers.get(player.getUniqueId());
                        if (storedEndTime != -1 && storedEndTime <= System.currentTimeMillis()) {
                            handleEndOfFreeze(player);
                            this.cancel();
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(FrostGuard.getInstance(), 20L, 20L);
        }
    }

    private static void handleEndOfFreeze(Player player) {
        unfreezePlayer(player);

        // Уведомление игрока
        String endMessage = FrostGuard.getInstance().getConfig().getString("messages.freezeEnded", "&aЗаморозка завершена!");
        if (endMessage != null && !endMessage.isEmpty()) {
            player.sendMessage(formatMessage(endMessage.replace("%player%", player.getName())));
        }

        List<String> commands = FrostGuard.getInstance().getConfig().getStringList("end-freeze-commands");
        for (String command : commands) {

            String formattedCommand = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
        }
    }

    public static void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
    }

    public static List<Player> unfreezeAll() {
        List<Player> affectedPlayers = new ArrayList<>();
        for (UUID uuid : frozenPlayers.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                affectedPlayers.add(p);
            }
        }
        frozenPlayers.clear();
        return affectedPlayers;
    }

    public static boolean isFrozen(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }

    public static long getRemainingFreezeTime(Player player) {
        if (!isFrozen(player)) return 0;
        long endTime = frozenPlayers.get(player.getUniqueId());
        if (endTime == -1) return -1;

        return Math.max((endTime - System.currentTimeMillis()) / 1000, 0);
    }

    public static List<String> getFrozenPlayers() {
        List<String> list = new ArrayList<>();
        for (UUID uuid : frozenPlayers.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                list.add(p.getName());
            }
        }
        return list;
    }

    private static String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}