package org.tapchain.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.tapchain.core.Actor.Mover;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.TapChainEdit.ISystemPath;
import org.tapchain.core.TapChainEdit.ISystemPiece;
import org.tapchain.core.ActorManager.*;
import org.tapchain.core.Chain.*;
import org.tapchain.core.ChainPiece.PieceState;


public class EditorManager extends ActorManager implements IPieceEdit, IPathEdit {
	protected ActorManager systemManager;
	//dictPiece must have object ordering and allow concurrent modification
	public ConcurrentSkipListMap<IPiece,ISystemPiece> dictPiece = new ConcurrentSkipListMap<IPiece, ISystemPiece>();
	
	//dictPiece must allow concurrent modification
	ConcurrentHashMap<IPath, ISystemPath> dictPath = new ConcurrentHashMap<IPath, ISystemPath>();
	
	Map<PieceState, Actor> plist = new EnumMap<PieceState, Actor>(PieceState.class);
	Actor move = new Actor();
	Actor.Mover move_ef = (Mover) new Actor.Mover().initEffect(new WorldPoint(0,0), 1).setParentType(PackType.HEAP).boost();
	
	public EditorManager() {
		super();
		setPieceEdit(this);
		setPathEdit(this);
		systemManager = new ActorManager();
		getSystemManager().createChain(20).getChain().setName("System");
		createChain(50).getChain().setAutoEnd(false).setName("User");
	}
	public void init() {
		Actor ptmp = null;
		List<Integer> colors = Arrays.asList(0xff80ff80, 0xff80ff80, 0xffffffff, 0xff8080ff, 0xffff8080);
		for(PieceState state: PieceState.values()) {
			ptmp = new Actor.Colorer().color_init(colors.get(state.ordinal())).setParentType(PackType.HEAP).boost();
			systemManager.add(ptmp)
			/*.teacher(ptmp = new Actor())*/._save();
			plist.put(state, ptmp);
		}
//		move.setName("MOVE_ENTRANCE");
		getSystemManager()
		.add(move)
		.student(move_ef)
//		.add(move_ef)
		._save();

	}
	public void setAllCallback(IControlCallback control) {
		getSystemManager().SetCallback(control);
		SetCallback(control);
	}
	public ActorManager getSystemManager() {
		return systemManager;
	}
	public Collection<ISystemPiece> getSystemPieces() {
		return dictPiece.values();
	}
	public Collection<IPiece> getUserPieces() {
		return dictPiece.keySet();
	}
	@Override
	public ISystemPiece getView(IPiece bp) {
		if (dictPiece.get(bp) != null)
			return dictPiece.get(bp);
		return null;
	}

	@Override
	public ISystemPath getView(IPath path) {
		if (dictPath.get(path) != null)
			return dictPath.get(path);
		return null;
	}

	@Override
	public ISystemPiece onSetPieceView(final IPiece cp2, final Blueprint bp) throws ChainException {
		final ISystemPiece _view;
			ActorManager manager = getSystemManager();
			_view = (ISystemPiece) bp.newInstance(manager);
			if(_view == null)
				throw new ChainException(cp2, "view not created");
			manager._save();
		dictPiece.put(cp2, _view);
		_view.setMyTapChain(cp2);
		if(cp2 instanceof ChainPiece) {
			ChainPiece c = ((ChainPiece)cp2);
			c.setStatusHandler(new IStatusHandler() {
				@Override
				public synchronized void getStateAndSetView(PieceState state) {
					plist.get(state).innerRequest(PackType.HEAP, _view);
				}
	
				@Override
				public void tickView() {
					_view.onTick();
				}
	
			});
			c.setError(getErrorHandler());
		}
		return _view;
	}

	@Override
	public void onSetPathView(IPath path, IBlueprint _vReserve) {
		final ISystemPath _view;
		try {
			ActorManager manager = getSystemManager();
			_view = (ISystemPath) _vReserve.newInstance(manager);
			manager._save();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if(_view == null)
			return;
		dictPath.put(path, _view);
		path.setStatusHandler(new IStatusHandler() {
			@Override
			public void tickView() {
				_view.onTick();
//				ia.start_animation(_view);
			}
			
			@Override
			public void getStateAndSetView(PieceState state) {
			}
		});
		return;
	}
	
	
	@Override
	public void onMoveView(IView v, IPoint wp) {
		move_ef.initEffect(wp==null?new WorldPoint(0,0):wp, 1);
		move.push(v);
//		move_ef.innerRequest(v);
//		v.setCenter(wp);
		return;
	}

	@Override
	public void onUnsetView(IPiece bp) {
		if(bp == null) 
			return;
//		if(bp instanceof ChainPiece) {
//			ChainPiece c = (ChainPiece)bp;
//			c.setStatusHandler(null);
//		}
		ISystemPiece v = getView(bp);
		if(v == null)
			return;
		v.unsetMyTapChain();
		dictPiece.remove(bp);
//		v.finish(Continue.END);
		getSystemManager().remove(v);
		return;
	}

	@Override
	public void onUnsetPathView(IPath path) {
		if(path == null) 
			return;
		ISystemPath v = getView(path);
		dictPath.remove(path);
		if(v == null)
			return;
//		v.finish(Continue.END);
		getSystemManager().remove((IPiece)v);
		return;
	}

	@Override
	public void onRefreshView(IPiece bp, IPiece obj) {
		ISystemPiece v = dictPiece.get(bp);
		dictPiece.remove(bp);
		if (((Actor)bp).compareTo((Actor)obj) > 0) {
			((Actor)bp).initNum();
		}
		dictPiece.put(bp, v);
	}

//	public static class Initializer {
//		Actor bp;
//		List<IPiece> dump;
//		static HashMap<Class<? extends IEditAnimation>, Actor>dict
//			= new HashMap<Class<? extends IEditAnimation>, Actor>();
//		public Initializer() {
//		}
//		public void init_animation(ActorManager maker, IEditAnimation a) {
//			if(bp != null) return;
//			bp = new Actor();
//			PieceManager manager = maker._func(bp);
//			a.init_animation(manager);
//			manager._exit().save();
//			dump = maker.dump();
//		}
//		public void start_animation(Object target) {
//			if(bp != null)
//				bp.push(target);
//		}
//		public void term_animation() {
//		}
//	}

}
