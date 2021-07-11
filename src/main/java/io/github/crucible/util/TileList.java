package io.github.crucible.util;

import io.github.crucible.CrucibleConfigs;
import io.github.crucible.CrucibleModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public class TileList extends ArrayList<TileEntity> {
    private final World myWorld;
    private final HashMap<Long, TileEntity> backend;

    public TileList(int size, World myWorld) {
        super(size);
        this.myWorld = myWorld;
        backend = new HashMap<>(size);
    }

    public TileList(World myWorld) {
        super();
        this.myWorld = myWorld;
        backend = new HashMap<>();
    }

    public TileList(Collection<? extends TileEntity> collection, World myWorld) {
        super();
        this.myWorld = myWorld;
        backend = new HashMap<>();
        this.addAll(collection);
    }

    @Override
    public boolean add(TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        TileEntity old = backend.put(hash(element), element);
        if (old == null) {
            return super.add(element);
        } else {
            warn(element, old);
            if (CrucibleConfigs.configs.crucible_fix_replaceDuplicatedTile) {
                super.remove(old);
                return super.add(element);
            } else {
                backend.put(hash(old), old);
                return false;
            }
        }
    }

    @Override
    public TileEntity set(int index, TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        TileEntity old = backend.put(hash(element), element);
        TileEntity internal = super.get(index);
        if (old == null) {
            if (internal != null)
                backend.remove(hash(internal));
            super.set(index, element);
        } else {
            warn(element, old);
            int dupeIndex = super.indexOf(old);
            if (dupeIndex == index) {
                // lucky index
                super.set(index, element);
            } else {
                super.set(index, element);
                CrucibleModContainer.logger.warn("The tile entity list had to keep a duplicated tile, your server is leaking ram!");
                new Exception().printStackTrace();
            }
        }
        return internal;
    }

    @Override
    public void add(int index, TileEntity element) {
        element.myStopwatch = myWorld.myManager.of(element);
        TileEntity old = backend.put(hash(element), element);
        if (old == null) {
            super.add(index, element);
        } else {
            warn(element, old);
            if (CrucibleConfigs.configs.crucible_fix_replaceDuplicatedTile) {
                super.remove(old);
                super.add(index, element);
            } else {
                backend.put(hash(old), old);
            }
        }
    }

    @Override
    public TileEntity remove(int index) {
        TileEntity internal = super.remove(index);
        backend.remove(hash(internal));
        return internal;
    }

    @Override
    public boolean remove(Object element) {
        boolean flag;
        if (flag = (backend.remove(hash((TileEntity) element)) !=null))
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
                backend.remove(hash(tile));
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
                backend.remove(hash(oldValue));
                valueToAdd.myStopwatch = myWorld.myManager.of(valueToAdd);
                backend.put(hash(valueToAdd),valueToAdd);
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
            TileEntity old = backend.put(hash(element),element);
            if (old == null) {
                internal.add(element);
            } else {
                warn(element, old);
                if (CrucibleConfigs.configs.crucible_fix_replaceDuplicatedTile) {
                    super.remove(old);
                    internal.add(element);
                } else {
                    backend.put(hash(old), old);
                }
            }
        }
        return super.addAll(index, internal);
    }

    @Override
    public boolean contains(Object element) {
        if (element instanceof TileEntity) {
            return backend.containsValue(element);
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

    public long hash(TileEntity object) {
        switch (CrucibleConfigs.configs.crucible_fix_tileEntityDeduplicationStrategy) {
            case 1:
                return hashTilePosition(object.xCoord, object.yCoord, object.zCoord);
            case 0:
            default:
                return object.hashCode();
        }
    }

    public long hashTilePosition(int x,int y, int z) {
        long hash = 17;
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
