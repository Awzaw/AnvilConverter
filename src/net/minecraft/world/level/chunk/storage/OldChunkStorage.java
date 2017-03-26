package net.minecraft.world.level.chunk.storage;

/**
 * Copyright Mojang AB.
 *
 * Don't do evil.
 */
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.*;

import com.mojang.nbt.*;

public class OldChunkStorage {

    private final static int DATALAYER_BITS = 7;

    @SuppressWarnings("unchecked")
    public static OldLevelChunk load(CompoundTag tag) {
        int x = tag.getInt("xPos");
        int z = tag.getInt("zPos");

        OldLevelChunk levelChunk = new OldLevelChunk(x, z);
        levelChunk.blocks = tag.getByteArray("Blocks");
        levelChunk.data = new OldDataLayer(tag.getByteArray("Data"), DATALAYER_BITS);
        levelChunk.skyLight = new OldDataLayer(tag.getByteArray("SkyLight"), DATALAYER_BITS);
        levelChunk.blockLight = new OldDataLayer(tag.getByteArray("BlockLight"), DATALAYER_BITS);
        try {
            levelChunk.heightmap = tag.getIntArray("HeightMap");
        } catch (Exception e) {
            byte[] byteArray = tag.getByteArray("HeightMap");
            int[] intArray = new int[byteArray.length];

            for (int i = 0; i < byteArray.length; intArray[i] = byteArray[i++]);
            levelChunk.heightmap = intArray;
        }

        levelChunk.terrainPopulated = tag.getBoolean("TerrainPopulated");
        levelChunk.entities = (ListTag<CompoundTag>) tag.getList("Entities");
        levelChunk.tileEntities = (ListTag<CompoundTag>) tag.getList("TileEntities");
        levelChunk.tileTicks = (ListTag<CompoundTag>) tag.getList("TileTicks");
        levelChunk.lastUpdated = tag.getLong("LastUpdate");

        return levelChunk;
    }

    public static void convertToAnvilFormat(OldLevelChunk data, CompoundTag tag, BiomeSource biomeSource) {

        tag.putInt("xPos", data.x);
        tag.putInt("zPos", data.z);
        tag.putLong("LastUpdate", data.lastUpdated);
        int[] newHeight = new int[data.heightmap.length];
        for (int i = 0; i < data.heightmap.length; i++) {
            newHeight[i] = data.heightmap[i];
        }
        tag.putIntArray("HeightMap", newHeight);
        tag.putBoolean("TerrainPopulated", data.terrainPopulated);

        ListTag<CompoundTag> sectionTags = new ListTag<CompoundTag>("Sections");
        for (int yBase = 0; yBase < (128 / 16); yBase++) {

            // find non-air
            boolean allAir = true;
            for (int x = 0; x < 16 && allAir; x++) {
                for (int y = 0; y < 16 && allAir; y++) {
                    for (int z = 0; z < 16; z++) {
                        int pos = (x << 11) | (z << 7) | (y + (yBase << 4));
                        try {
                            int block = data.blocks[pos];
                            if (block != 0) {
                                allAir = false;
                                break;
                            }
                        } catch (Exception e) {
                            //???  
                        }

                    }
                }
            }

            if (allAir) {
                continue;
            }

            // build section
            byte[] blocks = new byte[16 * 16 * 16];
            DataLayer dataValues = new DataLayer(blocks.length, 4);
            DataLayer skyLight = new DataLayer(blocks.length, 4);
            DataLayer blockLight = new DataLayer(blocks.length, 4);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int pos = (x << 11) | (z << 7) | (y + (yBase << 4));
                        int block = data.blocks[pos];

                        blocks[(y << 8) | (z << 4) | x] = (byte) (block & 0xff);
                        dataValues.set(x, y, z, data.data.get(x, y + (yBase << 4), z));
                        skyLight.set(x, y, z, data.skyLight.get(x, y + (yBase << 4), z));
                        blockLight.set(x, y, z, data.blockLight.get(x, y + (yBase << 4), z));
                    }
                }
            }

            CompoundTag sectionTag = new CompoundTag();

            sectionTag.putByte("Y", (byte) (yBase & 0xff));
            sectionTag.putByteArray("Blocks", blocks);
            sectionTag.putByteArray("Data", dataValues.data);
            sectionTag.putByteArray("SkyLight", skyLight.data);
            sectionTag.putByteArray("BlockLight", blockLight.data);

            sectionTags.add(sectionTag);
        }
        tag.put("Sections", sectionTags);

        // create biome array
        if (biomeSource != null) {
            byte[] biomes = new byte[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    biomes[(z << 4) | x] = (byte) (biomeSource.getBiomeId((data.x << 4) | x, (data.z << 4) | z) & 0xff);
                }
            }
            tag.putByteArray("Biomes", biomes);
        }

        tag.put("Entities", data.entities);

        tag.put("TileEntities", data.tileEntities);

        if (data.tileTicks != null) {
            tag.put("TileTicks", data.tileTicks);
        }
    }

    public static void convertToPMAnvilFormat(OldLevelChunk data, CompoundTag tag, BiomeSource biomeSource) {

        tag.putInt("xPos", data.x);
        tag.putInt("zPos", data.z);
        tag.putLong("LastUpdate", data.lastUpdated);
        int[] newHeight = new int[data.heightmap.length];
        for (int i = 0; i < data.heightmap.length; i++) {
            newHeight[i] = data.heightmap[i];
        }
        tag.putIntArray("HeightMap", newHeight);
        tag.putBoolean("TerrainPopulated", data.terrainPopulated);

        ListTag<CompoundTag> sectionTags = new ListTag<CompoundTag>("Sections");
        for (int yBase = 0; yBase < (128 / 16); yBase++) {

            // find non-air
            boolean allAir = true;
            for (int x = 0; x < 16 && allAir; x++) {
                for (int y = 0; y < 16 && allAir; y++) {
                    for (int z = 0; z < 16; z++) {
                        int pos = (x << 11) | (z << 7) | (y + (yBase << 4));
                        try {
                            int block = data.blocks[pos];
                            if (block != 0) {
                                allAir = false;
                                break;
                            }
                        } catch (Exception e) {
                            //???  
                        }

                    }
                }
            }

            if (allAir) {
                continue;
            }

            // build section
            byte[] blocks = new byte[16 * 16 * 16];
            DataLayer dataValues = new DataLayer(blocks.length, 4);
            DataLayer skyLight = new DataLayer(blocks.length, 4);
            DataLayer blockLight = new DataLayer(blocks.length, 4);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int pos = (x << 11) | (z << 7) | (y + (yBase << 4));
                        int block = data.blocks[pos];

                        blocks[(y << 8) | (z << 4) | x] = (byte) (block & 0xff);
                        dataValues.set(x, y, z, data.data.get(x, y + (yBase << 4), z));
                        skyLight.set(x, y, z, data.skyLight.get(x, y + (yBase << 4), z));
                        blockLight.set(x, y, z, data.blockLight.get(x, y + (yBase << 4), z));
                    }
                }
            }

            CompoundTag sectionTag = new CompoundTag();

            sectionTag.putByte("Y", (byte) (yBase & 0xff));
            sectionTag.putByteArray("Blocks", reorderByteArray(blocks));
            sectionTag.putByteArray("Data", reorderNibbleArray(dataValues.data));
            sectionTag.putByteArray("SkyLight", reorderNibbleArray(skyLight.data));
            sectionTag.putByteArray("BlockLight", reorderNibbleArray(blockLight.data));

            sectionTags.add(sectionTag);
        }
        tag.put("Sections", sectionTags);

        // create biome array
        if (biomeSource != null) {
            byte[] biomes = new byte[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    biomes[(z << 4) | x] = (byte) (biomeSource.getBiomeId((data.x << 4) | x, (data.z << 4) | z) & 0xff);
                }
            }
            tag.putByteArray("Biomes", biomes);
        }

        tag.put("Entities", data.entities);

        tag.put("TileEntities", data.tileEntities);

        if (data.tileTicks != null) {
            tag.put("TileTicks", data.tileTicks);
        }
    }

    public static class OldLevelChunk {

        public long lastUpdated;
        public boolean lastSaveHadEntities;
        public boolean terrainPopulated;
        public int[] heightmap;
        public OldDataLayer blockLight;
        public OldDataLayer skyLight;
        public OldDataLayer data;
        public byte[] blocks;

        public ListTag<CompoundTag> entities;
        public ListTag<CompoundTag> tileEntities;
        public ListTag<CompoundTag> tileTicks;

        public final int x;
        public final int z;

        public OldLevelChunk(int x, int z) {
            this.x = x;
            this.z = z;
        }

    }

public static byte[] reorderByteArray(byte[] array) {
                StringBuilder result = new StringBuilder(getEmptyHex(4096));
                StringBuilder myarray = new StringBuilder(new String(array));
                myarray.setLength(4096);
		if(myarray != result){
			int i = 0;
			for(int x = 0; x < 16; ++x){
				int zM = x + 256;
				for(int z = x; z < zM; z += 16){
					int yM = z + 4096;
					for(int y = z; y < yM; y += 256){
                                            char newHex = myarray.charAt(y);
                                                result.setCharAt(i, newHex);
						++i;
					}
				}
			}
		}
		return result.toString().getBytes();
	}

	public static byte[] reorderNibbleArray(byte[] array) {
                StringBuilder commonValue = new StringBuilder(Integer.toHexString(0));
		StringBuilder result = new StringBuilder(getEmptyHex(2048));
                StringBuilder myarray = new StringBuilder(array.toString());
                myarray.setLength(2048);
		if(myarray != result){
			int i = 0;
			for(int x = 0; x < 8; ++x){
				for(int z = 0; z < 16; ++z){
					int zx = ((z << 3) | x);
					for(int y = 0; y < 8; ++y){
						int j = ((y << 8) | zx);
						int j80 = (j | 0x80);
						if (myarray.charAt(j) == commonValue.charAt(0) && myarray.charAt(j80) == commonValue.charAt(0)) {
							//values are already filled
						} else {
							char i1 = (char) myarray.charAt(j);
							char i2 = (char) myarray.charAt(j80);
							result.setCharAt(i, (char) ((i2 << 4) | (i1 & 0x0f)));
							result.setCharAt((i | 0x80), (char) ((i1 >> 4) | (i2 & 0xf0)));
						}
						i++;
					}
				}
				i += 128;
			}
		}

		return result.toString().getBytes();
	}
        

    public static String getEmptyHex(int count) {
        StringBuilder emptyHex = new StringBuilder();
        for (int c=0; c < count; c++){
            emptyHex.append(Integer.toHexString(0));
        }
        return emptyHex.toString();
    }
}
