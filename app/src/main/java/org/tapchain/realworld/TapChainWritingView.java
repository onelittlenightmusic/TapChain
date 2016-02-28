package org.tapchain.realworld;

import android.content.Context;
import android.graphics.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.tapchain.core.IPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.ITap;

/**
 * Created by hiro on 2015/12/27.
 */
public abstract class TapChainWritingView extends TapChainSurfaceView {
    IActorTap selected;
    Matrix savedMatrix = new Matrix();

    public TapChainWritingView(Context context) {
        super(context);
//            setSize(300, 300);
        move(-100, -100);
        gdetect = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                Log.w("Action", "onSingleTapConfirmed called");
                getEditor().onSingleTouch(getPosition(e.getX(), e.getY()));
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
//                Log.w("Action", "onScroll called");
                if(mode == ZOOM) {
                    //if second pointer has been down, this handler ends soon and continues to onTouchEvent
                    return false;
                }
                if (mode == CAPTURED) {
                    return onSecondTouch(getPosition(e2.getX(), e2.getY()));
                }
                if (standbyRegistration(selected, (int) e2.getRawX(),
                        (int) e2.getRawY()))
                    return true;
                IPoint v = getVector(-distanceX, -distanceY);
                if(scroll(selected, v,
                        getPosition(e2.getX(), e2.getY())))
                    return true;
                move(-v.x(), -v.y());
                TapChainWritingView.this.onDraw();
                return true;
            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
//                Log.w("Action", "onFling called");
                TapChainWritingView.this.onFling(selected, getVector(velocityX, velocityY));
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                TapChainWritingView.this.onLongPress(selected);
                setMode(CAPTURED);
            }
        });
    }

    protected abstract boolean scroll(IActorTap selected, IPoint v, IPoint position);

    protected abstract ITap onDown(IPoint position);

    protected abstract boolean onLongPress(IActorTap selected);

    protected abstract boolean onFling(IActorTap selected, IPoint vector);

    public boolean onTouchEvent(MotionEvent ev) {
        if (gdetect.onTouchEvent(ev))
            return true;
        int action = ev.getAction();
//        Log.w(TAG, String.format("Action=%d", action));
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                ITap selectedTap = onDown(getPosition(ev.getX(), ev.getY()));
                if (selectedTap instanceof IActorTap)
                    selected = (IActorTap) selectedTap;
                else
                    selected = null;
                resetRegistration();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                savedMatrix.set(matrix);
                oldDist = spacing(ev);
                Log.w(TAG, "oldDist=" + oldDist);
                midPoint(mid, ev);
                if (oldDist > 10f) {
                    mode = ZOOM;
                    Log.w(TAG, "mode=ZOOM");
                    getEditor().releaseTap(selected, getPosition(ev.getX(), ev.getY()));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    Log.w(TAG, "Action_Move,Zoom");
                    float newDist = spacing(ev);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        midPoint(mid, ev);
                        Log.w(TAG, String.format("Action_Move,Zoom,Change %f,%f,%f", scale, mid.x, mid.y));
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        onDraw();
                    }
                } else if (mode == CAPTURED) {
                    Log.w(TAG, "Action_Move");
                    onSecondTouch(getPosition(ev.getX(), ev.getY()));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mode = NONE;
                getEditor().releaseTap(selected, getPosition(ev.getX(), ev.getY()));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                matrix.invert(inverse);
                break;
        }
        return true;
    }


    /**
     * @return the editor
     */



    public abstract boolean standbyRegistration(IActorTap selected, int x, int y);

    public abstract void resetRegistration();



}
