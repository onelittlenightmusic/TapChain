package org.tapchain;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;

import org.tapchain.AndroidActor.AndroidImageMovable;
import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.QueueingTheory.Processor;
import org.tapchain.core.Actor;
import org.tapchain.core.Actor.Exp;
import org.tapchain.core.Actor.IntegerGenerator;
import org.tapchain.core.Actor.WordGenerator;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.ActorManager;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.D2Point;
import org.tapchain.core.IActionStyle;
import org.tapchain.core.IActorCollideHandler;
import org.tapchain.core.ICollidable;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.core.StyleCollection;
import org.tapchain.core.ViewActor;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.Geometry;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IWindow;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.game.CarEngineer;
import org.tapchain.game.ElectricityFactory;
import org.tapchain.game.Motor;
import org.tapchain.realworld.R;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("serial")
public class TapChainAndroidEditor extends TapChainEditor {

    // 1.Initialization
    final Actor v2 = new Actor();
    TapChainGoalTap goal;
    boolean magnet = true;
    Actor touch = null;
    Activity act = null;

    public TapChainAndroidEditor(IWindow w, Resources r, Activity activity) {
        super(w);

        act = activity;
        BitmapMaker.setActivity(act);
        // Setting styles
        ActorEventHandler aeh = new ActorEventHandler(this, r, activity);
        setStyle(new StyleCollection(this, BubbleTapStyle.class,
                BubblePathTap.class, new AndroidInteractionStyle(),
                aeh, aeh));
        // setInteract(new LocalInteraction());
        geos.add(new Geometry(
                new WorldPoint(100f, 0f), new WorldPoint(200f, 0f), new WorldPoint(300f, 0f),
                new WorldPoint(100f, 100f), new WorldPoint(200f, 100f), new WorldPoint(300f, 100f),
                new WorldPoint(100f, 200f), new WorldPoint(200f, 200f), new WorldPoint(300f, 200f),
                new WorldPoint(100f, 300f), new WorldPoint(200f, 300f), new WorldPoint(300f, 300f),
                new WorldPoint(100f, 400f), new WorldPoint(200f, 400f), new WorldPoint(300f, 400f)));
        // Making list of pieces
        getBlueprintManager()
                .add(AndroidActor.AndroidImageView.class, act, R.drawable.star1)
                .setViewArg(R.drawable.star2)
                .setTag("Star")
                .save()

                .add(AndroidActor.AndroidImageView.class, act, R.drawable.heart_bright)
                .setViewArg(R.drawable.heart2)
                .setTag("Heart")
                .save()

                .add(AndroidImageMovable.class, act, R.drawable.carframe, R.drawable.carframe)
                .setViewArg(R.drawable.carframe)
                .setTag("Car Frame")
                .save()

                .add(AndroidImageMovable.class, act, R.drawable.ray_face, R.drawable.moving)
                .setViewArg(R.drawable.ray_face)
                .setTag("Ray")
                .save()
//
//				.addFocusable(Actor.ScrollableAdjuster.class).setViewArg(R.drawable.flag).setTag("Adjuster")

//				.addFocusable(Actor.Booster.class).setViewArg(R.drawable.boost1).setTag("Booster")
//				.addFocusable(Actor.Charger.class).setViewArg(R.drawable.battery1).setTag("Charger")

                .add(Actor.Mover.class, new WorldPoint(1f, 0f))
                .setViewArg(R.drawable.right2)
                .setTag("Mover")
//                .setLogLevel()
                .save()



                .add(Actor.ArrayJumper.class, (Object) new WorldPoint[]{})
                .setViewArg(R.drawable.up2)
                .setTag("Array Jumper")
//                .setLogLevel()
                .save()

                .add(Actor.ArrayMover.class, (Object) new D2Point[]{})
                .setViewArg(R.drawable.down2)
                .setTag("Array Mover")
                .save()

                .add(WordGenerator.class, "A", false)
                .setViewArg(R.drawable.a)
                .setTag("Word")
                .save()

                .add(new Actor.IFunc<String, String, String>() {
                    @Override
                    public String func(IValue<String> val, String _in) throws ChainException {
                        return _in + " " + val._valueGet();
                    }

                    @Override
                    public void init(IValue<String> val) {
                        val._valueSet("");
                    }
                }).setViewArg(R.drawable.plus2).setTag("Plus Word")/*.setLogLevel()*/.save()


                .add(new Actor.IFunc<Integer, String, String>() {
                    @Override
                    public String func(IValue<Integer> val, String _in) throws ChainException {
                        StringBuilder stringBuilder = new StringBuilder();
                        int value = val._valueGet();
                        for (int i = 0; i < value; i++) {
                            stringBuilder.append(_in);
                            stringBuilder.append(" ");
                        }
                        return stringBuilder.toString();
                    }

                    @Override
                    public void init(IValue<Integer> val) {
                        val._valueSet(2);
                    }
                }).setViewArg(R.drawable.multi2).setTag("Multi Word")/*.setLogLevel()*/.save()

                .add(new Actor.IFunc<String, String, String>() {
                    @Override
                    public String func(IValue<String> val, String _in) throws ChainException {
                        val._valueSet(val._valueGet()+_in);
                        return val._valueGet();
                    }

                    @Override
                    public void init(IValue<String> val) {
                        val._valueSet("");
                    }
                }).setViewArg(R.drawable.plus2).setTag("Cumulate Word")/*.setLogLevel()*/.save()

                .add(AndroidActor.AndroidMail.class, act, "mailto:heretic55@docomo.ne.jp")
                .setViewArg(R.drawable.mail2)
                .setTag("Mail to Mari")
//                .setLogLevel()
                .save()

//				.addFocusable(Actor.Sizer.class)
//				.arg(new WorldPoint(1, 1), 3)
//				.setViewArg(R.drawable.widen2)
//				.setTag("Sizer")
//
//				.addFocusable(Actor.Accelerator.class)
//				.arg(new WorldPoint(100, 100))
//				.setViewArg(R.drawable.pin)
//				.setTag("Accel")

                .add(AndroidActor.AndroidRecognizer.class, act)
                .setViewArg(R.drawable.mic)
                .setTag("Recognizer").setLogLevel()
                .save()

//				.addFocusable(AndroidActor.AndroidCamera.class)
//				.setViewArg(R.drawable.pic)
//				.setTag("Camera")


                .add(IntegerGenerator.class, 1, false)
                .setViewArg(R.drawable.num)
                .setTag("Number")
                .save()

//                .add(Actor.PlusIntegerFilter.class).setViewArg(R.drawable.plus2).setTag("Plus").save()
//                .add(Actor.MultiIntegerFilter.class).setViewArg(R.drawable.multi2).setTag("Multiply").save()

                .add(new Actor.IFunc<Integer, Integer, Integer>() {
                    @Override
                    public Integer func(IValue<Integer> val, Integer _in) throws ChainException {
                        return _in + val._valueGet();
                    }

                    @Override
                    public void init(IValue<Integer> val) {
                        val._valueSet(1);
                    }
                }).setViewArg(R.drawable.plus2).setTag("Plus")/*.setLogLevel()*/.save()

                .add(new Actor.IFunc<Integer, Integer, Integer>() {
                    @Override
                    public Integer func(IValue<Integer> val, Integer _in) throws ChainException {
                        return _in - val._valueGet();
                    }

                    @Override
                    public void init(IValue<Integer> val) {
                        val._valueSet(1);
                    }
                }).setViewArg(R.drawable.minus2).setTag("Minus")/*.setLogLevel()*/.save()

                .add(new Actor.IFunc<Integer, Integer, Integer>() {
                    @Override
                    public Integer func(IValue<Integer> val, Integer _in) throws ChainException {
                        return _in * val._valueGet();
                    }

                    @Override
                    public void init(IValue<Integer> val) {
                        val._valueSet(2);
                    }
                }).setViewArg(R.drawable.multi2).setTag("Multiply")/*.setLogLevel()*/.save()
//				._out()

                .add(Actor.IntegerCounter.class)
                .setViewArg(R.drawable.rotate).setTag("Counter").save()

//				.addFocusable(FloatValue.class)
//				.arg(1f, false)
//				.setViewArg(R.drawable.f123)
//				.setTag("Decimal")
//
                .add(Exp.class, 1f, 10000)
                .setViewArg(R.drawable.walk)
                .setTag("Random Walk")
                .save()

//				.addFocusable(ValueLimited.class)
//				.arg(20, new Float(1f))
//				.setViewArg(R.drawable.num)
//				.setTag("Limited")

                .add(Processor.class).setViewArg(R.drawable.sit).setTag("Random Sit").save()
                .add(Actor.ValueLogPrinter.class).setViewArg(R.drawable.config).setTag("Log").save()

                .add(Actor.Time.class)
                .setViewArg(R.drawable.clock).setTag("Time").save()

                .add(Actor.Append.class)
                .setViewArg(R.drawable.draw).setTag("Writing").save()

                .add(Actor.Show.class)
                .setViewArg(R.drawable.draw).setTag("Show").save()

//				.addFocusable(Actor.LogEnabler.class)
//				.setViewArg(R.drawable.config).setTag("Log")
//
                .add(CarEngineer.AccelPedal.class, 1f, true)
                .setViewArg(R.drawable.pedal).setTag("Accel Pedal").save()

                .add(CarEngineer.Engine2.class)
                .setViewArg(R.drawable.engine).setTag("Engine").save()

                .add(CarEngineer.Tire.class)
                .setViewArg(R.drawable.wheel).setTag("Tire").save()

                .add(CarEngineer.BrakePedal.class, 1f, true)
                .setViewArg(R.drawable.pedal).setTag("Brake Pedal").save()

                .add(CarEngineer.Brake.class)
                .setViewArg(R.drawable.brake).setTag("Brake").save()


                .add(CarEngineer.RepeatRoad.class).setViewArg(R.drawable.roadback).setTag("Road").save()
                .add(ElectricityFactory.class).setViewArg(R.drawable.electricity).setTag("Electricity").save()
                .add(Motor.class).setViewArg(R.drawable.motor).setTag("Small motor").save()
                .add(Motor.class).setViewArg(R.drawable.motor2).setTag("Large motor").save()
                .add(Motor.MotorPedal.class).setViewArg(R.drawable.pedal).setTag("Motor Pedal").save()
        ;

        try {
            getBlueprintManager()
                    .add((Class<? extends Actor>)Class.forName("org.tapchain.core.Actor$WordGenerator"), "A", false)
                    .setViewArg(R.drawable.a)
                    .setTag("Word")
    //                .setLogLevel()
                    .save();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

//        goalBlueprintManager.add(TapChainGoalView.class).setSystem(
//                TapChainGoalTap.class).save();


        editTap()
                .add(touch = new AndroidView() {
                    Paint paint_ = new Paint();

                    public void view_init() throws ChainException {
                        IPoint p = (WorldPoint) pullInActor();
                        setCenter(p);
                        Collection<IActorTap> t = searchRoomPieces(p);
                        int c = Color.BLACK;
                        if (t != null && !t.isEmpty()) {
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
                    public boolean actorRun(Actor act) throws ChainException {
                        getTarget().addSize(new WorldPoint(30f, 0f));
                        getTarget().setAlpha(
                                getTarget().getAlpha() - 10);
                        invalidate();
                        return increment();
                    }
                }.initEffectValue(1, 10))
                .next(new Actor.Reset().setContinue(true))._out()
                .save();
        freezeToggle();
//        WorldPoint wp = new WorldPoint((int) (400 * Math.random()), (int) (400 * Math
//                .random()));
//        Actor sp = onAdd(
//                getGoal(),
//                0,
//                wp).getKey();
//        AndroidActor.AndroidImageView a = new AndroidActor.AndroidImageView(act, R.drawable.congratulation);
//        a.setSize(new WorldPoint(500f, 100f));
//        a.setCenter(wp);
//        editTap()
//                ._move(sp)
//                .next(new AndroidActor.AndroidImageView(act, R.drawable.star1).setCenter(wp))
//                ._in()
//                .add(new Actor.NewSizer(new WorldPoint(30f, 30f), 10))
//                .next(new Actor.Reset(false))
//                ._out()
//                ._move(sp)
//                .next(a)
//                ._move(sp)
//                .next(new AndroidActor.AndroidSound2(act, R.raw.people093))
//                ._out()
//                .save();
        freezeToggle();
    }

    public class TapChainGoalView extends AndroidActor.AndroidImageView implements ICollidable<Actor> {
        volatile boolean achieved = false;
        Collection<IActorCollideHandler> handlers = new ConcurrentLinkedQueue<IActorCollideHandler>();
        CollidableHandler h;

        public TapChainGoalView() {
            super(TapChainAndroidEditor.this.act, R.drawable.star);
            setCenter(new WorldPoint((float) 400f * (float) Math.random(),
                    400f * (float) Math.random()));
            h = new CollidableHandler();
            handlers.add(h);
        }

        @Override
        public void onCollideInternal(IEditor edit, IView v1, Collection<Actor> v2,
                                      IPoint pos) {
            if (achieved)
                return;
            for (IActorCollideHandler h : handlers)
                if (h.onCollide(TapChainAndroidEditor.this, v1, v2, pos))
                    achieved = true;
        }

        public void onCollide(IEditor edit, IView v1, Collection<Actor> v2, IPoint pos) {
            Collection<Actor> pieces;
            if (v2 == null)
                pieces = manager.getActors();
            else {
                pieces = v2;
            }
            for (IPiece o : pieces)
                if (o instanceof IView)
                    if (o != v1)
                        if (getInteract().checkTouchType((IView) o, v1, false) != InteractionType.NONE) {
                            ((Controllable) v1).interruptEnd();
                            achieved = true;
                        }
        }

        @Override
        public void onAdd(ActorManager maker) {
            super.onAdd(maker);
            maker.add(h);
        }


    }

    // 2.Getters and setters

    @Override
    public void log(String... s) {
        switch (s.length) {
            case 0:
                break;
            case 1:
                Log.w(s[0], "");
                break;
            case 2:
            default:
                Log.w(s[0], s[1]);
                break;
        }
        getWindow().log(s);
        return;
    }

    public boolean magnetToggle() {
        magnet = !magnet;
        return magnet;
    }


    // 3.Changing state
    @Override
    public boolean onDown(IPoint iPoint) {
        boolean rtn = super.onDown(iPoint);
        ((Actor) touch).offer(iPoint);
        return rtn;
    }

    @Override
    public void showFamily(IActorTap tap) {
        tap.getCenter().plus(10f * (float) Math.random() - 5f, 10f * (float) Math.random() - 5f);
    }

    public class RoadTapStyle extends MyTapStyle2 {
        public RoadTapStyle() {
            super(TapChainAndroidEditor.this, TapChainAndroidEditor.this.getEventHandler(), TapChainAndroidEditor.this.act);
            // set background image bitmap
            setBackground(road_init());
        }

        public RoadTapStyle(Integer bm) {
            // set foreground image resource
            super(TapChainAndroidEditor.this, TapChainAndroidEditor.this.getEventHandler(), TapChainAndroidEditor.this.act, bm);
            // set background image bitmap
            setBackground(road_init());
        }

        public Bitmap road_init() {
            return BitmapMaker.makeOrReuse(getName(), R.drawable.roadback, 100,
                    100);
        }
    }

    public class BubbleTapStyle extends MyTapStyle2 {
        public BubbleTapStyle() {
            super(TapChainAndroidEditor.this, TapChainAndroidEditor.this.getEventHandler(), TapChainAndroidEditor.this.act);
            setBackground(bubble_init());
        }

        public BubbleTapStyle(Integer bm) {
            super(TapChainAndroidEditor.this, TapChainAndroidEditor.this.getEventHandler(), TapChainAndroidEditor.this.act, bm);
            setBackground(bubble_init());
        }

        public Bitmap bubble_init() {
            return null;// bitmapmaker.makeOrReuse(getName(),
        }
    }

    public class AndroidInteractionStyle extends Actor.Relation implements
            IActionStyle {
        CountDownLatch c = new CountDownLatch(1);
        ViewActor a, b;
        ViewActor mark;

        AndroidInteractionStyle() {
            super();
            setLinkClass(LinkType.PUSH, IPoint.class);
        }

        @Override
        public boolean relation_impl(ViewActor _a, ViewActor _b)
                throws InterruptedException {
            c.await();
            c = new CountDownLatch(1);
            pushInActor(a.getCenter().multiplyNew(0.5f)
                    .plusNew(b.getCenter().multiplyNew(0.5f)));
            return true;// checkTouch(a, b);
        }

        @Override
        public IPoint pointOnAdd(IPoint raw) {
            if (magnet)
                return raw.copy().plus(50f, 50f).round(100).unsetDif();
            else
                return raw.unsetDif();
        }

        @Override
        public InteractionType checkTouchType(IView f1, IView f2,
                                              boolean onlyInclude) {
            Rect rect1 = ((AndroidActor.AndroidView) f1).getScreenRect();
            rect1.inset(-10, -10);
            Rect intersect = new Rect();
            boolean rtn = intersect.setIntersect(rect1,
                    ((AndroidView) f2).getScreenRect());
            if (!rtn) {
                return InteractionType.NONE;
            }
            a = (ViewActor) f1;
            b = (ViewActor) f2;
            if (checkGetIncluded(f1, f2)) {
                // when containing and not connected
                c.countDown();
                return InteractionType.INSIDE;
            }
            if (checkCrossing(intersect))
                return InteractionType.CROSSING;
            if (onlyInclude)
                return InteractionType.NONE;
            c.countDown();
            if (intersect.width() > intersect.height()) {
                if (intersect.bottom == ((AndroidView) f2).getScreenRect().bottom) {
                    return InteractionType.TOUCH_BOTTOM;
                } else {
                    return InteractionType.TOUCH_TOP;
                }
            } else {
                if (intersect.left == ((AndroidView) f2).getScreenRect().left) {
                    return InteractionType.TOUCH_LEFT;
                } else {
                    return InteractionType.TOUCH_RIGHT;
                }
            }

        }

        private boolean checkCrossing(Rect intersect) {
            return intersect.width() >= 100 && intersect.height() >= 100;
        }

        @Override
        public InteractionType checkLeaveType(IView v1, final IView v2) {
            if (checkLeave(v1, v2)) {
                // Check mark (view created when leaving)
                if (mark != null)
                    // Remove mark
                    editTap().remove(mark);
                // Clear mark
                mark = null;
                // Return state "disconnected"
                return InteractionType.OUTSIDE;
            } else if (!checkGetIncluded(v1, v2)) {
                // Check if Piece v1 is included _in Piece v2.
                // View change _in releasing state
                Rect intersect = new Rect();
                boolean rtn = intersect.setIntersect(
                        ((AndroidActor.AndroidView) v1).getScreenRect(),
                        ((AndroidView) v2).getScreenRect());
                if (rtn && checkCrossing(intersect))
                    return InteractionType.CROSSING;
                // Return state "releasing"
                return InteractionType.GOOUTSIDE;
            }
            // Return no disconnection.
            return InteractionType.NONE;
        }

        void releasing(IView v1, final IView v2) {
            if (mark != null)
                return;
            mark = new Mark(v2);
            editTap().add(mark).save();

        }

        public class Mark extends ActorTap {
            AndroidView v;
            final ShapeDrawable d = new ShapeDrawable(new RoundRectShape(
                    new float[]{150, 150, 150, 150, 150, 150, 150, 150},
                    null, null));

            public Mark(IView v2) {
                v = ((AndroidView) v2);
            }

            @Override
            public void view_init() {
                d.getPaint().setAntiAlias(true);
                d.getPaint().setColor(0xffffffff);
                d.getPaint().setStyle(Paint.Style.STROKE);
                d.getPaint().setStrokeWidth(3);
                d.getPaint().setPathEffect(
                        new DashPathEffect(new float[]{10, 20}, 0));
            }

            @Override
            public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
                                     int alpha) {
                Rect _r = v.getWorldRect();
                _r.inset(-150, -150);
                d.setBounds(_r);
                d.draw(canvas);
                return true;
            }
        }

        boolean checkLeave(IView f1, IView f2) {
            Rect rect1 = ((AndroidView) f2).getScreenRect();
            rect1.inset(-50, -50);
            return !(((AndroidView) f1).getScreenRect().intersect(rect1));
        }

        boolean checkSplit(ViewActor f1, ViewActor f2) {
            float a = f1.getRawSize().x() + f2.getRawSize().x();
            return getDistanceSq(f1.getCenter(), f2.getCenter()) > 4 * a * a;
        }

        boolean checkGetIncluded(IView v1, IView v2) {
            return ((AndroidView) v2).getScreenRect().contains(
                    ((AndroidView) v1).getScreenRect());
        }

        float getDistanceSq(IPoint iPoint, IPoint iPoint2) {
            return (iPoint.x() - iPoint2.x()) * (iPoint.x() - iPoint2.x())
                    + (iPoint.y() - iPoint2.y()) * (iPoint.y() - iPoint2.y());
        }

        public boolean checkReleasing(IView v1, IView v2) {
            return !((AndroidView) v2).getScreenRect().contains(
                    ((AndroidView) v1).getScreenRect());
        }

    }

    public static class StateLog {
        private static int count = 0;
        private int thisCount;
        private IState state;
        private float sweeplog;

        public StateLog(IState state, float sweep) {
            setState(state);
            setSweepLog(sweep);
            thisCount = count++;
        }

        public IState getState() {
            return state;
        }

        public void setState(IState state) {
            this.state = state;
        }

        public float getSweepLog() {
            return sweeplog;
        }

        public void setSweepLog(float sweeplog) {
            this.sweeplog = sweeplog;
        }

        public int getCount() {
            return thisCount;
        }

    }

    @Override
    public boolean onLongPress() {
        boolean rtn = super.onLongPress();
        if (rtn) {
            AndroidActor.AndroidDashRect a = new AndroidActor.AndroidDashRect();
            a.setSize(new WorldPoint(200f, 200f)).setColor(0xffffffff);
            a._valueGet().setOffset(getCapturedTap(), false);
            editTap()
                    .add(a)
                    ._in()
                    .add(new Actor.Sleep(2000))
                    .next(new Actor.Ender())
                    ._out()
                    .save();
        }
        if (getCapturedTap() instanceof IActorTap) {
            Actor a = ((IActorTap) getCapturedTap()).getActor();
            a.setLogLevel(true);
            a.setLogTag("test");
            Log.w("test", String.format("%s's setLogLevel true(lock:%s[%s], state:%s)",
                    a.getTag(), a.getLockStatus() ? "free" : "locked",
                    a.getLockTag(),
                    a.getState()));
            a.printLastExecLog();
        }
        return rtn;
    }

}