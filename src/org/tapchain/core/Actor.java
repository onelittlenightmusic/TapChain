package org.tapchain.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.tapchain.core.ActorChain.IActorInit;
import org.tapchain.core.ActorChain.IAnimation;
import org.tapchain.core.ActorChain.IRecorder;
import org.tapchain.core.ActorChain.ISound;
import org.tapchain.core.ActorChain.IControllable;
import org.tapchain.core.ActorChain.ILight;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Chain.LoopableError;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.IPoint.WPEffect;
import org.tapchain.core.PathPack.ChainInPathPack;
import org.tapchain.core.PathPack.ChainOutPathPack.Output;

@SuppressWarnings("serial")
public class Actor extends ChainPiece.FlexPiece implements Comparable<Actor>,
		IPieceHead {
	private IActorInit _init = null;
	private Boolean animation_loop = false, live = false;
	Actor target = null;
	ConcurrentLinkedQueue<Actor> members = new ConcurrentLinkedQueue<Actor>();
	LinkedList<Integer> generation = new LinkedList<Integer>();
	IActor act = null;
	int time = 0;

	// 1.Initialization
	public Actor() {
		super();
		super.setFunc(this);
		setInPackType(PackType.HEAP, ChainInPathPack.Input.FIRST);
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
			_preInit();
			time = 0;
			if (_init != null) {
				boolean reset_cont = __exec(_init.actorInit(),
						"BasicPiece#impl@reset");
				if (!reset_cont) {
					_postEnd();
					return false;
				}
			}
			if (!preRun()) {
				return __exec(animation_loop, "BasicPiece#impl@end");
			}
		}
		__exec(live = act.actorRun(this), "BasicPiece#impl@run");
		__exec(time++, "BasicPiece#impl@time");
		if (!live) {
			postRun();
			_postEnd();
			/* when this animation end */
		}
		return __exec(animation_loop || live, "BasicPiece#impl@end");
	}

	boolean outthis = true;

	private void _preInit() throws InterruptedException, ChainException {
		if (outthis)
			__exec(outputAllSimple(PackType.FAMILY, this),
					"BasicPiece#init@sendthis");
		__exec(input(PackType.EVENT), "BasicPiece#init@sendevent");
	}

	protected boolean preRun() throws ChainException, InterruptedException {
		return true;
	}

	protected boolean postRun() throws ChainException {
		return true;
	}

	private void _postEnd() throws InterruptedException {
		__exec(outputAllSimple(PackType.EVENT, this), "BasicPiece#end@");
	}

	public Actor kickFamily(boolean out) {
		outthis = out;
		return this;
	}

	public Actor setActor(IActor _act) {
		act = _act;
		return this;
	}

	protected Actor setLoop(IActorInit reset) {
		_init = reset;
		animation_loop = true;
		return this;
	}

	public Actor getParent(PackType type) throws ChainException {
		// return (Actor) pull(getInPack(type));// parent;
		Actor.Controllable rtn = (Actor.Controllable) pull(getInPack(type));
		if (rtn == null)
			throw new ChainException(this, "getParent(): null",
					LoopableError.LOCK);
		rtn.waitWake();
		return rtn;
	}

	public Actor.ViewActor getParentView() throws ChainException {
		return (Actor.ViewActor) getParent(PackType.FAMILY);
	}

	public ConnectionResultIO setCalledFunction(Actor called)
			throws ChainException {
		// Called Function is supposed to be registered to AnimationChain by
		// user;
		called.connectToPush(this);
		return connectToPush(called);

	}

	// 3.Changing state

	@Override
	public int compareTo(Actor obj) {
		return -mynum + obj.mynum;
	}

	public Actor addMember(Actor bp) throws ChainException {
		members.add(bp);
		return this;
	}

	public Actor removeMember(Actor bp) {
		members.remove(bp);
		return this;
	}

	public Collection<Actor> getMembers() {
		return members;
	}

	@Override
	public void end() {
		for (Actor bp : members)
			if (bp != this)
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

	public Actor disableLoop() {
		animation_loop = false;
		return this;
	}

	@Override
	public Actor boost() {
		super.boost();
		return this;
	}

	@Override
	public ConnectionResultIO appendTo(PackType stack, IPiece cp,
			PackType stack_target) throws ChainException {
		ConnectionResultIO i = super.appendTo(stack, cp, stack_target);
		// setReactor(cp.getReactor());
		try {
			target = (Actor) (i.getPiece());
			__exec(String.format("Action: append, %s -> %s", i.getPiece()
					.getName(), getName()), "BasicPiece#APPEND");
		} catch (ClassCastException e1) {
			throw new ChainException(this,
					"BasicEffect: target is not a BasicPiece");
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

	public ConnectionResultIO connectToPush(Actor _push) {
		ConnectionResultIO o = null;
		if (_push != null) {
			try {
				o = appendTo(PackType.HEAP, _push, PackType.HEAP);
			} catch (ChainException e) {
				__exec(getName() + "/Connection to PushEvent : NG",
						"BasicPiece#ConnectToPush");
				e.printStackTrace();
			}
		}
		return o;
	}

	public ConnectionResultIO connectToKick(Actor _kick) {
		ConnectionResultIO o = null;
		if (_kick != null) {
			try {
				o = appendTo(PackType.EVENT, _kick, PackType.EVENT);
			} catch (ChainException e) {
				__exec(getName() + "/Connection to PushEvent : NG",
						"BasicPiece#ConnectToKick");
				e.printStackTrace();
			}
		}
		return o;
	}

	protected Actor push(Object obj) {
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
			__exec(getOutPack(PackType.EVENT).outputAllSimple(""),
					"BasicKicker");
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

	Object pullOne(PathPack.ChainInPathPack in, int i) throws ChainException {
		try {
			Object rtn = in.inputOne(i);
			if (rtn == null)
				throw new ChainException(this, "Please Connect!",
						LoopableError.LOCK);
			return rtn;
		} catch (InterruptedException e) {
			throw new ChainException(this, "Interrupted in pull()");
		}
	}

	Object pull(PathPack.ChainInPathPack in) throws ChainException {
		try {
			ArrayList<Object> rtn = in.input();
			if (rtn.isEmpty())
				throw new ChainException(this, "Please Connect!",
						LoopableError.LOCK);
			return rtn.get(0);
		} catch (InterruptedException e) {
			throw new ChainException(this, "Stopped.");
		}
	}

	public void innerRequest(PackType type, Object obj) {
		try {
			getInPack(type)._queueInnerRequest(obj);
		} catch (InterruptedException e) {
			e.printStackTrace();
			// throw new ChainException(this, "Inner Error.");
		}
	}

	protected Object pull() throws ChainException {
		return pull(getInPack(PackType.HEAP));
	}

	Object pull(int i) throws ChainException {
		return pull();
	}

	public void onAdd(ActorManager maker) {
	}

	public void onRemove(ActorManager newSession) {
	}

	public Object call(Object obj) throws ChainException {
		push(obj);
		// return pull();
		return obj;
		// throw new ChainException("call Fail");
	}

	public Actor invalidate() {
		((ActorChain) _root_chain).kick(this);
		// __log(String.format("%s kicks", getName()), "TapChain");
		return this;
	}

	public static class SimpleActor extends Actor implements IActor {
		public SimpleActor() {
			super();
			setActor(this);
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException,
				InterruptedException {
			return false;
		}
	}

	public static abstract class Sound extends Actor.Controllable implements
			ISound {
		int length = 0;

		// 1.Initialization
		public Sound() {
			super();
			setControlled(false);
			setLoop(null);
		}

		public Sound setLength(int len) {
			length = len;
			return this;
		}

		@Override
		public void ctrlStop() {
			stop_impl();
		}

		@Override
		public void ctrlStart() throws ChainException {
			reset_sound_impl();
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
		}

	}

	public static class Controllable extends Loop implements IControllable {
		CountDownLatch wake = new CountDownLatch(1);
		BlockingQueue<ControllableSignal> continueSignalQueue = new LinkedBlockingQueue<ControllableSignal>(
				Integer.MAX_VALUE);
		boolean autostart = false;
		boolean autoend = false;
		// boolean auto = false;
		boolean error = false;

		// 1.Initialization
		public Controllable() {
			super();
		}

		@Override
		public boolean preRun() throws ChainException, InterruptedException {
			boolean rtn = super.preRun();
			_ctrlStart();
			if (!autostart)
				rtn &= _waitInterrupt();
			return rtn;
		}

		@Override
		public boolean postRun() throws ChainException {
			boolean rtn = true;
			rtn &= super.postRun();
			error = false;
			// if waitFinish returns false or error is true, kill this process
			// when next actorReset is called.
			rtn = autoend || (_waitInterrupt() & !error);
			// Log.w("TEST", String.format("waitFinish returns %s", rtn));
			if (!rtn)
				_ctrlStop();
			return rtn;
		}

		@Override
		public boolean actorInit() throws ChainException {
			return true;
		}

		// Every Termination calls _ctrlStop()
		@Override
		public void onTerminate() throws ChainException {
			_ctrlStop();
		}

		private Controllable _wake(boolean stat) {
			if (stat)
				wake.countDown();
			else
				wake = new CountDownLatch(1);
			return this;
		}

		public Controllable waitWake() throws ChainException {
			try {
				wake.await();
			} catch (InterruptedException e) {
				throw new ChainException(this, "init failed",
						LoopableError.INTERRUPT);
			}
			return this;
		}

		public void interrupt(final ControllableSignal intr) {
			try {
				continueSignalQueue.put(intr);
			} catch (InterruptedException e) {
				error = true;
			}
		}

		public void interrupt(IControllableInterruption intr) {
			interrupt(ControllableSignal.USER.setInterrupt(intr));
		}

		private boolean _waitInterrupt() throws ChainException {
			boolean noreset = true;
			try {
				while (noreset) {
					ControllableSignal finishCode2 = continueSignalQueue.take();
					if (!finishCode2.getContinueCode(this))
						disableLoop();
					// if reset code is true, noreset is set false and while
					// loop breaks
					noreset = !finishCode2.getResetCode();
				}
				return noreset;
			} catch (InterruptedException e) {
				throw new ChainException(this, "interrupt",
						LoopableError.INTERRUPT);
			}
		}

		// public Controllable permitAutoRestart() {
		// auto = true;
		// return this;
		// }
		//
		private Controllable _ctrlStart() throws ChainException,
				InterruptedException {
			ctrlStart();
			_wake(true);
			return this;
		}

		private Controllable _ctrlStop() {
			_wake(false);
			ctrlStop();
			return this;
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
		}

		@Override
		public void ctrlStop() {
		}

		@Override
		public void onRemove(ActorManager maker) {
			interrupt(ControllableSignal.END);
		}

		public void setAutoEnd() {
			autoend = true;
		}

		public void setAutoStart() {
			autostart = true;
		}
	}

	public enum ControllableSignal {
		END(false, true), RESTART(true, true), USER(null, false) {
			private IControllableInterruption _intr;

			@Override
			public ControllableSignal setInterrupt(
					IControllableInterruption intr) {
				_intr = intr;
				return this;
			}

			@Override
			public boolean getContinueCode(Controllable actor) {
				return _intr.onDo(actor);
			}
		},
		TICK(true, false);
		private Boolean _cont = null, _reset = null;

		private ControllableSignal(Boolean cont, Boolean reset) {
			_cont = cont;
			_reset = reset;
		}

		public ControllableSignal setInterrupt(IControllableInterruption intr) {
			return this;
		}

		public boolean getContinueCode(Controllable actor) {
			return _cont;
		}

		public boolean getResetCode() {
			return _reset;
		}
	}

	public interface IControllableInterruption {
		public boolean onDo(Controllable c);
	}

	public static class ViewActor extends Controllable implements
			IAnimation/* , IPoint */, IValue<IPoint> {
		IPoint _wp = new WorldPoint(), back_wp = new WorldPoint();
		WorldPoint _size = new WorldPoint(30, 30);
		WorldPoint _percent = new WorldPoint(100, 100);
		int _Alpha = 255;
		private float _Angle = 0.0f;
		int color = 0;

		// 1.Initialization
		public ViewActor() {
			super();
			setAutoStart();
		}

		// 2.Getters and setters
		public void ctrlStop() {
			removeViewFromAnimation();
		}

		@Override
		public void ctrlStart() throws ChainException {
			view_init();
			addViewToAnimation();
			push(this);
		}

		public boolean view_user(Object canvas, IPoint iPoint, WorldPoint size,
				int alpha, float angle) {
			return true;
		}

		public void view_init() throws ChainException {
		}

		public void view_move(IPoint iPoint) {
		}

		public ViewActor setSize(WorldPoint size) {
			_size = size;
			return this;
		}

		public ViewActor setPercent(WorldPoint effectp) {
			_percent = effectp;
			return this;
		}

		@Override
		public ViewActor setCenter(IPoint pos) {
			back_wp = _wp;
			switch (pos.getEffect()) {
			case POS:
				user_set_center(pos);
				break;
			case DIF:
				user_set_center(getCenter().plus(pos));
			}
			view_move(_wp.sub(back_wp).setDif());
			return this;
		}

		public ViewActor user_set_center(IPoint pos) {
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

		// public final ViewActor addCenter(WorldPoint pos) {
		// setCenter(getCenter().plus(pos));
		// view_move(pos);
		// return this;
		// }
		//
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

		public IPoint getCenter() {
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

		// 3.Changing state
		protected void addViewToAnimation() {
			((ActorChain) _root_chain).vlist.add(this);
		}

		protected void removeViewFromAnimation() {
			((ActorChain) _root_chain).vlist.remove(this);
		}

		@Override
		public final boolean view_impl(Object canvas) {
			return view_user(canvas, getCenter(), getSize(), getAlpha(),
					getAngle());
		}

		@Override
		public void set(IPoint value) {
			setCenter(value);
		}

		@Override
		public IPoint get() {
			return getCenter();
		};

		/*
		 * @Override public int x() { return getCenter().x(); }
		 * 
		 * @Override public int y() { return getCenter().y(); }
		 * 
		 * @Override public IPoint sub(IPoint pt) { return getCenter().sub(pt);
		 * }
		 * 
		 * @Override public WPEffect getEffect() { return
		 * getCenter().getEffect(); }
		 * 
		 * @Override public IPoint setDif() { return getCenter().setDif(); }
		 * 
		 * @Override public IPoint plus(IPoint pos) { return
		 * getCenter().plus(pos); }
		 * 
		 * @Override public IPoint multiply(float bezier_coeff) { return
		 * getCenter().multiply(bezier_coeff); }
		 * 
		 * @Override public IPoint round(int i) { return getCenter().round(i); }
		 * 
		 * @Override public IPoint add(int i, int j) { return getCenter().add(i,
		 * j); }
		 * 
		 * @Override public IPoint set(int x, int y) { return getCenter().set(x,
		 * y); }
		 */
	}

	public static class Effector extends Controllable {
		PackType parent_type = PackType.FAMILY;

		public Effector() {
			super();
			setAutoStart();
			setAutoEnd();
		}

		public Effector setParentType(PackType type) {
			parent_type = type;
			return this;
		}

		public Controllable getTarget() throws ChainException {
			try {
				return (Controllable) getParent(parent_type);
			} catch (ClassCastException e) {
				throw new ChainException(this,
						"EffectSkelton: Failed to get Parent",
						LoopableError.LOCK);
			}
			// return t;
		}
	}

	public static class EffectorSkelton<T, E> extends Effector implements
			IValue<E> {
		private T target_view = null, target_cache = null;
		private int _i = 0, _duration = 0;
		E effect_val = null, cache = null;

		public EffectorSkelton() {
			super();
		}

		@Override
		public boolean actorInit() throws ChainException {
			super.actorInit();
			resetTargetView();
			setCounter(0);
			resetEffectValue();
			return true;
		}

		public EffectorSkelton<T, E> setCounter(int _i) {
			this._i = _i;
			return this;
		}

		public int getCounter() {
			return _i;
		}

		public boolean increment() {
			return ++_i < _duration;
		}

		public EffectorSkelton<T, E> setTargetView(T target_view) {
			this.target_view = target_view;
			return this;
		}

		public T getTargetView() {
			return target_cache;
		}

		@SuppressWarnings("unchecked")
		public EffectorSkelton<T, E> resetTargetView() throws ChainException {
			target_cache = (target_view != null) ? target_view
					: (T) getTarget();
			return this;
		}

		public EffectorSkelton<T, E> initEffect(E val, int duration) {
			effect_val = val;
			_duration = duration;
			if (effect_val != null)
				cache = effect_val;
			return this;
		}

		@SuppressWarnings("unchecked")
		public EffectorSkelton<T, E> resetEffectValue() throws ChainException {
			cache = (effect_val != null) ? effect_val : (E) pull();
			return this;
		}

		public E getEffectValue() throws ChainException {
			return cache;
		}

		@Override
		public void set(E value) {
			effect_val = value;
		}

		@Override
		public E get() {
			return effect_val;
		}
	}

	public static abstract class Transaction<V, E> extends EffectorSkelton<V, E>
			implements ITxn<V> {
		@Override
		public boolean actorRun(Actor act) throws ChainException {
			V _t = getTargetView();
			synchronized (_t) {
				txn(_t);
			}
			invalidate();
			return __exec(increment(), "TE#end");
		}

		@Override
		public abstract void txn(V _t) throws ChainException;
	}

	public static abstract class Txn<E> extends Transaction<ViewActor, E> {
	}
	
	public static abstract class EffectorEffector<E> extends Transaction<IValue<E>, E> {
	}

	public static class Mover extends Txn<IPoint> {
		public Mover() {
			super();
		}

		// Blueprint's getDeclaredConnstructor can not find super class'
		// constructor other than default constructor.
		// The following line can not be in super class.
		public Mover(WorldPoint p) {
			this();
			initEffect(p, 0);
			// setLogLevel(true);
		}

		@Override
		public void txn(ViewActor _t) throws ChainException {
			IPoint _dir = getEffectValue();// (_direction != null)?
			__exec(_t.setCenter(_dir), "MVE#run");

		}
	}
	
	public static class Accelerator extends EffectorEffector<IPoint> {
		public Accelerator() {
			super();
		}

		// Blueprint's getDeclaredConnstructor can not find super class'
		// constructor other than default constructor.
		// The following line can not be in super class.
		public Accelerator(WorldPoint p) {
			this();
			initEffect(p, 0);
			// setLogLevel(true);
		}

		@Override
		public void txn(IValue<IPoint> _t) throws ChainException {
			_t.get().add(getEffectValue());
		}
		
	}

	public static class Sizer extends Txn<WorldPoint> {
		public Sizer() {
			super();
		}

		public Sizer(WorldPoint p, Integer duration) {
			this();
			initEffect(p, duration);
		}

		@Override
		public void txn(ViewActor _t) throws ChainException {
			_t.setPercent(_t.getPercent().plus(getEffectValue()));
		}
	}

	public static class Sleeper extends Effector {
		int sleepinterval = 2000;

		public Sleeper() {
			super();
		}

		public Sleeper(int interval) {
			this();
			setSleepTime(interval);
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException {
			// LoopControl t = (LoopControl) (getReactor().get(0));
			try {
				Thread.sleep(sleepinterval);
			} catch (InterruptedException e) {
				throw new ChainException(this, "SleepEffect: Interrupted",
						LoopableError.INTERRUPT);
			}
			return false;
		}

		public Sleeper setSleepTime(int _interval) {
			sleepinterval = _interval;
			return this;
		}
	}

	public static class Resetter extends Effector {
		boolean cont = false;

		public Resetter() {
			super();
		}

		public Resetter(boolean _cont) {
			super();
			setContinue(_cont);
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException {
			getTarget().interrupt(
					cont ? ControllableSignal.RESTART : ControllableSignal.END);
			return false;
		}

		public Resetter setContinue(boolean cont) {
			this.cont = cont;
			return this;
		}
	}

	public static class Ender extends Resetter {
		boolean cont = false;

		public Ender() {
			super(false);
		}
	}
	
	public static class Value extends Actor.BasicState {
		// 1.Initialization
		public Value(Object obj) {
			super();
			setValue(obj);
		}

		public Value setValue(Object obj) {
			push(obj);
			return this;
		}
	}

	public static class ValueLimited extends Actor {
		int count = 1;

		// 1.Initialization
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

	public abstract static class Loop extends SimpleActor implements IActorInit {
		public Loop() {
			super();
			super.setLoop(this);
		}

		@Override
		public boolean actorInit() throws ChainException, InterruptedException {
			// getInPack(PackType.HEAP).reset();
			return true;
		}
	}

	public static abstract class StaticPiece extends SimpleActor implements
			IPathListener {
		public StaticPiece() {
			super();
			kickFamily(false);
			setOutPackType(PackType.FAMILY, Output.NORMAL);
			getInPack(PackType.HEAP).setUserPathListener(this);
		}
	}

	public static class Variable extends SimpleActor {
		Variable() {
			super();
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException,
				InterruptedException {
			push(getParent(PackType.FAMILY));
			return false;
		}
	}

	public static class Counter extends Loop {
		int counter = 0, threshold = 3;

		// 1.Initiailization
		public Counter() {
			super();
			setInPackType(PackType.EVENT, ChainInPathPack.Input.FIRST);
		}

		public Counter setThreshold(int th) {
			this.threshold = th;
			return this;
		}

		@Override
		public boolean actorInit() throws ChainException {
			counter = 0;
			return true;
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException,
				InterruptedException {
			return ++counter < threshold;
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

		public RelationFilter() {
			super();
		}

		@Override
		public boolean actorRun(Actor act) throws InterruptedException,
				ChainException {
			Object event = null;// pull();
			boolean rtn = false;
			// for (Object obj : getReactor()) {
			Object obj = null;// pull();
			rtn |= relation_impl((ViewActor) obj, (ViewActor) event);
			// push(new Pair<Object, Object>(event, obj));
			// }
			return !rtn;
		}

		public abstract boolean relation_impl(ViewActor a, ViewActor b)
				throws InterruptedException;
	}

	@SuppressWarnings("unchecked")
	public static class Filter<T> extends LoopBoost implements IFilter<T> {
		@Override
		public boolean actorRun(Actor act) throws InterruptedException,
				ChainException {
			Object event = pull();
			boolean rtn = filter((T) event);
			if (rtn) {
				push(event);
			}
			return !rtn;
			// if filter() ret urns true, output and exit single function
		}

		@Override
		public boolean filter(T obj) throws ChainException {
			return true;
		}

	}

	public static abstract class Recorder extends Controllable implements
			IRecorder {

		// 1.Initialization
		public Recorder() {
			super();
			setControlled(false);
		}

		@Override
		public void ctrlStop() {
			record_stop();
		}

		@Override
		public void ctrlStart() throws ChainException {
			record_start();
		}

	}

	public static class BasicMerge extends LoopBoost {
		// 1.Initialization
		public BasicMerge() {
			super();
			setInPackType(PackType.EVENT, ChainInPathPack.Input.FIRST);
		}

	}

	public static class BasicSplit extends LoopBoost {
		ConnectionResultO o = null;
		Class<?> cls = null;

		BasicSplit(Class<?> _cls) {
			super();
			try {
				o = super.appended(Output.SYNC, PackType.HEAP, this);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			cls = _cls;
		}

		@Override
		public ConnectionResultO appended(Output type, PackType stack_target,
				IPiece from) throws ChainException {
			if (stack_target == PackType.EVENT)
				return o;
			return null;
		}
	}

	public static class BasicState extends Filter {
		// 1.Initialization
		public BasicState() {
			super();
			setOutPackType(PackType.HEAP, Output.HIPPO);
			setLoop(null);
		}

	}

	public static class BasicToggle extends LoopBoost {
		// 1.Intialization
		public BasicToggle() {
			super();
			setOutPackType(PackType.EVENT, Output.HIPPO);
			setLoop(null);
		}

		@Override
		public boolean actorRun(Actor act) throws InterruptedException,
				ChainException {
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

		// 1.Initialization
		public BasicQuaker(int interval) {
			super();
			// permitAutoRestart();
			val = interval;
		}

		public abstract boolean quake_impl();

		@Override
		public void ctrlStart() {
			quake_impl();
		}

		@Override
		public void ctrlStop() {
		}

		public int getVal() {
			return val;
		}
	}

	public static abstract class BasicLight extends Controllable implements
			ILight {
		// 1.Initialization
		public BasicLight() {
			super();
		}

		@Override
		public boolean actorRun(Actor act) {
			TurnOn();
			return false;
		}

		public abstract boolean turn_on();

		public abstract boolean turn_off();

		public abstract boolean change_color(int r, int g, int b, int a);

		public BasicLight TurnOn() {
			turn_on();
			// suspend();
			return this;
		}

		public BasicLight TurnOff() {
			turn_off();
			// suspend();
			return this;
		}

		public BasicLight ChangeColor(int r, int g, int b, int a) {
			change_color(r, g, b, a);
			return this;
		}

		@Override
		public void ctrlStart() {
			TurnOff();
			TurnOn();
		}

		@Override
		public void ctrlStop() {
			TurnOff();
		}

	}

	public static class Stun extends Loop {
		boolean started = false;

		@Override
		public boolean actorRun(Actor act) throws ChainException {
			started = !started;
			return started;
		}
	}

	static class WorldPointFilter extends Filter {
		// 1.Initialization
		public WorldPointFilter(Actor p) {
			super();
			connectToPush(p);
		}

		WorldPoint getWorldPoint() throws ChainException {
			return (WorldPoint) pull();
		}
	}

	public static class MoveFilter extends WorldPointFilter {
		// 1.Initialization
		public MoveFilter() {
			super(ActorChain.move);
		}
	}

	public static class FlingFilter extends Actor.WorldPointFilter {
		FlingFilter() {
			super(ActorChain.fling);
		}
	}

	public static class LongPressFilter extends Filter {
		// 1.Initialization
		public LongPressFilter() {
			super();
			connectToPush(ActorChain.longpress);
		}

	}

	public static class ShakeFilter extends Filter {
		// 1.Initialization
		public ShakeFilter() {
			super();
			connectToPush(ActorChain.shake);
		}
	}

	/**
	 * @author hiro
	 * 
	 */
	public static class ManagerPiece extends StaticPiece {
		BlueprintManager bm;
		PieceManager maker;
		IPiece parent;

		// 1.Initialization
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
			if (parent != null)
				bm.setOuterInstanceForInner(parent);
			try {
				outputAllSimple(PackType.FAMILY,
						bm.addLocal((Class<? extends Actor>) obj)
								.getBlueprint().newInstance(maker));
			} catch (ChainException e) {
				maker.log(e.err);
			}
			clearInputHeap();
		}

		@Override
		public void onAdd(ActorManager maker) {
			super.onAdd(maker);
			this.maker = maker;
		}
	}

	public static class TouchFilter extends WorldPointFilter {
		// 1.Initialization
		public TouchFilter() {
			super(ActorChain.touchOn);
		}
	}

	public static class TouchUpFilter extends Filter {
		// 1.Initialization
		public TouchUpFilter() {
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

		// 1.Initialization
		public TouchOnOffState() {
			super();
			// super.connectToPush(touchSw);
			setOutPackType(PackType.EVENT, Output.HIPPO);
			// setInPackType(PackType.HEAP, InputType.COUNT);
		}

		@Override
		public boolean filter(Object obj) {
			state_bool = !state_bool;
			if (!state_bool) {
				push(null);
				clearKick();
			}
			// Log.w("TouchOnOff", state_bool ? "on" : "off" + "ed");
			return state_bool;
		}
	}

	public interface IFilter<T> {
		public boolean filter(T obj) throws ChainException;
	}

	public interface ITxn<T> {
		public void txn(T _t) throws ChainException;
	}

	public interface IValue<T> {
		public void set(T value);
		public T get();
	}

	/**
	 * 
	 */
}