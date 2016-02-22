package org.tapchain.core;

public class TapBlueprint extends Blueprint {
	public TapBlueprint(Class<? extends IPiece> _cls) {
		super(_cls);
	}

	public TapBlueprint(Blueprint bp, IPiece... args) {
		super(bp, args);
	}
	
	@Override
	public IBlueprint copy() {
		return new TapBlueprint(this);
	}


}