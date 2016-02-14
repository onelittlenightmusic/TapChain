package org.tapchain.core;


import android.util.Log;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.ChainPiece.PieceState;

import java.util.List;


public class ActorManager extends PieceManager<Actor> {
	Actor root = null;
	ActorManager parent = null;

	//1.Initialization
	public ActorManager() {
		super();
	}
	
	public ActorManager(
//			IErrorHandler _error,
			ILogHandler _log)
	{
		this();
		this
		.setLog(_log)
//		.setError(_error)
		;
	}
	
	public ActorManager(ActorManager am) {
		this(
//                am.error,
                am.log
        );

	}

    public ActorManager createChain(int time) {
		setChain(new ActorChain(time));
		return this;
	}

	@Override
	public ActorManager newSession() {
		return new ActorManager(this)
			.setChain((ActorChain)chain);
	}
	
	//2.Getters and setters
	public ActorManager setChain(ActorChain c) {
		super.setChain(c);
		if(log != null)
			c.setLog(log);
		return this;
	}
	
	@Override
	public Actor getPiece() {
		return (Actor)super.getPiece();
	}
	
	public ActorManager setParentManager(ActorManager _parent) {
		parent = _parent;
		return this;
	}
	public ActorManager getParentManager() throws ChainException {
		if(parent != null)
			return parent;
		throw new ChainException(this, "No Parent");
	}
	@Override
	public ActorManager setLog(ILogHandler _log) {
		super.setLog(_log);
		if(chain != null)
			chain.setLog(_log);
		return this;
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
		return (ActorChain)super.getChain();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorManager add(Actor bp) {
		super.add(bp);
		if(bp == null) 
			return this;
		getChain().getOperator().add(bp);
//		bp.addFromFactory(newSession());
		_move(bp);
		if(getRoot() != null) {
			super.append(bp, PathType.FAMILY, getRoot(), PathType.FAMILY, false);
		}
		return this;
	}
	
	public ActorManager addActor(IActor actor) {
		return add(new Actor().setActor(actor));
	}
	
    @Override
	public <VALUE, INPUT, OUTPUT> ActorManager add(final IFunc<VALUE, INPUT, OUTPUT> func, final VALUE init) {
		Actor adding;
//		if(designer instanceof IFunc)
			adding = new Actor.FilterSkelton<>(func, init).setLogLevel(true);
        Log.w("test", "Func created");
//		else if(designer instanceof IConsumer)
//			adding = new Actor.ValueConsumer<VALUE>() {
//				@Override
//				public void init(IValue<VALUE> val) {
//					designer.init(val);
//				}
//
//				@Override
//				public void consume(VALUE in) {
//					((IConsumer<VALUE>)designer).consume(in);
//				}
//			};
//		else if(designer instanceof IGenerator)
//			adding = new Actor.Generator<VALUE>() {
//				@Override
//				public void init(IValue<VALUE> val) {
//					designer.init(val);
//				}
//
//				@Override
//				public VALUE generate() {
//					return ((IGenerator<VALUE>)designer).generate();
//				}
//			};
//		if(adding != null)
			add(adding);
		return this;
	}

    @Override
    public <OUTPUT> ActorManager add(final IGenerator<OUTPUT> generator, final OUTPUT init) {
        add(new Actor.GeneratorSkelton<>(generator, init).setLogLevel(true));
        Log.w("test", "Generator created");
        return this;
    }

    @Override
    public <VALUE, INPUT> ActorManager add(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
        add(new Actor.ConsumerSkelton<>(consumer, init).setLogLevel(true));
        Log.w("test", "Consumer created");
        return this;
    }

    public <VALUE, INPUT, OUTPUT> ActorManager pushTo(final IFunc<VALUE, INPUT, OUTPUT> func, final VALUE init) {
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

    public <PARENT, EFFECT> ActorManager addEffector(final IEffector<PARENT, EFFECT> effector) {
		add(new Actor.OriginalEffector<PARENT, EFFECT>() {
            @Override
            public void effect(PARENT _t, EFFECT _e) throws ChainException {
                effector.effect(_t, _e);
            }
        });
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
		return;
	}
	
	public void restart(IPiece pieceBody) {
		if(pieceBody instanceof ChainPiece)
			((ChainPiece)pieceBody).restart();
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

    public interface IStatusHandler<T> {
        void changeViewState(PieceState state);
		int tickView(T t, Packet obj);
		void pushView(T t, Object obj);
		int getTickInterval();
	}
	
}
