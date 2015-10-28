package org.tapchain.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Connector.InConnector;
import org.tapchain.core.Connector.OutConnector;
import org.tapchain.core.Connector.DummyConnector;
import org.tapchain.core.PathPack.OutPathPack.Output;

@SuppressWarnings("serial")
public class PathPack<T extends Connector> extends ArrayList<T> implements Serializable {
    public static interface PathPackType {
    }

    PathType ptype = PathType.EVENT;
    IPiece parent;
    HashSet<ClassEnvelope> envelopeClass = new HashSet<ClassEnvelope>();
    private static int COPY_QUEUE_MAX = 1;

    PathPack(Piece _parent) {
        parent = _parent;
    }

    public PathType getPathType() {
        return ptype;
    }

    public void setPtype(PathType ptype) {
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

    public void detachAll() throws ChainException {
        for (T outpath : this) {
            outpath.detach();
        }
    }

    public boolean hasPath() {
        return !isEmpty();
    }

    public boolean addPathClass(ClassEnvelope clz) {
        if (clz == null)
            return false;
        return envelopeClass.add(clz);
    }

    public boolean removePathClass(ClassEnvelope clz) {
        return envelopeClass.remove(clz);
    }

    public Collection<ClassEnvelope> getPathClasses() {
        return envelopeClass;
    }

    public ClassEnvelope getPathMainClass() {
        if (!envelopeClass.isEmpty())
            return ((ClassEnvelope) envelopeClass.toArray()[0]);
        return null;
    }

    public static class OutPathPack extends PathPack<OutConnector> {
        public static enum Output implements PathPackType {
            NORMAL, HIPPO, SYNC, TOGGLE
        }

        SyncQueue<Object> queue;
        Boolean lock = false;
        Output defaultType = Output.NORMAL;

        OutPathPack(Piece _parent) {
            super(_parent);
            queue = new SyncQueue<Object>();
        }

        public void reset() {
            for (OutConnector o : this) {
                o.reset();
            }
            queue.reset();
        }

        protected OutPathPack setOutType(Output type) {
            defaultType = type;
            return this;
        }

        public OutConnector addNewConnector(Output type) {
            OutConnector rtn = new OutConnector(parent, Object.class, this, (type != null) ? type : defaultType);
            parent.L("COPP#addNewPath").go(String.format("NewPath = %s", rtn.type));
            addPath(rtn);
            for (Object o : queue) {
                try {
                    rtn.sync_push(o);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rtn;
        }

        public boolean send_reset() {
            boolean rtn = false;
            for (OutConnector o : this) {
                rtn |= o.send_reset();
            }
            return rtn;
        }

        public synchronized void Lock(boolean _lock) {
            synchronized (lock) {
                lock = _lock;
                lock.notifyAll();
            }
            return;
        }

        public boolean outputAllSimple(Object obj) throws InterruptedException {
            synchronized (lock) {
                while (lock) lock.wait();
            }
            ArrayList<Object> rtn = new ArrayList<Object>();
            int size = size();
            for (int i = 0; i < size; i++)
                rtn.add(obj);
            if (queue.size() == COPY_QUEUE_MAX)
                try {
                    queue.sync_pop();
                } catch (IAxon.AxonException e) {
                    e.printStackTrace();
                    return false;
                }
            if (obj != null)
                queue.sync_push(obj);
            return outputAll(rtn);
        }

        public boolean outputAll(ArrayList<?> ar) throws InterruptedException {
            if (isEmpty()) return false;
            int i = 0;
            boolean rtn = false;
            for (OutConnector a : this)
                rtn |= a.async_push(ar.get(i < ar.size() - 1 ? i++ : i));
            return rtn;
        }

        public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
            while (!outputAll(rtn)) {
                synchronized (this) {
                    wait();
                }
            }
        }

        public void waitOutputAll(Object rtn) throws InterruptedException {
            while (!outputAllSimple(rtn)) {
                synchronized (this) {
                    wait();
                }
            }
        }
    }

    public static class InPathPack extends PathPack<InConnector> {
        public static enum Input implements PathPackType {
            ALL, FIRST, COUNT
        }

        InPathPack.Input inputType;
        IPathListener listen, userlisten, resetHandler;
        SyncQueue<Connector> order_first;
        Iterator<InConnector> now_count = null;
        SyncQueue<Object> inner_request = new SyncQueue<Object>();

        public InPathPack(Piece _parent) {
            super(_parent);
            order_first = new SyncQueue<Connector>();
            inputType = InPathPack.Input.ALL;
        }

        public InConnector addNewConnector() {
            InConnector rtn = new InConnector(parent, Object.class, this);
            rtn.setListener(new IPathListener() {
                @Override
                public void OnPushed(Connector p, Object obj) throws InterruptedException {
                    if (listen != null)
                        listen.OnPushed(p, obj);
                    if (userlisten != null)
                        userlisten.OnPushed(p, obj);
                }
            });
            rtn.setResetHandler(resetHandler);
            addPath(rtn);
            return rtn;
        }

        public ArrayList<Object> inputPeek() throws InterruptedException, IAxon.AxonException {
            switch (inputType) {
                case ALL:
                    return _inputPeekAll();
                case FIRST:
                    return _inputPeekFirst();
                case COUNT:
                default:
                    return new ArrayList<Object>();
            }
        }

        private ArrayList<Object> _inputPeekAll() throws InterruptedException, IAxon.AxonException {
            ArrayList<Object> rtn = new ArrayList<Object>();
            if (isEmpty())
                if (inner_request.isEmpty()) {
                    return rtn;
                } else {
                    rtn.addAll(inner_request);
                    return rtn;
                }
            for (InConnector a : this)
                rtn.add(a.sync_peek());
            return rtn;
        }

        private ArrayList<Object> _inputPeekFirst() throws InterruptedException, IAxon.AxonException {
            Connector p = order_first.sync_peek();
            ArrayList<Object> rtn = new ArrayList<Object>();
            rtn.add(p.sync_peek());
            return rtn;
        }

        public ArrayList<Object> input() throws InterruptedException {
            parent.L(String.format("InPack(%s)@input Start", ptype.toString())).go("WAITING");
            switch (inputType) {
                case ALL:
                    return _inputAll();
                case FIRST:
                    return _inputFirst();
                case COUNT:
                    return _inputCount();
                default:
                    return new ArrayList<Object>();
            }
        }

        private ArrayList<Object> _inputAll() throws InterruptedException {
            parent.L(String.format("InPack(%s)@inputAll Start", ptype.toString())).go("WAITING");
            ArrayList<Object> rtn = new ArrayList<Object>();
            if (isEmpty()) {
                rtn.addAll(inner_request);
                inner_request.clear();
                return rtn;
            }
            for (InConnector a : this)
                rtn.add(a.sync_pop());
            return rtn;
        }

        private ArrayList<Object> _inputFirst() throws InterruptedException {
            parent.L(String.format("InPack(%s)@inputFirst Start", ptype.toString())).go("WAITING");
            ArrayList<Object> rtn = new ArrayList<Object>();
            if (isEmpty() && order_first.isEmpty())
                return rtn;
            SyncQueue<Connector> _pushedPath = order_first;
            Connector p;
            try {
                p = _pushedPath.sync_pop();
            } catch (IAxon.AxonException e) {
                return rtn;
            }
            rtn.add(p.sync_pop());
            parent.L("ChainPiece#_inputFirst").go(String.format("outputType = %s", p.type));
            if (p.type == Output.HIPPO) {
                order_first.sync_push(p);
            }
            return rtn;
        }

        private ArrayList<Object> _inputCount() throws InterruptedException {
            ArrayList<Object> rtn = new ArrayList<Object>();
            if (isEmpty()) return rtn;
            if (now_count == null || !now_count.hasNext()) {
                now_count = iterator();
            }
            rtn.add(now_count.next().sync_pop());
            return rtn;
        }

        public Object inputOne(int num) throws InterruptedException {
            if (isEmpty()) return null;
            if (size() <= num) return null;
            return get(num).sync_pop();
        }

        public InPathPack setInType(InPathPack.Input type) {
            order_first = new SyncQueue<Connector>();
            inputType = type;
            IPathListener tmpListener = null;
            switch (type) {
                case ALL:
                case COUNT:
                    tmpListener = null;//new PathListener();
                    break;
                case FIRST:
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

        InPathPack _setPathListener(IPathListener _listen) {
            listen = _listen;
            return this;
        }

        public InPathPack setResetHandler(IPathListener _reset) {
            resetHandler = _reset;
            for (InConnector a : this)
                a.setListener(_reset);
            return this;
        }

        public InPathPack setUserPathListener(IPathListener _listen) {
            userlisten = _listen;
            return this;
        }

        public void reset() {
            for (InConnector o : this) {
                o.reset();
            }
        }

        protected void _queueInnerRequest(Object obj) throws InterruptedException {
            if (listen != null)
                listen.OnPushed(new DummyConnector(obj), obj);
            else
                inner_request.sync_push(obj);
        }

    }
}