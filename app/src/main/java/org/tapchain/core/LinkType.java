package org.tapchain.core;

public enum LinkType {
	PULL(PathType.OFFER, false), PUSH(PathType.OFFER, true),
	FROM_PARENT(PathType.FAMILY, false), TO_CHILD(PathType.FAMILY, true);
	PathType _pathType;
	boolean _out = false;

	LinkType(PathType pathType, boolean out) {
		_pathType = pathType;
		_out = out;
	}

	public LinkType reverse() {
		switch(this) {
		case PUSH:
			return PULL;
		case PULL:
			return PUSH;
		case FROM_PARENT:
			return TO_CHILD;
		case TO_CHILD:
			return FROM_PARENT;
		}
		return PUSH;
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

	public static LinkType fromPathType(PathType pathType, boolean out) throws Chain.ChainException {
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
				throw new Chain.ChainException("No LinkType for this PathType", "LinkType", pathType);
		}
	}

	public boolean getOutOrIn() {
		return _out;
	}

	public static LinkType fromPathPack(PathPack<?> pathPack) throws Chain.ChainException {
			return fromPathType(pathPack.getPathType(), pathPack instanceof PathPack.OutPathPack);
	}

}