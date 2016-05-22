package org.tapchain.editor;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain;
import org.tapchain.core.Chain;
import org.tapchain.core.IManager;
import org.tapchain.core.IPath;
import org.tapchain.core.IPoint;
import org.tapchain.core.PathType;

/**
 * Created by hiro on 2015/05/21.
 */
public interface IActorManager extends IManager<Actor, Actor> {
    Chain.ConnectionResultPath append(Actor x, PathType xp, Actor y,
                                             PathType yp, boolean addView);
    IPath disconnect(IPath path);
}
