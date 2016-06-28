package org.tapchain.core;

public interface IPieceHead {
	boolean pieceRun(IPiece f) throws InterruptedException,
			ChainException;

	boolean pieceReset(IPiece f);
}