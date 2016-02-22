package org.tapchain.core;

import java.util.EnumMap;

public enum LinkType {
	PULL(PathType.OFFER, false), PUSH(PathType.OFFER, true),
	FROM_PARENT(PathType.FAMILY, false), TO_CHILD(PathType.FAMILY, true);
	PathType _pathType;
	boolean _out = false;

    private static final EnumMap<LinkType, LinkType> reverse = new EnumMap<>(LinkType.class);
    static {
        reverse.put(PULL, PUSH);
        reverse.put(PUSH, PULL);
        reverse.put(FROM_PARENT, TO_CHILD);
        reverse.put(TO_CHILD, FROM_PARENT);
    }

	LinkType(PathType pathType, boolean out) {
		_pathType = pathType;
		_out = out;
	}

	public LinkType reverse() {
        return reverse.get(this);
	}
	
	public LinkBooleanSet getBooleanSet() {
		switch(this) {
		case PUSH:
			return LinkBooleanSet.PUSH;
		case PULL:
			return LinkBooleanSet.PULL;
		case FROM_PARENT:
			return LinkBooleanSet.PARENT;
		case TO_CHILD:
			return LinkBooleanSet.APPEARANCE;
		}
		return LinkBooleanSet.NULL;
	}

	public PathType getPathType() {
		return _pathType;
	}

	public static LinkType fromPathType(PathType pathType, boolean out) throws ChainException {
		switch(pathType) {
			case FAMILY:
				if(out)
					return TO_CHILD;
				else
					return FROM_PARENT;
			case OFFER:
				if(out)
					return PUSH;
				else
					return PULL;
			default:
				throw new ChainException("No LinkType for this PathType", "LinkType", pathType);
		}
	}

	public boolean getOutOrIn() {
		return _out;
	}

	public static LinkType fromPathPack(PathPack<?> pathPack) throws ChainException {
			return fromPathType(pathPack.getPathType(), pathPack instanceof PathPack.OutPathPack);
	}

}