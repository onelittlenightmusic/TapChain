package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;


public class ActorBlueprint extends Blueprint<Actor> implements IActorBlueprint {

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
		}
	}

	public ClassEnvelope getConnectClass(LinkType ac) {
        return Actor.getLinkClassFromLib(cls, ac);
	}

	@Override
	public ClassEnvelope getLinkClasses(LinkType al) {
		ClassEnvelope rtn = Actor.__collectClass(getBlueprintClass(), al);
		log("%s's %s getLinkedClasses is %s", getTag(), al.toString(), (rtn==null)?"null":rtn.getSimpleName());
		return rtn;
	}
	
	
	@Override
	public Actor __newInstance(IManager<Actor, Actor> maker) throws ChainException {
		Actor rtn = super.__newInstance(maker);
        rtn.setBlueprint(this);
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
        Actor rtn = super.newInstance(maker);
        if(logLevel)
            rtn.setLogLevel(true);
        return rtn;
    }
}
