package org.tapchain.core;

public interface IConnector {
	//1.Initialization
	//2.Getters and setters
	PathPack<?> getPack();
	IConnector setParentPath(ConnectorPath connectorPath);
	//3.Changing state
	//4.Termination
	void end();
}