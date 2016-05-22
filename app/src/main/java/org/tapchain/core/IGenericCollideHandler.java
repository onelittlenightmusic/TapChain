package org.tapchain.core;

import org.tapchain.editor.ITapChain;

import java.util.Collection;

/**
 * Created by hiro on 2015/05/21.
 */
public interface IGenericCollideHandler<ACTOR extends IPiece> {
    public boolean onCollide(ITapChain edit, ActorChain.IView tap, Collection<ACTOR> tap2, IPoint pos);

}
