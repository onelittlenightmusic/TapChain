package org.tapchain.core;

import java.util.ArrayList;
import java.util.List;


public class ActorChain extends Chain {
	ViewList vlist = null;

	// 1.Initialization
	public ActorChain(int time) {
		super(Chain.AUTO_MODE, time);
		vlist = new ViewList();
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
			kick(ef);
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
			kick(a);
			return true;
		}

		public int getSize() {
			return map.size();
		}
	}

	public interface IActorInit {
		boolean actorInit() throws ChainException, InterruptedException;
	}
	
	public interface IViewCallback {
		void view_init() throws ChainException;
		boolean view_impl(Object canvas);
	}
	
	public interface IView extends IValue<IPoint> {
		IView setAlpha(int i);
		IView setCenter(IPoint iPoint);
		IPoint getCenter();
		String getName();
		boolean view_user(Object canvas, IPoint iPoint, IPoint size,
				int alpha, float angle);
	}
	

	public interface ISound {
		boolean play_impl();
		boolean stop_impl();
		boolean wait_end_impl() throws InterruptedException;
		boolean reset_async_impl();
		boolean reset_sound_impl();
	}

	public interface IRecorder {
		boolean record_start() throws ChainException;
		boolean record_stop();
	}

	public interface ILight {
		boolean turn_on();
		boolean turn_off();
		boolean change_color(int r, int g, int b, int a);
	}

	public interface IPush {
		Actor push(Object obj);
	}


}
