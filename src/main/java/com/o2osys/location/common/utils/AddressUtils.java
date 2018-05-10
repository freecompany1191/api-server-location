package com.o2osys.location.common.utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.o2osys.location.common.constants.ADDRESS_PATTERN;
import com.o2osys.location.common.constants.ShareVal;
import com.o2osys.location.entity.kakao.Location;

/**
   @FileName  : AddressUtils.java
   @Description : 주소 유틸
   @author      : KMS
   @since       : 2017. 11. 16.
   @version     : 1.0

   @개정이력

   수정일          수정자         수정내용
   -----------     ---------      -------------------------------
   2017. 11. 16.   KMS            최초생성
   2017. 12. 19.   KMS            기본 정규식 패턴 매칭 STEP5 앞 또는 뒤에 공백이 있는 최소길이 에 부합하는 시도 시구군 동 제거하도록 로직 수정

 */
@Component
public class AddressUtils {
    // 로그
    private final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    /**
     * 기본 정규식 패턴 매칭
     * @Method Name : getMatch
     * @param p
     * @param target
     * @return
     */
    public String getMatchDefault(String target) throws Exception {

        //STEP1 - [문자] or (문자) or {문자} 제거
        target = getMatchRoopOut(ADDRESS_PATTERN.DEFAULT.BRACKET_STR.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP1 적용 : "+ target);

        //STEP2 - 특수문자 제거
        target = getMatchRoopOut(ADDRESS_PATTERN.DEFAULT.SPECIAL_STR.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP2 적용 : "+ target);

        //STEP3 - 시군구 패턴 적용
        target = getMatch(ADDRESS_PATTERN.DEFAULT.SIGUGUN.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP3 적용 : "+ target);

        //STEP4 - 중복 시도 시군구 읍면동가리로길 제거
        target = getMatchdupleOut(ADDRESS_PATTERN.DEFAULT.DUPLE_SIGUGUN.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP4 적용 : "+ target);

        //STEP5 - 앞 또는 뒤에 공백이 있는 시군구와 번지,도로명 분리
        target = getMatchAddBlank(ADDRESS_PATTERN.DEFAULT.SIGUGUN_ROAD_ADD_BLANK.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP5 적용 : "+ target);

        //STEP6 - 아파트와 동또는 번지 분리 패턴 적용
        target = getMatchAddBlank(ADDRESS_PATTERN.DEFAULT.APT_BLANK.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP6 적용 : "+ target);

        //STEP7 - 아파트동과 호 분리
        target = getMatchAddBlank(ADDRESS_PATTERN.DEFAULT.APT_DONG_BLANK.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP7 적용 : "+ target);

        //STEP8 - 동번지일 경우 띄어쓰기 해주는 패턴 적용
        target = getMatchAddBlank(ADDRESS_PATTERN.DEFAULT.DONG_BUNGI.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP8 적용 : "+ target);

        //STEP9 - 중복동 제거
        target = getMatchdupleDongOut(ADDRESS_PATTERN.DEFAULT.DUPLE_DONG.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP9 적용 : "+ target);

        //STEP10 - 번지에 문자가 붙어있을 경우 띄어쓰기 해주는 패턴 적용
        target = getMatchAddBlank(ADDRESS_PATTERN.DEFAULT.BUNGI_BLANK.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP10 적용 : "+ target);

        //STEP11 - 번지에 문자가 붙어있을 경우 띄어쓰기 해주는 패턴2 적용
        target = getMatchAddBlank(ADDRESS_PATTERN.DEFAULT.BUNGI_BLANK2.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP11 적용 : "+ target);

        //STEP12 - 층 호 제거 :: ex)주소에 9999층 9999호 제거
        target = getMatchRoopOut(ADDRESS_PATTERN.DEFAULT.FLOOR_OUT.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP12 적용 : "+ target);

        //STEP13 - 블랭크 제거
        target = getMatchOutBlank(ADDRESS_PATTERN.DEFAULT.BLANK_OUT.getPettern(), target);
        log.info("@@ 주소 기본패턴 STEP13 적용 : "+ target);

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭된 패턴과 일치할때는 일치한것 만 아니면 그대로)
     * @Method Name : getMatch
     * @param p
     * @param target
     * @return
     */
    public String getMatch(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        if (m.find()) target=m.group();

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭된 패턴과 일치한 것만 가져옴 일치하지 않으면 null)
     * @Method Name : getMatchOnly
     * @param p
     * @param target
     * @return
     */
    public String getMatchOnly(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        if (m.find()) target=m.group();
        else target = null;

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭된 패턴 제거)(공백추가)
     * @Method Name : getMatchOut
     * @param p
     * @param target
     * @return
     */
    public String getMatchOut(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        target =  m.replaceAll(" ");

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭된 패턴 제거)(Target 길이에 따라 적용)
     * @Method Name : getMatchOutLength
     * @param p
     * @param target
     * @param len
     * @return
     */
    public String getMatchOutLength(Pattern p, String target, int len) throws Exception {

        Matcher m = p.matcher(target);

        if(len == 0){
            len = 9;
        }

        if(target.length() > len){

            target =  m.replaceAll(" ");

        }

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭된 패턴 제거)(공백제거)
     * @Method Name : getMatchOutNoBlank
     * @param p
     * @param target
     * @return
     */
    public String getMatchOutNoBlank(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        target =  m.replaceAll("");

        return target;
    }


    /**
     * 정규식 패턴 매칭(매칭된 패턴에 블랭크 추가)
     * @Method Name : getMatchAddBlank
     * @param p
     * @param target
     * @return
     */
    public String getMatchAddBlank(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        while (m.find()) {
            target = m.replaceAll(m.group()+" ");
        }

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭된 패턴에 블랭크 제거)
     * @Method Name : getMatchOutBlank
     * @param p
     * @param target
     * @return
     */
    public String getMatchOutBlank(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        while (m.find()) {
            target = m.replaceAll(" ");
        }

        return target;
    }

    /**
     * 정규식 패턴 매칭(루프돌며 매칭된 패턴 모두 제거)
     * @Method Name : getMatchRoopOut
     * @param p
     * @param target
     * @return
     */
    public String getMatchRoopOut(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        while (m.find()) {
            target = m.replaceAll(" ");
        }

        return target;
    }

    /**
     * 동 정규식 패턴 매칭(루프돌며 중복동 제거 및 공유 리스트에 담기)
     * @Method Name : getMatchdupleOut
     * @param p
     * @param target
     * @return
     */
    public String getMatchdupleDongOut(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        int cnt = 0;
        String tmpStr = "";
        String dongArrStr = "";

        while (m.find()) {
            if(cnt == 0){
                //첫번째 동을 tmpStr에 담는다
                tmpStr = m.group();
                target = m.replaceFirst("TEMP_STR");
                dongArrStr=tmpStr;
            }
            else{
                //첫번째 동과 같으면 제거
                if(m.group().equals(tmpStr))
                    target = target.replace(m.group(), "");
                else{//다르면 | 구분자로 문자열 담는다
                    dongArrStr=String.join("@",dongArrStr,m.group());
                    target = target.replaceAll(m.group(), "");
                }
            }
            cnt++;
        }

        if(!dongArrStr.equals(tmpStr)){
            //공유 변수에 담기
            ShareVal.dongArr = Arrays.asList(dongArrStr.split("@"));
        }

        //tmpStr이 있으면 TEMP_STR로 변환시킨 첫번째 동을 다시 원복시킨다
        target = target.replaceAll("TEMP_STR", tmpStr);

        return target;
    }

    /**
     * 정규식 패턴 매칭(루프돌며 배열에 저장된 동으로 주소를 변환)
     * @Method Name : getMatchDongRoop
     * @param p
     * @param target
     * @param netxDong
     * @return
     */
    public String getMatchDongRoop(String target, String netxDong) throws Exception {

        Matcher m = ADDRESS_PATTERN.DEFAULT.DUPLE_DONG.getPettern().matcher(target);

        if (m.find()) target = target.replaceAll(m.group(), netxDong);

        return target;
    }

    /**
     * 시구군 정규식 패턴 매칭(루프돌며 중복 패턴 제거 및 공유 리스트에 담기)
     * @Method Name : getMatchdupleOut
     * @param p
     * @param target
     * @return
     */
    public String getMatchdupleOut(Pattern p, String target) throws Exception {

        Matcher m = p.matcher(target);

        int cnt = 0;
        String tmpStr = "";
        String sigugunArrStr = "";

        while (m.find()) {
            if(cnt == 0){
                //첫번째 시구군을 tmpStr에 담는다
                tmpStr = m.group();
                target = m.replaceFirst("TEMP_STR");
                sigugunArrStr=tmpStr;
            }
            else{
                //첫번째 시구군과 같으면 제거
                if(m.group().equals(tmpStr))
                    target = target.replace(m.group(), "");
                else{//다르면 @ 구분자로 문자열 담는다
                    //시구군데이터가 없으면 구분자 없음처리
                    sigugunArrStr=String.join(StringUtils.isEmpty(sigugunArrStr) ? "" : "@",sigugunArrStr,m.group());
                    target = target.replaceAll(m.group(), "");
                }
            }
            cnt++;
        }

        //첫번째 시구군을 제외하기 위한 임시 배열
        String[] tempArr = null;
        if(!sigugunArrStr.equals(tmpStr))
            tempArr = sigugunArrStr.split("@");

        //첫번째 시구군을 제외하고 공유 변수에 담기
        if(tempArr != null){
            for(String temp : tempArr){
                if(!temp.equals(tmpStr))
                    ShareVal.sigugunArr.add(temp);
            }
        }

        target = target.replaceAll("TEMP_STR", tmpStr);

        return target;
    }

    /**
     * 정규식 패턴 매칭(루프돌며 배열에 저장된 시구군으로 주소를 변환)
     * @Method Name : getMatchSigugunRoop
     * @param p
     * @param target
     * @param sigugun
     * @return
     */
    public String getMatchSigugunRoop(String target, String sigugun) throws Exception {

        Matcher m = ADDRESS_PATTERN.DEFAULT.DUPLE_SIGUGUN.getPettern().matcher(target);

        if (m.find()) target = target.replaceAll(m.group(), sigugun);

        return target;
    }

    /**
     * 정규식 패턴 매칭(매칭되는 패턴에 의해 정확도 확인)
     * @Method Name : getMatchXyAccType
     * @param p
     * @param target
     * @return
     */
    public boolean getMatchXyAccType(Pattern p, String target) throws Exception {
        boolean result = false;
        Matcher m = p.matcher(target);

        result = m.matches();

        //log.debug("## getMatchXyAccType target : "+target+" | result : "+result);
        return result;
    }

    /**
     * 뒤에서부터 공백을 기준으로 한블럭씩 잘라냄
     * @Method Name : nextAddr
     * @param address
     * @return
     */
    public String nextAddr(String address) throws Exception {
        //        log.debug("# Address address.length() : "+address.length());

        //검색할 주소가 10보다 작으면 null
        if (address.length() < 10)
            return null;

        String[] temp = address.split(" ");

        /*
        for (int i = 0; i < temp.length ; i++) {
            log.debug("# temp["+i+"] = "+temp[i]);
        }

        log.debug("# temp splite length() : "+temp.length);
         */

        if (temp == null || temp.length <= 3) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < temp.length - 1; i++) {
            stringBuilder.append(temp[i]);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString().trim();
    }

    /**
     * 뒤에서부터 공백을 기준으로 한블럭씩 잘라냄 길이 설정가능
     * @Method Name : nextAddr
     * @param address
     * @return
     */
    public String nextAddr(String address, int length) throws Exception {
        //        log.debug("# Keyword address.length() : "+address.length());

        if (address.length() < length)
            return null;

        String[] temp = address.split(" ");

        /*
        for (int i = 0; i < temp.length ; i++) {
            log.debug("# temp["+i+"] = "+temp[i]);
        }

        log.debug("# temp splite length() : "+temp.length);
         */

        if (temp == null || temp.length <= 3) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < temp.length - 1; i++) {
            stringBuilder.append(temp[i]);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString().trim();
    }

    /**
     * 앞에서부터 공백을 기준으로 한블럭씩 잘라냄
     * @Method Name : beforeAddr
     * @param address
     * @return
     */
    public String beforeAddr(String address) throws Exception {
        //        log.debug("# Address address.length() : "+address.length());

        //검색할 주소가 3보다 작으면 null
        if (address.length() < 3)
            return null;

        String[] temp = address.split(" ");

        /*
        for (int i = 0; i < temp.length ; i++) {
            log.debug("# temp["+i+"] = "+temp[i]);
        }

        log.debug("# temp splite length() : "+temp.length);
         */

        if (temp == null || temp.length <= 2) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < temp.length; i++) {
            stringBuilder.append(temp[i]);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString().trim();
    }

    /**
     * 앞에서부터 공백을 기준으로 한블럭씩 잘라냄 길이 설정가능
     * @Method Name : beforeAddr
     * @param address
     * @return
     */
    public String beforeAddr(String address, int length) throws Exception {
        //        log.debug("# Keyword address.length() : "+address.length());

        if (address.length() < length)
            return null;

        String[] temp = address.split(" ");

        /*
        for (int i = 0; i < temp.length ; i++) {
            log.debug("# temp["+i+"] = "+temp[i]);
        }

        log.debug("# temp splite length() : "+temp.length);
         */

        if (temp == null || temp.length <= 2) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < temp.length; i++) {
            stringBuilder.append(temp[i]);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString().trim();
    }


    /**
     * 조회 주소 정확도와 이전 주소 정확도를 비교하여 적용할 Location을 리턴
     * @Method Name : compareXyAccType
     * @param loc 조회 주소
     * @param oldLoc 이전 주소
     * @param defaultAddress 최초 기본 요청 주소
     * @return Location
     * @throws Exception
     */
    public Location compareXyAccType(Location loc, Location oldLoc, String defaultAddress) throws Exception {

        if(loc == null && oldLoc != null){
            log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소가 없음으로 이전 주소 적용 : "+oldLoc.getAddressName());
            return oldLoc;
        }

        if(loc != null && oldLoc == null){
            log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소가 없으므로 조회 주소 적용 : "+loc.getAddressName());
            return loc;
        }

        log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 정확도 비교 : 조회 주소(정확도:"+loc.getXyAccType()+", 카운트:"+loc.getTotalCount()+", 지명:"+loc.getAddressType()+") = "+loc.getAddressName()+", 이전 주소(정확도:"+oldLoc.getXyAccType()+", 카운트:"+oldLoc.getTotalCount()+", 지명:"+oldLoc.getAddressType()+") = "+oldLoc.getAddressName() );
        //조회 주소가 이전 주소보다 정확도가 높으면
        if(Integer.valueOf((loc.getXyAccType())) <= Integer.valueOf((oldLoc.getXyAccType()))){
            log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소 정확도가 이전 주소 정확도 보다 높거나 같음[조건IN]");

            boolean locPlace = placeCheck(loc);         //조회 주소 지명여부
            boolean oldLocPlace = placeCheck(oldLoc);   //이전 주소 지명여부

            boolean locDong = false;    //조회 주소 요청 주소와 동이 같은지 체크
            boolean oldLocDong = false; //이전 주소 요청 주소와 동이 같은지 체크

            //조회 주소와 이전 주소가 둘다 지명이 아닐때만
            if( (!locPlace && !oldLocPlace) ){
                log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소와 이전 주소가 둘다 지명이 아님[조건IN]");
                //조회 주소 카운트 수가 이전 주소 카운트 수보다 낮을 경우에만 적용

                locDong = compareDong(loc, defaultAddress);
                oldLocDong = compareDong(oldLoc, defaultAddress);

                //조회 주소만 요청 주소와 동이 같으면 조회 주소를 우선 적용
                if(locDong && !oldLocDong){
                    log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소만 요청 주소와 동이 같음 조회 주소를 우선 적용 : "+loc.getAddressName());
                    return loc;
                }
                //이전 주소만 요청 주소와 동이 같으면 이전 주소를 우선 적용
                else if(!locDong && oldLocDong){
                    log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소만 요청 주소와 동이 같음 이전 주소를 우선 적용 : "+oldLoc.getAddressName());
                    return oldLoc;
                }

                //위의 조건이 모두 예외이면 두 주소 카운트 비교
                if(searchCountCheck(loc, oldLoc)) return loc;

            }
            //조회 주소와 이전 주소가 둘다 지명이면
            else if( locPlace && oldLocPlace ){
                log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소와 이전 주소가 둘다 지명임[조건IN]");

                locDong = compareDong(loc, defaultAddress);
                oldLocDong = compareDong(oldLoc, defaultAddress);

                //이전 주소만 요청 주소와 동이 같을 경우를 제외하고는 조회 주소를 우선 적용
                if( !(!locDong && oldLocDong)){
                    log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소만 요청 주소와 동이 같을 경우를 제외하는 조건[IN]");

                    //조회 주소와 이전 주소의 동이 같을 경우
                    if(locDong && oldLocDong){
                        log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소와 요청 주소가 동이 같을 경우 조건[IN]");
                        if(loc.getAddressName().length() > oldLoc.getAddressName().length()){
                            log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소의 길이가 이전 주소 길이보다 길면 적용 : "+loc.getAddressName());
                            return loc;
                        }

                    }else{
                        log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소만 요청 주소와 동이 같을 경우를 제외하고는 조회 주소를 우선 적용 : "+loc.getAddressName());
                        //위의 조건이 모두 예외이면 두 주소 카운트 비교
                        if(searchCountCheck(loc, oldLoc)) return loc;
                    }

                }

            }else if(!locPlace && oldLocPlace){
                log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 조회 주소가 지명이 아니고 이전주소가 지명임[조건IN]");

                locDong = compareDong(loc, defaultAddress);
                oldLocDong = compareDong(oldLoc, defaultAddress);

                //이전 주소만 요청 주소와 동이 같을 경우를 제외하고는 조회 주소를 우선 적용
                if( !(!locDong && oldLocDong) ){
                    log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소만 요청 주소와 동이 같을 경우를 제외하고는 조회 주소를 우선 적용 : "+loc.getAddressName());
                    return loc;

                }

            }
        }

        log.debug("@@ ADDRESS UTIL[COMPARE_XY_ACC_TYPE] 이전 주소 정확도가 조회 주소보다 높음으로 이전 주소 그대로 유지 : "+oldLoc.getAddressName());
        return oldLoc;
    }


    /**
     * 조회 카운트 수와 이전 주소 카운트 수를 비교하여 적용여부를 True, False로 리턴
     * @Method Name : searchCountCheck
     * @param newCount 조회 주소 카운트
     * @param oldCount 이전 주소 카운트
     * @return
     */
    public boolean searchCountCheck(Location loc, Location oldLoc) {
        int newCount = loc.getTotalCount();
        int oldCount = oldLoc.getTotalCount();

        log.debug("@@ ADDRESS UTIL[SEARCH_COUNT_CHECK] 주소 카운트 체크[조건IN] :: 조회 주소 카운트 = "+newCount+", 이전 주소 카운트 = "+oldCount);
        //조회 주소 카운트 수가 이전 주소 카운트 수보다 낮을 경우에만 적용
        if(newCount < oldCount){
            log.debug("@@ ADDRESS UTIL[SEARCH_COUNT_CHECK] 조회 주소 카운트 수가 이전 주소 카운트 수보다 낮으므로 TRUE");
            return true;
        }

        log.debug("@@ ADDRESS UTIL[SEARCH_COUNT_CHECK] 조회 주소 카운트 수가 이전 주소 카운트 수보다 높으므로 FALSE");
        return false;
    }



    /**
     * 주소 타입이 지번 또는 도로명 지명일 경우 TRUE
     * @Method Name : addressTypeCheck
     * @param type
     * @return
     */
    public boolean placeCheck(Location loc){

        if(loc == null) return false;

        String type = loc.getAddressType();
        if(StringUtils.isEmpty(type)) return false;

        //주소 타입 확인
        switch(type){
            case "REGION" : //주소 타입이 지명 FALSE
                log.debug("@@ ADDRESS UTIL[ADDRESS_TYPE_CHECK] :: 주소 타입이 지번 지명임 TRUE (정확도:"+loc.getXyAccType()+", 카운트:"+loc.getTotalCount()+", 지명:"+loc.getAddressType()+") = "+loc.getAddressName());
                return true;
            case "ROAD" : //주소 타입이 도로명이면 정확도 낮음
                log.debug("@@ ADDRESS UTIL[ADDRESS_TYPE_CHECK] :: 주소 타입이 도로명 지명임 TRUE (정확도:"+loc.getXyAccType()+", 카운트:"+loc.getTotalCount()+", 지명:"+loc.getAddressType()+") = "+loc.getAddressName());
                return true;
            default:
                return false;
        }
    }



    /**
     * 조회 동과 다르면 FALSE 같으면 TRUE
     * 요청 주소 동이 있을때 요청 주소 동과 조회 주소 동을 비교하여 앞 문자열이 일치하지 않으면 FALSE
     * @Method Name : compareDong
     * @param loc
     * @return
     * @throws Exception
     */
    public boolean compareDong(Location loc, String defaultAddress) throws Exception {

        Pattern DONG_CHECK = ADDRESS_PATTERN.CUSTOM.DONG_CHECK.getPettern(); //동만 가지고 오는 패턴 적용
        Pattern ROAD_CHECK = ADDRESS_PATTERN.CUSTOM.ROAD_CHECK.getPettern(); //도로명 주소체크 패턴
        Pattern NUMBER_OUT = ADDRESS_PATTERN.CUSTOM.NUMBER_OUT.getPettern(); //숫자만 가지고 오는 패턴 적용

        if(loc == null){
            return false;
        }

        if(StringUtils.isEmpty(defaultAddress)){
            return false;
        }

        boolean roadChk = !StringUtils.isEmpty(getMatchOnly(ROAD_CHECK, defaultAddress)); //요청 주소가 도로명 주소이면 null을 리턴해서 FALSE
        log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 요청주소 도로명주소 확인 = "+(roadChk ? "도로명주소임" : "도로명주소가 아님")+" 동 비교 패턴 시작 ... : "+defaultAddress);

        String old_dong = getMatchOnly(DONG_CHECK, defaultAddress); //요청 주소 동(법정동)
        //법정동이 없으면 행정동을 넣음
        String new_dong = StringUtils.isEmpty(loc.getEaAddr3()) ? loc.getEaAddr8() : loc.getEaAddr3(); //조회 주소 동(법정동)

        //요청 주소가 도로명 주소이면 도로명 셋팅
        if(roadChk){
            old_dong = getMatchOnly(ROAD_CHECK, defaultAddress);

            //도로명 주소가 없으면 null로 처리
            try {
                new_dong = loc.getRoadAddress().getRoadName();
            } catch (NullPointerException e) {
                new_dong = null;
            }
        }
        log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);

        if(StringUtils.isEmpty(old_dong) || StringUtils.isEmpty(new_dong)){
            log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 비교할 동이 없음 FALSE, 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);
            return false;
        }

        //동에 들어간 숫자때문에 일치한것을 찾기 힘드므로 비교를 위해 숫자는 우선 제거
        old_dong = getMatchOutNoBlank(NUMBER_OUT, old_dong); //요청 주소 동에서 숫자제거
        new_dong = getMatchOutNoBlank(NUMBER_OUT, new_dong); //조회 주소 동에서 숫자제거
        log.debug("@@ ADDRESS UTIL[COMPARE_DONG](숫자제거) :: 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);

        //요청 주소 동이 두글자이면
        if(old_dong.trim().length() == 2 ){
            log.debug("@@ ADDRESS UTIL[COMPARE_DONG](길이체크) :: 요청 주소 동 = "+old_dong.trim().length()+", 조회 주소 동 = "+new_dong.trim().length());

            //조회 주소 동이 한글자 이상일때
            if(new_dong.trim().length() > 1 ){
                if(!compareExtDong(new_dong, old_dong, 1)) return false;
            }
            //조회 주소 동이 한글자 이하로 정확도 낮음
            else{
                log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 조회 주소 동 한글자이하 FALSE, 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);
                return false;
            }

        }
        //요청 주소 동이 두글자보다 크면
        else if( old_dong.trim().length() > 2 ){

            //조회 주소 동이 두글자보다 크면
            if(new_dong.trim().length() > 2){
                if(!compareExtDong(new_dong, old_dong, 2)) return false;
            }
            //조회 주소 동이 두글자이면
            else if(new_dong.trim().length() == 2) {
                if(!compareExtDong(new_dong, old_dong, 1)) return false;
            }
            //조회 주소 동이 두글자 미만일때
            else {
                log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 조회 주소 동 두글자이하 FALSE, 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);
                return false;
            }

        }else {
            log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 요청 주소 동 두글자이하 FALSE, 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);
            return false;
        }

        log.debug("@@ ADDRESS UTIL[COMPARE_DONG] 동 일치 TRUE :: 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);
        return true;
    }

    /**
     * 추출카운트만큼 동의 글자를 추출해서 비교후 같지 않으면 FALSE 같으면 TRUE
     * @Method Name : compareSubstrDong
     * @param new_dong 조회 주소 동
     * @param old_dong 요청 주소 동
     * @param extCount 추출카운트
     * @return
     */
    private boolean compareExtDong(String new_dong, String old_dong, int extCount){

        old_dong = old_dong.trim().substring(0,extCount); //요청 주소 동 앞 한글자 추출
        new_dong = new_dong.trim().substring(0,extCount); //조회 주소 동 앞 한글자 추출
        log.debug("@@ ADDRESS UTIL[COMPARE_DONG](extCount:"+extCount+") :: 요청 주소 동 = "+old_dong+", 조회 주소 동 = "+new_dong);

        //요청 주소 동과 조회 주소 동이 같지 않으면 FALSE
        if(!old_dong.equals(new_dong)){
            log.debug("@@ ADDRESS UTIL[COMPARE_DONG] :: 요청 주소 동과 조회 주소 동 불일치 FALSE");
            return false;
        }

        return true;

    }



}