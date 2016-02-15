package org.tapchain.core;


public interface IActorBlueprint extends IBlueprint<Actor> {
	ClassEnvelope getConnectClass(LinkType appearance);
//	ClassEnvelope getLinkClasses(LinkType al);
}
