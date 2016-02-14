package org.tapchain.core.actors;

import org.tapchain.core.Actor;
import org.tapchain.core.IValue;

/**
 * Created by hiro on 2015/11/03.
 */
public abstract class PushOut<VALUE> extends Actor.Filter<VALUE, VALUE, VALUE> {
    @Override
    public VALUE func(IValue<VALUE> val, VALUE in) {
        VALUE tmp = val._get();
        val._set(in);
        return tmp;
    }

    @Override
    public String funcTag(String input) {
        String nowtmp = getNowTag();
        setNowTag(input);
        return nowtmp;
    }

    public static class IntegerPushOut extends PushOut<Integer> {
        @Override
        public void init(IValue<Integer> val) {
            val._set(0);
        }
    }

}
