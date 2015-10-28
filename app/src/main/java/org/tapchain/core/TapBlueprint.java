package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.editor.IActorTap;

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