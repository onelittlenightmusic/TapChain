package org.tapchain.editor;

import org.tapchain.ActorTap;
import org.tapchain.core.Actor;
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
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IBlueprintInitialization;
import org.tapchain.core.ILogHandler;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.ISelectable;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.core.PathType;
import org.tapchain.core.StyleCollection;
import org.tapchain.core.TapLib;
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
	protected List<Geometry> geos = new ArrayList<Geometry>();
	private IPoint nextConnectivityPoint = null;
    ISensorView sensorView = null;
    public enum FACTORY_KEY {
        ALL, LOG, RELATIVES
    }
    public enum EditMode {
        ADD, REMOVE
    }

    EditMode editmode = EditMode.ADD;

	protected TapChainEditor(IWindow w) {
		setWindow(w);
		goalBlueprintManager = new ActorBlueprintManager<Actor>(goalFactory);
		editorManager.setAllCallback(this);
		editorManager.setPathInterval(1000);
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

    @Override
    public void log(String... s) {
        win.log(s);
        return;
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

    /**
     * Get
     * @param pt
     * @return
     */
	private Collection<IActorTap> getTaps(IPoint pt) {
		return TapLib.getTaps(pt);
	}

	public BlueprintManager<Actor> editBlueprint() {
		return blueprintManager;
	}


	@Override
	public IActorTap toTap(Actor actor) {
		return editorManager.getTap(actor);
	}


	@Override
	public Actor toActor(IActorTap tap) {
		return tap.getActor();
	}

    @Override
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
	public void invalidate() {
		editorManager.getTapManager().getChain().kick(null);
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
    //TODO: migrate to VIEW class (ex. CanvasViewImpl)

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
    //TODO: migrate to VIEW class (ex. CanvasViewImpl)

//	protected Collection<IActorTap> searchRoomPieces(IPoint iPoint,
//			IPiece... exclusive) {
//		Collection<IActorTap> tList = getTaps(iPoint);
//		if (tList == null || tList.size() <= 0)
//			return null;
//		tList.removeAll(Arrays.asList(exclusive));
//		return tList;
//	}
//

    //TODO: migrate to VIEW class (ex. CanvasViewImpl)
	IRelease lockedReleaseTap = null;

    //TODO: migrate to VIEW class (ex. CanvasViewImpl)
	public void lockReleaseTap(IRelease t) {
		if (lockedReleaseTap == null && t != null) {
			lockedReleaseTap = t;
		}
	}

    //TODO: migrate to VIEW class (ex. CanvasViewImpl)
    boolean hasLockReleaseTap() {
        return lockedReleaseTap != null;
    }

    //TODO: migrate to VIEW class (ex. CanvasViewImpl)
	boolean unlockReleaseTap(IPoint point) {
        boolean rtn = false;
        if (lockedReleaseTap != null) {
            rtn = lockedReleaseTap.onRelease(this, point);
            lockedReleaseTap = null;
        }
        return rtn;
	}

    //TODO: migrate to VIEW class (ex. CanvasViewImpl)
	public boolean releaseTap(IActorTap selected, IPoint point) {
        boolean rtn = false;
        /*else */
        if(hasLockReleaseTap() && unlockReleaseTap(point))
            return true;
        if(selected instanceof IRelease) {
			((IRelease) selected).onRelease(this, point);
            rtn = true;
		}

		return rtn;
	}

    public boolean freezeToggle() {
        return edit().getChain().getCtrl().Toggle();
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

    @Override
    public EditorReturn addActorFromBlueprint(FACTORY_KEY key, int num,
                                              IPoint pos) {
        return addActorFromBlueprint(factoryToBlueprint(key, num), pos);
    }

    @Override
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
            win.run(new Runnable() {
                public void run() {
                    getEventHandler().onAdd(actor, tap, blueprint, iPoint);
                    getEventHandler().createFocus(tap);
                    getFactory(FACTORY_KEY.LOG).Register(blueprint);
                }
            });
            return rtn;
        } catch (ChainException e) {
            editorManager.error(e);
        }
        return null;
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
        actorTap.invalidate();
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
