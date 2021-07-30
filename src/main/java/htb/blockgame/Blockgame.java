package htb.blockgame;

import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Blockgame extends JavaPlugin implements Listener {
    Map<UUID, BlockInformation> lastPlacedBlock = new HashMap<>();
    Map<UUID, Instant> lastPlacedInstants = new HashMap<>();
    long cooldown;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getWorld(getServer().getWorlds().get(0).getUID()).setDifficulty(Difficulty.PEACEFUL);
        getServer().getWorld(getServer().getWorlds().get(0).getUID()).setGameRule(GameRule.MOB_GRIEFING, false);
        getServer().getWorld(getServer().getWorlds().get(0).getUID()).setGameRule(GameRule.DO_FIRE_TICK, false);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent arg) {
        UUID id = arg.getPlayer().getUniqueId();

        if (!lastPlacedInstants.containsKey(id))
            lastPlacedInstants.put(id, Instant.now());
        else
            lastPlacedInstants.replace(id, Instant.now());

        cooldown = Math.round(getServer().getOnlinePlayers().size() / 8 + 15);
    }

    @EventHandler
    public void onPlayerQuit() {
        cooldown = Math.round(getServer().getOnlinePlayers().size() / 8 + 15);
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent arg) {
        UUID id = arg.getPlayer().getUniqueId();

        BlockInformation blockInformation = new BlockInformation();
        blockInformation.setX(arg.getBlock().getX());
        blockInformation.setY(arg.getBlock().getY());
        blockInformation.setZ(arg.getBlock().getZ());

        long offsetSinceLastPlaced = Duration.between(lastPlacedInstants.get(id), Instant.now()).toSeconds();

        if (blockInformation == lastPlacedBlock.get(id)) {
            return;
        }
        if (!(offsetSinceLastPlaced > cooldown)) {
            arg.setCancelled(true);
            arg.getPlayer().sendTitle("Not Yet!", "Cooldown: " + (cooldown-offsetSinceLastPlaced) + " seconds left.", 1, 7, 1);
        }
        else {
            lastPlacedInstants.replace(id, Instant.now());

            if (!lastPlacedBlock.containsKey(id))
                lastPlacedBlock.put(id, blockInformation);
            else
                lastPlacedBlock.replace(id, blockInformation);
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent arg){
        UUID id = arg.getPlayer().getUniqueId();

        BlockInformation blockInformation = new BlockInformation();
        blockInformation.setX(arg.getBlock().getX());
        blockInformation.setY(arg.getBlock().getY());
        blockInformation.setZ(arg.getBlock().getZ());

        long offsetSinceLastPlaced = Duration.between(lastPlacedInstants.get(id), Instant.now()).toSeconds();

        if (blockInformation == lastPlacedBlock.get(id)) {
            return;
        }
        if (!(offsetSinceLastPlaced > cooldown)) {
            arg.setCancelled(true);
            arg.getPlayer().sendTitle("Not Yet!", "Cooldown: " + (cooldown-offsetSinceLastPlaced) + " seconds left.", 1, 7, 1);
        }
        else {
            lastPlacedInstants.replace(id, Instant.now());
        }
    }
}
