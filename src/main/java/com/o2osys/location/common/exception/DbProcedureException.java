package com.o2osys.location.common.exception;

/**
   @FileName  : RestConnectException.java
   @Description : REST 서비스 예외처리
   @author      : KMS
   @since       : 2017. 8. 28.
   @version     : 1.0

   @개정이력

   수정일           수정자         수정내용
   -----------      ---------      -------------------------------
   2017. 8. 28.     KMS            최초생성

 */
public class DbProcedureException extends RuntimeException {

    /** long */
    private static final long serialVersionUID = -5140351663276021125L;

    public DbProcedureException() {
        super("DbProcedureException");
    }

    public DbProcedureException(String message) {
        super(message);
    }

    public DbProcedureException(String message, Throwable cause) {
        super(message, cause);
    }

    public DbProcedureException(Throwable cause) {
        super(cause);
    }

    public DbProcedureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
