package org.tapchain.core;

/**
 * Created by hiro on 2016/02/10.
 */
public interface IConsumer<VALUE, INPUT> {
    void consume(IValue<VALUE> val, INPUT in);
}
