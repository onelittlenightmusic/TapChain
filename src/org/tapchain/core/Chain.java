package org.tapchain.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import org.tapchain.core.ChainPiece;
import org.tapchain.core.ActorManager.ILogHandler;
import org.tapchain.core.ChainController.IControlCallback;
import org.tapchain.core.Connector.*;

public class Chain {
	static int n = 0;
	ConcurrentSkipListSet<IPiece> aFunc;
	ChainController ctrl;
	private ChainPieceOperator operator = null;
	int mode = 0;// 0: Run-as-a-thread mode, 1: Automatic mode
	public static int THREAD_MODE = 0;
	public static int AUTO_MODE = 1;
	public boolean AUTO_END = true;
	ILogHandler log = null;

	// 1.Initialization
	Chain(int _mode, int time) {
		aFunc = new ConcurrentSkipListSet<IPiece>();
		mode = _mode;
		ctrl = new ChainController(time);
		setCallback(null);
		operator = new ChainPieceOperator(this);
	}

	Chain() {
		this(AUTO_MODE, 30);
	}

	// 2.Getters and setters
	public Chain setCallback(final IControlCallback cb) {
		// if(mode == AUTO_MODE) {
		if (cb == null) {
			ctrl.Set(new IControlCallback() {
				public boolean onCalled() {
					Chain.this.notifyAllFunc();
					return false;
				}
			});
		} else {
			ctrl.Set(new IControlCallback() {
				public boolean onCalled() {
					cb.onCalled();
					Chain.this.notifyAllFunc();
					return false;
				}
			});
		}
		return this;
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

	public Chain kick() {
		ctrl.kick();
		return this;
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
			((ChainPiece) cp).setParent(this);
		if (mode == AUTO_MODE) {
			// aFunc.add(cp);
			if (cp instanceof ChainPiece) {
				((ChainPiece) cp).start();
			}
			kick();
		}
		if (log != null)
			log.log("Chain", String.format("RTN: ADD, CP: %s", cp.getName()));
		return cp;

	}

	public boolean notifyAllFunc() {
		// for (IPiece f : aFunc)
		if (aFunc.isEmpty())
			return false;
		else
			aFunc.first().signal();
		return true;
	}

	public ChainPiece _addPiece(ChainPiece cp) {
		aFunc.add(cp);
		return cp;
	}

	public IPiece removePiece(IPiece bp) {
		bp.end();
		if (log != null)
			log.log("Chain", String.format("RTN: REM, CP: %s", bp.getName()));
		aFunc.remove(bp);
		return bp;
	}

	// 4.Termination
	// 5.Local classes
	public class ChainOperator {
		List<IPiece> q = new ArrayList<IPiece>();
		List<IPiece> q2 = new ArrayList<IPiece>();
		IAxon<String> revolver = new Toggle<String>();
		Chain _p = null;

		// 1.Initialization
		public ChainOperator(Chain parent) {
			_p = parent;
		}

		// 2.Getters and setters
		public boolean isEmpty() {
			return q.isEmpty();
		}

		// 3.Changing state
		boolean remove(ChainPiece ef) {
			return false;
		}

		public synchronized IPiece add(IPiece bp) {
			q.add(bp);
			kick();
			return bp;
		}

		public synchronized List<IPiece> save() {
			aFunc.addAll(q);
			for (IPiece cp : q)
				addPiece(cp);
			q = new ArrayList<IPiece>();
			return q;
		}

		// 4.Termination
		// 5.Local classes

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
			ctrl.kick();
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

		public T getConnect() {
			return t;
		}
	}

	public static class ConnectionResultIO extends ConnectionResult<ConnectorPath> {
		public ConnectionResultIO(IPiece piece, ConnectorPath t) {
			super(piece, t);
		}
	}

	public static class ConnectionResultO extends
			ConnectionResult<ChainOutConnector> {
		public ConnectionResultO(IPiece piece, ChainOutConnector t) {
			super(piece, t);
		}
	}

	public static class FlexPiece extends ChainPiece {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		FlexPiece() {
			super();
		}

		FlexPiece(IPieceHead fImpl) {
			super(fImpl);
		}

		public ConnectionResultIO appendTo(PackType stack, IPiece cp, Class<?> cls,
				PackType stack_target) throws ChainException {
			super.appendTo(stack, cp, stack_target);
			ChainInConnector i = addInPath(cls, stack);
			ConnectionResultO o = cp.appended(cls, null, stack_target, this);
			if (i.connect(o.getConnect())) {
				ConnectorPath p = new ConnectorPath((ChainPiece) o.getPiece(), this,
						o.getConnect(), i);
				return new ConnectionResultIO(o.getPiece(), p);
			}
			return null;
		}

		@Override
		public ConnectionResultIO appendTo(PackType stack, IPiece cp,
				PackType stack_target) throws ChainException {
			return appendTo(stack, cp, ChainPiece.class, stack_target);
		}

		@Override
		public ConnectionResultO appended(Class<?> cls, Output type,
				PackType stack_target, IPiece from) throws ChainException {
			ChainOutConnector o = addOutPath(cls, type, stack_target);
			// partner.setPartner(o, from);
			return new ConnectionResultO(this, o);
		}

		@Override
		public void detached(IPiece cp) {
			super.detached(cp);
		}
	}

	public static class FixedChainPiece extends ChainPiece {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		FixedChainPiece() {
			super();
		}

		FixedChainPiece(IPieceHead fImpl) {
			super(fImpl);
		}

		@Override
		public ConnectionResultIO appendTo(PackType stack, IPiece cp,
				PackType stack_target) throws ChainException {
			super.appendTo(stack, cp, stack_target);
			for (ChainInConnector i : getInPack(PackType.PASSTHRU)) {
				ConnectionResultO o = cp.appended(i.class_name, null, stack_target,
						this);
				if (i.connect(o.getConnect())) {
					// used = !hasAnyUnusedConnect();
					return new ConnectionResultIO(o.getPiece(), new ConnectorPath(
							(ChainPiece) o.getPiece(), this, o.getConnect(), i));
				}
			}
			return null;
		}

		protected ConnectionResultO appended(Class<?> cls) throws ChainException {
			for (ChainOutConnector o : getOutPack(PackType.PASSTHRU))
				if (o.class_name == cls)
					return new ConnectionResultO(this, o);
			return null;
		}

	}

	enum Output {
		NORMAL, HIPPO, SYNC, TOGGLE
	}

	public static class ChainException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String err = "";
		String location = "";
		LoopableError loop = LoopableError.LOOPABLE;

		ChainException() {
			super("ChainException: Unknown Error");
			err = "Unknown";
		}

		ChainException(IPiece piece, String str, LoopableError _loop) {
			super("ChainException: " + str);
			err = str;
			location = piece.getName();
			loop = _loop;
		}

		ChainException(IPiece piece, String str) {
			this(piece, str, LoopableError.LOOPABLE);
		}

		ChainException(Connector cp, String str) {
			super("ChainException: " + str);
			err = str;
			location = "Path";
		}

		ChainException(IManager<?> cp, String str) {
			super("ChainException: " + str);
			err = str;
			location = "Manager";
		}

		ChainException(ChainPiece cp, String str, Throwable throwable) {
			super("ChainException: " + str, throwable);
			err = str;
			location = cp.getName();
		}

		public ChainException(Factory pieceFactory, String str) {
			super("ChainException: " + str);
			err = str;
			location = "Factory";
		}

	}

	public enum LoopableError {
		LOOPABLE, INTERRUPT, LOCK
	}

	public interface IPathListener {
		public void OnPushed(Connector p, Object obj)
				throws InterruptedException;
	}

	public interface Tickable {
		boolean tick();
	}

	public interface IPieceHead {
		abstract boolean pieceRun(IPiece f) throws InterruptedException,
				ChainException;

		abstract boolean pieceReset(IPiece f);
	}

	public interface IPiece {
		public ConnectionResultO appended(Class<?> cls, Output type, PackType stack,
				IPiece from) throws ChainException;

		public ConnectionResultIO appendTo(PackType stack, IPiece piece_to,
				PackType stack_target) throws ChainException;

		public void detached(IPiece _cp_end);

		public void setPartner(IPath chainPath, IPiece _cp_start);

		public IPath detach(IPiece y);

		public Collection<IPiece> getPartners();

		public boolean isConnectedTo(IPiece target);

		public IPiece signal();

		public void end();

		public String getName();

		public <T> T __exec(T obj, String flg);
	}

	public enum PackType {
		PASSTHRU, HEAP, FAMILY, EVENT
	}
}
