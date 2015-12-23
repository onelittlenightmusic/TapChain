package org.tapchain.editor;

import org.tapchain.core.Packet;

/**
 * Created by hiro on 2015/12/22.
 */
public interface Tickable<T> {
    int onTick(T t, Packet obj);
}
