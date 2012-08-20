package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.ChainController.IControlCallback;

public class PieceManager extends Manager<IPiece> {
	IPiece mark = null;
	Chain chain = null;
	IPiece pt = null, pt_b = null;
	IControlCallback cc_cache = new IControlCallback() {
		public boolean onCalled() {
			return true;
		}
	};

	// 1.Initialization
	public PieceManager() {
	}

	public PieceManager(PieceManager m) {
		this();
	}

	public PieceManager newSession() {
		return new PieceManager().setChain(chain);
	}

	// 2.Getters and setters
	public IPiece getPiece() {
		return pt;
	}

	public PieceManager createChain() {
		chain = new Chain();
		if (cc_cache != null)
			chain.setCallback(cc_cache);
		return this;
	}

	public PieceManager setChain(Chain c) {
		chain = c;
		return this;
	}

	public Chain getChain() {
		return chain;
	}

	public PieceManager SetCallback(IControlCallback cb) {
		cc_cache = cb;
		if (chain != null && cc_cache != null)
			chain.setCallback(cc_cache);
		return this;
	}

	public PieceManager setPieceView(IPiece bp, Blueprint v, WorldPoint wp)
			throws ChainException {
		return this;
	}

	public PieceManager setPieceView(Blueprint v) throws ChainException {
		return this;
	}

	// 3.Changing state
	@SuppressWarnings("unchecked")
	@Override
	public PieceManager add(IPiece cp, IPiece... args) {
		pt_b = pt;
		pt = cp;
		return this;
	}

	@Override
	public PieceManager save() {
		return this;
	}

	ConnectionResultIO __connect(IPiece piece_from, PackType type_from,
			IPiece piece_to, PackType type_to) {
		ConnectionResultIO rtn = null;
		try {
			if ((rtn = piece_from.appendTo(type_from, piece_to, type_to)) != null) {
				if (piece_from instanceof ChainPiece)
					((ChainPiece) piece_from).postAppend();
				if (piece_from instanceof ChainPiece)
					((ChainPiece) piece_to).postAppend();
			}
		} catch (ChainException e) {
			error(e);
		}
		return rtn;
	}

	public ConnectionResultIO append(IPiece x, PackType xp, IPiece y,
			PackType yp, Boolean... con) {
		try {
			return __connect(x, xp, y, yp);
		} catch (NullPointerException e1) {
			error(new ChainException(this, "Append: Null Appender"));
			return null;
		}
	}

	public PieceManager student(IPiece cp, IPiece... args) {
		if (pt != null)
			__side(pt, cp, PackType.HEAP);
		add(cp);
		return this;
	}

	PieceManager __then(IPiece cpbase, IPiece target) {
		if (pt != null)
			__connect(cpbase, PackType.EVENT, target, PackType.EVENT);
		return this;
	}

	public PieceManager young(IPiece cp, IPiece... args) {
		__then(cp, pt);
		add(cp);
		return this;
	}

	public PieceManager old(IPiece cp, IPiece... args) {
		__then(pt, cp);
		add(cp);
		return this;
	}

	PieceManager prev(IPiece cp) {
		__connect(pt, PackType.PASSTHRU, cp, PackType.PASSTHRU);
		add(cp);
		return this;
	}

	public PieceManager teacher(IPiece cp, IPiece... args) {
		__side(cp, pt, PackType.HEAP);
		add(cp, args);
		return this;
	}

	public PieceManager error(ChainException e) {
		return this;
	}

	static int a = 0;

	public PieceManager _mark() {
		mark = pt;
		return this;
	}

	public PieceManager _gotomark() {
		return _return(mark);
	}

	public PieceManager _return(IPiece bp) {
		if (bp == null) {
			error(new ChainException(this, "returnToMark: no mark to return"));
		}
		pt = bp;
		return this;
	}

	public PieceManager _exit() {
		return this;
	}

	public PieceManager _child() {
		return this;
	}

	public PieceManager unsetPieceView(IPiece bp) {
		return this;
	}

	public PieceManager log(String... s) {
		return this;
	}

	public PieceManager _func(IPiece arg) {
		add(new Actor.Function(), arg);
		return _child();
	}

	public PieceManager remove(IPiece bp) {
		getChain().removePiece(bp);
		return this;
	}

	public PieceManager refreshPieceView(IPiece bp, IPiece obj) {
		return this;
	}

	public PieceManager creater_student() {
		student(new Actor.ManagerPiece(getPiece()));
		return this;
	}

	IPath __disconnect(IPiece x, IPiece y) {
		return x.detach(y);
	}

	PieceManager __next(IPiece base, IPiece cp, PackType stack) {
		__connect(cp, stack, base, PackType.PASSTHRU);
		return this;
	}

	PieceManager __side(IPiece base, IPiece cp, PackType stack) {
		__connect(cp, stack, base, PackType.HEAP);
		return this;
	}

	// 4.Termination: none
	// 5.Local classes: none

	/*
	 * public Manager and(IPiece cp, IPiece... args) { if (pt != null)
	 * __next(pt, cp, PackType.PASSTHRU); add(cp, args); return this; }
	 */// public ChainManager next2Heap(PieceBody cp) {
		// if (pt != null)
	// __next(pt, cp, PackType.EVENT);
	// add(cp);
	// return this;
	// }

	// public ChainManager child(PieceBody cp) {
	// if (pt != null)
	// __connect(cp, PackType.PASSTHRU, pt, PackType.FAMILY);
	// add(cp);
	// return this;
	// }

	// public ChainManager rightSync(PieceBody cp) {
	// if (pt != null) {
	// __side(pt, cp, PackType.HEAP);
	// __side(cp, pt, PackType.HEAP);
	// }
	// add(cp);
	// return this;
	// }

	// ChainManager __front(PieceBody cp, PieceBody target) {
	// if (pt != null)
	// __connect(cp, PackType.EVENT, pt, PackType.FAMILY);
	// return this;
	// }
	//
	// public ChainManager front(PieceBody cp) {
	// __front(cp, pt);
	// add(cp);
	// return this;
	// }
	// ChainManager prevFromHeap(PieceBody cp) {
	// __connect(pt, PackType.PASSTHRU, cp, PackType.HEAP);
	// add(cp);
	// return this;
	// }

	/*
	 * public Manager reset(IPiece cp, IPiece... args) {
	 * __connect(pt,PackType.FAMILY, cp, PackType.EVENT); add(cp, args); return
	 * this; }
	 */
	// public BlueprintManager makeBlueprint() {
	// return null;
	// }
	//

}
