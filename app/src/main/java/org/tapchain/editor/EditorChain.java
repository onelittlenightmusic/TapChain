package org.tapchain.editor;

import org.tapchain.PathTapView;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain;
import org.tapchain.core.Blueprint;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hiro on 2016/05/17.
 */
public class EditorChain extends ActorChain {
    ActorChain systemActorChain = new ActorChain(50);
    ConcurrentHashMap<Actor, IActorTapView> dictPiece = new ConcurrentHashMap<>();
    ConcurrentHashMap<IPath, PathTapView> dictPath = new ConcurrentHashMap<>();
    IBlueprint defaultView = null;

    IActorConnectHandler actorConnectHandler;
    Blueprint blueprintForPathTap;

    public EditorChain(int time) {
        super(time);
    }

    public ActorChain getSystemChain() {
        return systemActorChain;
    }

    /**
     * Get a collection of all taps
     * @return collection of all taps
     */
    public Collection<IActorTapView> getTaps() {
        return dictPiece.values();
    }

    /**
     * Get a collection of all actors
     * @return collection of all actors
     */
    public Collection<Actor> getActors() {
        return dictPiece.keySet();
    }

    /**
     * Get tap from actor
     * @param actor actor
     * @return tap
     */
    public IActorTapView toTap(Actor actor) {
        if (dictPiece.get(actor) != null)
            return dictPiece.get(actor);
        return null;
    }

    /**
     * Get actor from tap
     * @param path tap
     * @return actor
     */
    public PathTapView getTapPath(IPath path) {
        if (dictPath.get(path) != null)
            return dictPath.get(path);
        return null;
    }

    public void setPathBlueprint(Blueprint p) {
        blueprintForPathTap = p;
    }

    public void setActorConnectHandler(IActorConnectHandler a) {
        actorConnectHandler = a;
    }


    public void putPiece(Actor actor, IActorTapView view) {
        dictPiece.put(actor, view);
    }

    public void putPath(IPath path, PathTapView view) {
        dictPath.put(path, view);
    }

    public void removePiece(Actor actor) {
        dictPiece.remove(actor);
    }

    public void removePath(IPath path) {
        dictPiece.remove(path);
    }

    public IBlueprint<IPiece> getDefaultView() {
        return defaultView;
    }
}
