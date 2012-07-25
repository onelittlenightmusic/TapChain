package org.tapchain.core;


import java.util.List;

import org.tapchain.core.ActorChain.*;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.TapChainEdit.IPathView;
import org.tapchain.core.TapChainEdit.IPieceView;


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
	public ActorManager save() {
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
	public ActorManager add(IPiece bp, IPiece... args) {
		super.add(bp, args);
		if(bp == null) 
			return this;
		for(IPiece arg: args) {
			_return(bp);
			teacher(arg);
		}
		_return(bp);
		getChain().getOperator().add(bp);
		((Actor)bp).postRegister(this)
			;
		_return(bp);
		if(getRoot() != null) {
			super.append(bp, PackType.FAMILY, getRoot(), PackType.FAMILY);
		}
		return this;
	}
	
	@Override
	public ActorManager remove(IPiece bp) {
		if (bp == null)
			return this;
		unsetPieceView(bp);
		bp.end();
		for (IPiece cp : bp.getPartners()) {
			__disconnect(bp, cp);
		}
		super.remove(bp);
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
		return setPieceView(getPiece(), v);
	}
	
	@Override
	public ActorManager setPieceView(IPiece bp, Blueprint _view) throws ChainException {
		if(pieceEdit!=null) {
			IPieceView v = pieceEdit.onSetPieceView(bp, _view);
			if(v!=null)
				pieceEdit.onMoveView(v);
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
					Blueprint vReserve = new Blueprint(pbp_connect, pieceEdit.getView((Actor)y), pieceEdit.getView((Actor)x), new Actor.Value(yp), new Actor.Value(xp));
					if(pathEdit != null)
						pathEdit.setPathView(rtn.getConnect(), vReserve);
				save();
			}
		}
		
		return rtn;
	}
	
	@Override
	public IPath __disconnect(IPiece x, IPiece y) {
		IPath rtn = super.__disconnect(x, y);
		if(pathEdit != null)
			pathEdit.unsetPathView(rtn);
		return rtn;
	}
	
	public interface IPathEdit {
		public void setPathView(IPath second, IBlueprint vReserve);
		public IPathView getView(IPath path);
		public void unsetPathView(IPath rtn);
	}
	public interface IPieceEdit {
		public IPieceView onSetPieceView(IPiece bp, Blueprint _view) throws ChainException;
		public IPieceView getView(IPiece y);
		public void onUnsetView(IPiece bp);
		public void onRefreshView(IPiece bp, IPiece obj);
		public void onMoveView(IView v);
	}
	public interface IStatusHandler {
		public void getStateAndSetView(int state);
		public void tickView();
		public void end();
	}
}
