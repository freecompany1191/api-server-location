package com.o2osys.location.component;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.o2osys.location.common.constants.ADDRESS_PATTERN;
import com.o2osys.location.common.constants.ADDRESS_PATTERN.STEP;
import com.o2osys.location.common.constants.Define.XyAccType;
import com.o2osys.location.common.constants.ShareVal;
import com.o2osys.location.common.utils.AddressUtils;
import com.o2osys.location.entity.kakao.Location;
import com.o2osys.location.entity.kakao.Response.ResAddress;
import com.o2osys.location.entity.kakao.Response.ResAddress.Documents;
import com.o2osys.location.entity.kakao.Response.ResCoord2address;
import com.o2osys.location.entity.kakao.Response.ResKeyword;

/**
   @FileName  : LocationKakaoComponent.java
   @Description : 카카오API 주소 검색 모듈
   @author      : KMS
   @since       : 2017. 8. 22.
   @version     : 1.0

   @개정이력

   수정일           수정자         수정내용
   -----------      ---------      -------------------------------
   2017. 8. 22.     KMS            최초생성
   2017. 9. 12.     KMS            패턴 추가 및 로직 보완
   2017. 9. 14.     KMS            패턴 추가 및 로직 보완(동갯수에 따른 루프문 처리)
   2017. 10. 19.    KMS            도로명주소 없는 주소 정확도 낮음 제외, 동루프에 따른 조회주소 누락 버그 수정
   2017. 11. 01.    KMS            패턴매치시 이전주소 저장 누락보완, 동 제거 패턴 추가
   2017. 11. 02.    KMS            주소 정확도 비교 로직 보완, 로그 정리, 건물리스트 휴먼시아,아너스빌 추가 빌 패턴제거, 아파트이름과 동 띄어쓰기 패턴 보완, 행정동/법정동 패턴 수정
   2017. 11. 03.    KMS            패턴 로직개선, 999-999번지 패턴 수정
   2017. 11. 16.    KMS            층, 호 제거 패턴 추가, 조회된 동과 틀리면 정확도 낮음 처리, 정확도 낮음 처리 로직 보완
   2017. 11. 17.    KMS            getMatch 수정 후 null 발생 건 다수 확인 getMatchOnly 추가하고 getMatch 는 원복처리
   2017. 11. 27.    KMS            1. 도로명일 경우 동비교 패턴은 패스
   2017. 11. 27.    KMS            2. 패턴 미적용 주소에 대해서 주소 검색만 실시 한 후 조회된 주소가 있을때 패턴 검색 주소와 비교하면서 일치하는 주소는 정확도 높음으로 리턴
   2017. 11. 27.    KMS            3. 패턴 미적용 주소와 패턴적용 주소가 다르면 정확도 낮음으로 리턴
   2017. 11. 27.    KMS            4. 그 외 패턴 미적용 주소로 검색된 주소가 없는 경우에는 패턴 적용 주소로 진행하여 결과값 리턴
   2017. 11. 27.    KMS            5. 동넘버 로직 보완
   2017. 11. 27.    KMS            6. 동체크 로직 보완
   2017. 11. 27.    KMS            7. 정확도가 낮음이면 그 아래 조건들은 패스
   2017. 11. 28.    KMS            기본패턴에 붙어있는 시도 시구군 동 제거패턴 추가, 동,도로명 패턴 수정, 동패턴에 가 패턴 추가
   2017. 12. 06.    KMS            주소 검색 HttpStatus Code 리턴을 위한 로직 보완
   2017. 12. 07.    KMS            KAKAO API 키워드 검색시 LENGTH 50 이상이면 400 Bad Request 에러 리턴에 대한 조치
   2017. 12. 19.    KMS            패턴 미적용 조회 주소만 있을 경우 정확도 낮음으로 리턴 처리
   2018. 04. 12.    KMS            공유 배열 -> 리스트 타입으로 변환
   2018. 04. 12.    KMS            1개이상의 시구군 갯수 만큼 조회기능 추가(기존에는 더 긴것을 제외하고 제거함)
   2018. 04. 12.    KMS            검색할 주소 최소 길이 3 -> 10으로 증가
   2018. 04. 12.    KMS            최소 1 -> 3블럭인 주소만 검색 시도 하도록 수정(ex: 서울시(1블럭) -> 서울시 구로구 구로동(3블럭))
   2018. 04. 12.    KMS            동 비교시 요청주소 지번인지 도로명인지 판별하여 타입에 맞게 처리되도록 개선(기존에는 지번만 처리)
   2018. 04. 12.    KMS            카카오 API 주소 검색 응답 카운트수 비교 로직 추가
   2018. 04. 12.    KMS            정확도 판별 기능 향상(정확도 판별 시 주소 정확도가 낮음이 아니면 반드시 동 비교)
   2018. 04. 12.    KMS            지명 여부, 검색 주소 카운트수, 동 비교 여부등의 조건들을 조합하여 더 높은 정확도의 주소를 판별하도록 개선
   2018. 04. 12.    KMS            확실히 정확한 주소가 아니면 정확도 높음 처리 하지 않음
   2018. 04. 20.    KMS            STEP5 패턴 변경 앞 또는 뒤에 공백이 있는 시군구와 번지,도로명 분리
 */
@Component
public class LocationKakaoComponent {
    private final String TAG = LocationKakaoComponent.class.getSimpleName();
    // 로그
    private final Logger log = LoggerFactory.getLogger(LocationKakaoComponent.class);

    @Autowired
    AddressUtils addressUtils;

    //카카오 REST API KEY
    @Value("${kakao.restapi.key}")
    String KAKAO_API_KEY;

    //카카오 주소 검색 API
    @Value("${kakao.api.url.address}")
    String ADDRESS_URL;

    //카카오 키워드로 장소 검색 API
    @Value("${kakao.api.url.keyword}")
    String KEYWORD_URL;

    //카카오 좌표 -> 주소 변환 API
    @Value("${kakao.api.url.coord2address}")
    String COORD2ADDRESS_URL;

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void destroy() {
    }

    /**
     * 조회된 주소 및 향상된 주소 가져오기
     * @Method Name : getAddr
     * @param address
     * @return
     * @throws Exception
     */
    public Location getAddr(String address) throws Exception {

        ShareVal.dongArr = new ArrayList<String>(); //공유 변수 초기화
        ShareVal.sigugunArr = new ArrayList<String>(); //공유 변수 초기화
        ShareVal.noPatternAddressName = null;

        String oldAddress = address;

        log.info("@@ 주소 기본패턴 적용 전 Before Address : "+ address);
        address = addressUtils.getMatchDefault(address); //시군구 패턴을 1차적으로 걸러냄
        log.info("@@ 주소 기본패턴 적용 후 After Address : "+ address);

        //기본패턴 적용 전 주소검색 모듈
        Location noPatternLocation = getAddress(0, "address", oldAddress, null, false);
        log.info("@@ KAKAO API 패턴 미적용 주소 : "+ShareVal.noPatternAddressName);
        log.info("@@ KAKAO API 동 배열 데이터 : "+ShareVal.dongArr);
        log.info("@@ KAKAO API 시구군 배열 데이터 : "+ShareVal.sigugunArr);

        //주소 향상기능 모듈적용
        Location location = null;

        //동 배열이 없으면 기본 조회만 수행
        if(ShareVal.dongArr.size() == 0)
            location = addrAccInc(address);

        //동 배열에 데이터가 있을 경우 루프문을 돌며 주소검색 수행
        if(ShareVal.dongArr.size() > 0)
            location = dongArrRoop(location, address, true);

        //시구군 배열, 동 배열이 있을경우 수행하여 주소 가져옴 없으면 그냥 원래 주소를 가져옴
        if(ShareVal.sigugunArr.size() > 0)
            location = sigugunArrRoop(location, address);

        //조회된 주소가 있을 때
        if(location != null){
            //조회된 주소가 있고 패턴미적용 조회 주소가 있을 때
            if(!StringUtils.isEmpty(location.getAddressName()) && !StringUtils.isEmpty(ShareVal.noPatternAddressName)){
                //조회된 주소와 패턴미적용 조회 주소가 다르면
                if(!location.getAddressName().equals(ShareVal.noPatternAddressName)){
                    log.info("@@ KAKAO API 조회된 주소와 패턴미적용 조회 주소가 다르므로 주소 정확도 낮음으로 리턴 : "+location.getAddressName());
                    location.setXyAccType(XyAccType.TYPE_3);
                }
            }
            //배달 대행 업체에서 들어온 주소를 셋팅한다
            location.setEaAddr7(oldAddress);
        }

        //조회된 주소가 없고 패턴 미적용 주소만 있을 때
        if( location == null && noPatternLocation != null){
            log.info("@@ KAKAO API 패턴 미적용 조회 주소만 있어 주소 정확도 낮음으로 리턴 : "+noPatternLocation.getAddressName());
            noPatternLocation.setXyAccType(XyAccType.TYPE_3);
            //배달 대행 업체에서 들어온 주소를 셋팅한다
            noPatternLocation.setEaAddr7(oldAddress);
            location = noPatternLocation;
        }

        return location;
    }


    /**
     * 시구군 배열에 데이터가 있을 경우 루프문을 돌며 주소검색 수행
     * @Method Name : sigugunArrRoop
     * @param location
     * @param address
     * @return
     * @throws Exception
     */
    private Location sigugunArrRoop(Location location, String address) throws Exception{

        if(location != null && location.getXyAccType().equals(XyAccType.TYPE_1)){
            return location;
        }

        //조회된 주소를 구주소로 저장
        Location oldLocation = location;

        //시구군 배열에 값이 있고 정확도가 높지 않을때 동 배열에 담긴 동들로 루프를 돌리며 주소검색 재시도
        if(ShareVal.sigugunArr.size() > 0){

            log.info("$$ KAKAO API SIGUGUN ARR SEARCH START SIZE : "+ShareVal.sigugunArr.size());
            int arr_num = 0;

            for(String sigugun : ShareVal.sigugunArr){

                //현재주소의 시구군을 배열의 다음 시구군으로 변환
                address = addressUtils.getMatchSigugunRoop(address, sigugun);
                log.info("$$ KAKAO API SIGUGUN ARR["+arr_num+"] :: NEXT SIGUGUN : "+sigugun+", ADDRESS : "+address);

                //다음 시구군으로 변환된 주소로 다시 주소를 검색한다
                location = dongArrRoop(location, address, false);

                //조회된 location 의 정확도가 높으면 적용
                if( location != null && location.getXyAccType().equals(XyAccType.TYPE_1)){
                    log.info("$$ KAKAO API SIGUGUN ARR["+arr_num+"] 조회된 주소 정확도가 높음으로 적용 : "+location.getAddressName());
                    break;
                }

                //조회된 주소 정확도와 이전 주소 정확도를 비교하여 적용
                if(oldLocation != null)
                    location = addressUtils.compareXyAccType(location, oldLocation, address);

                arr_num++;
            }
        }

        return location;
    }

    /**
     * 동 배열에 데이터가 있을 경우 루프문을 돌며 주소검색 수행
     * @Method Name : dongArrRoop
     * @param location
     * @param address
     * @return
     * @throws Exception
     */
    private Location dongArrRoop(Location location, String address, boolean first) throws Exception{

        if(location != null && location.getXyAccType().equals(XyAccType.TYPE_1)){
            return location;
        }

        //조회된 주소를 구주소로 저장
        Location oldLocation = location;

        //동 배열에 값이 있고 정확도가 높지 않을때 동 배열에 담긴 동들로 루프를 돌리며 주소검색 재시도
        if(ShareVal.dongArr.size() > 0){
            log.info("$$ KAKAO API DONG ARR SEARCH START SIZE : "+ShareVal.dongArr.size());
            int arr_num = 0;

            for(String netxDong : ShareVal.dongArr){

                //현재주소의 동을 배열의 다음 동으로 변환
                address = addressUtils.getMatchDongRoop(address, netxDong);
                log.info("$$ KAKAO API DONG ARR["+arr_num+"] :: NEXT DONG : "+netxDong+", ADDRESS : "+address);
                //다음 동으로 변환된 주소로 다시 주소를 검색한다
                location = addrAccInc(address);

                //조회된 location 의 정확도가 높으면 적용
                if( location != null && location.getXyAccType().equals(XyAccType.TYPE_1)){
                    log.info("$$ KAKAO API DONG ARR["+arr_num+"] 조회된 주소 정확도가 높음으로 적용 : "+location.getAddressName());
                    break;
                }

                //조회된 주소 정확도와 이전 주소 정확도를 비교하여 적용
                if(oldLocation != null)
                    location = addressUtils.compareXyAccType(location, oldLocation, address);

                arr_num++;
            }
        }else location = addrAccInc(address);

        return location;
    }

    /**
     * 단계별 패턴을 적용하여 주소 향상기능 호출
     * @Method Name : addrApiRoop
     * @param address
     * @return
     * @throws Exception
     */
    private Location addrAccInc(String address) throws Exception{

        address = address.trim();
        String defaultAddress = address; //패턴 적용 기본 주소 저장

        Location loc = null;
        Location old_loc = null;

        String searchType = "address";
        log.info("@@ KAKAO API [PART1] ===========================================================================================================");
        log.info("@@ KAKAO API 주소 검색 시작 : "+address);
        loc = addrApiRoop(searchType, address, defaultAddress);

        if(loc != null && loc.getXyAccType().equals(XyAccType.TYPE_1)){
            log.info("@@ KAKAO API 주소 정확도 높음으로 리턴 : "+loc.getAddressName());
            return loc;
        }


        log.info("@@ KAKAO API [PART2] ===========================================================================================================");
        log.info("@@ KAKAO API 키워드 검색 시작 : "+address);
        //키워드 시작전에 이전주소를 저장
        old_loc = loc;
        if(old_loc != null){
            log.info("@@ KAKAO API 기존 주소 타입 : "+old_loc.getAddressType()+" | 조회결과 : "+old_loc.getAddressName()+" | 정확도 : "+old_loc.getXyAccType());
        }

        searchType = "keyword";

        //KAKAO API LENGTH가 50이상이면 400 Bad Request 에러 리턴에 대한 조치
        String keywordAddress = address;
        if(address.length() > 50){
            keywordAddress = address.substring(0,50);
            log.info("@@ KAKAO API 키워드 주소 LENGTH 50 자리 이상으로 이하 주소 제거 :: ADDRESS = " + keywordAddress + ", LENGTH = "+address.length());

        }
        //키워드 검색 제외 문자열
        String[] arrOutStr = {"앞"};
        //경기도 고양시 일산서구 일산2동 655-15 일산역앞이안아파트 1701호 오류로 임시조치
        for(String outStr: arrOutStr){

            if(keywordAddress.indexOf(outStr) != -1){
                log.info("@@ KAKAO API 키워드 주소 제외문자 제거 전 :: ADDRESS = " + keywordAddress + ", 제거문자열 = "+outStr);
                keywordAddress = keywordAddress.replaceAll(outStr, "");
                log.info("@@ KAKAO API 키워드 주소 제외문자 제거 후 :: ADDRESS = " + keywordAddress);
            }
        }

        //패턴이 적용된 주소를 반복하여 정확도 향상된 주소 추출
        loc = addrApiRoop(searchType, keywordAddress, defaultAddress);

        if(loc != null){
            log.info("@@ KAKAO API 키워드 주소 타입 : "+loc.getAddressType()+" | 조회결과 : "+loc.getAddressName()+" | 정확도 : "+loc.getXyAccType());

            if(loc.getXyAccType().equals(XyAccType.TYPE_1)){
                log.info("@@ KAKAO API 키워드 주소 정확도 높음으로 리턴 : "+loc.getAddressName());
                return loc;
            }

            //조회된 주소 정확도와 이전 주소 정확도를 비교하여 적용할 Location을 리턴
            old_loc = addressUtils.compareXyAccType(loc, old_loc, defaultAddress);
        }

        int pattern_num = 0;
        searchType = "pattern";
        log.info("@@ KAKAO API [PART3] ===========================================================================================================");
        log.info("@@ KAKAO API 매칭할 패턴 갯수 : "+ADDRESS_PATTERN.STEP.values().length);

        //매칭할 패턴 갯수만 큼 루프돌면서 수행
        for(STEP step : ADDRESS_PATTERN.STEP.values()){

            String addressPM = addressUtils.getMatchOutBlank(step.getPettern(),address).trim();

            log.info("@@ KAKAO API 패턴매치["+pattern_num+"] 주소 체크 : "+addressPM);

            if(old_loc != null){
                log.info("@@ KAKAO API 기존 주소 타입 : "+old_loc.getAddressType()+" | 조회결과 : "+old_loc.getAddressName()+" | 정확도 : "+old_loc.getXyAccType());
            }

            if(!addressPM.equals(address)){
                log.info("@@ KAKAO API 패턴매치["+pattern_num+"] 수행 시작 ... : addressPM("+addressPM+") = address("+address+")["+!addressPM.equals(address)+"]");
                loc = addrApiRoop(searchType, addressPM, defaultAddress);

                if(loc != null){

                    log.info("@@ KAKAO API 패턴매치["+pattern_num+"] 주소 타입 : "+loc.getAddressType()+" | 조회결과 : "+loc.getAddressName()+" | 정확도 : "+loc.getXyAccType());

                    if(loc.getXyAccType().equals(XyAccType.TYPE_1)){
                        log.info("@@ KAKAO API 패턴매치["+pattern_num+"] 주소 정확도 높음으로 리턴 : "+loc.getAddressName());
                        return loc;
                    }

                    //조회된 주소 정확도와 이전 주소 정확도를 비교하여 적용할 Location을 리턴
                    old_loc = addressUtils.compareXyAccType(loc, old_loc, defaultAddress);
                }

            }
            //기존 주소와 같으므로 패스
            else{
                log.info("@@ KAKAO API 패턴매치["+pattern_num+"] 기존 주소와 같으므로 패스 : addressPM("+addressPM+") = address("+address+")["+!addressPM.equals(address)+"]");
            }

            pattern_num++;
        }


        if(old_loc != null)
            log.info("@@ KAKAO API 최종 조회 값 리턴 : "+old_loc.getAddressName()+" | 정확도 : "+old_loc.getXyAccType());
        else
            log.info("@@ KAKAO API 조회된 주소 없음 요청 주소데이터 확인 요망 : "+address);

        log.info("@@ KAKAO API [END] =============================================================================================================");

        //REGION or ROAD or REGION_ADDR or ROAD_ADDR
        //"address_type": "REGION" 지명이면 낮음, "road_address": null 이면 낮음
        //"address_type": "ROAD" 도로명이면 낮음
        return old_loc;
    }


    /**
     * 패턴이 적용된 주소를 반복하여 정확도 향상된 주소 추출
     * @Method Name : addrApiRoop
     * @param searchType
     * @param address
     * @return
     * @throws Exception
     */
    private Location addrApiRoop(String searchType, String address, String defaultAddress) throws Exception{

        Location loc = null;
        Location old_loc = null;

        int num = 0;

        do{
            log.info("API ROOP START["+searchType+"]["+num+"]-----------------------------------------------------------------------------------------------------");
            log.info("@@ KAKAO API START ["+searchType+"]["+num+"] : 조회할 주소 = "+address);

            //패턴 매칭 주소값 가져오기 수행
            loc = getAddress(num, searchType, address, defaultAddress, true);

            //조회된 주소가 있을때
            if(loc != null){

                //조회된 주소가 있고 패턴미적용 조회 주소가 있을 때
                if(!StringUtils.isEmpty(loc.getAddressName()) && !StringUtils.isEmpty(ShareVal.noPatternAddressName)){

                    //조회된 주소와 패턴미적용 조회 주소가 같으면
                    if(loc.getAddressName().equals(ShareVal.noPatternAddressName)){

                        log.info("@@ KAKAO API ["+searchType+"]["+num+"] 조회된 주소와 패턴미적용 조회 주소가 같으므로 주소 정확도 높음으로 리턴 : "+loc.getAddressName());
                        loc.setXyAccType(XyAccType.TYPE_1);
                        return loc;

                    }
                }
            }

            //검색한 주소가 시도 시군구 읍면동리가길로 까지만 있으면 true
            boolean matchXyAccType = addressUtils.getMatchXyAccType(ADDRESS_PATTERN.CUSTOM.SIDODONG_ONLY.getPettern() , address);

            if (loc != null) {

                //검색한 주소가 시도 시군구 읍면동리가길로 false 이고
                if(!matchXyAccType){

                    //주소 정확도가 높음이면 리턴
                    if(loc.getXyAccType().equals(XyAccType.TYPE_1)){
                        log.info("@@ KAKAO API ["+searchType+"]["+num+"] 주소 정확도 높음으로 리턴 : "+loc.getAddressName());
                        return loc;
                    }

                }
                //검색한 주소가 시도 시군구 읍면동리가길로 까지만 있고
                else{

                    //주소 정확도가 높음이면 낮음으로 변경
                    if(loc.getXyAccType().equals(XyAccType.TYPE_1)){
                        log.info("@@ KAKAO API ["+searchType+"]["+num+"] 시도 시군구 읍면동리가길로 정확도 낮음 처리 : "+address);
                        loc.setXyAccType(XyAccType.TYPE_3);
                    }

                }

                //주소 정확도가 낮음일때
                if(loc.getXyAccType().equals(XyAccType.TYPE_3)){
                    if(old_loc != null){
                        //이전 주소 정확도가 중간이면 중간 주소를 리턴
                        if(old_loc.getXyAccType().equals(XyAccType.TYPE_2)){
                            log.info("@@ KAKAO API ["+searchType+"]["+num+"] 이전 주소 정확도가 중간으로 리턴 : "+old_loc.getAddressName());
                            return old_loc;
                        }
                    }
                }

                //조회된 주소 정확도와 이전 주소 정확도를 비교하여 적용할 Location을 리턴
                old_loc = addressUtils.compareXyAccType(loc, old_loc, defaultAddress);
            }

            //검색할 주소가 시도 시군구 읍면동리가길로 까지만 있으면 BREAK
            if(matchXyAccType){
                log.info("@@ KAKAO API ["+searchType+"]["+num+"] 시도 시군구 읍면동리가길로 BREAK ["+num+"] = "+address);
                break;
            }

            //공백기준으로 뒷쪽 한블럭 제거 후 주소가 null 일때까지 다시 반복
            //log.debug("## Address nextAddr = "+address+", address.length() : "+address.length()+", searchType : "+searchType);
            if(searchType.equals("address")){
                address = addressUtils.nextAddr(address);
                //log.debug("## Address nextAddr = "+address);
            }else{
                address = addressUtils.nextAddr(address, 10);
                //log.debug("## Keyword nextAddr = "+address);
            }

            log.info("@@ KAKAO API ["+searchType+"]["+num+"] 검색 결과 없음 재시도 주소 = "+address);
            log.info("API ROOP END["+searchType+"]["+num+"]-------------------------------------------------------------------------------------------------------");
            num++;

        }while(address != null);

        if(old_loc != null)
            log.info("@@ KAKAO API ["+searchType+"] 최종 검색 결과 없음 초기 저장 주소 리턴 = "+old_loc.getAddressName());
        else
            log.info("@@ KAKAO API ["+searchType+"] 조회된 주소 없음 null 리턴 :: "+address);

        log.info("API ROOP RETURN****************************************************************************************************************");
        //주소가 없을때까지 반복했는데도 정확도가 높지 않을때는 최근에 조회되었던 주소를 리턴
        return old_loc;

    }


    /**
     * 패턴 매칭 주소값 가져오기 수행
     * @Method Name : getAddress
     * @param num
     * @param searchType
     * @param address
     * @return
     * @throws Exception
     */
    private Location getAddress(int num, String searchType, String address, String defaultAddress, boolean pattrenYn) throws Exception {
        Location loc = new Location();
        ResAddress res = new ResAddress();

        //log.debug("#### getKeyword["+num+"] address = "+address);

        if (StringUtils.isEmpty(address)) return null;

        //카카오 API 로컬 Address를 REST로 호출
        res = RestTranAddress(searchType, address);

        //패턴사용여부가 TRUE 이면 정확도체크 후 리턴
        if(pattrenYn){
            //정확도 체크 후 리턴
            loc = XyAccTypeCheck(searchType, address, defaultAddress, res);
        }else{

            //조회된 주소가 없으면 null을 리턴 하여 반복수행
            if(res.getMeta().getTotalCount() == 0) return null;
            //조회된 주소가 있을때 정확도 셋팅하여 리턴
            else if(res.getMeta().getTotalCount() >= 1){

                Documents doc  = res.getDocuments().get(0);
                loc = new Location(doc, res.getMeta());

                if(loc != null)
                    //기본패턴 적용 전 조회된 주소가 있다면 주소 정보 저장
                    ShareVal.noPatternAddressName = loc.getAddressName();
            }
        }

        return loc;
    }

    /**
     * 정확도 체크 모듈
     * @Method Name : XyAccTypeCheck
     * @param res
     * @return
     * @throws Exception
     */
    private Location XyAccTypeCheck(String searchType, String address, String defaultAddress, ResAddress res) throws Exception{
        Location loc = new Location();

        //조회된 주소가 없으면 null을 리턴 하여 반복수행
        if(res.getMeta().getTotalCount() == 0) return null;

        //조회된 주소가 있을때 정확도 셋팅하여 리턴
        else if(res.getMeta().getTotalCount() > 0){
            Documents doc  = res.getDocuments().get(0);
            loc = new Location(doc, res.getMeta());
            log.info("@@ KAKAO API ["+searchType+"] 조회된 주소 : "+loc.getAddressName());

            //REGION or ROAD or REGION_ADDR or ROAD_ADDR
            //"address_type": "REGION" 지명이면 낮음, "road_address": null 이면 낮음
            //"address_type": "ROAD" 도로명이면 낮음//REGION 지명

            //조회된 주소 카운트가 1이면 정확도 높음
            if(res.getMeta().getTotalCount() == 1){
                log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 조회된 주소 카운트 1로 정확도 높음 COUNT = "+res.getMeta().getTotalCount());
                loc.setXyAccType(XyAccType.TYPE_1);
            }
            //조회된 주소 카운트가 1이상 3이하이면 정확도 중간
            else if(res.getMeta().getTotalCount() > 1 && res.getMeta().getTotalCount() <= 3){
                log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 조회된 주소 카운트 1이상 3이하로 정확도 중간 COUNT = "+res.getMeta().getTotalCount());
                loc.setXyAccType(XyAccType.TYPE_2);
            }
            //그외 정확도 낮음
            else{
                log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 조회된 주소 카운트 조건 3이상으로 정확도 낮음 COUNT = "+res.getMeta().getTotalCount());
                loc.setXyAccType(XyAccType.TYPE_3);
            }

            //주소 타입 확인
            if(!loc.getXyAccType().equals(XyAccType.TYPE_3)){
                switch(doc.getAddressType()){
                    case "REGION" : //주소 타입이 지명이면 정확도 낮음
                        log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 주소 타입이 지번 지명으로 정확도 낮음 ADDRESS TYPE = "+doc.getAddressType());
                        loc.setXyAccType(XyAccType.TYPE_3);
                        break;
                    case "ROAD" : //주소 타입이 도로명이면 정확도 낮음
                        log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 주소 타입이 도로명 지명으로 정확도 낮음 ADDRESS TYPE = "+doc.getAddressType());
                        loc.setXyAccType(XyAccType.TYPE_3);
                        break;
                }
            }

            //도로명 주소가 null 이면 정확도 낮음
            /*
            if(doc.getRoadAddress() == null)
                loc.setXyAccType(XyAccType.TYPE_3);
             */

            //검색 주소가 시도 시군구 읍면동리가길로 까지만 있으면 정확도 낮음
            if(!loc.getXyAccType().equals(XyAccType.TYPE_3) && addressUtils.getMatchXyAccType(ADDRESS_PATTERN.CUSTOM.SIDODONG_ONLY.getPettern() , address)){
                log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 검색 주소가 시도 시군구 읍면동리가길로 까지만 있어 정확도 낮음 ADDRESS = "+address);
                loc.setXyAccType(XyAccType.TYPE_3);
            }

            //검색 주소가 동 까지만 있으면 정확도 낮음
            if(!loc.getXyAccType().equals(XyAccType.TYPE_3) && addressUtils.getMatchXyAccType(ADDRESS_PATTERN.CUSTOM.DONG_ONLY.getPettern() , address)){
                log.info("@@ KAKAO API ["+searchType+"] 정확도 체크 :: 검색 주소가 동 까지만 있어 정확도 낮음 ADDRESS = "+address);
                loc.setXyAccType(XyAccType.TYPE_3);
            }

            //정확도가 낮음이 아닐때
            if(!loc.getXyAccType().equals(XyAccType.TYPE_3)){
                //조회 주소 동과 요청 주소 동이 다르면 정확도 낮음
                if(!addressUtils.compareDong(loc, defaultAddress)) loc.setXyAccType(XyAccType.TYPE_3);
            }//정확도가 낮음이 아닐때 END

        }
        return loc;
    }


    /**
     * 카카오 API 로컬 Address를 REST로 호출
     * @Method Name : RestTranAddress
     * @param searchType
     * @param address
     * @return
     * @throws Exception
     */
    private ResAddress RestTranAddress(String searchType, String address) throws Exception {
        ResAddress res = new ResAddress();
        ResKeyword resK = new ResKeyword();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization",KAKAO_API_KEY);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<ResAddress> resMsg = null;

        //검색 타입별 분기
        switch(searchType){
            case "address": //주소검색 일때

                try {

                    //log.debug("#ADDRESS_URL : "+ADDRESS_URL +", address : "+address);
                    resMsg = restTemplate.exchange(getUri(ADDRESS_URL, address), HttpMethod.GET, entity, ResAddress.class);

                } catch (HttpServerErrorException | HttpClientErrorException e) {

                    if(e.getStatusCode() == HttpStatus.BAD_REQUEST){
                        log.error("@@ KAKAO API [ResAddress REST_CONNECT_FAIL RESPONSE "+e.getClass().getSimpleName()+" ERROR] :: STATUS = "+e.getStatusCode()+", MSG = "+e.getResponseBodyAsString());
                        resMsg = null;
                    }else throw e;

                }

                break;

            case "keyword": //키워드검색일때

                resK = (ResKeyword) getResponse(ResKeyword.class, getUri(KEYWORD_URL, address), entity);
                //log.info("[KEYWORD RestTranAddress resK] = "+CommonUtils.jsonStringFromObject(resK));
                ResKeyword.Documents docK = null;
                if(resK != null && resK.getDocuments() != null){
                    docK = resK.getDocuments().get(0);
                }

                if(docK != null){

                    //log.debug("# road_address_name : "+docK.getRoadAddressName()+" | address_name : "+docK.getAddressName()+" | place_name : "+docK.getPlaceName());
                    //ResponseEntity<ResCoord2address> resCoord2address = null;

                    String queryRoad = null;
                    String queryAddress = null;
                    int totalCount = 0; //지번주소가 없을경우 조회여부 판단을 위한 카운트 변수

                    //키워드 검색으로 조회된 지번 주소를 담는다
                    if(!StringUtils.isEmpty(docK.getAddressName())){ //키워드 지번주소가 있으면 query에 담는다
                        queryAddress = docK.getAddressName();
                    }
                    //지번주소가 있을 경우 지번으로 조회하여 결과를 담는다
                    if(!StringUtils.isEmpty(queryAddress)){
                        //log.info("[KEYWORD ResAddress queryAddress] = "+CommonUtils.jsonStringFromObject(queryAddress));

                        resMsg = restTemplate.exchange(getUri(ADDRESS_URL, queryAddress), HttpMethod.GET, entity, ResAddress.class);

                        if(resMsg.getBody().getMeta().getTotalCount() > 0){
                            totalCount = resMsg.getBody().getMeta().getTotalCount();
                            //log.info("[KEYWORD queryAddress resMsg] = "+CommonUtils.jsonStringFromObject(resMsg));
                            resMsg.getBody().getMeta().setTotalCount(resK.getMeta().getTotalCount());
                            resMsg.getBody().getMeta().setPageableCount(resK.getMeta().getPageableCount());
                            resMsg.getBody().getMeta().setEnd(resK.getMeta().isEnd());
                        }
                    }

                    //지번주소로 조회된 결과가 없을 경우 도로명으로 조회
                    if(totalCount < 1){

                        //키워드 검색으로 조회된 도로명 주소를 담는다
                        //도로명 주소가 없는 경우에는 정확도가 낮으므로 도로명 주소를 기준으로 검색한다
                        if(!StringUtils.isEmpty(docK.getRoadAddressName())){ //키워드 도로명 주소가 있으면 query에 담는다

                            queryRoad = docK.getRoadAddressName();

                        } else { //키워드 도로명 주소가 없으면 좌표->주소 호출

                            //좌표->주소 검색 호출
                            ResCoord2address.Documents docC2A = (ResCoord2address.Documents) getDocument(ResCoord2address.class, getUri(COORD2ADDRESS_URL, docK.getLongitude(), docK.getLatitude()), entity);

                            //좌표->주소 검색 결과가 있으면
                            if(docC2A != null){

                                if(docC2A.getRoadAddress() != null && !StringUtils.isEmpty(docC2A.getRoadAddress().getAddressName())) //좌표->주소 도로명 주소가 있으면 query에 담는다
                                    queryRoad = docC2A.getRoadAddress().getAddressName();
                                else if(docC2A.getAddress() != null && !StringUtils.isEmpty(docC2A.getAddress().getAddressName())) //좌표->주소 지번 주소가 있으면 query에 담는다
                                    queryRoad = docC2A.getAddress().getAddressName();

                            }

                        }

                        //query 담긴 주소가 있으면 주소 검색 호출
                        if(!StringUtils.isEmpty(queryRoad)){
                            //log.info("[KEYWORD ResAddress queryRoad] = "+CommonUtils.jsonStringFromObject(queryRoad));

                            resMsg = restTemplate.exchange(getUri(ADDRESS_URL, queryRoad), HttpMethod.GET, entity, ResAddress.class);
                            if(resMsg.getBody().getMeta().getTotalCount() > 0){
                                //log.info("[KEYWORD queryRoad resMsg] = "+CommonUtils.jsonStringFromObject(resMsg));
                                resMsg.getBody().getMeta().setTotalCount(resK.getMeta().getTotalCount());
                                resMsg.getBody().getMeta().setPageableCount(resK.getMeta().getPageableCount());
                                resMsg.getBody().getMeta().setEnd(resK.getMeta().isEnd());
                            }
                        }
                    }//totalCount < 1 end
                }//docK != null end
                break;
        }

        //주소 검색이 호출되어 Response 데이터가 있을 때
        if(resMsg != null){
            if(resMsg.getStatusCode() == HttpStatus.OK) {

                res.setMeta(resMsg.getBody().getMeta());
                res.setDocuments(resMsg.getBody().getDocuments());

            }
        }else{ //주소 검색조건에 맞지 않았을 경우 기본 초기 데이터를 입력하여 리턴

            ResAddress.Meta addressMeta = new ResAddress.Meta();

            addressMeta.setTotalCount(0);
            addressMeta.setPageableCount(0);
            addressMeta.setEnd(true);

            res.setMeta(addressMeta);
        }

        //log.debug("[RESPONSE BODY] = "+CommonUtils.jsonStringFromObject(res.getMeta()));

        return res;
    }


    /**
     * 주소 검색 이외 각 패턴별 API 호출
     * @Method Name : getResponse
     * @param cls
     * @param uri
     * @param entity
     * @return
     * @throws JsonProcessingException
     */
    private Object getResponse(Class cls, URI uri, HttpEntity<?> entity) throws Exception{

        RestTemplate rest = new RestTemplate();
        //cls.cast(obj)
        //키워드 검색으로 주소 호출
        ResponseEntity res = null;
        try {
            //log.error("[KEYWORD REQUEST BODY] = "+CommonUtils.jsonStringFromObject(entity));
            res = rest.exchange(uri ,HttpMethod.GET, entity, cls);

            //키워드 검색 통신 성공일때
            if(res.getStatusCode() == HttpStatus.OK) {

                //log.debug("# ClassName ["+cls.getSimpleName()+"]");
                switch(cls.getSimpleName()){

                    case "ResKeyword" :
                        //log.error("[KEYWORD RESPONSE BODY] = "+CommonUtils.jsonStringFromObject(res.getBody()));
                        ResKeyword.Meta metaK = ((ResKeyword) res.getBody()).getMeta();

                        //키워드 검색 결과가 있을때
                        if(metaK.getTotalCount() != null && metaK.getTotalCount() > 0)
                            return res.getBody();

                        break;

                    case "ResCoord2address" :
                        //log.error("[Coord2address RESPONSE BODY] = "+CommonUtils.jsonStringFromObject(res.getBody()));
                        ResCoord2address.Meta metaC2A = ((ResCoord2address) res.getBody()).getMeta();

                        //키워드 검색 결과가 있을때
                        if(metaC2A.getTotalCount() != null && metaC2A.getTotalCount() > 0)
                            return res.getBody();

                        break;
                }

            }

        } catch (HttpServerErrorException | HttpClientErrorException e) {

            if(e.getStatusCode() == HttpStatus.BAD_REQUEST){
                log.error("@@ KAKAO API ["+cls.getSimpleName()+" REST_CONNECT_FAIL RESPONSE "+e.getClass().getSimpleName()+" ERROR] :: STATUS = "+e.getStatusCode()+", MSG = "+e.getResponseBodyAsString());
                return null;
            }
            throw e;
        }

        return null;

    }


    /**
     * 주소 검색 이외 각 패턴별 API 호출
     * @Method Name : getDocument
     * @param cls
     * @param uri
     * @param entity
     * @return
     * @throws JsonProcessingException
     */
    private Object getDocument(Class cls, URI uri, HttpEntity<?> entity) throws Exception{

        RestTemplate rest = new RestTemplate();
        //cls.cast(obj)
        //키워드 검색으로 주소 호출
        ResponseEntity res = null;
        try {
            res = rest.exchange(uri ,HttpMethod.GET, entity, cls);

            //키워드 검색 통신 성공일때
            if(res.getStatusCode() == HttpStatus.OK) {

                //log.debug("# ClassName ["+cls.getSimpleName()+"]");
                switch(cls.getSimpleName()){

                    case "ResKeyword" :
                        //log.debug("[KEYWORD RESPONSE BODY] = "+CommonUtils.jsonStringFromObject(res.getBody()));
                        ResKeyword.Meta metaK = ((ResKeyword) res.getBody()).getMeta();

                        //키워드 검색 결과가 있을때
                        if(metaK.getTotalCount() != null && metaK.getTotalCount() > 0)
                            return ((ResKeyword) res.getBody()).getDocuments().get(0);

                        break;

                    case "ResCoord2address" :
                        //log.debug("[Coord2address RESPONSE BODY] = "+CommonUtils.jsonStringFromObject(res.getBody()));
                        ResCoord2address.Meta metaC2A = ((ResCoord2address) res.getBody()).getMeta();

                        //키워드 검색 결과가 있을때
                        if(metaC2A.getTotalCount() != null && metaC2A.getTotalCount() > 0)
                            return ((ResCoord2address) res.getBody()).getDocuments().get(0);

                        break;
                }

            }

        } catch (HttpServerErrorException | HttpClientErrorException e) {

            if(e.getStatusCode() == HttpStatus.BAD_REQUEST){
                log.error("@@ KAKAO API ["+cls.getSimpleName()+" REST_CONNECT_FAIL RESPONSE "+e.getClass().getSimpleName()+" ERROR] :: STATUS = "+e.getStatusCode()+", MSG = "+e.getResponseBodyAsString());
                return null;
            }
            throw e;
        }

        return null;

    }


    /**
     * URI 가져오기
     * @Method Name : getUri
     * @param url
     * @param query
     * @return
     * @throws UnsupportedEncodingException
     */
    private URI getUri(String url, String query) throws UnsupportedEncodingException{

        URI uri=UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("query", query)
                .build()
                .encode("UTF-8")
                .toUri();

        //log.debug("# getUri : "+uri.toString());

        return uri;
    }


    /**
     * URI 가져오기(좌표->주소)
     * @Method Name : getUri
     * @param url
     * @param x
     * @param y
     * @return
     * @throws UnsupportedEncodingException
     */
    private URI getUri(String url, String x, String y) throws UnsupportedEncodingException{

        URI uri=UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("x", x)
                .queryParam("y", y)
                .build()
                .encode("UTF-8")
                .toUri();

        //log.debug("# getUri : "+uri.toString());

        return uri;
    }

}
