package org.tapchain.core;

public class TapBlueprint extends Blueprint {
	public TapBlueprint(Chain c, Class<? extends IPiece> _cls) {
		super(c, _cls);
	}

	public TapBlueprint(Blueprint bp, IPiece... args) {
		super(bp, args);
	}
	
	@Override
	public IBlueprint copy() {
		return new TapBlueprint(this);
	}


}