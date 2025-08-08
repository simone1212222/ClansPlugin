package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCommand extends BaseCommand {

    protected HomeCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan home");
            return true;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        // Controlla se il player e' nel clan
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        // Controlla se esiste una home del clan
        Location home = clan.getHome();
        if (home == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("clan-home-not-set"));
            return true;
        }

        int delay = plugin.getConfigManager().getTeleportDelay();
        final int[] secondsFelt = {delay};

        // Verifica se il tempo del teletrasporto, se e' diverso da 0 invia il messaggio del teleporting
        if (secondsFelt[0] != 0){
            player.sendMessage(plugin.getConfigManager().getMessage("teleporting"));
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            // Se il tempo e' 0 viene teletrasportato, inviando anche il messaggio
            if (secondsFelt[0] <= 0) {
                task.cancel();
                player.teleport(home);
                player.sendMessage(plugin.getConfigManager().getMessage("home-teleported"));
            } else {
                // Senno gli invia il messaggio del tempo rimanente
                player.sendMessage(ChatColor.GREEN + String.valueOf(secondsFelt[0]));
                secondsFelt[0]--;
            }
        }, 20L, 20L);
        return true;
    }
    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}
