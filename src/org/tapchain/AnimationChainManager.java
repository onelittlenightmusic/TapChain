package org.tapchain;


import java.util.LinkedList;

import org.tapchain.AndroidPiece.AndroidView;
import org.tapchain.AnimationChain.*;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainPath;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.TapChainEditor.IWindow;
import org.tapchain.TapChainEditor.TapChainEditorView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Pair;


public class AnimationChainManager extends ChainManager {
	LinkedList<ChainPiece> locked = new LinkedList<ChainPiece>();
	LinkedList<BasicBalloon> pt_balloon = new LinkedList<BasicBalloon>();//, mark_balloon = null;
	ChainPiece root = null;
	AnimationChainManager parent = null;
	PieceFactory pf = null;
	static IWindow v = null;
	ErrorHandler error = null;
	LogHandler log = null;
	TapChainPathView path = null;
	TapChainPieceView piecev = null;
	StatusHandler pieces = null;
	TapChainEditor editor = null;
	
	public AnimationChainManager() {
		super();
	}
	public AnimationChainManager setEditor(TapChainEditor e) {
		this.editor = e;
		if(editor != null)
			pf = new PieceFactory(editor);
		return this;
	}
	public AnimationChainManager SetWindow(IWindow window) {
		v = window;
		return this;
	}
	@Override
	public AnimationChainManager CreateChain() {
		chain = new AnimationChain();
//		if(editor != null)
		chain.setLog(log);
		return this;
	}
	public AnimationChainManager Set(AnimationChain c) {
		super.Set(c);
		return this;
	}
	public AnimationChainManager SetFactory(PieceFactory _pf) {
		pf = _pf;
		return this;
	}
	public PieceFactory GetFactory() {
		return pf;
	}
	public AnimationChainManager SetParentManager(AnimationChainManager _parent) {
		parent = _parent;
		return this;
	}
	public AnimationChainManager GetParentManager() throws ChainException {
		if(parent != null)
			return parent;
		throw new ChainException(this, "No Parent");
	}
	public AnimationChainManager setError(ErrorHandler handle) {
		error = handle;
		return this;
	}
	public AnimationChainManager setLog(LogHandler _log) {
		log = _log;
		if(chain != null)
			chain.setLog(_log);
		return this;
	}
	public AnimationChainManager setPathView (TapChainPathView pv) {
		path = pv;
		return this;
	}
	public AnimationChainManager setPieceView (TapChainPieceView pv) {
		piecev = pv;
		return this;
	}
	public AnimationChainManager setStatusHandler (StatusHandler ps) {
		pieces = ps;
		return this;
	}
	public AnimationChainManager setRoot(BasicPiece arg) {
		root = arg;
		return this;
	}
	public ChainPiece getRoot() {
		return root;
	}
	
	@Override
	public AnimationChainManager NewSession() {
		return new AnimationChainManager()
			.setEditor(editor)
			.Set((AnimationChain)chain)
			.SetFactory(pf)
			.SetWindow(v)
//			.SetError(error)
			.setPathView(path)
			.setPieceView(piecev)
			.setStatusHandler(pieces)
			.setLog(log)
			;
	}
	@Override
	public AnimationChainManager _child() {
		return NewSession().SetParentManager(this).setRoot(GetPiece());
	}
	@Override
	public AnimationChainManager Save() {
		Get().getManager().save();
		return this;
	}
	@Override
	public AnimationChain Get() {
		return (AnimationChain)chain;
	}
	
	@Override
	public AnimationChainManager Error(ChainException e) {
		if(error != null)
			error.ErrorHandler(null, e);
		return this;
	}
	@Override
	public AnimationChainManager add(ChainPiece bp, ChainPiece... args) {
		super.add(bp, args);
		if(bp == null) 
			return this;
		for(ChainPiece arg: args) {
			_return(bp);
			args(arg);
		}
		_return(bp);
		Get().getManager().add(bp);
		((BasicPiece)bp).postRegister(this)
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
	public AnimationChainManager remove(ChainPiece bp) {
		if (bp == null)
			return this;
		unsetView(bp);
		bp.end();
		for (ChainPiece cp : bp.getPartners()) {
			__disconnect(bp, cp);
		}
		super.remove(bp);
		return this;
	}
	
	public void restart(ChainPiece chainPiece) {
		chainPiece.restart();
	}

	public AnimationChainManager EnterNewZone(boolean origin, boolean hippo, BasicBalloon _bb) {
		BasicBalloon bb = _bb;
		if(bb == null)
			bb = new BasicBalloon(Get(), hippo);
		if(origin) {
			bb.setOrigin(GetPiece());
			bb.addEndPiece(GetPiece());
		} else {
			if(getRoot() != null) {
				bb.setOrigin(((BasicBalloon)getRoot()).origin);
			}
		}
		add(bb);
//		pt_balloon.addFirst(bb);
		return NewSession().SetParentManager(this).setRoot(bb);
	}
	
	@Override
	public AnimationChainManager _exit() {
//		pt = pt_balloon.poll();
//		return this;
//		pt = balloon;
		try {
			return GetParentManager();
		} catch (ChainException e) {
			Error(e);
		}
		return this;
	}
	
//	public AnimationChainManager cd(BasicBalloon bp) {
//		if (bp == null)
//			return _exit();
//		return EnterNewZone(false, false, bp);
//	}
//	
//	public AnimationChainManager cd() {
//		return _exit();
//	}
//	
//	public AnimationChainManager mkdir(boolean origin, boolean hippo) {
//		return EnterNewZone(origin, hippo, null);
//	}
	
	@Override
	public BasicPiece GetPiece() {
		return (BasicPiece)super.GetPiece();
	}
	
	@Override
	public BlueprintManager makeBlueprint() {
		return new BlueprintManager().SetParent(this);
	}
	
	@Override
	public AnimationChainManager setView(TapChainEditorView v) {
		return setView(GetPiece(), v);
	}
	
	@Override
	public AnimationChainManager setView(ChainPiece bp, TapChainEditorView v) {
		if(piecev!=null) {
			piecev.setView(bp, v);
			piecev.moveView(v);
		}
		return this;
	}
	
	@Override
	public AnimationChainManager unsetView(ChainPiece bp) {
		if(piecev!=null)
			piecev.unsetView(bp);
		return this;
	}
	
	@Override
	public AnimationChainManager refreshView(ChainPiece bp, ChainPiece obj) {
		if(piecev!= null)
			piecev.refreshView(bp, obj);
		return this;
	}
	
	@Override
	public Pair<ChainPiece,ChainPath> append(ChainPiece x, PackType xp, ChainPiece y,
			PackType yp, Boolean... con) {
		Pair<ChainPiece,ChainPath> rtn = super.append(x, xp, y, yp);
		if(rtn != null)
		if(con != null) {
			Log("ACM","Chained");
			try {
				BasicView v = (BasicView)pbp_connect.newReservation(editor.getView((BasicPiece)y), editor.getView((BasicPiece)x), new Value(yp), new Value(xp)).instantiate(this, null);
				if(path != null)
					path.setPathView(rtn.second, v);
				Save();
			} catch (ChainException e) {
				Error(e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			BasicView connect = new Connect();
//			super.append(connect, PackType.HEAP, ChainEditor.getView(x), PackType.HEAP);
//			super.append(connect, PackType.HEAP, ChainEditor.getView(y), PackType.HEAP);
//			add(connect, ChainEditor.getView(x), ChainEditor.getView(y));
		}
		
		return rtn;
	}
	
	@Override
	public ChainPath __disconnect(ChainPiece x, ChainPiece y) {
		ChainPath rtn = super.__disconnect(x, y);
		if(path != null)
			path.unsetPathView(rtn);
		return rtn;
	}
	
	PieceBlueprint pbp_connect = new PieceBlueprint(Connect.class);
	@SuppressWarnings("serial")
	protected static class Connect extends AndroidView {
		BasicView start, stop;
		PackType starttype, stoptype;
		WorldPoint offset1, offset2;
		Paint paint;
		@Override
		public void view_init() throws ChainException {
//			if(log != null)
//				Log.w("ACM","Chained and StandBy");
			start = (BasicView)pull();
			stop = (BasicView)pull();
			starttype = (PackType)pull();
			stoptype = (PackType)pull();
//			Log.w("ACM","Start");
			paint = new Paint();
			paint.setColor(Color.argb(128, 255, 255, 255));
			paint.setStyle(Paint.Style.STROKE); 
			paint.setAntiAlias(true);
			paint.setStrokeWidth(4);
		}
		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size, int alpha) {
			offset1 = getOffset(start.getSize(), starttype);
			offset2 = getOffset(stop.getSize(), stoptype);
			ScreenPoint sp1 = start.getCenter().plus(offset1).getScreenPoint(v);
			ScreenPoint sp2 = stop.getCenter().sub(offset2).getScreenPoint(v);
			Path p = new Path();
			p.moveTo(sp1.x, sp1.y);
//			p.cubicTo(sp1.x + offset1.x, sp1.y + offset1.y, sp2.x - offset2.x, sp2.y - offset2.y, sp2.x, sp2.y);
			p.lineTo(sp1.x + offset1.x, sp1.y + offset1.y);
			p.lineTo(sp2.x - offset2.x, sp2.y - offset2.y);
			p.lineTo(sp2.x, sp2.y);
			canvas.drawPath(p, paint);
			return true;
		}
	}
	static WorldPoint getOffset(WorldPoint size, PackType type) {
		WorldPoint offset = null;
		switch(type) {
		case FAMILY:
			offset = new WorldPoint((int)size.x/3, (int)size.y/3);
			break;
		case HEAP:
			offset = new WorldPoint(size.x/2, 0);
			break;
		default:
			offset = new WorldPoint(0, size.y/2);
		}
		return offset;
	}
	
	@Override
	public AnimationChainManager Log(String... s) {
		if(log != null)
			log.Log(s);
		return this;
	}
	public interface LogHandler {
		public void Log(String... s);
	}
	public interface TapChainPathView {
		public void setPathView(ChainPath second, BasicView v);
		public void unsetPathView(ChainPath p);
	}
	public interface TapChainPieceView {
		public void setView(ChainPiece cp, TapChainEditorView v);
		public void unsetView(ChainPiece cp);
		public void refreshView(ChainPiece cp, ChainPiece obj);
		public void moveView(TapChainViewI v);
	}
	public interface StatusHandler {
		public void getStateAndSetView(int state);
		public void tickView();
	}
}
