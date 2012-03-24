package org.tapchain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.tapchain.AnimationChain.BasicView;
import org.tapchain.AnimationChainManager.StatusHandler;
import org.tapchain.ChainController.ControlCallback;
import org.tapchain.TapChainEditor.IWindow;

import android.util.Log;
import android.util.Pair;


@SuppressWarnings("serial")
public class AnimationChain extends Chain {
	private ViewList vlist = null;
	static BasicPiece touchOn = new BasicPiece();
	static BasicPiece touchOff = new BasicPiece();
	static BasicPiece move = new BasicPiece();
	static BasicPiece fling = new BasicPiece();
	static BasicPiece longpress = new BasicPiece();
	static BasicPiece shake = new BasicPiece();
	static BasicPiece error = new BasicPiece();
	static BasicPiece touchSw = new BasicPiece();
	// static PieceState globalState = new PieceState();
	static int num = 0;

	public AnimationChain() {
		super(Chain.AUTO_MODE);
		touchOn.setControlled(false);
		touchOff.setControlled(false);
		move.setControlled(false);
		fling.setControlled(false);
		longpress.setControlled(false);
		shake.setControlled(false);
		touchSw.setControlled(false);
		vlist = new ViewList();
		// shake.setOutDefaultType(PackType.EVENT, IOType.TOGGLE);
		Set(null);
	}

	public int getViewNum() {
		return vlist.getSize();
	}
	
	public AnimationChain Show(Object canvas) {
		vlist.show(canvas);
		return this;
	}

	public class ViewList {
		private TreeMap<TapChain, TapChainViewI> map = new TreeMap<TapChain, TapChainViewI>();

		ViewList() {
			super();
		}

		synchronized boolean remove(TapChainAnimation ef) {
			if (map.containsKey(ef)) {
				map.remove(ef);
			}
			return true;
		}

		// @Override
		public synchronized boolean show(Object canvas) {
			for (TapChainViewI i : map.values()) {
				i.view_impl(canvas);
			}
			return true;
		}

		public synchronized boolean add(TapChainAnimation a) {
			map.put(a, a);
			return true;
		}

		public int getSize() {
			return map.size();
		}
	}

	public AnimationChain TouchOn(WorldPoint wp) {
		touchOn.push(wp);
		touchSw.push(new Boolean(true));
		return this;
	}

	public AnimationChain TouchOff() {
		touchOff.push("");
		touchSw.push(new Boolean(false));
		return this;
	}

	public AnimationChain Fling(WorldPoint wp) {
		fling.push(wp);
		TouchOff();
		return this;
	}

	public AnimationChain Move(WorldPoint wp) {
		move.push(wp);
		// TouchOff();
		return this;
	}

	public AnimationChain LongPress() {
		longpress.push("");
		return this;
	}

	public AnimationChain Shake(Float strength) {
		shake.push(strength);
		return this;
	}

	public static class BasicPiece extends FlexChainPiece implements
			TapChain, TapChainPushEvent, TapChainPullEvent, Comparable<BasicPiece>, PieceHead {
		private TapChain effect = null;
		private TapChainReset _reset = null;
		private Boolean animation_loop = false, live = false;
		// SyncQueue<Boolean> liveQ = new SyncQueue<Boolean>();
		BasicPiece target = null;
		ArrayList<Object> reactor = null;
		ConcurrentLinkedQueue<BasicPiece> member = new ConcurrentLinkedQueue<BasicPiece>();
		BasicPiece parent = null;
		LinkedList<Integer> generation = new LinkedList<Integer>();
		int time = 0;

		BasicPiece() {
			super();
			super.setFunc(this);
			setEffect(this);
			setInPackType(PackType.HEAP, InputType.FIRST);
//			setLoop(null);
		}

		@Override
		public boolean pieceReset(PieceBody f) {
			live = false;
			reactor = null;
			return true;
		}

		@Override
		public boolean pieceImpl(PieceBody f) throws ChainException,
				InterruptedException {
			/* when this animation start */
			if (effect == null) {
				live = false;
			} else {
				if (!live) {
					__exec(reactor = onBasicPieceInitialize(), "BasicPiece#impl@init");
//					if (effect != null)
//						Thread.sleep(effect.getDuration());
					if (_reset != null)
						_reset.effect_reset();
					time = 0;
				}
				setLive(true);
				__exec(live = effect.effect_run(),"BasicPiece#impl@run");
				__exec(time++, "BasicPiece#impl@time");
			}
			if (!live) {
				onBasicPieceTerminate(reactor);
				/* when this animation end */
			}
			return __exec(animation_loop || live, "BasicPiece#impl@end");
		}

		boolean outthis = true;
		public synchronized ArrayList<Object> onBasicPieceInitialize()
				throws InterruptedException {
			ArrayList<Object> rtn = null;
			if(outthis)
				__exec(outputAllSimple(PackType.FAMILY, this), "BasicPiece#init@sendthis");
			__exec(input(PackType.EVENT), "BasicPiece#init@sendevent");
//			rtn = inputPeek(PackType.PASSTHRU);
			return rtn;
		}
		
		public void onBasicPieceTerminate(ArrayList<Object> rtn)
			throws InterruptedException {
			__exec(outputAllSimple(PackType.EVENT, this), "BasicPiece#end@");
			_root_chain.Kick();
		}

		public BasicPiece kickFamily(boolean out) {
			outthis = out;
			return this;
		}
		
		int mynum = ++num;

		@Override
		public int compareTo(BasicPiece obj) {
//			if()
			return - mynum + obj.mynum;
		}
		
		public BasicPiece addMember(BasicPiece bp) throws ChainException {
			member.add(bp);
//			Log.w("ChainMaster", "member_"+getClass().getCanonicalName()+"?"+bp.getClass().getCanonicalName());

//			bp.parent = this;
//			bp.generation.addAll(generation);
//			bp.generation.add(member.size());
			return this;
		}

		public BasicPiece removeMember(BasicPiece bp) {
			member.remove(bp);
			return this;
		}
		
		public BasicPiece end() {
			for(BasicPiece bp : member)
				bp.end();
			super.end();
			return this;
		}

		// public BasicPiece endWithAtLeastOneOutput(boolean type) {
		// outputType = type;
		// return this;
		// }

		private void setLive(boolean b) throws InterruptedException {
			live = b;
			// liveQ.sync_push(b);
		}

		/*
		 * private boolean isDead() { return !live; } protected boolean waitAwake()
		 * { while(isDead()) { try { liveQ.sync_pop(); } catch (InterruptedException
		 * e) { e.printStackTrace(); return false; } } return true; } protected
		 * synchronized boolean waitDead() { while(live) { try { liveQ.sync_pop(); }
		 * catch (InterruptedException e) { e.printStackTrace(); return false; } }
		 * return true; }
		 */
		BasicPiece(final TapChain ef) {
			this();
			setEffect(ef);
		}

		protected void setEffect(final TapChain ef) {
			effect = ef;
		}

		protected int getTime() {
			return time;
		}

		protected BasicPiece resetTime() {
			time = 0;
			return this;
		}

//		@Override
//		protected boolean onError(ChainException e) {
//			if (_error != null)
//				_error.ErrorHandler(this, e);
//			changeState(PieceState.ERROR);
//			return super.onError(e);
//		}
//		
//		@Override
//		protected boolean onUnerror(ChainException e) {
//			if(_error != null)
//				_error.ErrorCanceller(this, e);
//			restoreState();
//			return super.onUnerror(e);
//		}
//		
//		@Override
//		protected boolean changeState(PieceState state) {
//			__exec_and_log(state.toString(), "changeState");
//			if(_statush != null)
//				_statush.getStateAndSetView(this, state.ordinal());
//			return super.changeState(state);
//		}
//		
//		@Override
//		public boolean tick() {
//			if(_statush != null)
//				_statush.tickView();
//			return super.tick();
//		}
//		
//		@Override
//		protected PieceState restoreState() {
//			PieceState s = super.restoreState();
////			Log.w("TapChain_AC", "restoreState called: "+s.toString());
//			if(_statush != null)
//				_statush.getStateAndSetView(this, s.ordinal());
//			return s;
//		}
//		
		protected BasicPiece setEraser(boolean erase) {
			return this;
		}

		protected BasicPiece disableLoop() {
			animation_loop = false;
			return this;
		}

		protected BasicPiece setLoop(TapChainReset reset) {
			_reset = reset;
			animation_loop = true;
			return this;
		}

		@Override
		public Pair<ChainPiece, ChainPath> appendTo(PackType stack,
				ChainPiece cp, PackType stack_target) throws ChainException {
			Pair<ChainPiece, ChainPath> i = super.appendTo(stack, cp, stack_target);
			// setReactor(cp.getReactor());
			try {
				target = (BasicPiece) (i.first);
				__exec(String.format("target %s: append %s",
						i.first.getName(), getName()), "BasicPiece#APPEND");
			} catch (ClassCastException e1) {
				throw new ChainException(this,
						"BasicEffect: target is not a BasicPiece");
			}
			if(stack == PackType.FAMILY && stack_target == PackType.FAMILY)
				target.addMember(this);
			return i;
		}

		@Override
		public BasicPiece detachFrom(ChainPiece cp) {
			super.detachFrom(cp);
			return this;
		}
		
		@Override
		public BasicPiece detached(ChainPiece cp) {
			super.detached(cp);
			removeMember((BasicPiece)cp);
			return this;
		}
		public Pair<ChainPiece, ChainPath> connectToPush(BasicPiece _push) {
			Pair<ChainPiece, ChainPath> o = null;
			if (_push != null) {
				try {
					o = appendTo(PackType.HEAP, _push, Object.class/* cls */,
							PackType.HEAP);
					// Log.w("ConnectToPush", fImpl.name
					// + "/Connection to PushEvent : OK");
				} catch (ChainException e) {
					__exec(getName() + "/Connection to PushEvent : NG", "BasicPiece#ConnectToPush");
					e.printStackTrace();
				}
			}
			return o;
		}

		public Pair<ChainPiece, ChainPath> connectToKick(BasicPiece _kick) {
			Pair<ChainPiece, ChainPath> o = null;
			if (_kick != null) {
				try {
					o = appendTo(PackType.EVENT, _kick, Object.class/* cls */,
							PackType.EVENT);
				} catch (ChainException e) {
					__exec(getName() + "/Connection to PushEvent : NG", "BasicPiece#ConnectToKick");
					e.printStackTrace();
				}
			}
			return o;
		}

		@Override
		public boolean filter(Object obj) throws ChainException {
			return true;
		}

		@Override
		public BasicPiece push(Object obj) {
			if(obj == null)
				return this;
			try {
				getOutPack(PackType.HEAP).outputAllSimple(obj);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return this;
		}

		public void kick() {
			try {
				__exec(getOutPack(PackType.EVENT).outputAllSimple(""), "BasicKicker");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void clearKick() {
			__exec(getOutPack(PackType.EVENT).clear(), "BasicKicker");
		}

		public BasicPiece createPush() {
			BasicPiece p = new BasicPiece();
			connectToPush(p);
			return p;
		}

		Object pull(ChainInPathPack in) throws ChainException {
			try {
				ArrayList<Object> rtn = in.input();
				if(rtn == null)
					throw new ChainException(this, "BasicPiece.pull(): null", LoopableError.LOCK);
				return rtn.get(0);
			} catch (InterruptedException e) {
				throw new ChainException(this, "BasicPiece: Interrupted in pull()");
			}
		}
		
		Object pull() throws ChainException {
			return pull(getInPack(PackType.HEAP));
		}
		
		Object pull(int i) throws ChainException  {
			return pull();
		}

		public BasicPiece postRegister(AnimationChainManager maker) {
			return this;
		}

		public Object call(Object obj) throws ChainException {
			push(obj);
			// return pull();
			return obj;
			// throw new ChainException("call Fail");
		}

		@Override
		public int getDuration() {
			return 0;
		}

		public BasicPiece getParent(PackType type) throws ChainException {
			return (BasicPiece)pull(getInPack(type));//parent;
		}

		public ArrayList<Object> getReactor() throws ChainException {
			if (reactor != null)
				return reactor;
			throw new ChainException(this, "BasicPiece.getReactor(): null reactor", LoopableError.LOCK);
		}

		public BasicView getReactorView(int i) throws ChainException {
			return (BasicView) getReactorCtrl(i);
		}

		public Control getReactorCtrl(int i) throws ChainException {
			if (i < 0)
				return getParentCtrl();
			if (i >= getReactor().size())
				throw new ChainException(this,
						"getReactorView(): invalid Reactor number");
			// BasicView rtn = (BasicView) (getReactor().get(i));
			Control rtn = (Control) (getReactor().get(i));
			rtn.waitWake();
			return rtn;
		}

		public Control getParentCtrl() throws ChainException {
			Control rtn = (Control) getParent(PackType.FAMILY);
			if (rtn == null)
				throw new ChainException(this, "getParentView(): null", LoopableError.LOCK);
			rtn.waitWake();
			return rtn;
		}

		public BasicView getParentView() throws ChainException {
			return (BasicView) getParentCtrl();
		}

		public Pair<ChainPiece, ChainPath> setCalledFunction(BasicPiece called)
				throws ChainException {
			// Called Function is supposed to be registered to AnimationChain by
			// user;
			called.connectToPush(this);
			return connectToPush(called);

		}
		@Override
		public boolean effect_run() throws ChainException, InterruptedException {
			return false;
		}
	}

	public abstract static class Loop extends BasicPiece implements TapChainReset {
		Loop() {
			super();
			super.setLoop(this);
		}

		@Override
		public void effect_reset() throws ChainException {
//			getInPack(PackType.HEAP).reset();
			return;
		}
	}

	public abstract static class LoopBoost extends Loop {
		LoopBoost() {
			super();
			super.boost();
		}
	}

	public static class Control extends BasicPiece implements TapChainControllable {
		CountDownLatch state = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		boolean auto = false;

		Control() {
			super();
		}

		@Override
		public boolean effect_run() throws InterruptedException, ChainException {
			_ctrlReset();
			_ctrlStart();
			boolean rtn = _waitFinish();
			_ctrlStop();
			return rtn;
		}

		//Every Termination calls _ctrlStop()
		@Override
		public void onTerminate() throws ChainException {
			_ctrlStop();
		}

		private Control _wake(boolean stat) {
			if (stat)
				state.countDown();
			else
				state = new CountDownLatch(1);
			return this;
		}

		public Control waitWake() throws ChainException {
			try {
				state.await();
			} catch (InterruptedException e) {
				throw new ChainException(this, "LoopCtrl: Interrupted in waitWake()", LoopableError.INTERRUPT);
			}
			return this;
		}

		boolean end = false;
		public Control finish(boolean cont) {
			if (auto)
				return this;
			if(finish!=null) {
				end = !cont;
				finish.countDown();
			}
			return this;
		}

		private boolean _waitFinish() throws ChainException {
			if (auto)
				return !end;
			finish = new CountDownLatch(1);
			try {
				finish.await();
			} catch (InterruptedException e) {
				throw new ChainException(this, "LoopCtrl: Interrupted in waitFinish()", LoopableError.INTERRUPT);
			}
			return !end;
		}

		public Control permitAutoRestart() {
			auto = true;
			return this;
		}

		private Control _ctrlStart() throws ChainException, InterruptedException {
			ctrlStart();
			_wake(true);
			return this;
		}

		private Control _ctrlStop() {
			_wake(false);
			__exec(ctrlStop(), "BasicView_Stop");
			// try {
			// clearAllThis();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			return this;
		}

		private Control _ctrlReset() throws ChainException {
			end = false;
			__exec(ctrlReset(), "BasicView_Reset");
			return this;
		}

		@Override
		public Control ctrlStart() throws ChainException {
			return this;
		}

		@Override
		public Control ctrlStop() {
			return this;
		}

		@Override
		public Control ctrlReset() throws ChainException {
			return this;
		}
	}

	public static abstract class BasicView extends Control implements
			TapChainAnimation {
		WorldPoint _wp = new WorldPoint();
		WorldPoint _size = new WorldPoint(30, 30);
		WorldPoint _percent = new WorldPoint(100, 100);
		int _Alpha = 255;
		private float _Angle = 0.0f;
		static IWindow w = null;
		int color = 0;

		BasicView() {
			super();
		}
		
		protected void addViewToAnimation() {
			((AnimationChain) _root_chain).vlist.add(this);
		}

		protected void removeViewFromAnimation() {
			((AnimationChain) _root_chain).vlist.remove(this);
		}
		
		public BasicView setWindow(IWindow window) {
			w = window;
			return this;
		}

		public IWindow getWindow() {
			return w;
		}

		public BasicView ctrlStop() {
			removeViewFromAnimation();
			return this;
		}

		public BasicView ctrlStart() {
			addViewToAnimation();
			push(this);
			return this;
		}

		public BasicView ctrlReset() throws ChainException {
			view_init();
			return this;
		}

		@Override
		public final boolean view_impl(Object canvas) {
			return view_user(canvas, getCenter().getScreenPoint(w), getSize(),
					getAlpha(), getAngle());
		};

		public boolean view_user(Object canvas, ScreenPoint sp, WorldPoint size,
				int alpha, float angle) {
			return true;
		}

		public void view_init() throws ChainException {
		}

		public BasicView setSize(WorldPoint size) {
				_size = size;
			return this;
		}
		
		public BasicView setPercent(WorldPoint effectp) {
				_percent = effectp;
		return this;
		}

		public BasicView setCenter(WorldPoint pos) {
			_wp = pos;
			return this;
		}
		public final BasicView addSize(WorldPoint size) {
			setSize(getSize().plus(size));
			return this;
		}
	
		public final BasicView addPercent(WorldPoint effectp) {
			setPercent(getPercent().plus(effectp));
			return this;
		}

		public final BasicView addCenter(WorldPoint pos) {
			setCenter(getCenter().plus(pos));
			return this;
		}
		
		public BasicView setAlpha(int a) {
			_Alpha = a;
			return this;
		}
		
		public BasicView setColor(int _color) {
			color = _color;
			return this;
		}

		public WorldPoint getSize() {
			return _size;
		}

		public WorldPoint getPercent() {
			return _percent;
		}

		public WorldPoint getCenter() {
			return _wp;
		}

		public int getAlpha() {
			return _Alpha;
		}
		
		public int getColor() {
			return color;
		}

		public void setAngle(float _Angle) {
			this._Angle = _Angle;
		}

		public float getAngle() {
			return _Angle;
		}
		

	}
	
	public static class EffectSkelton extends Loop {
		private Control t;
		PackType parent_type = PackType.FAMILY;
		public EffectSkelton setParentType(PackType type) {
			parent_type = type;
			return this;
		}

		@Override
		public void effect_reset() throws ChainException {
			try {
				setTarget((Control) (getParent(parent_type)));
			} catch(ClassCastException e) {
				throw new ChainException(this, "EffectSkelton: Failed to get Parent", LoopableError.LOCK);
			}
		}

		@Override
		public boolean effect_run() throws ChainException {
			return false;
		}

		public void setTarget(Control t) {
			this.t = t;
		}

		public Control getTarget() {
			return t;
		}
	}

	public static class ResetEffect extends EffectSkelton {
		boolean cont = false;
		@Override
		public boolean effect_run() {
			getTarget().finish(cont);
			return false;
		}
		public ResetEffect setContinue(boolean cont) {
			this.cont = cont;
			return this;
		}
	}

	public static class SleepEffect extends EffectSkelton {
		@Override
		public boolean effect_run() throws ChainException {
//			LoopControl t = (LoopControl) (getReactor().get(0));
			getTarget().waitWake();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				throw new ChainException(this, "SleepEffect: Interrupted", LoopableError.INTERRUPT);
			}
			return false;
		}
	}
	
	public static class ViewEffectSkelton<T> extends EffectSkelton {
		private BasicView target_view = null, target_cache = null;
		private int _i = 0, _duration = 0;
		T effect_val = null, cache = null;
		@Override
		public void effect_reset() throws ChainException {
			super.effect_reset();
//			setTargetView((BasicView)getTarget());
			resetTargetView();
			setCounter(0);
			resetEffectValue();
		}
		public ViewEffectSkelton<T> setCounter(int _i) {
			this._i = _i;
			return this;
		}
		public int getCounter() {
			return _i;
		}
		public boolean increment() {
			return ++_i < _duration;
		}
		public ViewEffectSkelton<T> setTargetView(BasicView target_view) {
			this.target_view = target_view;
			return this;
		}
		public BasicView getTargetView() {
			return target_cache;
		}
		public ViewEffectSkelton<T> resetTargetView() throws ChainException {
			target_cache = (target_view!= null)? target_view:(BasicView)getTarget();
			return this;
		}
		public ViewEffectSkelton<T> initEffectValue(T val, int duration) {
			effect_val = val;
			_duration = duration;
			if(effect_val != null)
				cache = effect_val;
			return this;
		}
		@SuppressWarnings("unchecked")
		public ViewEffectSkelton<T> resetEffectValue() throws ChainException {
			cache = (effect_val!= null)? effect_val:(T) pull();
			return this;
		}
		public T getEffectValue() throws ChainException {
			return cache;
		}
	}

	public static class MoveViewEffect extends ViewEffectSkelton<WorldPoint> {
		public MoveViewEffect move_init(WorldPoint direction, int duration) {
			initEffectValue(direction, duration);
			return this;
		}

		@Override
		public boolean effect_run() throws ChainException {
			WorldPoint _dir = getEffectValue();//(_direction != null)? _direction:(WorldPoint) pull();
			BasicView _t = getTargetView();
			synchronized (_t) {
				switch(_dir.getEffect()) {
				case POS:
					__exec(_t.setCenter(_dir), "MVE#run#set");
					break;
				case DIF:
					__exec(_t.addCenter(_dir), "MVE#run#add");
					break;
				}
			}
			return __exec(increment(), "MVE#end");
		}
	}
	

	public static class SizeEffect extends ViewEffectSkelton<WorldPoint> {
		public SizeEffect size_init(WorldPoint direction, int duration) {
			initEffectValue(direction, duration);
			return this;
		}

		@Override
		public boolean effect_run() throws ChainException {
			BasicView _t = getTargetView();
			synchronized (_t) {
				_t.setPercent(_t.getPercent().plus(getEffectValue()));
			}
			return increment();
		}
	}

	public static class AlphaEffect extends ViewEffectSkelton<Integer> {
		public AlphaEffect alpha_init(int direction, int duration) {
			initEffectValue(direction, duration);
			return this;
		}

		@Override
		public boolean effect_run() throws ChainException {
			BasicView _t = getTargetView();
				synchronized (_t) {
					_t.setAlpha(_t.getAlpha() + getEffectValue());
				}
				return increment();
		}
	}
	
	public static class RotateEffect extends ViewEffectSkelton<Integer> {
		public RotateEffect alpha_init(int direction, int duration) {
			initEffectValue(direction, duration);
			return this;
		}

		@Override
		public boolean effect_run() throws ChainException {
			BasicView _t = getTargetView();
				synchronized (_t) {
					_t.setAngle(_t.getAngle() + getEffectValue());
				}
				return increment();
		}
	}

	public static class ColorViewEffect extends ViewEffectSkelton<Integer> {
		public ColorViewEffect color_init(int _color) {
			initEffectValue(_color, 0);
			return this;
		}
		@Override
		public boolean effect_run() throws ChainException {
			synchronized(getTargetView()) {
				getTargetView().setColor(getEffectValue());
			}
			return false;
		}
	}

	public static class HeapFilter extends LoopBoost {
		@Override
		public boolean effect_run() throws InterruptedException, ChainException {
			Object event = pull();
			boolean rtn = filter(event);
			if (rtn) {
				push(event);
			}
			return !rtn;
			// if filter() returns true, output and exit single function
			// loop
		}

	}

	public abstract static class SimpleRelationFilter extends LoopBoost {
		private static final long serialVersionUID = 1L;

		SimpleRelationFilter() {
			super();
		}

		@Override
		public boolean effect_run() throws InterruptedException, ChainException {
			Object event = null;//pull();
			boolean rtn = false;
//			for (Object obj : getReactor()) {
			Object obj = null;//pull();
				rtn |= relation_impl((BasicView) obj, (BasicView) event);
				push(new Pair<Object, Object>(event, obj));
//			}
			return !rtn;
		}

		public abstract boolean relation_impl(BasicView a, BasicView b) throws InterruptedException;
	}

	public static class BasicMerge extends LoopBoost {
		BasicMerge() {
			super();
			setInPackType(PackType.EVENT, InputType.FIRST);
		}

	}

	public static class BasicSplit extends LoopBoost {
		Pair<ChainPiece, ChainOutConnector> o = null;
		Class<?> cls = null;

		BasicSplit(Class<?> _cls) {
			super();
			try {
				o = super.appended(_cls, OutType.SYNC, PackType.HEAP, this);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			cls = _cls;
		}

		@Override
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> _cls, OutType type,
				PackType stack_target, ChainPiece from) throws ChainException {
			if (stack_target == PackType.EVENT)
				return o;
			return null;
		}
	}

	static class WorldPointFilter extends HeapFilter {
		WorldPointFilter(BasicPiece p) {
			super();
			connectToPush(p);
		}

		WorldPoint getWorldPoint() throws ChainException {
			return (WorldPoint) pull();
		}
	}

	public static class TouchFilter extends WorldPointFilter {
		TouchFilter() {
			super(touchOn);
		}
	}

	public static class TouchUpFilter extends HeapFilter {
		TouchUpFilter() {
			super();
			connectToPush(touchOff);
		}
	}

	public static class MoveFilter extends WorldPointFilter {
		MoveFilter() {
			super(move);
		}
	}

	public static class FlingFilter extends WorldPointFilter {
		FlingFilter() {
			super(fling);
		}
	}

	public static class LongPressFilter extends HeapFilter {
		LongPressFilter() {
			super();
			connectToPush(longpress);
		}

	}

	public static class ShakeFilter extends HeapFilter {
		ShakeFilter() {
			super();
			connectToPush(shake);
		}
	}

	public static class LockSingle extends HeapFilter {
		static BasicPiece open = new BasicPiece();

		LockSingle() {
			super();
			connectToPush(open);
		}

		@Override
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls, OutType type,
				PackType stack_target, ChainPiece from) throws ChainException {
			Pair<ChainPiece, ChainOutConnector> rtn = super.appended(cls, type,
					stack_target, from);
			open.push("");
			return rtn;
		}
	}

	public static class WaitEndHeap extends HeapFilter {
		WaitEndHeap() {
			super();
		}

		@Override
		public boolean filter(Object obj) {
			return true;
		}

	}

//	public static class BasicKicker extends BasicPiece {
//		// SyncObject<String> sync = new SyncObject<String>();
//		boolean kicked = false;
//
//		BasicKicker() {
//			super();
//			suspend();
//		}
//
//		public void kick() {
//			super.kick();
//			kicked = true;
//		}
//	}

	public static abstract class BasicGate<T> extends HeapFilter {
		BasicGate() {
			super();
			super.setEffect(new TapChain() {
				@Override
				public boolean effect_run() throws InterruptedException {
					boolean rtn = inputHeapAsync();
					clearInputHeap();
					return !rtn;
				}

				@Override
				public int getDuration() {
					return 0;
				}
			});
			setLoop(null);
		}
	}

	public static class BasicState extends HeapFilter {
		BasicState() {
			super();
			setOutPackType(PackType.HEAP, OutType.HIPPO);
			setLoop(null);
		}

	}

	public static class Value extends BasicState {
		Value(Object obj) {
			super();
//				push(obj);
			setValue(obj);
		}
		public Value setValue(Object obj) {
			push(obj);
			return this;
		}
	}
	
	public static class ValueLimited extends BasicPiece {
		int count;
		ValueLimited(int count) {
			super();
			setLoop(null);
			this.count = count;
		}
		
		public ValueLimited setValue(Object obj) {
			for(int i = 0; i < count; i++)
				push(obj);
			return this;
		}
		
	}

	public static class BasicToggle extends LoopBoost {
		BasicToggle() {
			super();
			setOutPackType(PackType.EVENT, OutType.HIPPO);
			setLoop(null);
		}

		@Override
		public boolean effect_run() throws InterruptedException, ChainException {
			kick();
			pull();
			clearKick();
			return true;
			// if filter() returns true, output and exit single function
			// loop
		}
	}

	public static class TouchOnOffState extends BasicState {
		boolean state_bool = false;

		TouchOnOffState() {
			super();
			// super.connectToPush(touchSw);
			setOutPackType(PackType.EVENT, OutType.HIPPO);
//			setInPackType(PackType.HEAP, InputType.COUNT);
		}

		@Override
		public boolean filter(Object obj) {
			state_bool = !state_bool;
			if (!state_bool) {
				push(null);
				clearKick();
			}
			Log.w("TouchOnOff", state_bool ? "on" : "off" + "ed");
			return state_bool;
		}
	}

	public static abstract class BasicSound extends Control implements
			TapChainSound {
		int length = 0;

		BasicSound() {
			super();
			setControlled(false);
			setLoop(null);
		}

		public BasicSound setLength(int len) {
			length = len;
			return this;
		}

		public BasicSound ctrlStop() {
			stop_impl();
			return this;
		}

		public BasicSound ctrlStart() {
			play_impl();
			try {
				if (length == 0)
					wait_end_impl();
				else {
					Thread.sleep(length);
				}
//				finish(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// super.suspend();
			return this;
		}

		public BasicSound ctrlReset() throws ChainException {
			// try {
			// wait_end_impl();
			// waitFinish();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			reset_sound_impl();
			return this;
		}
	}

	public static abstract class BasicRecorder extends Control implements
			TpaChainRecorder {

		BasicRecorder() {
			super();
			setControlled(false);
		}

		public BasicRecorder ctrlStop() {
			record_stop();
			return this;
		}

		public BasicRecorder ctrlStart() throws ChainException {
			record_start();
			return this;
		}

		public BasicRecorder ctrlReset() {
			return this;
		}
	}

	public static abstract class BasicQuaker extends Control {
		int val = 100;

		public BasicQuaker(int interval) {
			super();
			permitAutoRestart();
			val = interval;
		}

		public abstract boolean quake_impl();

		public BasicQuaker ctrlStart() {
			quake_impl();
			return this;
		}

		public BasicQuaker ctrlStop() {
			return this;
		}

		public BasicQuaker ctrlReset() {
			return this;
		}
	}

	public static abstract class BasicLight extends Control implements
			TapChainLight {
		BasicLight() {
			super();
			super.setEffect(new TapChain() {
				@Override
				public boolean effect_run() {
					TurnOn();
					return false;
				}

				@Override
				public int getDuration() {
					return 0;
				}
			});
		}

		public abstract boolean turn_on();

		public abstract boolean turn_off();

		public abstract boolean change_color(int r, int g, int b, int a);

		public BasicLight TurnOn() {
			turn_on();
//			suspend();
			return this;
		}

		public BasicLight TurnOff() {
			turn_off();
//			suspend();
			return this;
		}

		public BasicLight ChangeColor(int r, int g, int b, int a) {
			change_color(r, g, b, a);
			return this;
		}

		public BasicLight ctrlStart() {
			TurnOn();
			return this;
		}

		public BasicLight ctrlStop() {
			TurnOff();
			return this;
		}

		public BasicLight ctrlReset() {
			TurnOff();
			return this;
		}
	}

	public static class BasicFactory extends BasicPiece {
		Class<? extends BasicPiece> cls = null;

		BasicFactory() {
			super();
			super.setEffect(new TapChain() {
				@Override
				public boolean effect_run() throws ChainException, InterruptedException {
					if (cls == null)
						return false;
					try {
						push(cls.newInstance());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new ChainException(BasicFactory.this,
								"BasicFactory: No Default Constructor");
					} catch (InstantiationException e) {
						e.printStackTrace();
						throw new ChainException(BasicFactory.this, "BasicFactory:");
					}
					return false;
				}

				@Override
				public int getDuration() {
					return 0;
				}
			});
		}

		public BasicFactory setClass(Class<? extends BasicPiece> _cls) {
			cls = _cls;
			return this;
		}

		@Override
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls, OutType type,
				PackType stack_target, ChainPiece from) throws ChainException {
			return new Pair<ChainPiece, ChainOutConnector>(this, addOutPath(cls, type,
					stack_target));
		}
	}

	public static class BasicBalloon extends Control {
		ArrayList<BasicPiece> start = new ArrayList<BasicPiece>();
		ArrayList<BasicPiece> end = new ArrayList<BasicPiece>();
		BasicPiece origin = null;
		Control eventAggre = new Control();

		BasicBalloon(AnimationChain ac, boolean hippo) {
			super();
			eventAggre.permitAutoRestart();
			if (hippo) {
				eventAggre.setOutPackType(PackType.EVENT, OutType.HIPPO);
			} else {
			}
			// eventAggre.appendTo(false, (BasicControllable)this, false);
			/*
			 * super.setFunc(new Func("BasicBalloon/"+name) {
			 * 
			 * @Override public boolean func_impl(IO f) throws InterruptedException,
			 * ChainException { onBasicBalloonInitialize(); while (start.isEmpty())
			 * wait(); for (BasicPiece startPiece : start) if(!startPiece.waitAwake())
			 * { onBasicBalloonTerminate(); throw new ChainException(
			 * "BasicBalloon: Failed to wait for Start Piece's Awakeness"); }
			 * balloonlive = true; while (end.isEmpty()) wait(); for (BasicPiece
			 * endPiece : end) if(!endPiece.waitDead()) { onBasicBalloonTerminate();
			 * throw new
			 * ChainException("BasicBalloon: Failed to wait for End Piece's Death" );
			 * } balloonlive = false; onBasicBalloonTerminate(); return false; } });
			 */}

		public BasicBalloon setOrigin(BasicPiece bp) {
			origin = bp;
			return this;
		}

		synchronized BasicBalloon addStartPiece(BasicPiece bp) {
			start.add(bp);
			notifyAll();
			return this;
		}

		synchronized boolean removeStartPiece(BasicPiece bp) {
			return start.remove(bp);
		}

		synchronized BasicBalloon addEndPiece(BasicPiece bp) {
			end.add(bp);
			notifyAll();
			return this;
		}

		synchronized boolean removeEndPiece(BasicPiece bp) {
			return end.remove(bp);
		}

		public BasicBalloon addMember(BasicPiece bp) throws ChainException {
			super.addMember(bp);
			bp.parent = this;
			if (origin != null) {
				bp.parent = origin;
				bp.appendTo(PackType.PASSTHRU, origin, PackType.FAMILY);
			}
			bp.appendTo(PackType.EVENT, eventAggre, PackType.EVENT);
			return this;
		}

		public BasicBalloon removeMember(BasicPiece bp) {
			return this;
		}

		@Override
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls, OutType type,
				PackType stack_target, ChainPiece from) throws ChainException {
			if (end.isEmpty()) {
				throw new ChainException(this,
						"BasicBalloon: no end piece assigned when this Balloon appended");
			}
			for (BasicPiece endPiece : end) {
				Pair<ChainPiece, ChainOutConnector> o = endPiece.appended(cls, type,
						stack_target, from);
				if (o != null) {
//					partner.setPartner(o.second, from);
					return o;
				}
			}
			throw new ChainException(this, String.format(
					"BasicBalloon: no end piece matching Class %s", cls.getName()));
		}

		@Override
		public Pair<ChainPiece, ChainPath> appendTo(PackType stack,
				ChainPiece cp, PackType stack_target) throws ChainException {
			if (stack != PackType.PASSTHRU) {
				// Log.w("BalloonTest", "connect to "+ cp.fImpl.name + "/" +
				// stack + "," + stack_target);
				return eventAggre.appendTo(stack, cp, stack_target);
			}
			if (start.isEmpty()) {
				Pair<ChainPiece, ChainPath> i = eventAggre.appendTo(stack, cp,
						stack_target);
				if (i != null) {
					partner.setPartner(i.second, cp);
					cp.partner.setPartner(i.second, this);
					return i;
				}
			}
			for (BasicPiece startPiece : start) {
				Pair<ChainPiece, ChainPath> i = startPiece.appendTo(stack, cp,
						stack_target);
				if (i != null) {
					partner.setPartner(i.second, cp);
					cp.partner.setPartner(i.second, this);
					return i;
				}
			}
			throw new ChainException(this,
					"BasicBalloon: no start piece matching with Target BasicPiece");
		}

		@Override
		public BasicBalloon ctrlStart() {
			// eventAggre.AnimationStart();
			return this;
		}

		@Override
		public BasicBalloon ctrlStop() {
			// eventAggre.AnimationStop();
			return this;
		}

		@Override
		public BasicBalloon ctrlReset() {
			// eventAggre.AnimationReset();
			return this;
		}

		@Override
		public BasicBalloon postRegister(AnimationChainManager maker) {
//			eventAggre.postRegister(al);
			maker.add(eventAggre);
			return this;
		}
	}

	public static abstract class TargetTouchFilter extends TouchFilter {
		BasicView target_view = null;

		TargetTouchFilter() {
			super();
		}

		public boolean filter(Object obj) {
			if (isTouching((WorldPoint) obj)) {
				return true;
			}
			return false;
		}

		public abstract boolean isTouching(WorldPoint sp);

		@Override
		public void effect_reset() throws ChainException {
			super.effect_reset();
			target_view = getReactorView(0);
		}

	}

	public static abstract class BasicViewAndTouch extends BasicView {
		TargetTouchFilter t;

		public BasicViewAndTouch() {
			super();
			t = new TargetTouchFilter() {
				@Override
				public boolean isTouching(WorldPoint sp) {
					return BasicViewAndTouch.this.isTouching(sp);
				}
			};
			t.setControlled(false);
			try {
				t.appendTo(PackType.PASSTHRU, this, PackType.FAMILY);
			} catch (ChainException e) {
				e.printStackTrace();
			}
		}

		public abstract boolean isTouching(WorldPoint wp);

		public BasicViewAndTouch postRegister(AnimationChainManager maker) {
//			t.postRegister(al);
			maker.add(t);
			return this;
		}

		@Override
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls, OutType type,
				PackType stack_target, ChainPiece from) throws ChainException {
			if (stack_target == PackType.HEAP || stack_target == PackType.EVENT)
				return t.appended(cls, type, stack_target, from);
			else
				return super.appended(cls, type, stack_target, from);
		}
	}
	
	public static class Variable extends BasicPiece {
		Variable() {
			super();
		}

		@Override
		public boolean effect_run() throws ChainException, InterruptedException {
			push(getParent(PackType.FAMILY));
			return false;
		}
	}
	
	public static class Function extends BasicPiece {
		Function() {
			super();
			kickFamily(false);
		}

		@Override
		public boolean effect_run() throws ChainException, InterruptedException {
			outputAllSimple(PackType.FAMILY, (BasicPiece) pull());
			return true;
		}
	}

	public interface TapChainAnimation extends TapChain, TapChainViewI {
	}

	public interface TapChain {
		public boolean effect_run() throws ChainException, InterruptedException;

		public int getDuration();
	}

	public interface TapChainReset {
		public void effect_reset() throws ChainException;
	}

	public interface TapChainViewI {
		public boolean view_impl(Object canvas);
		public TapChainViewI setCenter(WorldPoint point);
	}

	public interface TapChainSound {
		public boolean play_impl();
		public boolean stop_impl();
		public boolean wait_end_impl() throws InterruptedException;
		public boolean reset_async_impl();
		public boolean reset_sound_impl();
	}

	public interface TpaChainRecorder {
		public boolean record_start() throws ChainException;
		public boolean record_stop();
	}

	public interface TapChainLight {
		public boolean turn_on();
		public boolean turn_off();
		public boolean change_color(int r, int g, int b, int a);
	}

	public interface TapChainPullEvent {
		public boolean filter(Object obj) throws ChainException;
	}

	public interface TapChainPushEvent {
		public BasicPiece push(Object obj);
	}

	public interface TapChainCanvas {
	}

	public interface TapChainControllable {
		public BasicPiece ctrlStart() throws ChainException, InterruptedException;
		public BasicPiece ctrlStop();
		public BasicPiece ctrlReset() throws ChainException, InterruptedException;
	}

	public interface ErrorHandler {
		public ChainPiece ErrorHandler(ChainPiece chainPiece, ChainException e);
		public ChainPiece ErrorCanceller(ChainPiece bp, ChainException e);
	}
	
}
