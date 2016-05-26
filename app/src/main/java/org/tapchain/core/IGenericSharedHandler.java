package org.tapchain.core;

import org.tapchain.editor.ITapView;
import org.tapchain.editor.TapChain;

/**
 * Created by hiro on 2015/05/20.
 */
public interface IGenericSharedHandler<EDITOR, ACTOR extends IPiece, VIEW extends ITapView> {
    boolean onAttach(VIEW t1, VIEW t2, ACTOR chainPiece, ACTOR chainPiece2, TapChain.InteractionType type);
    void onAdd(ACTOR p, VIEW v, IPoint pos);
}
