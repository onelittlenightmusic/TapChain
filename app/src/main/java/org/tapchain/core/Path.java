package org.tapchain.core;

import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Chain.Tickable;
import org.tapchain.core.Connector.InConnector;
import org.tapchain.core.Connector.OutConnector;

public class Path implements Tickable, IPath {
	IPiece _cp_start, _cp_end;
	IConnector _out;
	IConnector _in;
	private IStatusHandler<IPath> h = null;
	ClassEnvelope cls = null;

	/**
	 * Create ConnectorPath from start ChainPiece and end ChainPiece objects.
	 *  @param cp_start Start ChainPiece
	 * @param cp_end   End ChainPiece
     * @param out      ChainOutConnector object
     * @param in       ChainInConnector object
     * @param type_out
     * @param type_in
     */
	Path(ChainPiece cp_start, ChainPiece cp_end,
         OutConnector out, InConnector in, PathType type_out, PathType type_in) {
		attach(cp_start, cp_end, out, in, type_out, type_in);
	}

	@Override
	public Path attach(IPiece cp_start, IPiece cp_end,
                       IConnector out, IConnector in, PathType type_out, PathType type_in) {
		_out = out;
		_in = in;
		_cp_start = cp_start;
		_cp_end = cp_end;
		_cp_end.setPartner(this, _cp_start, type_out);
		_cp_start.setPartner(this, _cp_end, type_in);
		return this;
	}

	@Override
	public IConnector getOutConnector() {
		return _out;
	}

	public IConnector getInConnector() {
		return _in;
	}

	public Path detach() {
		getOutConnector().end();
		getInConnector().end();
		_cp_start.detached(_cp_end);
		_cp_end.detached(_cp_start);
		return this;
	}

	public void start() {
		_out.setParentPath(this);
		_in.setParentPath(this);

	}

	@Override
	public PathType getPathType() {
		return getOutConnector().getPack().getPathType();
	}

	@Override
	public boolean isOut(IPiece piece) {
		return _cp_start == piece;
	}

	@Override
	public int tick(Packet packet) {
		if (h != null) {
			return h.tickView(this, packet);
		}
		return 1;
	}

	@Override
	public int getTickInterval() {
		return tickInterval;
	}

    int tickInterval = 0;
    public void setTickInterval(int interval) {
        this.tickInterval = interval;
    }

	public Path setStatusHandler(IStatusHandler<IPath> h) {
		this.h = h;
		return this;
	}

	public IStatusHandler<IPath> getStatusHandler() {
		return h;
	}
	
	public ClassEnvelope setConnectionClass(ClassEnvelope _cls) {
		cls = _cls;
		return cls;
	}

	@Override
	public ClassEnvelope getConnectionClass() {
		return cls;
	}
}