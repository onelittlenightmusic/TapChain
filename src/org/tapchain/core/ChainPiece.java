package org.tapchain.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.tapchain.core.ActorChain.IErrorHandler;
import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.IPieceHead;
import org.tapchain.core.Chain.Output;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.Chain.Tickable;
import org.tapchain.core.Connector.*;
import org.tapchain.core.PathPack.*;

@SuppressWarnings("serial")
public class ChainPiece extends Piece implements IPiece, Serializable, Tickable {
		public static ExecutorService threadExecutor = Executors.newCachedThreadPool();
		protected IPieceHead fImpl;
		protected ArrayList<ChainInPathPack> inPack = new ArrayList<ChainInPathPack>();
		protected ArrayList<ChainOutConnectorPack> outPack = new ArrayList<ChainOutConnectorPack>();
		protected static CyclicBarrier signal = new CyclicBarrier(Integer.MAX_VALUE);
		private Boolean _chainlive = false, controlled_by_ac = true, inited = false;
//		Thread _th = null;
		Future<?> f = null;
		Chain _root_chain = null;
		ChainPiece cp_reactor = this;
		int mynum = 0;
		protected ChainPiece.Partner partner = new Partner();
		private IErrorHandler _error = null;
		private IStatusHandler _statush = null;
		public enum PieceState {
			NOTSTARTED, STARTED, RUNNING, END, ERROR
		}
		ChainPiece.PieceState status = PieceState.NOTSTARTED, status_bak = PieceState.NOTSTARTED;

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
			mynum = Chain.n++;
			fImpl = tmpFImpl;
			addNewInPack().setInType(ChainInPathPack.Input.ALL);//PASSTHRU
			addNewInPack().setInType(ChainInPathPack.Input.FIRST);//HEAP
			addNewInPack().setInType(ChainInPathPack.Input.ALL);//FAMILY
			addNewInPack().setInType(ChainInPathPack.Input.ALL);//.setUserPathListener(reset);//EVENT
			
			addNewOutPack();//PASSTHRU
			addNewOutPack();//HEAP
			addNewOutPack().setOutType(Output.HIPPO);//FAMILY
			addNewOutPack();//EVENT
		}
		
		protected ChainInPathPack addNewInPack() {
			ChainInPathPack rtn = new ChainInPathPack(this);
			inPack.add(rtn);
			return rtn;
		}
		
		protected ChainOutConnectorPack addNewOutPack() {
			ChainOutConnectorPack rtn = new ChainOutConnectorPack(this);
			outPack.add(rtn);
			return rtn;
		}
		
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
			if(logFlag)
				return __log(obj, flg);
			return obj;
		}
		
		public <T> T __log(T obj, String flg) {
			if( _root_chain != null && _root_chain.log != null)
				_root_chain.log.log(flg, String.format("RTN: %s, CP: %s", obj, getName()));
			return obj;
			
		}
		
		public void init() {
		}
		public void restart() {
			if(isAlive()) {
				f.cancel(true);
//				signal = new CountDownLatch(1);
			} else {
				start();
			}
			return;
		}
		
		@Override
		public ChainPiece signal() {
				signal.reset();
			return this;
		}
		
		public boolean next() throws InterruptedException {
			try {
				signal.await();
			} catch (BrokenBarrierException e) {
//				throw new InterruptedException();
			}
			return true;
	}
	
		public void start() {
			final ChainPiece cp_this = this;
			if(_root_chain == null) return;
			final boolean autoend = _root_chain.AUTO_END;
			if(isAlive()) return;
//			_th = new Thread() {
			if(!inited) {
				init();
				inited = true;
			}
			f = threadExecutor.submit(new Runnable() {
				public void run() {
					changeState(PieceState.STARTED);
//					signal = new CountDownLatch(1);
					synchronized(_chainlive) {
						_chainlive = true;
					}
					fImpl.pieceReset(cp_reactor);
					changeState(PieceState.RUNNING);
					main_loop: while (true) {
						try {
							__exec(String.format("ID:%d Main[#0 ->SIG]", mynum), "ChainPiece#impl");
							if(controlled_by_ac && !cp_this.next()) {
								break main_loop;
							}
							__exec(String.format("ID:%d Main[#1 SIG->FUNC]", mynum), "ChainPiece#impl");
							if(!_doAndLoopInError(fImpl, cp_reactor)) {
								break main_loop;
							}
							__exec(String.format("ID:%d Main[#2 FUNC->OK]", mynum), "ChainPiece#impl");
							tick();
						} catch (InterruptedException e) {
							if(!_chainlive)
								break main_loop;
							__exec(String.format("ID:%d Main[#-1 INTERRUPTED]", mynum), "ChainPiece#impl");
							fImpl.pieceReset(cp_reactor);
						}
					}
					try {
						changeState(PieceState.END);
						__exec(String.format("ID:%d Main[#-2 END]", mynum), "ChainPiece#impl");
						cp_this.onTerminate();
					} catch (ChainException e) {
						cp_this.onError(e);
					}
					if(autoend)  {
						 _root_chain.removePiece(cp_this);
						__exec(String.format("ID:%d Main[#-3 AUTOREMOVE]", mynum), "ChainPiece#impl");
					}
				}
			});
//			};
//			_th.start();
		}
		
		public void end() {
			synchronized(_chainlive) {
				_chainlive = false;
			}
			f.cancel(true);
			return;
		}

		protected void waitEnd() throws InterruptedException {
			if(f!=null)
				try {
					f.get();
				} catch (CancellationException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
		}
		
		private boolean _doAndLoopInError(IPieceHead head, ChainPiece cp) throws InterruptedException {
			while(true) {
				try {
					return head.pieceRun(cp);
				} catch (ChainException e) {
					onError(e);
					switch(e.loop){
					case INTERRUPT:
						throw new InterruptedException();
					case LOCK:
						try{
							recvUnerrorEvent();
						} finally {
							onUnerror(e);
						}
					default:
					}
				}
			}
		}
		protected ChainPiece setError(IErrorHandler er) {
			_error = er;
			return this;
		}
		
		protected ChainPiece setStatusHandler(IStatusHandler st) {
			_statush = st;
			return this;
		}
		
		protected IStatusHandler getStatusHandler() {
			return _statush;
		}

		protected boolean onError(ChainException e) {
			if (_error != null)
				_error.onError(this, e);
			changeState(PieceState.ERROR);
			return true;
		}
		
		protected boolean onUnerror(ChainException e) {
			if(_error != null)
				_error.onCancel(this, e);
			restoreState();
			return true;
		}
		
		protected boolean changeState(ChainPiece.PieceState state) {
			__exec(state.toString(), "changeState");
			if(_statush != null)
				_statush.getStateAndSetView(state.ordinal());
			status_bak = status;
			status = state;
			return true;
		}
		
		public boolean tick() {
			if(_statush != null)
				_statush.tickView();
			return true;
		}
		
		protected ChainPiece.PieceState restoreState() {
			status = status_bak;
			if(_statush != null)
				_statush.getStateAndSetView(status.ordinal());
			return status;
		}
		
		public boolean isAlive() {
			return status!=PieceState.NOTSTARTED && status!=PieceState.END;
		}
		protected void onTerminate() throws ChainException {
			return;
		}
		
		
		//2.PathPack setting functions
		protected ChainOutConnectorPack getOutPack(PackType stack) {
			return outPack.get(stack.ordinal());
		}
		protected ChainOutConnectorPack getOutPack(int num) {
			if(num >= outPack.size())
				return null;
			return outPack.get(num);
		}
		protected ChainInPathPack getInPack(PackType packtype) {
			return inPack.get(packtype.ordinal());
		}
		
		protected ChainInPathPack getInPack(int num) {
			if(num >= inPack.size())
				return null;
			return inPack.get(num);
		}
		public ChainPiece setOutPackType(PackType pack, Output type) {
			getOutPack(pack).setOutType(type);
			return this;
		}
		
		public ChainPiece setInPackType(PackType pack, ChainInPathPack.Input type) {
			getInPack(pack).setInType(type);
			return this;
		}
		protected ChainInConnector addInPath(Class<?> c, PackType stack) {
			return getInPack(stack).addNewPath(c);
		}
		protected ChainOutConnector addOutPath(Class<?> c, Output io, PackType stack) {
			ChainOutConnector rtn = getOutPack(stack).addNewPath(c, io);
			return rtn;
		}
		public void detachAll() {
			partner.detachAll();
			return;
		}
		public Collection<IPath> getLinks() {
			return partner.getPaths();
		}
		public Collection<IPiece> getPartners() {
			return partner.getPartners();
		}
		public void setPartner(IPath o, IPiece cp) {
			partner.setPartner(o, cp);
		}
		public IPath detach(IPiece cp) {
			return partner.getPath((ChainPiece)cp).detach();
		}
		public boolean hasInPath(PackType packtype) {
			return getInPack(packtype).hasPath();
		}

		public boolean hasOutPath(PackType packtype) {
			return getOutPack(packtype).hasPath();
		}

		
		//3.Input/Output functions
		public <T> T getCache(ChainInConnector i) throws InterruptedException {
			if(getInPack(PackType.PASSTHRU).contains(i)) {
				return i.<T>getCache();
			}
			else if(getInPack(PackType.EVENT).contains(i)) {
				return i.<T>getCache();
			}
			return null;
		}
		
		public boolean outputAll(PackType type, ArrayList<?> ar) throws InterruptedException {
			return getOutPack(type).outputAll(ar);
		}

		public boolean outputAllSimple(PackType type, Object obj) throws InterruptedException {
			return getOutPack(type).outputAllSimple(obj);
		}
		
		
		public boolean outputAllReset() {
//			if(aOutHeap.array.isEmpty()) return false;
			boolean rtn = false;
//			Log.e("AllReset", "Called");
//			rtn |= aOutHeap.send_reset();
//			rtn |= aOut.send_reset();
//			rtn |= aOutThis.send_reset();
//			aInEvent.send_reset();
			rtn |= getOutPack(PackType.EVENT).send_reset();
			return rtn;
		}
		protected ChainPiece resetInPathPack(PackType pack) {
			getInPack(pack).reset();
			return this;
		}
		
		ChainPiece getNext() {
			ChainPiece tmp = null;
			if(getOutPack(PackType.PASSTHRU).isEmpty()) {
			} else {
				getOutPack(PackType.PASSTHRU).get(0);
			}
			return tmp;
		}
		
		//4.External functions related with connections(called by other thread)
		public void detached(IPiece cp) {
			partner.unsetPartner(cp);
		}
		
		public boolean postAppend() {
			return sendUnerrorEvent();
		}
		
		public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
			getOutPack(PackType.PASSTHRU).waitOutput(rtn);
		}
		public void waitOutputAll(Object rtn) throws InterruptedException {
			getOutPack(PackType.PASSTHRU).waitOutputAll(rtn);
		}
		
//		boolean outputType = false;//true: wait for at least one output, false: no wait
		public boolean clearInputHeap() {
			if(getInPack(PackType.HEAP).isEmpty()) return false;
			getInPack(PackType.HEAP).reset();
			return true;
		}
		
		public boolean clearOutputHeap() {
			if(getOutPack(PackType.HEAP).isEmpty()) return false;
			getOutPack(PackType.HEAP).reset();
			return true;
			
		}
		protected boolean inputHeapAsync() {
			if(getInPack(PackType.EVENT).isEmpty()) return false;
			for(ChainInConnector a : getInPack(PackType.EVENT))
				if(a.isNotEmpty())
					return true;
//			for(ListIterator<ChainInPath> itr = aInQueueHeap.listIterator(aInQueueHeap.size()-1); itr.hasPrevious();)
//				itr.previous().pop();
			return false;
		}
		public ArrayList<Object> inputPeek(PackType type) throws InterruptedException {
			return getInPack(type).inputPeek();
		}
		public ArrayList<Object> input(PackType type) throws InterruptedException {
			return getInPack(type).input();
		}
		public boolean isConnectedTo(IPiece cp) {
			return partner.isConnectedTo(cp);
		}
		public boolean isAppendedTo(ChainPiece cp, PackType pt) {
			return partner.isAppendedTo(cp, pt);
		}
		CountDownLatch lock = null;
		boolean recvUnerrorEvent() throws InterruptedException {
			lock = new CountDownLatch(1);
			lock.await();
			return true;
		}
		
		boolean sendUnerrorEvent() {
		if(lock != null)
				lock.countDown();
			return true;
		}
		
		public static class Partner implements Serializable {
			ConcurrentHashMap<IPiece, IPath> partner;
			Partner() {
				partner = new ConcurrentHashMap<IPiece, IPath>();
			}
			public ChainPiece.Partner setPartner(IPath o, IPiece cp) {
				partner.put(cp, o);
				return this;
			}
			public ChainPiece.Partner unsetPartner(IPiece cp) {
				partner.remove(cp);
				return this;
			}
//			public ChainPiece getPartner(ChainPathPair o) {
//				return partner.get(o);
//			}
			public boolean isConnectedTo(IPiece cp) {
				return partner.containsKey(cp);
			}
			public boolean isAppendedTo(ChainPiece cp, PackType pt) {
				IPath _path = null;
				if((_path = partner.get(cp)) != null)
					if(_path.getOutConnector().getPack() == cp.getOutPack(pt))
						return true;
				return false;
			}
			public Collection<IPath> getPaths() {
				return partner.values();
			}
			public void detachAll() {
				for(IPath pair : partner.values())
					pair.detach();
				partner.clear();
			}
			public IPath getPath(ChainPiece cp) {
				return partner.get(cp);
			}
			public Collection<IPiece> getPartners() {
				return partner.keySet();
			}
		}
	}