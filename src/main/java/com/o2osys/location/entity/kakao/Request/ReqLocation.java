package com.o2osys.location.entity.kakao.Request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReqLocation {

    @NotNull
    @JsonProperty("address")
    private String address;

}