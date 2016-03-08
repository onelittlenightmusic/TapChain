package org.tapchain.core;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by hiro on 2016/02/23.
 */
public abstract class Generator<VALUE, OUTPUT> extends Actor.Memory<VALUE, OUTPUT>
        implements ICommit, IGenerator<VALUE, OUTPUT>, Actor.IInit<VALUE> {
    VALUE output;

    // 1.Initialization
    public Generator() {
        super();
    }

    public Generator(VALUE obj, Boolean hippo) {
        this();
        setMemoryState(hippo);
        _set(obj);
    }

    public Generator(Object obj, Class<?> target) {
        super(obj, target);
    }

    @Override
    public boolean actorRun(Actor act) throws ChainException, InterruptedException {
        generate(this);
        return super.actorRun(act);
    }

    @Override
    public boolean _set(VALUE value) {
        output = value;
        return true;
    }

    @Override
    public VALUE _get() {
        return output;
    }

    @Override
    public Object _commit() {
        return generate(this);
    }

    @Override
    public boolean actorInit() throws ChainException {
        super.actorInit();
        init(this);
        return true;
    }

    public static abstract class SimpleGenerator<OUTPUT> extends org.tapchain.core.Generator<OUTPUT, OUTPUT> {
        public SimpleGenerator() {
            super();
        }

        public SimpleGenerator(OUTPUT obj, Boolean hippo) {
            super(obj, hippo);
        }

        public SimpleGenerator(Object obj, Class<?> target) {
            super(obj, target);
        }

        @Override
        public OUTPUT generate(IValue<OUTPUT> val) {
            OUTPUT out = _get();
            if (out != null) {
                push(out);
            }
            return out;
        }
    }

    public static class GeneratorSkelton<VALUE, OUTPUT> extends org.tapchain.core.Generator<VALUE, OUTPUT> {
        IGenerator<VALUE, OUTPUT> _generator;
        VALUE _init;

        public GeneratorSkelton(IGenerator<VALUE, OUTPUT> g, VALUE i) {
            super(g, IGenerator.class);
            _generator = g;
            _init = i;
        }

        @Override
        public void init(IValue<VALUE> val) {
            Log.w("test", String.format("generatoR initialized by %s", val.toString()));
            val._set(_init);
        }

        @Override
        public OUTPUT generate(IValue<VALUE> val) {
            OUTPUT out = _generator.generate(this);
            if (out != null) {
                push(out);
            }
            return out;
        }

    }

    public static class PointGenerator extends SimpleGenerator<IPoint> {

        public PointGenerator() {
            super();

        }

        public PointGenerator(WorldPoint obj, Boolean hippo) {
            super(obj, hippo);
        }

        @Override
        public void init(IValue<IPoint> val) {
            val._set(new WorldPoint());
        }
    }

    public static class WordGenerator extends SimpleGenerator<String> {
        public WordGenerator() {
            super();
        }

        public WordGenerator(String obj, Boolean hippo) {
            super(obj, hippo);
        }

        @Override
        public void init(IValue<String> val) {
            val._set("");
        }
    }

    public static class IntegerGenerator extends SimpleGenerator<Integer> {

        public IntegerGenerator() {
            super();
        }

        @Override
        public void init(IValue<Integer> val) {
            val._set(0);
        }
    }

    public static class IntegerCounter extends IntegerGenerator implements IStep {
        public IntegerCounter() {
            super();
        }

        @Override
        public void onStep() {
            _set(_get() + 1);
            interruptStep();
        }

        boolean inited = false;

        @Override
        public void init(IValue<Integer> val) {
            if (inited) return;
            inited = true;
            super.init(val);
        }
    }

    public static class FloatGenerator extends SimpleGenerator<Float> {

        public FloatGenerator() {
            super();
        }

        public FloatGenerator(Float obj, Boolean hippo) {
            super(obj, hippo);
        }

        public FloatGenerator plus(Float i) {
            _set(_get() + i);
            return this;
        }

        public FloatGenerator multiply(Float f) {
            _set(_get() * f);
            return this;
        }

        public FloatGenerator minus(Float i) {
            _set(_get() - i);
            return this;
        }

        @Override
        public void init(IValue<Float> val) {
            val._set(0f);
        }
    }

    public static class Time extends SimpleGenerator<Calendar> {

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
        public Float generate(IValue<Float> val) {
            return now += -_get() * (float) Math.log(Math.random());
        }

        @Override
        public Object _commit() {
            if (_get() != null) {
                for (int i = 0; i < count; i++)
                    push(generate(this));
                return _get();
            }
            return null;
        }
    }
}
