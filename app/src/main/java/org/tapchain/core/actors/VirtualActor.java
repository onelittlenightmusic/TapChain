package org.tapchain.core.actors;

import org.tapchain.core.Actor;
import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IValue;
import org.tapchain.core.IValueLog;

public class VirtualActor extends Controllable implements IValue<String>, IValueLog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8815935158215335864L;
	static {
	}
	String realId;
	String url = "http://gbforonelittlenight.appspot.com/push";
	String value;
	
	public VirtualActor(Class<?> c) {
		super();
		setAutoStart();
		setAutoEnd();
//		setLogLevel(true);
//		once();
		this.realId = c.getSimpleName();
		try {
			c.newInstance();
		} catch (InstantiationException e) {
//			e.printStackTrace();
		} catch (IllegalAccessException e) {
//			e.printStackTrace();
		}
//		__setAssociatedClasses(c);
	}
	
	public VirtualActor(IBlueprint b) {
		this(b.getBlueprintClass());
	}
	
	@Override
	public boolean actorRun(Actor a) throws ChainException, InterruptedException {
		pushObject(realId, pull());
		return true;
	}

	public void pushObject(final String realId, final Object obj) {
	}
	
	@Override
	public boolean _set(String value) {
		this.value = value;
		return true;
	}

	@Override
	public String _get() {
		return value;
	}
	String dest = "";

	@Override
	public Object _valueLog() {
		return value;
	}


}
