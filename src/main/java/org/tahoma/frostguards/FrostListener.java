package org.tahoma.frostguards;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.tahoma.frostguards.utils.FreezeManager;

public class FrostListener implements Listener {

    private final FrostGuard plugin;

    public FrostListener(FrostGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (FreezeManager.isFrozen(player)) {
            // Отмена перемещения — возвращаем игрока на прежнюю позицию
            event.setTo(event.getFrom());

            // Визуальный эффект (например, частицы снега)
            player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(), 5, 0.5, 1, 0.5, 0.01);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (FreezeManager.isFrozen(player)) {
            boolean disallowCommands = plugin.getConfig().getBoolean("blocked-actions.commands", true);
            if (disallowCommands) {
                String msg = plugin.getConfig().getString("messages.blockedCommand", "&cВы не можете использовать команды во время заморозки!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (FreezeManager.isFrozen(player)) {
            boolean disallowBlockBreak = plugin.getConfig().getBoolean("blocked-actions.block-break", true);
            if (disallowBlockBreak) {
                String msg = plugin.getConfig().getString("messages.blockedBlockBreak", "&cВы не можете ломать блоки во время заморозки!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (FreezeManager.isFrozen(player)) {
            boolean disallowBlockPlace = plugin.getConfig().getBoolean("blocked-actions.block-place", true);
            if (disallowBlockPlace) {
                String msg = plugin.getConfig().getString("messages.blockedBlockPlace", "&cВы не можете ставить блоки во время заморозки!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (FreezeManager.isFrozen(player)) {
            boolean disallowInteraction = plugin.getConfig().getBoolean("blocked-actions.interaction", true);
            if (disallowInteraction) {
                String msg = plugin.getConfig().getString("messages.blockedInteraction", "&cВы не можете взаимодействовать во время заморозки!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (FreezeManager.isFrozen(player)) {
                boolean disallowInventory = plugin.getConfig().getBoolean("blocked-actions.inventory", true);
                if (disallowInventory) {
                    String msg = plugin.getConfig().getString("messages.blockedInventory", "&cВы не можете использовать инвентарь во время заморозки!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Если игрок заморожен и вышел, можно добавить логику (например, логирование)
        if (FreezeManager.isFrozen(player)) {
            plugin.getFrostLogs().logToFile("Игрок " + player.getName() + " вышел из игры во время заморозки.");
        }
    }
}