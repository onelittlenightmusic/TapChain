package org.tapchain.core;

/**
 * Created by hiro on 2016/02/23.
 */
public class ChainException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String errorMessage = "";
    String location = "";
    Chain.IErrorCode errorCode = Chain.PieceErrorCode.LOOPABLE;
    Object target = null;

    ChainException() {
        super("ChainException: Unknown Error");
        errorMessage = "Unknown";
    }

    public ChainException(String str) {
        super(str);
        errorMessage = "Unknown";
    }

    public ChainException(String errorMessage, String location, Object target) {
        this(String.format("ChainException: %s on %s", errorMessage, location));
        this.errorMessage = errorMessage;
        this.location = location;
        this.target = target;
    }

    public ChainException(IPiece piece, String str, Chain.IErrorCode _loop) {
        this("ChainException: " + str);
        errorMessage = str;
        location = piece.getName();
        errorCode = _loop;
        target = piece;
    }

    public ChainException(IPiece piece, ClassEnvelope class1, String str, Chain.IErrorCode _loop) {
        this(piece, str, _loop);
        target = class1;
    }

    public ChainException(IPiece piece, String str) {
        this(piece, str, Chain.PieceErrorCode.LOOPABLE);
    }

    public ChainException(Connector connector, String str) {
        this("ChainException: " + str);
        errorMessage = str;
        location = "Path";
        target = connector;
    }

    ChainException(IManager<?, ?> manager, String str) {
        this("ChainException: " + str);
        errorMessage = str;
        location = "Manager";
        target = manager;
    }

    ChainException(IPiece piece, String str, Throwable throwable) {
        super("ChainException: " + str, throwable);
        errorMessage = str;
        location = piece.getName();
        target = piece;
    }

    public ChainException(Factory pieceFactory, String str) {
        this("ChainException: " + str);
        errorMessage = str;
        location = "Factory";
        target = pieceFactory;
    }

    public String getLocation() {
        return location;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Chain.IErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getObject() {
        return target;
    }

}
