package org.tapchain.core;

import org.tapchain.core.Blueprint.TmpInstance;

/** Blueprint interface.
 * Blueprint interface is the template of mutable objects.
 * Manager can __create mutable objects from Blueprint.
 * @author Hiro Osaki/android
 */
public interface IBlueprint<ACTOR extends IPiece> {
	//1.Initialization
	//2.Getters and setters
	/** Get a reservation of this blueprint.
	 * Blueprint has a reservation of itself called "This". 
	 * This method returns "This" reservation.
	 * @return "This" Reservation
	 */
	TmpInstance<ACTOR> This();
	/** Get the name of Blueprint.
	 * @return Name String.
	 */
	String getName();
	/** Get a blueprint of view of current blueprint
	 * @return Blueprint of view
	 */
	IBlueprint<ACTOR> getView();
	/** Set a blueprint to view of current blueprint
	 * @param setLocalClass Blueprint of view
	 * @return current Blueprint
	 */
	IBlueprint setView(IBlueprint setLocalClass);
	
	
	//3.Changing state
	/** Create new instance of this Blueprint.
	 * @return
	 * @throws ChainException
	 */
	ACTOR newInstance() throws ChainException;
	/** Add local blueprint into current blueprint
	 * @param _pbp Local blueprint
	 * @return
	 */
	IBlueprint addLocal(IBlueprint _pbp);
	//4.Termination: none
	//5.Local classes: none
	IBlueprint addArg(Object... objs);
	IBlueprint append(PathType heap, IBlueprint reserved, PathType heap2);
	IBlueprint refresh();
	IBlueprint copy();
	IBlueprint copyAndRenewArg();
	void setTag(String tag);
	String getTag();
	Class<? extends IPiece> getBlueprintClass();
	void setInitialization(IBlueprintInitialization i);
	boolean getFocused(LinkType ac);
	void highlight(LinkType ac);
	void unhighlight();
	void setNotification(IBlueprintFocusNotification n);
    Chain getRootChain();
}