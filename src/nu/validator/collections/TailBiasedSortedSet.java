/*
 * Copyright (c) 2008 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public final class TailBiasedSortedSet<E> extends AbstractSet<E> implements
        SortedSet<E> {

    private final class Node<F> {
        public final F value;

        public Node<F> next;

        public Node<F> prev;
        
        /**
         * @param value
         * @param next
         */
        public Node(F value, Node<F> next, Node<F> prev) {
            this.value = value;
            this.next = next;
            this.prev = prev;
        }
    }

    private final class IteratorImpl implements Iterator<E> {

        private Node<E> next;

        private final Node<E> sentinel;
        
        /**
         * @param next
         */
        IteratorImpl(Node<E> first, Node<E> sentinel) {
            this.next = first;
            this.sentinel = sentinel;
        }

        @Override
        public boolean hasNext() {
            return next != sentinel;
        }

        @Override
        public E next() {
            if (next == sentinel) {
                throw new NoSuchElementException();
            }
            E rv = next.value;
            next = next.next;
            return rv;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private final Comparator<? super E> comparator;

    private final Node<E> head = new Node<>(null, null, null);

    private final Node<E> tail = new Node<>(null, null, null);
    
    private int size = 0;

    /**
     * @param comparator
     */
    public TailBiasedSortedSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
        head.next = tail;
        tail.prev = head;
    }

    public TailBiasedSortedSet() {
        this.comparator = null;
        head.next = tail;
        tail.prev = head;
    }
    
    public TailBiasedSortedSet(SortedSet<E> set) {
        this.comparator = set.comparator();
        head.next = tail;
        tail.prev = head;
        for (E e : set) {
            this.add(e);
        }
    }

    public TailBiasedSortedSet(Collection<? extends E> collection) {
        this.comparator = null;
        head.next = tail;
        tail.prev = head;
        for (E e : collection) {
            this.add(e);
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl(head.next, tail);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E first() {
        Node<E> first = head.next;
        if (first == null) {
            throw new NoSuchElementException();
        } else {
            return first.value;
        }
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E last() {
        Node<E> last = tail.prev;
        if (last == null) {
            throw new NoSuchElementException();
        } else {
            return last.value;
        }
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.AbstractCollection#add(java.lang.Object)
     */
    @Override
    public boolean add(E o) {
        Node<E> next = tail;
        while (next.prev != head) {
            Node<E> prev = next.prev;
            int comp = compare(o, prev.value);
            if (comp > 0) {
                next.prev = prev.next = new Node<>(o, next, prev);
                size++;
                return true;
            } else if (comp == 0) {
                return false;
            }
            next = next.prev;
        }
        // if we haven't returned yet, this is less than
        next.prev = head.next = new Node<>(o, next, head);
        size++;
        return true;
    }

    @SuppressWarnings("unchecked")
    private int compare(E one, E other) {
        if (comparator == null) {
            return ((Comparable<E>) one).compareTo(other);
        } else {
            return comparator.compare(one, other);
        }
    }

    /**
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        size = 0;
        head.next = tail;
        tail.prev = head;
    }

}
