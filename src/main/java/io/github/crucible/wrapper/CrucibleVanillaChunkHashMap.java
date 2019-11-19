package io.github.crucible.wrapper;


import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.craftbukkit.util.LongHash;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

public class CrucibleVanillaChunkHashMap extends LongHashMap{
	
	/*
	 * VanillaChunkHashMap implementation using CrucibleChunkHashMap
	 */
	
	private CrucibleChunkBlockHashMap crucibleChunkBlockHashMap;
	private Long2ObjectMap<Chunk> vanilla_sync;
	
	private boolean notRealFace = false;
	
    public CrucibleVanillaChunkHashMap(CrucibleChunkBlockHashMap crucibleChunkBlockHashMap) {
    	
        this.crucibleChunkBlockHashMap = crucibleChunkBlockHashMap;
        this.vanilla_sync = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
        
    }
    
    public CrucibleVanillaChunkHashMap(CrucibleChunkBlockHashMap crucibleChunkBlockHashMap, ConcurrentHashMap<Long, Chunk> vanilla) {
    	
        this.crucibleChunkBlockHashMap = crucibleChunkBlockHashMap;
        
        this.vanilla_sync = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>(vanilla));
        
        this.notRealFace = true;
        
    }
    
    public ConcurrentHashMap<Long, Chunk> convert(Long2ObjectMap<Chunk> map){
    	return new ConcurrentHashMap<Long, Chunk>(map);
    }
    
    public CrucibleVanillaChunkHashMap thisIsNotMyRealFace() {
        return new CrucibleVanillaChunkHashMap(crucibleChunkBlockHashMap, convert(vanilla_sync));
    }
    
    private long V2B(long key) {
        if (notRealFace) {
            return key;
        } else {
            return ChunkCoordIntPair.chunkXZ2Int(LongHash.msw(key), LongHash.lsw(key));
        }
    }

    public ConcurrentHashMap<Long, Chunk> rawVanilla() {
        return convert(vanilla_sync);
    }

    public CrucibleChunkBlockHashMap rawCrucibleMap() {
        return crucibleChunkBlockHashMap;
    }

    public int size() {
        return this.crucibleChunkBlockHashMap.size();
    }

    @Override
    public void add(long key, Object value) {
        if (value instanceof Chunk) {
        	
            Chunk c = (Chunk) value;
            
            crucibleChunkBlockHashMap.put(c);
            vanilla_sync.put(V2B(key), c);
        }
    }


    @Override
    public boolean containsItem(long key) {
        return vanilla_sync.containsKey(V2B(key));
    }

    @Override
    public Object getValueByKey(long key) {
        return vanilla_sync.get(V2B(key));
    }

    @Override
    public Object remove(long key) {
        Object o = vanilla_sync.remove(V2B(key));
        if (o instanceof Chunk) // Thermos - Use our special map
        {
            Chunk c = (Chunk) o;
            crucibleChunkBlockHashMap.remove(c);
        }
        return o;
    }

}
