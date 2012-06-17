package org.tapchain;

public interface IAxon<T> {
	public static class AxonException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		AxonException() {
			super("PathException");
		}
	}
	public boolean sync_push(T i) throws InterruptedException;
	public T sync_pop() throws InterruptedException, IAxon.AxonException;
	public T sync_peek() throws InterruptedException;
	public boolean async_push(T i);
	public int size();
	public boolean isEmpty();
	public void CloseWhenEmpty();
	public void CloseForced();
	public boolean isClosed();
	public IAxon<T> reset();
	public T getCache();
	public boolean setSize(int a);
}
