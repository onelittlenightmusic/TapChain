package org.tapchain.editor;

import android.util.Log;

import org.tapchain.ActorTap;
import org.tapchain.PaletteSort;
import org.tapchain.core.Actor;
import org.tapchain.core.Actor.Mover;
import org.tapchain.core.ActorBlueprintManager;
import org.tapchain.core.ActorManager;
import org.tapchain.core.Blueprint;
import org.tapchain.core.BlueprintInitialization;
import org.tapchain.core.BlueprintManager;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.Factory;
import org.tapchain.core.IActionStyle;
import org.tapchain.core.IActor;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IBlueprintInitialization;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.ILockedScroll;
import org.tapchain.core.ILogHandler;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IPressed;
import org.tapchain.core.IRelease;
import org.tapchain.core.IScrollable;
import org.tapchain.core.ISelectable;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.core.Packet;
import org.tapchain.core.PathType;
import org.tapchain.core.StyleCollection;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public abstract class TapChainEditor implements IControlCallback, ILogHandler,
		IActorEditor {
	IWindow win = null;
	public EditorManager editorManager = new EditorManager();
    HashMap<FACTORY_KEY, Factory<Actor>> factories = new HashMap<>();
	Factory<Actor> factory = new Factory<Actor>(),
			recent = new Factory<Actor>(), relatives = new Factory<Actor>(),
			goalFactory = new Factory<Actor>();
	protected ActorBlueprintManager<Actor> blueprintManager = new ActorBlueprintManager<>(
			factory), goalBlueprintManager = null;
	private ITap selectedTap = null;
	private ITap previousTap = null;
	private ActorTap dummyTap = null;
	private StyleCollection styles = null;
	Actor move = new Actor().setLinkClass(LinkType.PUSH, IPoint.class);
	Actor.Mover move_ef = (Mover) new Actor.Mover()
			.initEffectValue(new WorldPoint(0f, 0f), 1)
			.setParentType(PathType.OFFER).boost();
	IPoint touched = null;
	protected List<Geometry> geos = new ArrayList<Geometry>();
	private IPoint nextConnectivityPoint = null;
    ArrayList<IActorTap> family = new ArrayList<IActorTap>();
    public enum FACTORY_KEY {
        ALL, RECENT, RELATIVES
    }

	// 1.Initialization
	protected TapChainEditor(IWindow w) {
		setWindow(w);
		goalBlueprintManager = new ActorBlueprintManager<Actor>(goalFactory);
		editorManager.setAllCallback(this);
		editorManager.setPathInterval(1000);
		editorManager.init();
		editTap()
			.add(move)
			.student(move_ef)
			.next(new Actor.Effector() {
				@Override
				public boolean actorRun(Actor act) throws ChainException {
					if (getTarget() instanceof ActorTap)
						checkAndAttach((ActorTap) getTarget(), false);
					return false;
				}
			}.setParentType(PathType.OFFER)
					.setLinkClass(LinkType.PULL, Object.class).boost())
			.teacher(move).save();
		setLog(this);
//		setError(this);
        factories.put(FACTORY_KEY.ALL, factory);
        factories.put(FACTORY_KEY.RECENT, recent);
        factories.put(FACTORY_KEY.RELATIVES, relatives);
	}

	public void reset() {
		for (Actor p : getActors())
			edit().remove(p);
	}

	// 2.Getters and setters
	@Override
	public boolean onCalled() {
		win.onDraw();
		return true;
	}

//	@Override
//	public ChainPiece onError(ChainPiece bp, ChainException e) {
//		if(!(e instanceof ActorInputException))
//			return bp;
//		IActorTap t = toTap((Actor) bp);
//		if(e instanceof ActorPullException)
//			getEventHandler().onPullLocked(t, (ActorPullException) e);
//		return bp;
//	}
//
//	@Override
//	public ChainPiece onUnerror(ChainPiece cp, ChainException e) {
//		if(!(e instanceof ActorInputException))
//			return cp;
//		if(e instanceof ActorPullException) {
//			IActorTap t = toTap((Actor) cp);
//			getEventHandler().onPullUnlocked(t, (ActorPullException) e);
//		}
//		return cp;
//	}

	public void setWindow(IWindow v) {
		win = v;
	}

	protected IWindow getWindow() {
		return win;
	}

	public Factory<Actor> getFactory() {
		return factory;
	}

    public Factory<Actor> getFactory(FACTORY_KEY key) {
        switch(key) {
            case ALL:
                return factory;
            case RECENT:
                return recent;
            case RELATIVES:
                return relatives;
            default:
                return factory;
        }
    }

	@Override
	public ActorManager editTap() {
		return editorManager.getTapManager()/*.newSession()*/;
	}

	@Override
	public ActorManager edit() {
		return editorManager/*.newSession()*/;
	}

    @Override
	public Collection<IActorTap> getTaps() {
		return editorManager.getTaps();
	}

    @Override
	public Collection<Actor> getActors() {
		return editorManager.getActors();
	}

	public Collection<IActorTap> getTaps(IPoint pt) {
		return TapLib.getTaps(pt);
	}

	IPoint rangeMin = new WorldPoint(-300f, -300f);
	IPoint rangeMax = new WorldPoint(300f, 300f);

	public List<IActorTap> getNearbyTaps(IPoint pt) {
		List<IActorTap> rtn = new ArrayList<IActorTap>();
		IPoint min = pt.plusNew(rangeMin);
		IPoint max = pt.plusNew(rangeMax);
		for (float f = min.x(); f <= max.x(); f += 100f) {
			for (float fy = min.y(); fy <= max.y(); fy += 100f) {
				Collection<IActorTap> tmp = getTaps(new WorldPoint(f, fy));
				if (tmp == null)
					continue;
				rtn.addAll(tmp);
			}
		}
		return rtn;
	}

	static WorldPoint approximate = new WorldPoint(400f, 400f);

	public Collection<IActorTap> getApproximateTaps(IPoint pt) {
		return TapLib.getRangeTaps(pt.subNew(approximate),
				pt.plusNew(approximate));
	}

	public BlueprintManager<Actor> getBlueprintManager() {
		return blueprintManager;
	}

	@Override
	public IActorTap toTap(Actor p) {
		return editorManager.getTap(p);
	}

	@Override
	public Actor toActor(IActorTap sp) {
		return sp.getActor();
	}

	/**
	 * @return the lInteract
	 */
	public IActionStyle getInteract() {
		if (styles == null)
			return null;
		return styles.getActionStyle();
	}

	public void setLog(ILogHandler l) {
		editorManager.getTapManager().setLog(l);
		editorManager.setLog(l);
	}

	public void setError(IErrorHandler err) {
		editorManager.getTapManager().setError(err);
		editorManager.setError(err);
		blueprintManager.setError(err);
		goalBlueprintManager.setError(err);
	}

	public void setStyle(StyleCollection s) {
		styles = s;
		blueprintManager.setOuterInstanceForInner(s.getOuter());
		blueprintManager.setDefaultView(s.getView());
		Blueprint connbp = blueprintManager.createTapBlueprint(s.getConnect());
		editorManager.setPathBlueprint(connbp);
		editorManager.setActorConnectHandler(s.getConnectHandler());
		goalBlueprintManager.setOuterInstanceForInner(s.getOuter());
	}

    @Override
	public IActorSharedHandler getEventHandler() {
		if (styles == null)
			return null;
		return styles.getEventHandler();
	}

	public interface IWindowCallback {
		public boolean redraw(String str);
	}

	@Override
	public void kickTapDraw(ITap startTap2) {
		editorManager.getTapManager().getChain().kick(startTap2);
	}

	public boolean kickUserDraw(IPiece pc) {
		editorManager.getChain().kick(pc);
		return true;
	}

	public enum EditMode {
		ADD, REMOVE, RENEW
	}

	EditMode editmode = EditMode.ADD;

	public boolean getMode(EditMode mode) {
		editmode = mode;
		return true;
	}

	protected boolean capturePiece(IPoint iPoint) {
		ITap t = searchTouchedTap(iPoint);
		if (t != null)
			Log.w("test", String.format("onScroll(%s)", t.toString()));
		captureTap(t);
		return true;
	}

	public boolean captureTap(ITap pv) {
		selectedTap = pv;
		return true;
	}

	protected boolean isNotCapturing() {
		return selectedTap == null;
	}

	public boolean isNotCapturingActorTap() {
		return selectedTap == null || !(selectedTap instanceof IActorTap);
	}

	public ITap getCapturedTap() {
		return selectedTap;
	}

	public ActorTap getCapturedActorTap() {
		return isNotCapturingActorTap() ? null : (ActorTap) selectedTap;
	}

	public ITap searchTouchedTap(IPoint iPoint, IPiece... exclusive) {
		Collection<ITap> ps = TapLib.getAllSystemPieces();
		ps.removeAll(Arrays.asList(exclusive));
		for (ITap f : ps) {
			if (f.contains(iPoint)) {
				return f;
			}
		}
		return null;
	}

	public Collection<IActorTap> searchRoomPieces(IPoint iPoint,
			IPiece... exclusive) {
		Collection<IActorTap> tList = getTaps(iPoint);
		if (tList == null || tList.size() <= 0)
			return null;
		tList.removeAll(Arrays.asList(exclusive));
		return tList;
	}


	IRelease lockedReleaseTap = null;

	@Override
	public void lockReleaseTap(IRelease t) {
		if (lockedReleaseTap == null && t != null) {
			lockedReleaseTap = t;
		}
	}

	@Override
	public ActorTap getLockedReleaseTap() {
		return (ActorTap)lockedReleaseTap;
	}

    boolean hasLockReleaseTap() {
        return lockedReleaseTap != null;
    }

	void unlockReleaseTap() {
        if (lockedReleaseTap != null) {
            lockedReleaseTap.onRelease(this, touched);
            lockedReleaseTap = null;
        }
	}

	public boolean freezeToggle() {
		return edit().getChain().getCtrl().Toggle();
	}

	public boolean releaseTap() {
        boolean rtn = false;
		if(hasLockReleaseTap())
            unlockReleaseTap();
        else if(selectedTap instanceof IRelease) {
			((IRelease) selectedTap).onRelease(this, touched);
            rtn = true;
		}

        clearSelectedTap();
		commitRegistration();
		return rtn;
	}

    public void clearSelectedTap() {
        family.clear();
        previousTap = selectedTap;
        selectedTap = null;
    }

	public boolean onUp() {
		if (!isNotCapturingActorTap())
    		round((IActorTap) selectedTap);
		return releaseTap();
	}

	public static int border = 700, border2 = 3 * border / 2;

	public boolean onDown(IPoint iPoint) {
		touched = iPoint;
		boolean rtn = capturePiece(iPoint);
		if(rtn)
			if (selectedTap instanceof IActorTap) {
				getEventHandler().addFocus((IActorTap) selectedTap);
			}

		return rtn;
	}

	public boolean onDownClear() {
		return true;
	}

	public boolean onFling(final float vx, final float vy) {
		if (vx < border && vx > -border && vy < border && vy > -border)
			return onUp();
		if (!isNotCapturingActorTap()) {
			_onFling((ActorTap) selectedTap, new WorldPoint(vx, vy).setDif());
		} else {
			_onFlingBackground(vx, vy);
		}
		return releaseTap();
	}

	private void _onFling(ActorTap t, IPoint vp) {
		editTap()._move(t)._in()
				.add(new Accel(vp).once()).save();
	}

	private void _onFlingBackground(final float vx, final float vy) {
		// Background center starts moving and slows down gradually.
		editTap().addActor(new IActor() {
            float delta = 0.03f;
            int t = 0;

            @Override
            public boolean actorRun(Actor act) {
                win.move(delta * -vx, delta * -vy);
                delta -= 0.003f;
                act.invalidate();
                return ++t < 10;
            }
        }).save();
	}

	public boolean onSingleTapConfirmed() {
		ITap t1 = selectedTap;
		if (t1 == null) {
			t1 = previousTap;
			previousTap = null;
		}
		if (t1== null) {
			return false;
		}
		Actor p = null;
		if (t1 instanceof IActorTap)
			p = toActor((ActorTap) t1);
		switch (editmode) {
		case REMOVE:
			if (p == null)
				return false;
			edit().remove(p);
			releaseTap();
			editmode = EditMode.ADD;
			return false;
		case RENEW:
			if (p == null)
				return false;
			edit().restart(p);
			releaseTap();
			editmode = EditMode.ADD;
			return false;
		default:
			if (t1 instanceof ISelectable)
				((ISelectable) t1).onSelected(this, touched);
		}
		return true;
	}

	public boolean onLongPress() {
		if (selectedTap instanceof IPressed)
			((IPressed) selectedTap).onPressed();

		if (isNotCapturing())
			return false;
		return true;
	}

	public boolean onLockedScroll(final IPoint wp) {
		if (isNotCapturing())
			return false;
		if (selectedTap instanceof ILockedScroll)
			((ILockedScroll)selectedTap).onLockedScroll(this, selectedTap, wp);
		return true;
	}

	public boolean onScroll(final IPoint vp, final IPoint pos) {
		touched = pos;
		if (!isNotCapturing()) {
			if (selectedTap instanceof IScrollable) {
				((IScrollable) selectedTap).onScrolled(this, pos, vp);
			}
            onShowFamily();
		} else {
			win.move(-vp.x(), -vp.y());
		}
		kickTapDraw(selectedTap);
		return true;
	}

    void onShowFamily() {
        if (family.isEmpty()) {
            for (IActorTap tap : getTaps()) {
                if (!tap.equals(selectedTap)) {
                    if (tap.isFamilyTo((IActorTap) selectedTap)) {
                        family.add(tap);
                    }
                }
            }
        }
        for(IActorTap tap: family) {
            showFamily(tap);
        }
    }

    public void showFamily(IActorTap tap) {
    }

    public boolean onShowPress() {
		return false;
	}

	/**
	 * Create an IPiece instance and addFocusable to Chain.
	 *
	 * @param key
	 *            the factory in which IPiece blueprint is registered.
	 * @param num
	 *            the number of the IPiece blueprint
	 * @param pos
	 *            position where the IPiece instance sho0uld be added
	 * @return the IPiece instance created and the Tap instance associated with
	 *         the IPiece instance
	 */
	public EditorReturn onAdd(FACTORY_KEY key, int num,
			IPoint pos) {
        Factory<Actor> f = factories.get(key);
		// Invalid parameter
        if (num >= f.getSize())
            return null;
		if (pos == null) {
			pos = getNextPos();
			if (pos == null)
				pos = new WorldPoint(0f, 0f);
		}

		// Create new instance piece
		IPoint setPos = checkRoom(pos);
        IBlueprint b = f.get(num);
        EditorReturn rtn = add(b, setPos);
        captureTap(rtn.getTap());
		if (rtn == null) {
			log("Chain", "Fatal Error: no instance");
			return null;
		}
		// Get PieceView
		if (key == FACTORY_KEY.ALL)
			getFactory(FACTORY_KEY.RECENT).Register(b);
		touched = pos;
		onUp();
		return rtn;
	}

    EditorReturn add(IBlueprint<Actor> blueprint, IPoint iPoint) {
        try {
            EditorReturn rtn = editorManager.addAndInstallView(blueprint, iPoint);
            editorManager.save();
            Actor actor = rtn.getActor();
            IActorTap tap = rtn.getTap();
            getEventHandler().onAdd(actor, tap, blueprint, iPoint);
            getEventHandler().addFocus(tap);
//            tap.postAdd(actor, tap, blueprint, iPoint);
            return rtn;
        } catch (ChainException e) {
            editorManager.error(e);
        }
        return null;
    }

    public ActorTap createView(Factory<Actor> f, int num, ActorManager manager)
			throws ChainException {
		ActorTap tp = (ActorTap) f.getViewBlueprint(num).newInstance(manager);
		return tp;
	}

	IPoint getPointOnAdd(IPoint iPoint) {
		return (styles == null || getInteract() == null) ? iPoint
				: getInteract().pointOnAdd(iPoint);
	}

	private void round(IActorTap startTap2) {
		if (styles == null || getInteract() == null)
			return;
		startTap2._valueSet(getInteract()
				.pointOnAdd((startTap2._valueGet())));

		TapLib.setTap(startTap2);
		kickTapDraw(startTap2);
	}

	public boolean onDummyAdd(FACTORY_KEY key, int num, IPoint iPoint) {
        ActorTap t = null;
        try {
            t = createView(getFactory(key), num, editTap());
        } catch (ChainException e) {
            editTap().error(e);
        }
        dummyTap = t;
		editTap().save();
		IPoint setPos = checkRoom(iPoint);
		dummyTap.setCenter(setPos);
		dummyTap.setAlpha(100);
		return true;
	}

	public boolean onDummyRemove() {
		if (dummyTap == null)
			return false;
		editTap().remove(dummyTap);
		dummyTap = null;
		return true;
	}

	public boolean onDummyScroll(IPoint iPoint) {
		if (dummyTap == null)
			return false;
		IPoint setPos = checkRoom(iPoint, dummyTap);
		if (!setPos.equals(dummyTap._valueGet())) {
			dummyTap._valueSet(setPos);
			kickTapDraw(dummyTap);
		}
		return true;
	}

	public IPoint checkRoom(IPoint basePos, IPiece... exclusive) {
		IPoint setPos = getPointOnAdd(basePos);
		while (true) {
			Collection<IActorTap> p = searchRoomPieces(setPos, exclusive);
			if (p != null && !p.isEmpty()) {
				setPos.plus(new WorldPoint(100, 0));
				continue;
			}
			break;
		}
		return setPos;
	}

	public void Compile() {
		editorManager.getChain().getOperator().reset();
		editorManager.getChain().setCallback(new IControlCallback() {
			public boolean onCalled() {
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
		editTap().getChain().Show(canvas);
	}

	public void userShow(Object canvas) {
		edit().getChain().Show(canvas);
	}

	/**
	 * Check if selected piece is now attaching to other pieces, and when it is
	 * true, connect it to them.
	 *
	 * @param t1
	 *            Selected piece view
	 * @param onlyInclude
	 *            Velocity of selected piece view
	 * @return True when selected piece was attached.
	 */
	@Override
	public boolean checkAndAttach(IActorTap t1, boolean onlyInclude) {
		if (t1 == null)
			return false;
		boolean rtn = false;
		for (IActorTap t2 :  getTaps()
			) {
			if (_attach((ActorTap) t1, (ActorTap) t2, onlyInclude))
				rtn = true;
		}
		return rtn;
	}

	/**
	 * Connect selected piece to another target piece. When two are already
	 * connected, disconnect two into pieces.
	 *
	 * @param t1
	 * @param t2
	 * @param onlyInclude
	 * @return True when selected piece was attached.
	 */
	boolean _attach(ActorTap t1, ActorTap t2, boolean onlyInclude) {
		if (t1 == t2) {
			return false;
		}
		InteractionType type = _checkInteractionType(t1, t2, onlyInclude);
		if (type == InteractionType.NONE) {
			return false;
		} else {
			Actor a1 = toActor(t1), a2 = toActor(t2);
			if (a1.isConnectedTo(a2)
					&& !EnumSet.of(InteractionType.OUTSIDE,
							InteractionType.CROSSING).contains(type)) {
				return false;
			}

			if ((type == InteractionType.INSIDE || type == InteractionType.CROSSING)
					&& t1 instanceof IAttachHandler)
				((IAttachHandler)t1).onInside(this, t2, a1, a2);


			// Connect/disconnect moving to/from target;
            if (!getEventHandler().onAttach(t1, t2, a1, a2, type)) {
                return false;
            }
		}

		return true;
	}

	/**
	 * Check if selected piece is now in position for deletion, and when it is
	 * true, delete this piece.
	 *
	 * //@param t
	 * //           Selected piece view.
	 * @return True when selected piece was deleted.
	 */
	public enum InteractionType {
		INSIDE, TOUCH_LEFT, TOUCH_RIGHT, TOUCH_TOP, TOUCH_BOTTOM, NONE, GOOUTSIDE, OUTSIDE, CROSSING
	}

	InteractionType _checkInteractionType(ActorTap t1, ActorTap t2,
			boolean onlyInclude) {
		IPiece p1 = toActor(t1);
		IPiece p2 = toActor(t2);
		if (p1 == null || p2 == null) {
			// log("Edit", "Checked Piece: NULL");
			return InteractionType.NONE;
		}
		IActionStyle style = getInteract();
		// Connected/or-not judgment
		if (p1.isConnectedTo(p2)) {
			return style.checkLeaveType(t1, t2);
		} else {
			return style.checkTouchType(t1, t2, onlyInclude);
		}
	}


	public class Accel extends Actor.Mover {
		float delta = 0.03f;
		int j = 0;
		IPoint wp = null;
		IPoint initial = null;
		Actor a = null;

		public Accel(IPoint vp) {
			super();
			setLinkClass(LinkType.PULL, IPoint.class);
			initial = wp = vp.copy().unsetDif();
		}

		@Override
		public boolean actorInit() throws ChainException {
			WorldPoint dummy = new WorldPoint();
			initEffectValue(dummy, 1);
			super.actorInit();
			j = 0;
			delta = 0.1f;
			if (initial == null)
				L( "Accel#reset").go(wp = pull());
			return true;
		}

		@Override
		public boolean actorRun(Actor act) throws ChainException {
			IPoint d = wp.multiplyNew(delta)/*
											 * new WorldPoint(wp.x() * delta,
											 * wp.y() * delta)
											 */;
			initEffectValue(d, 1);
			delta -= 0.01f;
			boolean rtn = ++j < 10 || d.getAbs() > 30;
			super.actorRun(act);
			if (checkAndAttach(((IActorTap) getTarget()), false)) {
				round((IActorTap) getTarget());
				return false;
			}
			if (!rtn) {
				ActorTap v = (ActorTap) getTarget();
				round(v);
				checkAndAttach(v, false);
				return false;
			}
			return rtn;
		}
	}


	public interface Tickable<T> {
		public int onTick(T t, Packet obj);
	}

	public interface Pushable<T> {
		public boolean onPush(T t, Object obj, ActorManager actorManager);
	}


	boolean standby = false;

	public IBlueprintInitialization standbyRegistration(Factory<?> f) {
		IBlueprintInitialization data = null;
		if (!standby)
			if (!isNotCapturing()) {
				IPiece p = getCapturedActorTap().getActor();
				String tag = p.getTag();
				if (p instanceof IValue) {
					Object v = ((IValue) p)._valueGet();
					data = standbyRegisteration(f, v, tag);
				}
				standby = true;
			}
		return data;
	}

	public IBlueprintInitialization standbyRegisteration(Factory<?> f,
			Object v, String tag) {
		IBlueprintInitialization data = null;
		IBlueprint b = f.search(tag);
		if (b != null) {
			if (b instanceof Blueprint) {
				IBlueprint bnew = b.copy();
				bnew.setTag(tag + "I");
				data = new BlueprintInitialization(v, tag + "I");
				bnew.setInitialization(data);
				f.Register(bnew);
			}
		}
		return data;
	}

	protected void commitRegistration() {
		standby = false;
	}

	public void registerBlueprint(Factory<?> f, IBlueprintInitialization bi) {
		String tag = bi.getTag();
		String t = tag.substring(0, tag.length() - 1);
		IBlueprint b2 = f.search(tag);
		if (b2 != null) {
			b2.setInitialization(bi);
			return;
		}
		IBlueprint b = f.search(t);
		if (b == null || !(b instanceof Blueprint)) {
            return;
        }
        IBlueprint bnew = b.copy();
        bnew.setTag(tag);
        bnew.setInitialization(bi);
        f.Register(bnew);
        f.save();

	}

	private IPoint getNextPos() {
		return nextConnectivityPoint;
	}

	public void setNextPos(IPoint nextConnectivity) {
		this.nextConnectivityPoint = nextConnectivity;
	}

	public void resetNextPos() {
		this.nextConnectivityPoint = null;
	}

    IActorTap highlighted = null;
	@Override
	public List<IBlueprint<Actor>> highlightConnectables(LinkType ac,
                                                         IActorTap target, ClassEnvelope classEnvelope) {
        List<IBlueprint<Actor>> rtn = setLastHighlighted(ac, target, classEnvelope);
        //Erase the indicator for the privious target.
        if (highlighted != null) {
            IActorTap last = highlighted.getAccessoryTap(ac);
            if (last != null && last instanceof IBlueprintFocusNotification) {
                ((IBlueprintFocusNotification) last).onFocus(null);
            }
        }

        //Create new indicator for the current target.
        highlighted = target;
        if (target == null)
            return null;
        IActorTap lt = target.getAccessoryTap(ac);
        if (lt != null && lt instanceof IBlueprintFocusNotification) {
            ((IBlueprintFocusNotification) lt).onFocus(ac.getBooleanSet());
        }
        return rtn;
	}

    public List<IBlueprint<Actor>> setLastHighlighted(LinkType ac, IActorTap target, ClassEnvelope classEnvelope) {
        changePaletteToConnectables(ac.reverse(), classEnvelope);
        //Clear all blueprints view with neutral coloring.
        for (IBlueprint<Actor> b : getFactory().getList()) {
            b.unhighlight();
        }
        //Coloring connectable blueprints with LinkType's color.
        List<IBlueprint<Actor>> bl = getFactory().getConnectables(ac, classEnvelope);
        if (bl.size() == 0) {
            getFactory().invalidate();
            return null;
        }
        for (IBlueprint<Actor> b : bl) {
            b.highlight(ac, true);
        }
        getFactory().invalidate();
        return bl;
    }


    public void unhighlightConnectables() {
		for(IBlueprint b: getFactory().getList()) {
			b.unhighlight();
		}
	}

	public void changePaletteToConnectables(LinkType ac,
			ClassEnvelope classEnvelope) {
		getFactory().setRelatives(ac, classEnvelope, relatives);
		win.showPalette(PaletteSort.RELATIVES);
	}

	@Override
	public boolean connect(Actor a1, LinkType al, Actor a2) {
		if (al == null)
			return false;
		switch (al) {
		case PUSH:
			return edit().append(a2, PathType.OFFER, a1,
					PathType.OFFER, true) != null;
		case PULL:
			return edit().append(a1, PathType.OFFER, a2,
					PathType.OFFER, true) != null;
		case TO_CHILD:
			return edit().append(a2, PathType.FAMILY, a1,
					PathType.FAMILY, true) != null;
		case FROM_PARENT:
			return edit().append(a1, PathType.FAMILY, a2,
					PathType.FAMILY, true) != null;
		default:
			return false;
		}
	}

}
