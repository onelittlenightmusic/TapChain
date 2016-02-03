package org.tapchain.realworld;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;

/**
 * Created by hiro on 2015/12/26.
 */
public class OverlayPopupView extends PopupWindow {
    View v = null;
    Activity act = null;

    public OverlayPopupView(Activity c) {
        super(c);
        act = c;
        setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setPopupView(Factory<Actor> f, int i) {
        v = new ActorImage(act, f, i);
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

    public void show(final int x, final int y) {
        act.runOnUiThread(() -> {
            if (v == null)
                return;
            if (!isShowing())
                showAtLocation(act.findViewById(R.id.overlay),
                        Gravity.NO_GRAVITY, x - v.getWidth() / 2,
                        y - v.getHeight() / 2);
            else
                update(x - v.getWidth() / 2, y - v.getHeight() / 2, -1, -1);
        });
    }

    public void showMiddle() {
        Display d = act.getWindowManager().getDefaultDisplay();
        show(d.getWidth()/2, d.getHeight()/2);
    }
}
