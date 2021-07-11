package io.github.crucible.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Collection;

public class TileList extends ListSet<TileEntity> {
    private final World myWorld;
    public TileList(int size, World world) {
        super(size);
        myWorld = world;
    }

    public TileList(World myWorld) {
        this.myWorld = myWorld;
    }

    public TileList(Collection<? extends TileEntity> collection, World myWorld) {
        super(collection);
        this.myWorld = myWorld;
    }

    @Override
    public boolean add(TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        return super.add(element);
    }

    @Override
    public TileEntity set(int index, TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        return super.set(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends TileEntity> elements) {
        for (TileEntity element : elements) {
            element.myStopwatch = myWorld.myManager.of(element);
        }
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TileEntity> elements) {
        for (TileEntity element : elements) {
            element.myStopwatch = myWorld.myManager.of(element);
        }
        return super.addAll(index, elements);
    }

    public World getMyWorld() {
        return myWorld;
    }
}
