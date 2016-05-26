package org.tapchain.editor;

import org.tapchain.core.Actor;

/**
 * Created by hiro on 2015/11/11.
 */
public class EditorReturn {
    Actor _actor;
    IActorTapView _tap;

    public EditorReturn(Actor actor, IActorTapView tap) {
        _actor = actor;
        _tap = tap;
    }

    public Actor getActor() {
        return _actor;
    }

    public IActorTapView getTap() {
        return _tap;
    }

}
