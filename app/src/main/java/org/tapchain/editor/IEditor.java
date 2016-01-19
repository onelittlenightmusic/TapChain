package org.tapchain.editor;

import org.tapchain.core.ActorManager;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.Factory;
import org.tapchain.core.IActionStyle;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.LinkType;

import java.util.Collection;
import java.util.List;

public interface IEditor<ACTOR extends IPiece, VIEW extends ITap> {
    /**
     * Check if an actor tap touches any other actor taps, then connect their actors.
     *
     * @param selected
     *            a target actor tap which is checked
     * @return True when a actor is checked and connected.
     */
    boolean checkAndConnect(VIEW selected);

    /**
     * Get a collection of all taps
     * @return collection of all taps
     */
    Collection<VIEW> getTaps();

    /**
     * Get a collection of all actors
     * @return collection of all actors
     */
    Collection<ACTOR> getActors();

    /**
     * Get tap from actor
     * @param actor actor
     * @return tap
     */
    VIEW toTap(ACTOR actor);

    /**
     * Get actor from tap
     * @param view tap
     * @return actor
     */
    ACTOR toActor(VIEW view);

    /**
     * Get a manager for editing actor and actor tap.
     * For adding or removing actor, this is the first step.
     * And when you add actor by this return, actor tap will be added automatically by
     * instantiating tap blueprint style that you set.
     * @return Actormanager for editing actors.
     */
    ActorManager edit();

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

    void log(String... strings);

    /**
     * Invalidate taps view
     */
    void invalidate();

//    VIEW getCapturedActorTap();

    void setNextPos(IPoint nextConnectivity);

    void resetNextPos();

    List<IBlueprint<ACTOR>> highlightConnectables(LinkType reverse, IActorTap target, ClassEnvelope ce);

    boolean connect(ACTOR a1, LinkType al, ACTOR a2);

    void lockReleaseTap(IRelease t);

    void unhighlightAllConnectables();

    void shake(int duration);

    void changeFocus(LinkType al, IFocusable spot, ClassEnvelope clazz);
}