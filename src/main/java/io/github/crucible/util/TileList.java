package io.github.crucible.util;

import io.github.crucible.CrucibleConfigs;
import io.github.crucible.CrucibleModContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.UnaryOperator;

public class TileList extends ArrayList<TileEntity> {
    private final World myWorld;
    //private final HashMap<Long, TileEntity> backend;
    private final Set<Hashable> backend;

    public TileList(int size, World myWorld) {
        super(size);
        this.myWorld = myWorld;
        backend = new HashSet<>(size);
    }

    public TileList(World myWorld) {
        super();
        this.myWorld = myWorld;
        backend = new HashSet<>();
    }

    public TileList(Collection<? extends TileEntity> collection, World myWorld) {
        super();
        this.myWorld = myWorld;
        backend = new HashSet<>();
        this.addAll(collection);
    }

    @Override
    public boolean add(TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        boolean flag;
        if (flag = backend.add(new Hashable(element)))
            super.add(element);
        return flag;
    }

    @Override
    public TileEntity set(int index, TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        TileEntity internal = super.get(index);
        if (internal != null)
            backend.remove(new Hashable(internal));
        if (backend.add(new Hashable(element)))
            super.set(index, element);
        return internal;
    }

    @Override
    public void add(int index, TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        if (!backend.contains(new Hashable(element))) {
            super.add(index, element);
            backend.add(new Hashable(element));
        }
    }

    @Override
    public TileEntity remove(int index) {
        TileEntity internal = super.remove(index);
        backend.remove(new Hashable(internal));
        return internal;
    }

    @Override
    public boolean remove(Object element) {
        boolean flag = false;
        if (element instanceof TileEntity && (flag = backend.remove(new Hashable((TileEntity) element))))
            super.remove(element);
        return flag;
    }

    @Override
    public boolean addAll(Collection<? extends TileEntity> elements) {
        boolean flag = false;
        for (TileEntity element : elements) {
            if (add(element))
                flag = true;
        }
        return flag;
    }

    @Override
    public boolean removeAll(Collection<?> elements) {
        boolean flag = false;
        for (Object element : elements) {
            if (remove(element))
                flag = true;
        }
        return flag;
    }

    @Override
    public boolean retainAll(Collection<?> elements) {
        boolean flag = false;
        Iterator<TileEntity> iterator = this.iterator();
        TileEntity tile;
        while(iterator.hasNext()) {
            if (!elements.contains(tile = iterator.next())) {
                iterator.remove();
                backend.remove(new Hashable(tile));
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public void replaceAll(UnaryOperator<TileEntity> operator) {
        Objects.requireNonNull(operator);
        ListIterator<TileEntity> iterator = this.listIterator();
        while (iterator.hasNext()) {
            TileEntity oldValue = iterator.next();
            TileEntity valueToAdd = operator.apply(oldValue);
            if (hash(oldValue) != hash(valueToAdd)) {
                backend.remove(new Hashable(oldValue));
                valueToAdd.myStopwatch = myWorld.myManager.of(valueToAdd);
                backend.add(new Hashable(valueToAdd));
            }
            iterator.set(valueToAdd);
        }
    }

    @Override
    public void clear() {
        super.clear();
        backend.clear();
    }

    @Override
    public boolean addAll(int index, Collection<? extends TileEntity> elements) {
        this.rangeCheckForAdd(index);
        List<TileEntity> internal = new ArrayList<>(elements.size());
        for (TileEntity element : elements) {
            element.myStopwatch = myWorld.myManager.of(element);
            boolean flag = backend.add(new Hashable(element));
            if (flag) {
                internal.add(element);
            } else {
                //warn(element, backend);
//                if (CrucibleConfigs.configs.crucible_fix_replaceDuplicatedTile) {
//                    super.remove(old);
//                    internal.add(element);
//                } else {
//                    backend.put(hash(old), old);
//                }
            }
        }
        return super.addAll(index, internal);
    }

    @Override
    public boolean contains(Object element) {
        if (element instanceof TileEntity) {
            return backend.contains(new Hashable((TileEntity) element));
        } else {
            return false;
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > this.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size());
        }
    }

    public World getMyWorld() {
        return myWorld;
    }

    public static int hash(TileEntity object) {
        switch (CrucibleConfigs.configs.crucible_fix_tileEntityDeduplicationStrategy) {
            case 1:
                return hashTilePosition(object.xCoord, object.yCoord, object.zCoord);
            case 0:
            default:
                return object.hashCode();
        }
    }

    public static int hashTilePosition(int x,int y, int z) {
        int hash = 17;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return hash;
    }

    public static void warn(TileEntity newTile, TileEntity oldTile) {
//        CrucibleModContainer.logger.warn("Tried to add an already existing tile\nNew: {}\nOld:{}",
//                newTile.toStringButDifferent(),
//                oldTile.toStringButDifferent());
    }
}
class Hashable {
    final TileEntity myTile;
    final int hash;

    Hashable(TileEntity myTile) {
        this.myTile = myTile;
        hash = TileList.hash(myTile);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hashable hashable = (Hashable) o;
        switch (CrucibleConfigs.configs.crucible_fix_tileEntityDeduplicationStrategy) {
            case 1:
                return myTile.xCoord == hashable.myTile.xCoord &&
                        myTile.yCoord == hashable.myTile.yCoord &&
                        myTile.zCoord == hashable.myTile.zCoord;
            case 0:
            default:
                return myTile.equals(hashable.myTile);
        }
    }
}
