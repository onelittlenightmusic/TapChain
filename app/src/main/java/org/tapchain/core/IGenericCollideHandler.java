package org.tapchain.core;

import org.tapchain.editor.IEditor;

import java.util.Collection;

/**
 * Created by hiro on 2015/05/21.
 */
public interface IGenericCollideHandler<ACTOR extends IPiece> {
    public boolean onCollide(IEditor edit, ActorChain.IView tap, Collection<ACTOR> tap2, IPoint pos);

}
