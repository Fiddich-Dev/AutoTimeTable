package org.fiddich.coreinfradomain.domain.Member;

import java.util.Map;

public class SchoolNameConverter {
    private static final Map<String, String> SCHOOL_MAPPING = Map.of(
            "서울대학교", "SNU",
            "한국공과대학교", "KITECH",
            "부산교육대학교", "BNUE",
            "성균관대학교", "SKKU"
            // 추가 매핑...
    );

    public static String convertToEng(String koreanName) {
        return SCHOOL_MAPPING.getOrDefault(koreanName, SCHOOL_MAPPING.get(koreanName));
    }
}
