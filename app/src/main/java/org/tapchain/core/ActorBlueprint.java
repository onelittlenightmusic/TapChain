package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;


public class ActorBlueprint extends Blueprint<Actor> implements IActorBlueprint {

	ClassEnvelope push;
	ClassEnvelope pull;
	ClassEnvelope parent;
	ClassEnvelope appearance;
	public ActorBlueprint() {
		super();
	}
	
	@Override
	public IBlueprint copy() {
		return new ActorBlueprint(this);
	}
	
	public ActorBlueprint(Blueprint ab, Actor... args) {
		super(ab, args);
	}

	public ActorBlueprint(Class<? extends IPiece> _cls, Actor... _args) {
		super(_cls, _args);
	}
	
	@Override
	protected Blueprint setBlueprintClass(Class<? extends IPiece> _cls) {
		super.setBlueprintClass(_cls);
		checkAndRegisterToActorLib();
		return this;
	}

	@Override

	protected Blueprint addLocalClass(Class<?> parent_type, Object parent_obj) {
		super.addLocalClass(parent_type, parent_obj);
		checkAndRegisterToActorLib();
		return this;
	}

	protected void checkAndRegisterToActorLib() {
		if(Actor.class.isAssignableFrom(cls)) {
			Class<? extends Actor> clsActor = (Class<? extends Actor>) cls;
			Actor.classLoadToLib(clsActor, this);
			setLinkClass(clsActor);
		}
	}

	protected void setLinkClass(Class<? extends Actor> cls) {
		for(LinkType ac: LinkType.values())
			setLinkClass(ac, Actor.getLinkClassFromLib(cls, ac));
	}

	private void setLinkClass(LinkType ac, ClassEnvelope envelope) {
		if(envelope == null)
			return;
//		ClassEnvelope envelope = new ClassEnvelope(classEnvelope);
		switch(ac) {
		case PUSH:
			push = envelope;
			break;
		case PULL:
			pull = envelope;
			break;
		case FROM_PARENT:
			parent = envelope;
			break;
		case TO_CHILD:
			appearance = envelope;
			break;
		}
		log("%s's %s setLinkedClasses to %s", cls.getSimpleName(), ac.toString(), (envelope==null)?"null":envelope.getSimpleName());
	}
	
	public ClassEnvelope getConnectClass(LinkType ac) {
		ClassEnvelope rtn = null;
		switch(ac) {
		case PUSH:

			rtn = push;
			break;
		case PULL:
			rtn = pull;
			break;
		case FROM_PARENT:
			rtn = parent;
			break;
		case TO_CHILD:
			rtn = appearance;
			break;
		}
		log(String.format("%s's %s getLinkedClasses is %s", getTag(), ac.toString(), (rtn==null)?"null":rtn.getSimpleName()));
		return rtn;
	}

	@Override
	public ClassEnvelope getLinkClasses(LinkType al) {
		ClassEnvelope rtn = Actor.__collectClass(getBlueprintClass(), al);
		log("%s's %s getLinkedClasses is %s", getTag(), al.toString(), (rtn==null)?"null":rtn.getSimpleName());
		return rtn;
	}
	
	
	@Override
	public Actor __newInstance(IManager<Actor, Actor> maker) throws ChainException {
		Actor rtn = (Actor) super.__newInstance(maker);
		if(rtn instanceof Actor)
			((Actor)rtn).setBlueprint(this);
		return rtn;
	}
	
	public void log(String format, String... l) {
//		Log.w("ActorBlueprint", String.format(format, l));
	}

    boolean logLevel = false;
    public void setLog() {
        logLevel = true;
    }

    @Override
    public Actor newInstance(IManager<Actor, Actor> maker) throws ChainException {
        Actor rtn = (Actor) super.newInstance(maker);
        if(logLevel)
            if(rtn instanceof Actor)
                ((Actor)rtn).setLogLevel(true);
        return rtn;
    }
}
