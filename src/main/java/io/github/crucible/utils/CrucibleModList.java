package io.github.crucible.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.DataWatcher;
import net.minecraft.world.gen.ChunkProviderServer;

public class CrucibleModList {
	
	private String forgeVersion;
	private List<String> mods;
	
	public CrucibleModList(String forgeVersion, ArrayList<String> mods) {
		this.setForgeVersion(forgeVersion);
		this.setMods(mods);
		
	}
	
	public boolean loadFromObject(Map<String, String> object) {
		try {
			if(object instanceof Map<?,?>) {
				Map<String,String> modsMap = (Map<String, String>) object;
				
				if(modsMap != null) {
					for(String m : modsMap.keySet()) {
						mods.add(m + "-" + modsMap.getOrDefault(m, "1.0"));
					}
				}		
			}
		}catch(Exception e) {
			return false;
		}
		return true;
	} 
	
	public CrucibleModList() {
		this.forgeVersion = "10.13.4.1614";
		this.mods = new ArrayList<String>();
	}

	public String getForgeVersion() {
		return forgeVersion;
	}

	public void setForgeVersion(String forgeVersion) {
		this.forgeVersion = forgeVersion;
	}

	public List<String> getMods() {
		return mods;
	}

	public void setMods(List<String> mods) {
		this.mods = mods;
	}
	
	public int getSize() {
		return mods.size();
	}

}
