package org.tapchain;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ChainController {
	IControlCallback local_cb = null;
	LocalThread1 th = null;
	CountDownLatch c = new CountDownLatch(1), go = new CountDownLatch(1);
	boolean running = true;
	
	ChainController(int pg) {
		Start();
		if(pg != 0)
			Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new LocalThread1(), 0, pg, TimeUnit.MILLISECONDS);
		else
			Executors.newSingleThreadExecutor().execute(new LocalThread1());
	}
	
	public ChainController Set(IControlCallback _callback) {
		local_cb = _callback;
		return this;
	}

	public boolean kick() {
		c.countDown();
		return true;
	}
	
	public boolean Toggle() {
		if(running) Stop();
		else Start();
		return running;
	}
	
	public boolean Stop() {
		go = new CountDownLatch(1);
		running = false;
		return true;
	}
	
	public boolean Start() {
		go.countDown();
		running = true;
		return true;
	}
	
	class LocalThread1 implements Runnable {
		public LocalThread1() {
		}
		
		@Override
		public synchronized void run() {
//			boolean rtn = true;
//			while (rtn) {
				try {
					go.await();
					c.await();
					c = new CountDownLatch(1);
					/*rtn = */local_cb.callback();
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
					return;
				}
//			}
		}
	};
	public interface IControlCallback {
		public boolean callback();
	}
}
