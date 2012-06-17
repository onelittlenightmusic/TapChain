package org.tapchain;

import org.tapchain.Chain.IPiece;

public interface IManager<T> {
	public <F> F add(T obj, IPiece... args);
	public IManager<T> reset(T obj, IPiece... args);
	public IManager<T> save();
	public IManager<T> teacher(T obj, IPiece... args);
	public IManager<T> and(T obj, IPiece... args);
	public IManager<T> _return();
	public IManager<T> _mark();
}
