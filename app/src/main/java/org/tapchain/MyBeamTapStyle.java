package org.tapchain;

import android.content.res.Resources;

import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IFocusControl;
import org.tapchain.realworld.R;

/**
 * Created by hiro on 2015/05/04.
 */
public class MyBeamTapStyle extends BeamTapStyle implements IRelease, IFocusable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    LinkType al = null;
    ClassEnvelope clazz = null;
    IActorSharedHandler handler;

	public MyBeamTapStyle(Resources r, IActorTap t, LinkType al, ClassEnvelope clz) {
        super(r, t, BitmapMaker.makeOrReuse("Beam", R.drawable.beam, 50, 50));
        this.al = al;
        this.clazz = clz;
    }

    @Override
    public void onRelease(IEditor edit, IPoint pos) {
        super.onRelease(edit, pos);
        edit.changeFocus(al, this, clazz);
    }

    @Override
    public void focus(IFocusControl focusControl, LinkType al) {
        setColorCode(ColorLib.getLinkColor(al.reverse()));
    }

    @Override
    public void unfocus(IFocusControl focusControl) {
        setColorCode(ColorLib.ColorCode.CLEAR);
    }
}
