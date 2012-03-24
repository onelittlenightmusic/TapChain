package org.tapchain;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.tapchain.Axon.AxonException;


public class ChainController {
	ControlCallback local_cb = null;
	LocalThread1 th = null;
	CountDownLatch c = new CountDownLatch(1);
	
	ChainController(int pg) {
		if(pg != 0)
			Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new LocalThread1(), 0, pg, TimeUnit.MILLISECONDS);
		else
			Executors.newSingleThreadExecutor().execute(new LocalThread1());
	}
	
	public ChainController Set(ControlCallback _callback) {
		local_cb = _callback;
		return this;
	}

	public boolean Kick() {
		c.countDown();
		return true;
	}
	
	class LocalThread1 implements Runnable {
		Axon<String> signalQueue = new Toggle<String>();
		public LocalThread1() {
		}
		
		@Override
		public synchronized void run() {
//			boolean rtn = true;
//			while (rtn) {
				try {
					c.await();
					c = new CountDownLatch(1);
					/*rtn = */local_cb.impl();
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
					return;
				}
//			}
		}
	};
	public interface ControlCallback {
		public boolean impl();
	}
}
