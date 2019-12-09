
package com.guosen.zebra.core.exception;

public class ProtobufException extends JException {

    private static final long serialVersionUID = -4951252714490754149L;

    public ProtobufException(Exception exception){
        super(exception);
    }

    public ProtobufException(String string){
        super(string);
    }

    public ProtobufException(String string, Exception exception){
        super(string, exception);
    }
}
