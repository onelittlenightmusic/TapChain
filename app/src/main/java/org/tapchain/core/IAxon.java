package org.tapchain.core;

public interface IAxon<T> {
	class AxonException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		AxonException() {
			super("PathException");
		}
	}
	boolean sync_push(T i) throws InterruptedException;
	T sync_pop() throws InterruptedException, IAxon.AxonException;
	T sync_peek() throws InterruptedException, AxonException;
	boolean async_push(T i);
	int size();
	boolean isEmpty();
	void CloseWhenEmpty();
	void CloseForced();
	boolean isClosed();
	IAxon<T> reset();
	T getCache();
	boolean setSize(int a);
}
