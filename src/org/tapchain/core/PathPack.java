package org.tapchain.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.Output;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.Connector.*;

@SuppressWarnings("serial")
public class PathPack<T extends Connector> extends ArrayList<T> implements Serializable {
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
	public static class ChainInPathPack extends PathPack<ChainInConnector> {
		static enum Input {
			ALL, FIRST, COUNT
		}
				ChainInPathPack.Input inputType;
				IPathListener listen, userlisten, resetHandler;
				SyncQueue<Connector> order_first;
				Iterator<ChainInConnector> now_count = null;
				public ChainInPathPack(ChainPiece _parent) {
					super(_parent);
					order_first = new SyncQueue<Connector>();
					inputType = ChainInPathPack.Input.ALL;
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
	
			}
}