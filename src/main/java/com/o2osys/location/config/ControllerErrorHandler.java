package com.o2osys.location.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerErrorHandler extends ResponseEntityExceptionHandler {
    // 로그
    private final Logger log = LoggerFactory.getLogger(ControllerErrorHandler.class);

    //log추가
    @Autowired
    private MessageSource mMessageSource;
    /*
    @ExceptionHandler(SQLException.class)
    public void sqlException(HttpServletRequest request, ServletResponse servletResponse, SQLException e)
            throws Exception {
        write((HttpServletResponse) servletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "디비 에러");
    }
     */

    /*
    @ExceptionHandler(Exception.class)
    public void exception(HttpServletRequest request, ServletResponse servletResponse, Exception e) throws Exception {
        write((HttpServletResponse) servletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
     */
    /*
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void methodArgumentNotValidException(MethodArgumentNotValidException ex, ServletResponse servletResponse,
            HttpServletRequest request) throws Exception {
        BindingResult bindingResult = ex.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();

        write((HttpServletResponse) servletResponse, HttpServletResponse.SC_BAD_REQUEST,
                getMessage(fieldError, request.getParameter(Define.Param.LANG_CODE)));
    }
     */

    @ExceptionHandler({HttpServerErrorException.class})
    public ResponseEntity<Object> httpClientErrorException(HttpServletRequest request, HttpServletResponse response, HttpServerErrorException ex) {
        log.error(ex.getClass().getSimpleName()+" ExceptionHandler : "+ex.getMessage(), ex.getStatusCode());
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(ex.getStatusCode().value());

        log.error(ex.getClass().getSimpleName()+" status code = "+ex.getStatusCode());
        log.error(ex.getClass().getSimpleName()+" error msg = "+ex.getMessage());

        //HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpStatus status  = HttpStatus.valueOf(ex.getStatusCode().value());

        return new ResponseEntity<Object>(ex.getMessage(), new HttpHeaders(), status);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<Object> httpClientErrorException(HttpServletRequest request, HttpServletResponse response, HttpClientErrorException ex) {
        log.error(ex.getClass().getSimpleName()+" ExceptionHandler : "+ex.getMessage(), ex.getStatusCode());
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(ex.getStatusCode().value());

        log.error(ex.getClass().getSimpleName()+" status code = "+ex.getStatusCode());
        log.error(ex.getClass().getSimpleName()+" error msg = "+ex.getMessage());

        //HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpStatus status  = HttpStatus.valueOf(ex.getStatusCode().value());

        return new ResponseEntity<Object>(ex.getMessage(), new HttpHeaders(), status);
    }

    /**
     * 에러메시지를가져온다.
     *
     * @param error
     * @param lang
     * @return
     */
    private String getMessage(FieldError error, String lang) {
        if (error == null) {
            return "";
        }

        return mMessageSource.getMessage(error.getDefaultMessage(), null, new Locale(lang));
    }

    /**
     * 에러메시지를 보여준다.
     *
     * @param response
     * @param code
     * @param message
     * @throws Exception
     */
    private void write(HttpServletResponse response, int code, String message) throws Exception {
        if (response == null) {
            return;
        }

        response.resetBuffer();
        response.setHeader("Content-Type", "text/html");
        response.setStatus(code);
        if (!StringUtils.isEmpty(message)) {
            response.getOutputStream().write(message.getBytes("utf-8"));
        }
        response.flushBuffer();
    }
}
