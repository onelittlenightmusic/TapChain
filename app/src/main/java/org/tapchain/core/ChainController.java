package org.tapchain.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ChainController {
	IControlCallback callback = null;
	CountDownLatch c = new CountDownLatch(1), go = new CountDownLatch(1);
	boolean running = true;
	ConcurrentLinkedQueue<Object> kickingObj;
	ConcurrentLinkedQueue<IChainAdapter> adapters = new ConcurrentLinkedQueue<IChainAdapter>();
	ExecutorService executorService;

	//1.Initialization
	ChainController(int pg) {
		Start();
		if(pg != 0) {
			executorService = Executors.newSingleThreadScheduledExecutor();
			((ScheduledExecutorService) executorService).scheduleWithFixedDelay(new LocalControllerThread(), 0, pg, TimeUnit.MILLISECONDS);
		}
		else
			executorService = Executors.newSingleThreadExecutor();
			executorService.execute(new LocalControllerThread());
	}
	
	//2.Getters and setters
	public ChainController Set(IControlCallback _callback) {
		callback = _callback;
		return this;
	}

	//3.Chaging state
	public boolean kick(Object obj) {
		if(obj != null && kickingObj != null)
			kickingObj.add(obj);
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
	
	public synchronized void registerControllerAdapter(IChainAdapter adapter) {
		if(kickingObj == null)
			kickingObj = new ConcurrentLinkedQueue<>();
		adapters.add(adapter);
	}
	
	public synchronized void unregisterControllerAdapter(IChainAdapter adapter) {
		adapters.remove(adapter);
		if(adapters.isEmpty())
			kickingObj = null;
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
				for(IChainAdapter adapter: adapters)
					adapter.adapterRun(kickingObj);
				if(kickingObj != null)
				    kickingObj.clear();
				c = new CountDownLatch(1);
				callback.onCalled();
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
