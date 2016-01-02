package org.tapchain.editor;

import android.os.AsyncTask;

import org.tapchain.ActorTap;
import org.tapchain.IFocusable;
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
import org.tapchain.core.PathType;
import org.tapchain.core.StyleCollection;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.game.ISensorView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * TapChainEditor is a model for TapChain.
 * TapChainEditor contains actor blueprints (Factory),
 * style for tap instantiation and ActorManager for editing actors and taps.
 */
@SuppressWarnings("serial")
public abstract class TapChainEditor implements IControlCallback, ILogHandler,
		IActorEditor {
	IWindow win = null;
	public EditorManager editorManager = new EditorManager();
    HashMap<FACTORY_KEY, Factory<Actor>> factories = new HashMap<>();
	Factory<Actor> factory = new Factory<Actor>(),
			recent = new Factory<>(), relatives = new Factory<>(),
			goalFactory = new Factory<>();
	protected ActorBlueprintManager<Actor> blueprintManager = new ActorBlueprintManager<>(
			factory), goalBlueprintManager = null;
	private StyleCollection styles = null;
	Actor move = new Actor().setLinkClass(LinkType.PUSH, IPoint.class);
	Actor.Mover move_ef = (Mover) new Actor.Mover()
			.initEffectValue(new WorldPoint(0f, 0f), 1)
			.setParentType(PathType.OFFER).boost();
	protected List<Geometry> geos = new ArrayList<Geometry>();
	private IPoint nextConnectivityPoint = null;
//    ArrayList<IActorTap> family = new ArrayList<IActorTap>();
    ISensorView sensorView = null;
    public enum FACTORY_KEY {
        ALL, LOG, RELATIVES
    }

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
						checkAndConnect((ActorTap) getTarget());
					return false;
				}
			}.setParentType(PathType.OFFER)
					.setLinkClass(LinkType.PULL, Object.class).boost())
			.teacher(move).save();
		setLog(this);
        factories.put(FACTORY_KEY.ALL, factory);
        factories.put(FACTORY_KEY.LOG, recent);
        factories.put(FACTORY_KEY.RELATIVES, relatives);
        if(win instanceof ISensorView)
            sensorView = (ISensorView)win;
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

	public void setWindow(IWindow v) {
		win = v;
	}

	protected IWindow getWindow() {
		return win;
	}

	public Factory<Actor> getFactory() {
		return factory;
	}

    /**
     * Get factory from factory key
     * @param key factory key
     * @return factory designated by factory key
     */
    public Factory<Actor> getFactory(FACTORY_KEY key) {
        switch(key) {
            case ALL:
                return factory;
            case LOG:
                return recent;
            case RELATIVES:
                return relatives;
            default:
                return factory;
        }
    }

    /**
     * Get a manager for editing only taps (not actors).
     * For adding or removing simple taps such as path taps, this is the first step.
     * @return ActorManager for editing taps
     */
	@Override
	public ActorManager editTap() {
		return editorManager.getTapManager()/*.newSession()*/;
	}

    /**
     * Get a manager for editing actor and actor tap.
     * For adding or removing actor, this is the first step.
     * And when you add actor by this return, actor tap will be added automatically by
     * instantiating tap blueprint style that you set.
     * @return Actormanager for editing actors.
     */
	@Override
	public ActorManager edit() {
		return editorManager/*.newSession()*/;
	}

    /**
     * Get a collection of all taps
     * @return collection of all taps
     */
    @Override
	public Collection<IActorTap> getTaps() {
		return editorManager.getTaps();
	}

    /**
     * Get a collection of all actors
     * @return collection of all actors
     */
    @Override
	public Collection<Actor> getActors() {
		return editorManager.getActors();
	}

    /**
     * Get
     * @param pt
     * @return
     */
	private Collection<IActorTap> getTaps(IPoint pt) {
		return TapLib.getTaps(pt);
	}

	public BlueprintManager<Actor> getBlueprintManager() {
		return blueprintManager;
	}

    /**
     * Get tap from actor
     * @param actor actor
     * @return tap
     */
	@Override
	public IActorTap toTap(Actor actor) {
		return editorManager.getTap(actor);
	}

    /**
     * Get actor from tap
     * @param tap tap
     * @return actor
     */
	@Override
	public Actor toActor(IActorTap tap) {
		return tap.getActor();
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

    /**
     * Set StyleCollection. This is the first step for instantiation actors and taps.
     * @param s
     */
	public void setStyle(StyleCollection s) {
		styles = s;
		blueprintManager.setOuterInstanceForInner(s.getOuter());
		blueprintManager.setDefaultView(s.getView());
		Blueprint connbp = blueprintManager.createTapBlueprint(s.getConnect());
		editorManager.setPathBlueprint(connbp);
		editorManager.setActorConnectHandler(s.getConnectHandler());
		goalBlueprintManager.setOuterInstanceForInner(s.getOuter());
	}

	private IActorSharedHandler getEventHandler() {
		if (styles == null)
			return null;
		return styles.getEventHandler();
	}

	@Override
	public void kickTapDraw(ITap startTap2) {
		editorManager.getTapManager().getChain().kick(startTap2);
	}

	public enum EditMode {
		ADD, REMOVE
	}

	EditMode editmode = EditMode.ADD;

	protected ITap searchTouchedTap(IPoint iPoint, IPiece... exclusive) {
		Collection<ITap> ps = TapLib.getAllSystemPieces();
		ps.removeAll(Arrays.asList(exclusive));
		for (ITap f : ps) {
			if (f.contains(iPoint)) {
				return f;
			}
		}
		return null;
	}

//	protected Collection<IActorTap> searchRoomPieces(IPoint iPoint,
//			IPiece... exclusive) {
//		Collection<IActorTap> tList = getTaps(iPoint);
//		if (tList == null || tList.size() <= 0)
//			return null;
//		tList.removeAll(Arrays.asList(exclusive));
//		return tList;
//	}
//

	IRelease lockedReleaseTap = null;

	public void lockReleaseTap(IRelease t) {
		if (lockedReleaseTap == null && t != null) {
			lockedReleaseTap = t;
		}
	}

    boolean hasLockReleaseTap() {
        return lockedReleaseTap != null;
    }

	boolean unlockReleaseTap(IPoint point) {
        boolean rtn = false;
        if (lockedReleaseTap != null) {
            rtn = lockedReleaseTap.onRelease(this, point);
            lockedReleaseTap = null;
        }
        return rtn;
	}

	public boolean freezeToggle() {
		return edit().getChain().getCtrl().Toggle();
	}

	public boolean releaseTap(IActorTap selected, IPoint point) {
        boolean rtn = false;
        /*else */
        if(hasLockReleaseTap() && unlockReleaseTap(point))
            return true;
        if(selected instanceof IRelease) {
			((IRelease) selected).onRelease(this, point);
            rtn = true;
		}

//        clearSelectedTap();
		return rtn;
	}

//    public void clearSelectedTap() {
////        family.clear();
//    }

	public static int vector_max = 700;//, border2 = 3 * vector_max / 2;

    /**
     * Touch down on one point and return tap which is located on the point.
     * @param iPoint
     *          down point
     * @return
     */
	public ITap onDown(IPoint iPoint) {
        ITap t = searchTouchedTap(iPoint);
        return t;//captureTap(t);
	}

    /**
     * Fling with vector
     * @param selected Target actor tap
     * @param vector Fling vector
     * @return True when selected tap is null
     */
	public boolean onFling(IActorTap selected, IPoint vector) {
		if (vector.x() < vector_max && vector.x() > -vector_max && vector.y() < vector_max && vector.y() > -vector_max)
			return true;//onUp(point);
		if (selected != null) {
			_onFling((ActorTap) selected, new WorldPoint(vector).setDif());
		} else {
			_onFlingBackground(vector);
		}
		return true;
	}

	private void _onFling(ActorTap t, IPoint vp) {
		editTap()._move(t)._in()
				.add(new Accel(vp).once()).save();
	}
    private void _onFlingBackground(IPoint v) {
        moveBackground(-0.2f*v.x(), -0.2f*v.y());
    }

    private void _onFlingBackgroundTo(float x, float y) {
        IPoint center = win.getMiddlePoint();
        moveBackground(x - center.x(), y - center.y());
    }

    private void moveBackground(final float dx, final float dy) {
		// Background center starts moving and slows down gradually.
		editTap().addActor(new IActor() {
            float delta = 0.1f;
            int t = 0;

            @Override
            public boolean actorRun(Actor act) {
                delta = 0.15f * (float)Math.sqrt((10f - t) / 10f);
                win.move(delta * dx, delta * dy);
                act.invalidate();
                return ++t < 10;
            }
        }).save();
	}

    /**
     * Single touch.
     * @param point touched point
     * @return
     */
	public boolean onSingleTouch(IPoint point) {
		ITap t1 = searchTouchedTap(point);
		if (t1== null) {
			return false;
		}
		Actor p = null;
		if (t1 instanceof IActorTap) {
            p = toActor((ActorTap) t1);
            getEventHandler().createFocus((IActorTap) t1);
        }
		switch (editmode) {
		case REMOVE:
			if (p == null)
				return false;
			edit().remove(p);
			return false;
		default:
			if (t1 instanceof ISelectable)
				((ISelectable) t1).onSelected(this, point);
		}
		return true;
	}

	public boolean onLongPress(IActorTap selected) {
		if (selected instanceof IPressed)
			((IPressed) selected).onPressed();
		return true;
	}

	public boolean onLockedScroll(IActorTap selected, final IPoint wp) {
		if (selected instanceof ILockedScroll)
			((ILockedScroll)selected).onLockedScroll(this, selected, wp);
		return true;
	}

    /**
     * Scroll tap by difference vector
     * @param selected target tap
     * @param vp vector
     * @param pos position
     * @return true
     */
	public boolean scroll(IActorTap selected, final IPoint vp, final IPoint pos) {
		if (selected == null) {
            return false;
        }
        if (selected instanceof IScrollable) {
//                Log.w("test", String.format("%s scrolled", selectedTap));
            ((IScrollable) selected).onScrolled(this, pos, vp);
        }
		kickTapDraw(selected);
		return true;
	}

    private IBlueprint factoryToBlueprint(FACTORY_KEY key, int num) {
        Factory<Actor> f = factories.get(key);
        // Invalid number input
        if (num >= f.getSize())
            return null;
        return f.get(num);
    }

    private IBlueprint factoryToBlueprint(FACTORY_KEY key, String tag) {
        Factory<Actor> f = factories.get(key);
        IBlueprint b = f.search(tag);
        if (b == null)
            return null;
        return b;
    }
    /**
     * Create an actor instance from its blueprint.
     *
     * @param key
     *           the key for the factory in which actor blueprint is registered
     * @param num
     *            the number of the actor blueprint
     * @param pos
     *            position where the actor instance sho0uld be added
     * @return EditorReturn including the created actor instance and the its tap instance
     */
    public EditorReturn addActorFromBlueprint(FACTORY_KEY key, int num,
                                              IPoint pos) {
        return addActorFromBlueprint(factoryToBlueprint(key, num), pos);
    }

    /**
     * Create an actor instance from its blueprint.
     * @param key
     *           the key for the factory in which actor blueprint is registered
     * @param tag
     *           the tag of the actor blueprint
     * @param pos
     *           position where the actor instance sho0uld be added
     * @return EditorReturn including the created actor instance and the its tap instance
     */
    public EditorReturn addActorFromBlueprint(FACTORY_KEY key, String tag,
                                              IPoint pos) {
        return addActorFromBlueprint(factoryToBlueprint(key, tag), pos);
    }

    /**
     * Create an actor instance from its blueprint.
     *
     * @param b
     *            the actor blueprint
     * @param pos
     *            position where the actor instance is added
     * @return EditorReturn including the created actor instance and the its tap instance
     */
    public EditorReturn addActorFromBlueprint(IBlueprint b, IPoint pos) {
        if(b == null)
            return null;
        if (pos == null) {
            pos = getNextPos();
        }
        EditorReturn rtn = addActor(b, pos);
        if(!win.isInWindow(pos.x(), pos.y()))
            //Centering
            _onFlingBackgroundTo(pos.x(), pos.y());
        return rtn;
    }

    /**
	 * Create an actor instance from its blueprint.
	 *
	 * @param blueprint
	 *            the actor blueprint
	 * @param iPoint
	 *            position where the actor instance is added
	 * @return the created actor instance and the its tap instance
	 */

    private EditorReturn addActor(final IBlueprint<Actor> blueprint, final IPoint iPoint) {
        try {
            // Create new instance actor
            EditorReturn rtn = editorManager.addAndInstallView(blueprint, iPoint);
            editorManager.save();
            final Actor actor = rtn.getActor();
            final IActorTap tap = rtn.getTap();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    getEventHandler().onAdd(actor, tap, blueprint, iPoint);
                    getEventHandler().createFocus(tap);
                    getFactory(FACTORY_KEY.LOG).Register(blueprint);
                    return null;
                }
            }.execute();
            return rtn;
        } catch (ChainException e) {
            editorManager.error(e);
        }
        return null;
    }

	private void round(IActorTap startTap2) {
		if (styles == null || getInteract() == null)
			return;
		startTap2._valueSet(getInteract()
                .pointOnAdd((startTap2._valueGet())));

		TapLib.setTap(startTap2);
		kickTapDraw(startTap2);
	}

//	public IPoint checkRoom(IPoint basePos, IPiece... exclusive) {
//        return basePos;
//	}

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
	 * Check if an actor tap touches any other actor taps, then connect their actors.
	 *
	 * @param actorTap
	 *            a target actor tap which is checked
	 * @return True when a actor is checked and connected.
	 */
	@Override
	public boolean checkAndConnect(IActorTap actorTap) {
		if (actorTap == null)
			return false;
		boolean rtn = false;
		for (IActorTap t2 :  getTaps()) {
            if (actorTap == t2) {
                continue;
            }
			if (_attach((ActorTap) actorTap, (ActorTap) t2))
				rtn = true;
		}
        kickTapDraw(actorTap);
		return rtn;
	}

	/**
	 * Connect selected actor to another target actor. When two are already
	 * connected, nothing happens.
	 *
	 * @param t1 actor tap 1
	 * @param t2 actor tap 2
	 * @return True when selected actor was attached.
	 */
	private boolean _attach(ActorTap t1, ActorTap t2) {
		InteractionType type = _checkInteractionType(t1, t2);
        return getEventHandler().onAttach(t1, t2, toActor(t1), toActor(t2), type);
	}

	public enum InteractionType {
		INSIDE, TOUCH_LEFT, TOUCH_RIGHT, TOUCH_TOP, TOUCH_BOTTOM, NONE, GOOUTSIDE, OUTSIDE, CROSSING;
        public boolean touching() {
            switch(this) {
                case INSIDE:
                case TOUCH_LEFT:
                case TOUCH_BOTTOM:
                case TOUCH_RIGHT:
                case TOUCH_TOP:
                case CROSSING:
                    return true;
                default:
                    return false;
            }
        }
	}

	private InteractionType _checkInteractionType(ActorTap t1, ActorTap t2) {
		Actor p1 = toActor(t1);
		Actor p2 = toActor(t2);
		if (p1 == null || p2 == null) {
			return InteractionType.NONE;
		}
		IActionStyle style = getInteract();
		// Connected/or-not judgment
		if (p1.isConnectedTo(p2)) {
			return style.checkLeaveType(t1, t2);
		} else {
			return style.checkTouchType(t1, t2);
		}
	}


	public class Accel extends Actor.Mover {
		float delta = 0.03f;
		int j = 0;
		IPoint wp = null;
		IPoint initial = null;

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
			IPoint d = wp.multiplyNew(delta);
			initEffectValue(d, 1);
			delta -= 0.01f;
			boolean rtn = ++j < 10 || d.getAbs() > 30;
			super.actorRun(act);
			if (checkAndConnect(((IActorTap) getTarget()))) {
				round((IActorTap) getTarget());
				return false;
			}
			if (!rtn) {
				ActorTap v = (ActorTap) getTarget();
				round(v);
				checkAndConnect(v);
			}
			return rtn;
		}
	}

    /**
     * Standby registration
     * @param f target factory for registering
     * @param selected actor tap
     * @return Blueprint initialization instance
     */
    public IBlueprintInitialization standbyRegistration(Factory<?> f, IActorTap selected) {
        if(selected == null)
            return null;
        IBlueprintInitialization data = null;
        Actor p = selected.getActor();
        if (p instanceof IValue) {
            Object v = ((IValue) p)._valueGet();
            data = standbyRegistration(f, v, p.getTag());
        }
		return data;
	}

	private IBlueprintInitialization standbyRegistration(Factory<?> f,
                                                           Object initObj, String tag) {
		IBlueprint b = f.search(tag);
		if (b == null || !(b instanceof Blueprint)) {
            return null;
        }
        IBlueprint bnew = b.copy();
        bnew.setTag(String.format("%s_(%s)", tag, initObj.toString()));
        IBlueprintInitialization data = new BlueprintInitialization(initObj);
        bnew.setInitialization(data);
        f.Register(bnew);
        f.notifyView();
		return data;
	}

	private IPoint getNextPos() {
		if(nextConnectivityPoint == null)
            return win.getMiddlePoint();
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
        List<IBlueprint<Actor>> rtn = setLastHighlighted(ac, classEnvelope);
        //Reset privious highlighted target's highlight.
        if (highlighted != null) {
            IActorTap last = highlighted.getAccessoryTap(ac);
            if (last != null && last instanceof IBlueprintFocusNotification) {
                ((IBlueprintFocusNotification) last).onFocus(null);
            }
        }

        //Add the current target's highlight.
        highlighted = target;
        if (target == null)
            return null;
        IActorTap lt = target.getAccessoryTap(ac);
        if (lt != null && lt instanceof IBlueprintFocusNotification) {
            ((IBlueprintFocusNotification) lt).onFocus(ac.getBooleanSet());
        }
        return rtn;
	}

    public List<IBlueprint<Actor>> setLastHighlighted(LinkType ac, ClassEnvelope classEnvelope) {
        unhighlightAllConnectables();
        //Coloring connectable blueprints with LinkType's color.
        List<IBlueprint<Actor>> bl = getFactory().getConnectables(ac.reverse(), classEnvelope);
        if (bl.size() == 0) {
            win.showPalette(PaletteSort.FACTORY);
            return null;
        }
        relatives.setBlueprints(bl);
        win.showPalette(PaletteSort.RELATIVES);
        for (IBlueprint<Actor> b : bl) {
            b.highlight(ac);
        }
        getFactory().invalidate();
        return bl;
    }


    public void unhighlightAllConnectables() {
        //Clear all blueprints view with neutral coloring.
		for(IBlueprint b: getFactory().getList()) {
			b.unhighlight();
		}
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

    @Override
    public void shake(int duration) {
        if(sensorView != null)
            sensorView.shake(duration);
    }

    @Override
    public void changeFocus(LinkType al, IFocusable spot, ClassEnvelope clazz) {
        resetNextPos();
        highlightConnectables(al, spot, clazz);
        if(spot == null)
            return;
        setNextPos(spot.getCenter());
        getEventHandler().getFocusControl().unfocusAll(spot);
        spot.focus(getEventHandler().getFocusControl(), al);
        getEventHandler().getFocusControl().setSpotActorLink(al);
    }
}
