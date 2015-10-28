package org.tapchain.core;

import java.util.Collection;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;

public interface IPiece<PARTNER extends IPiece> {
	/** Check and accept connection from other piece.
	 * @param type PackType of this piece
	 * @param from Piece from whom connection offered
	 * @param output Output type
	 * @return ConnectionResultO object
	 * @throws ChainException
	 */
	public ConnectionResultO appended(PathType type, IPiece from, Output output) throws ChainException;

	/** Check and append this piece to target piece.
	 * @param type Packtype of this piece
	 * @param target_piece IPiece of target piece
	 * @param type_target PackType of target piece
	 * @return ConnectionResultIO object
	 * @throws ChainException
	 */
	public ConnectionResultIO appendTo(PathType type, IPiece target_piece,
			PathType type_target) throws ChainException;

	/** Check and detach this piece from target piece.
	 * @param _cp_end IPiece of target piece
	 * @throws ChainException
	 */
	public void detached(IPiece _cp_end);

	Collection<PARTNER> getPartners(PathType pathType, boolean out);

	public void setPartner(IPath chainPath, IPiece _cp_start);

	public IPath detach(IPiece y);

	public Collection<PARTNER> getPartners();

	/** Check connection between this object and secondpiece.
	 * @param target
	 * @return True when this object and secondpiece are connected.
	 */
	public boolean isConnectedTo(IPiece target);
	public boolean isConnectedTo(IPiece target, PathType type);
	public boolean isConnectedTo(PathType type, boolean out);

	public IPiece end();

	public String getName();
	public int getId();

	public <T> T __exec(T obj, String flg);

	PathType getPackType(IPiece cp);

	OutPathPack getOutPack(PathType stack);

	InPathPack getInPack(PathType packtype);
	
	public String getTag();
	public void setTag(String tag);

    ChainPiece.LogCase L(String format);
}