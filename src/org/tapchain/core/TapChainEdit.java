package org.tapchain.core;

import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;

import org.tapchain.AndroidActor.AndroidDashRect;
import org.tapchain.core.Actor.ControllableSignal;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.ChainController.IControlCallback;


@SuppressWarnings("serial")
public abstract class TapChainEdit implements IControlCallback, ILogHandler, IErrorHandler  {
	IWindow win = null;
	public EditorManager editorManager = new EditorManager();
	Factory<IPiece> factory = new Factory<IPiece>(),
			recent = new Factory<IPiece>(),
			relatives = new Factory<IPiece>(),
			goalFactory = new Factory<IPiece>();
	protected BlueprintManager blueprintManager = new BlueprintManager(factory),
			goalBlueprintManager = null;
	public ISystemPiece startPiece = null, previousPiece = null, dummy = null;
//	protected WorldPoint nowPoint = null;
	protected IErrorHandler errHandle = null;
	protected StyleCollection styles = null;

	//1.Initialization
	protected TapChainEdit(IWindow w) {
		setWindow(w);
		goalBlueprintManager = new BlueprintManager(goalFactory);
		editorManager.setAllCallback(this);
		editorManager.init();
		editorManager.getSystemManager()
		._return(editorManager.move_ef)
		.young(new Actor.Effector() {
			@Override
			public boolean actorRun(Actor act) throws ChainException {
				if(getTarget() instanceof ISystemPiece)
					checkAndAttach((ISystemPiece)getTarget(), false);
				return false;
			}
		}.setParentType(PackType.HEAP).boost())
		.teacher(editorManager.move)
		._save();
		setLog(this);
		setError(this);
	}
	
	public void reset() {
		TreeMap<IPiece, ISystemPiece> copy = new TreeMap<IPiece, ISystemPiece>(editorManager.dictPiece);
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
//		init();
	}

	public Factory<IPiece> getFactory() {
		return factory;//blueprintManager;//getUserManager().getFactory();
	}
	
	public Factory<IPiece> getRecentFactory() {
		return recent;
	}
	
	public Factory<IPiece> getRelatives() {
		return relatives;
	}
	public Factory<IPiece> getGoal() {
		return goalFactory;
	}
	public ActorManager getSystemManager() {
		return editorManager.getSystemManager().newSession();
	}

	public ActorManager getUserManager() {
		return editorManager.newSession();
	}
	
	public BlueprintManager getBlueprintManager() {
		return blueprintManager;
	}

	/**
	 * @return the lInteract
	 */
	public ActionStyle getInteract() {
		return styles.getActionStyle();
	}

	public void setLog(ILogHandler l) {
		editorManager.getSystemManager().setLog(l);
		editorManager.setLog(l);
	}
	public void setError(IErrorHandler err) {
	 	editorManager.getSystemManager().setError(err);
		editorManager.setError(err);
		blueprintManager.setError(err);
		goalBlueprintManager.setError(err);
	}
	public void setStyle(StyleCollection s) {
		styles = s;
		blueprintManager.setOuterInstanceForInner(s.getOuter());
		blueprintManager.setDefaultView(s.getView());
		Blueprint connbp = blueprintManager.createBlueprint(s.getConnect());
		editorManager.getSystemManager().setPathBlueprint(connbp);
		editorManager.setPathBlueprint(connbp);
		goalBlueprintManager.setOuterInstanceForInner(s.getOuter());
	}

	public interface IWindowCallback {
		public boolean redraw(String str);
	}

	public boolean kickSystemDraw(IPiece pc) {
		editorManager.getSystemManager().getChain().kick(pc);
		return true;
	}

	public boolean kickUserDraw(IPiece pc) {
		editorManager.getChain().kick(pc);
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
	
	public boolean onDown(WorldPoint wp) {
		getSystemManager().getChain().TouchOn(wp);
		return capturePiece(wp);
	}

	public boolean capturePiece(WorldPoint sp) {
		capturePiece(searchTouchedPiece(sp));
		return true;
	}
	
	public boolean capturePiece(ISystemPiece pv) {
		startPiece = pv;
		return true;
	}
	
	public boolean isNotCapturing() {
		return startPiece == null;
	}
	
	public ISystemPiece getCapturedPiece() {
		return startPiece;
	}
	
	public ISystemPiece searchTouchedPiece(WorldPoint sp) {
		for (ISystemPiece f : editorManager.getSystemPieces()) {
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
	
	public boolean releasePiece() {
		previousPiece = startPiece;
		startPiece = null;
		getInteract().onRelease();
		return true;
	}

	public boolean onUp() {
		getSystemManager().getChain().TouchOff();
		if (isNotCapturing())
			return false;
		if(checkAndDelete(startPiece)) return releasePiece();
		checkAndAttach((ISystemPiece)startPiece, false);
		return releasePiece();
	}
	

	public boolean onFling(final int vx, final int vy) {
		if (startPiece != null) {
			//Target piece starts moving and slows down gradually.
			Actor v = (Actor) startPiece;
			getSystemManager()._return(v)._child()
					.add(new Accel(new WorldPoint(vx, vy).setDif()).disableLoop())._exit()
					._save();
			return releasePiece();
		}
		//Background center starts moving and slows down gradually.
		getSystemManager().addActor(new IActor() {
			float delta = 0.03f;
			int t = 0;

			@Override
			public boolean actorRun(Actor act) {
				win.move((int) (delta * -vx), (int) (delta * -vy));
				delta -= 0.003f;
				act.invalidate();
				return ++t < 10;
			}
		})._save();
		return releasePiece();
	}

	public boolean onLongPress() {
//		GetManager().Get().LongPress();
//		AndroidActor.makeAlert("onLongPress");
/*		if(startPiece == null)
			//Background no longer reacts.
			return releasePiece();
		if(startPiece.getEventHandler() == null)
			//Target piece without handler no logner reacts.
			return releasePiece();
		//Target piece with handler calls handler onSelected;
		startPiece.getEventHandler().onSelected(startPiece);
		return releasePiece();
*/
		if(startPiece == null)
			return false;
		getSystemManager()
		.add(new AndroidDashRect().setCenter(startPiece.get()).setSize(new WorldPoint(200,200)).setColor(0xffffffff))
		._child()
		.add(new Actor.Sleeper(2000))
		.young(new Actor.Ender())
		._exit()
		._save();
		return false;
	}
	
	public boolean onSecondTouch(final WorldPoint wp) {
//		AndroidActor.makeAlert("onSecondTouch");
		if(startPiece == null)
			return false;
		startPiece.setMyTapChainValue(wp.sub(startPiece.getCenter()).multiply(0.1f).setDif());
		return true;
	}

	public boolean onScroll(final WorldPoint vp, final WorldPoint wp) {
//		getSystemManager().getChain().Move(vp);
		if (startPiece != null) {
//			onClear();
			startPiece.setCenter(startPiece.getCenter().plus(vp));
			//Check disconnection
			if(!checkAndAttach(startPiece, true))
				checkAndDetach(startPiece);
		} else {
			win.move(-vp.x(), -vp.y());
		}
		kickSystemDraw(startPiece);
		return true;
	}

	public boolean onShowPress() {
		return false;
	}

	public boolean onSingleTapConfirmed() {
		ISystemPiece touchPiece = startPiece;//touchPiece(nowPoint);
		if(touchPiece == null)
			touchPiece = previousPiece;
		//Invalid status: startPiece and previousPiece are null
		if(touchPiece/*touchPiece*/ == null)
			return false;
		IPiece p = /*touchPiece(nowPoint)*/touchPiece.getMyTapChain();
		switch (editmode) {
		case REMOVE:
			getUserManager().remove(p);
			releasePiece();
			editmode = EditMode.ADD;
			return false;
		case RENEW:
			getUserManager().restart(p);
			releasePiece();
			editmode = EditMode.ADD;
			return false;
		default:
			touchPiece.getEventHandler().onSelected(touchPiece);
		}
		return true;
	}
	
	public Map.Entry<IPiece, ISystemPiece> onAdd(Factory<IPiece> f, int num, IPoint pos) {
		ISystemPiece rtn = null;
		//Invalid parameter
		if (num >= f.getSize())
			return null;
		//Create new instance piece
		IPiece p = f.newInstance(num, getPointOnAdd(pos), getUserManager());
		//Get PieceView
		rtn = editorManager.getView(p);
//			Log.w("TEST", String.format("Position %s", getPoint()));
//			Log.w("TEST", String.format("Position %s", getPointOnAdd(getPoint())));
		//Add to history factory.
		if(f==getFactory())
			getRecentFactory().Register(f.get(num));
		//Change relatives factory.
		getFactory().relatives(f.get(num), relatives);
		return new AbstractMap.SimpleEntry(p, rtn);
	}
	
	private IPoint getPointOnAdd(IPoint iPoint) {
		return (styles==null||getInteract()==null)
				?iPoint
				:getInteract().pointOnAdd(iPoint);
	}
	
	public boolean onDummyAdd(Factory<IPiece> f, int num, WorldPoint p) {
		if (num < f.getSize())
			try {
				dummy = (ISystemPiece) f.getView(num).newInstance(getSystemManager());
				getSystemManager()._save();
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
			kickSystemDraw(dummy);
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

	public void Compile() {
		editorManager.getChain().getOperator().reset();
		editorManager.getChain().setCallback(new IControlCallback() {
			public boolean onCalled() {
//				kickUserDraw();
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
	}
	
	public void userShow(Object canvas) {
		getUserManager().getChain().Show(canvas);
	}


//	public class Reform extends Actor.Loop {
//		Actor f = null;
//		WorldPoint pt = null;
//
//		Reform(Actor _f, WorldPoint _pt) {
//			super();
//			f = _f;
//			pt = _pt;
//		}
//
//		@Override
//		public boolean actorRun() throws ChainException, InterruptedException {
//			// start reforming toward upper(prev) functions[recursive invocation]
//			reformTo((Actor)editorManager.getView(f), true, pt);
//			// start reforming toward lower(next) functions[recursive invocation]
//			reformTo((Actor)editorManager.getView(f), false, pt);
//			return false;
//		}
//	}
//
//	void reformTo(Actor bp, final boolean UpDown, final WorldPoint pt) {
//		WorldPoint pt_ = pt;
//		if (pt_ != null) {
//			getSystemManager()._return(editorManager.getView(bp))
//					._child().add(new Accel().disableLoop())._exit().save();
//
//		} else {
//			pt_ = editorManager.getView(bp).getCenter();
//			int i = 0, i_max = bp.getOutPack(PackType.FAMILY).size();
//			for (Connector.ChainOutConnector _cp : bp.getOutPack(PackType.FAMILY)) {
//				Connector.ChainInConnector cip = _cp.getPartner();
//				if (cip == null)
//					break;
//				Actor part = (Actor) cip.getParent();
//				if (part == null)
//					break;
//				if (editorManager.getView(part) == null)
//					break;
//				reformTo(part, UpDown, pt_.plus(new WorldPoint(30 * (-i_max + 2 * i++),
//						UpDown ? -50 : 50)));
//			}
//		}
//		return;
//	}

	/** Check if selected piece is now attaching to other pieces, and when it is true, connect it to them.
	 * @param selected Selected piece view
	 * @param velocity Velocity of selected piece view
	 * @return True when selected piece was attached.
	 */
	private boolean checkAndAttach(ISystemPiece selected, boolean onlyInclude) {
		if(selected == null)
			return false;
		for (ISystemPiece bp : editorManager.getSystemPieces()) {
			// Not to check connection between pieces already connected each other.
			if(attach(selected, bp, onlyInclude))
				return true;
		}
		return false;
	}
	
	/** Connect selected piece to another target piece.
	 * When two are already connected, disconnect two into pieces.
	 * @param selected
	 * @param target
	 * @param velocity
	 * @return True when selected piece was attached.
	 */
	public boolean attach(ISystemPiece selected, ISystemPiece target, boolean onlyInclude) {
		if (selected == target) {
			return false;
		}
		// Check attack type.
		ConnectType t = _checkConnectType(selected, target, onlyInclude);
		// If CheckType is null, this means no connection/disconnection in moving and target.
		if(t == ConnectType.NONE){
			return false;
		// If CheckType is not null, a connection/disconnection can be made.
		} else {
			if(selected.getMyTapChain().isConnectedTo(target.getMyTapChain()) && t != ConnectType.DISCONNECT)
				return false;
			// Connect/disconnect moving to/from target;
			if(!connect(selected.getMyTapChain(), target.getMyTapChain(), t))
				return false;
			// Call EventHandler's onFrameConnection Handler. 
			if (selected instanceof EventHandler) {
				((EventHandler) selected).onFrameConnecting(selected, target);
			}
		}

		return true;
	}
	
	public boolean checkAndDetach(ISystemPiece selected) {
		for(IPiece parent : startPiece.getMyTapChain().getPartners()) {
			ISystemPiece v2 = editorManager.getView(parent);
			ConnectType t = getInteract().checkDisconnect(startPiece, editorManager.getView(parent),
					startPiece.getMyTapChain().getPackType(parent));
			// If CheckType is null, this means no disconnection occurs.
			if(t == ConnectType.NONE){
				return false;
			// If CheckType is not null, a disconnection can be made.
			} else {
				// Disconnect moving from target;
				if(!connect(selected.getMyTapChain(), parent, t))
					return false;
			}
		}
		//Disconnection occurred
		return true;
	}
	
	/** Check if selected piece is now in position for deletion, and when it is true, delete this piece.
	 * @param selected Selected piece view.
	 * @return True when selected piece was deleted.
	 */
	private boolean checkAndDelete(ISystemPiece selected) {
		IPoint sp = ((Actor.ViewActor)selected).getCenter();
		if(0< sp.x() && win.getWindowSize().y()-150 < sp.y() && 150 > sp.x() && win.getWindowSize().y() > sp.y()) {
			getSystemManager().remove(selected.getMyTapChain());
			return true;
		}
		return false;
	}
	public enum ConnectType {
		GETINCLUDED, INCLUDING, TOUCH_LEFT, TOUCH_RIGHT, TOUCH_TOP, TOUCH_BOTTOM, NONE, RELEASING, DISCONNECT
	}
	public boolean connect(IPiece chainPiece, IPiece chainPiece2, ConnectType type) {
		switch(type) {
		case GETINCLUDED:
			// this converts the view order of firstpiece and secondpiece.
			getUserManager().refreshPieceView(chainPiece, chainPiece2);
			return null != getUserManager()
			.append(chainPiece, PackType.FAMILY, chainPiece2, PackType.FAMILY, true);
		case INCLUDING:
			return null != getUserManager()
			.append(chainPiece2, PackType.FAMILY, chainPiece, PackType.FAMILY, true);
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
		return false;
	}
	
	ConnectType _checkConnectType(ISystemPiece v1, ISystemPiece v2, boolean onlyInclude) {
		IPiece firstpiece = v1.getMyTapChain();
		IPiece secondpiece = v2.getMyTapChain();
		ActionStyle style = getInteract();
		// Connected/or-not judgment
		if (firstpiece.isConnectedTo(secondpiece)) {
			return style.checkDisconnect(v1, v2, firstpiece.getPackType(secondpiece));
		} else {
			// neither containing nor colliding
			return style.checkConnect(v1, v2, onlyInclude);
		}
	}
	
	ConnectType _checkDisconnect(ISystemPiece v1, ISystemPiece v2) {
		IPiece firstpiece = v1.getMyTapChain();
		IPiece secondpiece = v2.getMyTapChain();
		ActionStyle style = getInteract();
		// Connected/or-not judgment
		if (firstpiece.isConnectedTo(secondpiece)) {
			return style.checkDisconnect(v1, v2, firstpiece.getPackType(secondpiece));
		} else {
			return ConnectType.NONE;
		}
	}
	
//	ConnectType _getCollisionConnectType(WorldPoint dir) {
//		if (null == dir)
//			return ConnectType.NULL;
//		int dxy = dir.x + dir.y;
//		int d_xy = -dir.x + dir.y;
//		if (dxy > 0) {
//			if(d_xy > 0) return ConnectType.TOUCH_TOP;
//			else return ConnectType.TOUCH_LEFT;
//		} else {
//			if(d_xy > 0) return ConnectType.TOUCH_RIGHT;
//			else return ConnectType.TOUCH_BOTTOM;
//		}
//	}
	
	public class Accel extends Actor.Mover {
		float delta = 0.03f;
		int j = 0;
		WorldPoint wp = null;
		WorldPoint initial = null;
		Actor bp = null;
		
		public Accel(WorldPoint _wp) {
			super();
			initial = wp = _wp;
		}

		@Override
		public boolean actorInit() throws ChainException {
			WorldPoint dummy = new WorldPoint();
			initEffect(dummy,1);
			super.actorInit();
			j = 0;
			delta = 0.03f;
			if (initial == null)
				__exec(wp = (WorldPoint) pull(), "Accel#reset");
			return true;
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException {
			WorldPoint d = new WorldPoint((int) (wp.x * delta), (int) (wp.y * delta)).setDif();
			initEffect(d,1);
			delta -= 0.003f;
			boolean rtn = ++j < 10 || d.getAbs() > 20;
			super.actorRun(act);
			if(checkAndAttach(((ISystemPiece)getTarget()), false)){
//				clearInputHeap();
//				ViewActor v = getTargetView();
//				v.setCenter(getPointOnAdd(v.getCenter()/*.add(0, -100)*/));
				return false;
			}
//			checkAndDetach((ISystemPiece)getTarget());
//			if (!rtn)
//				push(null);
			if(!rtn) {
				ViewActor v = getTargetView();
				getPointOnAdd(v.getCenter());
				checkAndAttach(((ISystemPiece)getTarget()),false);
				return false;
			}
			return rtn;
		}
	}

	public interface IWindow {
		public IPoint getWindowSize();
		public void move(int vx, int vy);
		void onDraw();
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
	
	public interface ISystemPiece extends IView, IPiece, Tickable {
		public void setMyTapChain(IPiece cp2);
		public void interrupt(ControllableSignal end);
		public IPoint getCenter();
		public void unsetMyTapChain();
		public IPiece getMyTapChain();
		public IEventHandler getEventHandler();
		public boolean contains(int x, int y);
		public boolean setMyTapChainValue(Object obj);
	}
	
	
	public interface IEventHandler {
		public void onSelected(IView v);
	}
	
	public interface ISystemPath extends IView, Tickable {
		public void setMyTapPath(ConnectorPath p);
		public void unsetMyTapPath();
		public ConnectorPath getMyTapPath();
		public void interrupt(ControllableSignal end);
	}
	
	public interface Tickable {
		public void onTick();
	}
	
	public interface IEditAnimation {
		public void init_animation(PieceManager maker);
	}
	
	public interface ActionStyle {
		public ConnectType checkConnect(IView v1, IView v2, boolean onlyInclude);
		public ConnectType checkDisconnect(IView v1, IView v2, PackType pt);
		public IPoint pointOnAdd(IPoint iPoint);
		public void onRelease();
	}

}
