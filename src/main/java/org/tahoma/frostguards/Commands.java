package org.tahoma.frostguards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tahoma.frostguards.utils.FreezeManager;

import java.util.List;

public class Commands implements CommandExecutor {

    private final FrostGuard plugin;

    public Commands(FrostGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Если не указаны аргументы или это команда help - показываем помощь
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelpCommands(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "freeze":
                if (!sender.hasPermission("frostguard.freeze")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                handleFreeze(sender, args);
                break;

            case "radius":
                if (!sender.hasPermission("frostguard.radius")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                handleRadius(sender, args);
                break;

            case "reload":
                if (!sender.hasPermission("frostguard.reload")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                plugin.reloadPluginConfig();
                sendConfigMessage(sender, "messages.reloadConfig");
                break;

            case "unfreeze":
                if (!sender.hasPermission("frostguard.unfreeze")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                handleUnfreeze(sender, args);
                break;

            case "unfreezeall":
                if (!sender.hasPermission("frostguard.unfreezeall")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                handleUnfreezeAll(sender);
                break;

            case "list":
                if (!sender.hasPermission("frostguard.list")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                handleList(sender);
                break;

            case "check":
                if (!sender.hasPermission("frostguard.check")) {
                    sendConfigMessage(sender, "messages.noPermission");
                    return true;
                }
                handleCheck(sender, args);
                break;

            default:
                showHelpCommands(sender);
                break;
        }

        return true;
    }

    private void showHelpCommands(CommandSender sender) {
        List<String> helpList = plugin.getConfig().getStringList("messages.helpCommands");
        if (helpList == null || helpList.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Список команд не настроен.");
            return;
        }
        for (String line : helpList) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    private void handleFreeze(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, "messages.usageFreeze");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sendConfigMessage(sender, "messages.playerNotFound");
            return;
        }

        long freezeTime = -1;
        if (args.length >= 3) {
            try {
                freezeTime = Long.parseLong(args[2]);
            } catch (NumberFormatException ignored) {
                sendConfigMessage(sender, "messages.incorrectFreezeTime");
                return;
            }
        }

        FreezeManager.freezePlayer(target, freezeTime);

        String msgKey = (freezeTime == -1) ? "messages.freezeCasterUnlimited" : "messages.freezeCaster";
        sendConfigMessage(sender, msgKey, "%player%", target.getName(), "%time%", String.valueOf(freezeTime));

        String freezeMsgKey = (freezeTime == -1) ? "messages.freezeTargetUnlimited" : "messages.freezeTarget";
        sendConfigMessage(target, freezeMsgKey, "%time%", String.valueOf(freezeTime));

        target.playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);

        plugin.getFrostLogs().logAction(sender, "заморозил игрока", target.getName(), freezeTime);

        notifyModerators(sender, "messages.notifyFreeze", target.getName());
    }

    private void handleRadius(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendConfigMessage(sender, "messages.onlyInGame");
            return;
        }

        if (args.length < 2) {
            sendConfigMessage(sender, "messages.usageRadius");
            return;
        }

        double radius;
        try {
            radius = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sendConfigMessage(sender, "messages.incorrectNumber");
            return;
        }

        Player player = (Player) sender;
        Location center = player.getLocation();

        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && p.getWorld().equals(player.getWorld())
                    && p.getLocation().distance(center) <= radius) {
                FreezeManager.freezePlayer(p, -1);
                sendConfigMessage(p, "messages.freezeTargetRadius");
                p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
                plugin.getFrostLogs().logAction(sender, "заморозил игрока по радиусу", p.getName(), -1);
                count++;
            }
        }

        String message = plugin.getConfig().getString("messages.freezeRadiusResult", "&aЗаморожено %count% игроков в радиусе %radius%.");
        message = message.replace("%count%", String.valueOf(count));
        message = message.replace("%radius%", String.valueOf(radius));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        notifyModerators(sender, "messages.notifyFreezeRadius", String.valueOf(radius));
    }

    private void handleUnfreeze(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, "messages.usageUnfreeze");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sendConfigMessage(sender, "messages.playerNotFound");
            return;
        }

        if (!FreezeManager.isFrozen(target)) {
            String msg = plugin.getConfig().getString("messages.notFrozen", "&eИгрок %player% не заморожен.");
            msg = msg.replace("%player%", target.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return;
        }

        FreezeManager.unfreezePlayer(target);

        String unfreezeCaster = plugin.getConfig().getString("messages.unfreezeCaster", "&aВы разморозили игрока %player%.");
        unfreezeCaster = unfreezeCaster.replace("%player%", target.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', unfreezeCaster));

        sendConfigMessage(target, "messages.unfreezeTarget");

        target.playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);

        plugin.getFrostLogs().logAction(sender, "разморозил игрока", target.getName(), -1);
        notifyModerators(sender, "messages.notifyUnfreeze", target.getName());
    }

    private void handleUnfreezeAll(CommandSender sender) {
        List<Player> unfrozenPlayers = FreezeManager.unfreezeAll();
        sendConfigMessage(sender, "messages.unfreezeAllCaster");
        for (Player p : unfrozenPlayers) {
            sendConfigMessage(p, "messages.unfreezeTarget");
            p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
        }
        plugin.getFrostLogs().logAction(sender, "разморозил всех игроков", "", -1);
        notifyModerators(sender, "messages.notifyUnfreezeAll", null);
    }

    private void handleList(CommandSender sender) {
        List<String> frozenPlayers = FreezeManager.getFrozenPlayers();
        if (frozenPlayers.isEmpty()) {
            sendConfigMessage(sender, "messages.noFrozenPlayers");
        } else {
            String listHeader = plugin.getConfig().getString("messages.frozenPlayersHeader", "&aСписок замороженных игроков:");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listHeader));
            for (String name : frozenPlayers) {
                sender.sendMessage(ChatColor.AQUA + "- " + name);
            }
        }
    }

    private void handleCheck(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, "messages.usageCheck");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sendConfigMessage(sender, "messages.playerNotFound");
            return;
        }

        if (!FreezeManager.isFrozen(target)) {
            String msg = plugin.getConfig().getString("messages.notFrozen", "&eИгрок %player% не заморожен.");
            msg = msg.replace("%player%", target.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return;
        }

        long remainingTime = FreezeManager.getRemainingFreezeTime(target);
        if (remainingTime == -1) {
            String msgUnlimited = plugin.getConfig().getString("messages.frozenUnlimited", "&aИгрок %player% заморожен безлимитно.");
            msgUnlimited = msgUnlimited.replace("%player%", target.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msgUnlimited));
        } else {
            String msgTime = plugin.getConfig().getString("messages.frozenTime", "&aИгрок %player% заморожен. Оставшееся время: %time% секунд.");
            msgTime = msgTime.replace("%player%", target.getName());
            msgTime = msgTime.replace("%time%", String.valueOf(remainingTime));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msgTime));
        }
    }

    private void sendConfigMessage(CommandSender sender, String path, String... placeholders) {
        String msg = plugin.getConfig().getString(path);
        if (msg == null || msg.isEmpty()) return;

        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length - 1; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void notifyModerators(CommandSender sender, String path, String extra) {
        String msg = plugin.getConfig().getString(path);
        if (msg == null || msg.isEmpty()) return;

        if (extra != null) {
            msg = msg.replace("%extra%", extra);
        }
        msg = msg.replace("%sender%", sender.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("frostguard.freeze") || p.hasPermission("frostguard.radius")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
    }
}
