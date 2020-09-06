package org.bukkit.craftbukkit.map;

import org.bukkit.map.MapCursor;

import java.util.ArrayList;

public class RenderData {

    public final byte[] buffer;
    public final ArrayList<MapCursor> cursors;

    public RenderData() {
        this.buffer = new byte[128 * 128];
        this.cursors = new ArrayList<MapCursor>();
    }

}
