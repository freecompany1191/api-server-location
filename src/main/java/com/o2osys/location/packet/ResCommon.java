package com.o2osys.location.packet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResCommon {

    /** 에러메세지 */
    @JsonProperty("ERROR_MSG")
    private String errorMsg;

}
