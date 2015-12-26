package org.tapchain.realworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;

/**
 * Created by hiro on 2015/12/26.
 */
public class OverlayPopup extends PopupWindow {
    View v = null;
    Context cxt = null;
    float lowx = 0f, lowy = 0f;

    public OverlayPopup(Context c) {
        super(c);
        cxt = c;
        setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setPopupView(Factory<Actor> f, int i) {
        v = new ActorImage(cxt, f, i);
        setContentView(v);
        // The following line is to prevent PopupWindow from drawing odd
        // background.
        setBackgroundDrawable(new BitmapDrawable());
    }

    public void setPopupView(View v) {
        this.v = v;
        setContentView(v);
        setBackgroundDrawable(new BitmapDrawable());
    }

    public void show(int x, int y) {
        if (v == null)
            return;
        if (!isShowing())
            showAtLocation(((Activity) cxt).findViewById(R.id.overlay),
                    Gravity.NO_GRAVITY, x - v.getWidth() / 2,
                    y - v.getHeight() / 2);
        else
            update(x - v.getWidth() / 2, y - v.getHeight() / 2, -1, -1);
        lowx = x;
        lowy = y;
    }
}
