package org.tapchain.core;

import org.tapchain.core.Chain.ConnectionResultPath;
import org.tapchain.core.Chain.ConnectionResultOutConnector;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;

import java.util.Collection;

public interface IPiece<PARTNER extends IPiece> {
	/** Check and accept connection from other piece.
	 * @param type PackType of this piece
	 * @param from Piece from whom connection offered
	 * @param output Output type
	 * @return ConnectionResultO object
	 * @throws ChainException
	 */
    ConnectionResultOutConnector appended(PathType type, IPiece from, Output output) throws ChainException;

	/** Check and append this piece to target piece.
	 * @param type Packtype of this piece
	 * @param target_piece IPiece of target piece
	 * @param type_target PackType of target piece
	 * @return ConnectionResultIO object
	 * @throws ChainException
	 */
    ConnectionResultPath appendTo(PathType type, IPiece target_piece,
                                  PathType type_target) throws ChainException;

	/** Check and detach this piece from target piece.
	 * @param _cp_end IPiece of target piece
	 * @throws ChainException
	 */
    void detached(IPiece _cp_end);

	Collection<PARTNER> getPartners(PathType pathType, boolean out);

	void setPartner(IPath chainPath, IPiece _cp_start, PathType type_in);

	IPath detach(IPiece y);

	Collection<PARTNER> getPartners();

	/** Check connection between this object and secondpiece.
	 * @param target
	 * @return True when this object and secondpiece are connected.
	 */
    boolean isConnectedTo(IPiece target);
	boolean isConnectedTo(IPiece target, PathType type);
	boolean isConnectedTo(PathType type, boolean out);

    IPiece end();

	String getName();
	int getId();

	<T> T __exec(T obj, String flg);

	PathType getPackType(IPiece cp);

	OutPathPack getOutPack(PathType stack);

	InPathPack getInPack(PathType packtype);
	
	String getTag();
	void setTag(String tag);

    ChainPiece.LogCase L(String format);
}