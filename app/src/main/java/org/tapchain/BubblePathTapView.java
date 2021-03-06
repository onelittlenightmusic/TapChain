package org.tapchain;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ChainException;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.Effector;
import org.tapchain.core.IPath;
import org.tapchain.core.IPoint;
import org.tapchain.core.ISelectable;
import org.tapchain.core.Packet;
import org.tapchain.core.PathType;
import org.tapchain.core.TapMath;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.ColorLib;
import org.tapchain.editor.TapManager;
import org.tapchain.editor.ITapChain;
import org.tapchain.realworld.R;
import org.tapchain.viewlib.ShowInstance;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by hiro on 2015/05/05.
 */
public class BubblePathTapView extends PathTapView implements ISelectable {
    ViewActor start, stop;
    PathType starttype, stoptype;
    Paint paint, paint2;
    IPoint sp1, sp2, sp12, sp21;
    // Tickview tickv = new Tickview();
    Bitmap bm_heart;
    Queue<Pos> bl;
    float length = 40f;
    ClassEnvelope cls = null;
    Object objcache;
    float gamma = 0.3f;
    //    List<IPoint> ar;
    int sampling_rate = 1, sampling_count = 0;


    public BubblePathTapView(ViewActor start, ViewActor stop,
                             PathType startType, PathType stopType,
                             org.tapchain.core.Path p) {
        super();
        bm_heart = BitmapMaker.makeOrReuse(getName(),
                R.drawable.heart_bright, 30, 30);
        bl = new ConcurrentLinkedQueue<>();
        this.start = start;
        this.stop = stop;
        this.starttype = startType;
        this.stoptype = stopType;
        cls = p.getConnectionClass();
        sp1 = start.getCenter();
        sp2 = stop.getCenter();
        sp12 = ((MyTapViewStyle2)start).getOffsetVector(cls, gamma);
        sp21 = ((MyTapViewStyle2)stop).getOffsetVector(cls, -gamma);
    }

    @Override
    public void view_init() throws ChainException {
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
            switch(cls.getRawClass().getSimpleName()) {
                case "String":
                    paint.setColor(Color.argb(255, 200, 230, 200));
                    break;
                case "Integer":
                    paint.setColor(Color.argb(255, 220, 100, 100));
                    break;
                case "Wind":
                    paint.setColor(Color.argb(255, 220, 100, 200));
                    break;
                case "ElectricPower":
                    paint.setColor(Color.argb(255, 220, 220, 100));
                    break;
                case "Fuel":
                    paint.setColor(Color.argb(255, 100, 220, 200));
                    break;
            }
        paint2.setAntiAlias(true);
    }

    Path path = new Path();
    @Override
    public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
                             int alpha) {
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
        } else {
            path.reset();
            float xsize = 0f;//r1.width() / 2;
            path.moveTo(sp1.x() + xsize, sp1.y());
            path.cubicTo(sp12.x() + xsize, sp12.y(), sp21.x(), sp21.y(),
                    sp2.x() - xsize, sp2.y());
            canvas.drawPath(path, paint);
        }
        return true;
    }

    @Override
    public IPoint getPoint(float beta) {
        return TapMath.getCurvePoint(beta, Arrays.asList(sp1, sp12, sp21, sp2));
    }

    public IPoint getCenter() {
        return getPoint(0.5f);
    }

    public class Pos {
        float b;
        Object obj;

        public Pos(int time, Object obj) {
            b = 1f/((float)time);
            this.obj = obj;
        }

        public float get() {
            return b;
        }

        public Object getObject() { return obj; }
    }

    public void setTickSamplingRate(int rate) {
        sampling_rate = rate;
    }

    @Override
    public int onTick(IPath p, final Packet _obj) {
        if(sampling_count == 0) {
            this.objcache = _obj.getObject();
            String objtag = _obj.getTag();
            final Object tmp_obj = objcache;
            final String tmp_objtag = objtag;
            AndroidActor.AndroidView view
                    = new AndroidActor.AndroidView(getOwnActivity()) {
                @Override
                public boolean view_user(Canvas canvas, IPoint sp, IPoint iPoint,
                                         int alpha) {
                    ShowInstance.showInstance(canvas, tmp_obj, sp, paint2, paint2, tmp_objtag);
                    return true;
                }

            };
            view.setColorCode(ColorLib.ColorCode.RED)
                    .setPercent(new WorldPoint(200f, 200f))/*.setLogLevel(true)*/;
            view._get().setOffset(this);
            view.once();
            new ActorManager(getRootChain()).add(view)
                    ._in()
                    .add(new PathMover(this, 0.1f, 10).once()/*.setLogLevel(true)*/)
                    .add(new Actor.Counter(10 - 1).once()/*.setLogLevel(true)*/)
                    .nextEvent(new Effector.Reset(false).once())
                    .save();

        }
        sampling_count++;
        if(sampling_count >= sampling_rate)
            sampling_count = 0;
        return 25;
    }

    @Override
    public void onSelected(ITapChain tapChain, IPoint pos) {
        IPath path = getMyPath();
        new TapManager(tapChain).disconnect(path);
    }
}
