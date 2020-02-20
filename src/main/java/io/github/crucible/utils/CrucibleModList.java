package io.github.crucible.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrucibleModList {

    private String forgeVersion;
    private List<String> mods;

    public CrucibleModList(String forgeVersion, ArrayList<String> mods) {
        this.setForgeVersion(forgeVersion);
        this.setMods(mods);

    }

    public CrucibleModList() {
        this.forgeVersion = "10.13.4.1614";
        this.mods = new ArrayList<String>();
    }

    public boolean loadFromObject(Map<String, String> object) {
        try {
            if (object instanceof Map<?, ?>) {
                Map<String, String> modsMap = object;

                if (modsMap != null) {
                    for (String m : modsMap.keySet()) {
                        mods.add(m + "-" + modsMap.getOrDefault(m, "1.0"));
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
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
