package io.github.michaeltleonard.minecrafthexland;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftHexLand extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("onEnable has been invoked!");
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable has been invoked!");
    }
    
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new CustomChunkGenerator();
    }
}
