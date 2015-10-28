package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public class Manager<T extends IPiece> implements IManager<T, T> {
	IErrorHandler error = null;
	ILogHandler log = null;

	@Override
	public IManager<T, T> newSession() {
		return this;
	}

	@Override
	public Manager<T> add(T obj) {
		return null;
	}

	@Override
	public IManager<T, T> save() {
		return this;
	}

	@Override
	public IManager<T, T> teacher(T obj, T... args) {
		return this;
	}

	@Override
	public IManager<T, T> next(T obj) {
		return this;
	}

	@Override
	public IManager<T, T> _gotomark() {
		return this;
	}

	@Override
	public IManager<T, T> _mark() {
		return this;
	}

	@Override
	public IManager<T, T> _move(T point) {
		return this;
	}

	@Override
	public IManager<T, T> log(String... s) {
		if (log != null)
			log.log(s);
		return this;
	}

	public IManager<T, T> setLog(ILogHandler handle) {
		log = handle;
		return this;
	}

	@Override
	public IManager<T, T> error(ChainException e) {
		if (error != null)
			error.onError(null, e);
		return this;
	}

	public IManager<T, T> setError(IErrorHandler handle) {
		error = handle;
		return this;
	}
	
	protected IErrorHandler getErrorHandler() {
		return error;
	}

	@Override
	public IManager<T, T> _out() {
		return this;
	}

	@Override
	public IManager<T, T> _in() {
		return this;
	}

	@Override
	public IManager<T, T> remove(T bp) {
		return null;
	}

	@Override
	public IManager<T, T> _in(IPiece piece) {
		return this;
	}
}
