package org.tapchain.core;

/**
 * Created by hiro on 2016/02/10.
 */
public interface IEffector<PARENT, EFFECT> {
    void effect(IValue<EFFECT> _effect_val, PARENT _parent) throws ChainException;
}
