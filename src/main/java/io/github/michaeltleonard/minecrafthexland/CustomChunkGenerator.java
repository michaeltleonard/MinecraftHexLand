package io.github.michaeltleonard.minecrafthexland;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class CustomChunkGenerator extends ChunkGenerator {
    int currentHeight = 50;

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
    	SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        ChunkData chunk = createChunkData(world);
        generator.setScale(0.005D);
        
        // Set Biome
        for (int X = 0; X < 16; X++)
            for (int Z = 0; Z < 16; Z++) 
            	for (int Y = 0; Y < 256; Y++)
            		biome.setBiome(chunkX*16+X, Y, chunkZ*16+Z, Biome.DESERT);
        
        // Populate chunk with blocks
        for (int X = 0; X < 16; X++)
            for (int Z = 0; Z < 16; Z++) {
                currentHeight = (int) (generator.noise(chunkX*16+X, chunkZ*16+Z, 0.5D, 0.5D)*15D+50D);
                if (((chunkX % 2) == 0) && ((chunkZ % 2) == 0)) {
                	chunk.setBlock(X, currentHeight, Z, Material.GRASS);
                } else if (((chunkX % 2) == 0) && ((chunkZ % 2) != 0)) {
                	chunk.setBlock(X, currentHeight, Z, Material.STONE);
                } else if (((chunkX % 2) != 0) && ((chunkZ % 2) == 0)) {
                	chunk.setBlock(X, currentHeight, Z, Material.COBBLESTONE);
                } else if (((chunkX % 2) != 0) && ((chunkZ % 2) != 0)) {
                	chunk.setBlock(X, currentHeight, Z, Material.SAND);
                }
                for (int i = currentHeight-1; i > 0; i--)
                    chunk.setBlock(X, i, Z, Material.STONE);
                chunk.setBlock(X, 0, Z, Material.BEDROCK);
            }
        return chunk;
    }
}