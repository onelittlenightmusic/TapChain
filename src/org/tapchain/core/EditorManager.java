package org.tapchain.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.tapchain.core.Actor.Mover2;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.TapChainEdit.IEditAnimation;
import org.tapchain.core.TapChainEdit.IPathView;
import org.tapchain.core.TapChainEdit.IPieceView;
import org.tapchain.core.ActorManager.*;
import org.tapchain.core.Chain.*;
import org.tapchain.core.ChainPiece.PieceState;


public class EditorManager extends ActorManager implements IPieceEdit, IPathEdit {
	protected ActorManager systemManager;
	public TreeMap<IPiece,IPieceView> dictPiece = new TreeMap<IPiece, IPieceView>();
	ConcurrentHashMap<IPath, IPathView> dictPath = new ConcurrentHashMap<IPath, IPathView>();
	ArrayList<Actor> plist = new ArrayList<Actor>();
	public EditorManager(ActorManager am) {
		super();
		setPieceEdit(this);
		setPathEdit(this);
		setSystemManager(am);
	}
	public void init() {
		Actor ptmp = null;
		for(int c : Arrays.asList(0xff80ff80, 0xff80ff80, 0xffffffff, 0xff8080ff, 0xffff8080)) {
			systemManager.add(new Actor.Colorer().color_init(c).setParentType(PackType.HEAP).boost())
			.teacher(ptmp = new Actor()).save();
			plist.add(ptmp);
		}
		move.setName("MOVE_ENTRANCE");
		getSystemManager()
		.add(move)
/*		.student(userManager.move_ef = (Actor.Mover) new Actor.Mover() {
			@Override
			public boolean actorRun() throws ChainException {
				super.actorRun();
				if(getTarget() instanceof IPieceView)
					checkAndAttach((IPieceView)getTarget(), null);
				return false;
			}
		}.initEffect(getPoint(), 1).setParentType(PackType.HEAP).boost())
*/
		.student(move_ef = (Mover2) new Actor.Mover2().initEffect(new WorldPoint(0,0), 1).setParentType(PackType.HEAP).boost())
		.save();

	}
	public ActorManager getSystemManager() {
		return systemManager;
	}
	public EditorManager setSystemManager(ActorManager am) {
		systemManager = am;
		return this;
	}
	public Collection<IPieceView> getPieceViews() {
		return dictPiece.values();
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
			ActorManager manager = getSystemManager();
			_view = (IPieceView) bp.newInstance(manager);
			if(_view == null)
				throw new ChainException(cp2, "view not created");
			manager.save();
		dictPiece.put(cp2, _view);
		_view.setMyTapChain(cp2);
		final Initializer ia = new Initializer();
		if(_view instanceof IEditAnimation) {
			ia.init_animation(getSystemManager(), (IEditAnimation)_view);
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
			c.setError(getErrorHandler());
		}
		return _view;
	}

	@Override
	public void onSetPathView(IPath path, IBlueprint _vReserve) {
		final IPathView _view;
		try {
			ActorManager manager = getSystemManager();
			_view = (IPathView) _vReserve.newInstance(manager);
			manager.save();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		dictPath.put(path, _view);
		final Initializer ia = new Initializer();
		if(_view instanceof IEditAnimation) {
			ia.init_animation(getSystemManager(), (IEditAnimation)_view);
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
	
	static Actor move = new Actor();
	static Actor.Mover2 move_ef = null;
	
	@Override
	public void onMoveView(IView v, WorldPoint wp) {
		move_ef.initEffect(wp==null?new WorldPoint(0,0):wp, 1);
		move.push(v);
//		kickDraw();
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
		IPieceView v = getView(bp);
		v.unsetMyTapChain();
		dictPiece.remove(bp);
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
			((Actor)bp).initNum();
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

}
