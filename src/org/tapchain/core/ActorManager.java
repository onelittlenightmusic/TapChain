package org.tapchain.core;


import java.util.List;

import org.tapchain.core.ActorChain.*;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.ChainPiece.PieceState;
import org.tapchain.core.TapChainEdit.ISystemPath;
import org.tapchain.core.TapChainEdit.ISystemPiece;

import android.util.Log;


public class ActorManager extends PieceManager {
	ChainPiece root = null;
	ActorManager parent = null;
	IPathEdit pathEdit = null;
	IPieceEdit pieceEdit = null;
	IStatusHandler statusHandle = null;
	Blueprint pbp_connect;
	
	//1.Initialization
	public ActorManager() {
		super();
	}
	
	public ActorManager(
			IErrorHandler _error, 
			ILogHandler _log,
			IPathEdit _pathEdit,
			IPieceEdit _pieceEdit,
			IStatusHandler _pieces,
			Blueprint _connect) {
		this();
		this
		.setPathEdit(_pathEdit)
		.setPieceEdit(_pieceEdit)
		.setStatusHandler(_pieces)
		.setPathBlueprint(_connect)
		.setLog(_log)
		.setError(_error)
		;
	}
	
	public ActorManager(ActorManager am) {
		this(//am.factory,
				am.error,
				am.log,
				am.pathEdit,
				am.pieceEdit,
				am.statusHandle,
				am.pbp_connect);

	}
	@Override
	public ActorManager createChain() {
		setChain(new ActorChain());
		return this;
	}
	public ActorManager createChain(int time) {
		setChain(new ActorChain(time));
		return this;
	}
	
	@Override
	public ActorManager newSession() {
		return new ActorManager(this)
			.setChain((ActorChain)chain);
	}
	
	//2.Getters and setters
	public ActorManager setChain(ActorChain c) {
		super.setChain(c);
		if(log != null)
			c.setLog(log);
		return this;
	}
	
	@Override
	public Actor getPiece() {
		return (Actor)super.getPiece();
	}
	
	public ActorManager setParentManager(ActorManager _parent) {
		parent = _parent;
		return this;
	}
	public ActorManager getParentManager() throws ChainException {
		if(parent != null)
			return parent;
		throw new ChainException(this, "No Parent");
	}
	@Override
	public ActorManager setLog(ILogHandler _log) {
		super.setLog(_log);
		if(chain != null)
			chain.setLog(_log);
		return this;
	}
	public ActorManager setPathEdit (IPathEdit pv) {
		pathEdit = pv;
		return this;
	}
	public ActorManager setPieceEdit (IPieceEdit pv) {
		pieceEdit = pv;
		return this;
	}
	public ActorManager setStatusHandler (IStatusHandler ps) {
		statusHandle = ps;
		return this;
	}
	public IStatusHandler getStatusHandler() {
		return statusHandle;
	}
	
	public ChainPiece getRoot() {
		return root;
	}
	
	public ActorManager setRoot(Actor arg) {
		root = arg;
		return this;
	}
	@Override
	public ActorManager log(String... s) {
		return this;
	}
	
	public ActorManager setPathBlueprint(Blueprint p) {
		pbp_connect = p;
		return this;
	}
	
	//3.Changing state
	@Override
	public ActorManager _child() {
		return newSession().setParentManager(this).setRoot(getPiece());
	}
	List<IPiece> dump;
	@Override
	public ActorManager _save() {
		dump = getChain().getOperator().save();
		return this;
	}
	public List<IPiece> dump() {
		return dump;
	}
	@Override
	public ActorChain getChain() {
		return (ActorChain)super.getChain();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorManager add(IPiece bp) {
		super.add(bp);
		if(bp == null) 
			return this;
//		for(IPiece arg: args) {
//			_return(bp);
//			teacher(arg);
//		}
//		_return(bp);
		getChain().getOperator().add(bp);
		((Actor)bp).onAdd(newSession());
		_return(bp);
		if(getRoot() != null) {
			super.append(bp, PackType.FAMILY, getRoot(), PackType.FAMILY);
		}
		return this;
	}
	
	public ActorManager addActor(IActor actor) {
		return add(new Actor().setActor(actor));
	}
	
	@Override
	public ActorManager remove(IPiece piece) {
		if (piece == null)
			return this;
		unsetPieceView(piece);
		piece.end();
		((Actor)piece).onRemove(newSession());
		for (IPiece cp : piece.getPartners()) {
			__disconnect(piece, cp);
		}
		super.remove(piece);
		return this;
	}
	
	public void restart(IPiece pieceBody) {
		if(pieceBody instanceof ChainPiece)
			((ChainPiece)pieceBody).restart();
	}

	@Override
	public ActorManager _exit() {
		try {
			return getParentManager();
		} catch (ChainException e) {
			error(e);
		}
		return this;
	}
	
	@Override
	public ActorManager setPieceView(Blueprint v) throws ChainException {
		return setPieceView(getPiece(), v, new WorldPoint(0, 0));
	}
	
	@Override
	public ActorManager setPieceView(IPiece bp, Blueprint _view, IPoint nowPoint) throws ChainException {
		if(pieceEdit!=null) {
			ISystemPiece v = pieceEdit.onSetPieceView(bp, _view);
			if(v!=null)
				pieceEdit.onMoveView(v, nowPoint);
		}
		return this;
	}
	
	@Override
	public ActorManager unsetPieceView(IPiece bp) {
		if(pieceEdit!=null)
			pieceEdit.onUnsetView(bp);
		return this;
	}
	
	@Override
	public ActorManager refreshPieceView(IPiece bp, IPiece obj) {
		if(pieceEdit!= null)
			pieceEdit.onRefreshView(bp, obj);
		return this;
	}
	
	@Override
	public ConnectionResultIO append(IPiece x, PackType xp, IPiece y,
			PackType yp, Boolean... con) {
		ConnectionResultIO rtn = super.append(x, xp, y, yp);
		if(rtn != null) {
			log("ACM","Chained");
			if(con != null && pbp_connect != null) {
					IBlueprint vReserve = pbp_connect.copyAndRenewArg()
					.addArg(pieceEdit.getView((Actor)y), pieceEdit.getView((Actor)x), yp, xp);
					if(pathEdit != null) {
						pathEdit.onSetPathView(rtn.getConnect(), vReserve);
//						Log.w("TEST",rtn.getConnect().toString());
					}
				_save();
			}
		}
		
		return rtn;
	}
	
	@Override
	public IPath __disconnect(IPiece x, IPiece y) {
		IPath rtn = super.__disconnect(x, y);
//		if(rtn == null) 
//			Log.w("test", "disconnect detect null path");
		if(pathEdit != null) {
			pathEdit.onUnsetPathView(rtn);
//			Log.w("TEST",rtn.toString());
		}
		return rtn;
	}
	
	public interface IPathEdit {
		public void onSetPathView(IPath second, IBlueprint vReserve);
		public ISystemPath getView(IPath path);
		public void onUnsetPathView(IPath rtn);
	}
	public interface IPieceEdit {
		public ISystemPiece onSetPieceView(IPiece bp, Blueprint _view) throws ChainException;
		public ISystemPiece getView(IPiece y);
		public void onUnsetView(IPiece bp);
		public void onRefreshView(IPiece bp, IPiece obj);
		public void onMoveView(IView v, IPoint wp);
	}
	public interface IStatusHandler {
		public void getStateAndSetView(PieceState state);
		public void tickView();
	}
}
