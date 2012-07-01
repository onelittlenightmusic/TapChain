package org.tapchain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.tapchain.Actor.WorldPointFilter;
import org.tapchain.ActorChain.TapChainAnimation;
import org.tapchain.ActorChain.TapChainControllable;
import org.tapchain.ActorChain.IFilter;
import org.tapchain.ActorChain.IPush;
import org.tapchain.ActorChain.IActorReset;
import org.tapchain.ActorChain.TapChainLight;
import org.tapchain.ActorChain.TapChainSound;
import org.tapchain.ActorChain.IRecorder;
import org.tapchain.Chain.ConnectionIO;
import org.tapchain.Chain.ConnectionO;
import org.tapchain.Chain.Connector;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.FlexPiece;
import org.tapchain.Chain.LoopableError;
import org.tapchain.Chain.Output;
import org.tapchain.Chain.IPathListener;
import org.tapchain.Chain.IPiece;
import org.tapchain.Chain.PieceHead;

@SuppressWarnings("serial")
public class Actor extends FlexPiece
		implements IActor,
		IPush, IFilter, Comparable<Actor>, PieceHead {
	private IActorReset _reset = null;
	private Boolean animation_loop = false, live = false;
	Actor target = null;
	ConcurrentLinkedQueue<Actor> member = new ConcurrentLinkedQueue<Actor>();
	LinkedList<Integer> generation = new LinkedList<Integer>();
	int time = 0;

	Actor() {
		super();
		super.setFunc(this);
		setInPackType(PackType.HEAP, Input.FIRST);
	}

	@Override
	public boolean pieceReset(IPiece f) {
		live = false;
		return true;
	}

	@Override
	public boolean pieceRun(IPiece f) throws ChainException,
			InterruptedException {
		/* when this animation start */
		if (!live) {
			__exec(preReset(), "BasicPiece#impl@init");
			time = 0;
			if (_reset != null) {
				boolean reset_cont = __exec(_reset.actorReset(),"BasicPiece#impl@reset");
				if(!reset_cont){
					postEnd();
					return false;
				}
			}
		}
		__exec(live = actorRun(), "BasicPiece#impl@run");
		__exec(time++, "BasicPiece#impl@time");
		if (!live) {
			postEnd();
			/* when this animation end */
		}
		return __exec(animation_loop || live, "BasicPiece#impl@end");
	}

	boolean outthis = true;

	private Actor preReset()
			throws InterruptedException, ChainException {
		if (outthis)
			__exec(outputAllSimple(PackType.FAMILY, this), "BasicPiece#init@sendthis");
		__exec(input(PackType.EVENT), "BasicPiece#init@sendevent");
		return this;
	}

	private void postEnd()
			throws InterruptedException {
		__exec(outputAllSimple(PackType.EVENT, this), "BasicPiece#end@");
	}

	public Actor kickFamily(boolean out) {
		outthis = out;
		return this;
	}

	int mynum = ++ActorChain.num;

	@Override
	public int compareTo(Actor obj) {
		return -mynum + obj.mynum;
	}

	public Actor addMember(Actor bp) throws ChainException {
		member.add(bp);
		return this;
	}

	public Actor removeMember(Actor bp) {
		member.remove(bp);
		return this;
	}

	public void end() {
		for (Actor bp : member)
			if(bp != this)
				bp.end();
		super.end();
		return;
	}

	protected int getTime() {
		return time;
	}

	protected Actor resetTime() {
		time = 0;
		return this;
	}

	protected Actor setEraser(boolean erase) {
		return this;
	}

	protected Actor disableLoop() {
		animation_loop = false;
		return this;
	}

	protected Actor setLoop(IActorReset reset) {
		_reset = reset;
		animation_loop = true;
		return this;
	}

	@Override
	public ConnectionIO appendTo(PackType stack, IPiece cp,
			PackType stack_target) throws ChainException {
		ConnectionIO i = super.appendTo(stack, cp, stack_target);
		// setReactor(cp.getReactor());
		try {
			target = (Actor) (i.getPiece());
			__exec(
					String.format("target %s: append %s", i.getPiece().getName(), getName()),
					"BasicPiece#APPEND");
		} catch (ClassCastException e1) {
			throw new ChainException(this, "BasicEffect: target is not a BasicPiece");
		}
		if (stack == PackType.FAMILY && stack_target == PackType.FAMILY)
			target.addMember(this);
		return i;
	}

	@Override
	public void detached(IPiece cp) {
		super.detached(cp);
		removeMember((Actor) cp);
	}

	public ConnectionIO connectToPush(Actor _push) {
		ConnectionIO o = null;
		if (_push != null) {
			try {
				o = appendTo(PackType.HEAP, _push, Object.class,
						PackType.HEAP);
			} catch (ChainException e) {
				__exec(getName() + "/Connection to PushEvent : NG",
						"BasicPiece#ConnectToPush");
				e.printStackTrace();
			}
		}
		return o;
	}

	public ConnectionIO connectToKick(Actor _kick) {
		ConnectionIO o = null;
		if (_kick != null) {
			try {
				o = appendTo(PackType.EVENT, _kick, Object.class,
						PackType.EVENT);
			} catch (ChainException e) {
				__exec(getName() + "/Connection to PushEvent : NG",
						"BasicPiece#ConnectToKick");
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
	public Actor push(Object obj) {
		if (obj == null)
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
		__exec(getOutPack(PackType.EVENT).clearAll(), "BasicKicker");
	}

	public Actor createPush() {
		Actor p = new Actor();
		connectToPush(p);
		return p;
	}

	Object pullOne(ChainInPathPack in, int i) throws ChainException {
		try {
			Object rtn = in.inputOne(i);
			if (rtn == null)
				throw new ChainException(this, "CONNECT!",
						LoopableError.LOCK);
			return rtn;
		} catch (InterruptedException e) {
			throw new ChainException(this, "Interrupted in pull()");
		}
	}
	
	Object pull(ChainInPathPack in) throws ChainException {
		try {
			ArrayList<Object> rtn = in.input();
			if (rtn == null)
				throw new ChainException(this, "CONNECT!",
						LoopableError.LOCK);
			return rtn.get(0);
		} catch (InterruptedException e) {
			throw new ChainException(this, "Interrupted in pull()");
		}
	}

	Object pull() throws ChainException {
		return pull(getInPack(PackType.HEAP));
	}

	Object pull(int i) throws ChainException {
		return pull();
	}

	public void postRegister(ActorManager maker) {
	}

	public Object call(Object obj) throws ChainException {
		push(obj);
		// return pull();
		return obj;
		// throw new ChainException("call Fail");
	}

	public Actor getParent(PackType type) throws ChainException {
		return (Actor) pull(getInPack(type));// parent;
	}

	public Actor.Controllable getParentCtrl() throws ChainException {
		Actor.Controllable rtn = (Actor.Controllable) getParent(PackType.FAMILY);
		if (rtn == null)
			throw new ChainException(this, "getParentView(): null",
					LoopableError.LOCK);
		rtn.waitWake();
		return rtn;
	}

	public Actor.ViewActor getParentView() throws ChainException {
		return (Actor.ViewActor) getParentCtrl();
	}

	public ConnectionIO setCalledFunction(Actor called)
			throws ChainException {
		// Called Function is supposed to be registered to AnimationChain by
		// user;
		called.connectToPush(this);
		return connectToPush(called);

	}

	public Actor validate() {
		((ActorChain)_root_chain).kick();
		__log(String.format("%s kicks", getName()), "TapChain");
		return this;
	}

	@Override
	public boolean actorRun() throws ChainException, InterruptedException {
		return false;
	}
	public static abstract class ViewActor extends Actor.Controllable implements
			TapChainAnimation {
		WorldPoint _wp = new WorldPoint();
		WorldPoint _size = new WorldPoint(30, 30);
		WorldPoint _percent = new WorldPoint(100, 100);
		int _Alpha = 255;
		private float _Angle = 0.0f;
		int color = 0;

		ViewActor() {
			super();
		}

		protected void addViewToAnimation() {
			((ActorChain) _root_chain).vlist.add(this);
		}

		protected void removeViewFromAnimation() {
			((ActorChain) _root_chain).vlist.remove(this);
		}

		public ViewActor ctrlStop() {
			removeViewFromAnimation();
			return this;
		}

		public ViewActor ctrlStart() {
			addViewToAnimation();
			push(this);
			return this;
		}

		public ViewActor ctrlReset() throws ChainException {
			view_init();
			return this;
		}

		@Override
		public final boolean view_impl(Object canvas) {
			return view_user(canvas, getCenter(), getSize(),
					getAlpha(), getAngle());
		};

		public boolean view_user(Object canvas, WorldPoint sp, WorldPoint size,
				int alpha, float angle) {
			return true;
		}

		public void view_init() throws ChainException {
		}

		public ViewActor setSize(WorldPoint size) {
			_size = size;
			return this;
		}

		public ViewActor setPercent(WorldPoint effectp) {
			_percent = effectp;
			return this;
		}

		public ViewActor setCenter(WorldPoint pos) {
			_wp = pos;
			return this;
		}

		public final ViewActor addSize(WorldPoint size) {
			setSize(getSize().plus(size));
			return this;
		}

		public final ViewActor addPercent(WorldPoint effectp) {
			setPercent(getPercent().plus(effectp));
			return this;
		}

		public final ViewActor addCenter(WorldPoint pos) {
			setCenter(getCenter().plus(pos));
			return this;
		}

		public ViewActor setAlpha(int a) {
			_Alpha = a;
			return this;
		}

		public ViewActor setColor(int _color) {
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

	public static abstract class Sound extends Actor.Controllable implements
			TapChainSound {
		int length = 0;

		Sound() {
			super();
			setControlled(false);
			setLoop(null);
		}

		public Sound setLength(int len) {
			length = len;
			return this;
		}

		public Sound ctrlStop() {
			stop_impl();
			return this;
		}

		public Sound ctrlStart() {
			play_impl();
			try {
				if (length == 0)
					wait_end_impl();
				else {
					Thread.sleep(length);
				}
				// finish(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// super.suspend();
			return this;
		}

		public Sound ctrlReset() throws ChainException {
			reset_sound_impl();
			return this;
		}
	}
	
	public static abstract class Controllable extends Loop implements
			TapChainControllable {
		CountDownLatch state = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		boolean auto = false;
		boolean rtn = true;

		Controllable() {
			super();
		}

		@Override
		public boolean actorReset() throws ChainException {
			if(rtn)
				_ctrlReset();
			return rtn;
		}
		
		@Override
		public boolean actorRun() throws InterruptedException, ChainException {
//			_ctrlReset();
			_ctrlStart();
			rtn = _waitFinish();
			_ctrlStop();
			return false;
		}

		// Every Termination calls _ctrlStop()
		@Override
		public void onTerminate() throws ChainException {
			_ctrlStop();
		}

		private Controllable _wake(boolean stat) {
			if (stat)
				state.countDown();
			else
				state = new CountDownLatch(1);
			return this;
		}

		public Controllable waitWake() throws ChainException {
			try {
				state.await();
			} catch (InterruptedException e) {
				throw new ChainException(this, "LoopCtrl: Interrupted in waitWake()",
						LoopableError.INTERRUPT);
			}
			return this;
		}

		boolean end = false;

		public Controllable finish(boolean cont) {
			if (auto)
				return this;
			if (finish != null) {
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
				throw new ChainException(this, "LoopCtrl: Interrupted in waitFinish()",
						LoopableError.INTERRUPT);
			}
			return !end;
		}

		public Controllable permitAutoRestart() {
			auto = true;
			return this;
		}

		private Controllable _ctrlStart() throws ChainException, InterruptedException {
			ctrlStart();
			_wake(true);
			return this;
		}

		private Controllable _ctrlStop() {
			_wake(false);
			__exec(ctrlStop(), "BasicView_Stop");
			// try {
			// clearAllThis();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			return this;
		}

		private Controllable _ctrlReset() throws ChainException {
			end = false;
			__exec(ctrlReset(), "BasicView_Reset");
			return this;
		}

		@Override
		public Controllable ctrlStop() {
			return this;
		}

		@Override
		public Controllable ctrlReset() throws ChainException {
			return this;
		}
	}

	public static abstract class Txn<T> extends
			Actor.EffecterSkelton<T> {
		@Override
		public boolean actorRun() throws ChainException {
			ViewActor _t = getTargetView();
			synchronized (_t) {
				txn(_t);
			}
			validate();
			return __exec(increment(), "TE#end");
		}

		public abstract void txn(ViewActor _t) throws ChainException;
	}

	public static class Mover extends
			Actor.EffecterSkelton<WorldPoint> {

		@Override
		public boolean actorRun() throws ChainException {
			WorldPoint _dir = getEffectValue();// (_direction != null)?
																					// _direction:(WorldPoint) pull();
			ViewActor _t = getTargetView();
			synchronized (_t) {
				switch (_dir.getEffect()) {
				case POS:
					__exec(_t.setCenter(_dir), "MVE#run#set");
					break;
				case DIF:
					__exec(_t.addCenter(_dir), "MVE#run#add");
					break;
				}
			}
			validate();
			return __exec(increment(), "MVE#end");
		}
	}

	public static class Sizer extends Txn<WorldPoint> {
		public Sizer size_init(WorldPoint direction, int duration) {
			initEffect(direction, duration);
			return this;
		}

		@Override
		public void txn(ViewActor _t) throws ChainException {
			_t.setPercent(_t.getPercent().plus(getEffectValue()));
		}
	}

	public static class Sleeper extends Actor.Effecter {
		@Override
		public boolean actorRun() throws ChainException {
			// LoopControl t = (LoopControl) (getReactor().get(0));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				throw new ChainException(this, "SleepEffect: Interrupted",
						LoopableError.INTERRUPT);
			}
			return false;
		}
	}

	public static class Resetter extends Actor.Effecter {
		boolean cont = false;

		@Override
		public boolean actorRun() {
			getTarget().finish(cont);
			return false;
		}

		public Resetter setContinue(boolean cont) {
			this.cont = cont;
			return this;
		}
	}

	public static class Value extends Actor.BasicState {
		public Value(Object obj) {
			super();
			// push(obj);
			setValue(obj);
		}

		public Value setValue(Object obj) {
			push(obj);
			return this;
		}
	}

	public static class ValueLimited extends Actor {
		int count = 1;

		public ValueLimited(Integer count) {
			super();
			setLoop(null);
			this.count = count;
		}
		
		public ValueLimited(Integer count, Object obj) {
			this(count);
			setValue(obj);
		}

		public ValueLimited setValue(Object obj) {
			for (int i = 0; i < count; i++)
				push(obj);
			return this;
		}

	}

	public static class Function extends Actor.StaticPiece {
		@Override
		public void OnPushed(Connector p, Object obj)
				throws InterruptedException {
			outputAllSimple(PackType.FAMILY, obj);
		}
	}

	public static class Effecter extends Actor.Loop {
		private Controllable t;
		PackType parent_type = PackType.FAMILY;

		public Effecter setParentType(PackType type) {
			parent_type = type;
			return this;
		}

		@Override
		public boolean actorReset() throws ChainException {
			try {
				setTarget((Controllable) (getParent(parent_type)));
			} catch (ClassCastException e) {
				throw new ChainException(this, "EffectSkelton: Failed to get Parent",
						LoopableError.LOCK);
			}
			getTarget().waitWake();
			return true;
		}

		@Override
		public boolean actorRun() throws ChainException {
			return false;
		}

		public void setTarget(Controllable t) {
			this.t = t;
		}

		public Controllable getTarget() {
			return t;
		}
	}

	public static class Alphar extends Txn<Integer> {
		public Alphar alpha_init(int direction, int duration) {
			initEffect(direction, duration);
			return this;
		}

		@Override
		public void txn(ViewActor _t) throws ChainException {
			_t.setAlpha(_t.getAlpha() + getEffectValue());
		}
	}

	public abstract static class Loop extends Actor implements IActorReset {
			Loop() {
				super();
				super.setLoop(this);
			}
	
			@Override
			public boolean actorReset() throws ChainException {
	//			getInPack(PackType.HEAP).reset();
				return true;
			}
		}

	public static abstract class StaticPiece extends Actor
			implements IPathListener {
		public StaticPiece() {
			super();
			kickFamily(false);
			setOutPackType(PackType.FAMILY, Output.NORMAL);
			getInPack(PackType.HEAP).setUserPathListener(this);
		}
	}

	public static class Variable extends Actor {
		Variable() {
			super();
		}
	
		@Override
		public boolean actorRun() throws ChainException, InterruptedException {
			push(getParent(PackType.FAMILY));
			return false;
		}
	}
	
	public static class Counter extends Loop {
		int counter = 0, threshold = 3;
		Counter() {
			super();
			setInPackType(PackType.EVENT, Input.FIRST);
		}
		public Counter setThreshold(int th) {
			this.threshold = th;
			return this;
		}
		@Override
		public boolean actorReset() throws ChainException {
			counter = 0;
			return true;
		}
		
		@Override
		public boolean actorRun() throws ChainException, InterruptedException {
			return ++counter < threshold;
		}
	}

	public static class EffecterSkelton<T> extends Effecter {
			private ViewActor target_view = null, target_cache = null;
			private int _i = 0, _duration = 0;
			T effect_val = null, cache = null;
			@Override
			public boolean actorReset() throws ChainException {
				super.actorReset();
	//			setTargetView((BasicView)getTarget());
				resetTargetView();
				setCounter(0);
				resetEffectValue();
				return true;
			}
			public EffecterSkelton<T> setCounter(int _i) {
				this._i = _i;
				return this;
			}
			public int getCounter() {
				return _i;
			}
			public boolean increment() {
				return ++_i < _duration;
			}
			public EffecterSkelton<T> setTargetView(ViewActor target_view) {
				this.target_view = target_view;
				return this;
			}
			public ViewActor getTargetView() {
				return target_cache;
			}
			public EffecterSkelton<T> resetTargetView() throws ChainException {
				target_cache = (target_view!= null)? target_view:(ViewActor)getTarget();
				return this;
			}
			public EffecterSkelton<T> initEffect(T val, int duration) {
				effect_val = val;
				_duration = duration;
				if(effect_val != null)
					cache = effect_val;
				return this;
			}
			@SuppressWarnings("unchecked")
			public EffecterSkelton<T> resetEffectValue() throws ChainException {
				cache = (effect_val!= null)? effect_val:(T) pull();
				return this;
			}
			public T getEffectValue() throws ChainException {
				return cache;
			}
		}

	public static class Rotater extends Txn<Integer> {
		public Rotater alpha_init(int direction, int duration) {
			initEffect(direction, duration);
			return this;
		}
	
		@Override
		public void txn(ViewActor _t) throws ChainException {
					_t.setAngle(_t.getAngle() + getEffectValue());
		}
	}

	public static class Colorer extends Txn<Integer> {
		public Colorer color_init(int _color) {
			initEffect(_color, 0);
			return this;
		}
		@Override
		public void txn(ViewActor _t) throws ChainException {
				_t.setColor(getEffectValue());
		}
	}

	public abstract static class LoopBoost extends Loop {
		LoopBoost() {
			super();
			super.boost();
		}
	}

	public abstract static class RelationFilter extends LoopBoost {
			private static final long serialVersionUID = 1L;
	
			RelationFilter() {
				super();
			}
	
			@Override
			public boolean actorRun() throws InterruptedException, ChainException {
				Object event = null;//pull();
				boolean rtn = false;
	//			for (Object obj : getReactor()) {
				Object obj = null;//pull();
					rtn |= relation_impl((ViewActor) obj, (ViewActor) event);
//					push(new Pair<Object, Object>(event, obj));
	//			}
				return !rtn;
			}
	
			public abstract boolean relation_impl(ViewActor a, ViewActor b) throws InterruptedException;
		}

	public static class Filter extends LoopBoost {
		@Override
		public boolean actorRun() throws InterruptedException, ChainException {
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

	public static abstract class Recorder extends Controllable implements
			IRecorder {
	
		Recorder() {
			super();
			setControlled(false);
		}
	
		public Recorder ctrlStop() {
			record_stop();
			return this;
		}
	
		public Recorder ctrlStart() throws ChainException {
			record_start();
			return this;
		}
	
		public Recorder ctrlReset() {
			return this;
		}
	}

	public static class BasicMerge extends LoopBoost {
		BasicMerge() {
			super();
			setInPackType(PackType.EVENT, Input.FIRST);
		}
	
	}

	public static class BasicSplit extends LoopBoost {
		ConnectionO o = null;
		Class<?> cls = null;
	
		BasicSplit(Class<?> _cls) {
			super();
			try {
				o = super.appended(_cls, Output.SYNC, PackType.HEAP, this);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			cls = _cls;
		}
	
		@Override
		public ConnectionO appended(Class<?> _cls, Output type,
				PackType stack_target, IPiece from) throws ChainException {
			if (stack_target == PackType.EVENT)
				return o;
			return null;
		}
	}

	public static class FlingFilter extends Actor.WorldPointFilter {
		FlingFilter() {
			super(ActorChain.fling);
		}
	}

	public static class BasicState extends Filter {
		BasicState() {
			super();
			setOutPackType(PackType.HEAP, Output.HIPPO);
			setLoop(null);
		}
	
	}

	public static class BasicToggle extends LoopBoost {
		BasicToggle() {
			super();
			setOutPackType(PackType.EVENT, Output.HIPPO);
			setLoop(null);
		}
	
		@Override
		public boolean actorRun() throws InterruptedException, ChainException {
			kick();
			pull();
			clearKick();
			return true;
			// if filter() returns true, output and exit single function
			// loop
		}
	}

	public static abstract class BasicQuaker extends Controllable {
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

	public static abstract class BasicLight extends Controllable implements
				TapChainLight {
			BasicLight() {
				super();
			}
			@Override
			public boolean actorRun() {
				TurnOn();
				return false;
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

	public static class Stun extends Loop {
		boolean started = false;
		@Override
		public boolean actorRun() throws ChainException {
			started = !started;
			return started;
		}
	}

	public static class MoveFilter extends Actor.WorldPointFilter {
		MoveFilter() {
			super(ActorChain.move);
		}
	}

	public static class LongPressFilter extends Filter {
		LongPressFilter() {
			super();
			connectToPush(ActorChain.longpress);
		}
	
	}

	public static class ShakeFilter extends Filter {
		ShakeFilter() {
			super();
			connectToPush(ActorChain.shake);
		}
	}

	public static class LockSingle extends Filter {
		static Actor open = new Actor();
	
		LockSingle() {
			super();
			connectToPush(open);
		}
	
		@Override
		public ConnectionO appended(Class<?> cls, Output type,
				PackType stack_target, IPiece from) throws ChainException {
			ConnectionO rtn = super.appended(cls, type,
					stack_target, from);
			open.push("");
			return rtn;
		}
	}

	/**
	 * @author hiro
	 * 
	 */
	public static class ManagerPiece extends StaticPiece {
		Manager maker;
		IPiece parent;
		public ManagerPiece() {
			super();
		}
		
		public ManagerPiece(IPiece pb) {
			this();
			parent = pb;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void OnPushed(Connector p, Object obj)
				throws InterruptedException {
			BlueprintManager bm = maker.makeBlueprint();
			if(parent != null)
				bm.setOuterInstanceForInner(parent);
			try {
				outputAllSimple(PackType.FAMILY,
					bm
					.add((Class<? extends Actor>) obj)
					.getBlueprint()
					.newInstance(maker));
			} catch (ChainException e) {
				maker.log(e.err);
			}
			clearInputHeap();
		}
		@Override
		public void postRegister(ActorManager maker) {
			super.postRegister(maker);
			this.maker = maker;
		}
	}

	static class WorldPointFilter extends Filter {
		WorldPointFilter(Actor p) {
			super();
			connectToPush(p);
		}
	
		WorldPoint getWorldPoint() throws ChainException {
			return (WorldPoint) pull();
		}
	}

	public static class TouchFilter extends WorldPointFilter {
		TouchFilter() {
			super(ActorChain.touchOn);
		}
	}

	public static class TouchUpFilter extends Filter {
		TouchUpFilter() {
			super();
			connectToPush(ActorChain.touchOff);
		}
	}

	public static class WaitEndHeap extends Filter {
		WaitEndHeap() {
			super();
		}
	
		@Override
		public boolean filter(Object obj) {
			return true;
		}
	
	}

	public static class TouchOnOffState extends BasicState {
			boolean state_bool = false;
	
			TouchOnOffState() {
				super();
				// super.connectToPush(touchSw);
				setOutPackType(PackType.EVENT, Output.HIPPO);
	//			setInPackType(PackType.HEAP, InputType.COUNT);
			}
	
			@Override
			public boolean filter(Object obj) {
				state_bool = !state_bool;
				if (!state_bool) {
					push(null);
					clearKick();
				}
	//			Log.w("TouchOnOff", state_bool ? "on" : "off" + "ed");
				return state_bool;
			}
		}

	/**
	 * 
	 */
}