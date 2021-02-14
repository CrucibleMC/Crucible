package io.github.crucible.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * A hacky array list implementation backended by a HashSet.
 * Honestly I have no idea why thermos uses it, but I made a clean and better reimplementation of it
 * My guess it's a fancy list with the ability of not having duplicate elements like a set.
 *
 * @param <E> The generic this array will hold.
 * @author juanmuscaria
 */
public class ListSet<E> extends ArrayList<E> {
    private final HashSet<E> backend;

    public ListSet(int size) {
        super(size);
        backend = new HashSet<>(size);
    }

    public ListSet() {
        super();
        backend = new HashSet<>();
    }

    public ListSet(Collection<? extends E> collection) {
        super(collection);
        backend = new HashSet<>(collection);
    }

    @Override
    public boolean add(E element) {
        boolean flag;
        if (flag = backend.add(element))
            super.add(element);
        return flag;
    }

    @Override
    public E set(int index, E element) {
        E internal = super.get(index);
        if (internal != null)
            backend.remove(internal);
        if (backend.add(element))
            super.set(index, element);
        return internal;
    }

    @Override
    public void add(int index, E element) {
        if (!backend.contains(element)) {
            super.add(index, element);
            backend.add(element);
        }
    }

    @Override
    public E remove(int index) {
        E internal = super.remove(index);
        backend.remove(internal);
        return internal;
    }

    @Override
    public boolean remove(Object element) {
        boolean flag;
        if (flag = backend.remove(element))
            super.remove(element);
        return flag;
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        Set<E> internal = new HashSet<>(elements);
        backend.addAll(internal);
        return super.addAll(internal);
    }

    @Override
    public boolean removeAll(Collection<?> elements) {
        backend.removeAll(elements);
        return super.removeAll(elements);
    }

    @Override
    public boolean retainAll(Collection<?> elements) {
        backend.retainAll(elements);
        return super.retainAll(elements);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        ListIterator<E> iterator = this.listIterator();
        while (iterator.hasNext()) {
            E oldValue = iterator.next();
            E valueToAdd = operator.apply(oldValue);
            if (oldValue != valueToAdd) {
                backend.remove(oldValue);
                backend.add(valueToAdd);
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
    public boolean addAll(int index, Collection<? extends E> elements) {
        this.rangeCheckForAdd(index);
        Set<E> internal = new HashSet<>(elements);
        backend.addAll(internal);
        return super.addAll(index, internal);
    }

    @Override
    public boolean contains(Object element) {
        return backend.contains(element);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> elements) {
        return backend.containsAll(elements);
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > this.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size());
        }
    }

}
