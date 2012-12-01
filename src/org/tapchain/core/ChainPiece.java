package org.tapchain.core;

import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.Chain.Tickable;
import org.tapchain.core.Connector.ChainInConnector;
import org.tapchain.core.Connector.ChainOutConnector;
import org.tapchain.core.PathPack.ChainInPathPack;
import org.tapchain.core.PathPack.ChainOutPathPack.Output;
import org.tapchain.core.PathPack.*;

@SuppressWarnings("serial")
public class ChainPiece extends Piece implements IPiece, Serializable, Tickable {
	public static ExecutorService threadExecutor = Executors
			.newCachedThreadPool();
	protected IPieceHead fImpl;
	private Boolean _chainlive = false, controlled_by_ac = true,
			inited = false;
	// Thread _th = null;
	Future<?> f = null;
	Chain _root_chain = null;
	ChainPiece cp_reactor = this;
	int mynum = 0;
	private IErrorHandler _error = null;
	private IStatusHandler _statush = null;

	public enum PieceState {
		NOTSTARTED, STARTED, RUNNING, END, ERROR
	}

	//1.Initialization
	PieceState status = PieceState.NOTSTARTED,
			status_bak = PieceState.NOTSTARTED;

	ChainPiece() {
		this(new IPieceHead() {
			@Override
			public boolean pieceRun(IPiece f) throws InterruptedException {
				return false;
			}

			@Override
			public boolean pieceReset(IPiece f) {
				return false;
			}
		});
	}

	ChainPiece(IPieceHead tmpFImpl) {
		initNum();
		fImpl = tmpFImpl;
		for(PackType type: PackType.values()) {
			ChainInPathPack pack = addNewInPack(type);
			pack.addPathClass(Object.class);
			if(type == PackType.HEAP)
				pack.setInType(ChainInPathPack.Input.FIRST);
			else
				pack.setInType(ChainInPathPack.Input.ALL);// PASSTHRU
		}

		for(PackType type: PackType.values()) {
			ChainOutPathPack pack = addNewOutPack(type);
			pack.addPathClass(Object.class);
			if(type == PackType.FAMILY)
				pack.setOutType(Output.HIPPO);
		}
	}

	public void init(Object... obj) {
	}
	
	public void initNum() {
		mynum = Chain.n++;
	}
	@Override
	public int getId() {
		return mynum;
	}

	//2.Getters and setters
	protected ChainPiece setFunc(IPieceHead f) {
		fImpl = f;
		return this;
	}

	public ChainPiece setParent(Chain c) {
		_root_chain = c;
		return this;
	}

	public ChainPiece setControlled(boolean c) {
		controlled_by_ac = c;
		return this;
	}

	public ChainPiece boost() {
		setControlled(false);
		return this;
	}

	boolean logFlag = false;

	public ChainPiece setLogLevel(boolean _log) {
		logFlag = _log;
		return this;
	}

	public <T> T __exec(T obj, String flg) {
		if (logFlag)
			return __log(obj, flg);
		return obj;
	}

	public <T> T __log(T obj, String flg) {
		if (_root_chain != null && _root_chain.log != null)
			_root_chain.log.log(flg,
					String.format("RTN: %s, Actor: %s", obj, getName()));
		return obj;

	}

	public ChainPiece setError(IErrorHandler er) {
		_error = er;
		return this;
	}

	public ChainPiece setStatusHandler(IStatusHandler st) {
		_statush = st;
		return this;
	}

	public IStatusHandler getStatusHandler() {
		return _statush;
	}

	public boolean isAlive() {
		return status != PieceState.NOTSTARTED && status != PieceState.END;
	}

	//3.Changing state
	public boolean postAppend() {
		return sendUnerrorEvent();
	}
	
	public void restart() {
		if (isAlive()) {
			f.cancel(true);
			// signal = new CountDownLatch(1);
		} else {
			start();
		}
		return;
	}

	public boolean waitNext() throws InterruptedException {
		_root_chain.waitNext();
		return true;
	}

	public void start() {
		final ChainPiece cp_this = this;
		if (_root_chain == null)
			return;
		final boolean autoend = _root_chain.AUTO_END;
		if (isAlive())
			return;
		// _th = new Thread() {
		if (!inited) {
//			init();
			inited = true;
		}
		f = threadExecutor.submit(new Runnable() {
			public void run() {
				changeState(PieceState.STARTED);
				// signal = new CountDownLatch(1);
				synchronized (_chainlive) {
					_chainlive = true;
				}
				fImpl.pieceReset(cp_reactor);
				changeState(PieceState.RUNNING);
				main_loop: while (true) {
					try {
						__exec(String.format("ID:%d Main[#0 ->SIG]", mynum),
								"ChainPiece#impl");
						if (controlled_by_ac && !cp_this.waitNext()) {
							break main_loop;
						}
						__exec(String.format("ID:%d Main[#1 SIG->FUNC]", mynum),
								"ChainPiece#impl");
						if (!_doAndLoopInError(fImpl, cp_reactor)) {
							break main_loop;
						}
						__exec(String.format("ID:%d Main[#2 FUNC->OK]", mynum),
								"ChainPiece#impl");
					} catch (InterruptedException e) {
						if (!_chainlive)
							break main_loop;
						__exec(String.format("ID:%d Main[#-1 INTERRUPTED]",
								mynum), "ChainPiece#impl");
						fImpl.pieceReset(cp_reactor);
					}
				}
				try {
					changeState(PieceState.END);
					__exec(String.format("ID:%d Main[#-2 END]", mynum),
							"ChainPiece#impl");
					cp_this.onTerminate();
				} catch (ChainException e) {
					cp_this.onError(e);
				}
				if (autoend) {
					_root_chain.removePiece(cp_this);
					__exec(String.format("ID:%d Main[#-3 AUTOREMOVE]", mynum),
							"ChainPiece#impl");
				}
			}
		});
		// };
		// _th.start();
	}

	public void end() {
		synchronized (_chainlive) {
			_chainlive = false;
		}
		f.cancel(true);
		return;
	}

	protected void waitEnd() throws InterruptedException {
		if (f != null)
			try {
				f.get();
			} catch (CancellationException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	}

	private boolean _doAndLoopInError(IPieceHead head, ChainPiece cp)
			throws InterruptedException {
		boolean rtn = false;
		while (true) {
			try {
				rtn = head.pieceRun(cp);
				tick();
				return rtn;
			} catch (ChainException e) {
				onError(e);
				switch (e.loop) {
				case INTERRUPT:
					throw new InterruptedException();
				case LOCK:
					try {
						recvUnerrorEvent();
					} finally {
						onUnerror(e);
					}
				default:
				}
			}
		}
	}

	protected boolean onError(ChainException e) {
		if (_error != null)
			_error.onError(this, e);
		changeState(PieceState.ERROR);
		return true;
	}

	protected boolean onUnerror(ChainException e) {
		if (_error != null)
			_error.onCancel(this, e);
		restoreState();
		return true;
	}

	protected boolean changeState(ChainPiece.PieceState state) {
		__exec(state.toString(), "changeState");
		if (_statush != null)
			_statush.getStateAndSetView(state);
		status_bak = status;
		status = state;
		return true;
	}

	public boolean tick() {
		if (_statush != null)
			_statush.tickView();
		return true;
	}

	protected ChainPiece.PieceState restoreState() {
		status = status_bak;
		if (_statush != null)
			_statush.getStateAndSetView(status);
		return status;
	}

	boolean recvUnerrorEvent() throws InterruptedException {
		lock = new CountDownLatch(1);
		lock.await();
		return true;
	}

	boolean sendUnerrorEvent() {
		if (lock != null)
			lock.countDown();
		return true;
	}
	//4.Termination
	protected void onTerminate() throws ChainException {
		return;
	}

	// public void detachAll() {
	// partners.detachAll();
	// return;
	// }
	// public Collection<IPath> getLinks() {
	// return partners.getPaths();
	// }
	// public Collection<IPiece> getPartners() {
	// return partners.getPartners();
	// }
	// public void setPartner(IPath o, IPiece cp) {
	// partners.setPartner(o, cp);
	// }
	// public IPath detach(IPiece cp) {
	// return partners.getPath((ChainPiece)cp).detach();
	// }

	CountDownLatch lock = null;


	public static class FlexPiece extends ChainPiece {
		/** FlexPiece is a connection-size-flexible subclass of ChainPiece.
		 * 
		 */
		ChainPiece firstPiece = null, lastPiece = null;
		private static final long serialVersionUID = 1L;

		FlexPiece() {
			super();
			setFirstPiece(this);
			setLastPiece(this);
		}

		FlexPiece(IPieceHead fImpl) {
			super(fImpl);
			setFirstPiece(this);
			setLastPiece(this);
		}
		
		public void setFirstPiece(IPiece appended) {
			firstPiece = (ChainPiece) appended;
		}
		
		public void setLastPiece(IPiece appending) {
			lastPiece = (ChainPiece) appending;
		}
		
		public IPiece getFirstPiece() {
			return firstPiece;
		}
		
		public IPiece getLastPiece() {
			return lastPiece;
		}
		
		@Override
		public Chain.ConnectionResultIO appendTo(Chain.PackType stack, IPiece target,
				Chain.PackType stack_target) throws Chain.ChainException {
			super.appendTo(stack, target, stack_target);
			//Create new ChainInConnector.
			ChainInConnector i = firstPiece.addInPath(stack);
			//Get new connection with target piece.
			Chain.ConnectionResultO o = target.appended(null, stack_target, firstPiece);
			//Check available connection between this piece and target piece.
			if (i.connect(o.getConnect())) {
				//Get path object
				ConnectorPath p = new ConnectorPath((ChainPiece) o.getPiece(), firstPiece,
						o.getConnect(), i);
				//Return ConnectionResultIO object.
				return new Chain.ConnectionResultIO(o.getPiece(), p);
			}
			//No connection
			return null;
		}

		@Override
		public Chain.ConnectionResultO appended(Output type, Chain.PackType stack_target,
				IPiece from) throws Chain.ChainException {
			//Create new ChainOutConnector.
			ChainOutConnector o = lastPiece.addOutPath(type, stack_target);
			// partner.setPartner(o, from);
			//Return ConnectionResultO object.
			return new Chain.ConnectionResultO(lastPiece, o);
		}

		@Override
		public void detached(IPiece cp) {
			super.detached(cp);
		}
	}


}