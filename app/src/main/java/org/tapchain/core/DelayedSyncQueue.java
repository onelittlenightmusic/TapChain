package org.tapchain.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;

/**
 * Created by hiro on 2015/06/03.
 */
public class DelayedSyncQueue<T extends Delayed> extends DelayQueue<T> implements IAxon<T> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    boolean lock = false;
    boolean closed = false;
    int size = 0;
    T cache = null;

    public DelayedSyncQueue() {
        super();
    }
    public DelayedSyncQueue(int _size) {
        super();
        size = _size;
    }

    public DelayedSyncQueue<T> reset() {
        super.clear();
        closed = false;
        lock = false;
        return this;
    }
    public boolean sync_push(T i) throws InterruptedException {
        if(closed) {
            return false;
        }
        boolean _tmp = false;
        if (super.isEmpty()) {
            _tmp = super.offer((T)i);
            synchronized(this) {
                notifyAll();
            }
        } else {
            _tmp = super.offer((T)i);
        }
        return _tmp;
    }

    public T sync_pop() throws InterruptedException, IAxon.AxonException {
        cache = take();
//        }
        return cache;
    }

    public boolean async_push(T i) {
        if(closed) {
            return false;
        }
        boolean _tmp = false;
        if (super.isEmpty()) {
            _tmp = super.offer(i);
            synchronized (this) {
                notifyAll();
            }
        } else {
            _tmp = super.offer(i);
        }
        return _tmp;
    }

    public synchronized T apop() {
        cache = super.poll();
        return cache;
    }

    public void CloseWhenEmpty() {
        closed = true;
        return;
    }
    public synchronized void CloseForced() {
        closed = true;
        super.clear();
        notifyAll();
    }
    public boolean isClosed() {
        return closed && super.isEmpty();
    }
    public T getCache() {
        return cache;
    }
    @Override
    public T sync_peek() throws InterruptedException, AxonException {
        T rtn;
        while(null == (rtn = super.peek())) {
            synchronized(this) {
                wait();
            }
        }
        return rtn;
    }
    @Override
    public boolean setSize(int a) {
        size = a;
        return true;
    }
}

