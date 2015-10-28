package org.tapchain.core;

import android.content.res.Resources;

import org.tapchain.IFocusable;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IFocusControl;
import org.tapchain.editor.ITap;
import org.tapchain.editor.TapChainEditor;

import java.util.Collection;

/**
 * Created by hiro on 2015/05/20.
 */
public interface IGenericSharedHandler<EDITOR, ACTOR extends IPiece, VIEW extends ITap> {
    public void onLockedScroll(EDITOR edit, VIEW startTap, IPoint wp);
    public boolean onAttach(VIEW t1, VIEW t2, ACTOR chainPiece, ACTOR chainPiece2, TapChainEditor.InteractionType type);
    public void onAdd(ACTOR p, VIEW v, IBlueprint b, IPoint pos);
    public void onPullLocked(VIEW t, ActorPullException e);
    public void onPullUnlocked(VIEW t, ActorPullException ePull);
    public void onPush(VIEW t, LinkType linkType, Object obj);

    public IFocusControl getFocusControl();
    public void resetSpot();
    public Resources getResources();
    void setSpot(LinkType al, IFocusable spot, ClassEnvelope clazz);

    public void changeFocus(VIEW t);

}
