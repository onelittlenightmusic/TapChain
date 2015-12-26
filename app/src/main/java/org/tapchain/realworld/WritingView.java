package org.tapchain.realworld;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.tapchain.core.Actor;
import org.tapchain.core.IPoint;
import org.tapchain.editor.EditorReturn;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.ITap;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/27.
 */
public abstract class WritingView extends TapChainSurfaceView {
    IActorTap selected;

    public WritingView(Context context) {
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
//                    ((MainActivity) getContext()).setVisibility();
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return getEditor().onSingleTapConfirmed(getPosition(e.getX(), e.getY()));
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                if (mode == CAPTURED) {
                    return onSecondTouch(getPosition(e2.getX(), e2.getY()));
                }
                if (standbyRegistration(selected, (int) e2.getRawX(),
                        (int) e2.getRawY()))
                    return true;
                getEditor().onScroll(selected, getVector(-distanceX, -distanceY),
                        getPosition(e2.getX(), e2.getY()));
                return false;
            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                return getEditor().onFling(selected, getPosition(e2.getRawX(), e2.getRawY()), getVector(velocityX, velocityY));
            }

            @Override
            public void onLongPress(MotionEvent e) {
                getEditor().onLongPress(selected);
                setMode(CAPTURED);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (gdetect.onTouchEvent(ev))
            return true;
        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                ITap selectedTap = getEditor().onDown(getPosition(ev.getX(), ev.getY()));
                if (selectedTap instanceof IActorTap)
                    selected = (IActorTap) selectedTap;
                else
                    selected = null;
                resetRegistration();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                savedMatrix.set(matrix);
                oldDist = spacing(ev);
                Log.d(TAG, "oldDist=" + oldDist);
                midPoint(mid, ev);
                if (oldDist > 10f) {
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                    getEditor().releaseTap(selected, getPosition(ev.getX(), ev.getY()));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    float newDist = spacing(ev);
                    matrix.set(savedMatrix);
                    if (newDist > 10f) {
                        float scale = newDist / oldDist;
                        midPoint(mid, ev);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                } else if (mode == CAPTURED) {
                    onSecondTouch(getPosition(ev.getX(), ev.getY()));
                    break;
                }

                break;
            case MotionEvent.ACTION_UP:
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

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag) {
        return onAdd(key, tag, null, null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag, float x, float y) {
        return onAdd(key, tag, getPosition(x, y), null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag, float x, float y, float vx, float vy) {
        return onAdd(key, tag, getPosition(x, y), getVector(vx, vy));
    }


    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = getEditor().onAdd(key, tag, pos);
        if (editorReturn == null)
            return null;
        if (vec == null)
            return editorReturn.getActor();
        getEditor().onFling(editorReturn.getTap(), pos, vec);
        return editorReturn.getActor();
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code) {
        return onAdd(key, code, null, null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code, float x, float y) {
        return onAdd(key, code, getPosition(x, y), null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code, float x, float y, float vx, float vy) {
        return onAdd(key, code, getPosition(x, y), getVector(vx, vy));
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = getEditor().onAdd(key, code, pos);
        if (editorReturn == null)
            return null;
        if (vec == null)
            return editorReturn.getActor();
        getEditor().onFling(editorReturn.getTap(), pos, vec);
        return editorReturn.getActor();
    }

    int initNum = 0;
    boolean standby = false;

    public abstract boolean standbyRegistration(IActorTap selected, int x, int y);

    public abstract void resetRegistration();

    public void inclementInitNum() {
        initNum++;
    }


    @Override
    public boolean onSecondTouch(final IPoint wp) {
        return getEditor().onLockedScroll(selected, wp);
    }

}
