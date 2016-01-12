package org.tapchain.realworld;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/26.
 */
public class ActorImageButton extends ActorImage implements
        View.OnTouchListener, GestureDetector.OnGestureListener {
    OverlayPopupView p;
    final MainActivity act;
    final Factory<Actor> factory;
    final TapChainEditor.FACTORY_KEY key;
    final int num;
    private GestureDetector touchDetector;

    ActorImageButton(Context c, Factory<Actor> f, TapChainEditor.FACTORY_KEY key, final int j) {
        super(c, f, j);
        registerToFactory();
        act = (MainActivity) c;
        touchDetector = new GestureDetector(act, this);
        factory = f;
        this.key = key;
        num = j;
        setOnTouchListener(this);
    }

    private GridFragment returnPaletteAble() {
        GridFragment f1 = act.getGrid();
        f1.enable();
        return f1;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (touchDetector.onTouchEvent(event))
            return true;
        int action = event.getAction();
        // Log.w("Action", String.format("action = %d", action));
        switch (action) {
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                GridFragment f1 = returnPaletteAble();
                if (f1 != null
                        && f1.contains((int) event.getRawX(),
                        (int) event.getRawY())) {
                    act.add(key, num);
                } else {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    act.add(key, num, x, y);
                }
                p.dismiss();
                break;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        getParent().requestDisallowInterceptTouchEvent(true);
        if (p == null)
            p = new OverlayPopupView(act);
        p.setPopupView(factory, num);
        GridFragment f0 = act.getGrid();
        if (f0 != null) {
            f0.disable();
            f0.kickAutohide();
        }
        p.show((int) e.getRawX(), (int) e.getRawY());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        p.show((int) e2.getRawX(), (int) e2.getRawY());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        GridFragment f1 = returnPaletteAble();
        if (f1 != null
                && f1.contains((int) e2.getRawX(), (int) e2.getRawY())) {
            p.dismiss();
            return true;
        }
        act.add(key, num, e2.getRawX(), e2.getRawY(), velocityX,
                velocityY);
        p.dismiss();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}
