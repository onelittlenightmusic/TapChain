package org.tapchain.realworld;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprint;
import org.tapchain.editor.TapChain;

/**
 * Created by hiro on 2015/12/26.
 */
public class ActorImageButton extends ActorImage implements
        View.OnTouchListener, GestureDetector.OnGestureListener {
    OverlayPopupView p;
    final MainActivity act;
    private GestureDetector touchDetector;
    Factory<Actor> factory;
    TapChain.FACTORY_KEY key;
    int num;

    public ActorImageButton(Context context, AttributeSet attr) {
        super(context, attr);
        registerToFactory();
        act = (MainActivity) context;
        touchDetector = new GestureDetector(act, this);
        touchDetector.setIsLongpressEnabled(false);
        setOnTouchListener(this);
        TypedArray a= context.obtainStyledAttributes(
                attr,
                R.styleable.ActorImageButton);
        String factory_key = a.getString(R.styleable.ActorImageButton_factoryKey);
        Integer num_in_factory = a.getInteger(R.styleable.ActorImageButton_numInFactory, 0);
        TapChain.FACTORY_KEY key = TapChain.FACTORY_KEY.valueOf(factory_key);
        factory = ((MainActivity)context).getTapChain().getFactory(key);
        init(factory.get(num_in_factory));
    }

    ActorImageButton(Context c, IBlueprint<Actor> blueprint) {
        super(c, blueprint);
        registerToFactory();
        act = (MainActivity) c;
        touchDetector = new GestureDetector(act, this);
        touchDetector.setIsLongpressEnabled(false);
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
        switch (action) {
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                GridFragment f1 = returnPaletteAble();
                if (f1 != null
                        && f1.contains((int) event.getRawX(),
                        (int) event.getRawY())) {
                    act.add(b);
                } else {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    act.add(b, x, y);
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
        p.setPopupView(b);
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
        act.add(b, e2.getRawX(), e2.getRawY(), velocityX,
                velocityY);
        p.dismiss();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.w("test", "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}
