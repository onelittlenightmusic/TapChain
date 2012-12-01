package org.tapchain.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;


public class ChainController {
	IControlCallback local_cb = null;
	LocalControllerThread th = null;
	CountDownLatch c = new CountDownLatch(1), go = new CountDownLatch(1);
	boolean running = true;
	
	//1.Initialization
	ChainController(int pg) {
		Start();
		if(pg != 0)
			Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new LocalControllerThread(), 0, pg, TimeUnit.MILLISECONDS);
		else
			Executors.newSingleThreadExecutor().execute(new LocalControllerThread());
	}
	
	//2.Getters and setters
	public ChainController Set(IControlCallback _callback) {
		local_cb = _callback;
		return this;
	}

	//3.Chaging state
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
	
	//4.Termination: none
	//5.Local classes
	class LocalControllerThread implements Runnable {
		public LocalControllerThread() {
		}
		
		@Override
		public synchronized void run() {
			try {
				go.await();
				c.await();
				c = new CountDownLatch(1);
				/*rtn = */local_cb.onCalled();
//				Log.i("test","____");
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
		}
	};
	
	public interface IControlCallback {
		public boolean onCalled();
	}
}
