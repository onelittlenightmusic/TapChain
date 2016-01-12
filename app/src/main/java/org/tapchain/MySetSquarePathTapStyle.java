package org.tapchain;

import android.app.Activity;
import android.graphics.Bitmap;

import org.tapchain.core.D2Point;
import org.tapchain.core.IPoint;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;

/**
 * Created by hiro on 2016/01/06.
 */
public class MySetSquarePathTapStyle extends MySetPathTapStyle {
    IPoint recent = new WorldPoint();
    int side = 100;
    public MySetSquarePathTapStyle(IActorTap _p, Bitmap bm_fg) {
        super(_p, bm_fg);
    }

    @Override
    public void onScroll(IEditor edit, IActorTap tap, IPoint pos, IPoint vp) {
        if(!isInSameSquare(recent, pos)) {
            recent = new WorldPoint(pos).round(side).plus(side/2, side/2);
            getParentTap().setMyActorValue(new D2Point(recent, vp));
        }
    }

    private boolean isInSameSquare(IPoint recent, IPoint now) {
        float x = recent.x()-now.x(), y = recent.y()-now.y();
        return (x > -side && x < side && y > -side && y < side);
    }
}
