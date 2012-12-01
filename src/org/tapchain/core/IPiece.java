package org.tapchain.core;

import java.util.Collection;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.PathPack.ChainInPathPack;
import org.tapchain.core.PathPack.ChainOutPathPack;
import org.tapchain.core.PathPack.ChainOutPathPack.Output;

public interface IPiece {
	/** Check and accept connection from other piece.
	 * @param output Output type
	 * @param type PackType of this piece
	 * @param from Piece from whom connection offered
	 * @return ConnectionResultO object
	 * @throws ChainException
	 */
	public ConnectionResultO appended(Output output, PackType type, IPiece from) throws ChainException;

	/** Check and append this piece to target piece.
	 * @param type Packtype of this piece
	 * @param target_piece IPiece of target piece
	 * @param type_target PackType of target piece
	 * @return ConnectionResultIO object
	 * @throws ChainException
	 */
	public ConnectionResultIO appendTo(PackType type, IPiece target_piece,
			PackType type_target) throws ChainException;

	public void detached(IPiece _cp_end);

	public void setPartner(IPath chainPath, IPiece _cp_start);

	public IPath detach(IPiece y);

	public Collection<IPiece> getPartners();

	/** Check connection between this object and secondpiece.
	 * @param target
	 * @return True when this object and secondpiece are connected.
	 */
	public boolean isConnectedTo(IPiece target);

	public void end();

	public String getName();
	public int getId();

	public <T> T __exec(T obj, String flg);

	PackType getPackType(IPiece cp);

	ChainOutPathPack getOutPack(PackType stack);

	ChainInPathPack getInPack(PackType packtype);
}