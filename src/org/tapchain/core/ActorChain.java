package org.tapchain.core;

import java.util.TreeMap;

public class ActorChain extends Chain {
	ViewList vlist = null;
	static Actor touchOn = new Actor();
	static Actor touchOff = new Actor();
	static Actor move = new Actor();
	static Actor fling = new Actor();
	static Actor longpress = new Actor();
	static Actor shake = new Actor();
	static Actor error = new Actor();
	static Actor touchSw = new Actor();
	// static PieceState globalState = new PieceState();
	static int num = 0;

	//1.Initialization
	public ActorChain(int time) {
		super(Chain.AUTO_MODE, time);
		touchOn.setControlled(false);
		touchOff.setControlled(false);
		move.setControlled(false);
		fling.setControlled(false);
		longpress.setControlled(false);
		shake.setControlled(false);
		touchSw.setControlled(false);
		vlist = new ViewList();
		// shake.setOutDefaultType(PackType.EVENT, IOType.TOGGLE);
		setCallback(null);
	}
	
	public ActorChain() {
		this(30);
	}

	//2.Getters and setters
	public int getViewNum() {
		return vlist.getSize();
	}
	
	public ActorChain Show(Object canvas) {
		vlist.show(canvas);
		return this;
	}

	//3.Changing state
	public ActorChain TouchOn(WorldPoint wp) {
		touchOn.push(wp);
		touchSw.push(new Boolean(true));
		return this;
	}
	
	public ActorChain TouchClear(){
		touchOn.clearInputHeap();
		touchOn.clearOutputHeap();
		return this;
	}

	public ActorChain TouchOff() {
		touchOff.push("");
		touchSw.push(new Boolean(false));
		return this;
	}

	public ActorChain Fling(WorldPoint wp) {
		fling.push(wp);
		TouchOff();
		return this;
	}

	public ActorChain Move(WorldPoint wp) {
		move.push(wp);
		// TouchOff();
		return this;
	}

	public ActorChain LongPress() {
		longpress.push("");
		return this;
	}

	public ActorChain Shake(Float strength) {
		shake.push(strength);
		return this;
	}

	//4.Termination
	//5.Local classes
	public class ViewList {
		private TreeMap<IActor, IView> map = new TreeMap<IActor, IView>();

		ViewList() {
			super();
		}

		synchronized boolean remove(IAnimation ef) {
			if (map.containsKey(ef)) {
				map.remove(ef);
			}
			kick();
			return true;
		}

		// @Override
		public synchronized boolean show(Object canvas) {
			for (IView i : map.values()) {
				i.view_impl(canvas);
			}
			return true;
		}

		public synchronized boolean add(IAnimation a) {
			map.put(a, a);
			kick();
			return true;
		}

		public int getSize() {
			return map.size();
		}
	}

	public interface IAnimation extends IActor, IView {
	}

	public interface IActorReset {
		public boolean actorReset() throws ChainException;
	}

	public interface IView {
		public boolean view_impl(Object canvas);
		public IView setCenter(WorldPoint point);
	}

	public interface ISound {
		public boolean play_impl();
		public boolean stop_impl();
		public boolean wait_end_impl() throws InterruptedException;
		public boolean reset_async_impl();
		public boolean reset_sound_impl();
	}

	public interface IRecorder {
		public boolean record_start() throws ChainException;
		public boolean record_stop();
	}

	public interface TapChainLight {
		public boolean turn_on();
		public boolean turn_off();
		public boolean change_color(int r, int g, int b, int a);
	}

	public interface IFilter {
		public boolean filter(Object obj) throws ChainException;
	}

	public interface IPush {
		public Actor push(Object obj);
	}


	public interface TapChainControllable {
		public Actor ctrlStart() throws ChainException, InterruptedException;
		public Actor ctrlStop();
		public Actor ctrlReset() throws ChainException, InterruptedException;
	}
	
}
