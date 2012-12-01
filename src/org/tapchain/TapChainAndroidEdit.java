package org.tapchain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.tapchain.AndroidActor.AndroidImageView;
import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Actor;
import org.tapchain.core.Actor.ViewActor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ConnectorPath;
import org.tapchain.core.EditorManager;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.ILogHandler;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.ScreenPoint;
import org.tapchain.core.StyleCollection;
import org.tapchain.core.TapChainEdit;
import org.tapchain.core.TapMath;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.IPoint.WPEffect;
import org.tapchain.core.PathPack.ChainInPathPack;
import org.tapchain.core.TapChainEdit.ConnectType;
import org.tapchain.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;

@SuppressWarnings("serial")
public class TapChainAndroidEdit extends TapChainEdit 
	{
//	private IInteraction lInteract = null;
	IEventHandler eh = null;
//	AndroidView circle = new AndroidView();
	//1.Initialization
	final Actor p = new Actor();
	Actor l;
	BitmapMaker bitmapmaker = new BitmapMaker();
	TapChainGoalSystem goal;
	boolean magnet = true;
	
	public TapChainAndroidEdit(IWindow w) {
		super(w);
		
		//Setting styles
		setStyle(new StyleCollection(this, BubbleSystemStyle.class, BubbleConnectStyle.class, new LocalInteraction()));
//		 setInteract(new LocalInteraction());
		 eh = new LocalEventHandler();
		 
		 //Making list of pieces
		 getBlueprintManager()
			.add(AndroidImageView.class)
			.arg(R.drawable.star1)
//			.child(Actor.Mover.class).arg(new WorldPoint(3, 0).setDif())
			.setSystemArg(R.drawable.star)
		
			.add(AndroidImageView.class).arg(R.drawable.heart_bright)
			.setSystemArg(R.drawable.heart)
			
//			.add(AndroidImageView.class).arg(R.drawable.heart)
//			.viewStyleArg(R.drawable.heart)
//			
			.add(Actor.Mover.class).arg(new WorldPoint(3, 0).setDif())
			.setSystemArg(R.drawable.right)
			
			.add(Actor.Mover.class).arg(new WorldPoint(0, 3).setDif())
			.setSystemArg(R.drawable.down)
			
			.add(Actor.Mover.class).arg(new WorldPoint(-3, 0).setDif())
			.setSystemArg(R.drawable.left)
			
			.add(Actor.Mover.class).arg(new WorldPoint(0, -3).setDif())
			.setSystemArg(R.drawable.up)
			
			.add(Actor.Sizer.class).arg(new WorldPoint(1, 1), 3)
			.setSystemArg(R.drawable.widen)
			
			.add(Actor.Accelerator.class).arg(new WorldPoint(1,0))
			.setSystemArg(R.drawable.shrink)
			
			.add(AndroidActor.AndroidRecognizer.class)
			.setSystemArg(R.drawable.record)

			.add(AndroidActor.AndroidCamera.class)
			.setSystemArg(R.drawable.photo)
			
			.add(AndroidActor.AndroidMail.class)
			.arg("mailto:heretic55@docomo.ne.jp")
//			.arg("mailto:o-sanmail@docomo.ne.jp")
			.setSystemArg(R.drawable.mail)
		;
			
//			.New(Actor.Sizer.class).arg(new WorldPoint(-1, -1), 3)
//			.viewStyleArg(R.drawable.shrink)
//			.save()
//			
//			.add(Actor.Rotater.class).arg(10)
//			.viewStyleArg(R.drawable.rotate)
//			
//			.New(AndroidActor.AndroidRecorder.class)
//			.child(Actor.Sleeper.class)
//			.young(Actor.Resetter.class)
//			.viewStyleArg(R.drawable.record)
//			.save()
//			
			
//			.New(AndroidActor.AndroidSearch.class)
//			.viewStyleArg(R.drawable.recognize)
//			.save()
//
//			
//			.New(AndroidActor.AndroidMail2.class)
//			.viewStyleArg(R.drawable.mail)
//			.save()
//			
//			.New(Actor.ShakeFilter.class)
//			.viewStyleArg(R.drawable.shake)
//			.save()
//			.New(AndroidQuaker.class)
//			.viewStyleArg(R.drawable.quake)
//			.save()
//			
//			.New(TestSound2.class)//.Save()
//			.because(Actor.ShakeFilter.class)
//			.viewStyleArg(R.drawable.sound1)
//			.save()
//			
//			.New(TapChainAndroidEdit.TestSound3.class)
//			.viewStyleArg(R.drawable.sound1)
//			.save()
//			
//			.New(Actor.Stun.class)
//			.setView(LocalViewFlower.class)
//			.save()
//			
//			.New(Actor.Sleeper.class)
//			.viewStyleArg(R.drawable.timer)
//			.save()
//			
//			.New(Actor.Resetter.class).setView(TestReset.class).save()
//			.New(Actor.Counter.class).setView(TestTime2.class).save()
//			.New(Actor.Value.class).arg(3).setView(TestTime2.class).save()
//			.New(AndroidActor.AndroidNumberView.class).setView(TestTime2.class).save()

//			.New(MoveViewEffect.class, new ValueLimited(new WorldPoint(0, -10), 10))
//			.setview(Test.class)
//			.Save()
//			.New(BasicPiece.AlphaEffect.class, new BasicPiece.Value(1))
//			.setview(Test.class)
//			.Save()
//			.New(BasicPiece.AlphaEffect.class, new BasicPiece.Value(-1))
//			.setview(Test.class)
//			.Save()
//			.New(AndroidPiece.AndroidImageView.class, new BasicPiece.Value(R.drawable.star))
//			.setview(Test.class)
//			._return()
//			.because(new TouchFilter() {
//				public boolean filter(Object obj) {
//					ScreenPoint __p = ((WorldPoint) obj)
//							.getScreenPoint(AndroidPiece.getWindow());
//					return __p.x < 30 && __p.y < 30;
//				}
//			})
//			.Save()
//			.New(new PieceBlueprint(BasicPiece.MoveViewEffect.class) {
//				@Override
//				public void init_user(BasicPiece newinstance,
//						ChainManager maker)
//						throws InterruptedException {
//					((BasicPiece.EffectSkelton)newinstance).setParentType(PackType.HEAP);
//					((BasicPiece.MoveViewEffect) newinstance).initEffect(new WorldPoint(10, 10).setDif(), 10);
//					newinstance.setInPackType(PackType.FAMILY,
//							ChainPiece.Input.FIRST);
//				}
//			})
//			.and(BasicPiece.ResetEffect.class)
//			.setview(Test.class).Save()
//		setPathBlueprint(new Blueprint(Connect1.class));
//			.because(TouchFilter.class)
//			._return()

		goalBlueprintManager
		.add(TapChainGoalView.class)
		.setSystem(TapChainGoalSystem.class);

		getSystemManager()
				.add(l = new AndroidActor.AndroidAlert())
				/*.teacher(p)*/._save();
			
			//Initialization of View(touching effect)
			getSystemManager()
				.add(new Actor.TouchFilter())
				._mark()
				.student(new AndroidView() {
					Paint paint_ = new Paint();

					public void view_init() {
						try {
							setCenter((WorldPoint) pull());
						} catch (Exception e) {
							setCenter(new WorldPoint(100, 100));
						}
						setColor(startPiece == null ? Color.BLACK : Color.WHITE);
						setSize(new WorldPoint(30, 100));
						setAlpha(100);
					}

					@Override
					public boolean view_user(Canvas canvas, IPoint sp,
							WorldPoint size, int alpha) {
						paint_.setColor(getColor());
						paint_.setAlpha(alpha);
						canvas.drawCircle(sp.x(), sp.y(), size.x(), paint_);
						return false;
					}
				})
				._child()
				.add(new Actor.EffectorSkelton<ViewActor, Integer>() {
					@Override
					public boolean actorRun(Actor act) throws ChainException {
						getTargetView().getSize().x += 30;
						getTargetView().setAlpha(getTargetView().getAlpha() - 10);
						invalidate();
						return increment();
					}
				}.initEffect(1, 10)/*.setLogLevel(true)*/)
				.young(new Actor.Resetter().setContinue(true))
				._exit()
				._gotomark()
				.add(((IPiece)getInteract())/*.setLogLevel(true)*/)
			._save();
			freezeToggle();
			IPiece sp = onAdd(getGoal(), 0, new WorldPoint((int)(400*Math.random()), (int)(400*Math.random()))).getKey();
			IPiece sp1 = onAdd(getGoal(), 0, new WorldPoint((int)(400*Math.random()), (int)(400*Math.random()))).getKey();
			getUserManager()._return(sp).young(sp1);
			freezeToggle();
	}
	
	public class TapChainGoalView extends AndroidImageView {
		boolean achieved = false;
		public TapChainGoalView() {
			super(R.drawable.star);
			setCenter(new WorldPoint((int)(400*Math.random()), (int)(400*Math.random())));
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
			if(!achieved)
				for(IPiece p : editorManager.getUserPieces())
					if(p instanceof IView && p != this)
						if(getInteract().checkConnect((IView)p, this, false)!=ConnectType.NONE) {
							//Goal!!!
							interrupt(ControllableSignal.END);
							achieved = true;
						}
			return super.view_user(canvas, sp, size, alpha);
		}
	}
	//2.Getters and setters

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
		return;
	}

//	public boolean onDown(WorldPoint sp) {
//		super.onDown(sp);
////		editorManager.onMoveView(circle, nowPoint);
//		return true;
//	}

	public boolean magnetToggle() {
		magnet = !magnet;
		return magnet;
	}
	
	@Override
	public ChainPiece onError(ChainPiece bp, ChainException e) {
		l.innerRequest(PackType.HEAP, String.format("%s: \"%s\"", e.getLocation(), e.getError()));
	    float[] colorTransform = {
	            1f, 0f, 0f, 0f, 0, 
	            0f, 0.2f, 0f, 0f, 0,
	            0f, 0f, 0.2f, 0f, 0, 
	            0f, 0f, 0f, 1f, 0};

		getSystemManager()
			.add(new AndroidImageView(R.drawable.error).setColorFilter(colorTransform).setCenter(editorManager.getView(bp).get()).setPercent(new WorldPoint(200,200)))
			._child()
			.add(new Actor.Sleeper(2000))
			.young(new Actor.Resetter(false))
			._exit()
			._save();
		return bp;
	}

	@Override
	public ChainPiece onCancel(ChainPiece bp, ChainException e) {
		return null;
	}
	//3.Changing state
	//4.Termination
	//5.Local classes

	public class RoadSystemStyle extends LocalSystemStyle {
		public RoadSystemStyle() {
			super();
			// set background image bitmap
			setBackground(road_init());
		}
		
		public RoadSystemStyle(Integer bm) {
			//set foreground image resource
			super(bm);
			// set background image bitmap
			setBackground(road_init());
		}
		public Bitmap road_init() {
			return bitmapmaker.makeOrReuse(getName(), R.drawable.roadback, 100, 100);
		}
	}

	public class BubbleSystemStyle extends LocalSystemStyle {
		public BubbleSystemStyle() {
			super();
			setBackground(bubble_init());
		}
		
		public BubbleSystemStyle(Integer bm) {
			super(bm);
			setBackground(bubble_init());
		}
		public Bitmap bubble_init() {
			return bitmapmaker.makeOrReuse(getName(), R.drawable.newframe, 100, 100);
		}
	}
	
	public class LocalSystemStyle extends EditorView implements
				Serializable {
			Paint _paint = new Paint();
			public Bitmap bm_fg = null;
			Integer frontview = null;
			String name;
			WorldPoint sizeOpened = new WorldPoint(300, 300), sizeClosed = new WorldPoint(50,50);
			ShapeDrawable d;
			RectF tickCircleRect = new RectF();
			int tickCircleRadius = 40;
			float sweep = 0f;
			Bitmap bm_bg;
	
			//1.Initialization
			public LocalSystemStyle() {
				this(null);
			}
	
			public LocalSystemStyle(Integer _bm) {
				super();
//				setInteraction(getInteract());
				setEventHandler(eh);
				_paint.setAntiAlias(true);
				_paint.setAlpha(255);
				_paint.setColor(0xffffffaa);
				_paint.setStrokeWidth(8);
				_paint.setTextAlign(Paint.Align.CENTER);
	
				if (_bm != null) {
					localview_init(_bm);
				}
				setPercent(new WorldPoint(0, 0));
				setAlpha(255);
	
				// _kick = new BasicKicker();
				d = new ShapeDrawable(new RoundRectShape(new float[] { 10, 10, 10, 10,
						10, 10, 10, 10 }, null, null));
				d.getPaint().setAntiAlias(true);
				d.getPaint().setColor(0xaabbbbbb);
				d.getPaint().setStyle(Paint.Style.STROKE);
	//			d.getPaint().setStyle(Paint.Style.FILL);
	//			d.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
				d.getPaint().setStrokeWidth(3);
	//		d.getPaint().setShadowLayer(4, 4, 4, 0x80000000);
			}
	
			public void setBackground(Bitmap background) {
				bm_bg = background;
				sizeClosed = new WorldPoint(bm_bg.getWidth() / 2, bm_bg.getHeight() / 2);
			}
			
			public LocalSystemStyle localview_init(Integer bit) {
				bm_fg = BitmapFactory.decodeResource(
						AndroidActor.getResources(), bit);
				frontview = bit;
				return this;
			}
			
			boolean open = false;
	
			@Override
			public void view_init() throws ChainException {
				 if(bm_fg == null) {
					 bm_fg = BitmapFactory.decodeResource(AndroidActor.getResources(), (Integer)pull());
				 }
				return;
			}
	
			//2.Getters and setters
			public LocalSystemStyle setError(boolean err) {
				error = err;
				return this;
			}
	
			@Override
			public boolean view_user(Canvas canvas, IPoint sp, WorldPoint size,
					int alpha) {
				_paint.setAlpha(alpha);
				//Draw a background view
				if(open) {
					d.setBounds(getScreenRect());
					d.draw(canvas);
				}
				drawBitmapCenter(canvas, bm_bg, sp, _paint);
				// Draw a foreground view
				if (bm_fg != null) {
					drawBitmapCenter(canvas, bm_fg, sp, _paint);
				}
				tickCircleRect.set(sp.x()-tickCircleRadius, sp.y()-tickCircleRadius, sp.x()+tickCircleRadius, sp.y()+tickCircleRadius);
				canvas.drawArc(tickCircleRect, -90f, (sweep-90)%720f-360f, false, d.getPaint());
				if(getMyTapChain() instanceof EffectorSkelton) {
					EffectorSkelton myc = (EffectorSkelton)getMyTapChain();
					Object val = myc.get();
					if(val instanceof Integer) {
						canvas.drawCircle(sp.x(), sp.y(), (Float) val, _paint);
					} else if(val instanceof IPoint) {
						IPoint v = ((IPoint) val);
						canvas.drawLine(sp.x(), sp.y(), sp.x()+20*v.x(), sp.y()+20*v.y(), _paint);
					}
				}
				return true;
			}
	
			private void drawBitmapCenter(Canvas canvas, Bitmap bm, IPoint center,
					Paint paint) {
				canvas.drawBitmap(bm, center.x() - bm.getWidth() / 2,
						center.y() - bm.getHeight() / 2, paint);
			}
	
			@Override
			public void user_rect(IPoint pt, WorldPoint size, Rect r) {
				IPoint sp = pt.sub(sizeClosed);
				r.set(sp.x(), sp.y(), sp.x() + size.x, sp.y() + size.y);
			}
	
			@Override
			public void user_rectF(IPoint pt, WorldPoint size, RectF rf) {
				IPoint sp = pt.sub(sizeClosed);
				rf.set(sp.x(), sp.y(), sp.x() + size.x, sp.y() + size.y);
			}
	
			@Override
			public LocalSystemStyle setPercent(WorldPoint wp) {
				super.setPercent(wp);
				setSize(sizeOpened.multiply(0.01f * (float) getPercent().x).plus(sizeClosed)
						.plus(sizeClosed));
				return this;
			}
	
			@Override
			public LocalSystemStyle setColor(int _color) {
				super.setColor(_color);
				d.getPaint().setColor(_color);
				return this;
			}
	
			//3.Changing state
			public LocalSystemStyle open() {
				if (open)
					return this;
				Actor.Sizer size = new Actor.Sizer(new WorldPoint(20, 0).setDif(), 5);
				Actor.Alphar alpha = new Actor.Alphar().alpha_init(20, 5);
				getSystemManager()._return(this)._child()
						.add(size.disableLoop()).add(alpha.disableLoop())._exit()._save();
				open = true;
				return this;
	
			}
	
			public LocalSystemStyle close() {
				if (!open)
					return this;
				Actor.Sizer size = new Actor.Sizer(new WorldPoint(-20, 0).setDif(), 5);
				Actor.Alphar alpha = new Actor.Alphar().alpha_init(-20, 5);
				getSystemManager()._return(this)._child()
						.add(size.disableLoop()).add(alpha.disableLoop())._exit()._save();
				open = false;
				return this;
			}
	
			public LocalSystemStyle toggle() {
				if (open)
					close();
				else
					open();
				return this;
			}
	
			boolean error = false;
	
			@Override
			public void onTick() {
				sweep += 20f;
			}
			
			@Override
			public void view_move(IPoint dif) {
				Actor a = (Actor)getMyTapChain();
				if(a == null)
					return;
				Collection<Actor> c = a.getMembers();
				if(c == null)
					return;
				for(Actor member: c)
					((ViewActor) editorManager.getView(member)).setCenter(dif);
//				__log(dif.toString(),"test");
			}
			
			@Override
			public boolean setMyTapChainValue(Object obj) {
//				AndroidActor.makeAlert("TEST");
				if(getMyTapChain() instanceof EffectorSkelton) {
					EffectorSkelton myc = (EffectorSkelton)getMyTapChain();
					Object val = myc.get();
					if(val.getClass() == obj.getClass()) {
						myc.set(obj);
						invalidate();
					}
				}
				return false;
			}
		}

	public static class LocalEventHandler implements IEventHandler {
		@Override
		public void onSelected(IView v) {
			if (v != null && v instanceof LocalSystemStyle) {
				((LocalSystemStyle) v).toggle();
			}
			
		}
	}

	public class LocalInteraction extends Actor.RelationFilter implements
			ActionStyle {
		CountDownLatch c = new CountDownLatch(1);
		ViewActor a, b;
		ViewActor mark;
		LocalInteraction() {
			super();
		}
	
		@Override
		public boolean relation_impl(Actor.ViewActor _a, Actor.ViewActor _b) throws InterruptedException {
			c.await();
			c = new CountDownLatch(1);
			push(a.getCenter().multiply(0.5f).plus(b.getCenter().multiply(0.5f)));
			return true;//checkTouch(a, b);
		}
		
		@Override
		public IPoint pointOnAdd(IPoint raw) {
			if(magnet)
				return raw.round(100).add(50, 50);
			else
				return raw;
		}

		@Override
		public ConnectType checkConnect(IView f1, IView f2, boolean onlyInclude) {
			Rect rect1 = ((AndroidView) f1).getScreenRect();
			rect1.inset(-10, -10);
			Rect intersect = new Rect();
			boolean rtn = intersect.setIntersect(rect1, ((AndroidView) f2).getScreenRect());
			if(!rtn) {
				return ConnectType.NONE;
			}
			a = (ViewActor) f1;
			b = (ViewActor) f2;
			if (checkGetIncluded(f1, f2)) {
				// when containing and not connected
				c.countDown();
				return ConnectType.GETINCLUDED;
			}
			if(onlyInclude)
				return ConnectType.NONE;
			c.countDown();
//			if (null == dir)
//				return ConnectType.NULL;
//			int dxy = dir.x + dir.y;
//			int d_xy = -dir.x + dir.y;
//			WorldPoint diff = f2.getCenter().sub(f1.getCenter());
//			int dxy = diff.x() + diff.y();
//			int d_xy = -diff.x() + diff.y();
//			if (dxy > 0) {
//				if(d_xy > 0) return ConnectType.TOUCH_TOP;
//				else return ConnectType.TOUCH_LEFT;
//			} else {
//				if(d_xy > 0) return ConnectType.TOUCH_RIGHT;
//				else return ConnectType.TOUCH_BOTTOM;
//			}
			if(intersect.width() > intersect.height()) {
				if(intersect.bottom == ((AndroidView) f2).getScreenRect().bottom) {
					return ConnectType.TOUCH_BOTTOM;
				} else {
					return ConnectType.TOUCH_TOP;
				}
			} else {
				if(intersect.left == ((AndroidView) f2).getScreenRect().left) {
					return ConnectType.TOUCH_LEFT;
				} else {
					return ConnectType.TOUCH_RIGHT;
				}
			}
			
		}
	
		@Override
		public ConnectType checkDisconnect(IView v1, final IView v2, PackType pt) {
//			Log.w("test", String.format("View1: %s, View2: %s, PackType: %s", v1.getName(), v2.getName(), pt.toString()));
			//Check if Piece v1 has left from Piece v2.
			if (checkLeave(v1, v2)) {
				//Check mark (view created when leaving)
				if(mark != null)
					//Remove mark
					getSystemManager().remove(mark);
				//Clear mark
				mark = null;
				//Return state "disconnected"
				return ConnectType.DISCONNECT;
			} else if (pt == PackType.FAMILY && !checkGetIncluded(v1, v2)) {
				//Check if PackType is family and Piece v1 is included in Piece v2.
				//View change in releasing state
				releasing(v1, v2);
				//Return state "releasing"
				return ConnectType.RELEASING;
			}
			//Return no disconnection.
			return ConnectType.NONE;
		}
		
		void releasing(IView v1, final IView v2) {
//			Log.w("test", "checkdisconnect detected family connection disconnecting");
			if(mark != null)
				return;
			final ShapeDrawable d = new ShapeDrawable(new RoundRectShape(new float[] { 150, 150, 150, 150,
					150, 150, 150, 150 }, null, null));
			d.getPaint().setAntiAlias(true);
			d.getPaint().setColor(0xffffffff);
			d.getPaint().setStyle(Paint.Style.STROKE);
			d.getPaint().setStrokeWidth(3);
			d.getPaint().setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
			mark = new EditorView() {
				@Override
				public boolean view_user(Canvas canvas, IPoint sp, WorldPoint size, int alpha) {
					Rect _r = ((AndroidView)v2).getWorldRect();
					_r.inset(-150, -150);
					d.setBounds(_r);
					d.draw(canvas);
					return true;
				}
			};
			getSystemManager().add(mark)._save();

		}
		
		boolean checkLeave(IView f1, IView f2) {
			Rect rect1 = ((AndroidView) f2).getScreenRect();
			rect1.inset(-50, -50);
			return !(((AndroidView) f1).getScreenRect().intersect(rect1));
		}
	
		boolean checkSplit(Actor.ViewActor f1, Actor.ViewActor f2) {
			int a = f1.getSize().x + f2.getSize().x;
			return getDistanceSq(f1.getCenter(), f2.getCenter()) > 4 * a * a;
		}
	
		boolean checkGetIncluded(IView v1, IView v2) {
			return ((AndroidView)v2).getScreenRect().contains(((AndroidView)v1).getScreenRect());
		}
	
		int getDistanceSq(IPoint iPoint, IPoint iPoint2) {
			return (iPoint.x() - iPoint2.x()) * (iPoint.x() - iPoint2.x()) + (iPoint.y() - iPoint2.y())
					* (iPoint.y() - iPoint2.y());
		}
		public boolean checkReleasing(IView v1, IView v2) {
			return !((AndroidView)v2).getScreenRect().contains(((AndroidView)v1).getScreenRect());
		}

		@Override
		public void onRelease() {
			if (mark != null)
				getSystemManager().remove(mark);
			mark = null;
		}

	}

//	public class LocalViewFlower extends TapChainAndroidEdit.EditorView {
//			Paint p = new Paint();
//			Bitmap bm_back;
//			public LocalViewFlower() {
//				super();
//	//			setWindow(window);
//				setInteraction(getInteract());
//				init_image();
//			}
//			private void init_image() {
//				bm_back = BitmapFactory.decodeResource(
//						AndroidActor.getResources(), R.drawable.newframe);
//				setSize(new WorldPoint(bm_back.getWidth(), bm_back.getHeight()));
//			}
//			@Override
//			public boolean view_user(Canvas canvas, IPoint sp, WorldPoint size,
//					int alpha) {
//				canvas.drawBitmap(bm_back, sp.x - bm_back.getWidth() / 2,
//						sp.y - bm_back.getHeight() / 2, paint);
//				return true;
//			}
//		}

	public class BubbleConnectStyle extends ConnectStyle  {
			Actor.ViewActor start, stop;
			PackType starttype, stoptype;
			WorldPoint offset1, offset2;
			Paint paint;
			IPoint sp1, sp2, sp12, sp21;
//			Tickview tickv = new Tickview();
			Bitmap bm_heart;
			Queue<Pos> bl;
			public BubbleConnectStyle(BubbleSystemStyle start, BubbleSystemStyle stop, PackType startType, PackType stopType) {
				super();
				bm_heart = bitmapmaker.makeOrReuse(getName(), R.drawable.heart);
				bl = new ConcurrentLinkedQueue<Pos>();
				this.start = start;
				this.stop = stop;
				this.starttype = startType;
				this.stoptype = stopType;
				sp1 = sp12 = sp21 = sp2 = new ScreenPoint();
			}
			@Override
			public void view_init() throws ChainException {
				paint = new Paint();
				paint.setColor(Color.argb(255, 255, 255, 255));
				paint.setStyle(Paint.Style.STROKE); 
				paint.setAntiAlias(true);
				paint.setStrokeWidth(4);
				offset1 = getOffset(start.getSize(), starttype);
				offset2 = getOffset(stop.getSize(), stoptype);
			}
			@Override
			public boolean view_user(Canvas canvas, IPoint sp, WorldPoint size, int alpha) {
				sp1 = start.getCenter().plus(offset1);
				sp12 = sp1.plus(offset1);
				sp2 = stop.getCenter().sub(offset2);
				sp21 = sp2.sub(offset2);
				Path p = new Path();
				p.moveTo(sp1.x(), sp1.y());
				p.cubicTo(sp12.x(), sp12.y(), sp21.x(), sp21.y(), sp2.x(), sp2.y());
				canvas.drawPath(p, paint);
				for(Pos b : bl) {
					if(b.over())
						bl.remove(b);
					IPoint p1 = getPoint(b.get());
					canvas.drawBitmap(bm_heart, p1.x() - bm_heart.getWidth()/2, p1.y() - bm_heart.getHeight()/2, paint);
					b.add();
				}
				return true;
			}
			public IPoint getPoint(float beta) {
				return TapMath.getCurvePoint(beta, Arrays.asList(sp1, sp12, sp21, sp2));
			}
			WorldPoint getOffset(WorldPoint size, PackType type) {
				WorldPoint offset = null;
				switch(type) {
				case FAMILY:
					offset = new WorldPoint(30, 30);
					break;
				case HEAP:
					offset = new WorldPoint(40, 0);
					break;
				default:
					offset = new WorldPoint(0, 40);
				}
				return offset;
			}
			
			//View class for tick action
			public class Tickview extends EditorView {
				public Tickview() {
				}
				@Override
				public void view_init() {
				}
				@Override
				public boolean view_user(Canvas canvas, IPoint sp, WorldPoint size, int alpha) {
					return true;
				}
			};
			public class Pos {
				float b;
				public Pos() {
					b = 0f;
				}
				public void add() {
					b += 0.2f;
				}
				public float get() {
					return b;
				}
				public boolean over() {
					return b > 0.8f;
				}
			}
			public void addPoint() {
				bl.add(new Pos());
			}
			@Override
			public void onTick() {
				addPoint();
			}
		}

	public static class EditorView extends AndroidView implements
		ISystemPiece {
		IEventHandler event = null;
		private IPiece mytapchain = null;
		
		public EditorView() {
			super();
		}
		
		public final void setMyTapChain(IPiece mytapchain) {
			this.mytapchain = mytapchain;
		}
		
		@Override
		public final void unsetMyTapChain() {
			this.mytapchain = null;
		}
	
		@Override
		public final IPiece getMyTapChain() {
			return mytapchain;
		}
	
		@Override
		public void onTick() {
		}
		
		protected EditorView setEventHandler(IEventHandler eh) {
			event = eh;
			return this;
		}
		
		@Override
		public IEventHandler getEventHandler() {
			return event;
		}
	
		@Override
		public boolean contains(int x, int y) {
			return getWorldRect().contains(x, y);
		}
		
		@Override
		public boolean setMyTapChainValue(Object obj) {
			return false;
		}

	}

	public static class ConnectStyle extends AndroidView
			implements ISystemPath {
			ConnectorPath myPath = null;
			public ConnectStyle() {
				super();
			}
			public void setMyTapPath(ConnectorPath p) {
				myPath = p;
			}
			public void unsetMyTapPath() {
				myPath = null;
			}
			public ConnectorPath getMyTapPath() {
				return myPath;
			}
			@Override
			public void onTick() {
			}
		}
	//	public interface FlowView {
	//		public boolean flowview_impl(Canvas canvas, Actor f);
	//	}

	public class TestSound2 extends AndroidActor.AndroidSound2 {
		TestSound2() {
			super(R.raw.drip);
			setInPackType(PackType.EVENT, ChainInPathPack.Input.FIRST);
		}
	
		@Override
		public boolean reset_sound_impl() {
			super.reset_sound_impl();
			rate = 1f;//(Float) pull();
			resetInPathPack(PackType.EVENT);
			return true;
		}
	}

	public static class TestSound3 extends AndroidActor.AndroidSound {
			TestSound3() {
				super(R.raw.drip);
				setInPackType(PackType.EVENT, ChainInPathPack.Input.FIRST);
			}
	
			@Override
			public boolean reset_sound_impl() {
				super.reset_sound_impl();
	//		resetInPathPack(PackType.HEAP);
				resetInPathPack(PackType.EVENT);
				return true;
			}
		}

	public static class TestSound4 extends AndroidActor.AndroidSound2 {
		TestSound4() {
			super("/sdcard/com.bambooflower.test.multitouch2/test.3gp");
		}
	
		@Override
		public void ctrlStop() {
			super.ctrlStop();
			resetInPathPack(PackType.HEAP);
		}
	
		@Override
		public void ctrlStart() throws ChainException {
			rate = (Float) pull();
			super.ctrlStart();
		}
	}
	
	public static class BitmapMaker {
		ConcurrentHashMap<String, Bitmap> bitmaps;
		public BitmapMaker() {
			bitmaps = new ConcurrentHashMap<String, Bitmap>();
		}
		public Bitmap makeOrReuse(String str, int resource, int x, int y) {
			if(bitmaps.containsKey(str))
				return bitmaps.get(str);
			Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
					AndroidActor.getResources(), resource), x, y, true);
			bitmaps.put(str, bitmap);
			return bitmap;
		}
		public Bitmap makeOrReuse(String str, int resource) {
			if(bitmaps.containsKey(str))
				return bitmaps.get(str);
			Bitmap bitmap = BitmapFactory.decodeResource(
					AndroidActor.getResources(), resource);
			bitmaps.put(str, bitmap);
			return bitmap;
		}
	}


}
