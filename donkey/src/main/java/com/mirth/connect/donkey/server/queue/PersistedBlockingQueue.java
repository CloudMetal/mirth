/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersistedBlockingQueue<E> implements BlockingQueue<E> {
    private BlockingQueue<E> buffer = new LinkedBlockingQueue<E>();
    private Integer size;
    private int bufferCapacity = 1000;
    private boolean reachedCapacity = false;
    private PersistedBlockingQueueDataSource<E> dataSource;
    private final AtomicBoolean timeoutLock = new AtomicBoolean(false);

    public int getBufferSize() {
        return buffer.size();
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public synchronized void setBufferCapacity(int bufferCapacity) {
        if (bufferCapacity < this.bufferCapacity) {
            buffer.clear();
        }

        this.bufferCapacity = bufferCapacity;
    }

    public PersistedBlockingQueueDataSource<E> getDataSource() {
        return dataSource;
    }

    public void setDataSource(PersistedBlockingQueueDataSource<E> dataSource) {
        this.dataSource = dataSource;
        invalidate();
    }

    public synchronized void updateSize() {
        size = dataSource.getSize();
    }

    protected synchronized void invalidate() {
        buffer.clear();
        size = null;
    }

    @Override
    public synchronized boolean add(E e) {
        boolean success = true;

        if (size == null) {
            updateSize();
        }

        if (!reachedCapacity) {
            if (size < bufferCapacity && !dataSource.wasItemRotated()) {
                success = buffer.add(e);

                // If there is a poll with timeout waiting, notify that an item was added to the buffer.
                if (timeoutLock.get()) {
                    synchronized (timeoutLock) {
                        timeoutLock.notifyAll();
                        timeoutLock.set(false);
                    }
                }
            } else {
                reachedCapacity = true;
            }
        }

        size++;

        return success;
    }

    @Override
    public synchronized boolean contains(Object o) {
        return buffer.contains(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return buffer.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        if (size == null) {
            updateSize();
        }

        return (size == 0);
    }

    public synchronized void rotate(Object o) {
        // Pass the item to rotate to the data source
        dataSource.rotateItem(o);
        // remove the first item from the queue, but do not decrease size so it will be picked up again
        buffer.poll();
    }

    @Override
    public synchronized E peek() {
        if (size == null) {
            updateSize();
        }

        E element = null;

        if (size > 0) {
            element = buffer.peek();

            // if no element was received and there are elements in the database,
            // fill the buffer from the database and get the next element in the queue
            if (element == null) {
                fillBuffer();
                element = buffer.peek();
            }
        }

        return element;
    }

    @Override
    public synchronized E poll() {
        if (size == null) {
            updateSize();
        }

        E element = null;

        if (size > 0) {
            element = buffer.poll();

            // if no element was received and there are elements in the database,
            // fill the buffer from the database and get the next element in the queue
            if (element == null) {
                fillBuffer();
                element = buffer.poll();
            }

            // if an element was found, decrement the overall count
            if (element != null) {
                size--;

                // reset the reachedCapacity flag if we have exhausted the queue, so
                // that elements will once again be polled from the buffer
                if (size == 0) {
                    reachedCapacity = false;
                }
            }
        }

        return element;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        waitTimeout(timeout, unit);

        return poll();
    }

    protected void waitTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        if ((size == null || size == 0) && timeout > 0) {
            synchronized (timeoutLock) {
                timeoutLock.set(true);
                timeoutLock.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        }
    }

    @Override
    public int size() {
        if (size == null) {
            if (dataSource == null) {
                return 0;
            }
            updateSize();
        }

        return size;
    }

    @Override
    public synchronized Object[] toArray() {
        return buffer.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return buffer.toArray(a);
    }

    public synchronized void fillBuffer() {
        if (size == null) {
            updateSize();
        }

        List<E> nextItems = dataSource.getItems(0, Math.min(bufferCapacity, size));

        for (E e : nextItems) {
            buffer.add(e);
        }

        // If there is a poll with timeout waiting, notify that an item was added to the buffer.
        if (nextItems.size() > 0 && timeoutLock.get()) {
            synchronized (timeoutLock) {
                timeoutLock.notifyAll();
                timeoutLock.set(false);
            }
        }
    }

    @Override
    public synchronized void put(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized E element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E take() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remainingCapacity() {
        throw new UnsupportedOperationException();
    }
}
