package org.tapchain.core;

import android.util.Log;

/**
 * Created by hiro on 2016/02/23.
 */
@SuppressWarnings("unchecked")
public abstract class Filter<VALUE, INPUT, OUTPUT> extends
        Actor.Controllable<Self, INPUT, OUTPUT, Void> implements
        IFilter<VALUE, INPUT, OUTPUT>, IValue<VALUE>, Actor.IInit<VALUE>, ICommit {
    VALUE o;
    INPUT event;

    public Filter() {
        super();
        setAutoStart();
        setAutoEnd();
        setControlled(false);
//            init(this);
    }

    public Filter(Object obj, Class<?> target) {
        super(obj, target);
//            setLoop(null);
        setAutoStart();
        setAutoEnd();
        setControlled(false);
//            init(this);
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
    public boolean _set(VALUE value) {
        o = value;
        return true;
    }

    @Override
    public VALUE _get() {
        return o;
    }

    @Override
    public Object _commit() {
        if (event != null) {
            // clearPush();
            offer(event);
            return _get();
        }
        return null;
    }

    @Override
    public abstract OUTPUT func(IValue<VALUE> val, INPUT in);

    public String funcTag(String input) {
        return input;
    }

    @Override
    public boolean actorInit() throws ChainException {
        super.actorInit();
        init(this);
        return true;
    }


    public static class FilterSkelton<VALUE, INPUT, OUTPUT> extends org.tapchain.core.Filter<VALUE, INPUT, OUTPUT> {
        IFilter<VALUE, INPUT, OUTPUT> _func;
        VALUE _init;

        public FilterSkelton(IFilter<VALUE, INPUT, OUTPUT> f, VALUE i) {
            super(f, IFilter.class);
            _func = f;
            _init = i;
        }

        @Override
        public void init(IValue<VALUE> val) {
            val._set(_init);
            Log.w("test", "filter initialized");
        }

        @Override
        public OUTPUT func(IValue<VALUE> val, INPUT in) {
            Log.w("test", "filter func called");
            return _func.func(val, in);
        }
    }

    public static abstract class IntegerFilter extends
            org.tapchain.core.Filter<Integer, Integer, Integer> {
        @Override
        public void init(IValue<Integer> val) {
            val._set(1);
        }

    }

    public static class PlusIntegerFilter extends IntegerFilter {
        public PlusIntegerFilter() {
            super();
        }

        @Override
        public Integer func(IValue<Integer> val, Integer obj) {
            int rtn = obj + val._get();
            // _set(rtn);
            return rtn;
        }

    }


    public static class MultiIntegerFilter extends IntegerFilter {
        public MultiIntegerFilter() {
            super();
        }

        @Override
        public Integer func(IValue<Integer> val, Integer obj) {
            int rtn = obj * val._get();
            return rtn;
        }
    }

    public static class SumIntegerFilter extends IntegerFilter {
        public SumIntegerFilter() {
            super();
        }

        @Override
        public Integer func(IValue<Integer> val, Integer obj) {
            Integer i = obj + val._get();
            _set(i);
            return i;
        }
    }

    public static abstract class FloatFilter extends
            org.tapchain.core.Filter<Float, Float, Float> {

        public FloatFilter() {
            super();
        }

        @Override
        public void init(IValue<Float> val) {
            _set(1f);

        }
    }

    public static class PlusFloatFilter extends FloatFilter {
        public PlusFloatFilter() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            float rtn = obj + val._get();
            return rtn;
        }

    }

    public static class PlusExp extends FloatFilter {
        public PlusExp() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            return obj - val._get() * (float) Math.log(Math.random());
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
            val._set(sum / (float) count);
            return in;
        }
    }

    public static class MultiFloatFilter extends FloatFilter {
        public MultiFloatFilter() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            float rtn = obj * val._get();
            return rtn;
        }
    }

    public static class Sum extends FloatFilter {
        public Sum() {
            super();
        }

        @Override
        public Float func(IValue<Float> val, Float obj) {
            Float sum = obj + val._get();
            _set(sum);
            return sum;
        }
    }

    public abstract static class StringFilter extends
            org.tapchain.core.Filter<String, Object, String> {

        public StringFilter() {
            super();
        }

        @Override
        public void init(IValue<String> val) {
            _set("");
        }
    }

    public static class Append extends StringFilter {
        public Append() {
            super();
        }

        @Override
        public String func(IValue<String> val, Object obj) {
            return CodingLib.encode(obj) + val._get();
        }

    }

}
