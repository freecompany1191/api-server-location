package com.o2osys.location.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 공통서비스
 * @author MS
 *
 */
@Service
public class CommonService {
    // 로그
    private final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);
    private final String TAG = CommonService.class.getSimpleName();

    /**
     * 에러로그를 뿌려준다
     * @param e
     */
    public void errorLog(String errorTag, Exception e) {
        LOGGER.error(errorTag, e);
        LOGGER.error(e.getMessage());
        LOGGER.error(e.toString());
        StackTraceElement[] ste = e.getStackTrace();
        for(int i = 0; i < ste.length; i++)
        {
            LOGGER.error(String.valueOf(ste[i]));
        }
    }
}
