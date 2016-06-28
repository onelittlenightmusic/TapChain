package org.tapchain;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;

import org.tapchain.AndroidActor.AndroidImageMovable;
import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Actor;
import org.tapchain.core.ChainException;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.Consumer;
import org.tapchain.core.D2Point;
import org.tapchain.core.Effector;
import org.tapchain.core.Filter;
import org.tapchain.core.Generator;
import org.tapchain.core.IActionStyle;
import org.tapchain.core.IConsumer;
import org.tapchain.core.IEffector;
import org.tapchain.core.IFilter;
import org.tapchain.core.IGenerator;
import org.tapchain.core.IPoint;
import org.tapchain.core.IState;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.core.StyleCollection;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.actors.PassThru;
import org.tapchain.core.actors.PushOut;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.IFocusable;
import org.tapchain.editor.IWindow;
import org.tapchain.editor.TapChain;
import org.tapchain.editor.TapManager;
import org.tapchain.game.CarEngineer;
import org.tapchain.game.ElectricityFactory;
import org.tapchain.game.Motor;
import org.tapchain.realworld.R;
import org.tapchain.realworld.queueing.QueueingTheory.Processor;
import org.tapchain.viewlib.ShowInstance;

import java.util.concurrent.CountDownLatch;

@SuppressWarnings("serial")
public class AndroidTapChain extends TapChain {

    // 1.Initialization
    Activity act = null;

    public AndroidTapChain(IWindow w, Activity activity) {
        super(w);

        act = activity;
        BitmapMaker.setActivity(act);
        // Setting styles
        ActorEventHandler aeh = new ActorEventHandler(this, activity);
        setStyle(new StyleCollection(this, getSystemChain(),
                BubbleTapViewStyle.class,
                BubblePathTapView.class,
                new AndroidInteractionStyle(),
                aeh, aeh));
        // setInteract(new LocalInteraction());
//        geos.add(new Geometry(
//                new WorldPoint(100f, 0f), new WorldPoint(200f, 0f), new WorldPoint(300f, 0f),
//                new WorldPoint(100f, 100f), new WorldPoint(200f, 100f), new WorldPoint(300f, 100f),
//                new WorldPoint(100f, 200f), new WorldPoint(200f, 200f), new WorldPoint(300f, 200f),
//                new WorldPoint(100f, 300f), new WorldPoint(200f, 300f), new WorldPoint(300f, 300f),
//                new WorldPoint(100f, 400f), new WorldPoint(200f, 400f), new WorldPoint(300f, 400f)));
        // Making list of pieces
        editBlueprint()
            .add(AndroidActor.AndroidImageView.class, act, R.drawable.star1)
            .view(R.drawable.star2)
            .tag("Star")
            .save()

            .add(AndroidActor.AndroidImageView.class, act, R.drawable.heart_bright)
            .view(R.drawable.heart2)
            .tag("Heart")
            .save()

            .add(AndroidImageMovable.class, act, R.drawable.carframe,
                    R.drawable.carframe)
            .view(R.drawable.carframe)
            .tag("Car Frame")
            .save()

            .add(AndroidImageMovable.class, act, R.drawable.ray_face, R.drawable.moving)
            .view(R.drawable.ray_face)
            .tag("Ray")
            .save()
//
//				.addFocusable(Actor.ScrollableAdjuster.class).view(R.drawable.flag).tag("Adjuster")

//				.addFocusable(Actor.Booster.class).view(R.drawable.boost1).tag("Booster")
//				.addFocusable(Actor.Charger.class).view(R.drawable.battery1).tag("Charger")

            .add(Effector.Mover.class, new WorldPoint(1f, 0f))
            .view(R.drawable.right2)
            .tag("Mover")
//                .setLogLevel()
            .save()



            .add(Effector.ArrayJumper.class, (Object) new WorldPoint[]{})
            .view(R.drawable.up2)
            .tag("Array Jumper")
//                .setLogLevel()
            .save()

            .add(Effector.ArrayMover.class, (Object) new D2Point[]{})
            .view(R.drawable.down2)
            .tag("Array Mover")
            .save()

            .add(Generator.WordGenerator.class, "A", false)
            .view(R.drawable.a)
            .tag("Word")
            .save()

            .add(AndroidActor.AndroidMail.class, act, "mailto:heretic55@docomo.ne.jp")
            .view(R.drawable.mail2)
            .tag("Mail to Mari")
//                .setLogLevel()
            .save()


//            .add(AndroidActor.AndroidRecognizer.class, act)
//            .view(R.drawable.mic)
//            .tag("Recognizer").setLogLevel()
//            .save()

//				.addFocusable(AndroidActor.AndroidCamera.class)
//				.view(R.drawable.pic)
//				.tag("Camera")


            .add(Generator.IntegerGenerator.class, 1, false)
            .view(R.drawable.num)
            .tag("Number")
            .save()

//                .add(Actor.PlusIntegerFilter.class).view(R.drawable.plus2).tag("Plus").save()
//                .add(Actor.MultiIntegerFilter.class).view(R.drawable.multi2).tag("Multiply").save()

            .add(PushOut.IntegerPushOut.class)
            .view(R.drawable.boost1)
            .tag("PushOut")
            .save()

            .add(PassThru.IntegerPassThru.class)
            .view(R.drawable.right2)
            .tag("PassThru")
            .save()

            .add(Filter.PlusIntegerFilter.class)
            .view(R.drawable.plus2)
            .tag("Plus")/*.setLogLevel()*/
            .save()

            .add(Filter.MultiIntegerFilter.class)
            .view(R.drawable.multi2)
            .tag("Multi")/*.setLogLevel()*/
            .save()

            .add(Filter.SumIntegerFilter.class)
            .view(R.drawable.filter)
            .tag("Accumulate")/*.setLogLevel()*/
            .save()

            .add(Generator.IntegerCounter.class)
            .view(R.drawable.rotate).tag("Counter")/*.setLogLevel()*/.save()

            .add(Filter.SumIntegerFilter.class)
            .view(R.drawable.plus).tag("Sum").save()
//				.addFocusable(FloatValue.class)
//				.arg(1f, false)
//				.view(R.drawable.f123)
//				.tag("Decimal")
//
            .add(Generator.Exp.class, 1f, 10000)
            .view(R.drawable.walk)
            .tag("Random Walk")
            .save()

//				.addFocusable(ValueLimited.class)
//				.arg(20, new Float(1f))
//				.view(R.drawable.num)
//				.tag("Limited")

            .add(Processor.class).view(R.drawable.sit).tag("Random Sit").save()
            .add(Effector.ValueLogPrinter.class).view(R.drawable.config).tag("Log").save()

            .add(Generator.Time.class)
            .view(R.drawable.clock).tag("Time").save()

            .add(Filter.Append.class)
            .view(R.drawable.draw).tag("Writing").save()

            .add(Consumer.Show.class)
            .view(R.drawable.draw).tag("Show").save()

//				.addFocusable(Actor.LogEnabler.class)
//				.view(R.drawable.config).tag("Log")
//
            .add(CarEngineer.AccelPedal.class, 1f, true)
            .view(R.drawable.pedal).tag("Accel Pedal").save()

            .add(CarEngineer.Engine2.class)
            .view(R.drawable.engine).tag("Engine").save()

            .add(CarEngineer.Tire.class)
            .view(R.drawable.wheel).tag("Tire").save()

            .add(CarEngineer.BrakePedal.class, 1f, true)
            .view(R.drawable.pedal).tag("Brake Pedal").save()

            .add(CarEngineer.Brake.class)
            .view(R.drawable.brake).tag("Brake").save()


            .add(CarEngineer.RepeatRoad.class).view(R.drawable.roadback).tag("Road").save()
            .add(ElectricityFactory.class).view(R.drawable.electricity).tag("Electricity").save()
            .add(Motor.class).view(R.drawable.motor).tag("Small motor").save()
            .add(Motor.class).view(R.drawable.motor2).tag("Large motor").save()
            .add(Motor.MotorPedal.class).view(R.drawable.pedal).tag("Motor Pedal").save()
//            .add(self->self._get(), 0)
//            .view(R.drawable.motor).tag("Generator").save()
//            .add((IValue<Integer> self, Integer i) -> i + self._get(), 1)
//            .view(R.drawable.motor).tag("Filter").save()
//            .add((IValue<Integer> self, Integer i) -> { self._set(i); Log.w("test", String.format("OK %d", i)); }, 0)
//            .view(R.drawable.motor).tag("Consumer").save()
//            .add((IValue<Integer> self, IValue<Integer> p) -> p._set(self._get()+p._get()), 3, 1)
//            .view(R.drawable.motor).tag("Effector").save()
                .add(new IGenerator<ElectricPower, ElectricPower>() {
                    @Override
                    public ElectricPower generate(IValue<ElectricPower> self1) {
                        return ElectricPower.GENERATE(self1);
                    }
                }, new ElectricPower(3f))
                .view(R.drawable.electricity)
                .tag("Power Generator")
                .save()
                .add(new IFilter<ElectricPower, ElectricPower, ElectricPower>() {
                    @Override
                    public ElectricPower func(IValue<ElectricPower> self1, ElectricPower i1) {
                        return ElectricPower.FILTER_PLUS(self1, i1);
                    }
                }, new ElectricPower(1f))
                .view(R.drawable.motor)
                .tag("Power Filter")
                .save()
//                .add((IValue<Power> self, Power i) -> { self._set(i); Log.w("test", String.format("OK %d", i.to_f())); }, new Power(0))
                .add(new IConsumer<ElectricPower, ElectricPower>() {
                    @Override
                    public void consume(IValue<ElectricPower> self1, ElectricPower i1) {
                        ElectricPower.CONSUME(self1, i1);
                    }
                }, new ElectricPower(0))
                .view(R.drawable.motor)
                .tag("Power Consumer")
//                .setLogLevel()
                .save()
//                .add((IValue<Power> self, IValue<Power> p) -> p._set(Power.plus(self._get(), p._get())), new Power(3), 1)
                .add(new IEffector<IValue<ElectricPower>, ElectricPower>() {
                    @Override
                    public void effect(IValue<ElectricPower> self1, IValue<ElectricPower> p) throws ChainException {
                        ElectricPower.EFFECT(self1, p);
                    }
                }, new ElectricPower(3), 1)
                .view(R.drawable.motor)
                .tag("Power Effector")
                .save()
        ;

        f = new IFilter<ElectricPower, ElectricPower, ElectricPower>() {
            @Override
            public ElectricPower func(IValue<ElectricPower> self, ElectricPower i) {
                if (((Actor) self).getTime() == 5) {
//                Actor actor =
                    new TapManager(AndroidTapChain.this)
                            .add(f, new ElectricPower(i.to_f() * 100))
                            .view(R.drawable.motor)
                            .save().pullFrom((Actor) self);

//                onAddAndInstallView(now.plus(100f, 100f), actor);
                }
                return ElectricPower.plus(i, self._get());
            }
        };
        editBlueprint()
                .add(f, new ElectricPower(1f))
                .view(R.drawable.motor2)
                .tag("Power Filter 2")
                .save();

        editBlueprint()
                .add(new IGenerator<Fuel, Fuel>() {
                    @Override
                    public Fuel generate(IValue<Fuel> self) {
                        return Fuel.GENERATE(self);
                    }
                }, new Fuel(1f))
                .view(R.drawable.oil)
                .tag("Fuel Generator")
                .save()
                .add(new IFilter<ElectricPower, Fuel, ElectricPower>() {
                    @Override
                    public ElectricPower func(IValue<ElectricPower> self, Fuel i) {
                        return Fuel.FUEL_TO_ELECTRIC(self, i);
                    }
                }, new ElectricPower(1f))
                .view(R.drawable.engine)
                .tag("Power Converter").setLogLevel()
                .save()
                .add(new IGenerator<Wind, Wind>() {
                    @Override
                    public Wind generate(IValue<Wind> self) {
                        return Wind.GENERATE(self);
                    }
                }, new Wind(1f))
                .view(R.drawable.wind2)
                .tag("Wind Generator")
                .save()
                .add(new IFilter<Fan, Wind, ElectricPower>() {
                    @Override
                    public ElectricPower func(IValue<Fan> self, Wind i) {
                        return Fan.WIND_TO_ELECTRIC(self, i);
                    }
                }, new Fan(1f))
                .view(R.drawable.propeller)
                .tag("fan")
                .save();

//        IFilter<ElectricPower, Fuel, ElectricPower> filter = new IFilter<ElectricPower, Fuel, ElectricPower>() {
//            @Override
//            public ElectricPower func(IValue<ElectricPower> self, Fuel i) {
//                return Fuel.FUEL_TO_ELECTRIC(self, i);
//            }
//        };
//        Log.w("test", ((ParameterizedType)filter.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].toString());
        ShowInstance.addClassImage(ElectricPower.class, R.drawable.electricity, ElectricPower::STR);
        ShowInstance.addClassImage(Fuel.class, R.drawable.oil, Fuel::STR);
        ShowInstance.addClassImage(Wind.class, R.drawable.wind, Wind::STR);
        ShowInstance.addClassImage(Fan.class, R.drawable.propeller2, Fan::STR);
        EditInstance.addEditCreator(ElectricPower.class, ElectricPower::EDIT);
        EditInstance.addEditCreator(Fuel.class, Fuel::EDIT);
        EditInstance.addEditCreator(Wind.class, Wind::EDIT);
        EditInstance.addEditCreator(Fan.class, Fan::EDIT);
    }
    IFilter<ElectricPower, ElectricPower, ElectricPower> f;
    public void setActivity(Activity act) {
        this.act = act;
    }

    // 2.Getters and setters
    public static class ElectricPower {
        float p = 0f;
        public ElectricPower(float value) {
            p = value;
        }
        public float to_f() {
            return p;
        }
        public String toString() {
            return String.valueOf(p);
        }
        public static String STR(ElectricPower electricPower) {
            return Float.toString(electricPower.p);
        }
        public static ElectricPower plus(ElectricPower a, ElectricPower b) {
            return new ElectricPower(a.to_f() + b.to_f());
        }
        public static ElectricPower GENERATE(IValue<ElectricPower> self) {
            return self._get();
        }
        public static ElectricPower FILTER_PLUS(IValue<ElectricPower> self, ElectricPower i) {
            return ElectricPower.plus(i, self._get());
        }
        public static void CONSUME(IValue<ElectricPower> self, ElectricPower i) {
            self._set(i);
            Log.w("test", String.format("OK %f", i.to_f()));
        }
        public static void EFFECT(IValue<ElectricPower> self, IValue<ElectricPower> p) {
            p._set(ElectricPower.plus(self._get(), p._get()));
        }
        public static ActorTapView EDIT(IActorTapView parent) {
            return new MySetFloatTapViewStyle(parent) {
                @Override
                public void setParentValue(IPoint pos, IPoint vp) {
                    float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/oneangle;
                    if (j < 0) j += 10f;
                    j = Math.round(j*10f)*0.1f;
                    getParentTap().setMyActorValue(new ElectricPower(j));
                }
                @Override
                public boolean equalMyValue(Integer val) {
                    return val == ((Float)((ElectricPower)getParentTap().getMyActorValue()).to_f()).intValue();
                }
            };
        }
    }

    public static class Fuel {
        float p = 0f;
        public Fuel(float f) {
            p = f;
        }
        public float to_f() {return p;}
        public String toString() { return Float.toString(p); }
        public static String STR(Fuel fuel) {
            return Float.toString(fuel.p);
        }
        public static Fuel GENERATE(IValue<Fuel> self) {
            return self._get();
        }
        public static ElectricPower FUEL_TO_ELECTRIC(IValue<ElectricPower> self, Fuel i) {
            return new ElectricPower(i.to_f()+ self._get().to_f());
        }
        public static ActorTapView EDIT(IActorTapView parent) {
            return new MySetFloatTapViewStyle(parent) {
                @Override
                public void setParentValue(IPoint pos, IPoint vp) {
                    float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/oneangle;
                    if (j < 0) j += 10f;
                    j = Math.round(j*10f)*0.1f;
                    getParentTap().setMyActorValue(new Fuel(j));
                }
                @Override
                public boolean equalMyValue(Integer val) {
                    return val == ((Float)((Fuel)getParentTap().getMyActorValue()).to_f()).intValue();
                }
            };
        }


    }
    public static class Fan {
        float p = 1f;
        public Fan(float f) { p = f; }
        public static ElectricPower WIND_TO_ELECTRIC(IValue<Fan> self, Wind i) {
            return new ElectricPower(i.to_f() * self._get().p);
        }
        public static String STR(Fan fan) {
            return Float.toString(fan.p);
        }
        public static ActorTapView EDIT(IActorTapView parent) {
            return new MySetFloatTapViewStyle(parent) {
                @Override
                public void setParentValue(IPoint pos, IPoint vp) {
                    float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/oneangle;
                    if (j < 0) j += 10f;
                    j = Math.round(j*10f)*0.1f;
                    getParentTap().setMyActorValue(new Fan(j));
                }
                @Override
                public boolean equalMyValue(Integer val) {
                    return val == ((Float)((Fan)getParentTap().getMyActorValue()).p).intValue();
                }
            };
        }
    }

    public static class Wind {
        float p = 0f;
        public Wind(float f) {
            p = f;
        }
        public float to_f() {return p;}
        public String toString() { return Float.toString(p); }
        public static String STR(Wind wind) {
            return Float.toString(wind.p);
        }
        public static Wind GENERATE(IValue<Wind> self) {
            return self._get();
        }
        public static ActorTapView EDIT(IActorTapView parent) {
            return new MySetFloatTapViewStyle(parent) {
                @Override
                public void setParentValue(IPoint pos, IPoint vp) {
                    float j = (pos.subNew(getParentTap().getCenter()).theta()-startangle)/oneangle;
                    if (j < 0) j += 10f;
                    j = Math.round(j*10f)*0.1f;
                    getParentTap().setMyActorValue(new Wind(j));
                }
                @Override
                public boolean equalMyValue(Integer val) {
                    return val == ((Float)((Wind)getParentTap().getMyActorValue()).to_f()).intValue();
                }
            };
        }
    }
    WorldPoint now = new WorldPoint(400f, 400f);
//    public Power FILTER_PLUS_ADD(IValue<Power> self, Power i) {
//        if(((Actor)self).getTime()==5) {
////            tapChain().add(this::FILTER_PLUS_ADD, new Power(1f)).save();
//            addActorFromBlueprint(b, now.plus(100f, 100f));
//        }
//        return Power.plus(i, self._get());
//    }


    // 3.Changing state
    public class BubbleTapViewStyle extends MyTapViewStyle2 {
        public BubbleTapViewStyle() {
            super(AndroidTapChain.this, AndroidTapChain.this.act);
            setBackground(bubble_init());
        }

        public BubbleTapViewStyle(Integer bm) {
            super(AndroidTapChain.this, AndroidTapChain.this.act, bm);
            setBackground(bubble_init());
        }

        public Bitmap bubble_init() {
            return null;
        }
    }

    public class AndroidInteractionStyle implements
            IActionStyle {
        CountDownLatch c = new CountDownLatch(1);
        ViewActor a, b;
        ViewActor mark;

        AndroidInteractionStyle() {
            super();
//            setLinkClass(LinkType.PUSH, IPoint.class);
        }

        @Override
        public InteractionType checkTouchType(IView f1, IView f2) {
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

        public class Mark extends ActorTapView {
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
    protected void createFocus(IActorTapView v) {
        final Actor actor = v.getActor();
        ClassEnvelope firstClassEnvelope = null;
        LinkType first = null;
        IFocusable firstSpot = null;
        if (actor == null || actor == getFocusControl().getTargetActor()) {
            return;
        }
        getFocusControl().clearAllFocusables();

        for (LinkType al : LinkType.values()) {
            ClassEnvelope clz = actor.getLinkClassFromLib(al);
            if (clz == null) {
                continue;
            }

            //Create beam view
            IFocusable spot;
            switch (al) {
                case PUSH:
                    MyBeamTapViewStyle beam = new MyBeamTapViewStyle(act.getResources(), v, al, clz);
                    if (v instanceof MyTapViewStyle2)
                        beam.init(((MyTapViewStyle2) v).getOffsetVectorRawCopy(clz));
                    spot = beam;
                    break;
                case TO_CHILD:
                    spot = new MySpotOptionTapViewStyle(v, al, clz);
                    break;
                default:
                    continue;
            }

            getFocusControl().addFocusable(spot, al);
            if (first == null/* spotLatest == al*/) {
                first = al;
                firstClassEnvelope = clz;
                firstSpot = spot;
            }
        }
        if(firstSpot == null) {
            changeFocus(null, null, null);
            return;
        }
        getFocusControl().save();
        getFocusControl().setTargetActor(actor);
        changeFocus(first, firstSpot, firstClassEnvelope);
    }


}
