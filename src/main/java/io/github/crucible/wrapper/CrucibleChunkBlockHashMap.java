package io.github.crucible.wrapper;

import java.util.Collection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.chunk.Chunk;

public class CrucibleChunkBlockHashMap {
	
	/*
	 * ChunkBlockHashMap implementation using fastutils Long2ObjectMap
	 */
	
	private final Long2ObjectMap<Chunk[][]> map = new Long2ObjectOpenHashMap<Chunk[][]>();
	private Chunk last1, last2, last3, last4;
	private int size = 0;
	
	public Long2ObjectMap<Chunk[][]> raw() {
		return map;
	}
	
	public int size() {
		return size;
	}
	
	public boolean bulkCheck(Collection<int[]> coords) {
		
        Chunk[][] last = null;
        
        int x = -1, z = -1;
        
        for (int[] set : coords) {
        	
            if (last != null) {
            	
                if (set[0] >> 4 == x >> 4 && set[1] >> 4 == z >> 4) {
                    x = set[0];
                    z = set[1];
                    x %= 16;
                    z %= 16;
                    
                    if (last[(x + (x >> 31)) ^ (x >> 31)][(z + (z >> 31)) ^ (z >> 31)] == null) {
                        return false;
                    }
                    
                    x = set[0];
                    z = set[1];
                    
                } else {
                	
                    x = set[0];
                    z = set[1];
                    
                    last = this.map.get((((long) (x >> 4)) << 32L) ^ (z >> 4));
                    
                    if (last == null) {
                        return false;
                    }
                    
                    x %= 16;
                    z %= 16;
                    
                    if (last[(x + (x >> 31)) ^ (x >> 31)][(z + (z >> 31)) ^ (z >> 31)] == null) {
                        return false;
                    }
                    
                    x = set[0];
                    z = set[1];

                }
                
            } else {
                x = set[0];
                z = set[1];
                
                last = this.map.get((((long) (x >> 4)) << 32L) ^ (z >> 4));

                if (last == null) {
                    return false;
                }
                
                x %= 16;
                z %= 16;
                
                if (last[(x + (x >> 31)) ^ (x >> 31)][(z + (z >> 31)) ^ (z >> 31)] == null) {
                    return false;
                }
                
                x = set[0];
                z = set[1];
            }
        }

        return true;
    }
	
	public Chunk get(int x, int z) {
        if (last1 != null && last1.xPosition == x && last1.zPosition == z) {
            return last1;
        }
        if (last2 != null && last2.xPosition == x && last2.zPosition == z) {
            return last2;
        }
        if (last3 != null && last3.xPosition == x && last3.zPosition == z) {
            return last3;
        }
        if (last4 != null && last4.xPosition == x && last4.zPosition == z) {
            return last4;
        }

        Chunk[][] bunch = this.map.get((((long) (x >> 4)) << 32L) ^ (z >> 4));
        if (bunch == null) return null;

        x %= 16;
        z %= 16;
        Chunk ref = bunch[(x + (x >> 31)) ^ (x >> 31)][(z + (z >> 31)) ^ (z >> 31)];

        if (ref != null) {
            last4 = last3;
            last3 = last2;
            last2 = last1;
            last1 = ref;
        }
        return ref;
    }
	
    public void put(Chunk chunk) {
        if (chunk == null)
            return;
        size++;
        int x = chunk.xPosition, z = chunk.zPosition;

        long chunkhash = (((long) (x >> 4)) << 32L) ^ (z >> 4);

        Chunk[][] temp_chunk_bunch = this.map.get(chunkhash);

        x %= 16;
        z %= 16;

        x = (x + (x >> 31)) ^ (x >> 31);
        z = (z + (z >> 31)) ^ (z >> 31);

        if (temp_chunk_bunch != null) {
            temp_chunk_bunch[x][z] = chunk;
        } else {
            temp_chunk_bunch = new Chunk[16][16];
            temp_chunk_bunch[x][z] = chunk;
            this.map.put(chunkhash, temp_chunk_bunch); //Thermos
        }
        if (chunk != null) {
            last4 = last3;
            last3 = last2;
            last2 = last1;
            last1 = chunk;
        }
    }
    
    public void remove(Chunk chunk) {
        int x = chunk.xPosition, z = chunk.zPosition;
        Chunk[][] temp_chunk_bunch = this.map.get((((long) (x >> 4)) << 32L) ^ (z >> 4));

        x %= 16;
        z %= 16;

        x = (x + (x >> 31)) ^ (x >> 31);
        z = (z + (z >> 31)) ^ (z >> 31);

        if (temp_chunk_bunch != null) {
            if (temp_chunk_bunch[x][z] != null) {
                size--;
                temp_chunk_bunch[x][z] = null;
            }
        }
        if (last1 != null && last1.xPosition == chunk.xPosition && last1.zPosition == chunk.zPosition) {
            last1 = null;
            last1 = last2;
            last2 = last3;
            last3 = last4;
            last4 = null;
        }
        if (last2 != null && last2.xPosition == chunk.xPosition && last2.zPosition == chunk.zPosition) {
            last2 = null;
            last2 = last3;
            last3 = last4;
            last4 = null;
        }
        if (last3 != null && last3.xPosition == chunk.xPosition && last3.zPosition == chunk.zPosition) {
            last3 = null;
            last3 = last4;
            last4 = null;
        }
        if (last4 != null && last4.xPosition == chunk.xPosition && last4.zPosition == chunk.zPosition) {
            last4 = null;
        }
    }

}
