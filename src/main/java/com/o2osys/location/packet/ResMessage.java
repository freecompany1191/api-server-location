package com.o2osys.location.packet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.o2osys.location.entity.kakao.Location;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResMessage {

    @JsonProperty("header")
    private ResHeader header;

    @JsonProperty("body")
    private ResBody body;

    @Data
    public static class ResHeader {

        /** 전문추적번호 - YYYYMMDDHH24MISS + Random3자리 */
        @JsonProperty("TRACE_NO")
        private String traceNo;

        /** 응답코드 실패: 0, 성공: 1 */
        @JsonProperty("RES_CODE")
        private String resCode;

        /** 응답메세지 */
        @JsonProperty("RES_MSG")
        private String resMsg;
    }

    @Data
    public static class ResBody extends ResCommon {

        /** 응답주소 */
        @JsonProperty("LOCATION")
        private Location location;

    }
}
