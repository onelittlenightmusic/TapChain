package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

/** Manager.
 * Manager is model class in TapChain.
 * TapChain creates and edits chains of piece class.
 * Manager has permission to create and editActor chains.
 * @author Hiro Osaki/android
 *
 * @param <T> Piece class
 */
/**
 * @author android
 *
 * @param <T>
 */
/**
 * @author android
 *
 * @param <T>
 */
/**
 * @author android
 *
 * @param <T>
 */
/**
 * @author android
 *
 * @param <T>
 */
public interface IManager<T, U> {
	//1.Initialization
	/** Create new session. In most cases, this method duplicates Manager object.
	 * @return Session object(Manager object).
	 */
    IManager<T, U> newSession();
	
	//2.Getters and setters: none
	/** Log internal information.
	 * Internal information must be declared as String type.
	 * @param s Internal information
	 * @return Manager object (this).
	 */
    IManager<T, U> log(String... s);
	//3.Chaging state
	/** Add new object to chain.
	 * [Caution] Until save() is called, this modification
	 * @param obj Object to be added to chain.
	 * @return Manager object.
	 */
    IManager<T, U> add(T obj);
//	public IManager<T> reset(T obj, IPiece... args);
	/** Save all modifications to chains.
	 * @return Manager object.
	 */
    IManager<T, U> save();
	/** Add new pullFrom object to chain.
	 * Teacher object means a piece that input information to current piece.
	 * [Caution] Until save() is called, this modification
	 * @param obj Teacher object to be added to chain.
	 * @return Manager object (this).
	 */
    IManager<T, U> pullFrom(T obj);
//    IManager<T, U> pushTo(T obj);

	/** Add new younger object to chain.
	 * Younger object means a subsequent piece after the current piece. 
	 * [Caution] Until save() is called, this modification
	 * @param obj Next object
	 * @return Manager object (this).
	 */
//    IManager<T, U> nextEvent(T obj);
	
	/** Go back to MARKED POINTER.
	 * This method sets MARKED POINTER (the internal "current piece" pointer) to the piece previously marked with _mark() method.
	 * @return Manager object (this).
	 */
    IManager<T, U> _gotomark();
	
	/** Mark current piece as MARKED POINTER.
	 * This method sets the current piece to MARKED POINTER (the internal "current piece" pointer)
	 * @return Manager object (this).
	 */
    IManager<T, U> _mark();

	/** Return to assigned pointer.
	 * @param point Pointer to return.
	 * @return Manager object (this).
	 */
    IManager<T, U> _move(T point);
	
	/** Send error signal from IManager
	 * @param e Error object to send.
	 * @return Manager object (this).
	 */
    IManager<T, U> error(ChainException e);
	/** Exit from this editorManager back to the offerToFamily editorManager.
	 * @return Parent editorManager object.
	 */
    IManager<T, U> _out();
	/** Go into child editorManager. The returned editorManager copes with current pointer object.
	 * To exit back to previous editorManager which is called "offerToFamily editorManager",
	 * you can call _out().
	 * @return Child editorManager object.
	 */
    IManager<T, U> _in();
	//4.Termination: none

	IManager<T, U> _in(T piece);
	void remove(T bp);
	}
