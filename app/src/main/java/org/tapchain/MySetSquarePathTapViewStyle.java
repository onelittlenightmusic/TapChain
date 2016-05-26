package org.tapchain;

import android.graphics.Bitmap;

import org.tapchain.core.D2Point;
import org.tapchain.core.IPoint;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.ITapChain;

/**
 * Created by hiro on 2016/01/06.
 */
public class MySetSquarePathTapViewStyle extends MySetPathTapViewStyle {
    IPoint recent = new WorldPoint();
    int side = 100;
    public MySetSquarePathTapViewStyle(IActorTapView _p, Bitmap bm_fg) {
        super(_p, bm_fg);
    }

    @Override
    public void onScroll(ITapChain edit, IActorTapView tap, IPoint pos, IPoint vp) {
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
