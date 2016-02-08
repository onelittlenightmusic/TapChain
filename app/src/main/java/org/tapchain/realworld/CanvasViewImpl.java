package org.tapchain.realworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;

import org.tapchain.ActorTap;
import org.tapchain.AndroidActor;
import org.tapchain.core.Actor;
import org.tapchain.core.Chain;
import org.tapchain.core.IActor;
import org.tapchain.core.ILockedScroll;
import org.tapchain.core.IPoint;
import org.tapchain.core.IPressed;
import org.tapchain.core.IScrollable;
import org.tapchain.core.LinkType;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.ITap;
import org.tapchain.editor.PaletteSort;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprintInitialization;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/29.
 */
public class CanvasViewImpl extends TapChainWritingView {
    Rect r = new Rect();
    private TapChainEditor editor;
    public static int vector_max = 700;//, border2 = 3 * vector_max / 2;
    boolean magnet = false;
    Actor touch = null;


    public CanvasViewImpl(Context context) {
        super(context);
        paint.setColor(0xff303030);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setEditor(TapChainEditor editor) {
        this.editor = editor;
        editor.editTap()
            .add(touch = new AndroidActor.AndroidView() {
                Paint paint_ = new Paint();

                public void view_init() throws Chain.ChainException {
                    IPoint p = (WorldPoint) pullInActor().getObject();
                    setCenter(p);
                    ITap t = getEditor().searchTouchedTap(p);
                    int c = Color.BLACK;
                    if (t != null) {
                        c = Color.WHITE;
                    }
                    setColor(c);
                    setSize(new WorldPoint(100f, 100f));
                    setAlpha(100);
                }

                @Override
                public boolean view_user(Canvas canvas, IPoint sp,
                                         IPoint size, int alpha) {
                    paint_.setColor(getColor());
                    paint_.setAlpha(alpha);
                    canvas.drawCircle(sp.x(), sp.y(), size.x(), paint_);
                    return false;
                }
            }.setLinkClass(LinkType.PULL, Object.class).setLinkClass(LinkType.TO_CHILD, ViewActor.class))
            ._in()
            .add(new Actor.EffectorSkelton<ViewActor, Integer>() {
                @Override
                public boolean actorRun(Actor act) throws Chain.ChainException {
                    getTarget().addSize(new WorldPoint(30f, 0f));
                    getTarget().setAlpha(
                            getTarget().getAlpha() - 10);
                    invalidate();
                    return increment();
                }
            }.initEffectValue(1, 10))
            .nextEvent(new Actor.Reset().setContinue(true))._out()
            .save();
    }

    @Override
    public void paintBackground(Canvas canvas) {
        canvas.drawRect(r, paint);
        return;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        int xmax = getWidth(), ymax = getHeight();
        r.set(0, 0, xmax, ymax);
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void myDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        getEditor().show(canvas);
        getEditor().userShow(canvas);
        canvas.drawText(
                "View = "
                        + Integer.toString(getEditor()
                        .editTap().getChain()
                        .getViewNum()), 20, 20,
                paint_text);
        canvas.drawText(
                "Effect = "
                        + Integer.toString(getEditor()
                        .editTap().getChain()
                        .getPieces().size()), 20,
                40, paint_text);
        canvas.drawText(
                "UserView = "
                        + Integer.toString(getEditor()
                        .getChain().getViewNum()),
                20, 60, paint_text);
        canvas.drawText(
                "UserEffect = "
                        + Integer.toString(getEditor()
                        .getChain().getPieces()
                        .size()), 20, 80,
                paint_text);

    }

    public TapChainEditor getEditor() {
        return editor;
    }

    @Override
    public void shake(int interval) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(interval);
    }

    @Override
    public void showPalette(final PaletteSort sort) {
        final MainActivity act = (MainActivity) getContext();
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (act.getGrid() != null)
                    act.getGrid().setCurrentFactory(sort.getNum());
            }
        });
    }

    boolean standby = false;

    @Override
    public boolean standbyRegistration(IActorTap selected, int x, int y) {
        final MainActivity act = (MainActivity) getContext();
        GridFragment f1 = act.getGrid();
        if (f1 != null
                && f1.contains(x, y)) {
            if (standby) {
                return true;
            }
            Factory f = act.getEditor().getFactory(f1.getCurrentFactory());
            IBlueprintInitialization i = getEditor().standbyRegistration(f, selected);
            if (i != null) {
                standby = true;
                return true;
            }
        }
        return false;
    }

    public void resetRegistration() {
        standby = false;
    }

    /**
     * Fling with vector
     * @param selected Target actor tap
     * @param vector Fling vector
     * @return True when selected tap is null
     */
    @Override
    public boolean onFling(IActorTap selected, IPoint vector) {
        if (vector.x() < vector_max && vector.x() > -vector_max && vector.y() < vector_max && vector.y() > -vector_max)
            return true;//onUp(point);
        if (selected != null) {
            _onFling((ActorTap) selected, new WorldPoint(vector).setDif());
        } else {
            _onFlingBackground(vector);
        }
        return true;
    }

    private void _onFling(ActorTap t, IPoint vp) {
        getEditor().editTap()._move(t)._in()
                .add(new Accel(vp).once()).save();
    }
    private void _onFlingBackground(IPoint v) {
        moveBackground(-0.2f*v.x(), -0.2f*v.y());
    }

    void _onFlingBackgroundTo(float x, float y) {
        IPoint center = getMiddlePoint();
        moveBackground(x - center.x(), y - center.y());
    }

    private void moveBackground(final float dx, final float dy) {
        // Background center starts moving and slows down gradually.
        getEditor().editTap().addActor(new IActor() {
            float delta = 0.1f;
            int t = 0;

            @Override
            public boolean actorRun(Actor act) {
                delta = 0.15f * (float)Math.sqrt((10f - t) / 10f);
                move(delta * dx, delta * dy);
                act.invalidate();
                return ++t < 10;
            }
        }).save();
    }

    /**
     * Touch down on one point and return tap which is located on the point.
     * @param iPoint
     *          down point
     * @return
     */
    public ITap onDown(IPoint iPoint) {
        ITap t = getEditor().searchTouchedTap(iPoint);
        touch.offer(iPoint);
        return t;//captureTap(t);
    }


//    public boolean onLongPress(IActorTap selected) {
//        if (selected instanceof IPressed)
//            ((IPressed) selected).onPressed();
//        return true;
//    }

    public boolean onLockedScroll(IActorTap selected, final IPoint wp) {
        if (selected instanceof ILockedScroll)
            ((ILockedScroll)selected).onLockedScroll(getEditor(), selected, wp);
        return true;
    }

    @Override
    public boolean onLongPress(IActorTap selected) {
        if(selected == null)
            return false;
        if (selected instanceof IPressed)
            ((IPressed) selected).onPressed();
//        boolean rtn = super.onLongPress(selected);
//        if (rtn) {
            AndroidActor.AndroidDashRect a = new AndroidActor.AndroidDashRect();
            a.setSize(new WorldPoint(200f, 200f)).setColor(0xffffffff);
            a._valueGet().setOffset(selected);
            getEditor().editTap()
                    .add(a)
                    ._in()
                    .add(new Actor.Sleep(2000))
                    .nextEvent(new Actor.Ender())
                    ._out()
                    .save();
//        }
        Actor a2 = (selected).getActor();
        a2.setLogLevel(true);
        a2.setLogTag("test");
        Log.w("test", String.format("%s's setLogLevel true(lock:%s[%s], state:%s)",
                a2.getTag(), a2.getLockStatus() ? "free" : "locked",
                a2.getLockTag(),
                a2.getState()));
        a2.printLastExecLog();
        return true;
    }
    /**
     * Scroll tap by difference vector
     * @param selected target tap
     * @param vp vector
     * @param pos position
     * @return true
     */
    @Override
    public boolean scroll(IActorTap selected, final IPoint vp, final IPoint pos) {
        if (selected == null) {
            return false;
        }
        if (selected instanceof IScrollable) {
//                Log.w("test", String.format("%s scrolled", selectedTap));
            ((IScrollable) selected).onScrolled(getEditor(), pos, vp);
        }
//        getEditor().invalidate(selected);
        selected.invalidate();
        return true;
    }

    public class Accel extends Actor.Mover {
        float delta = 0.03f;
        int j = 0;
        IPoint wp = null;
        IPoint initial = null;

        public Accel(IPoint vp) {
            super();
            setLinkClass(LinkType.PULL, IPoint.class);
            initial = wp = vp.copy().unsetDif();
        }

        @Override
        public boolean actorInit() throws Chain.ChainException {
            WorldPoint dummy = new WorldPoint();
            initEffectValue(dummy, 1);
            super.actorInit();
            j = 0;
            delta = 0.1f;
            if (initial == null)
                L( "Accel#reset").go(wp = pull());
            return true;
        }

        @Override
        public boolean actorRun(Actor act) throws Chain.ChainException {
            IPoint d = wp.multiplyNew(delta);
            initEffectValue(d, 1);
            delta -= 0.01f;
            boolean rtn = ++j < 10 || d.getAbs() > 30;
            super.actorRun(act);
            if (getEditor().checkAndConnect(((IActorTap) getTarget()))) {
                round((IActorTap) getTarget());
                return false;
            }
            if (!rtn) {
                ActorTap v = (ActorTap) getTarget();
                round(v);
                getEditor().checkAndConnect(v);
            }
            return rtn;
        }
    }

    public boolean magnetToggle() {
        magnet = !magnet;
        return magnet;
    }

    private void round(IActorTap startTap2) {
//        if (styles == null || getInteract() == null)
//            return;
        startTap2._valueSet(pointOnAdd((startTap2._valueGet())));

        TapLib.setTap(startTap2);
        startTap2.invalidate();
    }

    public IPoint pointOnAdd(IPoint raw) {
        if (magnet)
            return raw.copy().plus(50f, 50f).round(100).unsetDif();
        else
            return raw.unsetDif();
    }

    @Override
    public boolean onSecondTouch(final IPoint wp) {
        return onLockedScroll(selected, wp);
    }


}
