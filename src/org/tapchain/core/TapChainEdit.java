package org.tapchain.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.tapchain.core.ActorChain.*;
import org.tapchain.core.ActorManager.IPathEdit;
import org.tapchain.core.ActorManager.IPieceEdit;
import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Blueprint.TmpInstance;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.ChainPiece.PieceState;
import org.tapchain.core.PathPack.ChainInPathPack;


@SuppressWarnings("serial")
public class TapChainEdit implements IPieceEdit, IPathEdit, IControlCallback {
	IWindow win = null;
	IWindowCallback winCall = null;
	protected ActorManager editorManager = new ActorManager();
	protected ActorManager userManager = new ActorManager();
	protected BlueprintManager blueprintManager = null;;
	Factory userFactory = null;
	public IPieceView nowPiece = null;
	WorldPoint nowPoint = null;
	public TreeMap<IPiece,IPieceView> dictPiece = new TreeMap<IPiece, IPieceView>();
	ConcurrentHashMap<IPath, IPathView> dictPath = new ConcurrentHashMap<IPath, IPathView>();
	ArrayList<Actor> plist = new ArrayList<Actor>();
	protected IErrorHandler errHandle = null;

	//1.Initialization
	protected TapChainEdit() {
		if (winCall == null) {
			winCall = new IWindowCallback() {
				@Override
				public boolean redraw(String str) {
					return false;
				}
			};
		}
		userFactory = new Factory(userManager);
		blueprintManager = new BlueprintManager(userFactory);
		editorManager./*setFactory(userFactory).*/createChain(20);
		userManager./*setFactory(userFactory).*/createChain(50).getChain().setAutoEnd(false);
		editorManager.SetCallback(this);
		userManager.SetCallback(this);
		userManager.setPieceEdit(this);
		userManager.setPathEdit(this);
		return;
	}

	void init() {
		Actor ptmp = null;
		for(int c : Arrays.asList(0xff80ff80, 0xff80ff80, 0xffffffff, 0xff8080ff, 0xffff8080)) {
			editorManager.add(new Actor.Colorer().color_init(c).setParentType(PackType.HEAP).boost())
			.teacher(ptmp = new Actor()).save();
			plist.add(ptmp);
		}
		
		move.setName("MOVE_ENTRANCE");
		editorManager
		.add(move)
		.student(move_ef = (Actor.Mover) new Actor.Mover() {
			@Override
			public boolean actorRun() throws ChainException {
				super.actorRun();
				if(getTarget() instanceof IPieceView)
					checkAndAttach((IPieceView)getTarget(), null);
				return false;
			}
		}.initEffect(nowPoint==null?new WorldPoint(0,0):nowPoint, 1).setParentType(PackType.HEAP).boost())
		.save();
	}
	
	public void reset() {
		TreeMap<IPiece, IPieceView> copy = new TreeMap<IPiece, IPieceView>(dictPiece);
		for(IPiece bp : copy.keySet())
			getUserManager().remove(bp);
	}
	
	//2.Getters and setters
	@Override
	public boolean onCalled() {
		win.onDraw();
		return true;
	}
	public void setWindow(IWindow v) {
		win = v;
		init();
	}

	public Factory getFactory() {
		return userFactory;//blueprintManager;//getUserManager().getFactory();
	}
	
	public ActorManager getManager() {
		return editorManager.newSession();
	}

	public ActorManager getUserManager() {
		return userManager.newSession();
	}

	static Actor move = new Actor();
	static Actor.Mover move_ef = null;
	
	public static class AddPiece extends Actor {
		AddPiece() {
			super();
			boost();
			setInPackType(PackType.PASSTHRU, ChainInPathPack.Input.FIRST);
		}

		public boolean actorRun() throws ChainException, InterruptedException {
			return false;
		}
	}

	public void setLog(ILogHandler l) {
		editorManager.setLog(l);
		userManager.setLog(l);
	}
	public void setPathBlueprint(Blueprint p) {
		editorManager.setPathBlueprint(p);
		userManager.setPathBlueprint(p);
	}
	public void setModelAction(IWindowCallback ma) {
		winCall = ma;
	}

	public interface IWindowCallback {
		public boolean redraw(String str);
	}

	public boolean kickDraw() {
		editorManager.getChain().kick();
		return true;
	}

	@Override
	public IPieceView getView(IPiece bp) {
		if (dictPiece.get(bp) != null)
			return dictPiece.get(bp);
		return null;
	}

	@Override
	public IPathView getView(IPath path) {
		if (dictPath.get(path) != null)
			return dictPath.get(path);
		return null;
	}

	@Override
	public IPieceView onSetPieceView(final IPiece cp2, final Blueprint bp) throws ChainException {
		final IPieceView _view;
			ActorManager manager = getManager();
			_view = (IPieceView) bp.newInstance(manager);
			if(_view == null)
				throw new ChainException(cp2, "view not created");
			manager.save();
		dictPiece.put(cp2, _view);
		_view.setMyTapChain(cp2);
		final Initializer ia = new Initializer();
		if(_view instanceof IEditAnimation) {
			ia.init_animation(getManager(), (IEditAnimation)_view);
		}
		if(cp2 instanceof ChainPiece) {
			ChainPiece c = ((ChainPiece)cp2);
			c.setStatusHandler(new IStatusHandler() {
				@Override
				public synchronized void getStateAndSetView(int state) {
					if(state < PieceState.values().length)
						plist.get(state).push(_view);
				}
	
				@Override
				public void tickView() {
					_view.onTick();
					ia.start_animation(_view);
				}
	
				@Override
				public void end() {
					ia.term_animation();
				}
			});
			c.setError(errHandle);
		}
		return _view;
	}

	@Override
	public void setPathView(IPath path, IBlueprint _vReserve) {
		final IPathView _view;
		try {
			ActorManager manager = getManager();
			_view = (IPathView) _vReserve.newInstance(manager);
			manager.save();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		dictPath.put(path, _view);
		final Initializer ia = new Initializer();
		if(_view instanceof IEditAnimation) {
			ia.init_animation(getManager(), (IEditAnimation)_view);
		}
		path.setStatusHandler(new IStatusHandler() {
			@Override
			public void tickView() {
				_view.onTick();
				ia.start_animation(_view);
			}
			
			@Override
			public void getStateAndSetView(int state) {
			}

			@Override
			public void end() {
				ia.term_animation();
			}
		});
		return;
	}
	
	@Override
	public void onMoveView(IView v) {
		move_ef.initEffect(nowPoint==null?new WorldPoint(0,0):nowPoint, 1);
		move.push(v);
		kickDraw();
		return;
	}

	@Override
	public void onUnsetView(IPiece bp) {
		if(bp == null) 
			return;
		if(bp instanceof ChainPiece) {
			ChainPiece c = (ChainPiece)bp;
			c.getStatusHandler().end();
			c.setStatusHandler(null);
		}
//		ViewActor v = (ViewActor) getView(bp);
//		if(v instanceof IEditPieceView)
//			((IEditPieceView)v).unsetMyTapChain();
		IPieceView v = getView(bp);
		v.unsetMyTapChain();
		dictPiece.remove(bp);
//		if(v == null)
//			return;
		v.finish(false);
		return;
	}

	@Override
	public void unsetPathView(IPath path) {
		if(path == null) 
			return;
		IPathView v = getView(path);
		dictPath.remove(path);
		if(v == null)
			return;
		v.finishPath(false);
		return;
	}

	@Override
	public void onRefreshView(IPiece bp, IPiece obj) {
		IPieceView v = dictPiece.get(bp);
		dictPiece.remove(bp);
		if (((Actor)bp).compareTo((Actor)obj) > 0) {
			((Actor)bp).mynum = ++ActorChain.num;
		}
		dictPiece.put(bp, v);
	}

	public static class Initializer {
		Actor bp;
		List<IPiece> dump;
		static HashMap<Class<? extends IEditAnimation>, Actor>dict
			= new HashMap<Class<? extends IEditAnimation>, Actor>();
		public Initializer() {
		}
		public void init_animation(ActorManager maker, IEditAnimation a) {
			if(bp != null) return;
			bp = new Actor();
			PieceManager manager = maker._func(bp);
			a.init_animation(manager);
			manager._exit().save();
			dump = maker.dump();
		}
		public void start_animation(Object target) {
			if(bp != null)
				bp.push(target);
		}
		public void term_animation() {
		}
	}
	

	public enum EditMode {
		ADD, REMOVE, RENEW
	}

	EditMode editmode = EditMode.ADD;

	public boolean Mode(EditMode mode) {
		editmode = mode;
		return true;
	}

	public boolean onDown(WorldPoint sp) {
		nowPoint = sp;
		getManager().getChain().TouchOn(nowPoint);
		nowPiece = touchPiece(nowPoint);
//		moveView(circle);
		return true;
	}
	
	public IPieceView touchPiece(WorldPoint sp) {
		for (IPieceView f : dictPiece.values()) {
//			TapChainEditorView e = f.getValue();
			if (f.contains(sp.x, sp.y))
				return f;
		}
		return null;
	}

	public boolean onDownClear() {
		getManager().getChain().TouchClear();
		return true;
	}

	public void freezeToggle() {
		getUserManager().getChain().ctrl.Toggle();
	}
	
	public boolean up() {
		nowPiece = null;
		return true;
	}

	public boolean onUp() {
		getManager().getChain().TouchOff();
		if (nowPiece == null)
			return false;
		if(checkAndDelete(nowPiece)) return up();
		checkAndAttach((IPieceView)nowPiece, null);
		return up();
	}
	
	private boolean checkAndAttach(IPieceView target, WorldPoint d) {
		if(target == null)
			return false;
		for (IPieceView bp : dictPiece.values())
			if (attack(target, bp, d))
				return true;
		return false;
	}
	
	private boolean checkAndDelete(IPieceView v) {
		ScreenPoint sp = ((Actor.ViewActor)v).getCenter().getScreenPoint(win);
		if(0< sp.x && win.getWindowPoint().y()-150 < sp.y && 150 > sp.x && win.getWindowPoint().y() > sp.y) {
			getManager().remove(v.getMyTapChain());
			return true;
		}
		return false;
	}

	public boolean onFling(final int vx, final int vy) {
		if (nowPiece != null) {
			Actor v = (Actor) nowPiece;
			Actor.ValueLimited vl = new Actor.ValueLimited(1);
			getManager()._return(v)._child()
					.add(new Accel().disableLoop(), vl.disableLoop())._exit()
					.save();
			vl.setValue(new WorldPoint(vx, vy).setDif());
			return up();
		}
		getManager().add(new Actor() {
			float delta = 0.03f;
			int t = 0;

			@Override
			public boolean actorRun() {
				win.move((int) (delta * -vx), (int) (delta * -vy));
				delta -= 0.003f;
				invalidate();
				return ++t < 10;
			}
		}.disableLoop()).save();
		return up();
	}

	public boolean onLongPress() {
//		GetManager().Get().LongPress();
		if(nowPiece == null)
			return up();
		if(nowPiece.getEventHandler() == null)
			return up();
		nowPiece.getEventHandler().onSelected(nowPiece);
		return up();

	}

	public boolean onScroll(final WorldPoint vp, final WorldPoint wp) {
		getManager().getChain().Move(vp);
		if (nowPiece != null) {
			onClear();
			nowPiece.setCenter(nowPiece.getCenter().plus(vp));
				
		} else {
			win.move(-vp.x(), -vp.y());
		}
		kickDraw();
		return true;
	}

	public boolean onShowPress() {
		return up();
	}

	public boolean onSingleTapConfirmed() {
		// touching but against "kicked" piece
		
		IPieceView touchPiece = touchPiece(nowPoint);
		if(touchPiece == null)
			return false;
		IPiece p = touchPiece(nowPoint).getMyTapChain();
		switch (editmode) {
		case REMOVE:
			getUserManager().remove(p);
			up();
			editmode = EditMode.ADD;
			return false;
		case RENEW:
			getUserManager().restart(p);
			up();
			editmode = EditMode.ADD;
			return false;
		default:
		}
		if (nowPiece == null)
			return true;

		return true;
	}

	public static class DirOffset {
		static WorldPoint TOP = new WorldPoint(0, 50).setDif(),
			RIGHT = new WorldPoint(-50, 0).setDif(),
			BOTTOM = new WorldPoint(0, -50).setDif(),
			LEFT = new WorldPoint(50, 0).setDif(),
			NULL = null;
	}
	/*
	public WorldPoint checkReleasing(Actor bp) {
		TapChainAndroidEdit.EditorView v1 = (TapChainAndroidEdit.EditorView) getView(bp), v2 = null;
		if(!bp.hasInPath(PackType.FAMILY))
			return DirOffset.NULL;
		try {
			v2 = (TapChainAndroidEdit.EditorView) getView(bp.getParent(PackType.FAMILY));
		} catch (ChainException e) {
			return DirOffset.NULL;
		}
		if(v1.getInteraction()==null)
			return DirOffset.NULL;
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
	*/
	public boolean onClear() {
		nowPoint = null;
		return true;
	}


	public void Compile() {
		userManager.getChain().getOperator().reset();
		userManager.getChain().setCallback(new IControlCallback() {
			public boolean onCalled() {
				kickDraw();
				return true;
			}
		});
		return;
	}

	public void start() {
		if (userManager.getChain() == null) {
			return;
		}
		userManager.getChain().getOperator().start();
		return;
	}

	public void show(Object canvas) {
		getManager().getChain().Show(canvas);
		getUserManager().getChain().Show(canvas);
	}


	public class Reform extends Actor.Loop {
		Actor f = null;
		WorldPoint pt = null;

		Reform(Actor _f, WorldPoint _pt) {
			super();
			f = _f;
			pt = _pt;
		}

		@Override
		public boolean actorRun() throws ChainException, InterruptedException {
			// start reforming toward upper(prev) functions[recursive invocation]
			reformTo((Actor)getView(f), true, pt);
			// start reforming toward lower(next) functions[recursive invocation]
			reformTo((Actor)getView(f), false, pt);
			return false;
		}
	}

	void reformTo(Actor bp, final boolean UpDown, final WorldPoint pt) {
		WorldPoint pt_ = pt;
		if (pt_ != null) {
			getManager()._return(getView(bp))
					._child().add(new Accel().disableLoop())._exit().save();

		} else {
			pt_ = getView(bp).getCenter();
			int i = 0, i_max = bp.getOutPack(PackType.FAMILY).size();
			for (Connector.ChainOutConnector _cp : bp.getOutPack(PackType.FAMILY)) {
				Connector.ChainInConnector cip = _cp.getPartner();
				if (cip == null)
					break;
				Actor part = (Actor) cip.getParent();
				if (part == null)
					break;
				if (dictPiece.get(part) == null)
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
	public boolean connect(IPiece chainPiece, IPiece chainPiece2, ConnectType type) {
		switch(type) {
		case INCLUDED:
			return null != getUserManager()
			.append(chainPiece, PackType.FAMILY, chainPiece2, PackType.FAMILY);
		case INCLUDING:
			return null != getUserManager()
			.append(chainPiece2, PackType.FAMILY, chainPiece, PackType.FAMILY);
		case TOUCH_TOP:
			return null != getUserManager()
			.append(chainPiece2, PackType.EVENT, chainPiece, PackType.EVENT, true);
		case TOUCH_BOTTOM:
			return null != getUserManager()
			.append(chainPiece, PackType.EVENT, chainPiece2, PackType.EVENT, true);
		case TOUCH_LEFT:
			return null != getUserManager()
			.append(chainPiece2, PackType.HEAP, chainPiece, PackType.HEAP, true);
		case TOUCH_RIGHT:
			return null != getUserManager()
			.append(chainPiece, PackType.HEAP, chainPiece2, PackType.HEAP, true);
		case DISCONNECT:
			getUserManager().__disconnect(chainPiece, chainPiece2);
			return true;
		}
		return true;
	}
	
	public ConnectType checkAttackType(IPieceView v1, IPieceView v2, WorldPoint dir) {
		IPiece bp = v1.getMyTapChain();
		IPiece target = v2.getMyTapChain();
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
				getUserManager().refreshPieceView(bp, target);
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
	
	public boolean attack(IPieceView bp, IPieceView f, WorldPoint dir) {
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

	public class Accel extends Actor.Mover {
		float delta = 0.03f;
		int j = 0;
		WorldPoint wp = null;
		WorldPoint initial = null;
		Actor bp = null;

		@Override
		public boolean actorReset() throws ChainException {
			WorldPoint dummy = new WorldPoint();
			initEffect(dummy,1);
			super.actorReset();
			j = 0;
			delta = 0.03f;
			if (initial == null)
				__exec(wp = (WorldPoint) pull(), "Accel#reset");
			return true;
		}

		@Override
		public boolean actorRun() throws ChainException {
			WorldPoint d = new WorldPoint((int) (wp.x * delta), (int) (wp.y * delta)).setDif();
			initEffect(d,1);
			delta -= 0.003f;
			boolean rtn = ++j < 10;
			super.actorRun();
			if(checkAndAttach(((IPieceView)getTarget()), d)){
//				clearInputHeap();
				return false;
			}
//			if (!rtn)
//				push(null);
			return rtn;
		}
	}

	public interface IWindow {
		public WorldPoint getWindowPoint();
		public void move(int vx, int vy);
		void onDraw();
	}

	public WorldPoint getOrientAsWorld(IWindow i) {
		return (new ScreenPoint(i.getWindowPoint().x / 2,
				i.getWindowPoint().y / 2)).getWorldPoint(i);
	}
	
	public interface FrameEventHandler {
		public boolean onFrameConnecting(IView bp, IView f);

		public boolean onFrameDisconnecting(IView fremove, IView fleft);
	}

//	public interface ConnectEventHandler {
//		public boolean onConnectConnecting(ConnectClosedView cattack,
//				ConnectClosedView cdefend);
//
//		public boolean onConnectDisconnecting(ChainInConnector i, ChainOutConnector o,
//				ConnectClosedView cremove, ConnectClosedView cleft);
//	}

	public interface EventHandler extends FrameEventHandler/*, ConnectEventHandler*/ {
	}
	
	public interface IPieceView extends IView, IPiece, Tickable {
		public void setMyTapChain(IPiece cp2);
		public IPiece finish(boolean b);
		public WorldPoint getCenter();
		public void unsetMyTapChain();
		public IPiece getMyTapChain();
		public IInteraction getInteraction();
		public IEventHandler getEventHandler();
		public boolean contains(int x, int y);
	}
	
	public interface IInteraction {
		public boolean checkTouch(IView v1, IView v2);
		public boolean checkLeave(IView f1, IView f2);
		public boolean checkSplit(Actor.ViewActor f1, Actor.ViewActor f2);
		public boolean checkIn(IView v1, IView v2);
		public boolean checkOut(IView v1, IView v2);
	}

	public interface IEventHandler {
		public void onSelected(IView v);
	}
	
	public interface IPathView extends IView, Tickable {
		public void setMyTapPath(ConnectorPath p);
		public void unsetMyTapPath();
		public ConnectorPath getMyTapPath();
		public void finishPath(boolean cnt);
	}
	
	public interface Tickable {
		public void onTick();
	}
	
	public interface IEditAnimation {
		public void init_animation(PieceManager maker);
	}

}
