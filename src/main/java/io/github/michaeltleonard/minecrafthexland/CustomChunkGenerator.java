package io.github.michaeltleonard.minecrafthexland;

import java.util.Random;
import java.lang.Math;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class CustomChunkGenerator extends ChunkGenerator {
    int currentHeight = 50;
    int currentBiome = -1;

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
    	SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        ChunkData chunk = createChunkData(world);
        //generator.setScale(0.005D); // Tutorial Default
        generator.setScale(0D); // Super Flat
        
        
        // Populate chunk with blocks
        //Bukkit.getLogger().info("Biome of chunk corner: " + getColor(chunkX*16, chunkZ*16));
        for (int X = 0; X < 16; X++)
            for (int Z = 0; Z < 16; Z++) {
            	currentHeight = (int) (generator.noise(chunkX*16+X, chunkZ*16+Z, 0.5D, 0.5D)*15D+50D);
                currentBiome = getColor(chunkX*16+X, chunkZ*16+Z);
                
                if (currentBiome == 0) {
                	// 0, Yellow, Sand
                	chunk.setBlock(X, currentHeight, Z, Material.SAND);
                } else if (currentBiome == 1) {
                	// 1, Blue, Lapis
                	chunk.setBlock(X, currentHeight, Z, Material.LAPIS_BLOCK);
                } else if (currentBiome == 2) {
                	// 2, Red, Red Sandstone
                	chunk.setBlock(X, currentHeight, Z, Material.RED_SANDSTONE);
                } else {
                	chunk.setBlock(X, currentHeight, Z, Material.BEDROCK);
                }
                for (int i = currentHeight-1; i > 0; i--)
                    chunk.setBlock(X, i, Z, Material.STONE);
                chunk.setBlock(X, 0, Z, Material.BEDROCK);
                
                // Set Biome
                for (int Y = 0; Y < 256; Y++) {
                	if (currentBiome == 0) {
                    	// 0, Yellow, Sand, Desert
                		biome.setBiome(chunkX*16+X, Y, chunkZ*16+Z, Biome.DESERT);
                    } else if (currentBiome == 1) {
                    	// 1, Blue, Lapis, Ocean
                    	biome.setBiome(chunkX*16+X, Y, chunkZ*16+Z, Biome.DEEP_OCEAN);
                    } else if (currentBiome == 2) {
                    	// 2, Red, Red Sandstone, Badland
                    	biome.setBiome(chunkX*16+X, Y, chunkZ*16+Z, Biome.BADLANDS);
                    } else {
                    	biome.setBiome(chunkX*16+X, Y, chunkZ*16+Z, Biome.TAIGA_MOUNTAINS);
                    }
                }
            }
        
        return chunk;
    }
    
    public int getColor(int x, int y) {
    	int SIDE_LENGTH = 32;
    	double minDistance = -1;
    	int minColor = -1;
    	
    	// Step 0: Convert x,y to w,h
    	// Where "w" is actually 1/2 width of hexagon
    	// And "h" is actually 1/4 height of hexagon
    	double w = 2.0D * x / (SIDE_LENGTH * Math.sqrt(3));
    	double h = 2.0D * y / SIDE_LENGTH;
    	
    	//Bukkit.getLogger().info("x: " + x + ", y: " + y);
    	//Bukkit.getLogger().info("w: " + w + ", h: " + h);
    	
    	// Step 1: Round to nearest candidate values
    	// Centers occur on every integer "w" and every third "h",
    	// But since the centers are offset, not every point with
    	// those coordinates will be valid.
    	int wRounded = (int) Math.round(w);
    	int hRounded = 3 * ((int) Math.round(h/3.0));
    	
    	//Bukkit.getLogger().info("w_rounded: " + wRounded + ", h_rounded: " + hRounded);
    	
    	// Step 2: Populate set of nearest center candidates
    	//Bukkit.getLogger().info("Candidate centers:");
    	
    	int wArray[] = {wRounded+1, wRounded, wRounded-1};
    	int hArray[] = {hRounded+3, hRounded, hRounded-3};
    	
    	boolean valid = false;
    	
    	for (int wIndex = 0; wIndex < wArray.length; wIndex++) 
    		for (int hIndex = 0; hIndex < hArray.length; hIndex++) {
    			int wValue = wArray[wIndex];
    			int hValue = hArray[hIndex];
    			//Bukkit.getLogger().info("(" + wValue + "," + hValue + ")");
    			
    			// Step 2.5: Compute valid centers
    			// w is odd: h%6 == 0
    			// w is even: (h-3)%6 == 0
    			valid = false;
    			if (((wValue % 2) == 0) && ((hValue % 6) == 0)) {
    				valid = true; }
    			if (((wValue % 2) != 0) && (((hValue-3) % 6) == 0)) {
    				valid = true; }
    			//Bukkit.getLogger().info("(" + wValue + "," + hValue + ") " + valid);
    			
    			// Step 3: Compute distance to original (w,h)
    			if (valid) {
    				
    				// Distance = sqrt( (wDelta)^2 + (hDelta/sqrt(3))^2) )
    				double wDelta = w - (double) wValue;
    				double hDelta = h - (double) hValue;
    				double distance = Math.sqrt(wDelta*wDelta + (hDelta*hDelta)/3);
    				
    				// Determine if center is closest to (w,h)
    				if ((distance < minDistance) || (minDistance < 0)) {
    					minDistance = distance;
    					
    					// Assign a color (3 colors for now)
    					if ((wValue % 3) == 0)
    						minColor = 0; // Yellow, Sand
    					else if (((wValue % 3) == 1) || ((wValue % 3) == -2))
    						minColor = 1; // Blue, Lapis
    					else if (((wValue % 3) == 2) || ((wValue % 3) == -1))
    						minColor = 2; // Red, Red Sandstone
    				}
    				
    				//Bukkit.getLogger().info("(" + wValue + "," + hValue + ") " + valid + " " + distance);
    			}
    			//else
    			//	Bukkit.getLogger().info("(" + wValue + "," + hValue + ") ");
        	}
    	
    	//Bukkit.getLogger().info("Closest: minDistance = " + minDistance + ", color = " + minColor);
    	return minColor;
    }
}