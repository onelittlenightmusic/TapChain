package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

/** Manager.
 * Manager is model class in TapChain.
 * TapChain creates and edits chains of piece class.
 * Manager has permission to create and edit chains.
 * @author Hiro Osaki/android
 *
 * @param <T> Piece class
 */
public interface IManager<T> {
	//1.Initialization
	/** Create new session. In most cases, this method duplicates Manager object.
	 * @return Session object(Manager object).
	 */
	public IManager<T> newSession();
	
	//2.Getters and setters: none
	/** Log internal information.
	 * Internal information must be declared as String type.
	 * @param s Internal information
	 * @return Manager object (this).
	 */
	public IManager<T> log(String... s);
	//3.Chaging state
	/** Add new object to chain.
	 * [Caution] Until save() is called, this modification
	 * @param obj Object to be added to chain.
	 * @param args Options
	 * @return Manager object.
	 */
	public <F> F add(T obj, IPiece... args);
//	public IManager<T> reset(T obj, IPiece... args);
	/** Save all modifications to chains.
	 * @return Manager object.
	 */
	public IManager<T> save();
	/** Add new teacher object to chain.
	 * Teacher object means a piece that input information to current piece.
	 * [Caution] Until save() is called, this modification
	 * @param obj Teacher object to be added to chain.
	 * @param args Options
	 * @return Manager object (this).
	 */
	public IManager<T> teacher(T obj, IPiece... args);
	
	/** Add new younger object to chain.
	 * Younger object means a subsequent piece after the current piece. 
	 * [Caution] Until save() is called, this modification
	 * @param obj 
	 * @param args
	 * @return Manager object (this).
	 */
	public IManager<T> young(T obj, IPiece... args);
	
	/** Go back to MARKED POINTER.
	 * This method sets MARKED POINTER (the internal "current piece" pointer) to the piece previously marked with _mark() method.
	 * @return Manager object (this).
	 */
	public IManager<T> _gotomark();
	
	/** Mark current piece as MARKED POINTER.
	 * This method sets the current piece to MARKED POINTER (the internal "current piece" pointer)
	 * @return
	 */
	public IManager<T> _mark();

	public IManager<T> _return(T point);
	
	public IManager<T> error(ChainException e);
	
	//4.Termination: none
}
