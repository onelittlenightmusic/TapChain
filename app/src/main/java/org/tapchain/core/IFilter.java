package org.tapchain.core;

/**
 * Created by hiro on 2016/02/10.
 */
public interface IFilter<VALUE, INPUT, OUTPUT> {
    OUTPUT func(IValue<VALUE> val, INPUT in);
}
