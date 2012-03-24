package org.tapchain;

public interface Axon<T>  {
	public static class AxonException extends Exception {
		AxonException() {
			super("PathException");
		}
	}
	public boolean sync_push(T i) throws InterruptedException;
	public T sync_pop() throws InterruptedException, Axon.AxonException;
	public T sync_peek() throws InterruptedException;
	public boolean async_push(T i);
	public int size();
	public boolean isEmpty();
	public void CloseWhenEmpty();
	public void CloseForced();
	public boolean isClosed();
	public Axon<T> reset();
	public T getCache();
	public boolean setSize(int a);
}
