package org.tapchain.core;

/**
 * Created by hiro on 2015/05/13.
 */
public class ActorPullException extends ActorInputException {
    ActorPullException(Actor actor, ClassEnvelope ce, String errorMessage) {
        super(actor, ce, errorMessage, LinkType.PULL);
    }
}
