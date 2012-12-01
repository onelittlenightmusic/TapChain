package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public class Manager<T> implements IManager<T> {
	IErrorHandler error = null;
	ILogHandler log = null;

	@Override
	public IManager<T> newSession() {
		return this;
	}

	@Override
	public <F> F add(T obj) {
		return null;
	}

	@Override
	public IManager<T> _save() {
		return this;
	}

	@Override
	public IManager<T> teacher(T obj, IPiece... args) {
		return this;
	}

	@Override
	public IManager<T> young(T obj) {
		return this;
	}

	@Override
	public IManager<T> _gotomark() {
		return this;
	}

	@Override
	public IManager<T> _mark() {
		return this;
	}

	@Override
	public IManager<T> _return(T point) {
		return this;
	}

	@Override
	public IManager<T> log(String... s) {
		if (log != null)
			log.log(s);
		return this;
	}

	public IManager<T> setLog(ILogHandler handle) {
		log = handle;
		return this;
	}

	@Override
	public IManager<T> error(ChainException e) {
		if (error != null)
			error.onError(null, e);
		return this;
	}

	public IManager<T> setError(IErrorHandler handle) {
		error = handle;
		return this;
	}
	
	protected IErrorHandler getErrorHandler() {
		return error;
	}
}
