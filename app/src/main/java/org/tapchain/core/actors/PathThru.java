package org.tapchain.core.actors;

import org.tapchain.core.Actor;
import org.tapchain.core.IValue;

/**
 * Created by hiro on 2015/11/10.
 */
public abstract class PathThru<VALUE> extends Actor.Filter<VALUE, VALUE, VALUE> {
    @Override
    public VALUE func(IValue<VALUE> val, VALUE in) {
        return in;
    }

    public static class IntegerPathThru extends PathThru<Integer> {
        @Override
        public void init(IValue<Integer> val) {
            val._valueSet(0);
        }
    }
}
