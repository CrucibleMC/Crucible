package io.github.crucible.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * An {@link ArrayList} whose iterators walk a copy of the list taken when they are created, so iterating never throws
 * {@link java.util.ConcurrentModificationException}, not even while another thread adds or removes elements. It is still
 * an {@code ArrayList}, so code that casts the list keeps working.
 * <p>
 * Writes must stay on one thread. A reader on another thread sees the list as it was at some recent point, possibly
 * without the latest change. {@code Iterator.remove} removes the element from the live list; {@code ListIterator.set}
 * and {@code ListIterator.add} are not supported.
 */
public class SnapshotIteratingArrayList<E> extends ArrayList<E> {

    @Override
    public Iterator<E> iterator() {
        return new SnapshotIterator(0);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new SnapshotIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new SnapshotIterator(index);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        for (E e : this) {
            action.accept(e);
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(snapshot(), Spliterator.ORDERED);
    }

    // ArrayList.toArray is a single Arrays.copyOf: read off the writer's thread it cannot throw, but it can catch the
    // backing array mid-growth and pad the copy with nulls, which no caller of this list expects.
    private Object[] snapshot() {
        Object[] copy = toArray();
        int size = 0;
        for (Object o : copy) {
            if (o != null) copy[size++] = o;
        }
        if (size == copy.length) return copy;
        Object[] trimmed = new Object[size];
        System.arraycopy(copy, 0, trimmed, 0, size);
        return trimmed;
    }

    private class SnapshotIterator implements ListIterator<E> {
        private final Object[] elements = snapshot();
        private int cursor;
        private int lastReturned = -1;

        SnapshotIterator(int index) {
            if (index < 0 || index > elements.length) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + elements.length);
            cursor = index;
        }

        @Override
        public boolean hasNext() {
            return cursor < elements.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (cursor >= elements.length) throw new NoSuchElementException();
            lastReturned = cursor;
            return (E) elements[cursor++];
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E previous() {
            if (cursor <= 0) throw new NoSuchElementException();
            lastReturned = --cursor;
            return (E) elements[cursor];
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            if (lastReturned < 0) throw new IllegalStateException("next() or previous() has to be called before remove()");
            SnapshotIteratingArrayList.this.remove(elements[lastReturned]);
            lastReturned = -1;
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException("This iterator walks a copy of the list, so it cannot replace elements in place - call set(index, element) on the list instead.");
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException("This iterator walks a copy of the list, so it cannot insert in place - call add(index, element) on the list instead.");
        }
    }
}
