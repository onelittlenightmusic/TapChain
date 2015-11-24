package org.tapchain;

import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IFocusControl;
import org.tapchain.realworld.R;

import java.util.HashMap;

/**
 * Created by hiro on 2015/05/04.
 */
public class MySpotOptionTapStyle extends MySimpleOptionTapStyle implements IFocusable {
    static HashMap<LinkType, SpotProperties> spotProperties = new HashMap<LinkType, SpotProperties>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(LinkType.PUSH,
                    new SpotProperties(
                            BitmapMaker.makeOrReuse("pushSpot", R.drawable.right),
                            new WorldPoint(100, 0)));
            put(LinkType.FROM_PARENT,
                    new SpotProperties(
                            BitmapMaker.makeOrReuse("parentSpot", R.drawable.up),
                            new WorldPoint(0, 100)));
            put(LinkType.TO_CHILD,
                    new SpotProperties(
                            BitmapMaker.makeOrReuse("appearanceSpot", R.drawable.down),
                            new WorldPoint(0, -100)
                    ));
            put(LinkType.PULL,
                    new SpotProperties(
                            BitmapMaker.makeOrReuse("pullSpot", R.drawable.left),
                            new WorldPoint(-100, 0)));
        }
    };
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    LinkType al = null;
    ClassEnvelope clazz = null;
    IActorSharedHandler handler;

	public MySpotOptionTapStyle(IActorTap t, LinkType al, ClassEnvelope clz, IActorSharedHandler handler) {
        super(t, spotProperties.get(al).getBitmap());
        this.al = al;
        this.clazz = clz;
        this.handler = handler;
        setCenter(spotProperties.get(al).getMargin());
    }

    @Override
    public void onRelease(IEditor edit, IPoint pos) {
        handler.changeFocus(al, this, clazz);
    }

    @Override
    public void focus(IFocusControl focusControl, LinkType al) {
        focusControl.unfocusAll(this);
        setColorCode(ColorLib.getLinkColor(al.reverse()));
        focusControl.setSpotActorLink(al);
    }

    @Override
    public void unfocus(IFocusControl focusControl) {
        setColorCode(ColorLib.ColorCode.CLEAR);
        focusControl.setSpotActorLink(null);
    }


}
