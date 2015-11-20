package org.tapchain.core;

import java.util.ArrayList;
import java.util.List;


public class ActorChain extends Chain {
	ViewList vlist = null;
//	Actor touchOn = new Actor().setPushClass(IPoint.class);
////	static Actor touchOff = new Actor().setPushClass(IPoint.class);
//	Actor move = new Actor().setPushClass(IPoint.class);
//	Actor fling = new Actor().setPushClass(IPoint.class);
//	Actor longpress = new Actor().setPushClass(IPoint.class);
//	Actor shake = new Actor().setPushClass(IPoint.class);
//	Actor error = new Actor().setPushClass(IPoint.class);
//	static Actor touchSw = new Actor().setPushClass(IPoint.class);
	// static PieceState globalState = new PieceState();

	// 1.Initialization
	public ActorChain(int time) {
		super(Chain.AUTO_MODE, time);
//		touchOn.setControlled(false);
////		touchOff.setControlled(false);
//		move.setControlled(false);
//		fling.setControlled(false);
//		longpress.setControlled(false);
//		shake.setControlled(false);
//		touchSw.setControlled(false);
		vlist = new ViewList();
		// shake.setOutDefaultType(PackType.EVENT, IOType.TOGGLE);
		setCallback(null);
	}

	public ActorChain() {
		this(30);
	}

	// 2.Getters and setters
	public int getViewNum() {
		return vlist.getSize();
	}

	public ActorChain Show(Object canvas) {
		vlist.show(canvas);
		return this;
	}
	
	public ActorChain add(IViewCallback v) {
		vlist.add(v);
		return this;
	}
	
	public ActorChain remove(IViewCallback v) {
		vlist.remove(v);
		return this;
	}

	// 3.Changing state
//	public ActorChain TouchOn(IPoint iPoint) {
//		touchOn.push(iPoint.copy());
//		touchSw.push(Boolean.valueOf(true));
//		return this;
//	}

//	public ActorChain TouchClear() {
//		touchOn.clearPull();
//		touchOn.clearPush();
//		return this;
//	}
//
//	public ActorChain TouchOff() {
////		touchOff.push("");
////		touchSw.push(Boolean.valueOf(false));
//		return this;
//	}
//
//	public ActorChain Fling(WorldPoint wp) {
//		fling.push(wp);
////		TouchOff();
//		return this;
//	}
//
//	public ActorChain Move(WorldPoint wp) {
//		move.push(wp);
//		// TouchOff();
//		return this;
//	}
//
//	public ActorChain LongPress() {
//		longpress.push("");
//		return this;
//	}
//
//	public ActorChain Shake(Float strength) {
//		shake.push(strength);
//		return this;
//	}

	// 4.Termination
	// 5.Local classes
	public class ViewList {
		private List<IViewCallback> map = new ArrayList<IViewCallback>();

		ViewList() {
			super();
		}

		synchronized boolean remove(IViewCallback ef) {
			if (map.contains(ef)) {
				map.remove(ef);
			}
			kick((IPiece)ef);
			return true;
		}

		// @Override
		public synchronized boolean show(Object canvas) {
			for (IViewCallback i : map) {
				i.view_impl(canvas);
			}
			return true;
		}

		public synchronized boolean add(IViewCallback a) {
			map.add(a);
			kick((IPiece)a);
			return true;
		}

		public int getSize() {
			return map.size();
		}
	}

	public interface IActorInit {
		public boolean actorInit() throws ChainException, InterruptedException;
	}
	
	public interface IViewCallback {
		void view_init() throws ChainException;
		boolean view_impl(Object canvas);
	}
	
	public interface IView extends IValue<IPoint> {
		public IView setAlpha(int i);
		public IView setCenter(IPoint iPoint);
		public IPoint getCenter();
		public String getName();
		public boolean view_user(Object canvas, IPoint iPoint, IPoint size,
				int alpha, float angle);
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

	public interface ILight {
		public boolean turn_on();

		public boolean turn_off();

		public boolean change_color(int r, int g, int b, int a);
	}

	public interface IPush {
		public Actor push(Object obj);
	}

	public interface IControllable {
		public void ctrlStart() throws ChainException, InterruptedException;
		public void ctrlStop() throws ChainException;

	}

}
