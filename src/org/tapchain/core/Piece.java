package org.tapchain.core;

import java.util.Collection;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.IPiece;
import org.tapchain.core.Chain.Output;

public abstract class Piece implements IPiece {

	@Override
	public ConnectionResultIO appendTo(Chain.PackType stack, IPiece target, Chain.PackType stack_target) throws ChainException {
		//if user assigns PREV FUNCTION
		if(target == null) {
			throw new ChainException(this, "appendTo()/Invalid Target/Null");
		} else if(this == target) {
			throw new ChainException(this, "appendTo()/Invalid Target/Same as Successor");
		}
		return null;
	}
	
	@Override
	public ConnectionResultO appended(Class<?> cls, Output type, Chain.PackType stack, IPiece from) throws ChainException {
		return null;
	}
	
	@Override
	public IPath detach(IPiece y) {
		return null;
	}

	@Override
	public Collection<IPiece> getPartners() {
		return null;
	}

	@Override
	public boolean isConnectedTo(IPiece target) {
		return false;
	}

	@Override
	public IPiece signal() {
		return null;
	}

	@Override
	public void end() {
	}

	private String name = null;
	public IPiece setName(String name) {
		this.name = name;
		return this;
	}
	public String getName() {
		if(name != null)
			return name;
		return getClass().getName();
	}

}