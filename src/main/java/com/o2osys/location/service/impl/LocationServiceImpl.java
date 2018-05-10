
package com.o2osys.location.service.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.o2osys.location.common.constants.Const;
import com.o2osys.location.common.exception.FailException;
import com.o2osys.location.common.exception.RequestException;
import com.o2osys.location.common.service.CommonService;
import com.o2osys.location.component.LocationKakaoComponent;
import com.o2osys.location.entity.kakao.Location;
import com.o2osys.location.packet.ReqMessage;
import com.o2osys.location.packet.ReqMessage.ReqBody;
import com.o2osys.location.packet.ReqMessage.ReqHeader;
import com.o2osys.location.packet.ResMessage;
import com.o2osys.location.packet.ResMessage.ResBody;
import com.o2osys.location.packet.ResMessage.ResHeader;
import com.o2osys.location.service.LocationService;

/**
   @FileName  : LocationServiceImpl.java
   @Description : 카카오 API 주소 검색 서비스
   @author      : KMS
   @since       : 2017. 10. 16.
   @version     : 1.0

   @개정이력

   수정일          수정자         수정내용
   -----------     ---------      -------------------------------
   2017. 10. 16.   KMS            최초생성
   2017. 12. 19.   KMS            KAKAO API I/O 에러 응답에 대한 재시도 로직 추가 처리

 */
@Service("LocationService")
public class LocationServiceImpl implements LocationService {

    // 로그
    private final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);
    private final String TAG = LocationServiceImpl.class.getSimpleName();

    /* 카카오 주소 API 호출 */
    @Autowired
    private LocationKakaoComponent locationKakaoComponent;

    /** 공통서비스 */
    @Autowired
    private CommonService commonService;

    private ObjectMapper mObjectMapper = new ObjectMapper();

    /**
     * 카카오 API 주소 검색
     * @Method Name : getAddress
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    public ResMessage getAddress(ReqMessage req, HttpServletRequest request, HttpServletResponse response) throws Exception {

        /*
        log.info("# req.getServerName() : " + request.getServerName());
        log.info("# req.getProtocol() : " + request.getProtocol());
        log.info("# req.getServerPort() : " + request.getServerPort());
        log.info("# req.getMethod() : " + request.getMethod());
        log.info("# req.getPathInfo() : " + request.getPathInfo());
        log.info("# req.getPathTranslated() : " + request.getPathTranslated());
        log.info("# req.getServletPath() : " + request.getServletPath());
        log.info("# req.getQueryString() : " + request.getQueryString());
        log.info("# req.getRemoteHost() : " + request.getRemoteHost());
        log.info("# req.getRemoteAddr() : " + request.getRemoteAddr());
        log.info("# req.getAuthType() : " + request.getAuthType());
        log.info("# req.getRemoteUser() : " + request.getRemoteUser());
        log.info("# req.getContentType() : " + request.getContentType());
        log.info("# req.getContentLength() : " + request.getContentLength());
         */

        ResMessage res = new ResMessage();
        ResHeader resHeader = new ResHeader();
        ResBody resBody = new ResBody();

        String traceNo = null;
        if(req == null) throw new RequestException("요청 데이터가 없습니다.");
        ReqHeader reqHeader = req.getHeader();
        ReqBody reqBody = req.getBody();

        if(req != null){
            if(req.getHeader() != null){
                if(req.getHeader().getTraceNo() != null) traceNo = req.getHeader().getTraceNo();
            }
        }

        if(reqHeader == null) throw new RequestException("요청 Header 데이터가 없습니다.");
        if(reqBody == null) throw new RequestException("요청 Body 데이터가 없습니다.");

        String serviceCd = reqHeader.getServiceCode();
        String address = reqBody.getAddress();

        log.info("================================================================");
        log.info("["+serviceCd+"] REMOTE ADDR : " + request.getRemoteAddr()+", PATH : "+request.getServletPath()+", CONTENT TYPE : "+request.getContentType());
        log.info("["+serviceCd+"] TRACE_NO : "+ traceNo +", REQUEST JSON : "+mObjectMapper.writeValueAsString(req));

        try {

            if (StringUtils.isEmpty(address)) throw new RequestException("요청 주소가 없습니다");


            //I/O 에러시 재시도
            Location loc = reTryAddrSearch(address);

            if(loc == null) throw new FailException("검색된 주소가 없습니다.");

            resHeader.setTraceNo(traceNo);
            resHeader.setResCode(Const.RES_CODE.OK);
            resHeader.setResMsg(Const.RES_CODE_NAME.OK);
            resBody.setLocation(loc);
            res.setHeader(resHeader);
            res.setBody(resBody);

            log.info("["+serviceCd+"] TRACE_NO : "+ traceNo +", RESPONSE JSON : "+mObjectMapper.writeValueAsString(res));
            log.info("================================================================");

            return res;

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR_MSG : "+e.getMessage());
            throw e;

        } catch (FailException e) {
            log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR_MSG : "+e.getMessage());
            resHeader.setTraceNo(traceNo);
            resHeader.setResCode(Const.RES_CODE.FAIL);
            resHeader.setResMsg(Const.RES_CODE_NAME.FAIL);
            resBody.setErrorMsg(e.getMessage());
            res.setHeader(resHeader);
            res.setBody(resBody);

            String resMsgJson = mObjectMapper.writeValueAsString(res);

            if(serviceCd != null)
                log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR RES JSON : "+resMsgJson);

            return res;

        } catch (RequestException e) {
            log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR_MSG : "+e.getMessage());
            resHeader.setTraceNo(traceNo);
            resHeader.setResCode(Const.RES_CODE.REQ_ERROR);
            resHeader.setResMsg(Const.RES_CODE_NAME.REQ_ERROR);
            resBody.setErrorMsg(e.getMessage());
            res.setHeader(resHeader);
            res.setBody(resBody);

            String resMsgJson = mObjectMapper.writeValueAsString(res);

            if(serviceCd != null)
                log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR RES JSON : "+resMsgJson);

            return res;

        } catch (Exception e) {

            log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR_MSG : "+e.getMessage());
            commonService.errorLog(TAG, e);
            resHeader.setTraceNo(traceNo);
            resHeader.setResCode(Const.RES_CODE.SYSTEM_ERROR);
            resHeader.setResMsg(Const.RES_CODE_NAME.SYSTEM_ERROR);
            resBody.setErrorMsg(e.getMessage());
            res.setHeader(resHeader);
            res.setBody(resBody);

            String resMsgJson = mObjectMapper.writeValueAsString(res);

            if(serviceCd != null)
                log.error("["+serviceCd+"] TRACE_NO : "+ traceNo +", ERROR RES JSON : "+resMsgJson);

            return res;

        }

    }

    /**
     * I/O 에러시 재시도 로직
     * @Method Name : reTrySearch
     * @param address
     * @return
     * @throws Exception
     */
    private Location reTryAddrSearch(String address) throws Exception{

        int retry = 4;    //재시도 횟수
        int delay = 1000; //재시도 딜레이 타임

        Location loc = null;
        while(retry > 0){

            try {

                loc = locationKakaoComponent.getAddr(address);

                /*
                boolean test = true;
                if(test){
                    throw new ResourceAccessException("I/O Error TEST");
                }
                 */

                return loc;

            } catch (ResourceAccessException e){
                //I/O error on GET request for "https://dapi.kakao.com/v2/local/search/address.json": dapi.kakao.com; nested exception is java.net.UnknownHostException: dapi.kakao.com
                retry--;
                log.error("[LOCATION] REST_CONNECT_FAIL Exception Error :: "+e.getMessage());
                log.error("[LOCATION] REST_CONNECT_FAIL RETRY COUNT :: "+retry);
                if(retry == 0){
                    throw new FailException("KAKAO ADDRESS API Exception :: "+e.getMessage());
                }
                Thread.sleep(delay);
            }

        }

        return loc;

    }
}