package org.tapchain.core;

import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Chain.IErrorCode;
import org.tapchain.core.Chain.Tickable;
import org.tapchain.core.Connector.InConnector;
import org.tapchain.core.Connector.OutConnector;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("serial")
public class ChainPiece<PARTNER extends Piece> extends Piece<PARTNER> implements Serializable, Tickable, Runnable {
	public static ExecutorService threadExecutor = Executors
			.newCachedThreadPool();
	protected IPieceHead fImpl;
	private Boolean _chainlive = false, controlled_by_ac = true,
			inited = false;
	// Thread _th = null;
	Future<?> f = null;
	private Chain _root_chain = null;
	ChainPiece cp_reactor = this;
	int mynum = 0;
	private IErrorHandler _errorHandler = null;
	protected IStatusHandler<IPiece> _statusHandler = null;
	String tag = null, lockTag = null;

	public enum PieceState implements IState {
		NOTSTARTED, STARTED, RUNNING, END, ERROR;
		@Override
		public boolean hasError() {
			return this == ERROR;
		}
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
		for(PathType type: PathType.values()) {
			InPathPack pack = addNewInPack(type);
			if(type == PathType.OFFER) {
				pack.setInType(InPathPack.Input.FIRST);
			} else {
				pack.setInType(InPathPack.Input.ALL);// PASSTHRU
			}
			if(type == PathType.EVENT)
				pack.addPathClass(new ClassEnvelope(Object.class));
		}

		for(PathType type: PathType.values()) {
			OutPathPack pack = addNewOutPack(type);
			if(type == PathType.FAMILY)  {
				pack.setOutType(Output.HIPPO);
			} else if(type == PathType.EVENT)
				pack.addPathClass(new ClassEnvelope(Object.class));
		}
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

	public ChainPiece setRootChain(Chain c) {
		_root_chain = c;
		return this;
	}
	
	public Chain getRootChain() {
		return _root_chain;
	}

	public IPiece setControlled(boolean c) {
		controlled_by_ac = c;
		return this;
	}

	public ChainPiece boost() {
		setControlled(false);
		return this;
	}

	boolean logFlag = false;
	String logTag = null;

	public ChainPiece setLogLevel(boolean _log) {
		logFlag = _log;
		return this;
	}
	
	public boolean getLogLevel() {
		return logFlag;
	}
	
	public void setLogTag(String tag) {
		logTag = tag;
	}

	String lastExecLog = null;
	Object lastExecObj = null;
	String nowExecLog = null;
    String header = "";
    int count = 0;

	@Override
	public <T> T __exec(T obj, String flg) {
		nowExecLog = flg;
		L(flg).go(obj);
		lastExecLog = flg;
		lastExecObj = obj;
		return obj;
	}
	
	public String printLastExecLog() {
		L(String.format("Last:%s, now:%s", lastExecLog, nowExecLog)).go(lastExecObj);
		return lastExecLog;
	}

	public <T> T __log(T obj, String tag) {
		if (_root_chain != null && _root_chain.log != null)
			if (logFlag) {
				_root_chain.log.addLog((logTag == null)?String.format("[%s]%s",mynum,getTag()):logTag,
					String.format("|%s| %s -> %s", header,tag, obj));
			}
		return obj;
	}

	public <T> T __logout(T obj, String tag) {
		if (_root_chain != null && _root_chain.log != null)
			if (logFlag) {
				_root_chain.log.addLog((logTag == null)?String.format("[%s]%s",mynum,getTag()):logTag,
						String.format("|%s| %s <- %s", header,tag, obj));
			}
		return obj;
	}

    public synchronized LogCase L(String tag) {
        String prev = new String(header);
        int prevCount = ++count;
        if(header.equals(""))
                header = Integer.toHexString(count);
        else
            header = header + "/" + Integer.toHexString(count);
        __log("Go", tag);
        count = 0;
        return new LogCase(tag, prev, prevCount);
    }

    public class LogCase {
        String tag = null;
        String prev;
        int prevCount;
        public LogCase(String _tag, String _prev, int _prevCount) {
            tag = _tag;
            prev = _prev;
            prevCount = _prevCount;
        }

        public <T> T go(T obj) {
            T rtn = __logout(obj, tag);
            lastExecLog = tag;
            lastExecObj = obj;
            header = prev;
            count = prevCount;
            return rtn;
        }

    }

	public ChainPiece setError(IErrorHandler er) {
		_errorHandler = er;
		return this;
	}

	public ChainPiece setStatusHandler(IStatusHandler<IPiece> st) {
		_statusHandler = st;
		return this;
	}

	public IStatusHandler<IPiece> getStatusHandler() {
		return _statusHandler;
	}

	public boolean isAlive() {
		return status != PieceState.NOTSTARTED && status != PieceState.END;
	}

	//3.Changing state
	public boolean postAppend() {
		return sendUnerrorEvent();
	}
	
	public IPiece restart() {
		if (isAlive()) {
			f.cancel(true);
		} else {
			start();
		}
		return this;
	}

	protected boolean _waitNext() throws InterruptedException {
		if(controlled_by_ac)
			getRootChain().waitNext(invalidating);
        invalidating = false;
		return true;
	}
	boolean invalidating = false;
	public void invalidate() {
		tick(Packet.HeartBeat);//1 is dummy data for onTick handlers
        //If invalidating is on(so this piece was kicked) but main thread stops at _waitNext,
        //Then this calls kick() again to wake up main thread.
        if(getRootChain() != null)
            getRootChain().kick(this);
        invalidating = true;
	}

	public IPiece start() {
		if (_root_chain == null)
			return this;
		if (isAlive())
			return this;
		if (!inited) {
			inited = true;
		}
		f = threadExecutor.submit(this);
		return this;
	}

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
				L("ChainPiece@wait").go(mynum);
				if (!_waitNext()) {
					break main_loop;
				}
				L("ChainPiece@do").go(mynum);
				if (!_doAndLoopInError(fImpl, cp_reactor)) {
					break main_loop;
				}
				L("ChainPiece@done").go(mynum);
			} catch (InterruptedException e) {
				if (!_chainlive)
					break main_loop;
				L("ChainPiece@reset").go(mynum);
				fImpl.pieceReset(cp_reactor);
			}
		}
		try {
			changeState(PieceState.END);
			L("ChainPiece@end").go(mynum);
			onTerminate();
		} catch (ChainException e) {
			onError(e);
		}
		if (_root_chain.AUTO_END) {
			_root_chain.removePiece(this);
			L("5 ChainPiece@ended").go(mynum);
		}
	}

	public IPiece end() {
		synchronized (_chainlive) {
			_chainlive = false;
		}
		if(f != null)
			f.cancel(true);
		return this;
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
				return rtn;
			} catch (ChainException e) {
				onError(e);
				//distinguish between interruption and error
				IErrorCode code = e.getErrorCode();
				if(code.isInterrupted())
					throw new InterruptedException();
				if(code.isLocked()) {
					try {
						L("ChainPiece").go(recvUnlockEvent());
					} finally {
						onUnerror(e);
						L("ChainPiece").go("onUnerror called");
					}
				}
			}
		}
	}

	protected boolean onError(ChainException e) {
		lock = new CountDownLatch(1);
		if (_errorHandler != null)
			_errorHandler.onError(this, e);
		changeState(PieceState.ERROR);
		lockTag = e.getErrorMessage();
		return true;
	}

	protected boolean onUnerror(ChainException e) {
		if (_errorHandler != null)
			_errorHandler.onUnerror(this, e);
		restoreState();
		return true;
	}

	protected boolean changeState(ChainPiece.PieceState state) {
		L("changeState").go(state.toString());
		if (_statusHandler != null)
			_statusHandler.changeViewState(state);
		status_bak = status;
		status = state;
		return true;
	}
	
	public String getState() {
		return status.toString();
	}

	@Override
	public int tick(Packet packet) {
		if (_statusHandler != null)
			return _statusHandler.tickView(this, packet);
		return 0;
	}

    int tickInterval = 0;
	@Override
	public int getTickInterval() {
//		if (_statusHandler != null)
//			return _statusHandler.getTickInterval();
		return tickInterval;
	}

    public void setTickInterval(int interval) {
        tickInterval = interval;
    }

	protected ChainPiece.PieceState restoreState() {
		status = status_bak;
		if (_statusHandler != null)
			_statusHandler.changeViewState(status);
		return status;
	}

	boolean recvUnlockEvent() throws InterruptedException {
		lock.await();
		lock = null;
		return true;
	}

	public boolean sendUnerrorEvent() {
		if (lock != null)
			lock.countDown();
		return true;
	}
	
	public boolean getLockStatus() {
		return lock != null;
	}
	
	public String getLockTag() {
		return (lockTag == null) ? "": lockTag;
	}
	
	//4.Termination
	protected void onTerminate() throws ChainException {
//        end();
    }

	CountDownLatch lock = null;


	@Override
	public Chain.ConnectionResultPath appendTo(PathType stack, IPiece target,
											 PathType stack_target) throws ChainException {
		super.appendTo(stack, target, stack_target);
		InConnector i = addInPath(stack);
		if(i == null)
			return null;
		Chain.ConnectionResultOutConnector o = target.appended(stack_target, this, null);
		if (i.connect(o.getResult())) {
			Path p = new Path((ChainPiece) o.getPiece(), this,
					o.getResult(), i, stack_target, stack);
			return new Chain.ConnectionResultPath(o.getPiece(), p);
		}
		//No connection
		return null;
	}

	@Override
	public Chain.ConnectionResultOutConnector appended(PathType stack_target, IPiece from,
											Output type) throws ChainException {
		OutConnector o = addOutPath(type, stack_target);
		if(o == null)
			return null;
		//Return ConnectionResultO object.
		return new Chain.ConnectionResultOutConnector(this, o);
	}


	@Override
	public String getTag() {
		return (tag!=null)?tag:getName();
	}

	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}


}