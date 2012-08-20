package org.tapchain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.tapchain.AndroidActor.AndroidImageView;
import org.tapchain.AndroidActor.AndroidQuaker;
import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Actor;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ConnectorPath;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.ILogHandler;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.PieceManager;
import org.tapchain.core.ScreenPoint;
import org.tapchain.core.TapChainEdit;
import org.tapchain.core.TapMath;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.ChainException;
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
public class TapChainAndroidEdit extends TapChainEdit 
	implements ILogHandler, IErrorHandler {
	private IInteraction lInteract = null;
	IEventHandler eh = null;
	static AndroidView circle = new AndroidView();
	//1.Initialization
	public TapChainAndroidEdit() {
		super();
		
		//Setting styles
		setStyle(this, new StyleCollection(LocalViewStyle.class, ConnectStyle.class, new LocalActionStyle()));
		 setInteract(new LocalInteraction());
		 eh = new LocalEventHandler();
		 
		 //Making list of pieces
		 blueprintManager
			.New(AndroidImageView.class)
			.arg(R.drawable.star)
			.viewStyleArg(R.drawable.star)
			.save()
		
			.New(AndroidImageView.class).arg(R.drawable.star)
			.child(Actor.Mover.class).arg(new WorldPoint(3, 0).setDif())
			.viewStyleArg(R.drawable.star)
			.save()
			
			.New(AndroidImageView.class).arg(R.drawable.heart_bright)
			.viewStyleArg(R.drawable.heart_bright)
			.save()
			
			.New(AndroidImageView.class).arg(R.drawable.heart)
			.viewStyleArg(R.drawable.heart)
			.save()
			
			.New(Actor.Mover.class).arg(new WorldPoint(3, 0).setDif())
			.viewStyleArg(R.drawable.right)
			.save()
			
			.New(Actor.Mover.class).arg(new WorldPoint(0, 3).setDif())
			.viewStyleArg(R.drawable.down)
			.save()
			
			.New(Actor.Mover.class).arg(new WorldPoint(-3, 0).setDif())
			.viewStyleArg(R.drawable.left)
			.save()
			
			.New(Actor.Mover.class).arg(new WorldPoint(0, -3).setDif())
			.viewStyleArg(R.drawable.up)
			.save()
			
			.New(Actor.Sizer.class).arg(new WorldPoint(1, 1))
			.viewStyleArg(R.drawable.widen)
			.save()
			
			.New(Actor.Sizer.class).arg(new WorldPoint(-1, -1))
			.viewStyleArg(R.drawable.shrink)
			.save()
			
			.New(Actor.Rotater.class, new Actor.Value(10))
			.viewStyleArg(R.drawable.rotate)
			.save()
			
			.New(AndroidActor.AndroidRecorder.class)
			.child(Actor.Sleeper.class)
			.young(Actor.Resetter.class)
			.viewStyleArg(R.drawable.record)
			.save()
			
			.New(AndroidActor.AndroidCamera.class)
			.viewStyleArg(R.drawable.photo)
//			.setView(TestRecord.class)
			.save()
			
			.New(Actor.ShakeFilter.class)
			.viewStyleArg(R.drawable.shake)
//			.setView(Test2.class)
			.save()
			.New(AndroidQuaker.class)
			.viewStyleArg(R.drawable.quake)
//			.setView(Test3.class)
			.save()
			
			.New(TestSound2.class)//.Save()
	 .because(Actor.ShakeFilter.class)
	 .setView(TestSound.class)
	 .save()
		.New(TapChainAndroidEdit.TestSound3.class)
		.setView(TestSound.class)//.Save()
	 /*.because(ShakeFilter.class)*/
		.save()
		.New(Actor.Stun.class)
		.setView(LocalViewFlower.class)
		.save()
		.New(Actor.Sleeper.class)
		.setView(TestTime.class)
		.save()
		.New(Actor.Resetter.class).setView(TestReset.class).save()
		.New(Actor.Counter.class).setView(TestTime2.class).save()
		.New(Actor.Value.class).arg(3).setView(TestTime2.class).save()
		.New(AndroidActor.AndroidNumberView.class).setView(TestTime2.class).save()
		;
			getSystemManager().setError(this);
			editorManager.setError(this);
			blueprintManager.setError(this);

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
		setLog(this);

	}
	
	@Override
	public void setWindow(IWindow v) {
		super.setWindow(v);
		getSystemManager()
			.add(new AndroidActor.AndroidAlert())
			.teacher(p).save();
		
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
				._gotomark()
				.add(circle)
				._child()
				.add(new Actor.Colorer().color_init(0xffffffff).disableLoop().boost())
				._exit()
				.add(((IPiece)getInteract())/*.setLogLevel(true)*/)
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
	//2.Getters and setters
	/**
	 * @return the lInteract
	 */
	public IInteraction getInteract() {
		return lInteract;
	}

	/**
	 * @param lInteract the lInteract to set
	 */
	public void setInteract(IInteraction lInteract) {
		this.lInteract = lInteract;
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

	public boolean onDown(WorldPoint sp) {
		super.onDown(sp);
		editorManager.onMoveView(circle, nowPoint);
		return true;
	}

	final Actor p = new Actor();
	
	@Override
	public ChainPiece onError(ChainPiece bp, ChainException e) {
		p.push(e.getLocation() + "," + e.getError());
		return bp;
	}

	@Override
	public ChainPiece onCancel(ChainPiece bp, ChainException e) {
		return null;
	}
	//3.Changing state
	//4.Termination
	//5.Local classes

	public class Test2 extends LocalViewStyle {
		public Test2() {
			super(R.drawable.shake);
		}
	}

	public class Test3 extends LocalViewStyle {
		public Test3() {
			super(R.drawable.quake);
		}
	}

	public class TestReset extends LocalViewStyle {
		public TestReset() {
			super(R.drawable.stop);
		}
	}

	public class TestSound extends LocalViewStyle {
		public TestSound() {
			super(R.drawable.sound1);
		}
	}

	public class TestTime extends LocalViewStyle {
		public TestTime() {
			super(R.drawable.timer);
		}
	}

	public class TestTime2 extends LocalViewStyle {
		public TestTime2() {
			super(R.drawable.timer_red);
		}
	}
	
	public class LocalActionStyle implements ActionStyle {

		@Override
		public WorldPoint pointOnAdd(WorldPoint raw) {
			return raw.round(100).plus(50);
		}
		
	}

	static Bitmap bm_bg;
	public class LocalViewStyle extends TapChainAndroidEdit.EditorView implements
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
			public LocalViewStyle() {
				this(null);
			}
	
			public LocalViewStyle(Integer _bm) {
				super();
	//			editor = e;
				setInteraction(getInteract());
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
				d.getPaint().setColor(0xaabbbbbb);
				d.getPaint().setStyle(Paint.Style.STROKE);
	//			d.getPaint().setStyle(Paint.Style.FILL);
	//			d.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
				d.getPaint().setStrokeWidth(3);
	//		d.getPaint().setShadowLayer(4, 4, 4, 0x80000000);
			}
	
			public LocalViewStyle localview_init(Integer bit) {
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
			public LocalViewStyle setError(boolean err) {
				error = err;
				return this;
			}
	
			@Override
			public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
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
			public LocalViewStyle setPercent(WorldPoint wp) {
				super.setPercent(wp);
				setSize(sizeOpened.multiply(0.01f * (float) getPercent().x).plus(sizeClosed)
						.plus(sizeClosed));
				return this;
			}
	
			@Override
			public LocalViewStyle setColor(int _color) {
				super.setColor(_color);
				d.getPaint().setColor(_color);
				return this;
			}
	
			//3.Changing state
			public LocalViewStyle open() {
				if (open)
					return this;
				Actor.EffecterSkelton<WorldPoint> move = new Actor.Mover().initEffect(
						new WorldPoint(2, 2).setDif(), 5);
				Actor.Sizer size = new Actor.Sizer().size_init(new WorldPoint(20, 0).setDif(), 5);
				Actor.Alphar alpha = new Actor.Alphar().alpha_init(20, 5);
				getSystemManager()._return(this)._child().add(move.disableLoop())
						.add(size.disableLoop()).add(alpha.disableLoop())._exit().save();
//				d.getPaint().setColor(0x33bbbbbb);
//				d.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
				open = true;
				return this;
	
			}
	
			public LocalViewStyle close() {
				if (!open)
					return this;
				Actor.EffecterSkelton<WorldPoint> move = new Actor.Mover().initEffect(new WorldPoint(-2,
						-2).setDif(), 10);
				Actor.Sizer size = new Actor.Sizer().size_init(new WorldPoint(-10, 0), 10);
				Actor.Alphar alpha = new Actor.Alphar().alpha_init(-20, 10);
				getSystemManager()._return(this)._child().add(move.disableLoop())
						.add(size.disableLoop()).add(alpha.disableLoop())._exit().save();
//				d.getPaint().setColor(0xffbbbbbb);
//				d.getPaint().setStyle(Paint.Style.STROKE);
				open = false;
				return this;
			}
	
			public LocalViewStyle toggle() {
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
			if (v != null && v instanceof LocalViewStyle) {
				((LocalViewStyle) v).toggle();
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
			int a = f1.getSize().x + f2.getSize().x;
			return getDistanceSq(f1.getCenter(), f2.getCenter()) > 4 * a * a;
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
				setInteraction(getInteract());
				init_image();
			}
			private void init_image() {
				bm_back = BitmapFactory.decodeResource(
						AndroidActor.getResources(), R.drawable.newframe);
				setSize(new WorldPoint(bm_back.getWidth(), bm_back.getHeight()));
			}
			public void init_animation(PieceManager maker) {
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

	public static class ConnectStyle extends TapChainAndroidEdit.Connect implements IEditAnimation {
			Actor.ViewActor start, stop;
			PackType starttype, stoptype;
			WorldPoint offset1, offset2;
			Paint paint;
			ScreenPoint sp1, sp2, sp12, sp21;
			PieceManager maker;
			public ConnectStyle() {
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
			public void init_animation(PieceManager maker) {
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


}
