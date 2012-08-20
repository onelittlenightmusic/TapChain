package org.tapchain.core;

import java.util.Collection;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.Output;
import org.tapchain.core.Chain.PackType;

public interface IPiece {
	public ConnectionResultO appended(Class<?> cls, Output type, PackType stack,
			IPiece from) throws ChainException;

	public ConnectionResultIO appendTo(PackType stack, IPiece piece_to,
			PackType stack_target) throws ChainException;

	public void detached(IPiece _cp_end);

	public void setPartner(IPath chainPath, IPiece _cp_start);

	public IPath detach(IPiece y);

	public Collection<IPiece> getPartners();

	public boolean isConnectedTo(IPiece target);

	public IPiece signal();

	public void end();

	public String getName();

	public <T> T __exec(T obj, String flg);
}