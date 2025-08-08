package com.develop.clansPlugin.listeners.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.Territory;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ClaimListener implements Listener {

    private final ClansPlugin plugin;

    public ClaimListener(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        // Controlla se il sistema dei territori e' abilitato e anche is sistema di auto protect nel config
        if (plugin.getConfigManager().isTerritoryEnabled()) return;
        if (plugin.getConfigManager().isAutoProtect()) return;

        // Controlla se il player ha il permesso di admin se c'e' ha return
        Player player = event.getPlayer();
        if (player.hasPermission("clansplugin.admin")) return;

        // Controlla se esiste il territorio
        Territory territory = plugin.getTerritoryManager().getTerritoryAt(event.getBlock().getLocation());
        if (territory == null) return;

        // Controlla se esiste il clan
        Clan clan = plugin.getClanManager().getClan(territory.clanId());
        if (clan == null) return;

        // Controlla nei settings se e' abilitato/disabilitato il build
        if (!clan.hasMember(player.getUniqueId())) {
            if (plugin.getSettingsManager().canBuild(territory.clanId())) {
                event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getMessage("territory-blocked"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Controlla se il sistema dei territori e' abilitato e anche is sistema di auto protect nel config
        if (plugin.getConfigManager().isTerritoryEnabled()) return;
        if (plugin.getConfigManager().isAutoProtect()) return;

        // Controlla se il player ha il permesso di admin se c'e' ha return
        Player player = event.getPlayer();
        if (player.hasPermission("clansplugin.admin")) return;

        // Controlla se esiste il territorio
        Territory territory = plugin.getTerritoryManager().getTerritoryAt(event.getBlock().getLocation());
        if (territory == null) return;

        // Controlla se esiste il clan
        Clan clan = plugin.getClanManager().getClan(territory.clanId());
        if (clan == null) return;

        // Controlla nei settings se e' abilitato/disabilitato il build
        if (!clan.hasMember(player.getUniqueId())) {
            if (plugin.getSettingsManager().canBuild(territory.clanId())) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getMessage("territory-blocked"));
            }
            }
        }


    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Controlla se il sistema dei territori e' abilitato e anche is sistema di auto protect nel config
        if (plugin.getConfigManager().isTerritoryEnabled()) return;
        if (plugin.getConfigManager().isAutoProtect()) return;

        // Controlla se il damager e' un player
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Controlla se esiste il territorio
        Territory territory = plugin.getTerritoryManager().getTerritoryAt(victim.getLocation());
        if (territory == null) return;

        // Controlla se esiste il clan
        var clan = plugin.getClanManager().getPlayerClan(attacker.getUniqueId());
        if (clan == null) return;

        // Controlla nei settings se e' abilitato/disabilitato il pvp
        if (!plugin.getSettingsManager().canPvp(territory.clanId())) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getConfigManager().getMessage("pvp-disabled"));
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // Controlla se il sistema dei territori e' abilitato e anche is sistema di auto protect nel config
        if (plugin.getConfigManager().isTerritoryEnabled()) return;
        if (plugin.getConfigManager().isAutoProtect()) return;

        // Verifica se il mobs e' un monster
        if (!(event.getEntity() instanceof Monster)) return;

        // Controlla se esiste il territorio
        Territory territory = plugin.getTerritoryManager().getTerritoryAt(event.getLocation());
        if (territory == null) return;

        // Controlla nei settings se e' abilitato/disabilitato il mob spawning
        if (!plugin.getSettingsManager().canMobSpawn(territory.clanId())) {
            event.setCancelled(true);
        }
    }
}
