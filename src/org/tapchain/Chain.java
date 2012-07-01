package org.tapchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import org.tapchain.ActorChain.IErrorHandler;
import org.tapchain.ActorManager.ILogHandler;
import org.tapchain.ActorManager.StatusHandler;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.ChainController.IControlCallback;



@SuppressWarnings("serial")
public class Chain {
	static int n = 0;
	ConcurrentSkipListSet<IPiece> aFunc;
	ChainController ctrl;
	private ChainPieceOperator operator = null;
	int mode = 0;//0: Run-as-a-thread mode, 1: Automatic mode
	public static int THREAD_MODE = 0;
	public static int AUTO_MODE = 1;
	public boolean AUTO_END = true;
	ILogHandler log = null;

	Chain(int _mode, int time) {
		aFunc = new ConcurrentSkipListSet<IPiece>();
		mode = _mode;
		ctrl = new ChainController(time);
		Set(null);
		operator = new ChainPieceOperator(this);
	}
	Chain() {
		this(AUTO_MODE, 30);
	}
	public Chain Set(final IControlCallback cb) {
//		if(mode == AUTO_MODE) {
			if(cb == null) {
				ctrl.Set(new IControlCallback() {
					public boolean callback() {
						Chain.this.notifyAllFunc();
						return false;
					}
				});
			} else {
				ctrl.Set(new IControlCallback() {
					public boolean callback() {
						cb.callback();
						Chain.this.notifyAllFunc();
						return false;
					}
				});
			}
		return this;
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
	
	public enum Flex { FIXED, FLEX }

	public IPiece addPiece(IPiece cp) {
		if(cp instanceof ChainPiece)
			((ChainPiece)cp).setParent(this);
		if(mode == AUTO_MODE) {
//			aFunc.add(cp);
			if(cp instanceof ChainPiece) {
				((ChainPiece)cp).start();
			}
			kick();
		}
		if(log != null)
			log.log("Chain", String.format("RTN: ADD, CP: %s", cp.getName()));
		return cp;
		
	}
	
	public boolean notifyAllFunc() {
//		for (IPiece f : aFunc)
		if(aFunc.isEmpty())
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
		if(log != null)
			log.log("Chain", String.format("RTN: REM, CP: %s", bp.getName()));
		aFunc.remove(bp);
		return bp;
	}
	
	public ChainPieceOperator getOperator() {
		return operator;
	}
	
	class ChainOperator {
		List<IPiece> q = new ArrayList<IPiece>();
		List<IPiece> q2 = new ArrayList<IPiece>();
		IAxon<String> revolver = new Toggle<String>();
		Chain _p = null;

		public ChainOperator(Chain parent) {
			_p = parent;
		}

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
			for(IPiece cp: q)
				addPiece(cp);
			q = new ArrayList<IPiece>();
			return q;
		}

		public boolean isEmpty() {
			return q.isEmpty();
		}
	}
	
	public class ChainPieceOperator extends ChainOperator {
		public ChainPieceOperator(Chain parent) {
			super(parent);
		}
		
		public synchronized void start() {
			if(mode == AUTO_MODE) return;
			for(IPiece cp : aFunc)
				if(cp instanceof ChainPiece) {
					((ChainPiece)cp).start();
				}
			ctrl.kick();
		}
		
		public synchronized void reset() {
			for(IPiece cp: aFunc)
				if(cp instanceof ChainPiece) {
					((ChainPiece)cp).restart();
				}
		}
	}
	public static class Connection<T> {
		IPiece piece;
		T t;
		public Connection(IPiece piece, T t) {
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
	public static class ConnectionIO extends Connection<ChainPath> {
		public ConnectionIO(IPiece piece, ChainPath t) {
			super(piece, t);
		}
	}
	public static class ConnectionO extends Connection<ChainOutConnector> {
		public ConnectionO(IPiece piece, ChainOutConnector t) {
			super(piece, t);
		}
	}
	public static abstract class Piece implements IPiece {

		@Override
		public ConnectionIO appendTo(PackType stack, IPiece target, PackType stack_target) throws ChainException {
			//if user assigns PREV FUNCTION
			if(target == null) {
				throw new ChainException(this, "appendTo()/Invalid Target/Null");
			} else if(this == target) {
				throw new ChainException(this, "appendTo()/Invalid Target/Same as Successor");
			}
			return null;
		}
		
		@Override
		public ConnectionO appended(Class<?> cls, Output type, PackType stack, IPiece from) throws ChainException {
			return null;
		}
		
		@Override
		public IPath detach(IPiece y) {
			return null;
		}

		@Override
		public Collection<IPiece> getPartners() {
			return null;
		}

		@Override
		public boolean isConnectedTo(IPiece target) {
			return false;
		}

		@Override
		public IPiece signal() {
			return null;
		}

		@Override
		public void end() {
		}

		private String name = null;
		public IPiece setName(String name) {
			this.name = name;
			return this;
		}
		public String getName() {
			if(name != null)
				return name;
			return getClass().getName();
		}

	}
	
	public static class ChainPiece extends Piece implements IPiece, Serializable, Tickable {
		public static ExecutorService threadExecutor = Executors.newCachedThreadPool();
		protected PieceHead fImpl;
		protected ArrayList<ChainInPathPack> inPack = new ArrayList<ChainInPathPack>();
		protected ArrayList<ChainOutConnectorPack> outPack = new ArrayList<ChainOutConnectorPack>();
		protected static CyclicBarrier signal = new CyclicBarrier(Integer.MAX_VALUE);
		private Boolean _chainlive = false, controlled_by_ac = true, inited = false;
//		Thread _th = null;
		Future<?> f = null;
		Chain _root_chain = null;
		ChainPiece cp_reactor = this;
		int mynum = 0;
		protected Partner partner = new Partner();
		private IErrorHandler _error = null;
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
				public boolean pieceRun(IPiece f) throws InterruptedException {
					return false;
				}

				@Override
				public boolean pieceReset(IPiece f) {
					return false;
				}
			});
		}
		ChainPiece(PieceHead tmpFImpl) {
			mynum = n++;
			fImpl = tmpFImpl;
			addNewInPack().setInType(Input.ALL);//PASSTHRU
			addNewInPack().setInType(Input.FIRST);//HEAP
			addNewInPack().setInType(Input.ALL);//FAMILY
			addNewInPack().setInType(Input.ALL);//.setUserPathListener(reset);//EVENT
			
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
		
		private boolean _doAndLoopInError(PieceHead head, ChainPiece cp) throws InterruptedException {
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
		
		protected ChainPiece setStatusHandler(StatusHandler st) {
			_statush = st;
			return this;
		}
		
		protected StatusHandler getStatusHandler() {
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
			return true;
		}
		
		protected PieceState restoreState() {
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
		
		public ChainPiece setInPackType(PackType pack, Input type) {
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
			public Partner setPartner(IPath o, IPiece cp) {
				partner.put(cp, o);
				return this;
			}
			public Partner unsetPartner(IPiece cp) {
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
					if(_path.getOutConnector().pack == cp.getOutPack(pt))
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
		
		
		public static class PathPack<T extends Connector> extends ArrayList<T> implements Serializable {
			PackType ptype = PackType.EVENT;
			IPiece parent;
			PathPack(ChainPiece _parent) {
				parent = _parent;
			}
			public PackType getPtype() {
				return ptype;
			}
			public void setPtype(PackType ptype) {
				this.ptype = ptype;
			}
			protected synchronized T addPath(T connect) {
				add(connect);
				notifyAll();
				return connect;
			}
			public synchronized T removePath(T connect) {
				remove(connect);
				notifyAll();
				return connect;
			}
			public void detachAll() {
				for(T outpath : this) {
					outpath.detach();
				}
			}
			public boolean hasPath() {
				return !isEmpty();
			}
		}
		
		
		public static class ChainOutConnectorPack extends PathPack<ChainOutConnector> {
//			Queue<ChainOutConnector> array;
			SyncQueue<Object> queue;
			Boolean lock = false;
			Output defaultType = Output.NORMAL;
			ChainOutConnectorPack(ChainPiece _parent) {
				super(_parent);
//				array = new ConcurrentLinkedQueue<ChainOutConnector>();
				queue = new SyncQueue<Object>();
			}
			public void reset() {
				queue.reset();
			}
			protected ChainOutConnectorPack setOutType(Output type) {
				defaultType = type;
				return this;
			}
			public ChainOutConnector addNewPath(Class<?> c, Output type) {
				ChainOutConnector rtn = new ChainOutConnector(parent, c, this, (type!=null)?type:defaultType);
				parent.__exec(String.format("NewPath = %s", rtn.type), "COPP#addNewPath");
				addPath(rtn);
				for(Object o : queue) {
					try {
						rtn.sync_push(o);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return rtn;
			}
			public boolean clearAll() {
				for(ChainOutConnector o: this) {
					o.reset();
				}
				queue.clear();
				return true;
			}
			public boolean send_reset() {
				boolean rtn = false;
				for(ChainOutConnector o: this) {
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
				int size = size();
				for(int i = 0; i < size; i++)
					rtn.add(obj);
				if(queue.size()==10)
					try {
						queue.sync_pop();
					} catch (IAxon.AxonException e) {
						return false;
					}
				if(obj != null)
					queue.sync_push(obj);
				return outputAll(rtn);
			}
			public boolean outputAll(ArrayList<?> ar) throws InterruptedException {
				if(isEmpty()) return false;
//				if(array.size() > ar.size()) return false;
				int i = 0;
				boolean rtn = false;
				for(ChainOutConnector a : this)
					rtn |= a.async_push(ar.get(i<ar.size()-1?i++:i));
//				ListIterator<?> itr2 = ar.listIterator(ar.size());
//				for(ListIterator<ChainOutPath> itr = aOutQueue.listIterator(aOutQueue.size()); itr.hasPrevious();)
//					itr.previous().push(itr2.previous());
				return rtn;
			}
			public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
				while(!outputAll(rtn)) {
					synchronized(this) {
						wait();
					}
				}
			}
			public void waitOutputAll(Object rtn) throws InterruptedException {
				while(!outputAllSimple(rtn)) {
					synchronized(this) {
						wait();
					}
				}
			}
		}
		
		
		
		static enum Input {
			ALL, FIRST, COUNT
		}
		public static class ChainInPathPack extends PathPack<ChainInConnector> {
			Input inputType;
			IPathListener listen, userlisten, resetHandler;
			SyncQueue<Connector> order_first;
			Iterator<ChainInConnector> now_count = null;
			public ChainInPathPack(ChainPiece _parent) {
				super(_parent);
				order_first = new SyncQueue<Connector>();
				inputType = Input.ALL;
			}
			public ChainInConnector addNewPath(Class<?> c)  {
				ChainInConnector rtn = new ChainInConnector(parent, c, this);
				rtn.setListener(new IPathListener() {
					@Override
					public void OnPushed(Connector p, Object obj) throws InterruptedException {
						if(listen != null)
							listen.OnPushed(p, obj);
						if(userlisten != null)
							userlisten.OnPushed(p, obj);
					}
				});
				rtn.setResetHandler(resetHandler);
				addPath(rtn);
				return rtn;
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
				if(isEmpty()) return null;
				ArrayList<Object> rtn = new ArrayList<Object>();
				for(ChainInConnector a : this)
					rtn.add(a.sync_peek());
				return rtn;
			}
			private ArrayList<Object> _inputPeekFirst() throws InterruptedException {
				Connector p = order_first.sync_peek();
				ArrayList<Object> rtn = new ArrayList<Object>();
				rtn.add(p.sync_peek());
//				Log.w("INPUT", p.parentPiece.getName());
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
				if(isEmpty()) return null;
				ArrayList<Object> rtn = new ArrayList<Object>();
				for(ChainInConnector a : this)
					rtn.add(a.sync_pop());
				return rtn;
			}
			
			private ArrayList<Object> _inputFirst() throws InterruptedException {
				SyncQueue<Connector> _pushedPath = order_first;
				Connector p;
				try {
					p = _pushedPath.sync_pop();
				} catch (IAxon.AxonException e) {
					return null;
				}
				ArrayList<Object> rtn = new ArrayList<Object>();
				rtn.add(p.sync_pop());
				parent.__exec(String.format("outputType = %s",p.type), "ChainPiece#_inputFirst");
				if(p.type == Output.HIPPO) {
					order_first.sync_push(p);
				}
				return rtn;
			}
			
			private ArrayList<Object> _inputCount() throws InterruptedException {
				if(isEmpty()) return null;
				ArrayList<Object> rtn = new ArrayList<Object>();
				if(now_count == null || !now_count.hasNext()) {
					now_count = iterator();
				}
				rtn.add(now_count.next().sync_pop());
//				reset();
				return rtn;
			}
			
			public Object inputOne(int num) throws InterruptedException {
				if(isEmpty()) return null;
				if(size() <= num) return null;
				return get(num).sync_pop();
			}

			public ChainInPathPack setInType(Input type) {
				order_first = new SyncQueue<Connector>();
				inputType = type;
				IPathListener tmpListener = null;
				switch(type) {
				case ALL:
				case COUNT:
					//cancel TYPE[INPUT FIRST]
					tmpListener = null;//new PathListener();
					break;
				case FIRST:
					//prepare TYPE[INPUT FIRST]
					tmpListener = new IPathListener() {
						@Override
						public void OnPushed(Connector p, Object obj) throws InterruptedException {
							order_first.sync_push(p);
						}
					};
					break;
				}
				_setPathListener(tmpListener);
				return this;
			}
			ChainInPathPack _setPathListener(IPathListener _listen) {
				listen = _listen;
				return this;
			}
			public ChainInPathPack setResetHandler(IPathListener _reset) {
				resetHandler = _reset;
				for(ChainInConnector a: this)
					a.setListener(_reset);
				return this;
			}
			public ChainInPathPack setUserPathListener(IPathListener _listen) {
				userlisten = _listen;
				return this;
			}
			public void reset() {
				for(ChainInConnector o: this) {
					o.reset();
				}
			}

		}
	};
	public static class FlexPiece extends ChainPiece {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		FlexPiece() {
			super();
		}
		FlexPiece(PieceHead fImpl) {
			super(fImpl);
		}
		public ConnectionIO appendTo(PackType stack, IPiece cp, Class<?> cls, PackType stack_target) throws ChainException {
			super.appendTo(stack,cp,stack_target);
			ChainInConnector i = addInPath(cls, stack);
			ConnectionO o = cp.appended(cls, null, stack_target, this);
			if(i.connect(o.getConnect())) {
				ChainPath p =  new ChainPath((ChainPiece)o.getPiece(), this, o.getConnect(), i);
				return new ConnectionIO(o.getPiece(),p);
			}
			return null;
		}
		@Override
		public ConnectionIO appendTo(PackType stack, IPiece cp, PackType stack_target) throws ChainException {
			return appendTo(stack, cp, ChainPiece.class, stack_target);
		}
		@Override
		public ConnectionO appended(Class<?> cls, Output type, PackType stack_target, IPiece from) throws ChainException {
			ChainOutConnector o = addOutPath(cls, type, stack_target);
//			partner.setPartner(o, from);
			return new ConnectionO(this, o);
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
		FixedChainPiece(PieceHead fImpl) {
			super(fImpl);
		}
		@Override
		public ConnectionIO appendTo(PackType stack, IPiece cp, PackType stack_target) throws ChainException {
			super.appendTo(stack, cp, stack_target);
			for(ChainInConnector i : getInPack(PackType.PASSTHRU)) {
				ConnectionO o = cp.appended(i.class_name, null, stack_target, this);
				if(i.connect(o.getConnect())) {
//					used = !hasAnyUnusedConnect();
					return new ConnectionIO(o.getPiece(), new ChainPath((ChainPiece)o.getPiece(), this, o.getConnect(), i));
				}
			}
			return null;
		}
		protected ConnectionO appended(Class<?> cls) throws ChainException {
			for(ChainOutConnector o : getOutPack(PackType.PASSTHRU))
				if(o.class_name == cls)
					return new ConnectionO(this, o);
			return null;
		}
		
	}
	enum Output { NORMAL, HIPPO, SYNC, TOGGLE }
	public static class Connector implements IConnector, Serializable {
		IPiece parentPiece = null;
		Class<?> class_name = null;
		boolean used = false;
		Connector partner = null;
		int order = 0;
		Hippo<IAxon<?>> QueueImpl = new Hippo<IAxon<?>>();
		Output type = Output.NORMAL;
		IPathListener listen = null, resetHandler = null;
		boolean end = false;
		ChainPath parentPath = null;
		
		public Connector() {
		}
		public Connector(IPiece parent, Class<?> c) {
			this();
			parentPiece = parent;
			class_name = c;
		}
		protected Connector setParentPath(ChainPath _path) {
			parentPath = _path;
			return this;
		}
		public void detach() {
			if(parentPath == null)
				return;
			parentPath.detach();
		}
		public IPiece getParent() {
			return parentPiece;
		}
		public Connector setUsed(boolean b) {
			used = b;
			return this;
		}
		public Connector setPartner(Connector i) {
			partner = i;
			return this;
		}
		public Connector setListener(IPathListener _listen) {
			listen = _listen;
			return this;
		}
		public Connector setResetHandler(IPathListener _reset) {
			resetHandler = _reset;
			return this;
		}
		public boolean isConnected() {
			return used;
		}
		//the best part of codes that i could implement
		public Connector setQueueImpl(IAxon<?> q) {
			QueueImpl.sync_push(q);
			return this;
		}
		public IAxon<?> getQueue() throws InterruptedException {
			return QueueImpl.sync_pop();
		}
		
		@SuppressWarnings("unchecked")
		public <T> T sync_pop() throws InterruptedException {
			try {
				T rtn = ((IAxon<Packet<T>>)getQueue()).sync_pop().getObject();
				if(parentPath != null)
					parentPath.tick();
				return rtn;
			} catch (IAxon.AxonException e) {
				return null;
			}
		}
		
		@SuppressWarnings("unchecked")
		public <T> T sync_peek() throws InterruptedException {
			return ((IAxon<Packet<T>>)getQueue()).sync_peek().getObject();
		}
		
		@SuppressWarnings("unchecked")
		public <T> boolean sync_push(T obj) throws InterruptedException {
			boolean rtn = ((IAxon<Packet<T>>)getQueue()).sync_push(new Packet<T>(obj, null));
			if(rtn && listen != null)
				listen.OnPushed(this, obj);
			return rtn;
		}
		
		public <T> boolean async_push(T obj) throws InterruptedException {
			@SuppressWarnings("unchecked")
			boolean rtn = ((IAxon<Packet<T>>)getQueue()).async_push(new Packet<T>(obj, null));
			if(rtn && listen != null)
				listen.OnPushed(this, obj);
			return rtn;
		}
		
		public boolean send_reset() {
			boolean rtn = resetHandler != null;
			if(rtn) {
				QueueImpl.reset();
				try {
					resetHandler.OnPushed(this, null);
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
			return ((IAxon<Packet<T>>)getQueue()).getCache().getObject();
		}
		
		@SuppressWarnings("unchecked")
		public IPiece getCacheSource() throws InterruptedException {
			return ((IAxon<Packet<?>>)getQueue()).getCache().getSource();
		}
		
		public void reset() {
			QueueImpl.reset();
			return;
		}
		
	}
	
	public static class ChainOutConnector extends Connector {
		ChainPiece.ChainOutConnectorPack pack = null;
		public ChainOutConnector(IPiece parent, Class<?> c, ChainPiece.ChainOutConnectorPack _pack, Output _type) {
			super(parent, c);
			pack = _pack;
			setType(_type);
			IAxon<?> q;
			if(_type == Output.NORMAL) {
//				if(c == String.class) {
//					q = new SyncQueue<Packet<String>>();
//				} else if(c == Integer.class) {
//					q = new SyncQueue<Packet<Integer>>();
//				} else {
//					q = new SyncQueue<Packet<String>>();
//				}
				q = new SyncQueue<Packet<?>>();
			} else if(_type == Output.HIPPO) {
//				if(c == String.class) {
//					q = new Hippo<Packet<String>>();
//				} else if(c == Integer.class) {
//					q = new Hippo<Packet<Integer>>();
//				} else {
//					q = new Hippo<Packet<String>>();
//				}
				q = new Hippo<Packet<?>>();
			} else if(_type == Output.TOGGLE) {
//				if(c == String.class) {
//					q = new Toggle<Packet<String>>();
//				} else if(c == Integer.class) {
//					q = new Toggle<Packet<Integer>>();
//				} else {
//					q = new Toggle<Packet<String>>();
//				}
				q = new Toggle<Packet<?>>();
			} else {
//				if(c == String.class) {
//					q = new SyncObject<Packet<String>>();
//				} else if(c == Integer.class) {
//					q = new SyncObject<Packet<Integer>>();
//				} else {
//					q = new SyncObject<Packet<String>>();
//				}
				q = new SyncObject<Packet<?>>();
			}
			setQueueImpl(q);
		}
/*		public Axon<?> compile(Chain c, Axon<?> q) {
			super.setQueueImpl(q);
			return compile(c);
		}
*/
		public ChainOutConnector setType(Output _type) {
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
	
	public static class ChainInConnector extends Connector {
		ChainPiece.ChainInPathPack pack = null;
		public ChainInConnector(IPiece parent, Class<?> c, ChainPiece.ChainInPathPack _pack) {
			super(parent, c);
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
						listen.OnPushed(o, null);
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
	
	public static class ChainPath implements Tickable, IPath {
		IPiece _cp_start, _cp_end;
		ChainOutConnector _out;
		ChainInConnector _in;
		private StatusHandler h = null;
		ChainPath(ChainPiece cp_start, ChainPiece cp_end, ChainOutConnector out, ChainInConnector in) {
			attach(cp_start, cp_end, out, in);
		}
		public ChainPath attach(IPiece cp_start, IPiece cp_end, ChainOutConnector out, ChainInConnector in) {
			_out = out;
			_in = in;
			_cp_start = cp_start;
			_cp_end = cp_end;
			_out.setParentPath(this);
			_in.setParentPath(this);
			_cp_end.setPartner(this, _cp_start);
			_cp_start.setPartner(this, _cp_end);
			return this;
	}
		public ChainOutConnector getOutConnector() {
			return _out;
		}
		public ChainInConnector getInConnector() {
			return _in;
		}
		public IPiece get_cp_end() {
			return _cp_end;
		}
		public IPiece get_cp_start() {
			return _cp_start;
		}
		public ChainPath detach() {
			getOutConnector().end();
			getInConnector().end();
			_cp_start.detached(_cp_end);
			_cp_end.detached(_cp_start);
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
		public StatusHandler getStatusHandler() {
			return h;
		}
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
			super("ChainException: "+str);
			err = str;
			location = piece.getName();
			loop = _loop;
		}
		ChainException(IPiece piece, String str) {
			this(piece, str, LoopableError.LOOPABLE);
		}
		ChainException(Connector cp, String str) {
			super("ChainException: "+str);
			err = str;
			location = "Path";
		}
		ChainException(IManager<?> cp, String str) {
			super("ChainException: "+str);
			err = str;
			location = "Manager";
		}
		ChainException(ChainPiece cp, String str, Throwable throwable) {
			super("ChainException: "+str, throwable);
			err = str;
			location = cp.getName();
		}
		public ChainException(Factory pieceFactory, String str) {
			super("ChainException: "+str);
			err = str;
			location = "Factory";
		}
		
	}
	public enum LoopableError {
		LOOPABLE, INTERRUPT, LOCK
	}
	public interface IPathListener {
		public void OnPushed(Connector p, Object obj) throws InterruptedException;
	}
	
	public static class Packet<T> implements IPacket<T> {
		T obj = null;
		IPiece source = null;
		Packet(T _obj, IPiece _source) {
			obj = _obj;
			source = _source;
		}
		public T getObject() {
			return obj;
		}
		public IPiece getSource() {
			return source;
		}
	}
	
	public interface IPacket<T> {
		public T getObject();
		public IPiece getSource();
	}
	
	public interface Tickable {
		boolean tick();
	}
	public interface IPath {
		public IPath attach(IPiece cp_start, IPiece cp_end, ChainOutConnector out, ChainInConnector in);
		public ChainOutConnector getOutConnector();
		public IPath detach();
		public IPath setStatusHandler(StatusHandler statusHandler);
	}
	
	public interface IConnector {
	}
	public interface PieceHead {
		abstract boolean pieceRun(IPiece f) throws InterruptedException, ChainException;
		abstract boolean pieceReset(IPiece f);
	}
	
	public interface IPiece {
		public ConnectionO appended(Class<?> cls, Output type, PackType stack, IPiece from) throws ChainException;
		public ConnectionIO appendTo(PackType stack, IPiece piece_to, PackType stack_target) throws ChainException;
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
}

