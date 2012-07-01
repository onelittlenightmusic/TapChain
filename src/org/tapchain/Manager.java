package org.tapchain;

import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ConnectionIO;
import org.tapchain.Chain.IPath;
import org.tapchain.Chain.IPiece;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.ChainController.IControlCallback;


public class Manager implements IManager<IPiece> {
	IPiece mark = null;
	Chain chain = null;
	IPiece pt = null, pt_b = null;
	IControlCallback cc_cache = new IControlCallback() {
		public boolean callback() {
			return true;
		}
	};

	//1.INITIALIZATIONS
	public Manager() {
	}

	public Manager Set(Chain c) {
		chain = c;
		return this;
	}

	public Manager newSession() {
		return new Manager().Set(chain);
	}

	public Manager SetCallback(IControlCallback cb) {
		cc_cache = cb;
		if (chain != null && cc_cache != null)
			chain.Set(cc_cache);
		return this;
	}

	public Chain get() {
		return chain;
	}

	public Manager createChain() {
		chain = new Chain();
		if (cc_cache != null)
			chain.Set(cc_cache);
		return this;
	}

//	public ChainManager Next() {
//		pt = pt.getNext();
//		return this;
//	}

	public IPiece getPiece() {
		return pt;
	}
	
	//2.FUNDAMENTALS
	ConnectionIO __connect(IPiece piece_from, PackType type_from, IPiece piece_to, PackType type_to) {
		ConnectionIO rtn = null;
		try {
			if((rtn = piece_from.appendTo(type_from, piece_to, type_to))!=null) {
				if(piece_from instanceof ChainPiece)
					((ChainPiece)piece_from).postAppend();
				if(piece_from instanceof ChainPiece)
					((ChainPiece)piece_to).postAppend();
			}
		} catch (ChainException e) {
			error(e);
		}
		return rtn;
	}

	public ConnectionIO append(IPiece x, PackType xp, IPiece y,
			PackType yp, Boolean... con) {
		try {
			return __connect(x, xp, y, yp);
		} catch (NullPointerException e1) {
			error(new ChainException(this, "Append: Null Appender"));
			return null;
		}
	}
	
	IPath __disconnect(IPiece x, IPiece y) {
		return x.detach(y);
	}

	Manager __next(IPiece base, IPiece cp, PackType stack) {
		__connect(cp, stack, base, PackType.PASSTHRU);
		return this;
	}

	Manager __side(IPiece base, IPiece cp, PackType stack) {
		__connect(cp, stack, base, PackType.HEAP);
		return this;
	}

	public Manager and(IPiece cp, IPiece... args) {
		if (pt != null)
			__next(pt, cp, PackType.PASSTHRU);
		add(cp, args);
		return this;
	}

//	public ChainManager next2Heap(PieceBody cp) {
//		if (pt != null)
//			__next(pt, cp, PackType.EVENT);
//		add(cp);
//		return this;
//	}

//	public ChainManager child(PieceBody cp) {
//		if (pt != null)
//				__connect(cp, PackType.PASSTHRU, pt, PackType.FAMILY);
//		add(cp);
//		return this;
//	}

	public Manager student(IPiece cp) {
		if (pt != null)
			__side(pt, cp, PackType.HEAP);
		add(cp);
		return this;
	}

//	public ChainManager rightSync(PieceBody cp) {
//		if (pt != null) {
//			__side(pt, cp, PackType.HEAP);
//			__side(cp, pt, PackType.HEAP);
//		}
//		add(cp);
//		return this;
//	}

//	ChainManager __front(PieceBody cp, PieceBody target) {
//		if (pt != null)
//			__connect(cp, PackType.EVENT, pt, PackType.FAMILY);
//		return this;
//	}
//
//	public ChainManager front(PieceBody cp) {
//		__front(cp, pt);
//		add(cp);
//		return this;
//	}

	Manager __then(IPiece cpbase, IPiece target) {
		if (pt != null)
			__connect(cpbase,PackType.EVENT, target, PackType.EVENT);
		return this;
	}

	public Manager young(IPiece cp) {
		__then(cp, pt);
		add(cp);
		return this;
	}

	public Manager old(IPiece cp) {
		__then(pt, cp);
		add(cp);
		return this;
	}

	Manager prev(IPiece cp) {
		__connect(pt, PackType.PASSTHRU, cp, PackType.PASSTHRU);
		add(cp);
		return this;
	}

//	ChainManager prevFromHeap(PieceBody cp) {
//		__connect(pt, PackType.PASSTHRU, cp, PackType.HEAP);
//		add(cp);
//		return this;
//	}

	public Manager teacher(IPiece cp, IPiece... args) {
		__side(cp, pt, PackType.HEAP);
		add(cp, args);
		return this;
	}

	public Manager reset(IPiece cp, IPiece... args) {
		__connect(pt,PackType.FAMILY, cp, PackType.EVENT);
		add(cp, args);
		return this;
	}

	public Manager error(ChainException e) {
		return this;
	}

	static int a = 0;
	@SuppressWarnings("unchecked")
	public Manager add(IPiece cp, IPiece... args) {
		pt_b = pt;
		pt = cp;
//		Log.w("Maker", "add "+Integer.toString(a++));
		return this;
	}

	@Override
	public Manager save() {
		return this;
	}

	public Manager _mark() {
		mark = pt;
		return this;
	}

	public Manager _return() {
		return _return(mark);
	}

	public Manager _return(IPiece bp) {
		if (bp == null) {
			error(new ChainException(this, "returnToMark: no mark to return"));
		}
		pt = bp;
		return this;
	}
	
	public Manager _exit() {
		return this;
	}
	
	public Manager _child() {
		return this;
	}

	public Manager setView(IPiece bp, Blueprint v) throws ChainException {
		return this;
	}

	public Manager setView(Blueprint v) throws ChainException {
		return this;
	}

	public BlueprintManager makeBlueprint() {
		return null;
	}

	public Manager unsetView(IPiece bp) {
		return this;
	}

	public Manager log(String... s) {
		return this;
	}
	
	public Manager _func(IPiece arg) {
		add(new Actor.Function(), arg);
		return _child();
	}

	public Manager remove(IPiece bp) {
		get().removePiece(bp);
		return this;
	}

	public Manager refreshView(IPiece bp, IPiece obj) {
		return this;
	}
	
	public Manager creater_student() {
		student(new Actor.ManagerPiece(getPiece()));
		return this;
	}

}
