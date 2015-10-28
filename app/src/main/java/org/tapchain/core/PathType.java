package org.tapchain.core;

/**
 * Created by hiro on 2015/05/08.
 */
public enum PathType {
    PASSTHRU(), OFFER(Chain.PieceErrorCode.LOCK_OFFER),
    FAMILY(Chain.PieceErrorCode.LOCK_FAMILY), EVENT();
    private Chain.PieceErrorCode errorCode = Chain.PieceErrorCode.LOCK_OTHER;

	PathType() {
    }

	PathType(Chain.PieceErrorCode err) {
        this();
        this.errorCode = err;
    }

	public String toString() {
        return name();
    }

	public boolean isFamily() {
        return this.equals(FAMILY);
    }

	public Chain.IErrorCode getErrorCode() {
        return errorCode;
    }
}
