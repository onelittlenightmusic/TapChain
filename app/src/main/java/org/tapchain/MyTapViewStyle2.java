package org.tapchain;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.Log;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Effector;
import org.tapchain.core.IPath;
import org.tapchain.core.Piece;
import org.tapchain.editor.ColorLib;
import org.tapchain.editor.ColorLib.ColorCode;
import org.tapchain.AndroidTapChain.StateLog;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorInputException;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ActorPullException;
import org.tapchain.core.ChainException;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.ClassLib;
import org.tapchain.core.ClassLib.ClassLibReturn;
import org.tapchain.core.CodingLib;
import org.tapchain.core.IActorBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.ICommit;
import org.tapchain.core.IConnectHandler;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.ILockedScroll;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.IScrollable;
import org.tapchain.core.ISelectable;
import org.tapchain.core.IState;
import org.tapchain.core.IStep;
import org.tapchain.core.IValue;
import org.tapchain.core.IValueArray;
import org.tapchain.core.LinkBooleanSet;
import org.tapchain.core.LinkType;
import org.tapchain.core.Packet;
import org.tapchain.core.Value;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.ITapChain;
import org.tapchain.editor.IPathTapView;
import org.tapchain.editor.ITapView;
import org.tapchain.editor.TapChain;
import org.tapchain.realworld.R;
import org.tapchain.viewlib.DrawLib;
import org.tapchain.viewlib.ShowInstance;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MyTapViewStyle2 extends ActorTapView implements Serializable, IScrollable,
        ISelectable, IRelease, ILockedScroll, IBlueprintFocusNotification,
        IConnectHandler<IActorTapView, IPathTapView>, IErrorHandler<Actor> {

    /**
     *
     */
    private final TapChain tapChain;
    Paint _paint = new Paint(),
            textPaint = new Paint(),
            miniTextPaint = new Paint();
    float sizeDefaultX = 50f;
    public Bitmap bm_fg = null, bm_face = null, bm_fg_mini = null;
    WorldPoint sizeOpened = new WorldPoint(200f, 200f),
            sizeClosed = new WorldPoint(sizeDefaultX, sizeDefaultX),
            sizeDefault = new WorldPoint(2 * sizeDefaultX, 2 * sizeDefaultX);
//    ShapeDrawable d, dOut;
    RectF tickCircleRect = new RectF();
    int tickCircleRadius = 30;
    float sweep = 0f;
    Bitmap bm_bg;
    ConcurrentLinkedQueue<StateLog> stateList = new ConcurrentLinkedQueue<StateLog>();
    float textSize = 60f, miniTextSize = 10f;

    // 1.Initialization
    public MyTapViewStyle2(TapChain tapChain, Activity activity) {
        this(tapChain, activity, null);
    }

    public MyTapViewStyle2(TapChain tapChain, Activity activity, Integer fg) {
        super(activity);
        this.tapChain = tapChain;
        myview_init(fg);

        _paint.setStyle(Paint.Style.STROKE);
        _paint.setAntiAlias(true);
        _paint.setAlpha(120);
        _paint.setColor(0xffffffff);
        _paint.setStrokeWidth(3f);
        _paint.setTextAlign(Align.CENTER);
        _paint.setFilterBitmap(true);

        textPaint.setAntiAlias(true);
        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setFilterBitmap(true);
        miniTextPaint.setAntiAlias(true);
        miniTextPaint.setColor(0xffffffff);
        miniTextPaint.setTextSize(miniTextSize);
        miniTextPaint.setTextAlign(Align.CENTER);
        miniTextPaint.setFilterBitmap(true);
        setPercent(new WorldPoint(0f, 0f));
        setAlpha(128);

        tickCircleRect.set(-tickCircleRadius, -tickCircleRadius,
                tickCircleRadius, tickCircleRadius);

    }

    boolean inited = false;

    //	@Override
    public void initBackground() {
        inited = true;
        if (getActor() == null)
            return;
        if (getActor().getBlueprint() == null)
            return;
        IActorBlueprint b = getActor().getBlueprint();
        Class<? extends IPiece> blueprintClass = b.getBlueprintClass();
        if (IValue.class.isAssignableFrom(blueprintClass)) {
            ClassLibReturn classReturn = ClassLib.getParameterizedType(blueprintClass, IValue.class);
            if (classReturn == null)
                return;
            ClassEnvelope second = classReturn.searchByName("T");
            if (second == null)
                return;
        }

    }

    public void setBackground(Bitmap background) {
        bm_bg = background;
        if (bm_bg != null)
            sizeClosed = new WorldPoint(bm_bg.getWidth() / 2,
                    bm_bg.getHeight() / 2);
    }

    public boolean myview_init(Integer fg) {
        if (fg != null) {
            bm_fg = BitmapMaker.makeOrReuse("Large" + fg.toString(), fg, 250, 250);
            bm_fg_mini = BitmapMaker.makeOrReuse("Mini" + fg.toString(), fg, 70, 70);
        }
        return true;
    }


    @Override
    public void view_init() throws ChainException {
        bm_face = BitmapMaker.makeOrReuse("MyTapFace", R.drawable.face);
        return;
    }

    RectF rect = new RectF();

    @Override
    public boolean view_user(Canvas canvas, IPoint cp, IPoint size, int alpha) {
        if (!inited)
            initBackground();

        float r = 50f;
        _paint.setAlpha(alpha);
        canvas.save();
        canvas.translate(cp.x(), cp.y());
        canvas.translate(-r, -r);
        int sizex = (int) size.x(), sizey = (int) size.y();
        rect.set(0f, 0f, sizex, sizey);
        canvas.drawRoundRect(rect, r, r, _paint);
        canvas.translate(r, r);


        if (getActor() == null) {
            DrawLib.drawBitmapCenter(canvas, bm_fg_mini, WorldPoint.zero(),
                    _paint);
            canvas.restore();
            return true;
        }

        // Draw Extensions _in association with IValue interface
        float theta = getOffsetVectorRawCopy(getActor().getLinkClassFromLib(LinkType.PUSH)).theta();
        DrawLib.drawBitmapCenter(canvas, bm_fg, theta, WorldPoint.zero(),
                _paint);
        canvas.restore();
        Actor actor = getActor();
        if (actor instanceof IValueArray) {
            ShowInstance.showPath(canvas, (IValueArray<IPoint>) actor);
        } else if (actor instanceof IValue) {
            Object val = ((IValue<?>) actor)._get();
            String tag = "";
            if (actor instanceof Controllable)
                tag = ((Controllable) actor).getNowTag();
            if (val != null)
                ShowInstance.showInstance(canvas, val, cp, textPaint, _paint, tag);
        }
        return true;
    }


    @Override
    public MyTapViewStyle2 setPercent(IPoint wp) {
        super.setPercent(wp);
        setSize(sizeOpened.multiplyNew(0.01f * getPercent().x())
                .plus(sizeClosed).plus(sizeClosed));
        return this;
    }

    @Override
    public MyTapViewStyle2 setColor(int _color) {
        return this;
    }

    // 3.Changing state

    @Override
    public int onTick(Actor p, Packet obj) {
        sweep += 20f;
        return 1;
    }

    @Override
    public void changeState(IState state) {
        stateList.add(new StateLog(state, sweep));
        if (state.hasError())
            bm_face = BitmapMaker.makeOrReuse("MyTapFaceError",
                    R.drawable.facesad);
        else
            bm_face = BitmapMaker.makeOrReuse("MyTapFace", R.drawable.face);
        while (stateList.peek().getSweepLog() < sweep - 360) {
            stateList.poll();
        }
        sweep += 20f;
    }

    @Override
    public boolean setMyActorValue(Object obj) {
        Actor actor = getActor();
        if (actor instanceof IValueArray) {
            ((IValueArray) actor)._set(obj);
        } else if (actor instanceof IValue) {
            IValue v = (IValue) actor;
            Object val = v._get();
            if (val.getClass().isAssignableFrom(obj.getClass())) {
                v._set(obj);
                invalidate();
            }
        }
        return false;
    }

    @Override
    public void commitMyActorValue() {
        Actor actor = getActor();
        if (!(actor instanceof ICommit)) {
            return;
        }
        Object commit = ((ICommit) actor)._commit();
        if (commit == null)
            return;
        try {
            new ActorManager(getRootChain())
                    .add(new AndroidActor.AndroidTTS(getOwnActivity(), CodingLib.talk(actor.getTag(), commit)))
                    .save();
        } catch (ChainException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setGridSize(IPoint gs) {
        IPoint now_size = sizeDefault.scalerNew(getGridSize());
        super.setGridSize(gs);
        WorldPoint final_size = sizeDefault.scalerNew(getGridSize());
        Effector.NewSizer size = new Effector.NewSizer(final_size.subNew(now_size)
                .multiplyNew(0.25f).setDif(), 4);
        new ActorManager(getRootChain())._move(this)._in().add(size.once())
                ._out().save();
        return true;
    }

    @Override
    public boolean onScrolled(TapChain edit, IPoint pos, IPoint vp) {
        setCenter(pos);
        return false;
    }

    @Override
    public AndroidView setColorCode(ColorCode colorCode) {
        ColorMatrixColorFilter cMatrix = AndroidColorCode.getColorMatrix(colorCode);
//        pathInnerPaint.setColorFilter(cMatrix);
//        pathInnerPaint2.setColorFilter(cMatrix);
        _paint.setColorFilter(cMatrix);
        return super.setColorCode(colorCode);
    }


    @Override
    public void onFocus(LinkBooleanSet booleanSet) {
        if (booleanSet == null || booleanSet.isEmpty()) {
            setColorCode(ColorCode.CLEAR);
//            addLog("%s unfocused", getTag());
        } else for (LinkType ac : booleanSet) {
            setColorCode(ColorLib.getLinkColor(ac));
//            addLog("%s(%s) focused", getTag(), ac.toString());
        }
    }

    @Override
    public void onSelected(ITapChain tapChain, IPoint pos) {
        Actor actor = getActor();
        if (actor instanceof IStep) {
            ((IStep) actor).onStep();
//            Log.w("test", "onStep called");
        } else if (actor instanceof IValueArray) {
            EditInstance e = new EditInstance(getOwnActivity(), this, actor);
            e.registerToManager(getRootChain(), tapChain);
        } else if (actor instanceof IValue) {
            Object val = ((IValue) actor)._get();
            EditInstance e = new EditInstance(getOwnActivity(), this, val);
            e.registerToManager(getRootChain(), tapChain);
        }
    }

    @Override
    public void onConnect(IActorTapView iActorTap, IPathTapView iPathTap, IActorTapView iActorTap2, LinkType linkType) {
        setOffsetVector();
        LinkType lt = (iActorTap == this) ? linkType : linkType.reverse();
        new ActorManager(getRootChain()).remove((Actor) getAccessoryTap(lt));
        unsetAccessoryTap(lt);
    }


    public static void log(String format, String... l) {
//		Log.w("test", String.format(format, l));
    }

    @Override
    public boolean onRelease(ITapChain edit, IPoint pos) {
        edit.checkAndConnect(this);
        return true;
    }


    @Override
    public boolean onPush(Actor t, Object obj) {
        super.onPush(t, obj);
        setPushOutBalloon(this, LinkType.PUSH, obj);
        return true;
    }

    void setPushOutBalloon(IActorTapView t, LinkType linkType, Object obj) {
        if (t.getActor().isConnectedTo(linkType)) {
            return;
        }
        IActorTapView accessoryTap = t.getAccessoryTap(linkType);
        if (accessoryTap != null) {
            accessoryTap.setMyActorValue(obj);
            return;
        }

        ClassEnvelope classEnvelope = new ClassEnvelope(obj.getClass());
        IActorTapView balloon = BalloonTapViewStyle.createBalloon(t, linkType, classEnvelope);
        new ActorManager(getRootChain()).add((Actor) balloon).save();
        t.setAccessoryTap(linkType, balloon);
    }


    @Override
    public boolean onLockedScroll(ITapChain edit, ITapView selectedTap, IPoint wp) {
        setParentSize(this, wp);
        return false;
    }

    public void setParentSize(IActorTapView _p, IPoint pos) {
        IPoint p = _p.getGridSize();
        IPoint p2 = pos.subNew(_p.getCenter())
                .plus(new WorldPoint(50f, 50f)).multiply(0.01f).round(1)
                .plus(new WorldPoint(1f, 1f));
        boolean eq = (int) p.x() == (int) p2.x() && ((int) p.y() == (int) p2.y());
        IPoint min = _p.getMinGridSize();
        if (!eq && p2.x() >= min.x() && p2.y() >= min.y()) {
            _p.setGridSize(p2);
        }

    }

    boolean isZero = true;

    public void setOffsetVector() {
//        Class<?> cls = Object.class;
        for(OffsetVector vector: partnersOffsetAverage.values()) {
            vector.counter = 0;
            vector.average.clear();
        }
        HashMap<Actor, IPath> map = getActor().getPartnerList();
            for (Map.Entry<Actor, IPath> kv : map.entrySet()) {
                Actor vec1 = kv.getKey();
                int push = kv.getValue().isOut(vec1) ? -1 : 1;
                OffsetVector vector = getOffsetVector(kv.getValue().getConnectionClass());
                vector.average.setOffset(tapChain.toTap(vec1), push * 0.5f);
                vector.counter += push;
            }
        for(OffsetVector vector: partnersOffsetAverage.values()) {
            vector.average.setOffset(this, - vector.counter * 0.5f);
            vector.set(true);
        }
//        }
    }

    private HashMap<ClassEnvelope, OffsetVector> partnersOffsetAverage = new HashMap<>();

//    ClassEnvelope simpleCE= new ClassEnvelope(Object.class);
    public WorldPoint getOffsetVector(ClassEnvelope cls, float alpha) {
        WorldPoint offsetVector = new WorldPoint(WorldPoint.zero());
        offsetVector.setOffset(this);
        offsetVector.setOffset(getOffsetVector(cls).offset, alpha);
        return offsetVector;
    }

    public OffsetVector getOffsetVector(ClassEnvelope cls) {
        if(cls == null) {
            Log.w("test", String.format("Class(null)<=%s", getTag()));
            new Throwable().printStackTrace();
            return null;
        } else
            Log.w("test", String.format("Class(%s)", cls.toString()));
        if(!partnersOffsetAverage.containsKey(cls))
            partnersOffsetAverage.put(cls, new OffsetVector());
        return partnersOffsetAverage.get(cls);
    }

    class OffsetVector {
        Value<IPoint> offset;
        WorldPoint average;
        int counter;
        public OffsetVector() {
            this(new WorldPoint(10f, 0f));
        }
        public OffsetVector(WorldPoint ave) {
            average = ave;
            offset = new Value<>(new WorldPoint());
        }
        public void set(boolean averageOn) {
            offset._set(averageOn ? average : new WorldPoint());
        }
    }

    public WorldPoint getOffsetVectorRawCopy(ClassEnvelope clz) {
        if (getActor().getPartners(LinkType.PULL).size() == 0) {
            if (getActor().getPartners(LinkType.PUSH).size() == 0) {
                return new WorldPoint(150f, 0f);
            }
        }
        OffsetVector vector = getOffsetVector(clz);
        if(vector == null)
            return new WorldPoint(150f, 0f);
        WorldPoint rtn = new WorldPoint(vector.average);
        return rtn.multiply(150f / rtn.getAbs());
    }

    @Override
    public ChainPiece onError(Actor actor, ChainException e) {
        if (!(e instanceof ActorInputException))
            return actor;
        if (e instanceof ActorPullException)
            onPullLocked(this, (ActorPullException) e);
        return actor;
    }

    @Override
    public ChainPiece onUnerror(Actor actor, ChainException e) {
        if (!(e instanceof ActorInputException))
            return actor;
        if (e instanceof ActorPullException) {
            onPullUnlocked(this, (ActorPullException) e);
        }
        return actor;
    }

    public void onPullLocked(IActorTapView t, ActorPullException actorPullException) {
        AndroidActor.AndroidImageView errorMark = new AndroidActor.AndroidImageView(getOwnActivity(), R.drawable.error);
        errorMark.setColorCode(ColorCode.RED)
                .setPercent(new WorldPoint(200f, 200f));
        errorMark._get().setOffset(t);
        ActorManager manager = new ActorManager(getRootChain());
        manager.add(errorMark
                )
                ._in()
                .add(new Effector.Reset(false)/*.setLogLevel(true)*/)
                .prevEvent(new Effector.Sleep(2000)/*.setLogLevel(true)*/)
                .save();

        //Create error balloon
        LinkType linkType = actorPullException.getLinkType();
        ClassEnvelope classEnvelopeInLink = actorPullException.getClassEnvelopeInLink();
        IActorTapView balloon = BalloonTapViewStyle.createBalloon(t, linkType, classEnvelopeInLink);
        manager.add((Actor) balloon).save();
        t.setAccessoryTap(linkType, balloon);
        tapChain.highlightLinkables(linkType, t, classEnvelopeInLink);
    }

    public void onPullUnlocked(IActorTapView t, ActorPullException actorPullException) {
        LinkType linkType = actorPullException.getLinkType();
        if (linkType != null) {
            new ActorManager(getRootChain()).remove((Actor) t.getAccessoryTap(linkType));
            t.unsetAccessoryTap(linkType);
            tapChain.unhighlightAllLinkables();
        }
    }
}