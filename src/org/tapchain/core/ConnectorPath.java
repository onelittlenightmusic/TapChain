package org.tapchain.core;

import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Chain.Tickable;
import org.tapchain.core.Connector.ChainInConnector;
import org.tapchain.core.Connector.ChainOutConnector;

public class ConnectorPath implements Tickable, IPath {
	IPiece _cp_start, _cp_end;
	IConnector _out;
	IConnector _in;
	private IStatusHandler h = null;

	/** Create ConnectorPath from start ChainPiece and end ChainPiece objects.
	 * @param cp_start Start ChainPiece
	 * @param cp_end End ChainPiece
	 * @param out ChainOutConnector object
	 * @param in ChainInConnector object
	 */
	ConnectorPath(ChainPiece cp_start, ChainPiece cp_end,
			ChainOutConnector out, ChainInConnector in) {
		attach(cp_start, cp_end, out, in);
	}

	@Override
	public ConnectorPath attach(IPiece cp_start, IPiece cp_end,
			IConnector out, IConnector in) {
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

	@Override
	public IConnector getOutConnector() {
		return _out;
	}

	public IConnector getInConnector() {
		return _in;
	}

	public IPiece get_cp_end() {
		return _cp_end;
	}

	public IPiece get_cp_start() {
		return _cp_start;
	}

	public ConnectorPath detach() {
		getOutConnector().end();
		getInConnector().end();
		_cp_start.detached(_cp_end);
		_cp_end.detached(_cp_start);
		return this;
	}

	@Override
	public boolean tick() {
		if (h != null)
			h.tickView();
		return false;
	}

	public ConnectorPath setStatusHandler(IStatusHandler h) {
		this.h = h;
		return this;
	}

	public IStatusHandler getStatusHandler() {
		return h;
	}
}