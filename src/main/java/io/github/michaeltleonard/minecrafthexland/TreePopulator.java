package io.github.michaeltleonard.minecrafthexland;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

public class TreePopulator extends BlockPopulator {
    
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        // Determine if trees grow
        if (random.nextBoolean()) {
            // Amount of trees per chunk
            int amount = random.nextInt(4)+1;
            for (int i = 1; i < amount; i++) {
                int X = random.nextInt(15);
                int Z = random.nextInt(15);
                
                // Find the highest block of the (X,Z) coordinate chosen.
                int Y;
                for (Y = world.getMaxHeight()-1; chunk.getBlock(X, Y, Z).getType() == Material.AIR; Y--);
                
                // Use bukkit tree generation
                // Note: Trees only grow on grass
                world.generateTree(chunk.getBlock(X, Y, Z).getLocation(), TreeType.TREE); 
            }
        }
    }
}
