package org.tapchain;

import org.tapchain.Chain.ChainPiece;

public interface Manager<T> {
	public <F> F add(T obj, ChainPiece... args);
	public Manager<T> reset(T obj, ChainPiece... args);
	public Manager<T> Save();
	public Manager<T> args(T obj, ChainPiece... args);
	public Manager<T> and(T obj, ChainPiece... args);
	public Manager<T> _return();
	public Manager<T> _mark();
}
