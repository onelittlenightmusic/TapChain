package org.tapchain.core;

import java.util.concurrent.ConcurrentLinkedQueue;


public class SyncQueue<T> extends ConcurrentLinkedQueue<T>
		implements IAxon<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean lock = false;
	boolean closed = false;
	int size = 0;
	T cache = null;

	public SyncQueue() {
		super();
	}
	public SyncQueue(int _size) {
		super();
		size = _size;
	}
	
	public SyncQueue<T> reset() {
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
		cache = null;
			synchronized(this) {
				while (null == (cache = super.poll())) {
					if(closed)
						throw new IAxon.AxonException();
					wait();
				}
			}
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
		return cache = super.poll();
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
	public T sync_peek() throws InterruptedException {
		while(super.isEmpty()) {
			synchronized (this) {
				wait();
			}
		}
		return super.peek();
	}
	@Override
	public boolean setSize(int a) {
		size = a;
		return true;
	}
}
