package com.o2osys.location.common.constants;

import java.util.regex.Pattern;

import lombok.Getter;

public interface ADDRESS_PATTERN {

    String SIDO = "서울|부산|대구|인천|광주|대전|울산|세종|경기|강원|충북|충남|전북|경북|경남|제주";

    String APT_LIST =
            "스카이뷰|자이|파크|아이파크|편한세상|래미안|푸르지오|프레스티지|뉴스테이|로프트|현대|모닝|팰리스|에버하임|카운티|s클래스|S클래스|렉시온|사랑|스테이트|"
                    + "아너스빌|휴먼시아|에스티지|주공|"
                    + "홈|하이츠|상떼뷰|보람|모아|타운|연립|맨션|트위트|주택|하우스|리움|좋은집|빌라트|"
                    + "미소가|미래가|캠퍼스|토담채|퍼스트|스마트뷰|"
                    + "빌라|빌리지|빌딩|텔|"
                    + "아파트|APT|\\d{1,2}단지|\\d{1,2}차";

    String DONG_NUM = "\\d{1,2}|\\d{1,2}(,|\\.)\\d{1,2}";
    String BUNGI_NUM = "\\d{1,5}(~|-)\\d{1,5}|\\d{1,5}";
    String BUNGI_NUM_TYPE2 = "\\d{1,5}(~|-)\\d{1,5}";
    String APT_KOR_DONG_STR = "(가|나|다|라|마|바|사|아|자|차|카|타|파|하)";
    String APT_DONG="("+APT_KOR_DONG_STR+"|\\d{1,4}|[a-zA-Z]{1})+동";
    String APT_HO = "(\\d{1,4}|[A-Z]{1}\\d{1,3})호?";
    String DONG_STR = "([가-힣]{1,5})("+DONG_NUM+"|)(읍|면|동|가|리)";
    String ROAD_STR = "([가-힣]{1,5})(("+BUNGI_NUM+")|("+BUNGI_NUM+"[가-힣]{1,5})|)(로|길)";

    //제외방법 +(?![제외문자열|제외문자열])(포함문자|포함문자)

    /** 기본 패턴 매칭 그룹 */
    public enum DEFAULT {
        /** STEP1 - [문자] or (문자) or {문자} 제거 */
        BRACKET_STR(Pattern.compile("\\[.*\\]|\\(.*\\)|\\{.*\\}"))
        /** STEP2 - 특수문자 제거 */
        ,SPECIAL_STR(Pattern.compile("[\"\'{}\\[\\];:|\\(\\)*`!_+<>@#$%^&\\=/ㅡ]"))
        /** STEP3 - 시도 시구군 이 매칭되면 포함 그뒤에 주소들 가져옴 */
        ,SIGUGUN( Pattern.compile("(([가-힣]{1,5}+(시|도)|"+SIDO+")\\s[가-힣]{1,5}+(시|군|구).*)"))
        /** STEP4 - 시도 시구군 읍면동가리로길 이 중복일때 하나만 남기고 제거 */
        //        ,DUPLE_SIGUGUN(Pattern.compile("([가-힣]+(시|도)|"+SIDO+")\\s[가-힣]+(시|군|구)\\s(([가-힣]{1,5})+("+DONG_NUM+"|)+(읍|면|동|가|리|로|길)\\s)?"))
        ,DUPLE_SIGUGUN(Pattern.compile("([가-힣]{1,5}+(시|도)|"+SIDO+")\\s[가-힣]{1,5}+(시|군|구)\\s?"))
        /** STEP5 - 앞 또는 뒤에 공백이 있는 시군구와 번지,도로명 분리 */
        ,SIGUGUN_ROAD_ADD_BLANK(Pattern.compile(""
                //              + "((\\s[가-힣]{1,5}(시|도)|"+SIDO+")(?!(광역시|특별시))(?=[가-힣]{1,5}(시|군|구)\\s))|" //시도 시군구 분리 주석처리
                + "((\\s[가-힣]{1,5}(시|군|구))(?!(로|대로|길))(?=("+DONG_STR+")|("+ROAD_STR+")\\s))"))
        /** STEP6 - 아파트와 동호번지 분리 :: ex)아파트와숫자 or 아파트가동 or 아파트A동 or 아파드726번지 or 아파트108호 패턴이 붙어있을 시 아파트 이름만 가져옴 */
        ,APT_BLANK(Pattern.compile("(([가-힣]\\d{1,5}|[가-힣])?)+(\\s?)("+APT_LIST+")(?=(\\d{1,4}|\\d{1,4}(~|-)\\d{1,4}|\\w|"+APT_KOR_DONG_STR+")(동|번지|호|차|))"))
        /** STEP7 - 아파트동과 호 분리 :: ex)아파트의 가동101호 or A동101호 2동101호 패턴이 붙어있을 시 동이름만 가져옴 */
        ,APT_DONG_BLANK(Pattern.compile("("+APT_DONG+")(?=("+APT_HO+"))"))
        /** STEP8 - 주소동과 번지 분리 :: ex) OO동999-999 or 002.3동999-999 or OO2,3동999-999 */
        ,DONG_BUNGI(Pattern.compile("([가-힣]{1,5})("+DONG_NUM+"|)(읍|면|동|가|리)(?=("+BUNGI_NUM+")(?!가)([가-힣]|\\s))"))
        /** STEP9 - 중복동 제거 :: ex)OO동 or 002.3동 or OO2,3동 */
        ,DUPLE_DONG(Pattern.compile("\\s((?!"+APT_DONG+")([가-힣]{1,5})("+DONG_NUM+"|)(동)(?=\\s))"))
        /** STEP10 - 번지와 문자 분리 :: ex)999번지 or 999-999번지 or 999-999 에 공백 이외의 문자가 붙어있을때 번지만 가져옴 */
        ,BUNGI_BLANK(Pattern.compile("(("+BUNGI_NUM+")(번지))(?=(\\w|[가-힣]|\\d{1,5}))"))
        /** STEP11 - 번지와 문자 분리 :: ex)999-999에 공백 이외의 문자가 붙어있을때 번지만 가져옴 */
        ,BUNGI_BLANK2(Pattern.compile("("+BUNGI_NUM_TYPE2+")(?!번지)(?=([a-zA-Z가-힣]))"))
        /** STEP12 - 층 호 제거 :: ex)주소에 9999층 9999호 제거 */
        ,FLOOR_OUT(Pattern.compile("\\s\\d{1,4}(층|호)(?!\\w|[가-힣])"))
        /** STEP13 - 블랭크 제거 */
        ,BLANK_OUT(Pattern.compile("\\s{2,}"));
        ;

        @Getter private final Pattern pettern;

        DEFAULT(Pattern p) {
            this.pettern = p;
        }
    }


    /** 조건에 따라 사용하는 매칭 패턴 */
    public enum CUSTOM {
        /** 시도 시구군 읍면동리 or 시도 시구군 or 시도 만 남을 경우 낮음처리 */
        SIDODONG_ONLY(Pattern.compile(""
                +"([가-힣]+(시|도)|"+SIDO+")\\s[가-힣]+(시|군|구)\\s(("+DONG_STR+")|("+ROAD_STR+"))|"
                +"([가-힣]+(시|도)|"+SIDO+")\\s[가-힣]+(시|군|구)|"
                +"([가-힣]+(시|도)|"+SIDO+")"
                + ""))
        /** 동만 남을 경우 낮음처리 */
        ,DONG_ONLY(Pattern.compile("([가-힣]{1,5})("+DONG_NUM+"|)(동|가)"))
        /** 검색전 동과 검색된 동을 비교 */
        ,DONG_CHECK(Pattern.compile("((?!"+APT_DONG+")([가-힣]{1,5})("+DONG_NUM+"|)(동|가)(?=\\s))"))
        /** 도로명주소 체크 */
        ,ROAD_CHECK(Pattern.compile("\\s([가-힣]{1,5}|)(("+BUNGI_NUM+")|("+BUNGI_NUM+"[가-힣]{1,5})|)(로|길)(?=\\s)"))
        /** 숫자제거 */
        ,NUMBER_OUT(Pattern.compile("[0-9]"))
        ;


        @Getter private final Pattern pettern;

        CUSTOM(Pattern p) {
            this.pettern = p;
        }
    }

    /** 스텝별 패턴 매칭 그룹 */
    public enum STEP {

        /** 999 999-999 999~999 or 999번지 999-999번지 999~999번지 */
        _01( Pattern.compile("\\s("+BUNGI_NUM+")+(번지|)\\s"))
        /** 동 제거 패턴 */
        ,_02( Pattern.compile("\\s([가-힣]{1,5})("+DONG_NUM+"|)(동|가)\\s"))
        ;

        @Getter private final Pattern pettern;

        STEP(Pattern p) {
            this.pettern = p;
        }
    }

}