package org.tapchain.core;

/**
 * Created by hiro on 2016/02/10.
 */
public interface IGenerator<VALUE, OUTPUT> {
    OUTPUT generate(IValue<VALUE> val);
}
