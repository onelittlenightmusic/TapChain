package org.tapchain.game;

import org.tapchain.core.Actor;
import org.tapchain.core.IValue;

public class ElectricityFactory extends Actor.Generator<Electricity> {
	public ElectricityFactory() {
		super();
		_valueSet(new Electricity());
	}

	@Override
	public void init(IValue<Electricity> val) {
		val._valueSet(new Electricity());
	}
}
