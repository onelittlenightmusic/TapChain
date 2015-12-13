package org.tapchain.editor;

import org.tapchain.IFocusable;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.LinkType;

/**
 * Created by hiro on 2015/05/04.
 */
public interface IFocusControl {
    void unfocusAll(IFocusable spotOption);
    void clearAllFocusables();
    void addFocusable(IFocusable spot, LinkType al);
    void setTargetActor(Actor actor);
    LinkType getLinkType();
    Actor getTargetActor();
    void save(ActorManager manager);
    void setSpotActorLink(LinkType al);
}
