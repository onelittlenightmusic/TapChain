package org.tapchain.editor;

import org.tapchain.ActorTap;
import org.tapchain.PathTap;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.ActorManager;
import org.tapchain.core.Blueprint;
import org.tapchain.core.Chain;
import org.tapchain.core.ChainException;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ChainPiece.PieceState;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IConnectHandler;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.ILogHandler;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.Packet;
import org.tapchain.core.PathType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TapManager extends ActorManager implements IActorManager {
	Map<PieceState, Actor> plist = new EnumMap<>(
            PieceState.class);

    /**
     * Constructor
     */
//    public Editor(Chain actorChain, Chain systemActorChain) {
    public TapManager(ITapChain actorChain) {
        super((Chain)actorChain);
//        setChain(actorChain);
//        tapManager = new ActorManager(systemActorChain);

//        Actor ptmp;
//        List<Integer> colors = Arrays.asList(0xff447744, 0xf447744,
//                0xff777777, 0xff447777, 0xff774444);
//        for (PieceState state : PieceState.values()) {
//            ptmp = new Effector.Colorer().color_init(colors.get(state.ordinal()))
//                    .setParentType(PathType.OFFER).boost();
//            tapManager.add(ptmp).save();
//            plist.put(state, ptmp);
//        }
    }

	private int intervalMs = 0;

    /***
     * Copy constructor
     * @param e EditorManager to copy
     */
	public TapManager(TapManager e) {
		super(e);
//		tapManager = e.tapManager;
//		dictPiece = e.dictPiece;
//		dictPath = e.dictPath;
	}

    @Override
    public EditorChain getChain() {
        return (EditorChain)super.getChain();
    }

	public ActorManager editTap() {
		return new ActorManager(getChain().getSystemChain());
	}

	protected void setAllCallback(IControlCallback control) {
		editTap().SetCallback(control);
		SetCallback(control);
	}

	public void onMoveView(IView v, IPoint wp) {
		v.setCenter(wp);
    }

	protected EditorReturn addAndInstallView(IBlueprint<Actor> blueprint, IPoint nowPoint) throws ChainException {
        Actor rtn = blueprint.newInstance();
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
		final ActorTap _view = (ActorTap) bp.newInstance();
		if (_view == null)
			throw new ChainException(actor, "view not created");
		getChain().putPiece(actor, _view);
		if(actor.getLogLevel())
			((ChainPiece)_view).setLogLevel(true);
		_view.setMyActor(actor);
        actor.setStatusHandler(new IStatusHandler<IPiece>() {
            @Override
            public void changeViewState(PieceState state) {
//                synchronized (_view) {
//                    plist.get(state).offer(_view);
//                }
//                _view.changeState(state);
            }

            @Override
            public int tickView(IPiece p, Packet packet) {
                return _view.onTick((Actor) p, packet);
            }

            @Override
            public void pushView(IPiece t, Object obj) {
                if (_view.onPush((Actor) t, obj))
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
			if(rtn != null) {
				if(addView && getChain().blueprintForPathTap != null) {
					//PathTap instantiation
					IActorTap xTap = getChain().toTap(x), yTap = getChain().toTap(y);
					IBlueprint newBlueprintForPath = getChain().blueprintForPathTap.copyAndRenewArg()
							.addArg(yTap, xTap, yp, xp, rtn.getResult());
					IPathTap pathTap = __setPathView(rtn.getResult(), newBlueprintForPath);

					//Post process 1 ( invoking common handler )
					if(getChain().actorConnectHandler != null)
						getChain().actorConnectHandler.onConnect(yTap, pathTap, xTap, LinkType.fromPathType(yp, true));

					//Post process 2 ( invoking individual handler if registered )
					for(IActorTap iTap: Arrays.asList(xTap, yTap))
						if(iTap instanceof IConnectHandler)
							((IConnectHandler)iTap).onConnect(yTap, pathTap, xTap, LinkType.fromPathType(yp, true));
					save();
				}
			}
//		logLocal("link ended [%s]", toString(x, xp, y, yp));

		return rtn;
	}

	IPathTap __setPathView(IPath path, IBlueprint _vReserve) {
		final PathTap _view;
		try {
			_view = (PathTap) _vReserve.newInstance();
            if (_view == null)
                return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		getChain().putPath(path, _view);
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
		IActorTap v = getChain().toTap(bp);
		getChain().removePiece(bp);
		if (v == null)
			return;
		if(v instanceof ITapControlInterface)
			((ITapControlInterface)v).unsetActor();
		editTap().remove((Actor) v);
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
		PathTap v = getChain().getTapPath(path);
		getChain().removePath(path);
		if (v == null)
			return;
		v.unsetMyPath();
		editTap().remove(v);
    }

	public void setPathInterval(int intervalMs) {
		this.intervalMs = intervalMs;
    }

    public int getPathInterval() {
        return intervalMs;
    }

	public void onRefreshView(Actor bp, Actor obj) {
		IActorTap v = getChain().toTap(bp);
		getChain().removePiece(bp);
		if (bp.compareTo(obj) > 0) {
			bp.initNum();
		}
		getChain().putPiece(bp, v);
	}

    @Override
    public void setLog(ILogHandler l) {
        super.setLog(l);
        editTap().setLog(l);
    }
}
