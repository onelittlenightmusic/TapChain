package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public interface IErrorHandler {
	public ChainPiece onError(ChainPiece chainPiece, ChainException e);
	public ChainPiece onCancel(ChainPiece bp, ChainException e);
}