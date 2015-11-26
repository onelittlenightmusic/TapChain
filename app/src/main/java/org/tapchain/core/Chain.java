package org.tapchain.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.Connector.OutConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Chain implements JSONSerializable {
	static int n = 0;
	ConcurrentSkipListSet<IPiece> aFunc;
	private ChainController ctrl;
	private ChainPieceOperator operator = null;
	int mode = 0;// 0: Run-as-a-thread mode, 1: Automatic mode
	public static final int AUTO_MODE = 1;
	public boolean AUTO_END = true;
    int time = 0;
	ILogHandler log = null;
	String name = "";
	Chain(int _mode, int time) {
		aFunc = new ConcurrentSkipListSet<IPiece>();
		mode = _mode;
        this.time = time;
		setCtrl(new ChainController(time));
		setCallback(null);
		operator = new ChainPieceOperator(this);
	}

    protected CyclicBarrier signal = new CyclicBarrier(Integer.MAX_VALUE);

	Chain() {
		this(AUTO_MODE, 30);
	}

	public Chain setCallback(final IControlCallback cb) {
		if (cb == null) {
			getCtrl().Set(new IControlCallback() {
				public boolean onCalled() {
					Chain.this.notifyAllFunc();
					return false;
				}
			});
		} else {
			getCtrl().Set(new IControlCallback() {
				public boolean onCalled() {
					cb.onCalled();
					Chain.this.notifyAllFunc();
					return false;
				}
			});
		}
		return this;
	}
	
	public void registerAdapter(IChainAdapter adapter) {
		getCtrl().registerControllerAdapter(adapter);
	}
	
	public void unregisterAdapter(IChainAdapter adapter) {
		getCtrl().unregisterControllerAdapter(adapter);
	}

	public ConcurrentSkipListSet<IPiece> getPieces() {
		return aFunc;
	}
	
	public Chain setLog(ILogHandler l) {
		log = l;
		return this;
	}

	public Chain setAutoEnd(boolean end) {
		AUTO_END = end;
		return this;
	}

	public Chain kick(Object pc) {
		getCtrl().kick(pc);
		return this;
	}

	public void signal() {
		signal.reset();
	}

	public void waitNext(boolean now) throws InterruptedException {
		try {
            if(now)
                signal.await(time, TimeUnit.MILLISECONDS);
            else
    			signal.await();
		} catch (BrokenBarrierException e) {
		} catch (TimeoutException e) {
//            e.printStackTrace();
        }
    }

	public void setName(String _name) {
		name = _name;
	}

	public String getName() {
		return name;
	}
	public enum Flex {
		FIXED, FLEX
	}

	public ChainPieceOperator getOperator() {
		return operator;
	}

	// 3.Changing state
	public IPiece addPiece(IPiece cp) {
		if (cp instanceof ChainPiece)
			((ChainPiece) cp).setParentChain(this);
		if (mode == AUTO_MODE) {
			if (cp instanceof ChainPiece) {
				((ChainPiece) cp).start();
			}
			kick(cp);
		}
		if (log != null)
			log.log("Chain", String.format("+ADD %s (%s)", cp.getTag(), getName()));
		return cp;

	}

	public boolean notifyAllFunc() {
		if (aFunc.isEmpty())
			return false;
		else
			signal();
		return true;
	}

	public IPiece removePiece(IPiece bp) {
		if (log != null)
			log.log("Chain", String.format("-REM %s (%s)", bp.getTag(), getName()));
		aFunc.remove(bp);
		return bp;
	}

	public ChainController getCtrl() {
		return ctrl;
	}

	public void setCtrl(ChainController ctrl) {
		this.ctrl = ctrl;
	}

	public class ChainOperator {
		List<IPiece> q = new ArrayList<IPiece>();
		Chain _p = null;

		public ChainOperator(Chain parent) {
			_p = parent;
		}

		public boolean isEmpty() {
			return q.isEmpty();
		}

		boolean remove(ChainPiece ef) {
			return false;
		}

		public synchronized IPiece add(IPiece bp) {
			q.add(bp);
			kick(bp);
			return bp;
		}

		public synchronized List<IPiece> save() {
			aFunc.addAll(q);
			for (IPiece cp : q)
				addPiece(cp);
			q.clear();
			return q;
		}

	}

	public class ChainPieceOperator extends ChainOperator {
		public ChainPieceOperator(Chain parent) {
			super(parent);
		}

		public synchronized void start() {
			if (mode == AUTO_MODE)
				return;
			for (IPiece cp : aFunc)
				if (cp instanceof ChainPiece) {
					((ChainPiece) cp).start();
				}
			getCtrl().kick(null);
		}

		public synchronized void reset() {
			for (IPiece cp : aFunc)
				if (cp instanceof ChainPiece) {
					((ChainPiece) cp).restart();
				}
		}
	}

	public static class ConnectionResult<T> {
		IPiece piece;
		T t;

		public ConnectionResult(IPiece piece, T t) {
			this.piece = piece;
			this.t = t;
		}

		public IPiece getPiece() {
			return piece;
		}

		public T getResult() {
			return t;
		}

	}

	public static class ConnectionResultPath extends ConnectionResult<IPath> {
		ClassEnvelope _class;
		public ConnectionResultPath(IPiece piece, IPath t) {
			super(piece, t);
		}

		public ClassEnvelope getConnectionClass() {
			 return _class;
		}

		public void setConnectionClass(ClassEnvelope connectionClass) {
			this._class = connectionClass;
		}
	}

	public static class ConnectionResultOutConnector extends
			ConnectionResult<OutConnector> {
		public ConnectionResultOutConnector(IPiece piece, OutConnector t) {
			super(piece, t);
		}
	}

	public static class ChainException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String errorMessage = "";
		String location = "";
		IErrorCode errorCode = PieceErrorCode.LOOPABLE;
		Object target = null;

		ChainException() {
			super("ChainException: Unknown Error");
			errorMessage = "Unknown";
		}

		public ChainException(String str) {
			super(str);
			errorMessage = "Unknown";
		}

		public ChainException(String errorMessage, String location, Object target) {
			this(String.format("ChainException: %s on %s", errorMessage, location));
			this.errorMessage = errorMessage;
			this.location = location;
			this.target = target;
		}

		public ChainException(IPiece piece, String str, IErrorCode _loop) {
			this("ChainException: " + str);
			errorMessage = str;
			location = piece.getName();
			errorCode = _loop;
			target = piece;
		}

		public ChainException(IPiece piece, ClassEnvelope class1, String str, IErrorCode _loop) {
			this(piece, str, _loop);
			target = class1;
		}
		public ChainException(IPiece piece, String str) {
			this(piece, str, PieceErrorCode.LOOPABLE);
		}

		public ChainException(Connector connector, String str) {
			this("ChainException: " + str);
			errorMessage = str;
			location = "Path";
			target = connector;
		}

		ChainException(IManager<?, ?> manager, String str) {
			this("ChainException: " + str);
			errorMessage = str;
			location = "Manager";
			target = manager;
		}

		ChainException(IPiece piece, String str, Throwable throwable) {
			super("ChainException: " + str, throwable);
			errorMessage = str;
			location = piece.getName();
			target = piece;
		}

		public ChainException(Factory pieceFactory, String str) {
			this("ChainException: " + str);
			errorMessage = str;
			location = "Factory";
			target = pieceFactory;
		}

		public String getLocation() {
			return location;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
		
		public IErrorCode getErrorCode() {
			return errorCode;
		}
		
		public Object getObject() {
			return target;
		}

	}
	
	public interface IErrorCode {
		public boolean isLocked();
		public boolean isInterrupted();
		public PathType getPathTypeLocked();
	}

	public enum PieceErrorCode implements IErrorCode {
		LOOPABLE, INTERRUPT, LOCK_OFFER(PathType.OFFER), LOCK_FAMILY(PathType.FAMILY), LOCK_OTHER;
		PieceErrorCode() {
		}
		PieceErrorCode(PathType _type) {
			this();
		}
		public boolean isLocked() {
			return this == LOCK_OFFER || this == LOCK_FAMILY || this == LOCK_OTHER;
		}
		public boolean isInterrupted() {
			return this == INTERRUPT;
		}
		public PathType getPathTypeLocked() {
			if(this == LOCK_OFFER)
				return PathType.OFFER;
			else if (this == LOCK_FAMILY)
				return PathType.FAMILY;
			return PathType.EVENT;
		}
	}

	public interface IPathListener {
		public void OnPushed(Connector p, Object obj)
				throws InterruptedException;
	}

	public interface Tickable {
        int tick(Packet packet);

        public int getTickInterval();
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject rtn = new JSONObject();
		for(IPiece p: aFunc)
			if(p instanceof JSONSerializable)
				rtn.putOpt(p.getName(),((JSONSerializable)p).toJSON());
		return rtn;
	}

}
