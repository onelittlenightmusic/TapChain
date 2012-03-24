package org.tapchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.tapchain.AndroidPiece.*;
import org.tapchain.AnimationChain.*;
import org.tapchain.AnimationChain.BasicPiece;
import org.tapchain.AnimationChainManager.StatusHandler;
import org.tapchain.AnimationChainManager.LogHandler;
import org.tapchain.AnimationChainManager.TapChainPathView;
import org.tapchain.AnimationChainManager.TapChainPieceView;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainInConnector;
import org.tapchain.Chain.ChainOutConnector;
import org.tapchain.Chain.ChainPath;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.Chain.ChainPiece.PieceState;
import org.tapchain.ChainController.ControlCallback;
import org.tapchain.TapChainView.WritingView;

import org.tapchain.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.MotionEvent;

@SuppressWarnings("serial")
public class TapChainEditor implements TapChainPieceView {
	// EventHandler handler = null;
	static ModelActionCallback action = null;
	public static BasicPiece origin = null;// , focused = null;
	// static Chain chain_ = new Chain(Chain.THREAD_MODE, 0);
	static TapChainEditor back = null;
	static AnimationChainManager editorManager = new AnimationChainManager();
	AnimationChainManager userManager = new AnimationChainManager();
	static IWindow window = null;
	static TapChainEditorView selectedf = null;
	private static WorldPoint _p = null;
	static TreeMap<ChainPiece, TapChainEditorView> dictionary = new TreeMap<ChainPiece, TapChainEditorView>();
	static ConcurrentHashMap<ChainPath, BasicView> dictionary_path = new ConcurrentHashMap<ChainPath, BasicView>();
	static ArrayList<BasicPiece> plist = new ArrayList<BasicPiece>();
	PieceFactory pf = null;
	static ErrorHandler h = null;

	protected TapChainEditor(IWindow v) {
		window = v;
		pf = new PieceFactory(this);
		// origin =/* focused =*/ new BasicPiece();
		if (action == null) {
			action = new ModelActionCallback() {
				@Override
				public boolean redraw(String str) {
					return false;
				}
			};
		}
		editorManager.setEditor(this).SetFactory(pf).CreateChain();
		userManager.setEditor(this).SetFactory(pf).CreateChain().Get().setAutoEnd(false);
		ControlCallback b = new ControlCallback() {
			public boolean impl() {
				window.onDraw();
				return true;
			}
		};
		editorManager.SetCallback(b);
		userManager.SetCallback(b);
		editorManager.SetWindow(window);
		userManager.SetWindow(window);
		AndroidPiece.setWindow(window);
		init();
		return;
	}

	public void Create() throws ChainException {
	}

	public static class ChainEditorModel {
		public ChainEditorModel() {
		}
	}

	public void InitWindow(IWindow v) {
//		window = v;
//		ControlCallback b = new ControlCallback() {
//			public boolean impl() {
//				window.onDraw();
//				return true;
//			}
//		};
//		editorManager.SetCallback(b);
//		userManager.SetCallback(b);
//		editorManager.SetWindow(v);
//		userManager.SetWindow(v);
//		AndroidPiece.setWindow(v);
//		return;
	}

	public PieceFactory GetFactory() {
		return GetUserManager().GetFactory();
	}
	
	public void reset() {
		TreeMap<ChainPiece, TapChainEditorView> copy = new TreeMap<ChainPiece, TapChainEditorView>(dictionary);
		for(ChainPiece bp : copy.keySet())
			GetManager().remove(bp);
	}

	void init() {
		LogHandler l = new LogHandler() {
			@Override
			public void Log(String... s) {
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
		};
		
		editorManager.setLog(l);
		userManager.setLog(l);
		editorManager.setPieceView(this);
		editorManager.setPathView(new TapChainPathView() {
			@Override
			public void setPathView(ChainPath second, BasicView v) {
				TapChainEditor.setView(second, v);
			}

			public void unsetPathView(ChainPath p) {
				TapChainEditor.unsetView(p);
			}
		});
		BasicPiece ptmp = null;
		for(int c : Arrays.asList(0x8080ff80, 0x8080ff80, 0x80ffffff, 0x808080ff, 0x80ff8080)) {
			editorManager.add(new ColorViewEffect().color_init(c).setParentType(PackType.HEAP).boost())
			.args(ptmp = new BasicPiece()).Save();
			plist.add(ptmp);
		}
		
		//Initialization of PieceFactory
		userManager
				.makeBlueprint()
				.setOuterInstanceForInner(this)
				.New(AndroidImageView.class, new Value(R.drawable.star))
				.setview(Teststar.class)
				.Save()
				.New(AndroidImageView.class, new Value(R.drawable.star))
				.setview(LocalViewFlower.class)
				.Save()
				.New(AndroidImageView.class, new Value(R.drawable.heart_bright))
				.setview(Testheartbright.class)
				.Save()
				.New(AndroidImageView.class, new Value(R.drawable.heart))
				.setview(Testheart.class)
				.Save()
				.New(MoveViewEffect.class, new Value(new WorldPoint(3, 0).setDif()))
				.setview(Testright.class)
				.Save()
				.New(MoveViewEffect.class, new Value(new WorldPoint(0, 3).setDif()))
				.setview(Testdown.class)
				.Save()
				.New(MoveViewEffect.class, new Value(new WorldPoint(-3, 0).setDif()))
				.setview(Testleft.class)
				.Save()
				.New(MoveViewEffect.class, new Value(new WorldPoint(0, -3).setDif()))
				.setview(Testtop.class)
				.Save()
//				.New(MoveViewEffect.class, new ValueLimited(new WorldPoint(10, 0), 10))
//				.setview(Test.class)
//				.Save()
//				.New(MoveViewEffect.class, new ValueLimited(new WorldPoint(0, 10), 10))
//				.setview(Test.class)
//				.Save()
//				.New(MoveViewEffect.class, new ValueLimited(new WorldPoint(-10, 0), 10))
//				.setview(Test.class)
//				.Save()
//				.New(MoveViewEffect.class, new ValueLimited(new WorldPoint(0, -10), 10))
//				.setview(Test.class)
//				.Save()
				.New(SizeEffect.class, new Value(new WorldPoint(1, 1)))
				.setview(TestWiden.class)
				.Save()
				.New(SizeEffect.class, new Value(new WorldPoint(-1, -1)))
				.setview(TestShrink.class)
				.Save()
				.New(RotateEffect.class, new Value(10))
				.setview(TestRotate.class)
				.Save()
				.New(AlphaEffect.class, new Value(1))
				.setview(Test.class)
				.Save()
				.New(AlphaEffect.class, new Value(-1))
				.setview(Test.class)
				.Save()
				.New(AndroidPiece.AndroidImageView.class, new Value(R.drawable.star))
				.setview(Test.class)
				._return()
				.because(new TouchFilter() {
					public boolean filter(Object obj) {
						ScreenPoint __p = ((WorldPoint) obj)
								.getScreenPoint((WritingView) window);
						return __p.x < 30 && __p.y < 30;
					}
				})
				.Save()
				.New(new PieceBlueprint(MoveViewEffect.class) {
					@Override
					public void init_user(BasicPiece newinstance,
							AnimationChainManager maker, PieceFactory factory)
							throws InterruptedException {
						((EffectSkelton)newinstance).setParentType(PackType.HEAP);
						((MoveViewEffect) newinstance).move_init(new WorldPoint(10, 10).setDif(), 10);
						newinstance.setInPackType(PackType.FAMILY,
								ChainPiece.InputType.FIRST);
					}
				})
				.and(ResetEffect.class)
				.setview(Test.class).Save()
				.New(AndroidPiece.AndroidRecorder.class)
				.because(TouchFilter.class)
				._return()
				.child(SleepEffect.class)
				.and(ResetEffect.class)
				.setview(Test.class)
				.Save()
				.New(ResetEffect.class).setview(Test.class).Save()
				.New(ShakeFilter.class).setview(Test2.class).Save()
				.New(AndroidQuaker.class).setview(Test3.class).Save()
				.New(Sound2.class).setview(TestSound.class)//.Save()
			 .because(ShakeFilter.class).Save()
				.New(Sound3.class).setview(TestSound.class)//.Save()
			 /*.because(ShakeFilter.class)*/.Save()
				;
		// .New(AndroidPiece.AndroidRecognizer.class)
		// .setview(Test.class)
		// .Save();

		//Initialization of View
		editorManager
				.add(new TouchFilter())
				._mark()
				.func(new AndroidView() {
					Paint paint_ = new Paint();

					public void view_init() {
						try {
							setCenter((WorldPoint) pull());
						} catch (Exception e) {
							setCenter(new WorldPoint(100, 100));
						}
						setColor(selectedf == null ? Color.BLACK : Color.WHITE);
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
				.add(new Loop() {
					int t = 0;

					@Override
					public void effect_reset() throws ChainException {
						t = 0;
					}

					@Override
					public boolean effect_run() throws ChainException {
						getParentView().getSize().x += 30;
						getParentView().setAlpha(getParentView().getAlpha() - 10);
						return ++t < 10;
					}
				}/*.setLogLevel(true)*/)
				.then(new ResetEffect())
				._exit()
				._return()
				.add(circle)
				._child()
				.add(new ColorViewEffect().color_init(0xffffffff).disableLoop().boost())
				._exit()
				.add(((ChainPiece)i).setLogLevel(true))
				.then(new AndroidView() {
					Bitmap bm_star = BitmapFactory.decodeResource(
							((WritingView) window).getResources(), R.drawable.star);
					Paint paint = new Paint();
					@Override
					public void view_init() {
						setCenter(new WorldPoint(100, 100));
					}
	
					@Override
					public boolean view_user(Canvas canvas, ScreenPoint sp,
							WorldPoint size, int alpha) {
						canvas.drawBitmap(bm_star, sp.x, sp.y, paint);
						return true;
					}
				})
			._child()
			.add(new MoveViewEffect().move_init(new WorldPoint(10, 10).setDif(), 10))
			.then(new ResetEffect().setContinue(false))
			._exit()
				.Save();
		move.setName("MOVE_ENTRANCE");
		editorManager
		.add(move)
		.func(move_ef = (MoveViewEffect) new MoveViewEffect() {
			@Override
			public boolean effect_run() throws ChainException {
				super.effect_run();
				if(getTarget() instanceof TapChainEditorView)
					checkAndAttach((TapChainEditorView)getTarget(), null);
				return false;
			}
		}.move_init(_p==null?new WorldPoint(0,0):_p, 1).setParentType(PackType.HEAP).boost())
		.Save();
		final BasicPiece p = new BasicPiece();
		editorManager
			.add(new AndroidPiece.AndroidAlert())
			.args(p).Save();
		h = new ErrorHandler() {
				public ChainPiece ErrorHandler(ChainPiece bp, ChainException e) {
					p.push(e.location + "," + e.err);
					return bp;
				}

				@Override
				public ChainPiece ErrorCanceller(ChainPiece bp, ChainException e) {
					return null;
				}
			};
		editorManager.setError(h);
		userManager.setError(h);
	}
	static BasicPiece move = new BasicPiece();
	static MoveViewEffect move_ef = null;
	
	public class Test extends LocalView {
		public Test() {
			super(R.drawable.gu);
		}
	}

	public class Teststar extends LocalView {
		public Teststar() {
			super(R.drawable.star);
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

	public class Testtop extends LocalView {
		public Testtop() {
			super(R.drawable.move_top);
		}
	}

	public class Testdown extends LocalView {
		public Testdown() {
			super(R.drawable.move_down);
		}
	}

	public class Testleft extends LocalView {
		public Testleft() {
			super(R.drawable.move_left);
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

	public class Testright extends LocalView {
		public Testright() {
			super(R.drawable.move_right);
		}
	}

	public class TestSound extends LocalView {
		public TestSound() {
			super(R.drawable.sound1);
		}
	}
	
	public class TestWiden extends LocalView {
		public TestWiden() {
			super(R.drawable.wide);
		}
	}

	public class TestShrink extends LocalView {
		public TestShrink() {
			super(R.drawable.shrink);
		}
	}

	public class TestRotate extends LocalView {
		public TestRotate() {
			super(R.drawable.rotate);
		}
	}

	public class Sound2 extends AndroidPiece.AndroidSound2 {
		Sound2() {
			super(R.raw.drip);
			setInPackType(PackType.EVENT, InputType.FIRST);
		}

		@Override
		public boolean reset_sound_impl() {
			super.reset_sound_impl();
//		resetInPathPack(PackType.HEAP);
			rate = 1f;//(Float) pull();
			resetInPathPack(PackType.EVENT);
			return true;
		}
	}

	public static class Sound3 extends AndroidPiece.AndroidSound {
		Sound3() {
			super(R.raw.drip);
			setInPackType(PackType.EVENT, InputType.FIRST);
		}

		@Override
		public boolean reset_sound_impl() {
			super.reset_sound_impl();
//		resetInPathPack(PackType.HEAP);
			resetInPathPack(PackType.EVENT);
			return true;
		}
	}

	public static class Sound extends AndroidPiece.AndroidSound2 {
		Sound() {
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

	public static class AddPiece extends BasicPiece {
		AddPiece() {
			super();
			boost();
			setInPackType(PackType.PASSTHRU, ChainPiece.InputType.FIRST);
		}

		public boolean effect_run() throws ChainException, InterruptedException {
			// __exec_and_log(ChainEditor.addPiece((BasicPiece)getReactor().get(0),
			// (BasicView)pull()), "AddPiece");
			return false;
		}
	}

	public static void setModelAction(ModelActionCallback ma) {
		action = ma;
	}

	public abstract static class ModelActionCallback {
		public boolean collide() {
			return false;
		};

		public boolean append() {
			return false;
		};

		public boolean split() {
			return false;
		};

		public boolean start() {
			return false;
		};

		public boolean end() {
			return false;
		};

		public abstract boolean redraw(String str);
	}

	public boolean kickDraw() {
		editorManager.Get().Kick();
		return true;
	}

	public BasicView getView(ChainPiece bp) {
		if (dictionary.get(bp) != null)
			return (BasicView)dictionary.get(bp);
		// default parameter.
		return null;
	}

	public static BasicView getView(ChainPath path) {
		if (dictionary_path.get(path) != null)
			return dictionary_path.get(path);
		return null;
	}

	public void setView(final ChainPiece cp2, final TapChainEditorView _view) {
		dictionary.put(cp2, _view);
		_view.setMyTapChain(cp2);
		if (cp2 == origin)
			return;
		cp2.setStatusHandler(new StatusHandler() {
			@Override
			public synchronized void getStateAndSetView(int state) {
				if(state < PieceState.values().length)
					plist.get(state).push(_view);
			}

			@Override
			public void tickView() {
				_view.tickView();
			}
		});
		cp2.setError(h);
	}

	public static void setView(ChainPath path, BasicView _view) {
		dictionary_path.put(path, _view);
		path.setStatusHandler(new StatusHandler() {
			@Override
			public void tickView() {
//				_view.tickView();
//				Log.w("ChainPath", "ticked");
			}
			
			@Override
			public void getStateAndSetView(int state) {
			}
		});
		return;
	}
	
	public void moveView(TapChainViewI v) {
		move_ef.move_init(_p==null?new WorldPoint(0,0):_p, 1);
		move.push(v);
		kickDraw();
		return;
	}

	public void unsetView(ChainPiece bp) {
		if(bp == null) 
			return;
		bp.setStatusHandler(null);
		BasicView v = getView(bp);
		if(v instanceof TapChainEditorView)
			((TapChainEditorView)v).unsetMyTapChain();
		dictionary.remove(bp);
		if(v == null)
			return;
		v.finish(false);
		return;
	}

	public static void unsetView(ChainPath path) {
		if(path == null) 
			return;
		BasicView v = getView(path);
		dictionary_path.remove(path);
		if(v == null)
			return;
		v.finish(false);
		return;
	}

	public void refreshView(ChainPiece bp, ChainPiece obj) {
		TapChainEditorView v = dictionary.get(bp);
		dictionary.remove(bp);
		if (((BasicPiece)bp).compareTo((BasicPiece)obj) > 0) {
			bp.mynum = ++AnimationChain.num;
		}
		dictionary.put(bp, v);
	}

	public static BasicPiece GetRootModel() {
		return origin;
	}

	public static BasicPiece GetCurrentModel() {
		return origin;// focused;
	}

	public BasicPiece Focus(BasicPiece m) {
		if (getView(m) instanceof LocalView) {
			((LocalView) getView(m)).open();
		}
		return GetRootModel();// focused;
	}

	public enum EditMode {
		ADD, REMOVE, RENEW
	}

	static EditMode editmode = EditMode.ADD;

	public static boolean Mode(EditMode mode) {
		editmode = mode;
		return true;
	}

	static AndroidView circle = new AndroidCircle();
	public boolean onDown(WorldPoint sp) {
		_p = sp;
		GetManager().Get().TouchOn(_p);
		selectedf = TouchPiece(_p);
		moveView(circle);
		return true;
	}

	public TapChainEditorView TouchPiece(WorldPoint sp) {
		for (Entry<ChainPiece, TapChainEditorView> f : dictionary.entrySet()) {
			TapChainEditorView e = f.getValue();
			if (e.getWorldRect().contains(sp.x, sp.y))
				return e;
		}
		return null;
	}

	public static boolean up() {
		selectedf = null;
		return true;
	}

	public boolean onUp() {
		GetManager().Get().TouchOff();
		if (selectedf == null)
			return false;
		if(checkAndDelete(selectedf)) return up();
		checkAndAttach((TapChainEditorView)selectedf, null);
		return up();
	}
	
	private boolean checkAndAttach(TapChainEditorView target, WorldPoint d) {
//		BasicPiece target = v.getMyTapChain();
		if(target == null)
			return false;
		for (TapChainEditorView bp : dictionary.values())
			if (Attack(target, bp, d))
				return true;
		return false;
	}
	
	private boolean checkAndDelete(TapChainEditorView v) {
		if(((BasicView)v).getCenter().getScreenPoint(window).isContained(new Rect(0, window.window_size.y-100, 100, window.window_size.y))) {
			GetManager().remove(v.getMyTapChain());
			return true;
		}
		return false;
	}

	public boolean onFling(final int vx, final int vy) {
		GetManager().Get().Fling(new WorldPoint(vx, vy));
		if (selectedf != null) {
			BasicPiece v = (BasicPiece) selectedf;
			ValueLimited vl = new ValueLimited(1);
			GetManager()._return(v)._child()
					.add(new Accel().disableLoop(), vl.disableLoop())._exit()
					.Save();
			vl.setValue(new WorldPoint(vx, vy).setDif());
			return up();
		}
		Log.w("Fling", "NULL!");
//		final WritingView v = (WritingView) window;
		GetManager().add(new BasicPiece() {
			float delta = 0.03f;
			int t = 0;

			@Override
			public boolean effect_run() {
				window.move((int) (delta * -vx), (int) (delta * -vy));
				delta -= 0.003f;
				return ++t < 10;
			}
		}).Save();
		return up();
	}

	public boolean onLongPress() {
		GetManager().Get().LongPress();
		if (selectedf != null && selectedf instanceof LocalView) {
			((LocalView) selectedf).toggle();
		}
		selectedf = null;
		return up();

	}

	public boolean onScroll(final WorldPoint vp, final WorldPoint wp) {
		GetManager().Get().Move(vp);
		if (selectedf != null) {
			onClear();
			selectedf.setCenter(wp);
//			WorldPoint d = checkReleasing(selectedf.getMyTapChain());
//			if(d != DirOffset.NULL)
//				selectedf.addCenter(d);
				
		} else {
			window.move(-vp.x(), -vp.y());
		}
//		if (_p == null) {
//			return false;
//		}
		kickDraw();
		return true;
	}

	public static class DirOffset {
		static WorldPoint TOP = new WorldPoint(0, 50).setDif(),
			RIGHT = new WorldPoint(-50, 0).setDif(),
			BOTTOM = new WorldPoint(0, -50).setDif(),
			LEFT = new WorldPoint(50, 0).setDif(),
			NULL = null;
	}
	public WorldPoint checkReleasing(BasicPiece bp) {
		EditorView v1 = (EditorView) getView(bp), v2 = null;
		if(!bp.hasInPath(PackType.FAMILY))
			return DirOffset.NULL;
		try {
			v2 = (EditorView) getView(bp.getParent(PackType.FAMILY));
		} catch (ChainException e) {
			return DirOffset.NULL;
		}
		if(v1.getInteraction().checkOut(v1, v2)) {
		Rect r = new Rect(v2.getScreenRect());
//		if(interact.checkTouch(v1, v2))
//			return 
		r.bottom += v1.getSize().y;
		if(r.contains(v1.getScreenRect()))
				return DirOffset.BOTTOM;
		r.right += v1.getSize().x;
		if(r.contains(v1.getScreenRect()))
				return DirOffset.RIGHT;
		r.top -= v1.getSize().y;
		if(r.contains(v1.getScreenRect()))
				return DirOffset.TOP;
		r.left -= v1.getSize().x;
		if(r.contains(v1.getScreenRect()))
				return DirOffset.LEFT;
		}
		return DirOffset.NULL;
	}
	
	public static boolean onShowPress(MotionEvent e) {
		return up();
	}

	public boolean onSingleTapConfirmed() {
		// touching but against "kicked" piece
		switch (editmode) {
		case REMOVE:
			GetManager().remove(TouchPiece(_p).getMyTapChain());
			up();
			editmode = EditMode.ADD;
			return false;
		case RENEW:
			GetManager().restart(TouchPiece(_p).getMyTapChain());
			up();
			editmode = EditMode.ADD;
			return false;
		default:
		}
		if (selectedf != null && selectedf instanceof LocalView) {
			LocalView v = (LocalView) selectedf;
			if (!v.open) {
				v.open();
				return false;
			}
			return true;
		}
		// open area
		if (selectedf == null)
			return true;
		if (!TouchParent(_p)) {
			// GoParent();
			return false;
		}

		return true;
	}

	public static boolean onClear() {
		_p = null;
		return true;
	}

	public AnimationChainManager GetManager() {
		return editorManager.NewSession();
	}

	public AnimationChainManager GetUserManager() {
		return userManager.NewSession();
	}

	public Rect getWorldRect() {
		WorldPoint sp = getView(GetCurrentModel()).getCenter();
		WorldPoint wp = getView(GetCurrentModel()).getCenter().plus(
				getView(GetCurrentModel()).getSize());

		return new Rect((int) sp.x, (int) sp.y, (int) wp.x, (int) wp.y);
	}

	public static Rect getWorldRect(BasicView view) {
		WorldPoint size = view.getSize().divide(2);
		WorldPoint sp = view.getCenter().sub(size);
		WorldPoint wp = view.getCenter().plus(size);
		return new Rect((int) sp.x, (int) sp.y, (int) wp.x, (int) wp.y);
	}

	public void Compile() {
		userManager.Get().getManager().reset();
		userManager.Get().Set(new ControlCallback() {
			public boolean impl() {
				kickDraw();
				return true;
			}
		});
		return;
	}

	public void Start() {
		if (userManager.Get() == null) {
			return;
		}
		action.start();
		userManager.Get().getManager().start();
		return;
	}

	public void Show(Canvas canvas) {
		// GetCurrentModel().show_this(canvas);
		GetManager().Get().Show(canvas);
		GetUserManager().Get().Show(canvas);
	}

	public boolean TouchParent(WorldPoint sp) {
		return getWorldRect().contains(sp.x, sp.y);
	}

	public ChainPiece Crash() {
		for (ChainPiece f1 : GetCurrentModel().member) {
			for (ChainPiece f2 : GetCurrentModel().member) {
				try {
					f1.appendTo(PackType.PASSTHRU, f2, PackType.PASSTHRU);
				} catch (ChainException e) {
					GetManager().Error(e);
				}
			}
		}
		return origin;// focused;
	}

	public class Reform extends Loop {
		BasicPiece f = null;
		WorldPoint pt = null;

		Reform(BasicPiece _f, WorldPoint _pt) {
			super();
			f = _f;
			pt = _pt;
		}

		@Override
		public boolean effect_run() throws ChainException, InterruptedException {
			// start reforming toward upper(prev) functions[recursive invocation]
			reformTo(getView(f), true, pt);
			// start reforming toward lower(next) functions[recursive invocation]
			reformTo(getView(f), false, pt);
			return false;
		}
	}

	void reformTo(BasicPiece bp, final boolean UpDown, final WorldPoint pt) {
		WorldPoint pt_ = pt;
		if (pt_ != null) {
			WorldPoint s = pt_.sub(getView(bp).getCenter());
			GetManager()._return(getView(bp))
			// .child(
					._child().add(new Accel().disableLoop())._exit().Save();

		} else {
			pt_ = getView(bp).getCenter();
			int i = 0, i_max = bp.getOutPack(PackType.FAMILY).array.size();
			for (ChainOutConnector _cp : bp.getOutPack(PackType.FAMILY).array) {
				ChainInConnector cip = _cp.getPartner();
				if (cip == null)
					break;
				BasicPiece part = (BasicPiece) cip.getParent();
				if (part == null)
					break;
				if (dictionary.get(part) == null)
					break;
				reformTo(part, UpDown, pt_.plus(new WorldPoint(30 * (-i_max + 2 * i++),
						UpDown ? -50 : 50)));
			}
		}
		return;
	}

	public enum ConnectType {
		INCLUDED, INCLUDING, TOUCH_LEFT, TOUCH_RIGHT, TOUCH_TOP, TOUCH_BOTTOM, NULL, RELEASING, DISCONNECT
	}
	public boolean connect(ChainPiece chainPiece, ChainPiece chainPiece2, ConnectType type) {
		switch(type) {
		case INCLUDED:
			return null != GetManager()
			.append(chainPiece, PackType.FAMILY, chainPiece2, PackType.FAMILY);
		case INCLUDING:
			return null != GetManager()
			.append(chainPiece2, PackType.FAMILY, chainPiece, PackType.FAMILY);
		case TOUCH_TOP:
			return null != GetManager()
			.append(chainPiece2, PackType.EVENT, chainPiece, PackType.EVENT, true);
		case TOUCH_BOTTOM:
			return null != GetManager()
			.append(chainPiece, PackType.EVENT, chainPiece2, PackType.EVENT, true);
		case TOUCH_LEFT:
			return null != GetManager()
			.append(chainPiece2, PackType.HEAP, chainPiece, PackType.HEAP, true);
		case TOUCH_RIGHT:
			return null != GetManager()
			.append(chainPiece, PackType.HEAP, chainPiece2, PackType.HEAP, true);
		case DISCONNECT:
			GetManager().__disconnect(chainPiece, chainPiece2);
			return true;
		}
		return true;
	}
	
//	public static boolean checkConnect(BasicPiece bp, BasicPiece target, ConnectType type) {
//		return bp.isConnectedTo(target);
//	}
//	
	public ConnectType checkAttackType(TapChainEditorView v1, TapChainEditorView v2, WorldPoint dir) {
//		AndroidView v1 = (AndroidView) getView(bp);
//		AndroidView v2 = (AndroidView) getView(target);
		ChainPiece bp = v1.getMyTapChain();
		ChainPiece target = v2.getMyTapChain();
		// #20111011 Connected/or-not judgment
		if (bp.isConnectedTo(target)) {
			if (v1.getInteraction().checkLeave(v1, v2)) {
				return ConnectType.DISCONNECT;
//			} else if (bp.isAppendedTo(target, PackType.FAMILY) && !interact.checkInclude(v1, v2)) {
//				return ConnectType.RELEASING;
			}
			return ConnectType.NULL;
		} else {
			// #20111011
			if (v1.getInteraction().checkIn(v1, v2)) {
				// containing and not connected
				GetManager().refreshView(bp, target);
				return ConnectType.INCLUDED;
			}
	
			// neither containing nor colliding
			if (!v1.getInteraction().checkTouch(v1, v2)) {
				return ConnectType.NULL;
			}
		}
	
		return getConnectType(dir);
	}
	
	public static ConnectType getConnectType(WorldPoint dir) {
		if (null == dir)
			return ConnectType.NULL;
		int dxy = dir.x + dir.y;
		int d_xy = -dir.x + dir.y;
		if (dxy > 0) {
			if(d_xy > 0) return ConnectType.TOUCH_TOP;
			else return ConnectType.TOUCH_LEFT;
		} else {
			if(d_xy > 0) return ConnectType.TOUCH_RIGHT;
			else return ConnectType.TOUCH_BOTTOM;
		}
	}
	public boolean Attack(TapChainEditorView bp, TapChainEditorView f, WorldPoint dir) {
		if (bp == f) {
			return false;
		}
		ConnectType t = checkAttackType(bp, f, dir);
		if(t == ConnectType.NULL){
			return false;
		} else {
			if(!connect(bp.getMyTapChain(), f.getMyTapChain(), t))
				return false;
//			BasicView _v = bp;
			if (bp instanceof EventHandler) {
				((EventHandler) bp).onFrameConnecting(bp, f);
			}
		}

		return true;
	}

	public class Accel extends MoveViewEffect {
		float delta = 0.03f;
		int j = 0;
		WorldPoint wp = null;
		WorldPoint initial = null;
		BasicPiece bp = null;

		@Override
		public void effect_reset() throws ChainException {
			WorldPoint dummy = new WorldPoint();
			move_init(dummy,1);
			super.effect_reset();
			j = 0;
			delta = 0.03f;
			if (initial == null)
				__exec(wp = (WorldPoint) pull(), "Accel#reset");
			return;
		}

		@Override
		public boolean effect_run() throws ChainException {
			WorldPoint d = new WorldPoint((int) (wp.x * delta), (int) (wp.y * delta)).setDif();
			move_init(d,1);
			delta -= 0.003f;
			boolean rtn = ++j < 10;
			super.effect_run();
			if(checkAndAttach(((TapChainEditorView)getTarget()), d)){
//				clearInputHeap();
				return false;
			}
//			if (!rtn)
//				push(null);
			return rtn;
		}
	}

	public interface IWindow {
		WorldPoint window_orient = new WorldPoint();
		WorldPoint window_size = new WorldPoint();
		public void move(int vx, int vy);
		void onDraw();
	}

	public WorldPoint getOrientAsWorld(IWindow i) {
		return (new ScreenPoint(IWindow.window_size.x / 2,
				IWindow.window_size.y / 2)).getWorldPoint(i);
	}
	
	public static class EditorView extends AndroidView implements
		TapChainEditorView {
		TapChainInteraction interact = null;
		public EditorView() {
			super();
		}

		@Override
		public final void setMyTapChain(ChainPiece mytapchain) {
			this.mytapchain = mytapchain;
		}
		
		@Override
		public final void unsetMyTapChain() {
			this.mytapchain = null;
		}

		@Override
		public final ChainPiece getMyTapChain() {
			return mytapchain;
		}

		private ChainPiece mytapchain = null;

		@Override
		public void tickView() {
		}
		
		protected EditorView setInteraction(TapChainInteraction i) {
			interact = i;
			return this;
		}
		
		public TapChainInteraction getInteraction() {
			return interact;
		}
	}

	final TapChainInteraction i = new LocalInteraction();
	public class LocalView extends EditorView implements
			TapChainEditor.ClosedView, TapChainEditor.EventHandler, TapChainEditor.FlowView,
			Serializable {
		Bitmap bitmap = BitmapFactory.decodeResource(
				((WritingView) window).getResources(), R.drawable.newframe2);
		TapChainEditor.IWindow v = window;
		Paint _paint = new Paint();
		Paint paint2 = new Paint();
		int fontsize = 20;
		public Bitmap bm_bg = null, bm_bg_anim = null, bm_fg = null, bm_cn = null,
				bm_star = null;
		Integer frontview = null;
		String name;
		WorldPoint sizeOpened = new WorldPoint(300, 300), sizeClosed = null;
		ShapeDrawable d;
		RectF tickCircleRect = new RectF();
		int tickCircleRadius = 40;
		float sweep = 0f;

		public LocalView() {
			this(null);
		}

		public LocalView(Integer _bm) {
			super();
//			editor = e;
			setInteraction(i);
			_paint.setAntiAlias(true);
			_paint.setAlpha(255);
			_paint.setTextSize(fontsize);
			_paint.setColor(0xff999999);
			_paint.setTextAlign(Paint.Align.CENTER);

			bm_bg = BitmapFactory.decodeResource(
					((WritingView) window).getResources(), R.drawable.newframe);
			sizeClosed = new WorldPoint(bm_bg.getWidth() / 2, bm_bg.getHeight() / 2);
			bm_bg_anim = BitmapFactory.decodeResource(
					((WritingView) window).getResources(), R.drawable.newframe);
			bm_cn = BitmapFactory.decodeResource(
					((WritingView) window).getResources(), R.drawable.bubble4);
			bm_star = BitmapFactory.decodeResource(
					((WritingView) window).getResources(), R.drawable.star);
//			_size = sizeClosed.clone();
			if (_bm != null) {
				localview_init(_bm);
			}
			// _Size = sizeClosed.clone();
			setPercent(new WorldPoint(0, 0));
			setAlpha(255);

			// _kick = new BasicKicker();
			setWindow(v);
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
			Bitmap tmp = BitmapFactory.decodeResource(
					((WritingView) window).getResources(), bit);
			bm_fg = tmp;
			frontview = bit;
			return this;
		}
		
		boolean open = false;

		public LocalView open() {
			if (open)
				return this;
			MoveViewEffect move = new MoveViewEffect().move_init(
					new WorldPoint(2, 2).setDif(), 10);
			SizeEffect size = new SizeEffect().size_init(new WorldPoint(10, 0).setDif(), 10);
			AlphaEffect alpha = new AlphaEffect().alpha_init(20, 10);
			GetManager()._return(this)._child().add(move.disableLoop())
					.add(size.disableLoop()).add(alpha.disableLoop())._exit().Save();
			open = true;
			return this;

		}

		public LocalView close() {
			if (!open)
				return this;
			MoveViewEffect move = new MoveViewEffect().move_init(new WorldPoint(-2,
					-2).setDif(), 10);
			SizeEffect size = new SizeEffect().size_init(new WorldPoint(-10, 0), 10);
			AlphaEffect alpha = new AlphaEffect().alpha_init(-20, 10);
			GetManager()._return(this)._child().add(move.disableLoop())
					.add(size.disableLoop()).add(alpha.disableLoop())._exit().Save();
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

		public LocalView setError(boolean err) {
			error = err;
			return this;
		}

		@Override
		public void view_init() throws ChainException {
			 if(bm_fg == null) {
				 bm_fg = BitmapFactory.decodeResource(((WritingView)
				 window).getResources(), (Integer)pull());
			 }
			return;
		}

		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			if(open) {
				d.setBounds(getScreenRect());
				d.draw(canvas);
			}
			Bitmap bm_back = (attack || attacked) ? bm_bg_anim : bm_bg;
			drawBitmapCenter(canvas, bm_back, sp, _paint);
			// Draw a foreground
			if (bm_fg != null) {
				drawBitmapCenter(canvas, bm_fg, sp, _paint);
			}
			tickCircleRect.set(sp.x()-tickCircleRadius, sp.y()-tickCircleRadius, sp.x()+tickCircleRadius, sp.y()+tickCircleRadius);
			canvas.drawArc(tickCircleRect, -90f, sweep%720f-450f, false, d.getPaint());
			return true;
		}

		private void drawBitmapCenter(Canvas canvas, Bitmap bm, ScreenPoint center,
				Paint paint) {
			canvas.drawBitmap(bm, center.x - bm.getWidth() / 2,
					center.y - bm.getHeight() / 2, paint);
		}

		@Override
		public void user_rect(HeroicPoint pt, WorldPoint size, Rect r) {
			HeroicPoint sp = pt.sub(sizeClosed);
			r.set(sp.x(), sp.y(), sp.x() + size.x, sp.y() + size.y);
		}

		@Override
		public void user_rectF(HeroicPoint pt, WorldPoint size, RectF rf) {
			HeroicPoint sp = pt.sub(sizeClosed);
			rf.set(sp.x(), sp.y(), sp.x() + size.x, sp.y() + size.y);
		}

		@Override
		public boolean view_iconnect_impl(Canvas canvas, ChainInConnector i,
				BasicPiece f1) {
			if (bm_cn == null) {
				return true;
			}
			ScreenPoint s3 = getView(f1).getCenter().plus(new WorldPoint(0, 10))
					.getScreenPoint(v);
			canvas.drawBitmap(bm_cn, s3.x, s3.y, _paint);

			return true;
		}

		@Override
		public boolean view_oconnect_impl(Canvas canvas, ChainOutConnector o,
				BasicPiece f1) {
			if (bm_cn == null) {
				return true;
			}
			return true;
		}

		boolean attack = false;
		boolean attacked = false;

		@Override
		public boolean onFrameConnecting(TapChainViewI fattack, TapChainViewI fdefend) {
			final LocalView f1 = (LocalView) fattack, f2 = (LocalView) fdefend;
			f1.attack = true;
			f2.attacked = true;
			return true;
		}

		@Override
		public boolean onFrameDisconnecting(TapChainViewI fremove, TapChainViewI fleft) {
			return false;
		}

		@Override
		public boolean flowview_impl(Canvas canvas, BasicPiece f) {
			int j = 0;
			ScreenPoint sp = getView(f).getCenter().getScreenPoint(v);
			for (ChainInConnector i : f.getInPack(PackType.PASSTHRU).array) {
				Axon<?> q;
				try {
					q = i.getQueue();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
//				if (q != null) {
					for (int i1 = 0; i1 < q.size(); i1++) {
						canvas.drawBitmap(bm_cn, sp.x + _size.x - bm_cn.getWidth() / 2,
								sp.y + _size.x - j * 10 - bm_cn.getHeight() / 2, _paint);
						j++;
					}
//				}
			}
			return false;
		}

		public boolean view_iconnect_open_impl(Canvas canvas, ChainInConnector i,
				BasicPiece c1) {
			ScreenPoint sp1 = getView(c1).getCenter().getScreenPoint(v);
			ScreenPoint sp2 = getView(c1).getCenter()
					.plus(getView(c1).getSize().divide(2)).getScreenPoint(v);
			canvas.drawBitmap(bm_cn, sp2.x - bm_cn.getWidth() / 2,
					sp1.y - bm_cn.getHeight() / 2, _paint);
			return true;
		}

		public boolean view_oconnect_open_impl(Canvas canvas, ChainOutConnector o,
				BasicPiece c1) {
			ScreenPoint sp1 = getView(c1).getCenter().plus(getView(c1).getSize())
					.getScreenPoint(v);
			ScreenPoint sp2 = getView(c1).getCenter()
					.plus(getView(c1).getSize().divide(2)).getScreenPoint(v);
			canvas.drawBitmap(bm_cn, sp2.x - bm_cn.getWidth() / 2,
					sp1.y - bm_cn.getHeight() / 2, _paint);
			return true;
		}

		@Override
		public boolean onConnectConnecting(ConnectClosedView cattack,
				ConnectClosedView cdefend) {
			return false;
		}

		@Override
		public boolean onConnectDisconnecting(ChainInConnector i, ChainOutConnector o,
				ConnectClosedView cremove, ConnectClosedView cleft) {
			return false;
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

		@Override
		public void tickView() {
			sweep += 20f;
		}
	}
	

	public class LocalInteraction extends SimpleRelationFilter implements
			TapChainInteraction {
		CountDownLatch c = new CountDownLatch(1);
		LocalInteraction() {
			super();
		}

		@Override
		public boolean checkTouch(TapChainViewI f1, TapChainViewI f2) {
			boolean rtn = (((AndroidView) f1).getScreenRect().intersect(((AndroidView) f2).getScreenRect()));
			if(rtn) c.countDown();
			return rtn;
		}

		@Override
		public boolean checkLeave(TapChainViewI f1, TapChainViewI f2) {
			return !(((AndroidView) f1).getScreenRect().intersect(((AndroidView) f2).getScreenRect()));
		}

		@Override
		public boolean checkSplit(BasicView f1, BasicView f2) {
			int a = f1._size.x + f2._size.x;
			return getDistanceSq(f1._wp, f2._wp) > 4 * a * a;
		}

		@Override
		public boolean checkIn(TapChainViewI v1, TapChainViewI v2) {
			boolean rtn = ((AndroidView)v2).getScreenRect().contains(((AndroidView)v1).getScreenRect());
			if(rtn) c.countDown();
			return rtn;
		}

		public boolean checkOut(TapChainViewI v1, TapChainViewI v2) {
			return !((AndroidView)v2).getScreenRect().contains(((AndroidView)v1).getScreenRect());
		}
		
		@Override
		public boolean relation_impl(BasicView a, BasicView b) throws InterruptedException {
			c.await();
			c = new CountDownLatch(1);
			return true;//checkTouch(a, b);
		}

		int getDistanceSq(WorldPoint sp1, WorldPoint sp2) {
			return (sp1.x - sp2.x) * (sp1.x - sp2.x) + (sp1.y - sp2.y)
					* (sp1.y - sp2.y);
		}
	}

	public class LocalViewFlower extends EditorView {
		Paint p = new Paint();
		Bitmap bm_back;
		public LocalViewFlower() {
			super();
			setInteraction(i);
			setWindow(window);
			bm_back = BitmapFactory.decodeResource(
					((WritingView) window).getResources(), R.drawable.newframe);
			setSize(new WorldPoint(bm_back.getWidth(), bm_back.getHeight()));
		}
		@Override
		public void view_init() throws ChainException {
			return;
		}
		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			drawBitmapCenter(canvas, bm_back, sp, p);
			return true;
		}

		private void drawBitmapCenter(Canvas canvas, Bitmap bm, ScreenPoint center,
				Paint paint) {
			canvas.drawBitmap(bm, center.x - bm.getWidth() / 2,
					center.y - bm.getHeight() / 2, paint);
		}
	}
	
	public interface FlowView {
		public boolean flowview_impl(Canvas canvas, BasicPiece f);
	}

	interface ConnectClosedView {
		public boolean view_iconnect_impl(Canvas canvas, ChainInConnector i,
				BasicPiece f1);

		public boolean view_oconnect_impl(Canvas canvas, ChainOutConnector o,
				BasicPiece f1);
	}

	public interface ClosedView extends ConnectClosedView {
	}

	public interface TapChainInteraction {
		public boolean checkTouch(TapChainViewI v1, TapChainViewI v2);
		public boolean checkLeave(TapChainViewI f1, TapChainViewI f2);
		public boolean checkSplit(BasicView f1, BasicView f2);
		public boolean checkIn(TapChainViewI v1, TapChainViewI v2);
		public boolean checkOut(TapChainViewI v1, TapChainViewI v2);
	}

	public interface FrameEventHandler {
		public boolean onFrameConnecting(TapChainViewI bp, TapChainViewI f);

		public boolean onFrameDisconnecting(TapChainViewI fremove, TapChainViewI fleft);
	}

	public interface ConnectEventHandler {
		public boolean onConnectConnecting(ConnectClosedView cattack,
				ConnectClosedView cdefend);

		public boolean onConnectDisconnecting(ChainInConnector i, ChainOutConnector o,
				ConnectClosedView cremove, ConnectClosedView cleft);
	}

	public interface EventHandler extends FrameEventHandler, ConnectEventHandler {
	}
	
	public interface TapChainEditorView extends TapChainViewI {
		public void setMyTapChain(ChainPiece cp2);
		public Rect getWorldRect();
		public void unsetMyTapChain();
		public ChainPiece getMyTapChain();
		public void tickView();
		public TapChainInteraction getInteraction();
	}

}
