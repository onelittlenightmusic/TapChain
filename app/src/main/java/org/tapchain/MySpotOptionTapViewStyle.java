package org.tapchain;

import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.ColorLib;
import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.ITapChain;
import org.tapchain.editor.IFocusControl;
import org.tapchain.editor.IFocusable;
import org.tapchain.realworld.R;
import org.tapchain.editor.SpotProperties;

import java.util.HashMap;

/**
 * Created by hiro on 2015/05/04.
 */
public class MySpotOptionTapViewStyle extends MySimpleOptionTapViewStyle implements IFocusable {
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

	public MySpotOptionTapViewStyle(IActorTapView t, LinkType al, ClassEnvelope clz) {
        super(t, spotProperties.get(al).getBitmap());
        this.al = al;
        this.clazz = clz;
        setCenter(spotProperties.get(al).getMargin());
    }

    @Override
    public boolean onRelease(ITapChain edit, IPoint pos) {
        boolean rtn = super.onRelease(edit, pos);
        edit.changeFocus(al, this, clazz);
        return rtn;
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
