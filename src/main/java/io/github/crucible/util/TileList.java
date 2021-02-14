package io.github.crucible.util;

import net.minecraft.tileentity.TileEntity;

import java.util.Collection;

public class TileList extends ListSet<TileEntity> {
    public TileList(int size) {
        super(size);
    }

    public TileList() {
    }

    public TileList(Collection<? extends TileEntity> collection) {
        super(collection);
    }

    @Override
    public boolean add(TileEntity element) {
        element.myStopwatch = element.worldObj.myManager.of(element);
        return super.add(element);
    }

    @Override
    public TileEntity set(int index, TileEntity element) {
        element.myStopwatch = element.worldObj.myManager.of(element);
        return super.set(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends TileEntity> elements) {
        for (TileEntity element : elements) {
            element.myStopwatch = element.worldObj.myManager.of(element);
        }
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TileEntity> elements) {
        for (TileEntity element : elements) {
            element.myStopwatch = element.worldObj.myManager.of(element);
        }
        return super.addAll(index, elements);
    }
}
