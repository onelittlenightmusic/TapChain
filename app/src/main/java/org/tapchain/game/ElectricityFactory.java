package org.tapchain.game;

import org.tapchain.core.Actor;
import org.tapchain.core.IValue;

public class ElectricityFactory extends Actor.SimpleGenerator<Electricity> {
	public ElectricityFactory() {
		super();
		_set(new Electricity());
	}

	@Override
	public void init(IValue<Electricity> val) {
		val._set(new Electricity());
	}
}
