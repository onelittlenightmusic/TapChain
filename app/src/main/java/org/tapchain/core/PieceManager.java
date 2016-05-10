package org.tapchain.core;

import org.tapchain.core.Chain.ConnectionResultPath;
import org.tapchain.core.ChainController.IControlCallback;

import java.util.Collection;

public class PieceManager<PIECE extends Piece> extends Manager<PIECE> {
	PIECE mark = null;
	protected Chain chain = null;
	PIECE pt = null, pt_b = null;
	IControlCallback cc_cache = () -> true;

	// 1.Initialization
	public PieceManager() {
	}

    /**
     * copy constructor
     * @param m PieceManager to copy (but not copy pointers in PieceManager)
     */
	public PieceManager(PieceManager m) {
		this();
        setChain(m.getChain());
	}

    public PieceManager(Chain chain) {
        this();
        setChain(chain);
    }

    public PieceManager newSession() {
		PieceManager rtn = new PieceManager(this);
        rtn.setChain(chain);
        return rtn;
	}

	// 2.Getters and setters
	public PIECE getPiece() {
		return pt;
	}

    @Override
    public void setChain(Chain c) {
		chain = c;
	}

    @Override
	public Chain getChain() {
		return chain;
	}

	public PieceManager SetCallback(IControlCallback cb) {
		cc_cache = cb;
		if (chain != null && cc_cache != null)
			chain.setCallback(cc_cache);
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

		for (ClassEnvelope from_cls : classesIn)
			for (ClassEnvelope to_cls : classesOut) {
				if (from_cls.isAssignableFrom(to_cls)) {
					logLocal("__canConnect Succeeded %s",
							toString(pIn, tIn, pOut, tOut));
					return from_cls;
				}
			}
		logLocal("__canConnect Failed %s", toString(pIn, tIn, pOut, tOut));
		return null;
	}

	public synchronized ConnectionResultPath connect(PIECE pIn, PathType tIn,
												   PIECE pOut, PathType tOut, boolean addView) {
		ConnectionResultPath rtn = null;
		logLocal("_connect started %s",
				toString(pIn, tIn, pOut, tOut));
		if (pIn.isConnectedTo(pOut)) {
			logLocal(
					"link Failed: pieces are already connected %s",
					toString(pIn, tIn, pOut, tOut));
			return null;
		}
		ClassEnvelope cls = __canConnect(pIn, tIn, pOut, tOut);
		if (cls == null) {
			logLocal("link Failed: Argument is null %s",
					toString(pIn, tIn, pOut, tOut));
			return null;
		}
		try {
			rtn = pIn.appendTo(tIn, pOut, tOut);
		} catch (ChainException e) {
			e.printStackTrace();
		}
        assert rtn != null;
        rtn.setConnectionClass(cls);
		return rtn;
	}

    protected static <PIECE extends Piece> PathType getPathType(PIECE pIn, PIECE pOut) {
        if(pIn != null && pOut != null)
            return pIn.getPathTypeTo(pOut);
        return null;
    }
    protected static <PIECE extends Piece> Boolean isOutTo(PIECE pIn, PIECE pOut) {
        if(pIn != null && pOut != null)
            return pIn.isOutTo(pOut);
        return null;
    }
    protected static <PIECE extends Piece> IPath getPath(PIECE pIn, PIECE pOut) {
        return pIn.getPathTo(pOut);
    }

	public String toString(IPiece pFrom, PathType tFrom, IPiece pTo,
			PathType tTo) {
		return String.format("[%s(%s)->%s(%s)]",
				(pTo == null) ? "null" : pTo.getClass().getSimpleName(),
				(tTo == null) ? "null" : tTo.toString(),
				(pFrom == null) ? "null" : pFrom.getClass().getSimpleName(),
				(tFrom == null) ? "null" : tFrom.toString());
	}

	public ConnectionResultPath append(PIECE x, PathType xp, PIECE y,
			PathType yp, boolean addView) {
//		try {
			if(x == null || xp == null || y == null || yp == null)
				return null;
			ConnectionResultPath io = connect(x, xp, y, yp, addView);
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
	}


	@Override
	public PieceManager nextEvent(PIECE cp) {
		_appendNextEventToPrevEvent(cp, pt);
		add(cp);
		return this;
	}

	public PieceManager prevEvent(PIECE cp) {
		_appendNextEventToPrevEvent(pt, cp);
		add(cp);
		return this;
	}

    PieceManager prevPassThru(PIECE cp) {
        append(pt, PathType.PASSTHRU, cp, PathType.PASSTHRU, false);
        add(cp);
        return this;
    }

    PieceManager nextPassThru(PIECE cp) {
        append(cp, PathType.PASSTHRU, pt, PathType.PASSTHRU, false);
        add(cp);
        return this;
    }

    @Override
    public PieceManager pullFrom(PIECE cp) {
		_appendOffer(cp, pt);
		add(cp);
		return this;
	}

    @Override
    public PieceManager pushTo(PIECE cp) {
        if (pt != null)
            _appendOffer(pt, cp);
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


	@Override
	public void remove(PIECE bp) {
		bp.end();
	}

    public IPath disconnect(PIECE x, PIECE y) {
		if (x instanceof ChainPiece)
			((ChainPiece) x).postAppend();
		if (y instanceof ChainPiece)
			((ChainPiece) y).postAppend();
		return disconnect(x.getPathTo(y));
	}

	PieceManager _appendPassThru(PIECE to, PIECE from) {
		append(from, PathType.PASSTHRU, to, PathType.PASSTHRU, false);
		return this;
	}

	PieceManager _appendOffer(PIECE to, PIECE from) {
		append(from, PathType.OFFER, to, PathType.OFFER, false);
		return this;
	}

    PieceManager _appendNextEventToPrevEvent(PIECE cpbase, PIECE target) {
        if (pt != null)
            append(cpbase, PathType.EVENT, target, PathType.EVENT, false);
        return this;
    }

	public IPath disconnect(IPath path) {
		path.detach();
		return path;
	}

	// 4.Termination: none
	// 5.Local classes: none


	public void logLocal(String format, String... l) {
//		Log.w("PieceManager", String.format(format, l));
	}

    public <VALUE, INPUT, OUTPUT> PieceManager<PIECE> add(final IFunc<VALUE, INPUT, OUTPUT> func, final VALUE init) {
        return this;
    };
    public <VALUE, OUTPUT> PieceManager<PIECE> add(final IGenerator<VALUE, OUTPUT> generator, final VALUE init) {
        return this;
    };
    public <VALUE, INPUT> PieceManager<PIECE> add(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
        return this;
    };

    public <PARENT, EFFECT> PieceManager<PIECE> add(final IEffector<PARENT, EFFECT> effector, final EFFECT init, int duration) {
        return this;
    }

    @Override
    public void setLog(ILogHandler _log) {
        super.setLog(_log);
        if(chain != null)
            chain.setLog(_log);
    }
}
