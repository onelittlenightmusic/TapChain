package org.tapchain.core;

import java.io.Serializable;

import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.PathPack.ChainOutPathPack.Output;
import org.tapchain.core.PathPack.*;

@SuppressWarnings("serial")
public abstract class Connector implements IConnector, Serializable {
	IPiece parentPiece = null;
	Class<?> class_name = null;
	boolean used = false;
	Connector partner = null;
	int order = 0;
	Hippo<IAxon<?>> QueueImpl = new Hippo<IAxon<?>>();
	Output type = Output.NORMAL;
	IPathListener listen = null, resetHandler = null;
	boolean end = false;
	ConnectorPath parentPath = null;

	//1.Initialization
	public Connector() {
	}

	public Connector(IPiece parent, Class<?> c) {
		this();
		parentPiece = parent;
		class_name = c;
	}

	//2.Getters and setters
	public IConnector setParentPath(ConnectorPath _path) {
		parentPath = _path;
		return this;
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

	public boolean isNotEmpty() {
		return !(QueueImpl).isEmpty();
	}

	@SuppressWarnings("unchecked")
	public <T> T getCache() throws InterruptedException {
		return ((IAxon<Packet<T>>) getQueue()).getCache().getObject();
	}

	@SuppressWarnings("unchecked")
	public IPiece getCacheSource() throws InterruptedException {
		return ((IAxon<Packet<?>>) getQueue()).getCache().getSource();
	}

	//3.Changing state
	// the best part of code of this class
	Connector setQueueImpl(IAxon<?> q) {
		QueueImpl.sync_push(q);
		return this;
	}

	IAxon<?> getQueue() throws InterruptedException {
		return QueueImpl.sync_pop();
	}

	public void detach() {
		if (parentPath == null)
			return;
		parentPath.detach();
	}

	@SuppressWarnings("unchecked")
	public <T> T sync_pop() throws InterruptedException {
		try {
			T rtn = ((IAxon<Packet<T>>) getQueue()).sync_pop().getObject();
			if (parentPath != null)
				parentPath.tick();
			return rtn;
		} catch (IAxon.AxonException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T sync_peek() throws InterruptedException {
		return ((IAxon<Packet<T>>) getQueue()).sync_peek().getObject();
	}

	@SuppressWarnings("unchecked")
	public <T> boolean sync_push(T obj) throws InterruptedException {
		boolean rtn = ((IAxon<Packet<T>>) getQueue())
				.sync_push(new Packet<T>(obj, null));
		if (rtn && listen != null)
			listen.OnPushed(this, obj);
		return rtn;
	}

	public <T> boolean async_push(T obj) throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean rtn = ((IAxon<Packet<T>>) getQueue())
				.async_push(new Packet<T>(obj, null));
		if (rtn && listen != null)
			listen.OnPushed(this, obj);
		return rtn;
	}

	public boolean send_reset() {
		boolean rtn = resetHandler != null;
		if (rtn) {
			QueueImpl.reset();
			try {
				resetHandler.OnPushed(this, null);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return rtn;
	}

	//4.Termination
	public void reset() {
		QueueImpl.reset();
		return;
	}
	
	//5.Local classes
	public static class ChainOutConnector extends Connector {
		ChainOutPathPack pack = null;

		public ChainOutConnector(IPiece parent, Class<?> c,
				ChainOutPathPack _pack, Output _type) {
			super(parent, c);
			pack = _pack;
			setType(_type);
			IAxon<?> q;
			if (_type == Output.NORMAL) {
				// if(c == String.class) {
				// q = new SyncQueue<Packet<String>>();
				// } else if(c == Integer.class) {
				// q = new SyncQueue<Packet<Integer>>();
				// } else {
				// q = new SyncQueue<Packet<String>>();
				// }
				q = new SyncQueue<Packet<?>>();
			} else if (_type == Output.HIPPO) {
				// if(c == String.class) {
				// q = new Hippo<Packet<String>>();
				// } else if(c == Integer.class) {
				// q = new Hippo<Packet<Integer>>();
				// } else {
				// q = new Hippo<Packet<String>>();
				// }
				q = new Hippo<Packet<?>>();
			} else if (_type == Output.TOGGLE) {
				// if(c == String.class) {
				// q = new Toggle<Packet<String>>();
				// } else if(c == Integer.class) {
				// q = new Toggle<Packet<Integer>>();
				// } else {
				// q = new Toggle<Packet<String>>();
				// }
				q = new Toggle<Packet<?>>();
			} else {
				// if(c == String.class) {
				// q = new SyncObject<Packet<String>>();
				// } else if(c == Integer.class) {
				// q = new SyncObject<Packet<Integer>>();
				// } else {
				// q = new SyncObject<Packet<String>>();
				// }
				q = new SyncObject<Packet<?>>();
			}
			setQueueImpl(q);
		}

		/*
		 * public Axon<?> compile(Chain c, Axon<?> q) {
		 * super.setQueueImpl(q); return compile(c); }
		 */
		public ChainOutConnector setType(Output _type) {
			type = _type;
			return this;
		}

		/*
		 * public Axon<?> compile(Chain c) { if(partner == null) { return
		 * null; } return QueueImpl; }
		 */
		public ChainInConnector getPartner() {
			return (ChainInConnector) partner;
		}

		@Override
		public void end() {
			pack.removePath(this);
			try {
				getQueue().CloseForced();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public PathPack<?> getPack() {
			return pack;
		}
	}

	public static class ChainInConnector extends Connector {
		ChainInPathPack pack = null;

		public ChainInConnector(IPiece parent, Class<?> c,
				ChainInPathPack _pack) {
			super(parent, c);
			pack = _pack;
		}

		/*
		 * public Axon<?> compile() { return QueueImpl; }
		 */public boolean canPair(ChainOutConnector o) {
			return !this.used && !o.used && this.class_name == o.class_name;
		}

		public ChainOutConnector getPartner() {
			return (ChainOutConnector) partner;
		}

		public boolean connect(ChainOutConnector o)
				throws Chain.ChainException {
			if (!canPair(o)) {
				return false;
			}
			try {
				setQueueImpl(o.getQueue());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			o.setPartner(this).setUsed(true);
			this.setPartner(o).setUsed(true);
			o.setListener(listen);
			o.setResetHandler(resetHandler);
			try {
				int size = o.getQueue().size();
				// Log.w("ChainConnect", Integer.toString(size));
				for (int i = 0; i < size; i++) {
					listen.OnPushed(o, null);
				}
			} catch (InterruptedException e) {
				throw new Chain.ChainException(this,
						"Connect was cancelled");
			}
			return true;
		}

		@Override
		public void end() {
			pack.removePath(this);
		}
		@Override
		public PathPack<?> getPack() {
			return pack;
		}
	}
	public static class DummyConnector extends Connector {
		Object cache;
		public DummyConnector(Object obj) {
			super();
			cache = obj;
		}

		@Override
		public PathPack<?> getPack() {
			return null;
		}

		@Override
		public void end() {
		}
		@SuppressWarnings("unchecked")
		public <T> T sync_pop() throws InterruptedException {
			return (T)cache;
		}

	}

}