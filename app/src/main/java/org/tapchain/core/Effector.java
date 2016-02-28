package org.tapchain.core;

import org.tapchain.core.actors.ViewActor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by hiro on 2016/02/23.
 */
public abstract class Effector<Parent, Effect> extends
        Actor.Controllable<Self, Void, Void, Parent> implements IValue<Effect>,
        IEffector<Parent, Effect> {
    PathType parent_type = PathType.FAMILY;
    private int _i = 0, _duration = 0;
    Effect /* effect_val = null, */cache = null;
    Class<?> type = null;

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

    public org.tapchain.core.Effector setParentType(PathType type) {
        parent_type = type;
        return this;
    }

    public Parent getTarget() throws ChainException {
        return getTarget(true);
    }

    public Parent getTarget(boolean wait) throws ChainException {
        try {
            return (Parent) getParent(parent_type, wait);
        } catch (ClassCastException e) {
            throw new ChainException(this,
                    "EffectSkelton: Failed to get Parent",
                    parent_type.getErrorCode());
        }
    }

    @Override
    public boolean actorInit() throws ChainException {
        super.actorInit();
        setCounter(0);
        return true;
    }

//        public void setPull(boolean p) {
//            pull = p;
//        }

    public org.tapchain.core.Effector<Parent, Effect> setCounter(int _i) {
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

    public org.tapchain.core.Effector<Parent, Effect> initEffectValue(Effect val,
                                                                      int duration) {
        _set(val);
        setDuration(duration);
        return this;
    }

    @Override
    public boolean _set(Effect value) {
        cache = /* effect_val = */value;
        return true;
    }

    @Override
    public Effect _get() {
        return cache;
    }

    @Override
    public boolean actorRun(Actor act) throws ChainException {
        Parent _p = getTarget();
        synchronized (_p) {
            effect(_p, this);
        }
        invalidate();
        return increment();
    }

    @Override
    public abstract void effect(Parent _parent, IValue<Effect> _effect_val) throws ChainException;

    public static class EffectorSkelton<Parent, Effect> extends
            Effector<Parent, Effect> {
        IEffector<Parent, Effect> _effector;

        public EffectorSkelton(IEffector<Parent, Effect> ef, Effect i, int duration) {
            super(ef, IEffector.class);
            _effector = ef;
            initEffectValue(i, duration);
        }

        @Override
        public void effect(Parent _parent, IValue<Effect> _effect_val) throws ChainException {
            _effector.effect(_parent, _effect_val);
        }
    }

//    public static abstract class OriginalEffector<Parent, Effect> extends
//            org.tapchain.core.Effector<Parent, Effect> {
//    }

    public static abstract class Register<V, E> extends org.tapchain.core.Effector<V, E> {

        public Register() {
            super();
            unsetAutoEnd();
        }

        @Override
        public void effect(V _p, IValue<E> _e) throws ChainException {
            register();
        }

        @Override
        protected boolean postRun() throws ChainException {
            unregister();
            return true;
        }

        public abstract void register() throws ChainException;

        public abstract void unregister() throws ChainException;
    }


    public static abstract class ViewTxn<E> extends
            Effector<ViewActor, E> {

        public ViewTxn() {
            super();
        }
    }

    public static abstract class ValueEffector<EFFECT> extends
            Effector<IValue<EFFECT>, EFFECT> {
        public ValueEffector() {
            super();
        }

        @Override
        public void effect(IValue<EFFECT> _parent, IValue<EFFECT> _effect_val) throws ChainException {
            L("ValueEffector valueSet").go(_parent._set(_effect_val._get()));

        }
    }

    public static abstract class ValueArrayEffector<EFFECT> extends
            ValueEffector<EFFECT> implements IValueArray<EFFECT> {
        Iterator<EFFECT> value_itr = null;
        ConcurrentLinkedQueue<EFFECT> values = new ConcurrentLinkedQueue<>();
        EFFECT lastVal = null;

        public ValueArrayEffector() {
            super();
            setDuration(-1);
//            setPull(false);
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
        public synchronized boolean _set(EFFECT val) {
            addEffectValue(val);
            notifyAll();
            return true;
        }

        @Override
        public synchronized EFFECT _get() {
            return _valueGetNext();
        }

        @Override
        public boolean actorRun(Actor act) throws ChainException {
            IValue<EFFECT> _t = getTarget();
            synchronized (_t) {
                effect(_t, this);
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
            D2Point p = ((D2Point) super._get());
            if (p == null)
                return null;
            return p.getVector();
        }
    }

    /**
     * Mover class. Mover moves View objects toward the direction of WorldPoint.
     * If direction is null, Mover gets direction from PULL link.
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
        public boolean _set(IPoint p) {
            if (cache == null)
                cache = new WorldPoint().setDif();
            cache.set(p.multiply(0.1f));
            return true;
        }

    }

    /**
     * Jumper class. Jumper moves View objects to the absolute position of
     * WorldPoint. If position is null, Mover gets position from PULL link.
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
        public boolean _set(IPoint p) {
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
        public void effect(IValue<IPoint> _parent, IValue<IPoint> _effect_val) throws ChainException {
            _parent._set(center.subNew(_effect_val._get()).multiplyNew(coeff).setDif());
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
        public void effect(ViewActor _parent, IValue<WorldPoint> _effect_val) throws ChainException {
            _parent.setPercent(_parent.getPercent().plusNew(_effect_val._get()));
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
        public void effect(ViewActor _parent, IValue<WorldPoint> _effect_val) throws ChainException {
            _parent.setSize(_effect_val._get().copy().setDif());
        }
    }

//    public static class ControllableEffector extends
//            Effector<Self, Void, Void, Controllable> {
//
//        public ControllableEffector() {
//            super();
//        }
//    }

    public static class Sleep extends org.tapchain.core.Effector<Controllable, Void> {
        int sleepinterval = 2000;

        public Sleep() {
            super();
        }

        public Sleep(int interval) {
            this();
            setSleepTime(interval);
        }

//        @Override
//        public boolean actorRun(Actor act) throws ChainException {
//            try {
//                Thread.sleep(sleepinterval);
//            } catch (InterruptedException e) {
//                throw new ChainException(this, "SleepEffect: Interrupted",
//                        PieceErrorCode.INTERRUPT);
//            }
//            return false;
//        }

        @Override
        public void effect(Controllable _parent, IValue<Void> _effect_val) throws ChainException {
            try {
                Thread.sleep(sleepinterval);
            } catch (InterruptedException e) {
                throw new ChainException(this, "SleepEffect: Interrupted",
                        Chain.PieceErrorCode.INTERRUPT);
            }
//            return false;
        }

        public Sleep setSleepTime(int _interval) {
            sleepinterval = _interval;
            return this;
        }
    }

    public static class Reset extends org.tapchain.core.Effector<Controllable, ControllableSignal> implements IStep {
        boolean cont = false;

        public Reset() {
            super();
        }

        public Reset(boolean _cont) {
            this();
            setContinue(_cont);
        }

//        @Override
//        public boolean actorRun(Actor act) throws ChainException {
//            getTarget(false).interrupt(
//                    cont ? ControllableSignal.RESTART : ControllableSignal.END);
//            return false;
//        }

        @Override
        public void effect(Controllable _parent, IValue<ControllableSignal> _effect_val) throws ChainException {
            _parent.interrupt(_effect_val._get());
//            return false;

        }

        public Reset setContinue(boolean cont) {
            this.cont = cont;
            _set(cont ? ControllableSignal.RESTART : ControllableSignal.END);
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


    public static class Restarter extends Effector<Controllable, Void> implements IStep {
        public Restarter() {
            super();
            unsetAutoStart();
        }

//        @Override
//        public boolean actorRun(Actor act) throws ChainException {
//            L("Restarter calling").go(getTarget(false).restart());
//            getTarget(false).invalidate();
//            return false;
//        }

        @Override
        public void effect(Controllable _parent, IValue<Void> _effect_val) throws ChainException {
            L("Restarter calling").go(_parent.restart());
//            getTarget(false).invalidate();

        }

        @Override
        public void onStep() {
            L("Restarter tickled").go(interruptStep());
        }

    }

    public static class LogPrinter extends org.tapchain.core.Effector<Controllable, String> implements
            IStep {
        public LogPrinter() {
            super();
            unsetAutoStart();
        }

//        @Override
//        public boolean actorRun(Actor act) throws ChainException {
//            L("LogPrinter").go(getTarget(false).printLastExecLog());
//            getTarget(false).invalidate();
//            return false;
//        }

        @Override
        public void effect(Controllable _parent, IValue<String> _effect_val) throws ChainException {
            L("LogPrinter").go(_parent.printLastExecLog());

        }

        @Override
        public void onStep() {
            interruptStep();
        }

    }

//    public static class LogEnabler extends ControllableEffector implements
//            IStep {
//        boolean level = false;
//
//        public LogEnabler() {
//            super();
//            unsetAutoStart();
//        }
//
//        @Override
//        public boolean actorRun(Actor act) throws ChainException {
//            return false;
//        }
//
//        @Override
//        public void onStep() {
//            level = !level;
//            try {
//                getTarget().setLogLevel(level);
//            } catch (ChainException e) {
//                e.printStackTrace();
//            }
//            interruptStep();
//        }
//
//    }

    public static class ValueLogPrinter extends
            org.tapchain.core.Effector<IValueLog, Object> implements IStep {
        Object log;

        public ValueLogPrinter() {
            super();
            unsetAutoStart();
        }

//        @Override
//        public boolean actorRun(Actor act) throws ChainException {
//            L("ValueLog").go(_set(getTarget(false)._valueLog()));
////            push(_get().toString());
//            return false;
//        }

        @Override
        public void effect(IValueLog _p, IValue<Object> _e) throws ChainException {
            L("ValueLog").go(_set(_p._valueLog()));
        }

        @Override
        public void onStep() {
            interruptStep();
        }

        @Override
        public boolean _set(Object value) {
            log = value;
            return true;
        }

        @Override
        public Object _get() {
            return log;
        }

    }

    public static class Alphar extends ViewTxn<Integer> {
        public Alphar alpha_init(int direction, int duration) {
            initEffectValue(direction, duration);
            return this;
        }

        @Override
        public void effect(ViewActor _t, IValue<Integer> _e) throws ChainException {
            _t.setAlpha(_t.getAlpha() + _e._get());
        }
    }

    public static class Rotater extends ViewTxn<Integer> {
        public Rotater alpha_init(int direction, int duration) {
            initEffectValue(direction, duration);
            return this;
        }

        @Override
        public void effect(ViewActor _t, IValue<Integer> _e) throws ChainException {
            _t.setAngle(_t.getAngle() + _e._get());
        }
    }

    public static class Colorer extends ViewTxn<Integer> {
        public Colorer color_init(int _color) {
            initEffectValue(_color, 0);
            return this;
        }

        @Override
        public void effect(ViewActor _t, IValue<Integer> _e) throws ChainException {
            _t.setColor(_e._get());
        }
    }

    public static class ColorChanger extends ViewTxn<Integer> {
        public ColorChanger color_init(int _color) {
            initEffectValue(_color, 1);
            return this;
        }

        @Override
        public void effect(ViewActor _t, IValue<Integer> _e) throws ChainException {
            _t.setColor(_t.getColor() + _e._get());
        }
    }
}
