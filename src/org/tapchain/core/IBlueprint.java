package org.tapchain.core;

import org.tapchain.core.Blueprint.Reservation;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;

/** Blueprint interface.
 * Blueprint interface is the template of mutable objects.
 * Manager can create mutable objects from Blueprint.
 * @author Hiro Osaki/android
 */
public interface IBlueprint {
	//1.Initialization
	//2.Getters and setters
	/** Get a reservation of this blueprint.
	 * Blueprint has a reservation of itself called "This". This method returns "This" reservation.
	 * @return "This" Reservation
	 */
	Reservation This();
	/** Get the name of Blueprint.
	 * @return Name String.
	 */
	String getName();
	/** Get a blueprint of view of current blueprint
	 * @return Blueprint of view
	 */
	IBlueprint getView();
	/** Set a blueprint to view of current blueprint
	 * @param setLocalClass Blueprint of view
	 * @return current Blueprint
	 */
	IBlueprint setView(IBlueprint setLocalClass);
	
	
	//3.Changing state
	/** Create new instance of this Blueprint.
	 * @param maker Manager class.
	 * @return 
	 * @throws ChainException
	 */
	Actor newInstance(PieceManager maker) throws ChainException;
	Reservation newReservation(IPiece... args);
	/** Add local blueprint into current blueprint
	 * @param _pbp Local blueprint
	 * @param args Options
	 * @return 
	 */
	Reservation addLocal(IBlueprint _pbp, IPiece... args);
	/** Add argument classes and objects for current blueprint instantiation
	 * @param type Array of argument classes
	 * @param obj Array of argument objects
	 * @return
	 */
	IBlueprint addArgs(Class<?>[] type, Object[] obj);
	//4.Termination: none
	//5.Local classes: none
}