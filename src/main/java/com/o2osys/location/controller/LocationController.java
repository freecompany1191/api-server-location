package com.o2osys.location.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.o2osys.location.common.exception.FailException;
import com.o2osys.location.common.exception.RequestException;
import com.o2osys.location.common.service.CommonService;
import com.o2osys.location.component.LocationKakaoComponent;
import com.o2osys.location.entity.kakao.Location;
import com.o2osys.location.packet.ReqMessage;
import com.o2osys.location.packet.ResMessage;
import com.o2osys.location.packet.ResMessage.ResBody;
import com.o2osys.location.service.LocationService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/v1")
public class LocationController {
    // 로그
    private final Logger log = LoggerFactory.getLogger(LocationController.class);
    private final String TAG = LocationController.class.getSimpleName();

    /** 공통서비스 */
    @Autowired
    private LocationService locationService;

    @Autowired
    LocationKakaoComponent locationKakaoComponent;

    @Autowired
    CommonService commonService;

    @ApiOperation(value = "KAKAO 주소 검색 서비스", notes = "KAKAO 주소 검색 서비스", response = ResMessage.class)
    //    @PutMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    //    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    //    @RequestMapping(path = "/{address}", method = RequestMethod.GET, produces = Define.CONTENT_TYPE)
    @PostMapping(path = "/location", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResMessage getLocation(
            //@RequestHeader(value = Define.Param.X_AUTH_TOKEN) String token,
            //@Validated @RequestBody ReqLocation reqLocation,
            //@PathVariable(value = "address") String address
            //Exception ex,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody ReqMessage reqMessage
            ) throws Exception {

        ResMessage resMessage = new ResMessage();

        try {
            /*
            boolean throwException = true;

            if (throwException) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            }
             */
            resMessage = locationService.getAddress(reqMessage, request, response);

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            //HTTP 에러 발생시 ErrorHandler로 객체 전달
            throw e;
        }

        return resMessage;
    }

    @ApiOperation(value = "KAKAO 주소 검색 서비스 테스트", notes = "KAKAO 주소 검색 서비스 테스트", response = ResBody.class)
    @GetMapping(path = "/address/{address}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResBody getAddress(
            @PathVariable(value = "address") String address
            ) throws Exception {

        ResBody resBody = new ResBody();
        //long startTime = logUtil.startlog(log);

        try {

            if (StringUtils.isEmpty(address)) {
                throw new RequestException("검색할 주소가 없습니다.");
            }

            Location loc = locationKakaoComponent.getAddr(address);
            if(loc == null){
                throw new FailException("검색된 주소가 없습니다.");
            }

            resBody.setLocation(loc);

            return resBody;

        } catch (HttpServerErrorException e) {
            throw e;

        } catch (RequestException | FailException e) {
            log.error(e.getMessage());
            resBody.setErrorMsg(e.getMessage());
            return resBody;

        } catch (Exception e) {
            commonService.errorLog(TAG, e);
            resBody.setErrorMsg(e.getMessage());
            return resBody;
        }

        //logUtil.endlog(log, startTime);

    }

    @ApiOperation(value = "KAKAO 주소 검색 서비스 서버 에러 테스트", notes = "KAKAO 주소 검색 서버 에러 테스트", response = ResBody.class)
    @PostMapping(path = "/error/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResBody getErrorTest(
            @RequestBody ReqMessage reqMessage,
            @PathVariable(value = "type") String type
            ) throws Exception {

        ResBody resBody = new ResBody();
        //long startTime = logUtil.startlog(log);

        /*
        try {
            resMessage = locationService.getAddress(reqMessage, request, response);
        } catch (HttpServerErrorException e) {
            throw new HttpServerErrorException(e.getStatusCode());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode());
        }
         */

        /*
        boolean throwException = true;

        if (throwException) {
            throw new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
        }
         */

        if(type != null){

            try {
                HttpStatus status = null;
                if(StringUtils.isNumeric(type)){
                    status = HttpStatus.valueOf(Integer.valueOf(type));
                }

                throw new HttpServerErrorException(status);

            } catch (HttpServerErrorException e) {

                log.error("ERROR : "+type.toString());
                commonService.errorLog(TAG, e);
                throw e;

            }catch (Exception e) {

                log.error("ERROR : "+type.toString());
                commonService.errorLog(TAG, e);
                throw e;
            }
        }

        try {

            if (reqMessage == null) {
                throw new RequestException("검색할 주소가 없습니다.");
            }

            if (StringUtils.isEmpty(reqMessage.getBody().getAddress())) {
                throw new RequestException("검색할 주소가 없습니다.");
            }

            Location loc = locationKakaoComponent.getAddr(reqMessage.getBody().getAddress());
            if(loc == null){
                throw new FailException("검색된 주소가 없습니다.");
            }

            resBody.setLocation(loc);

            return resBody;

        } catch (RequestException | FailException e) {
            log.error(e.getMessage());
            resBody.setErrorMsg(e.getMessage());
            return resBody;

        } catch (Exception e) {
            commonService.errorLog(TAG, e);
            resBody.setErrorMsg(e.getMessage());
            return resBody;
        }

        //logUtil.endlog(log, startTime);

    }
}