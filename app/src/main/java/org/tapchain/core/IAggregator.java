package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

public interface IAggregator<Input, Output> {
	public Output aggregate(Input... inputs) throws ChainException;
}
