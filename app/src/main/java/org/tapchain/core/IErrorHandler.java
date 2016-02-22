package org.tapchain.core;

public interface IErrorHandler<ACTOR extends ChainPiece> {
	/** Handler function which will be called when an IPiece instance catches ChainException.
	 * @param actor Piece instance which catches ChainException.
	 * @param e ChainException object which an IPiece instance catches.
	 * @return IPiece instance.
	 */
	public ChainPiece onError(ACTOR actor, ChainException e);
	public ChainPiece onUnerror(ACTOR actor, ChainException e);
}