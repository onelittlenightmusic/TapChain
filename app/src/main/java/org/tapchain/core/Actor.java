package org.tapchain.core;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.tapchain.core.ActorChain.IActorInit;
import org.tapchain.core.ActorChain.ILight;
import org.tapchain.core.ActorChain.IRecorder;
import org.tapchain.core.ActorChain.ISound;
import org.tapchain.core.Chain.ConnectionResultOutConnector;
import org.tapchain.core.Chain.ConnectionResultPath;
import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Chain.PieceErrorCode;
import org.tapchain.core.ClassLib.ClassLibReturn;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.IActorTapView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

    static Map<Class<?>, LinkedList<LinkType>> classLimits = new HashMap<>();

    // Parts of logic implementations
    private IActorInit _actorInit = null;
    private IActor _actor = null;
    private Boolean animation_loop = false, live = false;
    IActorBlueprint blueprint = null;
    ConcurrentLinkedQueue<Actor> members = new ConcurrentLinkedQueue<>();
    private int time = 0;
    HashMap<LinkType, ClassEnvelope> linkClasses = new HashMap<>();

    // 1.Initialization
    public Actor() {
        super();
        super.setFunc(this);
        setInPackType(PathType.OFFER, InPathPack.Input.FIRST);
        __setAssociatedClasses(initializeLinks(this.getClass()));
    }


    public Map<LinkType, ClassEnvelope> initializeLinks(Class<? extends IPiece> registeredClass) {
        return overrideLinks(registeredClass, registeredClass, Controllable.class);
    }

    public Map<LinkType, ClassEnvelope> overrideLinks(Class<?> registeredClass,
                                                      Class<?> sampleClass,
                                                      Class<?> baseClass) {
        if (Controllable.class.isAssignableFrom(registeredClass)) {
            ClassLibReturn classReturn = getParameters(sampleClass, /* userParameters, */
                    baseClass);
//            Log.w("Actor:override", String.format("%s <= %s", sampleClass.getSimpleName(), classReturn.toString()));
            return staticRegisterLinkClass(registeredClass, classReturn);
//            initedClass.add(registeredClass);
        }
        return getLinkClassesFromLib();
    }

    public Map<LinkType, ClassEnvelope> staticRegisterLinkClass(Class<?> cls, ClassLibReturn classReturn) {
        ClassEnvelope parameter;
//        __initLinkClass(cls);
        for (Entry<LinkType, String> e : linkTypeName.entrySet()) {
            parameter = classReturn.searchByName(e.getValue());
            if (parameter == null) {
                continue;
            }
            LinkType link = e.getKey();
            Type rawType = parameter.getRawType();
            if (rawType == Self.class)
                __addLinkClass(link, cls);
            else if (rawType != Void.class && !staticHasClassLimit(cls, link))
                __addLinkClass(link, parameter);
        }
        return getLinkClassesFromLib();
    }

    @Override
    public Actor setLogLevel(boolean _log) {
        super.setLogLevel(_log);
        return this;
    }

    public int getTime() {
        return time;
    }

    public static boolean staticHasClassLimit(
            Class<?> cls, LinkType al) {
        if (!classLimits.containsKey(cls))
            return false;
        return classLimits.get(cls).contains(al);
    }

    public static void staticAddClassLimit(Class<?> thisClass, LinkType limitedLink) {
        if (!classLimits.containsKey(thisClass))
            classLimits.put(thisClass, new LinkedList<>());
        classLimits.get(thisClass).add(limitedLink);
    }

    public static void staticAddClassLimits(Class<?> thisClass, LinkType... limitedLinks) {
        for (LinkType al : limitedLinks)
            staticAddClassLimit(thisClass, al);
    }

    public static ClassLibReturn getParameters(Class<?> parent, Class<?> target) {
        return ClassLib.getParameterizedType(parent, target);
    }

    public void __setAssociatedClasses(Map<LinkType, ClassEnvelope> connectables) {
        for (LinkType al : LinkType.values())
            setLinkClass(al, connectables.get(al));
    }

    public ClassEnvelope getLinkClassFromLib(LinkType ac) {
        return linkClasses.get(ac);
    }

    public Map<LinkType, ClassEnvelope> getLinkClassesFromLib() {
        return linkClasses;
    }

    protected void __addLinkClass(LinkType linkType,
                                  Class<?> clz) {
        __addLinkClass(linkType, new ClassEnvelope(Arrays.asList(clz)));
//        log(String.format("=====%s, added(%s, %s)", cc.getSimpleName(), linkType.toString(), clz.getSimpleName()));
    }

    protected void __addLinkClass(LinkType linkType,
                                  ClassEnvelope clz) {
        getLinkClassesFromLib().put(linkType, clz);
//        linkClasses.put(linkType, clz);
//        log(String.format("=====%s, added(%s, %s)", cc.getSimpleName(), linkType.toString(), clz.getSimpleName()));
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

    protected ActorManager newManager() {
        return new ActorManager(getRootChain());
    }

    // 3.Changing state

    @Override
    public int compareTo(@NonNull Actor obj) {
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
            if (target == null)
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
            throw new ChainException(this, "Interrupted.", PieceErrorCode.INTERRUPT);
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
        BlockingQueue<ControllableSignal> continueSignalQueue = new LinkedBlockingQueue<>(
                Integer.MAX_VALUE);
        boolean autostart = false;
        boolean autoend = false;
        // boolean auto = false;
        boolean error = false;

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
            __setAssociatedClasses(overrideLinks(thisClass, sampleClass, targetClass));
        }

        public INPUT pull() throws ChainException {
            Packet input = L("pull()").go(super.pullInActor());
            pullTag = input.getTag();
            return (INPUT) input.getObject();
        }

        public void push(OUTPUT output) {
            super.pushInActor(output, pushTag == null ? output.toString() + getTag() : pushTag);
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

        public void ctrlStart() throws ChainException, InterruptedException {
        }

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

        @Override
        public void fork(Actor a) {
            newManager()._move(this).pushTo(a).save();
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
                if (actor instanceof IActorTapView)
                    ((IActorTapView) actor).onTick(null, null);
                return true;
            }
        };
        private Boolean _cont = null, _reset = null, _error = null;

        ControllableSignal(Boolean cont, Boolean reset, Boolean error) {
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
        boolean onDo(Controllable c);
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


    public static class HeapToFamily extends Actor.StandAlonePiece {
        @Override
        public void OnPushed(Connector p, Object obj)
                throws InterruptedException {
            outputAllSimple(PathType.FAMILY, new Packet(obj, this));
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


    public abstract static class Memory<VALUE, OUTPUT> extends
            Controllable<Self, Void, OUTPUT, Void> implements IValue<VALUE> {
        Boolean state = true;

        // 1.Initialization
        public Memory() {
            super();
            setAutoStart();
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


    public interface IInit<VALUE> {
        void init(IValue<VALUE> val);
    }

    final static CopyOnWriteArrayList<Class<?>> initedClass = new CopyOnWriteArrayList<>();
    final static Map<LinkType, String> linkTypeName = new HashMap<LinkType, String>() {
        {
            put(LinkType.TO_CHILD, "VALUE");
            put(LinkType.PULL, "INPUT");
            put(LinkType.PUSH, "OUTPUT");
            put(LinkType.FROM_PARENT, "PARENT");
        }
    };

    public static Map<LinkType, ClassEnvelope> classLoadToLib(ActorBlueprint bp) {
//        if (initedClass.contains(cls))
//            return;
        Actor instance;
        try {
            instance = bp.newInstance();
            //__create dummy instance not to be added to chain
            // (this instance will be removed soon after)
            new ActorManager(bp.getRootChain()).remove(instance);
            return instance.getLinkClassesFromLib();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return instance.getLinkClassFromLib();
        return null;
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
        for (Actor a : getPartners()) {
            rtn.accumulate("Connection", a.getName());
        }
        return rtn;
    }

}