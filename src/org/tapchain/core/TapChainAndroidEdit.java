package org.tapchain.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.tapchain.core.ActorChain.IErrorHandler;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.ActorManager.ILogHandler;
import org.tapchain.core.AndroidActor.AndroidImageView;
import org.tapchain.core.AndroidActor.AndroidQuaker;
import org.tapchain.core.AndroidActor.AndroidView;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.PathPack.ChainInPathPack;
import org.tapchain.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;

@SuppressWarnings("serial")
public class TapChainAndroidEdit extends TapChainEdit implements ILogHandler {
	static AndroidView circle = new AndroidView();
	//1.Initialization
	//2.Getters and setters
	//3.Changing state
	//4.Termination
	//5.Local classes
	public class Test extends LocalView {
		public Test() {
			super(R.drawable.gu);
		}
	}

	public class Test2 extends LocalView {
		public Test2() {
			super(R.drawable.shake);
		}
	}

	public class Test3 extends LocalView {
		public Test3() {
			super(R.drawable.quake);
		}
	}

	public class Testdown extends LocalView {
		public Testdown() {
			super(R.drawable.move_down);
		}
	}

	public class Testheart extends LocalView {
		public Testheart() {
			super(R.drawable.heart);
		}
	}

	public class Testheartbright extends LocalView {
		public Testheartbright() {
			super(R.drawable.heart_bright);
		}
	}

	public class Testleft extends LocalView {
		public Testleft() {
			super(R.drawable.move_left);
		}
	}

	public class TestRecord extends LocalView {
		public TestRecord() {
			super(R.drawable.record);
		}
	}

	public class TestReset extends LocalView {
		public TestReset() {
			super(R.drawable.stop);
		}
	}

	public class Testright extends LocalView {
		public Testright() {
			super(R.drawable.move_right);
		}
	}

	public class TestRotate extends LocalView {
		public TestRotate() {
			super(R.drawable.rotate);
		}
	}

	public class TestShrink extends LocalView {
		public TestShrink() {
			super(R.drawable.shrink);
		}
	}

	public class TestSound extends LocalView {
		public TestSound() {
			super(R.drawable.sound1);
		}
	}

	public class Teststar extends LocalView {
		public Teststar() {
			super(R.drawable.star);
		}
	}

	public class TestTime extends LocalView {
		public TestTime() {
			super(R.drawable.timer);
		}
	}

	public class TestTime2 extends LocalView {
		public TestTime2() {
			super(R.drawable.timer_red);
		}
	}

	public class Testtop extends LocalView {
		public Testtop() {
			super(R.drawable.move_top);
		}
	}

	public class TestWiden extends LocalView {
		public TestWiden() {
			super(R.drawable.wide);
		}
	}

	static Bitmap bm_bg;
	public class LocalView extends TapChainAndroidEdit.EditorView implements
				Serializable {
			Paint _paint = new Paint();
			Paint paint2 = new Paint();
			int fontsize = 20;
			public Bitmap bm_fg = null;
			Integer frontview = null;
			String name;
			WorldPoint sizeOpened = new WorldPoint(300, 300), sizeClosed = null;
			ShapeDrawable d;
			RectF tickCircleRect = new RectF();
			int tickCircleRadius = 40;
			float sweep = 0f;
	
			//1.Initialization
			public LocalView() {
				this(null);
			}
	
			public LocalView(Integer _bm) {
				super();
	//			editor = e;
				setInteraction(lInteract);
				setEventHandler(eh);
				_paint.setAntiAlias(true);
				_paint.setAlpha(255);
				_paint.setTextSize(fontsize);
				_paint.setColor(0xff999999);
				_paint.setTextAlign(Paint.Align.CENTER);
	
				if(bm_bg == null)
					bm_bg = BitmapFactory.decodeResource(
						AndroidActor.getResources(), R.drawable.newframe);
				sizeClosed = new WorldPoint(bm_bg.getWidth() / 2, bm_bg.getHeight() / 2);
				if (_bm != null) {
					localview_init(_bm);
				}
				// _Size = sizeClosed.clone();
				setPercent(new WorldPoint(0, 0));
				setAlpha(255);
	
				// _kick = new BasicKicker();
				d = new ShapeDrawable(new RoundRectShape(new float[] { 10, 10, 10, 10,
						10, 10, 10, 10 }, null, null));
				d.getPaint().setAntiAlias(true);
				d.getPaint().setColor(0xffbbbbbb);
				d.getPaint().setStyle(Paint.Style.STROKE);
	//			d.getPaint().setStyle(Paint.Style.FILL);
	//			d.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
				d.getPaint().setStrokeWidth(3);
	//		d.getPaint().setShadowLayer(4, 4, 4, 0x80000000);
			}
	
			public LocalView localview_init(Integer bit) {
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
			public LocalView setError(boolean err) {
				error = err;
				return this;
			}
	
			@Override
			public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
					int alpha) {
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
				return true;
			}
	
			private void drawBitmapCenter(Canvas canvas, Bitmap bm, ScreenPoint center,
					Paint paint) {
				canvas.drawBitmap(bm, center.x - bm.getWidth() / 2,
						center.y - bm.getHeight() / 2, paint);
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
			public LocalView setPercent(WorldPoint wp) {
				super.setPercent(wp);
				setSize(sizeOpened.multiply(0.01f * (float) _percent.x).plus(sizeClosed)
						.plus(sizeClosed));
				return this;
			}
	
			@Override
			public LocalView setColor(int _color) {
				super.setColor(_color);
				d.getPaint().setColor(_color);
				return this;
			}
	
			//3.Changing state
			public LocalView open() {
				if (open)
					return this;
				Actor.EffecterSkelton<WorldPoint> move = new Actor.Mover().initEffect(
						new WorldPoint(2, 2).setDif(), 5);
				Actor.Sizer size = new Actor.Sizer().size_init(new WorldPoint(20, 0).setDif(), 5);
				Actor.Alphar alpha = new Actor.Alphar().alpha_init(20, 5);
				getManager()._return(this)._child().add(move.disableLoop())
						.add(size.disableLoop()).add(alpha.disableLoop())._exit().save();
				d.getPaint().setColor(0x88bbbbbb);
				d.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
				open = true;
				return this;
	
			}
	
			public LocalView close() {
				if (!open)
					return this;
				Actor.EffecterSkelton<WorldPoint> move = new Actor.Mover().initEffect(new WorldPoint(-2,
						-2).setDif(), 10);
				Actor.Sizer size = new Actor.Sizer().size_init(new WorldPoint(-10, 0), 10);
				Actor.Alphar alpha = new Actor.Alphar().alpha_init(-20, 10);
				getManager()._return(this)._child().add(move.disableLoop())
						.add(size.disableLoop()).add(alpha.disableLoop())._exit().save();
				d.getPaint().setColor(0xffbbbbbb);
				d.getPaint().setStyle(Paint.Style.STROKE);
				open = false;
				return this;
			}
	
			public LocalView toggle() {
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
		}

	public static class LocalEventHandler implements IEventHandler {
		@Override
		public void onSelected(IView v) {
			if (v != null && v instanceof LocalView) {
				((LocalView) v).toggle();
			}
			
		}
	}

	public static class LocalInteraction extends Actor.RelationFilter implements
			IInteraction {
		CountDownLatch c = new CountDownLatch(1);
		ViewActor a, b;
		LocalInteraction() {
			super();
		}
	
		@Override
		public boolean checkTouch(IView f1, IView f2) {
			boolean rtn = (((AndroidView) f1).getScreenRect().intersect(((AndroidView) f2).getScreenRect()));
			a = (ViewActor) f1;
			b = (ViewActor) f2;
			if(rtn) c.countDown();
			return rtn;
		}
	
		@Override
		public boolean checkLeave(IView f1, IView f2) {
			Rect r = ((AndroidView) f2).getScreenRect();
			r.inset(-40, -40);
			return !(((AndroidView) f1).getScreenRect().intersect(r));
		}
	
		@Override
		public boolean checkSplit(Actor.ViewActor f1, Actor.ViewActor f2) {
			int a = f1._size.x + f2._size.x;
			return getDistanceSq(f1._wp, f2._wp) > 4 * a * a;
		}
	
		@Override
		public boolean checkIn(IView v1, IView v2) {
			boolean rtn = ((AndroidView)v2).getScreenRect().contains(((AndroidView)v1).getScreenRect());
			a = (ViewActor) v1;
			b = (ViewActor) v2;
			if(rtn) c.countDown();
			return rtn;
		}
	
		public boolean checkOut(IView v1, IView v2) {
			return !((AndroidView)v2).getScreenRect().contains(((AndroidView)v1).getScreenRect());
		}
		
		@Override
		public boolean relation_impl(Actor.ViewActor _a, Actor.ViewActor _b) throws InterruptedException {
			c.await();
			c = new CountDownLatch(1);
			push(a.getCenter().multiply(0.5f).plus(b.getCenter().multiply(0.5f)));
			return true;//checkTouch(a, b);
		}
	
		int getDistanceSq(WorldPoint sp1, WorldPoint sp2) {
			return (sp1.x - sp2.x) * (sp1.x - sp2.x) + (sp1.y - sp2.y)
					* (sp1.y - sp2.y);
		}
	}

	public class LocalViewFlower extends TapChainAndroidEdit.EditorView implements IEditAnimation {
			Paint p = new Paint();
			Bitmap bm_back;
			public LocalViewFlower() {
				super();
	//			setWindow(window);
				setInteraction(lInteract);
				init_image();
			}
			private void init_image() {
				bm_back = BitmapFactory.decodeResource(
						AndroidActor.getResources(), R.drawable.newframe);
				setSize(new WorldPoint(bm_back.getWidth(), bm_back.getHeight()));
			}
			public void init_animation(Manager maker) {
	//			maker
	//			.add(new BasicPiece.MoveViewEffect().initEffect(new WorldPoint(1,1).setDif(), 1));
			}
			@Override
			public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
					int alpha) {
				canvas.drawBitmap(bm_back, sp.x - bm_back.getWidth() / 2,
						sp.y - bm_back.getHeight() / 2, paint);
				return true;
			}
		}

	public static class Connect1 extends TapChainAndroidEdit.Connect implements IEditAnimation {
			Actor.ViewActor start, stop;
			PackType starttype, stoptype;
			WorldPoint offset1, offset2;
			Paint paint;
			ScreenPoint sp1, sp2, sp12, sp21;
			Manager maker;
			public Connect1() {
				super();
			}
			@Override
			public void view_init() throws ChainException {
	//			if(log != null)
	//				Log.w("ACM","Chained and StandBy");
				start = (Actor.ViewActor)pull();
				stop = (Actor.ViewActor)pull();
				starttype = (PackType)pull();
				stoptype = (PackType)pull();
	//			Log.w("ACM","Start");
				paint = new Paint();
				paint.setColor(Color.argb(255, 255, 255, 255));
				paint.setStyle(Paint.Style.STROKE); 
				paint.setAntiAlias(true);
				paint.setStrokeWidth(4);
				offset1 = getOffset(start.getSize(), starttype);
				offset2 = getOffset(stop.getSize(), stoptype);
			}
			@Override
			public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size, int alpha) {
				sp1 = start.getCenter().plus(offset1).getScreenPoint(AndroidActor.getWindow());
				sp12 = sp1.plus(offset1);
				sp2 = stop.getCenter().sub(offset2).getScreenPoint(AndroidActor.getWindow());
				sp21 = sp2.sub(offset2);
				Path p = new Path();
				p.moveTo(sp1.x, sp1.y);
				p.cubicTo(sp12.x, sp12.y, sp21.x, sp21.y, sp2.x, sp2.y);
	//			ScreenPoint packet = getPoint(beta);
	//			p.lineTo(sp1.x + offset1.x, sp1.y + offset1.y);
	//			p.lineTo(sp2.x - offset2.x, sp2.y - offset2.y);
	//			p.lineTo(sp2.x, sp2.y);
				canvas.drawPath(p, paint);
				return true;
			}
			public void init_animation(Manager maker) {
				this.maker = maker;
	//			maker
	//			._func(arg)
	//			.add(new TransactionalEffect<Float>() {
	//				public void effect_reset() throws ChainException {
	//					super.effect_reset();
	//					((Connect1)getTargetView()).beta = 0;
	//				}
	//				public void transaction(BasicView c) throws ChainException {
	//						((Connect1)c).beta += getEffectValue();
	//						push(((Connect1)c).beta);
	//				}
	//			}.initEffect(0.2f, 5))
	//			._exit()
	//			.Save();
			}
			public ScreenPoint getPoint(float beta) {
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
			static Bitmap bm_heart = BitmapFactory.decodeResource(
					AndroidActor.getResources(), R.drawable.heart_bright);
			class view extends EditorView {
				float b = 0f;
				@Override
				public void view_init() {
				}
				@Override
				public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size, int alpha) {
						ScreenPoint packet = getPoint(b);
						canvas.drawBitmap(bm_heart, packet.x - bm_heart.getWidth()/2, packet.y - bm_heart.getHeight()/2, paint);
						return true;
				}
			};
			public void onTick() {
//				maker
//				.add(new view().disableLoop())
//				._child()
//				.add(new Actor.Txn<Float>() {
//					public void txn(Actor.ViewActor c) throws ChainException {
//							((view)c).b += getEffectValue();
//					}
//				}.initEffect(0.2f, 5).disableLoop())
//				.young(new Actor.Resetter().setContinue(false).disableLoop())
//				._exit()
//				.save();
//					;
			}
		}

	public static class EditorView extends AndroidView implements
		IPieceView {
		IInteraction interact = null;
		IEventHandler event = null;
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
	
		private IPiece mytapchain = null;
	
		@Override
		public void onTick() {
		}
		
		protected EditorView setInteraction(IInteraction i) {
			interact = i;
			return this;
		}
	
		@Override
		public IInteraction getInteraction() {
			return interact;
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
	}

	public static class Connect extends AndroidView
			implements IPathView {
			ConnectorPath myPath = null;
			public Connect() {
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
			@Override
			public void finishPath(boolean cnt) {
				super.finish(cnt);
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
	
		public Sound ctrlStop() {
			super.ctrlStop();
			resetInPathPack(PackType.HEAP);
			return this;
		}
	
		public Sound ctrlReset() throws ChainException {
			super.ctrlReset();
			rate = (Float) pull();
			return this;
		}
	}

	@Override
	public void setWindow(IWindow v) {
		super.setWindow(v);
		final Actor p = new Actor();
		editorManager
			.add(new AndroidActor.AndroidAlert())
			.teacher(p).save();
		errHandle = new IErrorHandler() {
				public ChainPiece onError(ChainPiece bp, ChainException e) {
					p.push(e.location + "," + e.err);
					return bp;
				}

				@Override
				public ChainPiece onCancel(ChainPiece bp, ChainException e) {
					return null;
				}
			};
		editorManager.setError(errHandle);
		userManager.setError(errHandle);
		
		//Initialization of View(touching effect)
		editorManager
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
						setColor(nowPiece == null ? Color.BLACK : Color.WHITE);
						setSize(new WorldPoint(30, 100));
						setAlpha(100);
					}

					@Override
					public boolean view_user(Canvas canvas, ScreenPoint sp,
							WorldPoint size, int alpha) {
						paint_.setColor(getColor());
						paint_.setAlpha(alpha);
						canvas.drawCircle(sp.x, sp.y, size.x, paint_);
						return false;
					}
				})
				._child()
				.add(new Actor.EffecterSkelton<Integer>() {
					@Override
					public boolean actorRun() throws ChainException {
						getTargetView().getSize().x += 30;
						getTargetView().setAlpha(getTargetView().getAlpha() - 10);
						invalidate();
						return increment();
					}
				}.initEffect(1, 10)/*.setLogLevel(true)*/)
				.young(new Actor.Resetter().setContinue(true))
				._exit()
				._gomark()
				.add(circle)
				._child()
				.add(new Actor.Colorer().color_init(0xffffffff).disableLoop().boost())
				._exit()
				.add(((IPiece)lInteract)/*.setLogLevel(true)*/)
				.student(new AndroidView() {
					Bitmap bm_star;
					Paint paint = new Paint();
					@Override
					public void view_init() throws ChainException {
						bm_star = BitmapFactory.decodeResource(
								AndroidActor.getResources(), R.drawable.star);
						setCenter((WorldPoint)pull());
					}
	
					@Override
					public boolean view_user(Canvas canvas, ScreenPoint sp,
							WorldPoint size, int alpha) {
						canvas.drawBitmap(bm_star, sp.x, sp.y, paint);
						return true;
					}
				})
			._child()
			.add(new Actor.Mover().initEffect(new WorldPoint(10, 10).setDif(), 10)/*.setLogLevel(true)*/)
			.young(new Actor.Resetter().setContinue(true))
			._exit()
				.save();

	}

	public TapChainAndroidEdit() {
		super();
		 lInteract = new TapChainAndroidEdit.LocalInteraction();
		 eh = new TapChainAndroidEdit.LocalEventHandler();
		 userManager
		.makeBlueprint()
		.setOuterInstanceForInner(this)
		.New(AndroidImageView.class, new Actor.Value(R.drawable.star))
		.setView(Teststar.class)
		.save()
//		.New(AndroidImageView.class, new BasicPiece.Value(R.drawable.star))
//		.setview(LocalViewFlower.class)
//		.Save()
		.New(AndroidImageView.class, new Actor.Value(R.drawable.heart_bright))
		.setView(Testheartbright.class)
		.save()
		.New(AndroidImageView.class, new Actor.Value(R.drawable.heart))
		.setView(Testheart.class)
		.save()
		.New(Actor.Mover.class, new Actor.Value(new WorldPoint(3, 0).setDif()))
		.setView(Testright.class)
		.save()
		.New(Actor.Mover.class, new Actor.Value(new WorldPoint(0, 3).setDif()))
		.setView(Testdown.class)
		.save()
		.New(Actor.Mover.class, new Actor.Value(new WorldPoint(-3, 0).setDif()))
		.setView(Testleft.class)
		.save()
		.New(Actor.Mover.class, new Actor.Value(new WorldPoint(0, -3).setDif()))
		.setView(Testtop.class)
		.save()
//		.New(MoveViewEffect.class, new ValueLimited(new WorldPoint(0, -10), 10))
//		.setview(Test.class)
//		.Save()
		.New(Actor.Sizer.class, new Actor.Value(new WorldPoint(1, 1)))
		.setView(TestWiden.class)
		.save()
		.New(Actor.Sizer.class, new Actor.Value(new WorldPoint(-1, -1)))
		.setView(TestShrink.class)
		.save()
		.New(Actor.Rotater.class, new Actor.Value(10))
		.setView(TestRotate.class)
		.save()
//		.New(BasicPiece.AlphaEffect.class, new BasicPiece.Value(1))
//		.setview(Test.class)
//		.Save()
//		.New(BasicPiece.AlphaEffect.class, new BasicPiece.Value(-1))
//		.setview(Test.class)
//		.Save()
//		.New(AndroidPiece.AndroidImageView.class, new BasicPiece.Value(R.drawable.star))
//		.setview(Test.class)
//		._return()
//		.because(new TouchFilter() {
//			public boolean filter(Object obj) {
//				ScreenPoint __p = ((WorldPoint) obj)
//						.getScreenPoint(AndroidPiece.getWindow());
//				return __p.x < 30 && __p.y < 30;
//			}
//		})
//		.Save()
//		.New(new PieceBlueprint(BasicPiece.MoveViewEffect.class) {
//			@Override
//			public void init_user(BasicPiece newinstance,
//					ChainManager maker)
//					throws InterruptedException {
//				((BasicPiece.EffectSkelton)newinstance).setParentType(PackType.HEAP);
//				((BasicPiece.MoveViewEffect) newinstance).initEffect(new WorldPoint(10, 10).setDif(), 10);
//				newinstance.setInPackType(PackType.FAMILY,
//						ChainPiece.Input.FIRST);
//			}
//		})
//		.and(BasicPiece.ResetEffect.class)
//		.setview(Test.class).Save()
		.New(AndroidActor.AndroidRecorder.class)
//		.because(TouchFilter.class)
//		._return()
		.child(Actor.Sleeper.class)
		.and(Actor.Resetter.class)
		.setView(TestRecord.class)
		.save()
		.New(AndroidActor.AndroidCamera.class)
		.setView(TestRecord.class)
		.save()
		.New(Actor.ShakeFilter.class).setView(Test2.class).save()
		.New(AndroidQuaker.class).setView(Test3.class).save()
		.New(TestSound2.class).setView(TestSound.class)//.Save()
	 .because(Actor.ShakeFilter.class).save()
		.New(TapChainAndroidEdit.TestSound3.class).setView(TestSound.class)//.Save()
	 /*.because(ShakeFilter.class)*/.save()
		.New(Actor.Stun.class)
		.setView(LocalViewFlower.class)
		.save()
		.New(Actor.Sleeper.class)
		.setView(TestTime.class)
		.save()
		.New(Actor.Resetter.class).setView(TestReset.class).save()
		.New(Actor.Counter.class).setView(TestTime2.class).save()
		.New(Actor.Value.class).addArg(new Class<?>[]{Object.class},
				new Object[]{3}).setView(TestTime2.class).save()
		.New(AndroidActor.AndroidNumberView.class).setView(TestTime2.class).save()
		;
// .New(AndroidPiece.AndroidRecognizer.class)
// .setview(Test.class)
// .Save();

		setPathBlueprint(new Blueprint(Connect1.class));
		setLog(this);

	}
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

	@Override
	public boolean onDown(WorldPoint sp) {
		super.onDown(sp);
		onMoveView(circle);
		return true;
	}
}
