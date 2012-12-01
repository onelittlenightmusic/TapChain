package org.tapchain.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.PathPack.ChainOutPathPack.Output;
import org.tapchain.core.Connector.*;

@SuppressWarnings("serial")
public class PathPack<T extends Connector> extends ArrayList<T> implements Serializable {
	public static interface Type {}
	PackType ptype = PackType.EVENT;
	IPiece parent;
	Collection<Class<?>> envelopeClass = new HashSet<Class<?>>();
	private static int COPY_QUEUE_MAX = 10;
	PathPack(Piece _parent) {
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
	public boolean addPathClass(Class<?> cls) {
		envelopeClass.add(cls);
		return true;
	}
	public Collection<Class<?>> getPathClasses() {
		return envelopeClass;
	}
	public static class ChainOutPathPack extends PathPack<ChainOutConnector> {
	//	public static class FixedChainPiece extends ChainPiece {
		//		/**
		//		 * 
		//		 */
		//		private static final long serialVersionUID = 1L;
		//
		//		FixedChainPiece() {
		//			super();
		//		}
		//
		//		FixedChainPiece(IPieceHead fImpl) {
		//			super(fImpl);
		//		}
		//
		//		@Override
		//		public ConnectionResultIO appendTo(PackType stack, IPiece cp,
		//				PackType stack_target) throws ChainException {
		//			super.appendTo(stack, cp, stack_target);
		//			for (ChainInConnector i : getInPack(PackType.PASSTHRU)) {
		//				ConnectionResultO o = cp.appended(i.class_name, null, stack_target,
		//						this);
		//				if (i.connect(o.getConnect())) {
		//					// used = !hasAnyUnusedConnect();
		//					return new ConnectionResultIO(o.getPiece(), new ConnectorPath(
		//							(ChainPiece) o.getPiece(), this, o.getConnect(), i));
		//				}
		//			}
		//			return null;
		//		}
		//
		//		protected ConnectionResultO appended(Class<?> cls) throws ChainException {
		//			for (ChainOutConnector o : getOutPack(PackType.PASSTHRU))
		//				if (o.class_name == cls)
		//					return new ConnectionResultO(this, o);
		//			return null;
		//		}
		//
		//	}
		
			public static enum Output implements Type {
				NORMAL, HIPPO, SYNC, TOGGLE
			}
				//			Queue<ChainOutConnector> array;
				SyncQueue<Object> queue;
				Boolean lock = false;
				Output defaultType = Output.NORMAL;
				ChainOutPathPack(Piece _parent) {
					super(_parent);
	//				array = new ConcurrentLinkedQueue<ChainOutConnector>();
					queue = new SyncQueue<Object>();
				}
				public void reset() {
					queue.reset();
				}
				protected ChainOutPathPack setOutType(Output type) {
					defaultType = type;
					return this;
				}
				public ChainOutConnector addNewConnector(Output type) {
					ChainOutConnector rtn = new ChainOutConnector(parent, Object.class, this, (type!=null)?type:defaultType);
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
					if(queue.size()==COPY_QUEUE_MAX)
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
	public static class ChainInPathPack extends PathPack<ChainInConnector> {
		public static enum Input implements Type {
			ALL, FIRST, COUNT
		}
				ChainInPathPack.Input inputType;
				IPathListener listen, userlisten, resetHandler;
				SyncQueue<Connector> order_first;
				Iterator<ChainInConnector> now_count = null;
				SyncQueue<Object> inner_request = new SyncQueue<Object>();
				public ChainInPathPack(Piece _parent) {
					super(_parent);
					order_first = new SyncQueue<Connector>();
					inputType = ChainInPathPack.Input.ALL;
				}
				public ChainInConnector addNewConnector()  {
					ChainInConnector rtn = new ChainInConnector(parent, Object.class, this);
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
					ArrayList<Object> rtn = new ArrayList<Object>();
					if(isEmpty()) 
						if(inner_request.isEmpty()) {
							return null;
						} else {
							rtn.addAll(inner_request);
//							inner_request.clear();
							return rtn;
						}
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
					ArrayList<Object> rtn = new ArrayList<Object>();
					if(isEmpty()) 
						if(inner_request.isEmpty()) {
							return null;
						} else {
							rtn.addAll(inner_request);
							inner_request.clear();
							return rtn;
						}
//					if(isEmpty()) return null;
//					ArrayList<Object> rtn = new ArrayList<Object>();
					for(ChainInConnector a : this)
						rtn.add(a.sync_pop());
					return rtn;
				}
				
				private ArrayList<Object> _inputFirst() throws InterruptedException {
					ArrayList<Object> rtn = new ArrayList<Object>();
//					if(!inner_request.isEmpty()) {
//						rtn.addAll(inner_request);
//						inner_request.clear();
//						return rtn;
//					}
					SyncQueue<Connector> _pushedPath = order_first;
					Connector p;
					try {
						p = _pushedPath.sync_pop();
					} catch (IAxon.AxonException e) {
						return null;
					}
//					ArrayList<Object> rtn = new ArrayList<Object>();
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
	
				public ChainInPathPack setInType(ChainInPathPack.Input type) {
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
				PathPack.ChainInPathPack _setPathListener(IPathListener _listen) {
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
				protected void _queueInnerRequest(Object obj) throws InterruptedException {
					if(listen != null)
						listen.OnPushed(new DummyConnector(obj), obj);
					else
						inner_request.sync_push(obj);
				}
	
			}
}