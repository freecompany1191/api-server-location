package com.o2osys.location.mapper;

import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

import com.o2osys.location.config.Master;

@Mapper
@Master
public interface LogMapper {
    /**
     * 에러로그 입력
     *
     * @param map
     */
    void spSyPutLogError(HashMap<String, Object> map);

    /**
     * 서버 배민 JSON 요청정보로그 입력
     *
     * @param map
     */
    void spSyPutLogTmpSvr(HashMap<String, Object> map);
}
