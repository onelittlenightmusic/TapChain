package org.tapchain.core;

public interface IConnector {
	IConnector setParentPath(ConnectorPath connectorPath);
	void end();
	PathPack<?> getPack();
}