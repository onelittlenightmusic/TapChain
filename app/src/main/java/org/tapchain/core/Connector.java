package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;

import java.io.Serializable;

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
	Hippo<Path> parentPath = new Hippo<Path>();
	SyncQueue<Packet> pushWithNoPath = new SyncQueue<>();

	//1.Initialization
	public Connector() {
	}

	public Connector(IPiece parent, Class<?> c) {
		this();
		parentPiece = parent;
		class_name = c;
	}

	//2.Getters and setters
	public IConnector setParentPath(Path _path) {
		parentPath.sync_push(_path);
		while(!pushWithNoPath.isEmpty())
			try {
				_path.tick(pushWithNoPath.sync_pop());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IAxon.AxonException e) {
				e.printStackTrace();
			}
		return this;
	}
	
	public Path getParentPath() throws InterruptedException {
			return parentPath.sync_pop();
	}

	public Path getParentPathNoWait() throws InterruptedException {
		return parentPath.isEmpty() ? null : parentPath.sync_pop();
	}

	public void setPushWithNoPath(Packet obj) {
		try {
			pushWithNoPath.sync_push(obj);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

	public void detach() throws ChainException {
		if (parentPath == null)
			return;
		try {
			getParentPath().detach();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new Chain.ChainException("test");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Packet<T> sync_pop() throws InterruptedException {
		try {
			Packet<T> rtn2 = ((IAxon<Packet<T>>) getQueue()).sync_pop();
			return rtn2;
		} catch (IAxon.AxonException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Packet<T> sync_peek() throws InterruptedException, IAxon.AxonException {
		return ((IAxon<Packet<T>>) getQueue()).sync_peek();
	}

	@SuppressWarnings("unchecked")
	public <T> boolean sync_push(Packet<T> packet) throws InterruptedException {
		Path parent = getParentPathNoWait();
		if(parent != null) {
			parent.tick(packet);
			int intervalMs = parent.getTickInterval();
			if (intervalMs != 0)
				return __sync_push(((Packet)packet).setDelay(intervalMs), packet.getObject());
		} else {
			setPushWithNoPath(packet);
		}
		return __sync_push(packet, packet.getObject());
	}

	<T> boolean __sync_push(Packet<T> packet, T obj) throws InterruptedException {
		boolean rtn = ((IAxon<Packet<T>>) getQueue())
				.sync_push(packet);
		if (rtn && listen != null)
			listen.OnPushed(this, obj);
		return rtn;
	}

	public <T> boolean async_push(Packet<T> packet) throws InterruptedException {
		Path parent = getParentPathNoWait();
		if(parent != null) {
			parent.tick(packet);
			int intervalMs = parent.getTickInterval();
			if (intervalMs != 0)
				return __async_push(((Packet)packet).setDelay(intervalMs), packet.getObject());
		} else {
			setPushWithNoPath(packet);
		}
		return __async_push(packet, packet.getObject());
	}

	public <T> boolean __async_push(Packet<T> packet, T obj) throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean rtn = ((IAxon<Packet<T>>) getQueue())
				.async_push(packet);
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
	public static class OutConnector extends Connector {
		OutPathPack pack = null;
		public OutConnector(IPiece parent, Class<?> c,
				OutPathPack _pack, Output _type) {
			super(parent, c);
			pack = _pack;
			setType(_type);
			IAxon<?> q;
			if (_type == Output.NORMAL) {
				q = new DelayedSyncQueue<Packet<?>>();
			} else if (_type == Output.HIPPO) {
				q = new Hippo<Packet<?>>();
			} else if (_type == Output.TOGGLE) {
				q = new Toggle<Packet<?>>();
			} else {
				q = new SyncObject<Packet<?>>();
			}
			setQueueImpl(q);
		}

		public OutConnector setType(Output _type) {
			type = _type;
			return this;
		}

		public InConnector getPartner() {
			return (InConnector) partner;
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

	public static class InConnector extends Connector {
		InPathPack pack = null;

		public InConnector(IPiece parent, Class<?> c,
				InPathPack _pack) {
			super(parent, c);
			pack = _pack;
		}

		public boolean canPair(OutConnector o) {
			return !this.used && !o.used && this.class_name == o.class_name;
		}

		public OutConnector getPartner() {
			return (OutConnector) partner;
		}

		public boolean connect(OutConnector o)
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
		public <T> Packet<T> sync_pop() throws InterruptedException {
			return new Packet((T)cache, null);
		}

	}

}