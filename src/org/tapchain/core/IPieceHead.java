package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public interface IPieceHead {
	abstract boolean pieceRun(IPiece f) throws InterruptedException,
			ChainException;

	abstract boolean pieceReset(IPiece f);
}