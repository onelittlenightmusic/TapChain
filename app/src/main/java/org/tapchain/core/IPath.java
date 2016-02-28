package org.tapchain.core;

import org.tapchain.core.ActorManager.IStatusHandler;

public interface IPath {
	//1.Initialization
	//2.Getters and setters
	public IConnector getOutConnector();
	public IPath setStatusHandler(IStatusHandler<IPath> statusHandler);
	//3.Changing state
	/** Create Path object from start Piece object and end Piece object.
	 * @param cp_start Start Piece object
	 * @param cp_end End Piece object
	 * @param out Out IConnector object
	 * @param in In IConnector object
	 * @param type_out
     *@param type_in @return
	 */
	public IPath attach(IPiece cp_start, IPiece cp_end,
                        IConnector out, IConnector in, PathType type_out, PathType type_in);
	//4.Termination
	public IPath detach();
	
	/** Return class parameter of this path instance.
	 * Class parameter means that this path instance can pass objects of the class;
	 * @return Class parameter of the path
	 */
	public ClassEnvelope getConnectionClass();
	public ClassEnvelope setConnectionClass(ClassEnvelope cls);
	public void start();

	PathType getPathType();
	boolean isOut(IPiece piece);
}