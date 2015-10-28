package org.tapchain.core;


public interface IActorBlueprint extends IBlueprint<Actor> {
	public ClassEnvelope getConnectClass(LinkType appearance);
	ClassEnvelope getLinkClasses(LinkType al);

}
