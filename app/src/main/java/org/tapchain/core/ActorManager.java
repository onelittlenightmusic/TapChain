package org.tapchain.core;


import org.tapchain.core.Actor.IConsumer;
import org.tapchain.core.Actor.IDesigner;
import org.tapchain.core.Actor.IEffector;
import org.tapchain.core.Actor.IFunc;
import org.tapchain.core.Actor.IGenerator;
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
			IErrorHandler _error, 
			ILogHandler _log)
	{
		this();
		this
		.setLog(_log)
		.setError(_error)
		;
	}
	
	public ActorManager(ActorManager am) {
		this(
                am.error,
                am.log
        );

	}
	@Override
	public ActorManager createChain() {
		setChain(new ActorChain());
		return this;
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
		((Actor)bp).onAdd(newSession());
		_move(bp);
		if(getRoot() != null) {
			super.append(bp, PathType.FAMILY, getRoot(), PathType.FAMILY, false);
		}
		return this;
	}
	
	public ActorManager addActor(IActor actor) {
		return add(new Actor().setActor(actor));
	}
	
	@SuppressWarnings("serial")
	public <VALUE, INPUT, OUTPUT> ActorManager addDesigner(final IDesigner<VALUE, INPUT, OUTPUT> designer) {
		Actor adding = null;
		if(designer instanceof IFunc)
			adding = new Actor.Filter<VALUE, INPUT, OUTPUT>() {
				@Override
				public OUTPUT func(IValue<VALUE> val, INPUT in) {
					return ((IFunc<VALUE, INPUT, OUTPUT>)designer).func(val, in);
				}

				@Override
				public void init(IValue<VALUE> val) {
					designer.init(val);
				}
			};
		else if(designer instanceof IConsumer)
			adding = new Actor.ValueConsumer<VALUE>() {
				@Override
				public void init(IValue<VALUE> val) {
					designer.init(val);
				}

				@Override
				public void consume(VALUE in) {
					((IConsumer<VALUE>)designer).consume(in);
				}
			};
		else if(designer instanceof IGenerator)
			adding = new Actor.Generator<VALUE>() {
				@Override
				public void init(IValue<VALUE> val) {
					designer.init(val);
				}

				@Override
				public VALUE generate() {
					return ((IGenerator<VALUE>)designer).generate();
				}
			};
		if(adding != null)
			add(adding);
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
	public ActorManager remove(Actor piece) {
		if (piece == null)
			return this;
		unsetPieceView(piece);
		((Actor)piece).onRemove(newSession());
		for (Actor cp : piece.getPartners()) {
			disconnect(piece, cp);
		}
		super.remove(piece);
		piece.end();
		return this;
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
	
	@Override
	public ActorManager refreshPieceView(Actor bp, Actor obj) {
		return this;
	}

	public interface IStatusHandler<T> {
        void changeViewState(PieceState state);
		int tickView(T t, Packet obj);
		void pushView(T t, Object obj);

		int getTickInterval();
	}
	
}
