/*
 * MIT License
 *
 * Copyright (c) 2019 Marvin (DerFrZocker)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.michaeltleonard.minecrafthexland;

import net.minecraft.server.v1_15_R1.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class ChunkOverrider<C extends GeneratorSettingsDefault> extends ChunkGenerator<C> {

    private final static Method getBiome;

    static {
        try {
            getBiome = ChunkGenerator.class.getDeclaredMethod("getBiome", BiomeManager.class, BlockPosition.class);
            getBiome.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unexpected Error while get Method");
        }
    }
    
    private final ChunkGenerator<C> parent;

    public ChunkOverrider(final ChunkGenerator<C> parent) {
        super(DummyGeneratorAccess.INSTANCE, null, null);
        this.parent = parent;
    }

    @Override
    public void createBiomes(IChunkAccess ichunkaccess) {
        // Run native biome creation to populate ichunkaccess with everything necessary
        parent.createBiomes(ichunkaccess);
        
        // Get the chunk coordinate object from ichunkaccess
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
        Bukkit.getLogger().info("In createBiomes: ChunkCoordIntPair: " + chunkcoordintpair.toString());
        
        // Convert chunk coordinate object to block position (x,z)
        // NMS functions of net.minecraft.world.level.ChunkPos
        // int getMinBlockX() -> d
        // int getMinBlockZ() -> e
        int blockX = chunkcoordintpair.d();
        int blockZ = chunkcoordintpair.e();
        //Bukkit.getLogger().info("In createBiomes: X: " + blockX);
        //Bukkit.getLogger().info("In createBiomes: Z: " + blockZ);
        
        // Get the vanilla BiomeStorage object from ichunkaccess to be modified
        //Bukkit.getLogger().info("Get default BiomeStorage from ichunkaccess");
        BiomeStorage currentBiomeStorage = ichunkaccess.getBiomeIndex();
        
        //Bukkit.getLogger().info("Get BiomeStorage array size for future use:");
        //int[] biomeArray = currentBiomeStorage.a();
        //Bukkit.getLogger().info("" + biomeArray.length);
        
        // Convert to a hexagon grid with getColor()
        //Bukkit.getLogger().info("Running getColor");
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++) {
                // Get biome based on (blockX,blockZ) coordinates
                BiomeBase currentBiome = getColor(blockX+x, blockZ+z);
                
                for (int y = 0; y < 256; y++) {
                    //Set the entire chunk column to that biome
                    //i need some sort of BiomeStorage.setBiome(x, y, z, currentBiome)
                    //BiomeStorage(ChunkCoordIntPair chunkcoordintpair, WorldChunkManager worldchunkmanager)
                    //ichunkaccess is where the biome is being stored, I believe
                    //actually protochunk?
                    // chunkgenerator calls a(new BiomeStorage(chunkcoordintpair, worldchunkmanager) of ProtoChunk
                    // in vanilla terms that's a(ChunkBiomeContainer):
                    // void setBiomes(net.minecraft.world.level.chunk.ChunkBiomeContainer) -> a
                    //So in spigot, that's just a setter for "ProtoChunk.d"
                    currentBiomeStorage.setBiome(x, y, z, currentBiome);
                }
                    
            }
        
        // Add the hexagon BiomeStorage index back to ichunkaccess
        // NMS call obtained from ChunkGenerator createBiomes()
        //Bukkit.getLogger().info("Updated BiomeBase, Adding back to ichunkaccess");
        ((ProtoChunk) ichunkaccess).a(currentBiomeStorage);
    }

    @Override
    protected BiomeBase getBiome(BiomeManager biomemanager, BlockPosition blockposition) {
        try {
            return (BiomeBase) getBiome.invoke(parent, biomemanager, blockposition);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected Error while invoke method getCarvingBiome", e);
        }
    }

    @Override
    public void doCarving(BiomeManager biomemanager, IChunkAccess ichunkaccess, WorldGenStage.Features worldgenstage_features) {
        parent.doCarving(biomemanager, ichunkaccess, worldgenstage_features);
    }

    @Override
    public BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockPosition, int i, boolean b) {
        return parent.findNearestMapFeature(world, s, blockPosition, i, b);
    }

    @Override
    public void addDecorations(final RegionLimitedWorldAccess regionLimitedWorldAccess) {
        parent.addDecorations(regionLimitedWorldAccess);
        
        //Bukkit.getLogger().info("addDecorations has been invoked in a chunk");
        //final Set<Biome> biomes = getBiomes(regionLimitedWorldAccess);
    }

    @Override
    public void buildBase(RegionLimitedWorldAccess regionLimitedWorldAccess, IChunkAccess iChunkAccess) {
        parent.buildBase(regionLimitedWorldAccess, iChunkAccess);
    }

    @Override
    public void addMobs(RegionLimitedWorldAccess regionLimitedWorldAccess) {
        parent.addMobs(regionLimitedWorldAccess);
    }

    @Override
    public C getSettings() {
        return parent.getSettings();
    }

    @Override
    public int getSpawnHeight() {
        return parent.getSpawnHeight();
    }

    @Override
    public void doMobSpawning(WorldServer worldserver, boolean flag, boolean flag1) {
        parent.doMobSpawning(worldserver, flag, flag1);
    }

    @Override
    public boolean canSpawnStructure(BiomeBase biomeBase, StructureGenerator<? extends WorldGenFeatureConfiguration> structureGenerator) {
        return parent.canSpawnStructure(biomeBase, structureGenerator);
    }

    @Override
    public <C1 extends WorldGenFeatureConfiguration> C1 getFeatureConfiguration(BiomeBase biomebase, StructureGenerator<C1> structuregenerator) {
        return parent.getFeatureConfiguration(biomebase, structuregenerator);
    }

    @Override
    public WorldChunkManager getWorldChunkManager() {
        return parent.getWorldChunkManager();
    }

    @Override
    public long getSeed() {
        return parent.getSeed();
    }

    @Override
    public int getGenerationDepth() {
        return parent.getGenerationDepth();
    }

    @Override
    public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType enumCreatureType, BlockPosition blockPosition) {
        return parent.getMobsFor(enumCreatureType, blockPosition);
    }

    @Override
    public void createStructures(BiomeManager biomemanager, IChunkAccess ichunkaccess, ChunkGenerator<?> chunkgenerator, DefinedStructureManager definedstructuremanager) {
        parent.createStructures(biomemanager, ichunkaccess, chunkgenerator, definedstructuremanager);
    }

    @Override
    public void storeStructures(GeneratorAccess generatoraccess, IChunkAccess ichunkaccess) {
        parent.storeStructures(generatoraccess, ichunkaccess);
    }

    @Override
    public void buildNoise(GeneratorAccess generatorAccess, IChunkAccess iChunkAccess) {
        parent.buildNoise(generatorAccess, iChunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return parent.getSeaLevel();
    }

    @Override
    public int getBaseHeight(int i, int i1, HeightMap.Type type) {
        return parent.getBaseHeight(i, i1, type);
    }

    @Override
    public int b(int i, int j, HeightMap.Type heightmap_type) {
        return parent.b(i, j, heightmap_type);
    }

    @Override
    public int c(int i, int j, HeightMap.Type heightmap_type) {
        return parent.c(i, j, heightmap_type);
    }

    @Override
    public World getWorld() {
        return parent.getWorld();
    }

//    private Set<Biome> getBiomes(final RegionLimitedWorldAccess access) {
//        final Set<Biome> set = new HashSet<>();
//
//        final int x = access.a() << 4;
//        final int z = access.b() << 4;
//
//        for (int x2 = x; x2 < x + 16; x2++)
//            for (int z2 = z; z2 < z + 16; z2++) {
//                final BiomeBase base = access.getBiome(new BlockPosition(x2, 60, z2));
//                try {
//                    set.add(Biome.valueOf(IRegistry.BIOME.getKey(base).getKey().toUpperCase()));
//                } catch (Exception ignored) {
//                }
//            }
//
//        return set;
//    }
    
    public BiomeBase getColor(int x, int y) {
        //int SIDE_LENGTH = 32;
        int SIDE_LENGTH = 64;
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
                        
                        // Assign a color
                        // w - offset - (5/3)h should be a multiple of 14
                        if (((wValue - 0 - (5*hValue/3)) % 14) == 0)
                            minColor = 0; // 0, Yellow
                        
                        else if (((wValue - 2 - (5*hValue/3)) % 14) == 0)
                            minColor = 1; // 1, Green
                        
                        else if (((wValue - 4 - (5*hValue/3)) % 14) == 0)
                            minColor = 2; // 2, Pink
                        
                        else if (((wValue - 6 - (5*hValue/3)) % 14) == 0)
                            minColor = 3; // 3, Red
                        
                        else if (((wValue - 8 - (5*hValue/3)) % 14) == 0)
                            minColor = 4; // 4, Blue
                        
                        else if (((wValue - 10 - (5*hValue/3)) % 14) == 0)
                            minColor = 5; // 5, Purple
                        
                        else if (((wValue - 12 - (5*hValue/3)) % 14) == 0)
                            minColor = 6; // 6, Light Blue
                    }
                    
                    //Bukkit.getLogger().info("(" + wValue + "," + hValue + ") " + valid + " " + distance);
                }
                //else
                //    Bukkit.getLogger().info("(" + wValue + "," + hValue + ") ");
            }
        
        //Bukkit.getLogger().info("Closest: minDistance = " + minDistance + ", color = " + minColor);
        if (minColor == 0)
            return new BiomeDesert(); // 0, Yellow
        
        else if (minColor == 1)
            return new BiomeTaiga(); // 1, Green
        
        else if (minColor == 2)
            return new BiomeFlowerForest(); // 2, Pink
        
        else if (minColor == 3)
            return new BiomeMushrooms(); // 3, Red
        
        else if (minColor == 4)
            return new BiomeBambooJungle(); // 4, Blue
        
        else if (minColor == 5)
            return new BiomeRoofedForest(); // 5, Purple
        
        else if (minColor == 6)
            return new BiomeBirchForest(); // 6, Light Blue
        
        else
            return new BiomeIcePlains();
    }
}
