package io.github.michaeltleonard.minecrafthexland;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

public class GrassPopulator extends BlockPopulator {
    
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int MAX_GRASS = 16;
        
        // Grow grass in every chunk
        int amount = random.nextInt(MAX_GRASS);
        for (int i = 1; i < amount; i++) {
            int X = random.nextInt(15);
            int Z = random.nextInt(15);
            
            // Find the highest block of the (X,Z) coordinate chosen.
            int Y;
            for (Y = world.getMaxHeight()-1; chunk.getBlock(X, Y, Z).getType() == Material.AIR; Y--);
            
            // Add grass regardless of what is underneath
            chunk.getBlock(X, Y+1, Z).setType(Material.GRASS);
        }
    }
}
