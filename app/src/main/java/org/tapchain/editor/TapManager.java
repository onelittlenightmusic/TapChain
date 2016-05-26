package org.tapchain.editor;

import org.tapchain.ActorTapView;
import org.tapchain.PathTapView;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.ActorManager;
import org.tapchain.core.Blueprint;
import org.tapchain.core.Chain;
import org.tapchain.core.ChainException;
import org.tapchain.core.ChainPiece;
import org.tapchain.core.ChainPiece.PieceState;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IConnectHandler;
import org.tapchain.core.IErrorHandler;
import org.tapchain.core.IPath;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.Packet;
import org.tapchain.core.Path;
import org.tapchain.core.PathType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TapManager extends ActorManager implements IActorManager {
    Map<PieceState, Actor> plist = new EnumMap<>(
            PieceState.class);

    EditorReturn addedTap = null;

    /**
     * Constructor
     */
//    public Editor(Chain actorChain, Chain systemActorChain) {
    public TapManager(ITapChain actorChain) {
        super((Chain) actorChain);
    }

    /***
     * Copy constructor
     *
     * @param e EditorManager to copy
     */
    public TapManager(TapManager e) {
        super(e);
    }

    @Override
    public EditorChain getChain() {
        return (EditorChain) super.getChain();
    }

    public ActorManager editTap() {
        return new ActorManager(getChain().getSystemChain());
    }


    /**
     * Create an actor instance from its blueprint.
     *
     * @param blueprint the actor blueprint
     * @param nowPoint  position where the actor instance is added
     * @return the created actor instance and the its tap instance
     */

    public Actor add(IBlueprint<Actor> blueprint, IPoint nowPoint) {
        try {
            if (blueprint == null)
                return null;
            if (nowPoint == null) {
                if (getChain() instanceof TapChain)
                    nowPoint = ((TapChain) getChain()).getNextPos();
            }
//            EditorReturn rtn;
            Actor actor = blueprint.newInstance();
            ((TapChain)getChain()).getFactory(TapChain.FACTORY_KEY.LOG).Register(blueprint);
            add(actor).save();
            ((TapChain) getChain()).onAddAndInstallView(nowPoint, actor);
            return actor;
        } catch (ChainException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TapManager save() {
        super.save();
        Actor actor = getPiece();
        addedTap = null;
        Blueprint view = (Blueprint) ((TapChain)getChain()).getFactory().getView(actor.getTag());
        try {
            if (view != null) {
                ActorTapView v = __setPieceView(actor, view);
                addedTap = new EditorReturn(actor, v);
                return this;
            }
        } catch (ChainException e) {
            e.printStackTrace();
        }
        addedTap = new EditorReturn(actor, null);
        return this;
    }

    public EditorReturn getReturn() {
        return addedTap;
    }

    ActorTapView __setPieceView(Actor actor, final Blueprint bp)
            throws ChainException {
        final ActorTapView _view = (ActorTapView) bp.newInstance();
        if (_view == null)
            throw new ChainException(actor, "view not created");
        getChain().putPiece(actor, _view);
        if (actor.getLogLevel())
            ((ChainPiece) _view).setLogLevel(true);
        _view.setMyActor(actor);
        actor.setStatusHandler(new IStatusHandler<IPiece>() {
            @Override
            public void changeViewState(PieceState state) {
            }

            @Override
            public int tickView(IPiece p, Packet packet) {
                return _view.onTick((Actor) p, packet);
            }

            @Override
            public void pushView(IPiece t, Object obj) {
                if (_view.onPush((Actor) t, obj))
                    getChain().kick(_view);
            }
        });
        actor.setTickInterval(1000);
        if (_view instanceof IErrorHandler)
            actor.setError((IErrorHandler) _view);
        return _view;
    }

    @Override
    public Chain.ConnectionResultPath connect(Actor x, PathType xp, Actor y,
                                              PathType yp, boolean addView) {
        Chain.ConnectionResultPath rtn = super.connect(x, xp, y, yp, addView);
        if (rtn != null) {
            if (addView && getChain().blueprintForPathTap != null) {
                //PathTap instantiation
                IActorTapView xTap = getChain().toTap(x), yTap = getChain().toTap(y);
                IBlueprint newBlueprintForPath = getChain().blueprintForPathTap.copyAndRenewArg()
                        .addArg(yTap, xTap, yp, xp, rtn.getResult());
                IPathTapView pathTap = __setPathView(rtn.getResult(), newBlueprintForPath);

                //Post process 1 ( invoking common handler )
                if (getChain().actorConnectHandler != null)
                    getChain().actorConnectHandler.onConnect(yTap, pathTap, xTap, LinkType.fromPathType(yp, true));

                //Post process 2 ( invoking individual handler if registered )
                for (IActorTapView iTap : Arrays.asList(xTap, yTap))
                    if (iTap instanceof IConnectHandler)
                        ((IConnectHandler) iTap).onConnect(yTap, pathTap, xTap, LinkType.fromPathType(yp, true));
            }
        }
//		logLocal("link ended [%s]", toString(x, xp, y, yp));

        return rtn;
    }

    IPathTapView __setPathView(IPath path, IBlueprint _vReserve) {
        final PathTapView _view;
        try {
            _view = (PathTapView) _vReserve.newInstance();
            if (_view == null)
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        getChain().putPath(path, _view);
        _view.setMyPath(path);
        path.setStatusHandler(new IStatusHandler<IPath>() {
            @Override
            public int tickView(IPath path, Packet obj) {
                return _view.onTick(path, obj);
            }

            @Override
            public void changeViewState(PieceState state) {
            }

            @Override
            public void pushView(IPath t, Object obj) {
            }
        });
        ((Path) path).setTickInterval(1000);
        return _view;
    }

    @Override
    public ActorManager unsetPieceView(Actor bp) {
        __unsetView(bp);
        return this;
    }

    void __unsetView(Actor bp) {
        if (bp == null)
            return;
        IActorTapView v = getChain().toTap(bp);
        getChain().removePiece(bp);
        if (v == null)
            return;
        if (v instanceof ITapControlInterface)
            ((ITapControlInterface) v).unsetActor();
        editTap().remove((Actor) v);
    }

    @Override
    public IPath disconnect(IPath path) {
        super.disconnect(path);
        __unsetPathView(path);
        return path;
    }

    void __unsetPathView(IPath path) {
        if (path == null)
            return;
        PathTapView v = getChain().getTapPath(path);
        getChain().removePath(path);
        if (v == null)
            return;
        v.unsetMyPath();
        editTap().remove(v);
    }

    public void onRefreshView(Actor bp, Actor obj) {
        IActorTapView v = getChain().toTap(bp);
        getChain().removePiece(bp);
        if (bp.compareTo(obj) > 0) {
            bp.initNum();
        }
        getChain().putPiece(bp, v);
    }
}
