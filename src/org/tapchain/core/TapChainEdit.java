package org.tapchain.core;

import java.util.Collection;
import java.util.TreeMap;
import org.tapchain.core.ActorChain.*;
import org.tapchain.core.Actor.ViewActor;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.Output;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.ChainController.IControlCallback;


@SuppressWarnings("serial")
public class TapChainEdit implements IControlCallback {
	IWindow win = null;
	IWindowCallback winCall = null;
	public EditorManager editorManager = new EditorManager(new ActorManager());
	protected BlueprintManager blueprintManager = null;;
	Factory<IPiece> factory = null, recent = null, relatives = null;
	public IPieceView startPiece = null, dummy = null;
	protected WorldPoint nowPoint = null;
	protected IErrorHandler errHandle = null;
	protected StyleCollection styles = null;

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
		factory = new Factory<IPiece>();
		recent = new Factory<IPiece>();
		relatives = new Factory<IPiece>();
		blueprintManager = new BlueprintManager(factory);
		editorManager.getSystemManager()./*setFactory(userFactory).*/createChain(20);
		editorManager./*setFactory(userFactory).*/createChain(50).getChain().setAutoEnd(false);
		editorManager.getSystemManager().SetCallback(this);
		editorManager.SetCallback(this);
		return;
	}

	void init() {
		editorManager.init();
		editorManager.getSystemManager()
		._return(editorManager.move_ef)
		.young(new Actor.Effecter() {
			@Override
			public boolean actorRun() throws ChainException {
				if(getTarget() instanceof IPieceView)
					checkAndAttach((IPieceView)getTarget(), null);
				return false;
			}
		}.setParentType(PackType.HEAP).boost())
		.teacher(editorManager.move)
		.save();
	}
	
	public void reset() {
		TreeMap<IPiece, IPieceView> copy = new TreeMap<IPiece, IPieceView>(editorManager.dictPiece);
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

	public Factory<IPiece> getFactory() {
		return factory;//blueprintManager;//getUserManager().getFactory();
	}
	
	public Factory<IPiece> getRecent() {
		return recent;
	}
	
	public Factory<IPiece> getRelatives() {
		return relatives;
	}
	public ActorManager getSystemManager() {
		return editorManager.getSystemManager().newSession();
	}

	public ActorManager getUserManager() {
		return editorManager.newSession();
	}

	public WorldPoint getPoint() {
		return (nowPoint==null)?new WorldPoint(0,0):nowPoint;
	}
	
	public void setLog(ILogHandler l) {
		editorManager.getSystemManager().setLog(l);
		editorManager.setLog(l);
	}
	public void setPathBlueprint(Blueprint p) {
		editorManager.getSystemManager().setPathBlueprint(p);
		editorManager.setPathBlueprint(p);
	}
	public void setCallback(IWindowCallback ma) {
		winCall = ma;
	}
	public void setStyle(Object context, StyleCollection s) {
		styles = s;
		blueprintManager.setOuterInstanceForInner(context);
		blueprintManager.setDefaultView(s.getView());
		setPathBlueprint(new Blueprint(s.getConnect()));
	}

	public interface IWindowCallback {
		public boolean redraw(String str);
	}

	public boolean kickDraw() {
		editorManager.getSystemManager().getChain().kick();
		return true;
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
		getSystemManager().getChain().TouchOn(nowPoint);
		startPiece = touchPiece(nowPoint);
		return true;
	}
	
	public IPieceView touchPiece(WorldPoint sp) {
		for (IPieceView f : editorManager.getPieceViews()) {
			if (f.contains(sp.x, sp.y))
				return f;
		}
		return null;
	}

	public boolean onDownClear() {
		getSystemManager().getChain().TouchClear();
		return true;
	}

	public boolean freezeToggle() {
		return getUserManager().getChain().ctrl.Toggle();
	}
	
	public boolean up() {
		startPiece = null;
		return true;
	}

	public boolean onUp() {
		getSystemManager().getChain().TouchOff();
		if (startPiece == null)
			return false;
		if(checkAndDelete(startPiece)) return up();
		checkAndAttach((IPieceView)startPiece, null);
		return up();
	}
	
	private boolean checkAndAttach(IPieceView target, WorldPoint velocity) {
		if(target == null)
			return false;
		for (IPieceView bp : editorManager.getPieceViews())
			if (attack(target, bp, velocity))
				return true;
		return false;
	}
	
	public boolean attack(IPieceView bp, IPieceView f, WorldPoint velocity) {
		if (bp == f) {
			return false;
		}
		ConnectType t = checkAttackType(bp, f, velocity);
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

	private boolean checkAndDelete(IPieceView v) {
		ScreenPoint sp = ((Actor.ViewActor)v).getCenter().getScreenPoint(win);
		if(0< sp.x && win.getWindowPoint().y()-150 < sp.y && 150 > sp.x && win.getWindowPoint().y() > sp.y) {
			getSystemManager().remove(v.getMyTapChain());
			return true;
		}
		return false;
	}

	public boolean onFling(final int vx, final int vy) {
		if (startPiece != null) {
			//Target piece starts moving and slows down gradually.
			Actor v = (Actor) startPiece;
			Actor.ValueLimited vl = new Actor.ValueLimited(1);
			getSystemManager()._return(v)._child()
					.add(new Accel().disableLoop(), vl.disableLoop())._exit()
					.save();
			vl.setValue(new WorldPoint(vx, vy).setDif());
			return up();
		}
		//Background center starts moving and slows down gradually.
		getSystemManager().add(new Actor() {
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
		if(startPiece == null)
			//Background no longer reacts.
			return up();
		if(startPiece.getEventHandler() == null)
			//Target piece without handler no logner reacts.
			return up();
		//Target piece with handler calls handler onSelected;
		startPiece.getEventHandler().onSelected(startPiece);
		return up();

	}

	public boolean onScroll(final WorldPoint vp, final WorldPoint wp) {
		getSystemManager().getChain().Move(vp);
		if (startPiece != null) {
			onClear();
			startPiece.setCenter(startPiece.getCenter().plus(vp));
				
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
			touchPiece.getEventHandler().onSelected(touchPiece);
		}
		if (startPiece == null)
			return true;

		return true;
	}
	
	public boolean onAdd(Factory<IPiece> f, int num) {
		if (num < f.getSize())
			f.newInstance(num, getPointOnAdd(getPoint()), getUserManager());
		//addition to history.
		if(f==getFactory())
			getRecent().Register(f.get(num));
		//search for relatives.
		getFactory().relatives(f.get(num), relatives);
		return true;
	}
	
	private WorldPoint getPointOnAdd(WorldPoint p) {
		return (styles==null||styles.getActionStyle()==null)
				?p
				:styles.getActionStyle().pointOnAdd(p);
	}
	
	public boolean onDummyAdd(Factory<IPiece> f, int num, WorldPoint p) {
		if (num < f.getSize())
			try {
				dummy = (IPieceView) f.getView(num).newInstance(getSystemManager());
				getSystemManager().save();
				dummy.setCenter(p);
				dummy.setAlpha(100);
			} catch (ChainException e) {
				getSystemManager().error(e);
			}
		return true;
	}
	
	public boolean onDummyRemove() {
		getSystemManager().remove(dummy);
		dummy = null;
		return true;
	}
	
	public boolean onDummyMoveTo(WorldPoint vp) {
		if(!getPointOnAdd(vp).equals(dummy.getCenter())) {
			dummy.setCenter(vp);
			kickDraw();
		}
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
		editorManager.getChain().getOperator().reset();
		editorManager.getChain().setCallback(new IControlCallback() {
			public boolean onCalled() {
				kickDraw();
				return true;
			}
		});
		return;
	}

	public void start() {
		if (editorManager.getChain() == null) {
			return;
		}
		editorManager.getChain().getOperator().start();
		return;
	}

	public void show(Object canvas) {
		getSystemManager().getChain().Show(canvas);
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
			reformTo((Actor)editorManager.getView(f), true, pt);
			// start reforming toward lower(next) functions[recursive invocation]
			reformTo((Actor)editorManager.getView(f), false, pt);
			return false;
		}
	}

	void reformTo(Actor bp, final boolean UpDown, final WorldPoint pt) {
		WorldPoint pt_ = pt;
		if (pt_ != null) {
			getSystemManager()._return(editorManager.getView(bp))
					._child().add(new Accel().disableLoop())._exit().save();

		} else {
			pt_ = editorManager.getView(bp).getCenter();
			int i = 0, i_max = bp.getOutPack(PackType.FAMILY).size();
			for (Connector.ChainOutConnector _cp : bp.getOutPack(PackType.FAMILY)) {
				Connector.ChainInConnector cip = _cp.getPartner();
				if (cip == null)
					break;
				Actor part = (Actor) cip.getParent();
				if (part == null)
					break;
				if (editorManager.getView(part) == null)
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
			boolean rtn = ++j < 10 || d.getAbs() > 20;
			super.actorRun();
			if(checkAndAttach(((IPieceView)getTarget()), d)){
//				clearInputHeap();
				return false;
			}
//			if (!rtn)
//				push(null);
			if(!rtn) {
				ViewActor v = getTargetView();
				v.getCenter().round(100).plus(50);
			}
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
	
	public static class SimplePieceView implements IPieceView {
		@Override
		public boolean view_impl(Object canvas) {
			return false;
		}

		@Override
		public IView setCenter(WorldPoint point) {
			return null;
		}

		@Override
		public ConnectionResultO appended(Class<?> cls, Output type,
				PackType stack, IPiece from) throws ChainException {
			return null;
		}

		@Override
		public ConnectionResultIO appendTo(PackType stack, IPiece piece_to,
				PackType stack_target) throws ChainException {
			return null;
		}

		@Override
		public void detached(IPiece _cp_end) {
		}

		@Override
		public void setPartner(IPath chainPath, IPiece _cp_start) {
		}

		@Override
		public IPath detach(IPiece y) {
			return null;
		}

		@Override
		public Collection<IPiece> getPartners() {
			return null;
		}

		@Override
		public boolean isConnectedTo(IPiece target) {
			return false;
		}

		@Override
		public IPiece signal() {
			return null;
		}

		@Override
		public void end() {
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public <T> T __exec(T obj, String flg) {
			return null;
		}

		@Override
		public void onTick() {
		}

		@Override
		public void setMyTapChain(IPiece cp2) {
		}

		@Override
		public IPiece finish(boolean b) {
			return null;
		}

		@Override
		public WorldPoint getCenter() {
			return null;
		}

		@Override
		public void unsetMyTapChain() {
		}

		@Override
		public IPiece getMyTapChain() {
			return null;
		}

		@Override
		public IInteraction getInteraction() {
			return null;
		}

		@Override
		public IEventHandler getEventHandler() {
			return null;
		}

		@Override
		public boolean contains(int x, int y) {
			return false;
		}

		@Override
		public IView setAlpha(int i) {
			return null;
		}
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
	
	public interface ActionStyle {
		public WorldPoint pointOnAdd(WorldPoint raw);
	}
	
	public static class StyleCollection {
		Class<? extends ViewActor> view;
		Class<? extends IPiece> connect;
		ActionStyle actionStyle;
		public StyleCollection(Class<? extends ViewActor> v, Class<? extends IPiece> c, ActionStyle action) {
			view = v;
			connect = c;
			actionStyle = action;
		}
		public Class<? extends IPiece> getConnect() {
			return connect;
		}
		public Class<? extends ViewActor> getView() {
			return view;
		}
		public ActionStyle getActionStyle() {
			return actionStyle;
		}
	}

	public boolean magnetToggle() {
		return false;
	}
}
