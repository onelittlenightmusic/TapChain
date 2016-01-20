package org.tapchain.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.tapchain.core.ActorChain.IActorInit;
import org.tapchain.core.ActorChain.IControllable;
import org.tapchain.core.ActorChain.ILight;
import org.tapchain.core.ActorChain.IRecorder;
import org.tapchain.core.ActorChain.ISound;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultOutConnector;
import org.tapchain.core.Chain.ConnectionResultPath;
import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Chain.PieceErrorCode;
import org.tapchain.core.ClassLib.ClassLibReturn;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.IActorTap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class Actor extends ChainPiece<Actor> implements Comparable<Actor>,
        IPieceHead, JSONSerializable {
    static Map<LinkType, HashMap<Class<?>, ClassEnvelope>> mapLinkClass
            = new HashMap<LinkType, HashMap<Class<?>, ClassEnvelope>>() {
        {
            for(LinkType linkType: LinkType.values())
                put(linkType, new HashMap<Class<?>, ClassEnvelope>());
        }
    };

    static Map<Class<?>, LinkedList<LinkType>> classLimits = new HashMap<Class<?>, LinkedList<LinkType>>();

    // Parts of logic implementations
    private IActorInit _actorInit = null;
    private IActor _actor = null;
    private Boolean animation_loop = false, live = false;
    IActorBlueprint blueprint = null;
    ConcurrentLinkedQueue<Actor> members = new ConcurrentLinkedQueue<Actor>();
    private int time = 0;

    // 1.Initialization
    public Actor() {
        super();
        super.setFunc(this);
        setInPackType(PathType.OFFER, InPathPack.Input.FIRST);
        staticInitializeLinks(this.getClass());
        __setAssociatedClasses(this.getClass());
    }


    public static void staticInitializeLinks(Class<? extends IPiece> registeredClass) {
        if (initedClass.contains(registeredClass))
            return;
        if (Controllable.class.isAssignableFrom(registeredClass)) {
            ClassLibReturn classReturn = getParameters(registeredClass, /* userParameters, */
                    Controllable.class);
            staticRegisterLinkClass(registeredClass, classReturn);
        }
        initedClass.add(registeredClass);
    }

    public static void staticOverrideLinks(Class<? extends IPiece> registeredClass,
                                           Class<?> sampleClass,
                                           Class<?> baseClass) {
        if (Controllable.class.isAssignableFrom(registeredClass)) {
            ClassLibReturn classReturn = getParameters(sampleClass, /* userParameters, */
                    baseClass);
            staticRegisterLinkClass(registeredClass, classReturn);
        }
        initedClass.add(registeredClass);
    }

    public static void staticRegisterLinkClass(Class<? extends IPiece> cls, ClassLibReturn classReturn) {
        ClassEnvelope parameter;
        for (Entry<LinkType, String> e : linkTypeName.entrySet()) {
            parameter = classReturn.searchByName(e.getValue());
            if (parameter == null) {
                continue;
            }
            LinkType link = e.getKey();
            Type rawType = Void.class;
            if (parameter != null)
                rawType = parameter.getRawType();
            if (rawType == Self.class)
                __addLinkClass(cls, link, cls);
            else if (rawType != Void.class && !staticHasClassLimit(cls, link))
                __addLinkClass(cls, link, parameter);
        }
    }

    @Override
    public Actor setLogLevel(boolean _log) {
        super.setLogLevel(_log);
        return this;
    }

    public static boolean staticHasClassLimit(
            Class<?> cls, LinkType al) {
        if (!classLimits.containsKey(cls))
            return false;
        return classLimits.get(cls).contains(al);
    }

    public static void staticAddClassLimit(Class<?> thisClass, LinkType limitedLink) {
        if (!classLimits.containsKey(thisClass))
            classLimits.put(thisClass, new LinkedList<LinkType>());
        classLimits.get(thisClass).add(limitedLink);
    }

    public static void staticAddClassLimits(Class<?> thisClass, LinkType... limitedLinks) {
        for (LinkType al : limitedLinks)
            staticAddClassLimit(thisClass, al);
    }

    public static ClassLibReturn getParameters(Class<?> parent, Class<?> target) {
        return ClassLib.getParameterizedType(parent, target);
    }

    public void __setAssociatedClasses(Class<?> cc) {
        for (LinkType al : LinkType.values())
            setLinkClass(al, __collectClass(cc, al));
    }

    private static ClassEnvelope __get(Class<?> cc,
                                       Map<Class<?>, ClassEnvelope> m) {
        return m.get(cc);
    }

    public static ClassEnvelope getLinkClassFromLib(Class<?> cc, LinkType ac) {
        return __get(cc, __getMap(ac));
    }

    public ClassEnvelope getLinkClassFromLib(LinkType linkType) {
        return getLinkClassFromLib(this.getClass(), linkType);
    }

    private static Map<Class<?>, ClassEnvelope> __getMap(LinkType ac) {
        return mapLinkClass.get(ac);
    }

    public static ClassEnvelope __collectClass(Class<?> cc, LinkType ac) {
        if (cc == null || cc == Actor.class)
            return null;
        return getLinkClassFromLib(cc, ac);
    }

    protected static void __add(Class<?> cc, Map<Class<?>, ClassEnvelope> m,
                                ClassEnvelope clz) {
        m.put(cc, clz);
    }

    protected static void __add(Class<?> cc, Map<Class<?>, ClassEnvelope> m,
                                Class<?>... clz) {
        m.put(cc, new ClassEnvelope(Arrays.asList(clz)));
    }

    protected static void __addLinkClass(Class<?> cc, LinkType linkType,
                                         Class<?> clz) {
        __add(cc, __getMap(linkType), clz);
        log(String.format("=====%s, added(%s, %s)", cc.getSimpleName(), linkType.toString(), clz.getSimpleName()));

    }

    protected static void __addLinkClass(Class<?> cc, LinkType linkType,
                                         ClassEnvelope clz) {
        __add(cc, __getMap(linkType), clz);
        log(String.format("=====%s, added(%s, %s)", cc.getSimpleName(), linkType.toString(), clz.getSimpleName()));
    }

    @Override
    public boolean pieceReset(IPiece f) {
        live = false;
        return true;
    }

    @Override
    public boolean pieceRun(IPiece f) throws ChainException,
            InterruptedException {

        //Pre run
        if (!live) {
            _preInit();
            time = 0;
            if (_actorInit != null) {
                L("Actor.actorInit()").go(_actorInit.actorInit());
            }
            if (!L("Actor.preRun()").go(preRun())) {
                return L("Actor.end() loop=").go(animation_loop);
            }
        }

        //Main: Invoking actorRun
        L("Actor.actorRun()").go(live = _actor.actorRun(this));

        //Increment time count
        L("Actor.time++").go(time++);

        //Post run
        if (!live) {
            L("Actor.postRun()").go(postRun());
            _postEnd();
        }
        return L("Actor.pieceRun().exit").go(animation_loop || live);
    }

    boolean outthis = true;

    private void _preInit() throws InterruptedException, ChainException {
        if (outthis)
            L("Family pushed").go(outputAllSimple(PathType.FAMILY, new Packet(this, this)));
        L("Event pulled").go(input(PathType.EVENT));
    }

    protected boolean preRun() throws ChainException, InterruptedException {
        return true;
    }

    protected boolean postRun() throws ChainException {
        return true;
    }

    private void _postEnd() throws InterruptedException {
        L("Actor.postEnd()").go(outputAllSimple(PathType.EVENT, new Packet(this, this)));
    }

    public Actor setKickFamily(boolean out) {
        outthis = out;
        return this;
    }

    public Actor setActor(IActor _act) {
        _actor = _act;
        return this;
    }

    public Actor setLoop(IActorInit reset) {
        _actorInit = reset;
        animation_loop = true;
        return this;
    }

    public Actor getParent(PathType type) throws ChainException {
        return getParent(type, true);
    }

    public Actor getParent(PathType type, boolean wait) throws ChainException {
        Actor.Controllable rtn = (Actor.Controllable) __pull(getInPack(type)).getObject();
        if (rtn == null)
            throw new ChainException(this, "getParent(): null", type.getErrorCode());
        if (wait)
            rtn.waitWake();
        return rtn;
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

    public void setBlueprint(IActorBlueprint b) {
        blueprint = b;
    }

    public IActorBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    public void onTerminate() throws ChainException {
        for (Actor bp : members)
            if (bp != this)
                bp.end();
        super.onTerminate();
    }

    public Actor once() {
        animation_loop = false;
        return this;
    }

    @Override
    public Actor boost() {
        super.boost();
        return this;
    }

    @Override
    public ConnectionResultPath appendTo(PathType stack, IPiece cp,
                                         PathType stack_target) throws ChainException {
        ConnectionResultPath i = super.appendTo(stack, cp, stack_target);
        Actor target;
        try {
            target = (Actor) (i.getPiece());
            if(target == null)
                throw new ChainException(this, "Actor: target is not an Actor");
        } catch (ClassCastException e1) {
            throw new ChainException(this, "Actor: target is not an Actor");
        }
        if (stack == PathType.FAMILY && stack_target == PathType.FAMILY)
            target.addMember(this);
        return i;
    }

    @Override
    public void detached(IPiece cp) {
        super.detached(cp);
        removeMember((Actor) cp);
    }

    protected Actor pushInActor(Object obj, String pushTag) {
        if (obj == null)
            return this;
        try {
            L("Actor.pushInActor(" + obj.toString()).go(getOutPack(PathType.OFFER).outputAllSimple(
                    new Packet(obj, this).setTag(pushTag)));
            if (_statusHandler != null)
                _statusHandler.pushView(this, obj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    protected void kick() {
        try {
            L("Actor.kick()").go(getOutPack(PathType.EVENT).outputAllSimple(new Packet("", this)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Packet __pull(InPathPack in) throws ChainException {
        ArrayList<Packet> rtn;
        try {
            rtn = in.input();
        } catch (InterruptedException e) {
            throw new ChainException(this, "Interrupted.",PieceErrorCode.INTERRUPT);
        }
        if (rtn.isEmpty()) {
            if (in.getPathMainClass() == null)
                throw new ActorPullException(this, new ClassEnvelope(Actor.class),
                        "No permission for PULL()"
                );
            throw new ActorPullException(this, in.getPathMainClass(),
                    "No connection for PULL()"
            );
        }
        return rtn.get(0);
    }

    protected Packet pullInActor() throws ChainException {
        return __pull(getInPack(PathType.OFFER));
    }

    public boolean isConnectedTo(LinkType linkType) {
        PathType pathType1 = linkType.getPathType();
        boolean outOrIn = linkType.getOutOrIn();

        if (outOrIn)
            return hasOutPath(pathType1);
        else
            return hasInPath(pathType1);
    }

    public Collection<Actor> getPartners(LinkType linkType) {
        return super.getPartners(linkType.getPathType(), linkType.getOutOrIn());
    }


    public Actor setLinkClass(LinkType ac, Class<?>... clz) {
        return setLinkClass(ac, new ClassEnvelope(Arrays.asList(clz)));
    }

    public Actor setLinkClass(LinkType ac, ClassEnvelope clz) {
        setPathClass(ac.getPathType(), ac.getOutOrIn(), clz);
        return this;
    }

    public void offer(Object obj) {
        try {
            getInPack(PathType.OFFER)._queueInnerRequest(obj);
            sendUnerrorEvent();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void addFromFactory(ActorManager maker) {
//    }

    public void onRemove(ActorManager newSession) {
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

    public abstract static class Loop extends SimpleActor implements IActorInit {
        public Loop() {
            super();
            super.setLoop(this);
        }

        @Override
        public boolean actorInit() throws ChainException, InterruptedException {
            return true;
        }
    }

    public static class Controllable<VALUE, INPUT, OUTPUT, PARENT> extends Loop
            implements IControllable {
        CountDownLatch wake = new CountDownLatch(1);
        BlockingQueue<ControllableSignal> continueSignalQueue = new LinkedBlockingQueue<ControllableSignal>(
                Integer.MAX_VALUE);
        boolean autostart = false;
        boolean autoend = false;
        // boolean auto = false;
        boolean error = false;

        final static Map<LinkType, String> linkType = new HashMap<LinkType, String>() {
            {
                put(LinkType.TO_CHILD, "VALUE");
                put(LinkType.PULL, "INPUT");
                put(LinkType.PUSH, "OUTPUT");
                put(LinkType.FROM_PARENT, "PARENT");
            }
        };
        String nowTag = Integer.toString(getId());
        String pullTag = "";
        String pushTag = nowTag;

        // 1.Initialization
        public Controllable() {
            super();
        }

        public Controllable(Object sample, Class<?> targetClass) {
            super();
            Class<? extends Controllable> thisClass = this.getClass();
            Class<?> sampleClass = sample.getClass();
            staticOverrideLinks(thisClass, sampleClass, targetClass);
        }

        public void __initValue(ClassEnvelope classEnvelope) {
            if (classEnvelope != null && this instanceof IValue) {
                if (((IValue) this)._valueGet() != null)
                    return;
                Class<?> cls = classEnvelope.getRawClass();
                if (IPoint.class.isAssignableFrom(cls)) {
                    return;
                } else if (cls == Integer.class) {
                    ((IValue) this)._valueSet(1);
                    return;
                }

                try {
                    ((IValue) this)._valueSet(cls.newInstance());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        public INPUT pull() throws ChainException {
            Packet input = L("pull()").go(super.pullInActor());
            pullTag = input.getTag();
            return (INPUT)input.getObject();
        }

        public void push(OUTPUT output) {
            super.pushInActor(output, pushTag==null?output.toString()+getTag():pushTag);
            pushTag = null;
        }

        public void push(OUTPUT output, String pushTag) {
            super.pushInActor(output, pushTag);
        }

        @Override
        protected boolean preRun() throws ChainException, InterruptedException {
            boolean rtn = super.preRun();
            L("_ctrlStart()").go(_ctrlStart());
            rtn &= L("_waitInterrupt()").go(_waitInterrupt(!autostart));
            return rtn;
        }

        @Override
        protected boolean postRun() throws ChainException {
            super.postRun();
            error = false;
            // if waitFinish returns false or error is true, kill this process
            boolean rtn = (_waitInterrupt(!autoend) & !error);
            if (!rtn) {
                _ctrlStop();
            }
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
            super.onTerminate();
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
                        PieceErrorCode.INTERRUPT);
            }
            return this;
        }

        public ControllableSignal interrupt(final ControllableSignal intr) {
            try {
                continueSignalQueue.put(intr);
            } catch (InterruptedException e) {
                error = true;
            }
            return intr;
        }

        public IControllableInterruption interrupt(
                IControllableInterruption intr) {
            interrupt(ControllableSignal.USER.setInterrupt(intr));
            return intr;
        }

        public ControllableSignal interruptError() {
            return interrupt(ControllableSignal.ERROR);
        }

        public ControllableSignal interruptEnd() {
            return interrupt(ControllableSignal.END);
        }

        public ControllableSignal interruptStep() {
            invalidate();
            return interrupt(ControllableSignal.STEP);
        }

        public ControllableSignal interruptRestart() {
            return interrupt(ControllableSignal.RESTART);
        }

        private boolean _waitInterrupt(boolean emptywait) throws ChainException {
            boolean noreset = true, error = false;
            try {
                if (emptywait) {
                    ControllableSignal signal = L("take()").go(continueSignalQueue.take());
                    if (!signal.getContinueCode(this))
                        once();
                    error |= signal.getErrorCode();
                    // if reset is true, noreset is set false
                    noreset = !signal.getResetCode();
                } else {
                    // {ERROR} Following lines generate some wait with no
                    // queue.
                    while (true) {
                        ControllableSignal finishCode2 = L("poll()").go(continueSignalQueue
                                .poll(0L, TimeUnit.SECONDS));
                        if (finishCode2 == null)
                            break;
                        if (!finishCode2.getContinueCode(this)) {
                            once();
                        }
                        error |= finishCode2.getErrorCode();
                        // if reset code is true, noreset is set false and while
                        // loop breaks
                        noreset &= !finishCode2.getResetCode();
                    }
                }
                if (error) {
                    throw new ChainException(this, "Controllable ERROR called",
                            PieceErrorCode.LOCK_OTHER);
                }
                return noreset;
            } catch (InterruptedException e) {
                throw new ChainException(this, "interrupt",
                        PieceErrorCode.INTERRUPT);
            }
        }

        private Controllable _ctrlStart() throws ChainException,
                InterruptedException {
            ctrlStart();
            _wake(true);
            return this;
        }

        private Controllable _ctrlStop() throws ChainException {
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

        public void unsetAutoEnd() {
            autoend = false;
        }

        public void setAutoStart() {
            autostart = true;
        }

        public void unsetAutoStart() {
            autostart = false;
        }

        @Override
        boolean recvUnlockEvent() throws InterruptedException {
            super.recvUnlockEvent();
            continueSignalQueue.clear();
            return true;
        }

        public String getNowTag() {
            return nowTag;
        }
        protected void setNowTag(String now) {
            nowTag = now;
        }
    }

    public enum ControllableSignal {
        END(false, true, false), RESTART(true, true, false), USER(null, false,
                false) {
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
        STEP(true, false, false), ERROR(true, false, true), DUMP(true, false,
                false) {
            @Override
            public boolean getContinueCode(Controllable actor) {
                new Exception().printStackTrace();
                return true;
            }
        },
        TICK(true, false, false) {
            @Override
            public boolean getContinueCode(Controllable actor) {
                if (actor instanceof IActorTap)
                    ((IActorTap) actor).onTick(null, null);
                return true;
            }
        };
        private Boolean _cont = null, _reset = null, _error = null;

        private ControllableSignal(Boolean cont, Boolean reset, Boolean error) {
            _cont = cont;
            _reset = reset;
            _error = error;
        }

        public ControllableSignal setInterrupt(IControllableInterruption intr) {
            return this;
        }

        public boolean getContinueCode(Controllable actor) throws ChainException {
            return _cont;
        }

        public final boolean getResetCode() {
            return _reset;
        }

        public final boolean getErrorCode() {
            return _error;
        }
    }

    public interface IControllableInterruption {
        public boolean onDo(Controllable c);
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static class Effector<VALUE, INPUT, OUTPUT, PARENT> extends
            Controllable<VALUE, INPUT, OUTPUT, PARENT> {
        PathType parent_type = PathType.FAMILY;

        public Effector() {
            super();
            setAutoStart();
            setAutoEnd();
        }

        public Effector(Object obj, Class<?> target) {
            super(obj, target);
            setAutoStart();
            setAutoEnd();
        }

        public Effector setParentType(PathType type) {
            parent_type = type;
            return this;
        }

        public PARENT getTarget() throws ChainException {
            return getTarget(true);
        }

        public PARENT getTarget(boolean wait) throws ChainException {
            try {
                return (PARENT) getParent(parent_type, wait);
            } catch (ClassCastException e) {
                throw new ChainException(this,
                        "EffectSkelton: Failed to get Parent",
                        parent_type.getErrorCode());
            }
        }
    }

    public static class EffectorSkelton<Parent, Effect> extends
            Effector<Self, Effect, Effect, Parent> implements IValue<Effect> {
        // private T target_view = null, target_cache = null;
        private int _i = 0, _duration = 0;
        Effect /* effect_val = null, */cache = null;
        Class<?> type = null;
        boolean pull = false;

        public EffectorSkelton() {
            super();
            __initValue(getLinkClassFromLib(this.getClass(), LinkType.PUSH));
        }

        @Override
        public boolean actorInit() throws ChainException {
            super.actorInit();
            setCounter(0);
            return true;
        }

        public void setPull(boolean p) {
            pull = p;
        }

        public boolean getPull() {
            return pull;
        }

        public EffectorSkelton<Parent, Effect> setCounter(int _i) {
            this._i = _i;
            return this;
        }

        public int getCounter() {
            return _i;
        }

        public boolean increment() {
            return _duration < 0 || ++_i < _duration;
        }

        public int getDuration() {
            return _duration;
        }

        public void setDuration(int d) {
            _duration = d;
        }

        public EffectorSkelton<Parent, Effect> initEffectValue(Effect val,
                                                               int duration) {
            _valueSet(val);
            setDuration(duration);
            return this;
        }

        @Override
        public boolean _valueSet(Effect value) {
            cache = /* effect_val = */value;
            return true;
        }

        @Override
        public Effect _valueGet() {
            return cache;
        }

        public Class<?> getParameterClass() {
            if (type == null) {
                ParameterizedType pt = (ParameterizedType) this.getClass()
                        .getGenericSuperclass();
                type = (Class<?>) pt.getActualTypeArguments()[0];
            }
            return type;
        }
    }

    public static abstract class OriginalEffector<Parent, Effect> extends
            EffectorSkelton<Parent, Effect> implements
            IEffector<Parent, Effect> {
        @Override
        public boolean actorRun(Actor act) throws ChainException {
            Parent _t = getTarget();
            synchronized (_t) {
                effect(_t, _valueGet());
            }
            invalidate();
            return increment();
        }

        @Override
        public abstract void effect(Parent _t, Effect _e) throws ChainException;
    }

    public static class Booster extends OriginalEffector<IBoostable, Float> {

        public Booster() {
            super();
            initEffectValue(0f, 1);
            once();
        }

        @Override
        public void effect(IBoostable _t, Float _e) throws ChainException {
            _t.boost(_e * 10f);
        }
    }

    public static class Charger extends OriginalEffector<IChargeable, Float> {

        public Charger() {
            super();
            initEffectValue(0f, 1);
            once();
        }

        @Override
        public void effect(IChargeable _t, Float _e) throws ChainException {
            _t.charge(_e * 10f);
        }
    }

    public static abstract class Register<V, E> extends EffectorSkelton<V, E> {

        public Register() {
            super();
            unsetAutoEnd();
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException,
                InterruptedException {
            register();
            return true;
        }

        @Override
        protected boolean postRun() throws ChainException {
            unregister();
            return true;
        }

        public abstract void register() throws ChainException;

        public abstract void unregister() throws ChainException;
    }

    public static class ScrollableRegister extends
            Register<IRegister, IScrollHandler> {

        public ScrollableRegister() {
            super();
        }

        @Override
        public void register() throws ChainException {
            IRegister _t = getTarget();
            _t.registerHandler(_valueGet());
        }

        @Override
        public void unregister() throws ChainException {
            IRegister _t = getTarget();
            _t.unregisterHandler(_valueGet());
        }
    }

    public static class CollidableRegister extends
            Register<ICollideRegister, IActorCollideHandler> {

        public CollidableRegister() {
            super();
        }

        @Override
        public void register() throws ChainException {
            ICollideRegister _t = getTarget();
            _t.registerCollideHandler(_valueGet());
        }

        @Override
        public void unregister() throws ChainException {
            ICollideRegister _t = getTarget();
            _t.unregisterCollideHandler(_valueGet());
        }

    }


    public static class ScrollableAdjuster extends
            EffectorSkelton<IPiece, Integer> {
        IChainAdapter a;

        public ScrollableAdjuster() {
            super();
            unsetAutoEnd();
        }

        @Override
        public void ctrlStart() throws ChainException, InterruptedException {
            super.ctrlStart();
            final Collection<IPiece> pieces = getParentChain().getPieces();
            a = new IChainAdapter<Actor>() {
                @Override
                public void adapterRun(Collection<Actor> obj) {
                    for (Object o2 : pieces)
                        if (o2 instanceof ICollidable)
                            ((ICollidable) o2).onCollideInternal(null,
                                    (IView) o2, obj, null);
                }
            };
            getParentChain().registerAdapter(a);
        }

        @Override
        public void ctrlStop() {
            super.ctrlStop();
            getParentChain().unregisterAdapter(a);
        }
    }

    public static abstract class ViewTxn<E> extends
            OriginalEffector<ViewActor, E> {

        public ViewTxn() {
            super();
        }
    }

    public static abstract class ValueEffector<EFFECT> extends
            OriginalEffector<IValue<EFFECT>, EFFECT> {
        public ValueEffector() {
            super();
        }

        @Override
        public void effect(IValue<EFFECT> _t, EFFECT _e) throws ChainException {
            L("ValueEffector valueSet").go(_t._valueSet(_e));

        }
    }

    public static abstract class ValueArrayEffector<EFFECT> extends
            ValueEffector<EFFECT> implements IValueArray<EFFECT> {
        Iterator<EFFECT> value_itr = null;
        ConcurrentLinkedQueue<EFFECT> values = new ConcurrentLinkedQueue<EFFECT>();
        EFFECT lastVal = null;

        public ValueArrayEffector() {
            super();
            setDuration(-1);
            setPull(false);
        }

        @Override
        public synchronized EFFECT _valueGetNext() {
            while (values.isEmpty())
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            if (value_itr == null || !value_itr.hasNext()) {
                value_itr = values.iterator();
            }
            return lastVal = value_itr.next();
        }

        public synchronized void addEffectValue(EFFECT... vals) {
            values.addAll(Arrays.asList(vals));
            notifyAll();
        }

        @Override
        public Collection<EFFECT> _valueGetAll() {
            return values;
        }

        @Override
        public synchronized boolean _valueSet(EFFECT val) {
            addEffectValue(val);
            notifyAll();
            return true;
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            IValue<EFFECT> _t = getTarget();
            synchronized (_t) {
                effect(_t, _valueGetNext());
            }
            invalidate();
            return increment();
        }

        @Override
        public EFFECT _valueGetLast() {
            return values.peek();
        }
    }

    public static class ArrayJumper extends ValueArrayEffector<IPoint> {
        public ArrayJumper() {
            super();
        }

        public ArrayJumper(WorldPoint... p) {
            this();
            addEffectValue(p);
        }
    }

    public static class ArrayMover extends ValueArrayEffector<IPoint> {
        public ArrayMover() {
            super();
        }

        public ArrayMover(D2Point... p) {
            this();
            addEffectValue(p);
        }

        public ArrayMover(D2Point p, Integer duration) {
            this();
            for (int i = 0; i < duration; i++)
                addEffectValue(p.multiplyNew(i));
        }

        @Override
        public IPoint _valueGetNext() {
            D2Point p = ((D2Point) super._valueGet());
            if (p == null)
                return null;
            return p.getVector();
        }
    }

    /**
     * Mover class. Mover moves View objects toward the direction of WorldPoint.
     * If direction is null, Mover gets direction from PULL connect.
     *
     * @author Hiroyuki Osaki
     */
    public static class Mover extends ValueEffector<IPoint> {
        public Mover() {
            super();
        }

        public Mover(WorldPoint p) {
            this();
            initEffectValue(p, -1);
        }

        public Mover(WorldPoint p, int duration) {
            this();
            initEffectValue(p, duration);
        }
        @Override
        public boolean _valueSet(IPoint p) {
            if (cache == null)
                cache = new WorldPoint().setDif();
            cache.set(p.multiply(0.1f));
            return true;
        }

    }

    /**
     * Jumper class. Jumper moves View objects to the absolute position of
     * WorldPoint. If position is null, Mover gets position from PULL connect.
     *
     * @author Hiroyuki Osaki
     */
    public static class Jumper extends ValueEffector<IPoint> {
        public Jumper() {
            super();
        }

        // Blueprint's getDeclaredConnstructor can not find super class'
        // constructor other than default constructor.
        // The following line can not be in super class.
        public Jumper(WorldPoint p) {
            this();
            initEffectValue(p, 1);
        }

        @Override
        public boolean _valueSet(IPoint p) {
            if (cache == null)
                cache = new WorldPoint();
            cache.set(p);
            return true;
        }
    }

    public static class Centripetal extends Mover {
        float coeff = 1f;
        IPoint center = new WorldPoint(10, 0).setDif();

        public Centripetal() {
            super();
            setDuration(-1);
        }

        // Blueprint's getDeclaredConnstructor can not find super class'
        // constructor other than default constructor.
        // The following line can not be in super class.
        public Centripetal(WorldPoint p) {
            this();
            center.set(p);
        }

        @Override
        public void effect(IValue<IPoint> _t, IPoint _e) throws ChainException {
            _t._valueSet(center.subNew(_e).multiplyNew(coeff).setDif());
        }


    }

    public static class Sizer extends ViewTxn<WorldPoint> {
        public Sizer() {
            super();
        }

        public Sizer(WorldPoint p, Integer duration) {
            this();
            initEffectValue(p, duration);
        }

        @Override
        public void effect(ViewActor _t, WorldPoint _e) throws ChainException {
            _t.setPercent(_t.getPercent().plusNew(_e));
        }
    }

    public static class NewSizer extends ViewTxn<WorldPoint> {
        public NewSizer() {
            super();
        }

        public NewSizer(WorldPoint p, Integer duration) {
            this();
            initEffectValue(p, duration);
        }

        @Override
        public void effect(ViewActor _t, WorldPoint _e) throws ChainException {
            _t.setSize(_e.setDif());
        }
    }

    public static class ControllableEffector extends
            Effector<Self, Void, Void, Controllable> {

        public ControllableEffector() {
            super();
        }
    }

    public static class Sleep extends ControllableEffector {
        int sleepinterval = 2000;

        public Sleep() {
            super();
        }

        public Sleep(int interval) {
            this();
            setSleepTime(interval);
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            try {
                Thread.sleep(sleepinterval);
            } catch (InterruptedException e) {
                throw new ChainException(this, "SleepEffect: Interrupted",
                        PieceErrorCode.INTERRUPT);
            }
            return false;
        }

        public Sleep setSleepTime(int _interval) {
            sleepinterval = _interval;
            return this;
        }
    }

    public static class Reset extends ControllableEffector implements IStep {
        boolean cont = false;

        public Reset() {
            super();
        }

        public Reset(boolean _cont) {
            this();
            setContinue(_cont);
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            getTarget(false).interrupt(
                    cont ? ControllableSignal.RESTART : ControllableSignal.END);
            return false;
        }

        public Reset setContinue(boolean cont) {
            this.cont = cont;
            return this;
        }

        @Override
        public void onStep() {
        }
    }

    public static class Ender extends Reset {
        public Ender() {
            super(false);
        }
    }


    public static class Restarter extends ControllableEffector implements IStep {
        public Restarter() {
            super();
            unsetAutoStart();
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            L("Restarter calling").go(getTarget(false).restart());
            getTarget(false).invalidate();
            return false;
        }

        @Override
        public void onStep() {
            L("Restarter tickled").go(interruptStep());
        }

    }

    public static class LogPrinter extends ControllableEffector implements
            IStep {
        public LogPrinter() {
            super();
            unsetAutoStart();
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            L("LogPrinter").go(getTarget(false).printLastExecLog());
            getTarget(false).invalidate();
            return false;
        }

        @Override
        public void onStep() {
            interruptStep();
        }

    }

    public static class LogEnabler extends ControllableEffector implements
            IStep {
        boolean level = false;

        public LogEnabler() {
            super();
            unsetAutoStart();
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            return false;
        }

        @Override
        public void onStep() {
            level = !level;
            try {
                getTarget().setLogLevel(level);
            } catch (ChainException e) {
                e.printStackTrace();
            }
            interruptStep();
        }

    }

    public static class ValueLogPrinter extends
            Effector<Self, Void, String, IValueLog> implements IStep,
            IValue<Object> {
        Object log;

        public ValueLogPrinter() {
            super();
            unsetAutoStart();
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            L("ValueLog").go(_valueSet(getTarget(false)._valueLog()));
            push(_valueGet().toString());
            return false;
        }

        @Override
        public void onStep() {
            interruptStep();
        }

        @Override
        public boolean _valueSet(Object value) {
            log = value;
            return true;
        }

        @Override
        public Object _valueGet() {
            return log;
        }

    }


    public static class HeapToFamily extends Actor.StandAlonePiece {
        @Override
        public void OnPushed(Connector p, Object obj)
                throws InterruptedException {
            outputAllSimple(PathType.FAMILY, new Packet(obj, this));
        }
    }

    public static class Alphar extends ViewTxn<Integer> {
        public Alphar alpha_init(int direction, int duration) {
            initEffectValue(direction, duration);
            return this;
        }

        @Override
        public void effect(ViewActor _t, Integer _e) throws ChainException {
            _t.setAlpha(_t.getAlpha() + _e);
        }
    }

    public static abstract class StandAlonePiece extends SimpleActor implements
            IPathListener {
        public StandAlonePiece() {
            super();
            setKickFamily(false);
            setOutPackType(PathType.FAMILY, Output.NORMAL);
            getInPack(PathType.OFFER).setUserPathListener(this);
        }
    }


    public static class Counter extends Loop {
        int counter = 0, threshold = 3;

        // 1.Initiailization
        public Counter() {
            super();
            setInPackType(PathType.EVENT, InPathPack.Input.FIRST);
        }

        public Counter(int th) {
            this();
            setThreshold(th);
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

    public static class Rotater extends ViewTxn<Integer> {
        public Rotater alpha_init(int direction, int duration) {
            initEffectValue(direction, duration);
            return this;
        }

        @Override
        public void effect(ViewActor _t, Integer _e) throws ChainException {
            _t.setAngle(_t.getAngle() + _e);
        }
    }

    public static class Colorer extends ViewTxn<Integer> {
        public Colorer color_init(int _color) {
            initEffectValue(_color, 0);
            return this;
        }

        @Override
        public void effect(ViewActor _t, Integer _e) throws ChainException {
            _t.setColor(_e);
        }
    }

    public static class ColorChanger extends ViewTxn<Integer> {
        public ColorChanger color_init(int _color) {
            initEffectValue(_color, 1);
            return this;
        }

        @Override
        public void effect(ViewActor _t, Integer _e) throws ChainException {
            _t.setColor(_t.getColor() + _e);
        }
    }

    public abstract static class LoopBoost extends Loop {
        LoopBoost() {
            super();
            super.boost();
        }
    }

    public abstract static class Relation extends LoopBoost {
        private static final long serialVersionUID = 1L;

        public Relation() {
            super();
        }

        @Override
        public boolean actorRun(Actor act) throws InterruptedException,
                ChainException {
            Object event = null;// pull();
            boolean rtn = false;
            Object obj = null;// pull();
            rtn |= relation_impl((ViewActor) obj, (ViewActor) event);
            return !rtn;
        }

        public abstract boolean relation_impl(ViewActor a, ViewActor b)
                throws InterruptedException;
    }

    @SuppressWarnings("unchecked")
    public static abstract class Filter<VALUE, INPUT, OUTPUT> extends
            Controllable<Self, INPUT, OUTPUT, Void> implements
            IFunc<VALUE, INPUT, OUTPUT>, IValue<VALUE>, ICommit {
        VALUE o;
        INPUT event;

        public Filter() {
            super();
            setAutoStart();
            setAutoEnd();
            setControlled(false);
            init(this);
        }

        public Filter(Object obj, Class<?> target) {
            super(obj, target);
            setLoop(null);
            setAutoStart();
            setAutoEnd();
            setControlled(false);
            init(this);
        }

        @Override
        public boolean actorRun(Actor act) throws InterruptedException,
                ChainException {
            event = pull();
            String tmp_pushTag = funcTag(pullTag);
            OUTPUT rtn = func(this, event);
            pushTag = tmp_pushTag;
            if (rtn != null) {
                push(rtn);
                invalidate();
                return true;
            }
            return true;
        }

        @Override
        public boolean _valueSet(VALUE value) {
            o = value;
            return true;
        }

        @Override
        public VALUE _valueGet() {
            return o;
        }

        @Override
        public Object _commit() {
            if (event != null) {
                // clearPush();
                offer(event);
                return _valueGet();
            }
            return null;
        }

        @Override
        public abstract OUTPUT func(IValue<VALUE> val, INPUT in);

        public String funcTag(String input) {
            return input;
        }

    }

    public static abstract class IntegerFilter extends
            Filter<Integer, Integer, Integer> {
        @Override
        public void init(IValue<Integer> val) {
            val._valueSet(1);
        }

    }

    public static class PlusIntegerFilter extends IntegerFilter {
        public PlusIntegerFilter() {
            super();
        }

        @Override
        public Integer func(IValue<Integer> val, Integer obj) {
            int rtn = obj + val._valueGet();
            // _valueSet(rtn);
            return rtn;
        }

    }


    public static class MultiIntegerFilter extends IntegerFilter {
        public MultiIntegerFilter() {
            super();
        }

        @Override
        public Integer func(IValue<Integer> val, Integer obj) {
            int rtn = obj * val._valueGet();
            return rtn;
        }
    }

    public static class SumIntegerFilter extends IntegerFilter {
        public SumIntegerFilter() {
            super();
        }

        @Override
        public Integer func(IValue<Integer> val, Integer obj) {
            Integer i = obj + val._valueGet();
            _valueSet(i);
            return i;
        }
    }

    public static abstract class FloatFilter extends
            Filter<Float, Float, Float> {

        public FloatFilter() {
            super();
        }
        @Override
        public void init(IValue<Float> val) {
            _valueSet(1f);

        }
    }

    public static class PlusFloatFilter extends FloatFilter {
        public PlusFloatFilter() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            float rtn = obj + val._valueGet();
            return rtn;
        }

    }

    public static class PlusExp extends FloatFilter {
        public PlusExp() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            return obj - val._valueGet() * (float) Math.log(Math.random());
        }
    }

    public static class Average extends FloatFilter {
        int count = 0;
        float sum = 0f;

        public Average() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float in) {
            count++;
            sum += in;
            val._valueSet(sum / (float) count);
            return in;
        }
    }

    public static class MultiFloatFilter extends FloatFilter {
        public MultiFloatFilter() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            float rtn = obj * val._valueGet();
            return rtn;
        }
    }

    public static class Sum extends FloatFilter {
        public Sum() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            Float sum = obj + val._valueGet();
            _valueSet(sum);
            return sum;
        }
    }

    public abstract static class StringFilter extends
            Filter<String, Object, String> {

        public StringFilter() {
            super();
        }
        @Override
        public void init(IValue<String> val) {
            _valueSet("");
        }
    }

    public static class Append extends StringFilter {
        public Append() {
            super();
        }

        @Override
        public String func(IValue<String> val, Object obj) {
            return CodingLib.encode(obj) + val._valueGet();
        }

    }

    public abstract static class Aggregator<INPUT, OUTPUT> extends
            Controllable<Self, INPUT, OUTPUT, Void> implements
            IAggregator<INPUT, OUTPUT> {
        INPUT event;

        public Aggregator() {
            super();
            setAutoStart();
            setAutoEnd();
        }

        @Override
        public boolean actorRun(Actor act) throws InterruptedException,
                ChainException {
            event = pull();
            OUTPUT rtn = aggregate(event/* eval */);
            if (rtn != null) {
                push(rtn);
                invalidate();
                return true;
            }
            return true;
            // if filter() ret urns true, output and exit single function
        }

        @Override
        public abstract OUTPUT aggregate(INPUT... inputs);
    }

    public abstract static class Consumer<VALUE, INPUT> extends
            Controllable<Self, INPUT, Void, Void> implements IValue<VALUE>,
            IConsumer<INPUT> {
        VALUE value;

        public Consumer() {
            super();
            setAutoStart();
            setAutoEnd();
            init(this);
        }

        public Consumer(Object obj, Class<?> target) {
            super(obj, target);
            setAutoStart();
            setAutoEnd();
            init(this);
        }

        @Override
        public boolean _valueSet(VALUE v) {
            value = v;
            return true;
        }

        @Override
        public VALUE _valueGet() {
            return value;
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException,
                InterruptedException {
            consume(pull());
            invalidate();
            return true;
        }
    }

    public static class Show extends Consumer<String, Object> {
        public Show() {
            super();
        }

        @Override
        public void consume(Object in) {
            _valueSet(CodingLib.encode(in));
        }

        @Override
        public void init(IValue<Object> val) {
            val._valueSet("");
        }
    }

    public static abstract class ValueConsumer<INPUT> extends Consumer<INPUT, INPUT> {
        public ValueConsumer() {
            super();
        }

        public ValueConsumer(Object obj, Class<?> target) {
            super(obj, target);
        }

        @Override
        public void consume(INPUT in) {
            _valueSet(in);
        }

    }

    public abstract static class Memory<OUTPUT> extends
            Controllable<Self, Void, OUTPUT, Void> implements IValue<OUTPUT> {
        Boolean state = true;

        // 1.Initialization
        public Memory() {
            super();
            setAutoStart();
            // setLoop(null);
        }

        public Memory(Object obj, Class<?> target) {
            super(obj, target);
            setAutoStart();
        }

        public void setMemoryState(boolean state) {
            this.state = state;
            if (state)
                setOutPackType(PathType.OFFER, Output.HIPPO);
        }

        public Boolean getMemoryState() {
            return state;
        }

    }

    public static abstract class Generator<OUTPUT> extends Actor.Memory<OUTPUT>
            implements ICommit, IGenerator<OUTPUT> {
        OUTPUT output;

        // 1.Initialization
        public Generator() {
            super();
            setLoop(null);
            init(this);
        }

        public Generator(OUTPUT obj, Boolean hippo) {
            this();
            setLoop(null);
            setMemoryState(hippo);
            init(this);
            _valueSet(obj);
        }

        public Generator(Object obj, Class<?> target) {
            super(obj, target);
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException, InterruptedException {
            generate();
            return super.actorRun(act);
        }

        @Override
        public boolean _valueSet(OUTPUT value) {
            output = value;
            return true;
        }

        @Override
        public OUTPUT _valueGet() {
            return output;
        }

        @Override
        public OUTPUT generate() {
            OUTPUT out = _valueGet();
            if (out != null) {
                push(out);
            }
            return out;
        }

        @Override
        public Object _commit() {
//			interruptRestart();
            return generate();
        }


    }

    public static class PointGenerator extends Generator<IPoint> {

        public PointGenerator() {
            super();

        }

        public PointGenerator(WorldPoint obj, Boolean hippo) {
            super(obj, hippo);
        }

        @Override
        public void init(IValue<IPoint> val) {
            val._valueSet(new WorldPoint());
        }
    }

    public static class WordGenerator extends Generator<String> {
        public WordGenerator() {
            super();
        }

        public WordGenerator(String obj, Boolean hippo) {
            super(obj, hippo);
        }

        @Override
        public void init(IValue<String> val) {
            val._valueSet("");
        }
    }

    public static class IntegerGenerator extends Generator<Integer> {

        public IntegerGenerator() {
            super();
        }

        public IntegerGenerator(Integer obj, Boolean hippo) {
            super(obj, hippo);
        }

        @Override
        public void init(IValue<Integer> val) {
            val._valueSet(0);
        }
    }

    public static class IntegerCounter extends IntegerGenerator implements IStep {
        public IntegerCounter() {
            super();
        }

        @Override
        public void onStep() {
            _valueSet(_valueGet()+1);
            interruptStep();
        }
    }

    public static class FloatGenerator extends Generator<Float> {

        public FloatGenerator() {
            super();
        }

        public FloatGenerator(Float obj, Boolean hippo) {
            super(obj, hippo);
        }

        public FloatGenerator plus(Float i) {
            _valueSet(_valueGet() + i);
            return this;
        }

        public FloatGenerator multiply(Float f) {
            _valueSet(_valueGet() * f);
            return this;
        }

        public FloatGenerator minus(Float i) {
            _valueSet(_valueGet() - i);
            return this;
        }

        @Override
        public void init(IValue<Float> val) {
            val._valueSet(0f);
        }
    }

    public static class Time extends Generator<Calendar> {

        public Time() {
            super(Calendar.getInstance(), false);
        }

        @Override
        public void init(IValue<Calendar> val) {

        }
    }


    public static class Exp extends FloatGenerator {
        int count = 1;
        float now = 0f;

        public Exp() {
            super();
        }

        public Exp(Float f) {
            super(f, false);
        }

        public Exp(Float f, Integer n) {
            super(f, false);
            count = n;
        }

        @Override
        public Float generate() {
            return now += -_valueGet() * (float) Math.log(Math.random());
        }

        @Override
        public Object _commit() {
            if (_valueGet() != null) {
                for (int i = 0; i < count; i++)
                    push(generate());
                return _valueGet();
            }
            return null;
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
            setInPackType(PathType.EVENT, InPathPack.Input.FIRST);
        }

    }

    public static class BasicSplit extends LoopBoost {
        ConnectionResultOutConnector o = null;
        Class<?> cls = null;

        BasicSplit(Class<?> _cls) {
            super();
            try {
                o = super.appended(PathType.OFFER, this, Output.SYNC);
            } catch (ChainException e) {
                e.printStackTrace();
            }
            cls = _cls;
        }

        @Override
        public ConnectionResultOutConnector appended(PathType stack_target, IPiece from,
                                                     Output type) throws ChainException {
            if (stack_target == PathType.EVENT)
                return o;
            return null;
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

    static class WorldPointFilter extends
            Filter<WorldPoint, WorldPoint, WorldPoint> {
        // 1.Initialization
        public WorldPointFilter(/* Actor p */) {
            super();
        }

        WorldPoint getWorldPoint() throws ChainException {
            return pull();
        }

        @Override
        public ChainPiece setParentChain(Chain c) {
            ChainPiece rtn = super.setParentChain(c);
            postSetParent(c);
            return rtn;
        }

        protected void postSetParent(Chain c) {
        }

        @Override
        public WorldPoint func(IValue<WorldPoint> val, WorldPoint p) {
            return null;
        }

        @Override
        public void init(IValue<WorldPoint> val) {
            val._valueSet(new WorldPoint());
        }
    }

    public static class TouchUpFilter extends Filter<Object, Object, Object> {
        // 1.Initialization
        public TouchUpFilter() {
            super();
        }

        @Override
        public Object func(IValue<Object> val, Object obj) {
            return null;
        }

        @Override
        public void init(IValue<Object> val) {

        }
    }

    public static class WaitEndHeap extends Filter<Object, Object, Object> {
        WaitEndHeap() {
            super();
        }

        @Override
        public Object func(IValue<Object> val, Object obj) {
            return null;
        }

        @Override
        public void init(IValue<Object> val) {

        }

    }

//    /**
//     * @author hiro
//     */
//    public static class ManagerPiece<T extends IPiece> extends StandAlonePiece {
//        BlueprintManager bm;
//        PieceManager maker;
//        IPiece parent;
//
//        // 1.Initialization
//        public ManagerPiece() {
//            super();
//        }
//
//        public ManagerPiece(T pb) {
//            this();
//            parent = pb;
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public void OnPushed(Connector p, Object obj)
//                throws InterruptedException {
//            if (parent != null)
//                bm.setOuterInstanceForInner(parent);
//            try {
//                outputAllSimple(PathType.FAMILY,
//                        new Packet(bm.addLocal((Class<? extends Actor>) obj)
//                                .getBlueprint().newInstance(maker), this));
//            } catch (ChainException e) {
//                maker.addLog(e.errorMessage);
//            }
//            clearPull();
//        }
//
//        @Override
//        public void addFromFactory(ActorManager maker) {
//            super.addFromFactory(maker);
//            this.maker = maker;
//        }
//    }

    public interface IFunc<VALUE, INPUT, OUTPUT> extends
            IDesigner<VALUE, INPUT, OUTPUT> {
        public OUTPUT func(IValue<VALUE> val, INPUT in);
    }

    public interface IGenerator<OUTPUT> extends IDesigner<OUTPUT, Void, OUTPUT> {
        public OUTPUT generate();
    }

    public interface IConsumer<INPUT> extends IDesigner<INPUT, INPUT, Void> {
        public void consume(INPUT in);
    }

    public interface IEffector<PARENT, EFFECT> {
        public void effect(PARENT _t, EFFECT _e) throws ChainException;
    }

    public interface IDesigner<VALUE, INPUT, OUTPUT> {
        public void init(IValue<VALUE> val);
    }

    final static CopyOnWriteArrayList<Class<?>> initedClass = new CopyOnWriteArrayList<Class<?>>();
    final static Map<LinkType, String> linkTypeName = new HashMap<LinkType, String>() {
        {
            put(LinkType.TO_CHILD, "VALUE");
            put(LinkType.PULL, "INPUT");
            put(LinkType.PUSH, "OUTPUT");
            put(LinkType.FROM_PARENT, "PARENT");
        }
    };

    public static void classLoadToLib(Class<? extends Actor> cls, ActorBlueprint bp) {
        if (initedClass.contains(cls))
            return;
        try {
            bp.newInstance(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */

    public static void log(String logs) {
//		Log.w("Actor", logs);
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject rtn = new JSONObject();
        rtn.put("Tag", getTag());
        rtn.put("Class", getClass().getName());
        for(Actor a: getPartners()) {
            rtn.accumulate("Connection", a.getName());
        }
        return rtn;
    }

}