package org.tapchain.core;

/**
 * Created by hiro on 2015/05/13.
 */
public class ActorInputException extends ChainException {
    LinkType linkType;
    Actor actor;

	ActorInputException(Actor actor, ClassEnvelope ce, String errorMessage, LinkType linkType) {
        super(actor, ce, errorMessage, ActorInputErrorCode.fromLinkType(linkType));
        this.linkType = linkType;
        this.actor = actor;
    }

    public Actor getActor() {
        return actor;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public ClassEnvelope getClassEnvelopeInLink() {
        return (ClassEnvelope) getObject();
    }
}
