package org.tapchain.core;

import org.tapchain.core.ActorManager.IStatusHandler;
import org.tapchain.core.Chain.IPiece;

public interface IPath {
	//1.Initialization
	//2.Getters and setters
	public IConnector getOutConnector();
	public IPath setStatusHandler(IStatusHandler statusHandler);
	//3.Changing state
	public IPath attach(IPiece cp_start, IPiece cp_end,
			IConnector out, IConnector in);
	//4.Termination
	public IPath detach();
}