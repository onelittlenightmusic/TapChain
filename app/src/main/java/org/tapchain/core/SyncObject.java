package org.tapchain.core;

import java.util.concurrent.SynchronousQueue;


public class SyncObject<T> extends SynchronousQueue<T> implements IAxon<T> {
	/**
	 * 
	 */
	Thread t = null;
	boolean cont = true;
	private static final long serialVersionUID = 1L;

	@Override
	public boolean sync_push(T i) {
		if(!cont) return false;
		if(t == null)
			t = Thread.currentThread();
		boolean rtn = super.offer(i);
		t = null;
		return rtn;
	}

	@Override
	public T sync_pop() throws InterruptedException, IAxon.AxonException {
		if(!cont) return null;
		if(t == null)
			t = Thread.currentThread();
		T rtn = null;
		try {
			rtn = super.take();
		} catch (InterruptedException e) {
			if(cont)
				throw e;
			else
				throw new IAxon.AxonException();
		}
		t = null;
		return rtn;
	}

	@Override
	public T sync_peek() {
		return super.peek();
	}

	@Override
	public boolean async_push(T i) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void CloseWhenEmpty() {
		
	}

	@Override
	public void CloseForced() {
		cont = false;
		if(t != null)
			t.interrupt();
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public IAxon<T> reset() {
		return this;
	}

	@Override
	public T getCache() {
		return null;
	}

	@Override
	public boolean setSize(int a) {
		return a == 1;
	}

}
