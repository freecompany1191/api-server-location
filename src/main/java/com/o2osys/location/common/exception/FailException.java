package com.o2osys.location.common.exception;

/**
   @FileName  : FailException.java
   @Description : 실패 예외처리
   @author      : KMS
   @since       : 2017. 10. 16.
   @version     : 1.0

   @개정이력

   수정일           수정자         수정내용
   -----------      ---------      -------------------------------
   2017. 10. 16.    KMS            최초생성

 */
public class FailException extends RuntimeException {

    /** long */
    private static final long serialVersionUID = 974718217733386545L;

    public FailException() {
        super("FailException");
    }

    public FailException(String message) {
        super(message);
    }

    public FailException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailException(Throwable cause) {
        super(cause);
    }

    public FailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
