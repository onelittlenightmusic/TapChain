package org.tapchain.editor;

import org.tapchain.ActorTap;
import org.tapchain.PathTap;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.ActorInputException;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ActorPullException;
import org.tapchain.core.Blueprint;
import org.tapchain.core.Chain;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ChainPiece.PieceState;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IConnectHandler;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.Packet;
import org.tapchain.core.PathType;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class EditorManager extends ActorManager {
	ActorManager tapManager;
	IActorConnectHandler actorConnectHandler;
	Blueprint blueprintForPathTap;

	ConcurrentHashMap<Actor, IActorTap> dictPiece = new ConcurrentHashMap<Actor, IActorTap>();
	ConcurrentHashMap<IPath, PathTap> dictPath = new ConcurrentHashMap<IPath, PathTap>();

	Map<PieceState, Actor> plist = new EnumMap<PieceState, Actor>(
			PieceState.class);
	private int intervalMs = 0;

	public EditorManager() {
		super();
		tapManager = new ActorManager();
		getTapManager().createChain(50).getChain().setName("System");
		createChain(100).getChain().setAutoEnd(false).setName("User");
	}

	public EditorManager(EditorManager e) {
		super(e);
		tapManager = e.tapManager;
		dictPiece = e.dictPiece;
		dictPath = e.dictPath;
	}

	public ActorManager setPathBlueprint(Blueprint p) {
		blueprintForPathTap = p;
		return this;
	}

	public ActorManager setActorConnectHandler(IActorConnectHandler a) {
		actorConnectHandler = a;
		return this;
	}

	public ActorManager getTapManager() {
		return tapManager;
	}

	public Collection<IActorTap> getTaps() {
		return dictPiece.values();
	}

	public Collection<Actor> getActors() {
		return dictPiece.keySet();
	}

	public IActorTap getTap(Actor bp) {
		if (dictPiece.get(bp) != null)
			return dictPiece.get(bp);
		return null;
	}

	public PathTap getTapPath(IPath path) {
		if (dictPath.get(path) != null)
			return dictPath.get(path);
		return null;
	}

	protected void init() {
		Actor ptmp = null;
		List<Integer> colors = Arrays.asList(0xff447744, 0xf447744,
				0xff777777, 0xff447777, 0xff774444);
		for (PieceState state : PieceState.values()) {
			ptmp = new Actor.Colorer().color_init(colors.get(state.ordinal()))
					.setParentType(PathType.OFFER).boost();
			tapManager.add(ptmp).save();
			plist.put(state, ptmp);
		}
	}

	protected void setAllCallback(IControlCallback control) {
		getTapManager().SetCallback(control);
		SetCallback(control);
	}

	public void onMoveView(IView v, IPoint wp) {
		v.setCenter(wp);
		return;
	}

	public EditorReturn addAndInstallView(IBlueprint<Actor> blueprint, IPoint nowPoint) throws ChainException {
        Actor rtn = blueprint.newInstance(this);
        Blueprint view = (Blueprint)blueprint.getView();
        if (view != null) {
            ActorTap v = __setPieceView(rtn, view);
            if (v != null)
                onMoveView(v, nowPoint);
            return new EditorReturn(rtn, v);
        }
        return new EditorReturn(rtn, null);
	}

	ActorTap __setPieceView(Actor actor, final Blueprint bp)
			throws ChainException {
		final ActorTap _view;
		final ActorManager manager = getTapManager();
		_view = (ActorTap) bp.newInstance(manager);
		if (_view == null)
			throw new ChainException(actor, "view not created");
		manager.save();
		dictPiece.put(actor, _view);
		if(actor.getLogLevel())
			((ChainPiece)_view).setLogLevel(true);
		_view.setMyActor(actor);
        actor.setStatusHandler(new IStatusHandler<IPiece>() {
            @Override
            public void changeViewState(PieceState state) {
                synchronized (_view) {
                    plist.get(state).offer(_view);
                }
                _view.changeState(state);
            }

            @Override
            public int tickView(IPiece p, Packet packet) {
                return _view.onTick((Actor) p, packet);
            }

            @Override
            public void pushView(IPiece t, Object obj) {
                if (_view.onPush((Actor) t, obj, manager))
                    getChain().kick(_view);
            }


            @Override
            public int getTickInterval() {
                return getPathInterval();
            }
        });
//        actor.setError(getErrorHandler());
        if(_view instanceof IErrorHandler)
            actor.setError((IErrorHandler)_view);
		return _view;
	}

	@Override
	public Chain.ConnectionResultPath connect(Actor x, PathType xp, Actor y,
											PathType yp, boolean addView) {
		Chain.ConnectionResultPath rtn = super.connect(x, xp, y, yp, addView);
		try {
			if(rtn != null) {
				if(addView && blueprintForPathTap != null) {
					//PathTap instantiation
					IActorTap xTap = getTap(x), yTap = getTap(y);
					IBlueprint newBlueprintForPath = blueprintForPathTap.copyAndRenewArg()
							.addArg(yTap, xTap, yp, xp, rtn.getResult());
					IPathTap pathTap = __setPathView(rtn.getResult(), newBlueprintForPath);

					//Post process 1 ( invoking common handler )
					if(actorConnectHandler != null)
						actorConnectHandler.onConnect(yTap, pathTap, xTap, LinkType.fromPathType(yp, true));

					//Post process 2 ( invoking individual handler if registered )
					for(IActorTap iTap: Arrays.asList(xTap, yTap))
						if(iTap instanceof IConnectHandler)
							((IConnectHandler)iTap).onConnect(yTap, pathTap, xTap, LinkType.fromPathType(yp, true));
					save();
				}
			}
		} catch (ChainException e) {
			e.printStackTrace();
		}
		logLocal("connect ended [%s]", toString(x, xp, y, yp));

		return rtn;
	}

	IPathTap __setPathView(IPath path, IBlueprint _vReserve) {
		final PathTap _view;
		try {
			ActorManager manager = getTapManager();
			_view = (PathTap) _vReserve.newInstance(manager);
			manager.save();
			_view.setEditor(manager);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (_view == null)
			return null;
		dictPath.put(path, _view);
		_view.setMyPath(path);
		path.setStatusHandler(new IStatusHandler<IPath>() {
            @Override
            public int tickView(IPath path, Packet obj) {
                return _view.onTick(path, obj);
            }

            @Override
            public void changeViewState(PieceState state) {
            }

            @Override
            public void pushView(IPath t, Object obj) {
            }

            @Override
            public int getTickInterval() {
                return getPathInterval();
            }
        });
		return _view;
	}

	@Override
	public ActorManager unsetPieceView(Actor bp) {
		__unsetView(bp);
		return this;
	}

	void __unsetView(Actor bp) {
		if (bp == null)
			return;
		IActorTap v = getTap(bp);
		dictPiece.remove(bp);
		if (v == null)
			return;
		if(v instanceof ITapControlInterface)
			((ITapControlInterface)v).unsetActor();
		getTapManager().remove((Actor) v);
		return;
	}

	@Override
	public IPath disconnect(IPiece x, IPiece y) {
		IPath rtn = super.disconnect(x, y);
		__unsetPathView(rtn);
		return rtn;
	}

	@Override
	public IPath disconnect(IPath path) {
		super.disconnect(path);
		__unsetPathView(path);
		return path;
	}

	void __unsetPathView(IPath path) {
		if (path == null)
			return;
		PathTap v = getTapPath(path);
		dictPath.remove(path);
		if (v == null)
			return;
		v.unsetMyPath();
		getTapManager().remove(v);
		return;
	}

	public void setPathInterval(int intervalMs) {
		this.intervalMs = intervalMs;
		return;
	}

    public int getPathInterval() {
        return intervalMs;
    }

	public void onRefreshView(Actor bp, Actor obj) {
		IActorTap v = dictPiece.get(bp);
		dictPiece.remove(bp);
		if (bp.compareTo(obj) > 0) {
			bp.initNum();
		}
		dictPiece.put(bp, v);
	}

	@Override
	public ActorManager refreshPieceView(Actor bp, Actor obj) {
		onRefreshView(bp, obj);
		return this;
	}

}
