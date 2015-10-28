package org.tapchain.core;

import java.util.AbstractQueue;
import java.util.Iterator;

public class Hippo<T> extends AbstractQueue<T> 
	implements IAxon<T> {
	T memory = null, old = null;
	boolean closed = false;
//	boolean first = true;
	public Hippo () {
	}
	@Override
	public T sync_pop() throws InterruptedException {
		//when memory is null, memory is locked or not set
		while (memory == null) {
			synchronized(this) {
				wait();
			}
		}
		return memory;
	}
	@Override
	public boolean sync_push(T x) {
		old = memory;
		memory = x;
		if(old == null) {
			synchronized(this) {
				notifyAll();
			}
		}
		return true;
	}
	public synchronized boolean async_push(T x) {
		return sync_push(x);
	}
	@Override
	public int size() {
		if(memory != null) {
			return 1;
		}
		return 0;
	}
	@Override
	public boolean isEmpty() {
		return memory == null;
	}
	@Override
	public void CloseWhenEmpty() {
		closed = true;
		return;
	}
	public void CloseForced() {
		closed = true;
		return;
	}
	@Override
	public boolean isClosed() {
		return closed;
	}
	@Override
	public IAxon<T> reset() {
		memory = null;
		return this;
	}
	public T getCache() {
		return memory;
	}
	@Override
	public T sync_peek() throws InterruptedException {
		return sync_pop();
	}
	@Override
	public boolean offer(T arg0) {
		return sync_push(arg0);
	}
	@Override
	public T peek() {
		try {
			return sync_pop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public T poll() {
		try {
			return sync_pop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Iterator<T> iterator() {
		return null;
	}
	@Override
	public boolean setSize(int size) {
		return size == 1;
	}
}
