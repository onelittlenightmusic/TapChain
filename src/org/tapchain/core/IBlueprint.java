package org.tapchain.core;

import org.tapchain.core.Blueprint.TmpInstance;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.PackType;

/** Blueprint interface.
 * Blueprint interface is the template of mutable objects.
 * Manager can create mutable objects from Blueprint.
 * @author Hiro Osaki/android
 */
public interface IBlueprint {
	//1.Initialization
	//2.Getters and setters
	/** Get a reservation of this blueprint.
	 * Blueprint has a reservation of itself called "This". 
	 * This method returns "This" reservation.
	 * @return "This" Reservation
	 */
	TmpInstance This();
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
	 * @param usermaker Manager class.
	 * @return 
	 * @throws ChainException
	 */
	IPiece newInstance(IManager<IPiece> usermaker) throws ChainException;
	/** Add local blueprint into current blueprint
	 * @param _pbp Local blueprint
	 * @param args Options
	 * @return 
	 */
	IBlueprint addLocal(IBlueprint _pbp, IPiece... args);
	//4.Termination: none
	//5.Local classes: none
	IBlueprint addArg(Object... objs);
	IBlueprint Append(PackType heap, IBlueprint reserved, PackType heap2);
	IBlueprint refresh();
	IBlueprint copy(IPiece... args);
	IBlueprint copyAndRenewParam();
}