package org.tapchain.core.actors;

import org.tapchain.core.Actor;
import org.tapchain.core.IFunc;
import org.tapchain.core.IValue;

/**
 * Created by hiro on 2015/11/10.
 */
public abstract class PassThru<VALUE> extends Actor.Filter<Void, VALUE, VALUE> {
    @Override
    public VALUE func(IValue<Void> val, VALUE in) {
        return in;
    }

    public static class IntegerPassThru extends PassThru<Integer> {
        @Override
        public void init(IValue<Void> val){
        }
    }

    public interface IPassThru<VALUE> extends IFunc<VALUE, VALUE, VALUE> {
        @Override
        default VALUE func(IValue<VALUE> val, VALUE in) {
            return in;
        };

    }
}
