package org.tapchain.core;


import org.tapchain.core.ChainPiece.PieceState;

import java.util.List;


public class ActorManager extends PieceManager<Actor> {
    Actor root = null;
    ActorManager parent = null;

    //1.Initialization
    public ActorManager() {
        super();
    }

    public ActorManager(ActorManager am) {
        super(am);
    }

    public ActorManager(Chain chain) {
        super(chain);
    }

    @Override
    public ActorManager newSession() {
        ActorManager rtn = new ActorManager(this);
        rtn.setChain(chain);
        return rtn;
    }


    //2.Getters and setters

    @Override
    public Actor getPiece() {
        return super.getPiece();
    }

    public ActorManager setParentManager(ActorManager _parent) {
        parent = _parent;
        return this;
    }

    public ActorManager getParentManager() throws ChainException {
        if (parent != null)
            return parent;
        throw new ChainException(this, "No Parent");
    }

    public Actor getRoot() {
        return root;
    }

    public ActorManager setRoot(Actor arg) {
        root = arg;
        return this;
    }

    //3.Changing state
    @Override
    public ActorManager _in() {
        return newSession().setParentManager(this).setRoot(getPiece());
    }

    @Override
    public ActorManager _in(Actor actor) {
        return _in().add(actor)._out();
    }

    List<IPiece> dump;

    @Override
    public ActorManager save() {
        dump = getChain().getOperator().save();
        return this;
    }

    public List<IPiece> dump() {
        return dump;
    }

    @Override
    public ActorChain getChain() {
        return (ActorChain) super.getChain();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActorManager add(Actor bp) {
        super.add(bp);
        if (bp == null)
            return this;
        getChain().getOperator().add(bp);
//		bp.addFromFactory(newSession());
        _move(bp);
        if (getRoot() != null) {
            super.append(bp, PathType.FAMILY, getRoot(), PathType.FAMILY, false);
        }
        return this;
    }

    public ActorManager addActor(IActor actor) {
        return add(new Actor().setActor(actor));
    }

    @Override
    public <VALUE, INPUT, OUTPUT> ActorManager add(final IFilter<VALUE, INPUT, OUTPUT> func, final VALUE init) {
        Actor adding;
        adding = new Filter.FilterSkelton<>(func, init)/*.setLogLevel(true)*/;
        add(adding);
        return this;
    }

    @Override
    public <VALUE, OUTPUT> ActorManager add(final IGenerator<VALUE, OUTPUT> generator, final VALUE init) {
        add(new Generator.GeneratorSkelton<>(generator, init)/*.setLogLevel(true)*/);
//        Log.w("test", "Generator created");
        return this;
    }

    @Override
    public <VALUE, INPUT> ActorManager add(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
        add(new Consumer.ConsumerSkelton<>(consumer, init)/*.setLogLevel(true)*/);
//        Log.w("test", "Consumer created");
        return this;
    }

    @Override
    public <PARENT, EFFECT> ActorManager add(final IEffector<PARENT, EFFECT> effector, final EFFECT init, int duration) {
        add(new Effector.EffectorSkelton<>(effector, init, duration));
        return this;
    }

    public <VALUE, INPUT, OUTPUT> ActorManager pushTo(final IFilter<VALUE, INPUT, OUTPUT> func, final VALUE init) {
        Actor a = getPiece();
        add(func, init);
        _appendOffer(a, getPiece());
        return this;
    }

    public <VALUE, INPUT> ActorManager pushTo(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
        Actor a = getPiece();
        add(consumer, init);
        _appendOffer(a, getPiece());
        return this;
    }

    @Override
    public void remove(Actor actor) {
        if (actor == null)
            return;
        unsetPieceView(actor);
        actor.onRemove(newSession());
        for (Actor partner : actor.getPartners()) {
            disconnect(actor, partner);
        }
        super.remove(actor);
        actor.end();
    }

    public void restart(IPiece pieceBody) {
        if (pieceBody instanceof ChainPiece)
            ((ChainPiece) pieceBody).restart();
    }

    @Override
    public ActorManager _out() {
        try {
            return getParentManager();
        } catch (ChainException e) {
            error(e);
        }
        return this;
    }

    @Override
    public ActorManager unsetPieceView(Actor bp) {
        return this;
    }

    public ActorManager tag(String s) {
        if(getPiece() != null)
            getPiece().setTag(s);
        return this;
    }

    public interface IStatusHandler<T> {
        void changeViewState(PieceState state);

        int tickView(T t, Packet obj);

        void pushView(T t, Object obj);
//        int getTickInterval();
    }

}
