package org.tapchain.core;

import android.util.Log;

/**
 * Created by hiro on 2016/02/23.
 */
public abstract class Consumer<VALUE, INPUT> extends
        Actor.Controllable<Self, INPUT, Void, Void> implements IValue<VALUE>,
        IConsumer<VALUE, INPUT>, Actor.IInit<VALUE> {
    VALUE value;

    public Consumer() {
        super();
        setAutoStart();
        setAutoEnd();
    }

    public Consumer(Object obj, Class<?> target) {
        super(obj, target);
        setAutoStart();
        setAutoEnd();
//            init(this);
    }

    @Override
    public boolean _set(VALUE v) {
        value = v;
        return true;
    }

    @Override
    public VALUE _get() {
        return value;
    }

    @Override
    public boolean actorRun(Actor act) throws ChainException,
            InterruptedException {
        consume(this, pull());
        invalidate();
        return true;
    }

    @Override
    public boolean actorInit() throws ChainException {
        super.actorInit();
        init(this);
        return true;
    }

    public static class ConsumerSkelton<VALUE, INPUT> extends Consumer<VALUE, INPUT> {
        IConsumer<VALUE, INPUT> _consumer;
        VALUE _init;

        public ConsumerSkelton(IConsumer<VALUE, INPUT> c, VALUE i) {
            super(c, IConsumer.class);
            _consumer = c;
            _init = i;
        }


        @Override
        public void consume(IValue<VALUE> val, INPUT in) {
            _consumer.consume(val, in);
        }

        @Override
        public void init(IValue<VALUE> val) {
            Log.w("test", String.format("consumer initialized by %s", _init.toString()));
            val._set(_init);
        }
    }

    public static class Show extends Consumer<String, Object> {
        public Show() {
            super();
        }

        @Override
        public void consume(IValue<String> val, Object in) {
            val._set(CodingLib.encode(in));
        }

        @Override
        public void init(IValue<String> val) {
            val._set("");
        }
    }
}
