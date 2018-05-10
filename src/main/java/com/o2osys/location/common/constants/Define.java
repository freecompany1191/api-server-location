package com.o2osys.location.common.constants;

public interface Define {
    /**
     * reqeust parameter 변수 명 정의
     */
    interface Param {
        String X_AUTH_TOKEN = "x-auth-token"; // token
        String LANG_CODE = "lang_code"; // lang code
    }

    /**
     * 응답 코드 정의
     */
    interface HttpStatus {
        interface Code {
            int OK = 200;
            int BAD_REQUEST = 400;
            int FORBIDDEN = 403;
            int UNAUTHORIZED = 401;
            int NOT_FOUND = 404;
            int SERVER_ERROR = 500;
        }

        interface Message {
            String OK = "성공";
            String BAD_REQUEST = "Bad Request-field validation 실패";
            String FORBIDDEN = "Forbidden 서버 접근 금지";
            String UNAUTHORIZED = "Unauthorized-API 인증,인가 실패";
            String NOT_FOUND = "Not Found-해당 리소스가 없음";
            String SERVER_ERROR = "Internal Server Error-서버에러";
        }
    }

    String CONTENT_TYPE = "application/json; charset=utf-8";
    String LANG_CODE = "ko";

    /**
     * 프로시져 응답 정의 코드
     */
    interface ProcedureStatus {
        interface Code {
            int OK = 1;
            int FAIL = 2;
            int SYSTEM_ERROR = 0;
        }
    }

    /** 좌표 정확도 (1: 높음, 2: 중간, 3: 낮음) */
    interface XyAccType {
        /** 1: 높음 */
        String TYPE_1 = "1"; // 높음
        /** 2: 중간 */
        String TYPE_2 = "2"; // 중간
        /** 3: 낮음 */
        String TYPE_3 = "3"; // 낮음
    }

    interface DaumHttpStatus {
        interface Code {
            int TOO_MANY_REQUEST = 429;
        }

        interface Message {
            String TOO_MANY_REQUEST = "TOO MANY REQUEST";
        }
    }


    interface SYSTEM{
        String TYPE = "LOC";
    }

}
