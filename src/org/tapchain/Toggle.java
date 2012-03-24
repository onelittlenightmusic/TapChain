package org.tapchain;

public class Toggle<T> extends Hippo<T> {
	/* this class is meant to be a toggle button class with the interface like queue.
	 * push() : toggle on
	 * pop() : wait until toggle is turned on and toggle off
	 */
	
	public Toggle() {
		super();
	}

	@Override
	public T sync_pop() throws InterruptedException {
		/* when pop is called, as toggle-off, queue contents is cleared or wait until pushed. */
		T rtn = super.sync_pop();
		super.reset();
		return rtn;
	}
}
