package org.tapchain.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.tapchain.core.Blueprint.TmpInstance;
import org.tapchain.core.Chain.ChainException;

/** Blueprint interface.
 * Blueprint interface is the template of mutable objects.
 * Manager can create mutable objects from Blueprint.
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
	 * @param usermaker Manager class.
	 * @return 
	 * @throws ChainException
	 */
	ACTOR newInstance(IManager<ACTOR, ACTOR> usermaker) throws ChainException;
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
	public void setTag(String tag);
	public String getTag();
	Class<? extends IPiece> getBlueprintClass();
	public void setInitialization(IBlueprintInitialization i);
	public boolean getFocused(LinkType ac);
	public void highlight(LinkType ac, boolean f);
	public void unhighlight();
	public void setNotification(IBlueprintFocusNotification n);
}