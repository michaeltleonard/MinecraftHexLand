package io.github.michaeltleonard.minecrafthexland;

import java.lang.reflect.Field;

import net.minecraft.server.v1_15_R1.ChunkGenerator;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
//import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;


public final class MinecraftHexLand extends JavaPlugin implements Listener {
    String WORLD_NAME = "world";
    
    @Override
    public void onEnable() {
        Bukkit.getLogger().info("onEnable has been invoked!");
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getLogger().info("Events registered");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("onDisable has been invoked!");
    }
    
//    @Override
//    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
//        return new CustomChunkGenerator();
//    }
    
//    @EventHandler(priority = EventPriority.HIGH)
//    public void onWorldInit(WorldInitEvent e)
//    {
//        Bukkit.getLogger().info("WorldInitEvent called");
//    }
//
//    @EventHandler(priority = EventPriority.HIGH)
//    public void onWorldLoad(WorldLoadEvent e)
//    {
//        Bukkit.getLogger().info("WorldLoadEvent called");
//    }
    
    // Adapted from 2019 Marvin (DerFrZocker)
    // Custom-Ore-Generator WorldHandler_v1_15_R1
    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(final WorldInitEvent event) {
        Bukkit.getLogger().info("WorldInitEvent has been invoked!");
        Bukkit.getLogger().info("Bukkit.getWorlds() " + Bukkit.getWorlds());
        Bukkit.getLogger().info("Current world: " + event.getWorld().getEnvironment());
        //World world = Bukkit.getWorlds().get(0);
        
        // Only hook the Overworld
        // There might be a way to tell the dimension directly from the world
        if (event.getWorld().getEnvironment() != Environment.NORMAL) {
            Bukkit.getLogger().info("Ignoring hook to world: " + event.getWorld().getName());
            return;
        }
        
        Bukkit.getLogger().info("try to hook in to world " + event.getWorld().getName());

        // checking if the Bukkit world is an instance of CraftWorld, if not return
        if (!(event.getWorld() instanceof CraftWorld)) {
           Bukkit.getLogger().info("can't hook into world: " + event.getWorld().getName() + ", because World is not an instance of CraftWorld");
            return;
        }

        final CraftWorld world = (CraftWorld) event.getWorld();

        try {

            // get the playerChunkMap where the ChunkGenerator is store, that we need to override
            final PlayerChunkMap playerChunkMap = world.getHandle().getChunkProvider().playerChunkMap;

            // get the ChunkGenerator from the PlayerChunkMap
            final Field ChunkGeneratorField = PlayerChunkMap.class.getDeclaredField("chunkGenerator");
            ChunkGeneratorField.setAccessible(true);
            final Object chunkGeneratorObject = ChunkGeneratorField.get(playerChunkMap);

            // return, if the chunkGeneratorObject is not an instance of ChunkGenerator
            if(!(chunkGeneratorObject instanceof ChunkGenerator)) {
                Bukkit.getLogger().info("can't hook into world: " + world.getName() + ", because object is not an instance of ChunkTaskScheduler");
                return;
            }

            final ChunkGenerator<?> chunkGenerator = (ChunkGenerator<?>) chunkGeneratorObject;

            // create a new ChunkOverrider
            final ChunkOverrider<?> overrider = new ChunkOverrider<>(chunkGenerator);

            // set the ChunkOverrider to the PlayerChunkMap
            ChunkGeneratorField.set(playerChunkMap, overrider);

        } catch (final Exception e) {
            throw new RuntimeException("Unexpected error while hook into world " + world.getName(), e);
        }
    }
}
