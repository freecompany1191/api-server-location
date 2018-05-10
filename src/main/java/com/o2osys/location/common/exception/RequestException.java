package com.o2osys.location.common.exception;

/**
   @FileName  : RequestException.java
   @Description : Request 서비스 예외처리
   @author      : KMS
   @since       : 2017. 10. 16.
   @version     : 1.0

   @개정이력

   수정일           수정자         수정내용
   -----------      ---------      -------------------------------
   2017. 10. 16.    KMS            최초생성

 */
public class RequestException extends RuntimeException {

    /** long */
    private static final long serialVersionUID = 974718217733386545L;

    public RequestException() {
        super("RequestException");
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestException(Throwable cause) {
        super(cause);
    }

    public RequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
