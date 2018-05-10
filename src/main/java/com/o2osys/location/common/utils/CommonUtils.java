package com.o2osys.location.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.o2osys.location.common.constants.Const;
import com.o2osys.location.common.constants.Define.ProcedureStatus;
import com.o2osys.location.common.exception.DbProcedureException;

/**
   @FileName  : CommonUtil.java
   @Description : 공통유틸
   @author      : KMS
   @since       : 2017. 7. 21.
   @version     : 1.0

   @개정이력

   수정일           수정자         수정내용
   -----------      ---------      -------------------------------
   2017. 7. 21.     KMS            최초생성

 */
@Component
public class CommonUtils {
    // 로그
    private final Logger log = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * Object -> JSON 형식으로 변환
     *
     * @Method Name : jsonStringFromObject
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public static String jsonStringFromObject(Object object) throws JsonProcessingException {

        if(object == null){
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    /**
     * 날짜 형식 변경
     * yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss
     * @Method Name : strToDate
     * @param strDate
     * @return
     */
    public String strToDate(String strDate) {

        if(StringUtils.isEmpty(strDate)){
            return null;
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.KOREA);
        LocalDateTime toDate = LocalDateTime.parse(strDate,pattern);

        pattern = DateTimeFormatter.ofPattern("yyyy.MM.dd HH;mm;ss", Locale.KOREA);
        String formatStr = pattern.format(toDate);

        return formatStr;
    }

    /**
     * 현재시간 (YYYYMMDDHH24MISS)
     *
     * @Method Name : getNowTime
     * @return
     */
    public static String getNowTime(){

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        LocalDateTime now = LocalDateTime.now();
        String nowTime = now.format(format);

        return nowTime;
    }

    /**
     * 배달대행 전문추적번호 생성(YYYYMMDDHH24MISS + Random 자릿수 지정)
     *
     * @Method Name : getTraceNo
     * @return
     */
    public static String getTraceNo(int num){

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        //입력된 숫자만큼 Random 자릿수 셋팅
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<num;i++) {
            sb.append("9");
        }
        int cnt = Integer.valueOf(sb.toString());

        Random rn = new Random();
        String rnum = String.format("%0"+num+"d", rn.nextInt(cnt));

        LocalDateTime now = LocalDateTime.now();
        String nowTime = now.format(format);
        String traceNo = nowTime + rnum;

        return traceNo;
    }


    public void DbProcedureChk (Map<String, Object> map, String title) throws Exception{

        if(map.get("out_CODE") == null){

            throw new Exception("[MCS] : ["+title+"] :: out_CODE is Null");

        }else if(!NumberUtils.isDigits( String.valueOf(map.get("out_CODE")).trim() )){

            throw new Exception("[MCS] : ["+title+"] :: out_CODE is Not Number : "+map.get("out_CODE"));

        }

        int outCode = (int) map.get("out_CODE");
        log.debug("### out_CODE : "+outCode);

        if (ProcedureStatus.Code.OK != outCode) {

            switch(outCode){
                case ProcedureStatus.Code.FAIL :
                    // DB 에러처리 예외
                    throw new DbProcedureException(map.get("out_MSG").toString());
                default :
                    throw new Exception(map.get("out_MSG").toString());
            }
        }
    }


    /**
     * 공통 전문 헤더 설정
     * @Method Name : CodeToName
     * @param type
     * @param code
     * @return
     */
    public static String CodeToName(String type, String code){

        String name = "";
        switch(type){

            /** REST 응답 코드 */
            case "RES_CODE" :

                switch(code){
                    case Const.RES_CODE.OK :
                        name = Const.RES_CODE_NAME.OK;
                        break;
                    case Const.RES_CODE.FAIL :
                        name = Const.RES_CODE_NAME.FAIL;
                        break;
                    case Const.RES_CODE.REQ_ERROR :
                        name = Const.RES_CODE_NAME.REQ_ERROR;
                        break;
                    case Const.RES_CODE.SYSTEM_ERROR :
                        name = Const.RES_CODE_NAME.SYSTEM_ERROR;
                        break;
                }
                break;

        }
        return name;
    }

}
