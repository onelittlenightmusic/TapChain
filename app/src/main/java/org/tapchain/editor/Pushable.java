package org.tapchain.editor;

import org.tapchain.core.ActorManager;

/**
 * Created by hiro on 2015/12/22.
 */
public interface Pushable<T> {
    boolean onPush(T t, Object obj, ActorManager actorManager);
}
