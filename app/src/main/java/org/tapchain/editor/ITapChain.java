package org.tapchain.editor;

import org.tapchain.core.ActorManager;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.Factory;
import org.tapchain.core.IActionStyle;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.LinkType;

import java.util.List;

public interface ITapChain<ACTOR extends IPiece, VIEW extends ITapView> {
    /**
     * Check if an actor tap touches any other actor taps, then link their actors.
     *
     * @param selected
     *            a target actor tap which is checked
     * @return True when a actor is checked and connected.
     */
    boolean checkAndConnect(VIEW selected);

    /**
     * Get a manager for editing only taps (not actors).
     * For adding or removing simple taps such as path taps, this is the first step.
     * @return ActorManager for editing taps
     */
    ActorManager editTap();

    /**
     * @return the lInteract
     */
    IActionStyle getInteract();

    Factory getFactory();

    /**
     * Invalidate taps view
     */
    void invalidate();

//    VIEW getCapturedActorTap();

    void setNextPos(IPoint nextConnectivity);

    void resetNextPos();

    List<IBlueprint<ACTOR>> highlightLinkables(LinkType reverse, IActorTapView target, ClassEnvelope ce);

    /**
     * Connect an actor to another actor
     * @param a1 actor which link to another
     * @param type connection type as LinkType
     * @param a2 actor which is connected from a1
     */
    boolean link(ACTOR a1, LinkType type, ACTOR a2);
    IPath unlink(ACTOR x, ACTOR y);
    LinkType getLinkType(ACTOR a1, ACTOR a2);

    void lockReleaseTap(IRelease t);

    void unhighlightAllLinkables();

    void shake(int duration);

    void changeFocus(LinkType al, IFocusable spot, ClassEnvelope clazz);

}