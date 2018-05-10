package com.o2osys.location.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.o2osys.location.packet.ReqMessage;
import com.o2osys.location.packet.ResMessage;

/**
   @FileName  : LocationService.java
   @Description : 카카오 API 주소 검색 서비스
   @author      : KMS
   @since       : 2017. 10. 16.
   @version     : 1.0

   @개정이력

   수정일          수정자         수정내용
   -----------     ---------      -------------------------------
   2017. 10. 16.    KMS            최초생성

 */
public interface LocationService {

    /**
     * 카카오 API 주소 검색
     * @Method Name : getAddress
     * @param req
     * @return
     * @throws Exception
     */
    public ResMessage getAddress(ReqMessage req, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
