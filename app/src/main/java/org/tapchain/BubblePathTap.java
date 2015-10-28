package org.tapchain;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

import org.tapchain.core.Actor;
import org.tapchain.core.Chain;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IPath;
import org.tapchain.core.IPoint;
import org.tapchain.core.ISelectable;
import org.tapchain.core.PathType;
import org.tapchain.core.TapMath;
import org.tapchain.core.ViewActor;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IEditor;
import org.tapchain.realworld.R;
import org.tapchain.realworld.TapChainView;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by hiro on 2015/05/05.
 */
public class BubblePathTap extends PathTap implements ISelectable {
    ViewActor start, stop;
    PathType starttype, stoptype;
    WorldPoint offset1 = new WorldPoint(100f, 0f), offset2 = new WorldPoint(-100f, 0f);
    Paint paint, paint2;
    IPoint sp1, sp2, sp12, sp21;
    // Tickview tickv = new Tickview();
    Bitmap bm_heart;
    Queue<Pos> bl;
    float length = 40f;
    ClassEnvelope cls = null;
    Object obj;
    float gamma = 0.3f;


    public BubblePathTap(ViewActor start, ViewActor stop,
                         PathType startType, PathType stopType,
                         org.tapchain.core.Path p) {
        super();
        bm_heart = BitmapMaker.makeOrReuse(getName(),
                R.drawable.heart_bright, 30, 30);
        bl = new ConcurrentLinkedQueue<Pos>();
        this.start = start;
        this.stop = stop;
        this.starttype = startType;
        this.stoptype = stopType;
        sp1 = start.getCenter();
        sp2 = stop.getCenter();
//        sp12 = start.getCenter();
//        sp21 = stop.getCenter();
        sp12 = ((MyTapStyle2)start).getOffsetVector(gamma);
        sp21 = ((MyTapStyle2)stop).getOffsetVector(-gamma);
//        sp12 = new WorldPoint();
//        sp21 = new WorldPoint();
//        sp12.setOffset(start, false);
//        sp21.setOffset(stop, false);
//        sp12.plus(offset1);
//        sp21.plus(WorldPoint.minus(offset2));
        cls = p.getConnectionClass();
    }

    @Override
    public void view_init() throws Chain.ChainException {
        paint = new Paint();
        paint.setColor(Color.argb(255, 255, 255, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(15);
        paint2 = new Paint();
        paint2.setTextSize(40);
        paint2.setColor(Color.argb(255, 255, 255, 255));
        paint2.setTextAlign(Paint.Align.CENTER);
        if (starttype == PathType.OFFER)
            paint.setColor(Color.argb(220, 100, 200, 255));
//        offset1 = getOffset(start.getRawSize(), starttype);
//        offset2 = getOffset(stop.getRawSize(), stoptype);
        paint2.setAntiAlias(true);
    }

    @Override
    public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
                             int alpha) {
        sp12 = ((MyTapStyle2)start).getOffsetVector(gamma);
        sp21 = ((MyTapStyle2)stop).getOffsetVector(-gamma);
//        sp12 = new WorldPoint();
//        sp21 = new WorldPoint();
//        sp12.setOffset(start, false);
//        sp21.setOffset(stop, false);
        // sp1 = start.getCenter()
        // .plus(start.getSize()._valueGet().multiply(0.5f))
        // .sub(new WorldPoint(50, 50));// .plus(offset1);
        // sp2 = stop.getCenter()
        // .plus(stop.getSize()._valueGet().multiply(0.5f))
        // .sub(new WorldPoint(50, 50));// .sub(offset2);
        Rect r1 = ((AndroidActor.AndroidView) start).getScreenRect();
        Rect r2 = ((AndroidActor.AndroidView) stop).getScreenRect();
        if (starttype == PathType.FAMILY) {
            r2.inset(-10, -10);
            Rect intersect = new Rect();
            boolean rtn = intersect.setIntersect(r1, r2);
            if (!rtn)
                return false;
            if (intersect.width() < intersect.height()) {
                sp1 = new WorldPoint(intersect.left - length / 2,
                        intersect.centerY());
                sp2 = new WorldPoint(intersect.right + length / 2,
                        intersect.centerY());
            } else {
                sp1 = new WorldPoint(intersect.centerX(), intersect.top
                        - length / 2);
                sp2 = new WorldPoint(intersect.centerX(), intersect.bottom
                        + length / 2);
            }
            Path p = new Path();
            p.moveTo(sp1.x(), sp1.y());
            p.lineTo(sp2.x(), sp2.y());
            canvas.drawPath(p, paint);

            //Draw TickView
//            for (Pos b : bl) {
//                if (b.over())
//                    bl.remove(b);
//                IPoint p1 = getPoint(b.get());
//                canvas.drawBitmap(bm_heart, p1.x() - bm_heart.getWidth()
//                        / 2, p1.y() - bm_heart.getHeight() / 2, paint);
//                b.tick();
//            }
        } else {
            Path path = new Path();
//				float xsize = sp1.x() < sp2.x() ? 50f : -50f;
//				float xoffset = sp1.x() < sp2.x() ? 50f : -50f;
            float xsize = 0f;//r1.width() / 2;
            float xoffset = offset1.x();
            path.moveTo(sp1.x() + xsize, sp1.y());
            path.cubicTo(sp12.x() + xsize, sp12.y(), sp21.x(), sp21.y(),
                    sp2.x() - xsize, sp2.y());
            canvas.drawPath(path, paint);
            //Draw TickView
//            for (Pos b : bl) {
//                if (b.over()) {
//                    bl.remove(b);
//                    continue;
//                }
//                IPoint p1 = getPoint(b.get());
//                canvas.drawBitmap(bm_heart, p1.x() - bm_heart.getWidth()
//                        / 2, p1.y() - bm_heart.getHeight() / 2, paint);
//                b.tick();
//                ShowInstance.showInstance(canvas, obj, p1, paint2, paint2);
//            }
//            if(!bl.isEmpty())
//                invalidate();
        }
        IPoint center = sp1.multiplyNew(0.5f).plusNew(sp2.multiplyNew(0.5f));
//			if (cls != null)
//				BitmapLib.drawBitmapCenter(canvas, bitmapMaker.getClassImage(cls),
//						center,
//						paint);
        if (obj != null)
//				if(obj instanceof String)
//				canvas.drawText(obj.toString(), center.x(), center.y(), paint2);
//				DrawLib.drawStringCenter(canvas, center, obj.toString(), paint2);
            ShowInstance.showInstance(canvas, obj, center, paint2, paint2);
        return true;
    }

    @Override
    public IPoint getPoint(float beta) {
        WorldPoint wp1 = new WorldPoint(((AndroidActor.AndroidView) start).getScreenRect().width()/2, 0f);
        WorldPoint wp2 = new WorldPoint(-((AndroidActor.AndroidView) stop).getScreenRect().width() / 2, 0f);
        return TapMath.getCurvePoint(beta,
                Arrays.asList(sp1.plusNew(wp1), sp12.plusNew(wp1),
                        sp21.plusNew(wp2), sp2.plusNew(wp2)));
//            return sp1.multiplyNew(1 - beta).plus(sp2.multiplyNew(beta));
    }

    public IPoint getCenter() {
        return getPoint(0.5f);
    }

    WorldPoint getOffset(IPoint iPoint, PathType type) {
        WorldPoint offset = null;
        switch (type) {
            case FAMILY:
                offset = new WorldPoint(30f, 30f);
                break;
            case OFFER:
                offset = new WorldPoint(40f, 0f);
                break;
            default:
                offset = new WorldPoint(0f, 40f);
        }
        return offset;
    }

    // View class for tick action
    public class Tickview extends ActorTap {
        public Tickview() {
        }

        @Override
        public void view_init() {
        }

        @Override
        public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
                                 int alpha) {
            return true;
        }
    }

    ;

    public class Pos {
        float b, inclement;
        Object obj;

        public Pos(int time, Object obj) {
            b = 1f/((float)time);
            this.obj = obj;
        }

        public void tick() {
            b += inclement;
        }

        public float get() {
            return b;
        }

        public boolean over() {
            return b >= 1.0f;
        }

        public Object getObject() { return obj; }
    }

    public void addPoint(int  time, Object obj) {
        bl.add(new Pos(time, obj));
    }

    @Override
    public int onTick(IPath p, Object _obj) {
        Log.w("test", obj == null ? "null" : obj.toString());
        this.obj = _obj;
        AndroidActor.AndroidView errorMark
                = new AndroidActor.AndroidView(TapChainView.getNow()) {
            @Override
            public boolean view_user(Canvas canvas, IPoint sp, IPoint iPoint,
                                     int alpha) {
                ShowInstance.showInstance(canvas, obj, sp, paint2, paint2);
                return true;
            }

        };
        errorMark.setColorCode(ColorLib.ColorCode.RED)
                .setPercent(new WorldPoint(200f, 200f))/*.setLogLevel(true)*/;
        errorMark._valueGet().setOffset(this, false);
        errorMark.once();
        manager.add(errorMark)
                ._in()
                .add(new PathMover(this, 0.04f))
                .add(new Actor.Counter(25-1)/*.setLogLevel(true)*/)
                .next(new Actor.Reset(false).once())
//                .old(new Actor.Sleep(2000)/*.setLogLevel(true)*/)
                .save();

//        if (p.getPathType() != PathType.FAMILY) {
//            addPoint(20, obj);
//				Log.w("test", "ticked");
//            return true;
//        }
//        return false;
        return 25;
    }

    @Override
    public void onSelected(IEditor edit, IPoint pos) {
        IPath path = getMyPath();
        edit.edit().disconnect(path);
    }
}
