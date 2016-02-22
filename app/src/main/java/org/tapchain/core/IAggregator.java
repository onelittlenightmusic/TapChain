package org.tapchain.core;

public interface IAggregator<Input, Output> {
	public Output aggregate(Input... inputs) throws ChainException;
}
