package org.tapchain.core;

import android.util.Log;

import java.util.Map;


public class ActorBlueprint extends Blueprint<Actor> implements IActorBlueprint {
    Map<LinkType, ClassEnvelope> links;

	public ActorBlueprint(Chain c) {
		super(c);
	}
	
	@Override
	public IBlueprint copy() {
		return new ActorBlueprint(this);
	}
	
	public ActorBlueprint(Blueprint ab, Actor... args) {
		super(ab, args);
	}

	public ActorBlueprint(Chain c, Class<? extends Actor> _cls, Actor... _args) {
		super(c, _cls, _args);
	}
//
//	@Override
//	protected Blueprint setBlueprintClass(Class<? extends Actor> _cls) {
//		super.setBlueprintClass(_cls);
////		register();
//		return this;
//	}
//
//	@Override
//
//	protected Blueprint addLocalClass(Class<?> parent_type, Object parent_obj) {
//		super.addLocalClass(parent_type, parent_obj);
////		register();
//		return this;
//	}

	public ClassEnvelope getConnectClass(LinkType lt) {
        if(links == null)
            return null;
        else
            return links.get(lt);
	}

//	@Override
//	public ClassEnvelope getLinkClasses(LinkType al) {
//		ClassEnvelope rtn = Actor.__collectClass(getBlueprintClass(), al);
//		log("%s's %s getLinkedClasses is %s", getTag(), al.toString(), (rtn==null)?"null":rtn.getSimpleName());
//		return rtn;
//	}

	
	@Override
	public Actor __newInstance() throws ChainException {
		Actor rtn = super.__newInstance();
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
    public Actor newInstance() throws ChainException {
        Actor rtn = super.newInstance();
        if(logLevel)
            rtn.setLogLevel(true);
        return rtn;
    }

    public void register() {
        links = Actor.classLoadToLib(this);
        if(logLevel)
            Log.w("ActorBlueprint", String.format("%s <= register", getTag()));
    }
}
