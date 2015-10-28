package org.tapchain.core;

/**
 * Created by hiro on 2015/05/13.
 */
public enum ActorInputErrorCode implements Chain.IErrorCode {
    PULL_LOCKED(LinkType.PULL),
    FROM_PARENT_LOCKED(LinkType.FROM_PARENT);

    LinkType lockedLinkType;

    ActorInputErrorCode(LinkType linkType) {
        lockedLinkType = linkType;
    }

    @Override
    public boolean isLocked() {
        return true;
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

    @Override
    public PathType getPathTypeLocked() {
        return lockedLinkType.getPathType();
    }

    public LinkType getLinkType() {
        return lockedLinkType;
    }

    public static ActorInputErrorCode fromLinkType(LinkType linkType) {
        switch (linkType) {
            case PULL:
                return PULL_LOCKED;
            case PUSH:
            case FROM_PARENT:
                return FROM_PARENT_LOCKED;
        }
        return null;
    }
}
