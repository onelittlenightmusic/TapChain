package org.tapchain;


import java.util.LinkedList;
import java.util.List;

import org.tapchain.ActorChain.*;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.Chain.ConnectionIO;
import org.tapchain.Chain.IPath;
import org.tapchain.Chain.IPiece;
import org.tapchain.Blueprint.Reservation;
import org.tapchain.TapChainEdit.IPathView;
import org.tapchain.TapChainEdit.IPieceView;


public class ActorManager extends Manager {
	LinkedList<ChainPiece> locked = new LinkedList<ChainPiece>();
//	LinkedList<BasicBalloon> pt_balloon = new LinkedList<BasicBalloon>();//, mark_balloon = null;
	ChainPiece root = null;
	ActorManager parent = null;
	Factory factory = null;
	IErrorHandler error = null;
	ILogHandler log = null;
	IPathEdit pathEdit = null;
	IPieceEdit pieceEdit = null;
	StatusHandler pieces = null;
	TapChainEdit editor = null;
	
	public ActorManager() {
		super();
	}
	public ActorManager setEditor(TapChainEdit e) {
		this.editor = e;
		if(editor != null)
			factory = new Factory(editor);
		return this;
	}
	@Override
	public ActorManager createChain() {
		chain = new ActorChain();
		chain.setLog(log);
		return this;
	}
	public ActorManager createChain(int time) {
		chain = new ActorChain(time);
		chain.setLog(log);
		return this;
	}
	public ActorManager setChain(ActorChain c) {
		super.Set(c);
		return this;
	}
	public ActorManager setFactory(Factory _pf) {
		factory = _pf;
		return this;
	}
	public Factory getFactory() {
		return factory;
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
	public ActorManager setError(IErrorHandler handle) {
		error = handle;
		return this;
	}
	public ActorManager setLog(ILogHandler _log) {
		log = _log;
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
	public ActorManager setStatusHandler (StatusHandler ps) {
		pieces = ps;
		return this;
	}
	public ActorManager setRoot(Actor arg) {
		root = arg;
		return this;
	}
	public ChainPiece getRoot() {
		return root;
	}
	
	@Override
	public ActorManager newSession() {
		return new ActorManager()
			.setEditor(editor)
			.setChain((ActorChain)chain)
			.setFactory(factory)
			.setPathEdit(pathEdit)
			.setPieceEdit(pieceEdit)
			.setStatusHandler(pieces)
			.setLog(log)
			.setPathBlueprint(pbp_connect)
			;
	}
	@Override
	public ActorManager _child() {
		return newSession().setParentManager(this).setRoot(getPiece());
	}
	List<IPiece> dump;
	@Override
	public ActorManager save() {
		dump = get().getOperator().save();
		return this;
	}
	public List<IPiece> dump() {
		return dump;
	}
	@Override
	public ActorChain get() {
		return (ActorChain)chain;
	}
	
	@Override
	public ActorManager error(ChainException e) {
		if(error != null)
			error.onError(null, e);
		return this;
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
		get().getOperator().add(bp);
		((Actor)bp).postRegister(this)
//			.setError(error)
//			.setStatusHandler(pieces)
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
		unsetView(bp);
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
	public Actor getPiece() {
		return (Actor)super.getPiece();
	}
	
	@Override
	public BlueprintManager makeBlueprint() {
		return new BlueprintManager().SetParent(this);
	}
	
	@Override
	public ActorManager setView(Blueprint v) throws ChainException {
		return setView(getPiece(), v);
	}
	
	@Override
	public ActorManager setView(IPiece bp, Blueprint _view) throws ChainException {
		if(pieceEdit!=null) {
			IPieceView v = pieceEdit.setView(bp, _view);
			if(v!=null)
				pieceEdit.moveView(v);
		}
		return this;
	}
	
	@Override
	public ActorManager unsetView(IPiece bp) {
		if(pieceEdit!=null)
			pieceEdit.unsetView(bp);
		return this;
	}
	
	@Override
	public ActorManager refreshView(IPiece bp, IPiece obj) {
		if(pieceEdit!= null)
			pieceEdit.refreshView(bp, obj);
		return this;
	}
	
	@Override
	public ConnectionIO append(IPiece x, PackType xp, IPiece y,
			PackType yp, Boolean... con) {
		ConnectionIO rtn = super.append(x, xp, y, yp);
		if(rtn != null) {
			log("ACM","Chained");
			if(con != null && pbp_connect != null) {
					Reservation vReserve = pbp_connect.newReservation(pieceEdit.getView((Actor)y), pieceEdit.getView((Actor)x), new Actor.Value(yp), new Actor.Value(xp));
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
	
	Blueprint pbp_connect;
	
	public ActorManager setPathBlueprint(Blueprint p) {
//		if(p != null)
//			Log.w("TapChainTest","set path blueprint"+p.getName());
		pbp_connect = p;
		return this;
	}
	
	@Override
	public ActorManager log(String... s) {
		if(log != null)
			log.log(s);
		return this;
	}
	
	public interface ILogHandler {
		public void log(String... s);
	}
	public interface IPathEdit {
		public void setPathView(IPath second, Reservation vReserve);
		public IPathView getView(IPath path);
		public void unsetPathView(IPath rtn);
	}
	public interface IPieceEdit {
		public IPieceView setView(IPiece bp, Blueprint _view) throws ChainException;
		public IPieceView getView(IPiece y);
		public void unsetView(IPiece bp);
		public void refreshView(IPiece bp, IPiece obj);
		public void moveView(IView v);
	}
	public interface StatusHandler {
		public void getStateAndSetView(int state);
		public void tickView();
		public void end();
	}
}
