package com.o2osys.location.packet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReqMessage {

    @JsonProperty("header")
    private ReqHeader header;

    @JsonProperty("body")
    private ReqBody body;

    @Data
    public static class ReqHeader {

        /** 전문추적번호 - YYYYMMDDHH24MISS + Random3자리 */
        @JsonProperty("TRACE_NO")
        private String traceNo;

        /** 각 기능별 Service Code */
        @JsonProperty("SERVICE_CODE")
        private String serviceCode;

    }

    @Data
    public static class ReqBody {

        /** 요청주소 */
        @JsonProperty("ADDRESS")
        private String address;

    }

}