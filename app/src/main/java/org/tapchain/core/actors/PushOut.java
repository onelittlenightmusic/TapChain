package org.tapchain.core.actors;

import org.tapchain.core.Actor;
import org.tapchain.core.IValue;

/**
 * Created by hiro on 2015/11/03.
 */
public abstract class PushOut<VALUE> extends Actor.Filter<VALUE, VALUE, VALUE> {
    @Override
    public VALUE func(IValue<VALUE> val, VALUE in) {
        VALUE tmp = val._valueGet();
        val._valueSet(in);
        return tmp;
    }

    public static class IntegerPushOut extends PushOut<Integer> {
        @Override
        public void init(IValue<Integer> val) {
            val._valueSet(0);
        }
    }
}