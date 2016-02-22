package org.tapchain.core;

/**
 * Created by hiro on 2016/02/10.
 */
public interface IEffector<PARENT, EFFECT> {
    void effect(PARENT _parent, IValue<EFFECT> _effect_val) throws ChainException;
}
