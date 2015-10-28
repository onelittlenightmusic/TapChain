package org.tapchain.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tapchain.ActorTap;
import org.tapchain.PathTap;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.ActorManager;
import org.tapchain.core.Blueprint;
import org.tapchain.core.Chain;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.LinkType;
import org.tapchain.core.PathType;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ChainPiece.PieceState;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;

@SuppressWarnings("unchecked")
public class EditorManager extends ActorManager {
	ActorManager tapManager;
	IActorConnectHandler actorConnectHandler;
	Blueprint pbp_connect;

	ConcurrentHashMap<Actor, IActorTap> dictPiece = new ConcurrentHashMap<Actor, IActorTap>();
	ConcurrentHashMap<IPath, PathTap> dictPath = new ConcurrentHashMap<IPath, PathTap>();

	Map<PieceState, Actor> plist = new EnumMap<PieceState, Actor>(
			PieceState.class);
	private int intervalMs = 0;

	public EditorManager() {
		super();
		tapManager = new ActorManager();
		getTapManager().createChain(40).getChain().setName("System");
		createChain(80).getChain().setAutoEnd(false).setName("User");
	}

	public EditorManager(EditorManager e) {
		super(e);
		tapManager = e.tapManager;
		dictPiece = e.dictPiece;
		dictPath = e.dictPath;
	}

	public ActorManager setPathBlueprint(Blueprint p) {
		pbp_connect = p;
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

	@Override
	public ActorManager installView(Actor bp, Blueprint _view, IPoint nowPoint) throws ChainException {
		ActorTap v = __setPieceView(bp, _view);
		if(v!=null)
			onMoveView(v, nowPoint);
		return this;
	}

	ActorTap __setPieceView(Actor cp2, final Blueprint bp)
			throws ChainException {
		final ActorTap _view;
		ActorManager manager = getTapManager();
		_view = (ActorTap) bp.newInstance(manager);
		if (_view == null)
			throw new ChainException(cp2, "view not created");
		manager.save();
		dictPiece.put(cp2, _view);
		if(((ChainPiece)cp2).getLogLevel())
			((ChainPiece)_view).setLogLevel(true);
		((ITapControlInterface)_view).setMyActor((Actor) cp2);
		if (cp2 instanceof ChainPiece) {
			ChainPiece c = ((ChainPiece) cp2);
			c.setStatusHandler(new IStatusHandler<IPiece>() {
				@Override
				public void changeViewState(PieceState state) {
					synchronized (_view) {
						plist.get(state).offer(_view);
					}
					_view.changeState(state);
				}

				@Override
				public int tickView(IPiece p, Object obj) {
					return _view.onTick((Actor) p, obj);
				}

				@Override
				public void pushView(IPiece t, Object obj) {
					if(_view.onPush((Actor) t, obj))
						getChain().kick(_view);
				}

				@Override
				public int getTickInterval() {
					return intervalMs;
				}

			});
			c.setError(getErrorHandler());
		}
		return _view;
	}

	@Override
	public Chain.ConnectionResultIO connect(Actor x, PathType xp, Actor y,
											PathType yp, boolean addView) {
		Chain.ConnectionResultIO rtn = super.connect(x, xp, y, yp, addView);
		try {
			if(rtn != null) {
				if(addView && pbp_connect != null) {
					IPathTap pathTap;
					IBlueprint vReserve = pbp_connect.copyAndRenewArg()
							.addArg(getTap(y), getTap(x), yp, xp, rtn.getResult());
					pathTap = __setPathView(rtn.getResult(), vReserve);
					if(actorConnectHandler != null)
						actorConnectHandler.onConnect(getTap(y), pathTap, getTap(x), LinkType.fromPathType(yp, true));
//					}
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
			public int tickView(IPath path, Object obj) {
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
				return intervalMs;
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
