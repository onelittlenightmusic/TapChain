package org.tapchain;

import android.content.res.Resources;

import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;
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

	public MyBeamTapStyle(Resources r, IActorSharedHandler sh, IActorTap t, LinkType al, ClassEnvelope clz) {
        super(r, t, BitmapMaker.makeOrReuse("Beam", R.drawable.beam));
        this.al = al;
        this.clazz = clz;
        setEventHandler(sh);
    }

    @Override
    public void onRelease(IPoint pos, IEditor edit) {
        super.onRelease(pos, edit);
        getSharedHandler().setSpot(al, this, clazz);
    }

    public void focus(LinkType al) {
        getSharedHandler().getFocusControl().unfocusAll(this);
        setColorCode(ColorLib.getLinkColor(al.reverse()));
        getSharedHandler().getFocusControl().setSpotActorLink(al);
    }

    public void unfocus() {
        setColorCode(ColorLib.ColorCode.CLEAR);
        getSharedHandler().getFocusControl().setSpotActorLink(null);
    }
}
