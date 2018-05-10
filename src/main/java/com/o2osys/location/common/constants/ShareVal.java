package com.o2osys.location.common.constants;

import java.util.List;

import lombok.Data;

/**
   @FileName  : DongArr.java
   @Description : 중복동 처리를 위한 공유 변수
   @author      : KMS
   @since       : 2017. 11. 16.
   @version     : 1.0

   @개정이력

   수정일          수정자         수정내용
   -----------     ---------      -------------------------------
   2017. 11. 16.    KMS            최초생성

 */
@Data
public class ShareVal {

    /** 중복동 처리용 공유 배열 변수 */
    public static List<String> dongArr;

    /** 중복 시구군 처리용 공유 배열 변수 */
    public static List<String> sigugunArr;

    /** 패턴미적용 조회주소(조회된 주소가 있을 때만 저장) */
    public static String noPatternAddressName;

}
