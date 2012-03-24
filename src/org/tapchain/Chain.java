package org.tapchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

import org.tapchain.AnimationChain.BasicPiece;
import org.tapchain.AnimationChain.ErrorHandler;
import org.tapchain.AnimationChainManager.LogHandler;
import org.tapchain.AnimationChainManager.StatusHandler;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.Chain.ChainPiece.PieceState;
import org.tapchain.ChainController.ControlCallback;

import android.util.Log;
import android.util.Pair;


@SuppressWarnings("serial")
public class Chain {
	static int n = 0;
	ConcurrentSkipListSet<ChainPiece> aFunc;
	ChainController ctrl;
	private ChainOperator list = null;
	int mode = 0;//0: Run-as-a-thread mode, 1: Automatic mode
	public static int THREAD_MODE = 0;
	public static int AUTO_MODE = 1;
	public boolean AUTO_END = true;
	LogHandler log = null;

	Chain(int _mode, int time) {
		aFunc = new ConcurrentSkipListSet<ChainPiece>();
		mode = _mode;
		ctrl = new ChainController(time);
		Set(null);
		list = new ChainOperator(this);
	}
	Chain(int _mode) {
		this(_mode, 20);
	}
	Chain() {
		this(AUTO_MODE, 20);
	}
	public Chain Set(final ControlCallback cb) {
//		if(mode == AUTO_MODE) {
			if(cb == null) {
				ctrl.Set(new ControlCallback() {
					public boolean impl() {
						Chain.this.notifyAllFunc();
						return false;
					}
				});
			} else {
				ctrl.Set(new ControlCallback() {
					public boolean impl() {
						cb.impl();
						Chain.this.notifyAllFunc();
						return false;
					}
				});
			}
//		} else {
//			if(cb == null) {
//				ctrl.Set(new ControlCallback() {
//					public boolean impl() {
//						Chain.this.notifyAllFunc();
//						return false;
////						return !aFunc.isEmpty();
//					}
//				});
//			} else {
//				ctrl.Set(new ControlCallback() {
//					public boolean impl() {
//						cb.impl();
//						Chain.this.notifyAllFunc();
//						return false;
////						return !aFunc.isEmpty();
//					}
//				});
//			}			
//		}
		return this;
	}
	
	public Chain setLog(LogHandler l) {
		log = l;
		return this;
	}
	
	public Chain setAutoEnd(boolean end) {
		AUTO_END = end;
		return this;
	}
	
	public Chain Kick() {
		ctrl.Kick();
		return this;
	}
	
	public enum Flex { FIXED, FLEX }

	public ChainPiece addPiece(ChainPiece cp) {
		cp.setParent(this);
		if(mode == AUTO_MODE) {
//			aFunc.add(cp);
			cp.start();
			Kick();
		}
		if(log != null)
			log.Log("Chain", String.format("RTN: ADD, CP: %s", cp.getClass().getCanonicalName()));
		return cp;
		
	}
	
	public boolean notifyAllFunc() {
		for (ChainPiece f : aFunc)
				f.signal();
		return true;
	}
	
	public ChainPiece _addPiece(ChainPiece cp) {
		aFunc.add(cp);
		return cp;
	}

	public ChainPiece removePiece(ChainPiece f) {
		f.end();
		if(log != null)
			log.Log("Chain", String.format("RTN: REM, CP: %s", f.getClass().getCanonicalName()));
		aFunc.remove(f);
		return f;
	}
	
	public ChainOperator getManager() {
		return list;
	}
	
	class ChainManager {
		List<ChainPiece> q = new ArrayList<ChainPiece>();
		List<ChainPiece> q2 = new ArrayList<ChainPiece>();
		Axon<String> revolver = new Toggle<String>();
		Chain _p = null;

		public ChainManager(Chain parent) {
			_p = parent;
		}

		boolean remove(ChainPiece ef) {
			return false;
		}

		public synchronized ChainPiece add(ChainPiece cp) {
			q.add(cp);
			Kick();
			return cp;
		}

//		public synchronized boolean revolve() {
//			if (revolver.size() == 0) {
//				return false;
//			}
//			try {
//					revolver.sync_pop();
//				} catch (Axon.AxonException e) {
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				return false;
//			}
//			save();
//			return true;
//		}

		public synchronized boolean save() {
			aFunc.addAll(q);
			for(ChainPiece cp: q)
				addPiece(cp);
			q.clear();
			return false;
		}

		public boolean isEmpty() {
			return q.isEmpty();
		}
	}
	
	public class ChainOperator extends ChainManager {
		public ChainOperator(Chain parent) {
			super(parent);
		}
		
		public synchronized void start() {
			if(mode == AUTO_MODE) return;
			for(ChainPiece cp : aFunc)
				cp.start();
			ctrl.Kick();
		}
		
		public synchronized void reset() {
			for(ChainPiece cp: aFunc)
				cp.restart();
		}
	}
	
	public static class ChainPiece implements PieceBody, Serializable, Tickable {
		public static ExecutorService threadExecutor = Executors.newCachedThreadPool();
		protected PieceHead fImpl;
//		protected ChainInPathPack aIn, aInHeap, aInEvent, aInFamily;
		protected ArrayList<ChainInPathPack> inPack = new ArrayList<ChainInPathPack>();
		protected ArrayList<ChainOutConnectorPack> outPack = new ArrayList<ChainOutConnectorPack>();
//		protected ChainOutPathPack aOut, aOutHeap, aOutFamily, aOutEvent;
		protected CountDownLatch signal = new CountDownLatch(0);
		private Boolean _chainlive = false, controlled_by_ac = true;
//		Thread _th = null;
		private String name = null;
		Future f = null;
		Chain _root_chain = null;
		ChainPiece cp_reactor = this;
		int mynum = 0;
		protected ChainPartner partner = new ChainPartner();
		private ErrorHandler _error = null;
		private StatusHandler _statush = null;
		enum PackType {
			PASSTHRU, HEAP, FAMILY, EVENT
		}
		enum PieceState {
			NOTSTARTED, STARTED, RUNNING, END, ERROR
		}
		PieceState status = PieceState.NOTSTARTED, status_bak = PieceState.NOTSTARTED;

		ChainPiece() {
			this(new PieceHead() {
				@Override
				public boolean pieceImpl(PieceBody f) throws InterruptedException {
					return false;
				}

				@Override
				public boolean pieceReset(PieceBody f) {
					return false;
				}
			});
		}
		ChainPiece(PieceHead tmpFImpl) {
			mynum = n++;
			fImpl = tmpFImpl;
			addNewInPack().setInType(InputType.ALL);//PASSTHRU
			addNewInPack().setInType(InputType.FIRST);//HEAP
			addNewInPack().setInType(InputType.ALL);//FAMILY
			addNewInPack().setInType(InputType.ALL);//.setUserPathListener(reset);//EVENT
			
			addNewOutPack();//PASSTHRU
			addNewOutPack();//HEAP
			addNewOutPack().setDefaultType(OutType.HIPPO);//FAMILY
			addNewOutPack();//EVENT
			
//			signal = new Toggle<String>();
//			playing.sync_push(true);
			init();
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
		
		protected ChainPiece setFunc(PieceHead f) {
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
			if(logFlag && _root_chain != null && _root_chain.log != null)
				_root_chain.log.Log(flg, String.format("RTN: %s, CP: %s", obj, getName()));
			return obj;
		}
		
		public void init() {
		}
		public ChainPiece restart() {
//			for(ChainOutPath o: getOutPack(PackType.PASSTHRU).array)
//				try {
//					o.getQueue().reset();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			end();
			try {
				waitEnd();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			signal = new CountDownLatch(1);
			start();
			return this;
		}
		
		public ChainPiece signal() {
			signal.countDown();
			return this;
		}
		
		public boolean next() throws InterruptedException {
			signal.await();
			signal = new CountDownLatch(1);
			return true;
	}
	
		public ChainPiece setName(String name) {
			this.name = name;
			return this;
		}
		public String getName() {
			if(name != null)
				return name;
			return getClass().getName();
		}

		
		
//		public ChainPiece pushEndSignal() {
//			try {
//				signalQueue.sync_push(null);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			_th.interrupt();
//			return this;
//		}
//		
		public void start() {
			final ChainPiece cp_this = this;
			if(_root_chain == null) return;
			final boolean autoend = _root_chain.AUTO_END;
			if(isAlive()) return;
//			_th = new Thread() {
			f = threadExecutor.submit(new Runnable() {
				public void run() {
					changeState(PieceState.STARTED);
					try {
						cp_this.onInitialize();
					} catch (ChainException e) {
						e.printStackTrace();
						cp_this.onError(e);
					}
					int i = 0;
					boolean rtn = true;
					synchronized(_chainlive) {
						_chainlive = true;
					}
					fImpl.pieceReset(cp_reactor);
					changeState(PieceState.RUNNING);
					main_loop: while (rtn) {
						try {
							__exec(String.format("ID:%d Main[#0 ->SIG]", mynum), "ChainPiece#impl");
							if(controlled_by_ac && !cp_this.next()) {
								rtn = false;
								break main_loop;
							}
							__exec(String.format("ID:%d Main[#1 SIG->FUNC]", mynum), "ChainPiece#impl");
							if(!_doAndLoopInError(fImpl, cp_reactor)) {
								rtn = false;
								break main_loop;
							}
							__exec(String.format("ID:%d Main[#2 FUNC->OK]", mynum), "ChainPiece#impl");
							tick();
						} catch (InterruptedException e) {
							if(!_chainlive)
								break main_loop;
							cp_this.interrupted();
							__exec(String.format("ID:%d Main[#-1 INTERRUPTED]", mynum), "ChainPiece#impl");
							fImpl.pieceReset(cp_reactor);
						}
					}
					try {
						__exec(String.format("ID:%d Main[#-2 END]", mynum), "ChainPiece#impl");
						cp_this.onTerminate();
					} catch (ChainException e) {
						cp_this.onError(e);
					}
					if(autoend)  {
						 _root_chain.removePiece(cp_this);
						__exec(String.format("ID:%d Main[#-3 AUTOREMOVE]", mynum), "ChainPiece#impl");
					}
					changeState(PieceState.END);
				}
			});
//			};
//			_th.start();
		}
		
		protected ChainPiece end() {
			synchronized(_chainlive) {
				_chainlive = false;
			}
			interrupt();
			return this;
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
		
		private boolean _doAndLoopInError(PieceHead head, ChainPiece cp) throws InterruptedException {
			while(true) {
				try {
					return head.pieceImpl(cp);
				} catch (ChainException e) {
					onError(e);
					switch(e.loop){
					case INTERRUPT:
						throw new InterruptedException();
					case LOCK:
						recvUnerrorEvent();
						onUnerror(e);
					default:
					}
				}
			}
		}
		protected ChainPiece setError(ErrorHandler er) {
			_error = er;
			return this;
		}
		
		protected ChainPiece setStatusHandler(StatusHandler st) {
			_statush = st;
			return this;
		}

		protected boolean onError(ChainException e) {
			if (_error != null)
				_error.ErrorHandler(this, e);
			changeState(PieceState.ERROR);
			return true;
		}
		
		protected boolean onUnerror(ChainException e) {
			if(_error != null)
				_error.ErrorCanceller(this, e);
			restoreState();
			return true;
		}
		
		protected boolean changeState(PieceState state) {
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
			_root_chain.Kick();
			return true;
		}
		
		protected PieceState restoreState() {
			status = status_bak;
			if(_statush != null)
				_statush.getStateAndSetView(status.ordinal());
			return status;
		}
		
		public boolean isAlive() {
//			return _th != null && _th.isAlive();
			return status!=PieceState.NOTSTARTED && status!=PieceState.END;
		}
		public void interrupt() {
				f.cancel(true);
		}
		public void interrupted() {
			return;
		}
		protected void onInitialize() throws ChainException {
			signal = new CountDownLatch(1);
			return;
		}
		protected void onTerminate() throws ChainException {
			return;
		}
		
		
		//2.PathPack setting functions
		protected ChainOutConnectorPack getOutPack(PackType stack) {
//			ChainOutPathPack pack = null;
//			switch(stack) {
//			case PASSTHRU:
//				pack = aOut;
//				break;
//			case HEAP:
//				pack = aOutHeap;
//				break;
//			case FAMILY:
//				pack = aOutFamily;
//				break;
//			case EVENT:
//				pack = aOutEvent;
//				break;
//			}
//			return pack;
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
		public ChainPiece setOutPackType(PackType pack, OutType type) {
			getOutPack(pack).setDefaultType(type);
			return this;
		}
		
		public ChainPiece setInPackType(PackType pack, InputType type) {
			getInPack(pack).setInType(type);
			return this;
		}
		protected ChainInConnector addInPath(Class<?> c, PackType stack) {
			return getInPack(stack).addNewPath(c);
		}
		protected ChainOutConnector addOutPath(Class<?> c, OutType io, PackType stack) {
			ChainOutConnector rtn = getOutPack(stack).addNewPath(c, io);
			return rtn;
		}
		public void detachAll() {
			partner.detachAll();
			return;
		}
		public ChainPiece detachInPack(PackType packtype) {
			getInPack(packtype).detachAll();
			return this;
		}
		public ChainPiece detachOutPack(PackType packtype) {
			getOutPack(packtype).detachAll();
			return this;
		}
		public Collection<ChainPath> getLinks() {
			return partner.getPaths();
		}
		public Collection<ChainPiece> getPartners() {
			return partner.getPartners();
		}
		public ChainPath detach(ChainPiece cp) {
			return partner.getPath(cp).detach();
		}
		public boolean hasInPath(PackType packtype) {
			return getInPack(packtype).hasPath();
		}

		public boolean hasOutPath(PackType packtype) {
			return getOutPack(packtype).hasPath();
		}

		
		//3.Input/Output functions
//		protected boolean ioCheck() {
//			boolean rtn = true;
//			for(ChainInPath a: getInPack(PackType.PASSTHRU).array) {
//				if(a.getQueue().isClosed())
//					rtn = false;
//			}
//			for(ChainOutPath a: getOutPack(PackType.PASSTHRU).array) {
//				if(a.getQueue().isClosed())
//					rtn = false;
//			}
//			return rtn;
//		}
		public <T> T getCache(ChainInConnector i) throws InterruptedException {
			if(getInPack(PackType.PASSTHRU).array.contains(i)) {
				return i.<T>getCache();
			}
			else if(getInPack(PackType.EVENT).array.contains(i)) {
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
			Log.e("AllReset", "Called");
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
			if(getOutPack(PackType.PASSTHRU).array.isEmpty()) {
			} else {
				getOutPack(PackType.PASSTHRU).array.peek();
			}
			return tmp;
		}
		
		//4.External functions related with connections(called by other thread)
		public Pair<ChainPiece, ChainPath> appendTo(PackType stack, ChainPiece target, PackType stack_target) throws ChainException {
			//if user assigns PREV FUNCTION
			if(target == null) {
				throw new ChainException(this, "appendTo()/Invalid Target/Null");
			} else if(this == target) {
				throw new ChainException(this, "appendTo()/Invalid Target/Same as Successor");
			}
			return null;
		}
		
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls, OutType type, PackType stack, ChainPiece from) throws ChainException {
			return null;
		}
		
		public ChainPiece detachFrom(ChainPiece cp) {
			partner.unsetPartner(cp);
			return this;
		}
		
		public ChainPiece detached(ChainPiece cp) {
			partner.unsetPartner(cp);
			return this;
		}
		
		public boolean postAppend() {
			return sendUnerrorEvent();
		}
		
//		boolean waitResume() throws InterruptedException {
//			return playing.sync_pop();
//		}

//		protected ChainPiece suspend() {
//			playing.reset();
//			return this;
//		}
//		
//		protected ChainPiece resume() {
//			playing.sync_push(true);
//			return this;
//		}
//		
		public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
			getOutPack(PackType.PASSTHRU).waitOutput(rtn);
		}
		public void waitOutputAll(Object rtn) throws InterruptedException {
			getOutPack(PackType.PASSTHRU).waitOutputAll(rtn);
		}
		
//		boolean outputType = false;//true: wait for at least one output, false: no wait
		public boolean clearInputHeap() {
			if(getInPack(PackType.HEAP).array.isEmpty()) return false;
//			for(ChainInPath a : getInPack(PackType.HEAP).array)
//				a.reset();
			getInPack(PackType.HEAP).reset();
			return true;
		}
		
		protected boolean inputHeapAsync() {
			if(getInPack(PackType.EVENT).array.isEmpty()) return false;
			for(ChainInConnector a : getInPack(PackType.EVENT).array)
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
		public boolean isConnectedTo(ChainPiece cp) {
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
		
		public static class ChainPartner implements Serializable {
			ConcurrentHashMap<ChainPiece, ChainPath> partner;
			ChainPartner() {
				partner = new ConcurrentHashMap<ChainPiece, ChainPath>();
			}
			public ChainPartner setPartner(ChainPath o, ChainPiece cp) {
				partner.put(cp, o);
				return this;
			}
			public ChainPartner unsetPartner(ChainPiece cp) {
				partner.remove(cp);
				return this;
			}
//			public ChainPiece getPartner(ChainPathPair o) {
//				return partner.get(o);
//			}
			public boolean isConnectedTo(ChainPiece cp) {
				return partner.containsKey(cp);
			}
			public boolean isAppendedTo(ChainPiece cp, PackType pt) {
				ChainPath _path = null;
				if((_path = partner.get(cp)) != null)
					if(_path.getOutConnector().pack == cp.getOutPack(pt))
						return true;
				return false;
			}
			public Collection<ChainPath> getPaths() {
				return partner.values();
			}
			public void detachAll() {
				for(ChainPath pair : partner.values())
					pair.detach();
				partner.clear();
			}
			public ChainPath getPath(ChainPiece cp) {
				return partner.get(cp);
			}
			public Collection<ChainPiece> getPartners() {
				return partner.keySet();
			}
		}
		
		
		public static class ChainPathPack implements Serializable {
			PackType ptype = PackType.EVENT;
			ChainPathPack() {
			}
			public PackType getPtype() {
				return ptype;
			}
			public void setPtype(PackType ptype) {
				this.ptype = ptype;
			}
			public void detachAll() {
			}
		}
		
		
		public static class ChainOutConnectorPack extends ChainPathPack {
			Queue<ChainOutConnector> array;
			SyncQueue<Object> queue;
			ChainPiece parent;
			Boolean lock = false;
			OutType defaultType = OutType.NORMAL;
			ChainOutConnectorPack(ChainPiece _parent) {
				array = new ConcurrentLinkedQueue<ChainOutConnector>();
				queue = new SyncQueue<Object>();
				parent = _parent;
			}
			public boolean hasPath() {
				return !array.isEmpty();
			}
			protected ChainOutConnectorPack setDefaultType(OutType type) {
				defaultType = type;
				return this;
			}
			public ChainOutConnector addNewPath(Class<?> c, OutType type) {
				ChainOutConnector rtn = new ChainOutConnector(parent, c, this, (type!=null)?type:defaultType);
				parent.__exec(String.format("NewPath = %s", rtn.type), "COPP#addNewPath");
				synchronized(array) {
					array.add(rtn);
					array.notifyAll();
				}
				for(Iterator<Object> it = queue.iterator(); it.hasNext(); ) {
					try {
						rtn.sync_push(it.next());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return rtn;
			}
			public void removePath(ChainOutConnector path) {
				synchronized(array) {
					array.remove(path);
					array.notifyAll();
				}
			}
			public void detachAll() {
				for(ChainOutConnector outpath : array) {
					outpath.detach();
				}
			}
			public boolean clear() {
				for(ChainOutConnector o: array) {
					o.reset();
				}
				queue.clear();
				return true;
			}
			public boolean send_reset() {
				boolean rtn = false;
				for(ChainOutConnector o: array) {
					rtn |= o.send_reset();
				}
				return rtn;
			}
			public synchronized void Lock(boolean _lock) {
				synchronized(lock) {
					lock = _lock;
						lock.notifyAll();
				}
				return;
			}
			public boolean outputAllSimple(Object obj) throws InterruptedException {
				synchronized(lock) {
					while(lock) lock.wait();
				}
				ArrayList<Object> rtn = new ArrayList<Object>();
				int size = array.size();
				for(int i = 0; i < size; i++)
					rtn.add(obj);
				if(queue.size()==10)
					try {
						queue.sync_pop();
					} catch (Axon.AxonException e) {
						return false;
					}
				if(obj != null)
					queue.sync_push(obj);
				return outputAll(rtn);
			}
			public boolean outputAll(ArrayList<?> ar) throws InterruptedException {
				if(array.isEmpty()) return false;
//				if(array.size() > ar.size()) return false;
				int i = 0;
				boolean rtn = false;
				for(ChainOutConnector a : array)
					rtn |= a.async_push(ar.get(i<ar.size()-1?i++:i));
//				ListIterator<?> itr2 = ar.listIterator(ar.size());
//				for(ListIterator<ChainOutPath> itr = aOutQueue.listIterator(aOutQueue.size()); itr.hasPrevious();)
//					itr.previous().push(itr2.previous());
				return rtn;
			}
			public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
				while(!outputAll(rtn)) {
					synchronized(array) {
						array.wait();
					}
				}
			}
			public void waitOutputAll(Object rtn) throws InterruptedException {
				while(!outputAllSimple(rtn)) {
					synchronized(array) {
						array.wait();
					}
				}
			}
		}
		
		
		
		static enum InputType {
			ALL, FIRST, COUNT
		}
		public static class ChainInPathPack extends ChainPathPack {
			InputType inputType;
			PathListener listen, userlisten, resetHandler;
			ChainPiece parent;
			Queue<ChainInConnector> array;
			SyncQueue<ChainConnector> order_first;
			Iterator<ChainInConnector> now_count = null;
			public ChainInPathPack(ChainPiece _parent) {
				array = new ConcurrentLinkedQueue<ChainInConnector>();
				order_first = new SyncQueue<ChainConnector>();
				inputType = InputType.ALL;
				listen = new PathListener();
				userlisten = new PathListener();
				resetHandler = new PathListener();
				parent = _parent;
			}
			public boolean hasPath() {
				return !array.isEmpty();
			}
			public ChainInConnector addNewPath(Class<?> c)  {
				ChainInConnector rtn = new ChainInConnector(parent, c, this);
				rtn.setListener(new PathListener() {
					@Override
					public void OnPushed(ChainConnector p) throws InterruptedException {
						super.OnPushed(p);
						listen.OnPushed(p);
						userlisten.OnPushed(p);
					}
				});
				rtn.setResetHandler(resetHandler);
				synchronized(array) {
					array.add(rtn);
					array.notifyAll();
				}
				return rtn;
			}
			public void removePath(ChainInConnector path) {
				synchronized(array) {
					array.remove(path);
					array.notifyAll();
				}
			}
			public void detachAll() {
				for(ChainInConnector outpath : array) {
					outpath.detach();
				}
			}
			public ArrayList<Object> inputPeek() throws InterruptedException {
				ArrayList<Object> rtn = null;
				switch(inputType) {
				case ALL:
					rtn = _inputPeekAll();
					break;
				case FIRST:
					rtn = _inputPeekFirst();
					break;
				case COUNT:
				}
				return rtn;
			}
			private ArrayList<Object> _inputPeekAll() throws InterruptedException {
				if(array.isEmpty()) return null;
				ArrayList<Object> rtn = new ArrayList<Object>();
				for(ChainInConnector a : array)
					rtn.add(a.sync_peek());
				return rtn;
			}
			private ArrayList<Object> _inputPeekFirst() throws InterruptedException {
				ChainConnector p = order_first.sync_peek();
				ArrayList<Object> rtn = new ArrayList<Object>();
				rtn.add(p.sync_peek());
				Log.w("INPUT", p.parentPiece.getName());
				return rtn;
			}
			public ArrayList<Object> input() throws InterruptedException {
				ArrayList<Object> rtn = null;
				parent.__exec("WAITING","ChainPiece#input@start");
				switch(inputType) {
				case ALL:
					rtn = _inputAll();
					break;
				case FIRST:
					rtn = _inputFirst();
					break;
				case COUNT:
					rtn = _inputCount();
					parent.__exec("inputed COUNT","ChainPiece#input");
					break;
				}
				parent.__exec("INPUTED","ChainPiece#input@end");
				return rtn;
			}
			private ArrayList<Object> _inputAll() throws InterruptedException {
				if(array.isEmpty()) return null;
				ArrayList<Object> rtn = new ArrayList<Object>();
				for(ChainInConnector a : array)
					rtn.add(a.sync_pop());
				return rtn;
			}
			
			private ArrayList<Object> _inputFirst() throws InterruptedException {
				SyncQueue<ChainConnector> _pushedPath = order_first;
				ChainConnector p;
				try {
					p = _pushedPath.sync_pop();
				} catch (Axon.AxonException e) {
					return null;
				}
				ArrayList<Object> rtn = new ArrayList<Object>();
				rtn.add(p.sync_pop());
				parent.__exec(String.format("outputType = %s",p.type), "ChainPiece#_inputFirst");
				if(p.type == OutType.HIPPO) {
					order_first.sync_push(p);
				}
				return rtn;
			}
			
//			public Object inputHead() throws InterruptedException {
//				ChainInPath i = array.peek();
//				while (i == null)
//					synchronized (array) {
//						array.wait();
//						i = array.peek();
//					}
//				return input().get(0);
//			}

			private ArrayList<Object> _inputCount() throws InterruptedException {
				if(array.isEmpty()) return null;
				ArrayList<Object> rtn = new ArrayList<Object>();
				if(now_count == null || !now_count.hasNext()) {
					Log.w("Chain_InputCount", "Cleared");
					now_count = array.iterator();
				}
				rtn.add(now_count.next().sync_pop());
//				reset();
				return rtn;
			}
			
			public ChainInPathPack setInType(InputType type) {
				order_first = new SyncQueue<ChainConnector>();
				inputType = type;
				PathListener tmpListener = null;
				switch(type) {
				case ALL:
				case COUNT:
					//cancel TYPE[INPUT FIRST]
					tmpListener = new PathListener();
					break;
				case FIRST:
					//prepare TYPE[INPUT FIRST]
					tmpListener = new PathListener() {
						@Override
						public void OnPushed(ChainConnector p) throws InterruptedException {
							super.OnPushed(p);
							order_first.sync_push(p);
						}
					};
					break;
				}
				_setPathListener(tmpListener);
				return this;
			}
			ChainInPathPack _setPathListener(PathListener _listen) {
				listen = _listen;
				for(ChainInConnector a: array)
					a.setListener(new PathListener() {
						@Override
						public void OnPushed(ChainConnector p) throws InterruptedException {
							super.OnPushed(p);
							listen.OnPushed(p);
							userlisten.OnPushed(p);
						}
					});
				return this;
			}
			public ChainInPathPack setResetHandler(PathListener _reset) {
				resetHandler = _reset;
				for(ChainInConnector a: array)
					a.setListener(_reset);
				return this;
			}
			public ChainInPathPack setUserPathListener(PathListener _listen) {
				userlisten = _listen;
				for(ChainInConnector a: array)
					a.setListener(new PathListener() {
						@Override
						public void OnPushed(ChainConnector p) throws InterruptedException {
							super.OnPushed(p);
							listen.OnPushed(p);
							userlisten.OnPushed(p);
						}
					});
				return this;
			}
			public void reset() {
				for(ChainInConnector o: array) {
					o.reset();
				}
			}

		}
	};
	public static class FlexChainPiece extends ChainPiece {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		FlexChainPiece() {
			super();
		}
		FlexChainPiece(PieceHead fImpl) {
			super(fImpl);
		}
		public Pair<ChainPiece, ChainPath> appendTo(PackType stack, ChainPiece cp, Class<?> cls, PackType stack_target) throws ChainException {
			super.appendTo(stack,cp,stack_target);
			ChainInConnector i = addInPath(cls, stack);
			Pair<ChainPiece, ChainOutConnector> o = cp.appended(cls, null, stack_target, this);
			if(i.connect(o.second)) {
				ChainPath p =  new ChainPath(o.first, this, o.second, i);
				return new Pair<ChainPiece, ChainPath>(o.first,p);
			}
			return null;
		}
		@Override
		public Pair<ChainPiece, ChainPath> appendTo(PackType stack, ChainPiece cp, PackType stack_target) throws ChainException {
			return appendTo(stack, cp, ChainPiece.class, stack_target);
		}
		@Override
		public Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls, OutType type, PackType stack_target, ChainPiece from) throws ChainException {
			ChainOutConnector o = addOutPath(cls, type, stack_target);
//			partner.setPartner(o, from);
			return new Pair<ChainPiece, ChainOutConnector>(this, o);
		}
		@Override
		public FlexChainPiece detachFrom(ChainPiece cp) {
			super.detachFrom(cp);
			return this;
		}
		
		@Override
		public FlexChainPiece detached(ChainPiece cp) {
			super.detached(cp);
			return this;
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
		FixedChainPiece(PieceHead fImpl) {
			super(fImpl);
		}
		@Override
		public Pair<ChainPiece, ChainPath> appendTo(PackType stack, ChainPiece cp, PackType stack_target) throws ChainException {
			super.appendTo(stack, cp, stack_target);
			for(ChainInConnector i : getInPack(PackType.PASSTHRU).array) {
				Pair<ChainPiece, ChainOutConnector> o = cp.appended(i.class_name, null, stack_target, this);
				if(i.connect(o.second)) {
//					used = !hasAnyUnusedConnect();
					return new Pair<ChainPiece, ChainPath>(o.first, new ChainPath(o.first, this, o.second, i));
				}
			}
			return null;
		}
		protected Pair<ChainPiece, ChainOutConnector> appended(Class<?> cls) throws ChainException {
			for(ChainOutConnector o : getOutPack(PackType.PASSTHRU).array)
				if(o.class_name == cls)
					return new Pair<ChainPiece, ChainOutConnector>(this, o);
			return null;
		}
		
	}
	enum OutType { NORMAL, HIPPO, SYNC, TOGGLE }
	public static class ChainConnector implements Serializable {
		ChainPiece parentPiece = null;
		Class<?> class_name = null;
		boolean used = false;
		ChainConnector partner = null;
		int order = 0;
		Hippo<Axon<?>> QueueImpl = new Hippo<Axon<?>>();
		OutType type = OutType.NORMAL;
		PathListener listen = null, resetHandler = null;
		boolean end = false;
		ChainPath parentPath = null;
		
		public ChainConnector() {
		}
		public ChainConnector(ChainPiece parent_, Class<?> c) {
			this();
			parentPiece = parent_;
			class_name = c;
		}
		protected ChainConnector setParentPath(ChainPath _path) {
			parentPath = _path;
			return this;
		}
		public void detach() {
			if(parentPath == null)
				return;
			parentPath.detach();
		}
		public ChainPiece getParent() {
			return parentPiece;
		}
		public ChainConnector setUsed(boolean b) {
			used = b;
			return this;
		}
		public ChainConnector setPartner(ChainConnector i) {
			partner = i;
			return this;
		}
		public ChainConnector setListener(PathListener _listen) {
			listen = _listen;
			return this;
		}
		public ChainConnector setResetHandler(PathListener _reset) {
			resetHandler = _reset;
			return this;
		}
		public boolean isConnected() {
			return used;
		}
		public /*synchronized*/ChainConnector setQueueImpl(Axon<?> q) {
			QueueImpl.sync_push(q);
//			notifyAll();
			return this;
		}
		public Axon<?> getQueue() throws InterruptedException {
			return QueueImpl.sync_pop();
		}
		
		@SuppressWarnings("unchecked")
		public /*synchronized*/ <T> T sync_pop() throws InterruptedException {
//			while(QueueImpl==null) {
//				wait();
//			}
			try {
				T rtn = ((Axon<Packet<T>>)getQueue()).sync_pop().getObject();
				if(parentPath != null)
					parentPath.tick();
				return rtn;
			} catch (Axon.AxonException e) {
				return null;
			}
		}
		
		@SuppressWarnings("unchecked")
		public/* synchronized*/ <T> T sync_peek() throws InterruptedException {
//			while(QueueImpl==null) {
//				wait();
//			}
			return ((Axon<Packet<T>>)getQueue()).sync_peek().getObject();
		}
		
		@SuppressWarnings("unchecked")
		public <T> boolean sync_push(T obj) throws InterruptedException {
			boolean rtn = ((Axon<Packet<T>>)getQueue()).sync_push(new Packet<T>(obj, null));
			if(rtn && listen != null)
				listen.OnPushed(this);
			return rtn;
		}
		
		public <T> boolean async_push(T obj) throws InterruptedException {
			@SuppressWarnings("unchecked")
			boolean rtn = ((Axon<Packet<T>>)getQueue()).async_push(new Packet<T>(obj, null));
			if(rtn && listen != null)
				listen.OnPushed(this);
			return rtn;
		}
		
		public boolean send_reset() {
			boolean rtn = resetHandler != null;
			if(rtn) {
				QueueImpl.reset();
				try {
					resetHandler.OnPushed(this);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return rtn;
		}
		
		public boolean isNotEmpty() {
			return !(QueueImpl).isEmpty();
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getCache() throws InterruptedException {
//			while(QueueImpl==null) {
//				wait();
//			}
			return ((Axon<Packet<T>>)getQueue()).getCache().getObject();
		}
		
		@SuppressWarnings("unchecked")
		public ChainPiece getCacheSource() throws InterruptedException {
//			while(QueueImpl==null) {
//				wait();
//			}
			return ((Axon<Packet<?>>)getQueue()).getCache().getSource();
		}
		
		public void reset() {
			QueueImpl.reset();
			return;
		}
		
	}
	
	public static class ChainOutConnector extends ChainConnector {
		ChainPiece.ChainOutConnectorPack pack = null;
		public ChainOutConnector(ChainPiece parent, Class<?> c, ChainPiece.ChainOutConnectorPack _pack, OutType _type) {
			super(parent, c);
			pack = _pack;
			setType(_type);
			Axon<?> q;
			if(_type == OutType.NORMAL) {
				if(c == String.class) {
					q = new SyncQueue<Packet<String>>();
				} else if(c == Integer.class) {
					q = new SyncQueue<Packet<Integer>>();
				} else {
					q = new SyncQueue<Packet<String>>();
				}
			} else if(_type == OutType.HIPPO) {
				if(c == String.class) {
					q = new Hippo<Packet<String>>();
				} else if(c == Integer.class) {
					q = new Hippo<Packet<Integer>>();
				} else {
					q = new Hippo<Packet<String>>();
				}
			} else if(_type == OutType.TOGGLE) {
				if(c == String.class) {
					q = new Toggle<Packet<String>>();
				} else if(c == Integer.class) {
					q = new Toggle<Packet<Integer>>();
				} else {
					q = new Toggle<Packet<String>>();
				}
			} else {
				if(c == String.class) {
					q = new SyncObject<Packet<String>>();
				} else if(c == Integer.class) {
					q = new SyncObject<Packet<Integer>>();
				} else {
					q = new SyncObject<Packet<String>>();
				}
			}
			setQueueImpl(q);
		}
/*		public Axon<?> compile(Chain c, Axon<?> q) {
			super.setQueueImpl(q);
			return compile(c);
		}
*/
		public ChainOutConnector setType(OutType _type) {
			type = _type;
			return this;
		}
/*		public Axon<?> compile(Chain c) {
			if(partner == null) {
				return null;
			}
			return QueueImpl;
		}
*/
		public ChainInConnector getPartner() {
			return (ChainInConnector) partner;
		}
		public void end() {
			pack.removePath(this);
			try {
				getQueue().CloseForced();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class ChainInConnector extends ChainConnector {
		ChainPiece.ChainInPathPack pack = null;
		public ChainInConnector(ChainPiece cModel, Class<?> c, ChainPiece.ChainInPathPack _pack) {
			super(cModel, c);
			pack = _pack;
		}
/*		public Axon<?> compile() {
			return QueueImpl;
		}
*/		public boolean canPair(ChainOutConnector o) {
			return !this.used && !o.used && this.class_name == o.class_name;
		}
		public ChainOutConnector getPartner() {
			return (ChainOutConnector) partner;
		}
		public boolean connect(ChainOutConnector o) throws ChainException {
			if(!canPair(o)) { return false; }
			try {
				setQueueImpl(o.getQueue());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			o.setPartner(this)
				.setUsed(true);
			this.setPartner(o)
				.setUsed(true);
			o.setListener(listen);
			o.setResetHandler(resetHandler);
			try {
				int size = o.getQueue().size();
//				Log.w("ChainConnect", Integer.toString(size));
				for(int i = 0; i < size; i++ ) {
						listen.OnPushed(o);
				}
			} catch (InterruptedException e) {
				throw new ChainException(this, "Connect was cancelled");
			}
			return true;
		}
		public void end() {
			pack.removePath(this);
		}
	}
	
	public static class ChainPath implements Tickable {
		ChainPiece _cp_start, _cp_end;
		ChainOutConnector _out;
		ChainInConnector _in;
		private StatusHandler h = null;
		ChainPath(ChainPiece cp_start, ChainPiece cp_end, ChainOutConnector out, ChainInConnector in) {
			_out = out;
			_in = in;
			_cp_start = cp_start;
			_cp_end = cp_end;
			_out.setParentPath(this);
			_in.setParentPath(this);
			_cp_end.partner.setPartner(this, _cp_start);
			_cp_start.partner.setPartner(this, _cp_end);
	}
		public ChainOutConnector getOutConnector() {
			return _out;
		}
		public ChainInConnector getInConnector() {
			return _in;
		}
		public ChainPiece get_cp_end() {
			return _cp_end;
		}
		public ChainPiece get_cp_start() {
			return _cp_start;
		}
		public ChainPath detach() {
			getOutConnector().end();
			getInConnector().end();
//			_cp_start.partner.unsetPartner(_cp_end);
//			_cp_end.partner.unsetPartner(_cp_start);
			_cp_start.detached(_cp_end);
			_cp_end.detachFrom(_cp_start);
			return this;
		}
		@Override
		public boolean tick() {
			if(h != null)
				h.tickView();
			return false;
		}
		public ChainPath setStatusHandler(StatusHandler h) {
			this.h = h;
			return this;
		}
	}

	public interface PieceHead {
		abstract boolean pieceImpl(PieceBody f) throws InterruptedException, ChainException;
		abstract boolean pieceReset(PieceBody f);
	}
	
	public interface PieceBody {
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
		ChainException(ChainPiece cp, String str, LoopableError _loop) {
			super("ChainException: "+str);
			err = str;
			location = cp.getName();
			loop = _loop;
		}
		ChainException(ChainPiece cp, String str) {
			this(cp, str, LoopableError.LOOPABLE);
		}
		ChainException(ChainConnector cp, String str) {
			super("ChainException: "+str);
			err = str;
			location = "Path";
		}
		ChainException(Manager<?> cp, String str) {
			super("ChainException: "+str);
			err = str;
			location = "Manager";
		}
		ChainException(ChainPiece cp, String str, Throwable throwable) {
			super("ChainException: "+str, throwable);
			err = str;
			location = cp.getName();
		}
		
	}
	public enum LoopableError {
		LOOPABLE, INTERRUPT, LOCK
	}
	public static class PathListener implements Serializable {
		public void OnPushed(ChainConnector p) throws InterruptedException {
		}
	}
	
	public static class Packet<T> {
		T obj = null;
		ChainPiece source = null;
		Packet(T _obj, ChainPiece _source) {
			obj = _obj;
			source = _source;
		}
		public T getObject() {
			return obj;
		}
		public ChainPiece getSource() {
			return source;
		}
	}
	
	public interface Tickable {
		boolean tick();
	}
}

