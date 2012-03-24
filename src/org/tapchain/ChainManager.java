package org.tapchain;

import org.tapchain.AnimationChain.BasicPiece;
import org.tapchain.AnimationChain.BasicView;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainPath;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.ChainController.ControlCallback;
import org.tapchain.TapChainEditor.TapChainEditorView;

import android.util.Log;
import android.util.Pair;


public class ChainManager implements Manager<ChainPiece> {
	ChainPiece mark = null;
	Chain chain = null;
	ChainPiece pt = null, pt_b = null;
	ControlCallback cc_cache = new ControlCallback() {
		public boolean impl() {
			return true;
		}
	};

	//1.INITIALIZATIONS
	public ChainManager() {
	}

	public ChainManager Set(Chain c) {
		chain = c;
		return this;
	}

	public ChainManager NewSession() {
		return new ChainManager().Set(chain);
	}

	public ChainManager SetCallback(ControlCallback cb) {
		cc_cache = cb;
		if (chain != null && cc_cache != null)
			chain.Set(cc_cache);
		return this;
	}

	public Chain Get() {
		return chain;
	}

	public ChainManager CreateChain() {
		chain = new Chain();
		if (cc_cache != null)
			chain.Set(cc_cache);
		return this;
	}

	public ChainManager Next() {
		pt = pt.getNext();
		return this;
	}

	public ChainPiece GetPiece() {
		return pt;
	}
	
	//2.FUNDAMENTALS
	Pair<ChainPiece,ChainPath> __connect(ChainPiece piece_from, PackType type_from, ChainPiece piece_to, PackType type_to) {
		Pair<ChainPiece,ChainPath> rtn = null;
		try {
			if((rtn = piece_from.appendTo(type_from, piece_to, type_to))!=null) {
				piece_from.postAppend();
				piece_to.postAppend();
			}
		} catch (ChainException e) {
			Error(e);
		}
		return rtn;
	}

	public Pair<ChainPiece,ChainPath> append(ChainPiece x, PackType xp, ChainPiece y,
			PackType yp, Boolean... con) {
		try {
			return __connect(x, xp, y, yp);
		} catch (NullPointerException e1) {
			Error(new ChainException(this, "Append: Null Appender"));
			return null;
		}
	}
	
	ChainPath __disconnect(ChainPiece x, ChainPiece y) {
		return x.detach(y);
	}

	ChainManager __next(ChainPiece base, ChainPiece cp, PackType stack) {
		__connect(cp, stack, base, PackType.PASSTHRU);
		return this;
	}

	ChainManager __side(ChainPiece base, ChainPiece cp, PackType stack) {
		__connect(cp, stack, base, PackType.HEAP);
		return this;
	}

	public ChainManager and(ChainPiece cp, ChainPiece... args) {
		if (pt != null)
			__next(pt, cp, PackType.PASSTHRU);
		add(cp, args);
		return this;
	}

	public ChainManager next2Heap(ChainPiece cp) {
		if (pt != null)
			__next(pt, cp, PackType.EVENT);
		add(cp);
		return this;
	}

	public ChainManager child(ChainPiece cp) {
		if (pt != null)
				__connect(cp, PackType.PASSTHRU, pt, PackType.FAMILY);
		add(cp);
		return this;
	}

	public ChainManager func(ChainPiece cp) {
		if (pt != null)
			__side(pt, cp, PackType.HEAP);
		add(cp);
		return this;
	}

	public ChainManager rightSync(ChainPiece cp) {
		if (pt != null) {
			__side(pt, cp, PackType.HEAP);
			__side(cp, pt, PackType.HEAP);
		}
		add(cp);
		return this;
	}

	ChainManager __front(ChainPiece cp, ChainPiece target) {
		if (pt != null)
			__connect(cp, PackType.EVENT, pt, PackType.FAMILY);
		return this;
	}

	public ChainManager front(ChainPiece cp) {
		__front(cp, pt);
		add(cp);
		return this;
	}

	ChainManager __then(ChainPiece cpbase, ChainPiece target) {
		if (pt != null)
			__connect(cpbase,PackType.EVENT, target, PackType.EVENT);
		return this;
	}

	public ChainManager then(ChainPiece cp) {
		__then(cp, pt);
		add(cp);
		return this;
	}

	public ChainManager because(ChainPiece cp) {
		__then(pt, cp);
		add(cp);
		return this;
	}

	ChainManager prev(ChainPiece cp) {
		__connect(pt, PackType.PASSTHRU, cp, PackType.PASSTHRU);
		add(cp);
		return this;
	}

	ChainManager prevFromHeap(ChainPiece cp) {
		__connect(pt, PackType.PASSTHRU, cp, PackType.HEAP);
		add(cp);
		return this;
	}

	public ChainManager args(ChainPiece cp, ChainPiece... args) {
		__side(cp, pt, PackType.HEAP);
		add(cp, args);
		return this;
	}

	public ChainManager reset(ChainPiece cp, ChainPiece... args) {
		__connect(pt,PackType.FAMILY, cp, PackType.EVENT);
		add(cp, args);
		return this;
	}

	public ChainManager Error(ChainException e) {
		return this;
	}

	static int a = 0;
	@SuppressWarnings("unchecked")
	public ChainManager add(ChainPiece cp, ChainPiece... args) {
		pt_b = pt;
		pt = cp;
//		Log.w("Maker", "add "+Integer.toString(a++));
		return this;
	}

	@Override
	public ChainManager Save() {
		return this;
	}

	public ChainManager _mark() {
		mark = pt;
		return this;
	}

	public ChainManager _return() {
		return _return(mark);
	}

	public ChainManager _return(ChainPiece bp) {
		if (bp == null) {
			Error(new ChainException(this, "returnToMark: no mark to return"));
		}
		pt = bp;
		return this;
	}
	
	public ChainManager _exit() {
		return this;
	}
	
	public ChainManager _child() {
		return this;
	}

	public ChainManager setView(ChainPiece bp, TapChainEditorView v) {
		return this;
	}

	public ChainManager setView(TapChainEditorView v) {
		return this;
	}

	public BlueprintManager makeBlueprint() {
		return null;
	}

	public ChainManager unsetView(ChainPiece bp) {
		return this;
	}

	public ChainManager Log(String... s) {
		return this;
	}

	public ChainManager remove(ChainPiece bp) {
		Get().removePiece(bp);
		return this;
	}

	public ChainManager refreshView(ChainPiece bp, ChainPiece obj) {
		return this;
	}

}
