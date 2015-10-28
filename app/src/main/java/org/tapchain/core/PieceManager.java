package org.tapchain.core;

import java.util.Collection;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.ChainController.IControlCallback;

public class PieceManager<PIECE extends Piece> extends Manager<PIECE> {
	PIECE mark = null;
	protected Chain chain = null;
	PIECE pt = null, pt_b = null;
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

	public PieceManager installView(PIECE bp, Blueprint v, IPoint wp)
			throws ChainException {
		return this;
	}

	public PieceManager setPieceView(Blueprint v) throws ChainException {
		return this;
	}

	// 3.Changing state
	@SuppressWarnings("unchecked")
	@Override
	public PieceManager add(PIECE cp) {
		pt_b = pt;
		pt = cp;
		return this;
	}

	@Override
	public PieceManager save() {
		return this;
	}

	public ClassEnvelope __canConnect(IPiece pIn, PathType tIn, IPiece pOut,
			PathType tOut) {
		Collection<ClassEnvelope> classesIn = pIn.getInPack(tIn)
				.getPathClasses();
		Collection<ClassEnvelope> classesOut = pOut.getOutPack(tOut)
				.getPathClasses();
		if(classesIn == null) {
			logLocal("__canConnect from-class is null");
			return null;
		}
		if(classesOut == null) {
			logLocal("__canConnect to-class is null");
			return null;
		}
//		if(pIn == null || tIn == null || pOut == null || tOut == null) {
//			logLocal("__canConnect a param is null %s");
//			return null;
//		}
			
//		for (ClassEnvelope clsIn : classesIn) {
//			logLocal("Connection(Class check) Attempt: %s[%s](%s IN)",
//					pIn.toString(),
//					clsIn.toString(),
//					tIn.toString());
//		}
//		for (ClassEnvelope clsOut : classesOut)
//			logLocal("Connection(Class check) Attempt: %s[%s](%s OUT)",
//					pOut.toString(),
//					clsOut.toString(),
//					tOut.toString());
		for (ClassEnvelope from_cls : classesIn)
			for (ClassEnvelope to_cls : classesOut) {
				if (from_cls.isAssignableFrom(to_cls)) {
					// Log.w("test",
					// String.format("Connection succeeded: %s out %s %s==> %s in %s %s",
					// piece_to.getClass().getSimpleName(), type_to.toString(),
					// to_cls.getHeadClass().getSimpleName(),
					// piece_from.getClass().getSimpleName(),
					// type_from.toString(),
					// from_cls.getHeadClass().getSimpleName()));
					logLocal("__canConnect Succeeded %s",
							toString(pIn, tIn, pOut, tOut));
					return from_cls;
				}
			}
		logLocal("__canConnect Failed %s", toString(pIn, tIn, pOut, tOut));
		return null;
	}

	public synchronized ConnectionResultIO connect(PIECE pIn, PathType tIn,
												   PIECE pOut, PathType tOut, boolean addView) {
		ConnectionResultIO rtn = null;
		logLocal("_connect started %s",
				toString(pIn, tIn, pOut, tOut));
		if (pIn.isConnectedTo(pOut)) {
			logLocal(
					"connect Failed: pieces are already connected %s",
					toString(pIn, tIn, pOut, tOut));
			return null;
		}
		ClassEnvelope cls = __canConnect(pIn, tIn, pOut, tOut);
		if (cls == null) {
			logLocal("connect Failed: Argument is null %s",
					toString(pIn, tIn, pOut, tOut));
			return null;
		}
		try {
			rtn = pIn.appendTo(tIn, pOut, tOut);
		} catch (ChainException e) {
			e.printStackTrace();
		}
		rtn.setConnectionClass(cls);
		return rtn;
	}

	public String toString(IPiece pFrom, PathType tFrom, IPiece pTo,
			PathType tTo) {
		return String.format("[%s(%s)->%s(%s)]",
				(pTo == null) ? "null" : pTo.getClass().getSimpleName(),
				(tTo == null) ? "null" : tTo.toString(),
				(pFrom == null) ? "null" : pFrom.getClass().getSimpleName(),
				(tFrom == null) ? "null" : tFrom.toString());
	}

	public ConnectionResultIO append(PIECE x, PathType xp, PIECE y,
			PathType yp, boolean addView) {
//		try {
			if(x == null || xp == null || y == null || yp == null)
				return null;
			ConnectionResultIO io = connect(x, xp, y, yp, addView);
			if(io == null || io.getResult() == null)
				return null;
			if (x instanceof ChainPiece)
				((ChainPiece) x).postAppend();
			if (y instanceof ChainPiece)
				((ChainPiece) y).postAppend();
			IPath path = io.getResult();
			ClassEnvelope cls = io.getConnectionClass();
			path.setConnectionClass(cls);
			logLocal("append(PieceManager) Succeeded %s<%s>",
				toString(x, xp, y, yp), cls.getSimpleName());

			path.start();
			return io;
//		} catch (NullPointerException e1) {
//			error(new ChainException(this, "Append: Null Appender"));
//			return null;
//		}
	}

	public PieceManager student(PIECE cp, PIECE... args) {
		if (pt != null)
			__side(pt, cp, PathType.OFFER);
		add(cp);
		return this;
	}

	PieceManager __then(PIECE cpbase, PIECE target) {
		if (pt != null)
			append(cpbase, PathType.EVENT, target, PathType.EVENT, false);
		return this;
	}

	@Override
	public PieceManager next(PIECE cp) {
		__then(cp, pt);
		add(cp);
		return this;
	}

	public PieceManager old(PIECE cp, IPiece... args) {
		__then(pt, cp);
		add(cp);
		return this;
	}

	PieceManager prev(PIECE cp) {
		append(pt, PathType.PASSTHRU, cp, PathType.PASSTHRU, false);
		add(cp);
		return this;
	}

	public PieceManager teacher(PIECE cp) {
		__side(cp, pt, PathType.OFFER);
		add(cp);
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
		return _move(mark);
	}

	public PieceManager _move(PIECE bp) {
		if (bp == null) {
			error(new ChainException(this, "returnToMark: no mark to return"));
		}
		pt = bp;
		return this;
	}

	@Override
	public PieceManager _out() {
		return this;
	}

	@Override
	public PieceManager _in() {
		return this;
	}

	public PieceManager unsetPieceView(PIECE bp) {
		return this;
	}

//	public PieceManager _func(IPiece arg) {
//		add(new Actor.HeapToFamily());
//		return _in();
//	}

	@Override
	public PieceManager remove(PIECE bp) {
		((ChainPiece) bp).end();
		return this;
	}

	public PieceManager refreshPieceView(PIECE bp, PIECE obj) {
		return this;
	}

//	public PieceManager creater_student() {
//		student(new Actor.ManagerPiece(getPiece()));
//		return this;
//	}



	public IPath disconnect(IPiece x, IPiece y) {
		if (x instanceof ChainPiece)
			((ChainPiece) x).postAppend();
		if (y instanceof ChainPiece)
			((ChainPiece) y).postAppend();
		return x.detach(y);
	}

	PieceManager __next(PIECE to, PIECE from, PathType stack) {
		append(from, stack, to, PathType.PASSTHRU, false);
		return this;
	}

	PieceManager __side(PIECE to, PIECE from, PathType stack) {
		append(from, stack, to, PathType.OFFER, false);
		return this;
	}

	public IPath disconnect(IPath path) {
		path.detach();
		return path;
	}

	// 4.Termination: none
	// 5.Local classes: none

	/*
	 * public Manager and(IPiece cp, IPiece... args) { if (pt != null)
	 * __next(pt, cp, PackType.PASSTHRU); addFocusable(cp, args); return this; }
	 */// public ChainManager next2Heap(PieceBody cp) {
		// if (pt != null)
	// __next(pt, cp, PackType.EVENT);
	// addFocusable(cp);
	// return this;
	// }

	// public ChainManager child(PieceBody cp) {
	// if (pt != null)
	// connect(cp, PackType.PASSTHRU, pt, PackType.FAMILY);
	// addFocusable(cp);
	// return this;
	// }

	// public ChainManager rightSync(PieceBody cp) {
	// if (pt != null) {
	// __side(pt, cp, PackType.OFFER);
	// __side(cp, pt, PackType.OFFER);
	// }
	// addFocusable(cp);
	// return this;
	// }

	// ChainManager __front(PieceBody cp, PieceBody target) {
	// if (pt != null)
	// connect(cp, PackType.EVENT, pt, PackType.FAMILY);
	// return this;
	// }
	//
	// public ChainManager front(PieceBody cp) {
	// __front(cp, pt);
	// addFocusable(cp);
	// return this;
	// }
	// ChainManager prevFromHeap(PieceBody cp) {
	// connect(pt, PackType.PASSTHRU, cp, PackType.OFFER);
	// addFocusable(cp);
	// return this;
	// }

	/*
	 * public Manager reset(IPiece cp, IPiece... args) {
	 * connect(pt,PackType.FAMILY, cp, PackType.EVENT); addFocusable(cp, args); return
	 * this; }
	 */
	// public BlueprintManager makeBlueprint() {
	// return null;
	// }
	//

	public void logLocal(String format, String... l) {
//		Log.w("PieceManager", String.format(format, l));
	}
	
}
