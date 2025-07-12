package org.fiddich.api.domain.timetable;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.fiddich.api.domain.member.MemberService;
import org.fiddich.api.domain.timetable.dto.CreateTimetableWithExternalLecturesDto;

import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.nio.charset.StandardCharsets;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TimetableServiceTest {

    @Autowired
    TimetableService timetableService;
    @Autowired
    MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    EntityManager em;

//    @Test
//    void 시간표생성() {
//        Department[] departments = {Department.culture, Department.classicalChinese};
//        List<Long> likeLectureId = new ArrayList<>();
//        List<Long> dislikeLectureId = new ArrayList<>();
//        timetableService.createTimetable(2, 1, likeLectureId, dislikeLectureId, departments);
//
//    }

    public void saveUserDetails(Long id) {
        Member member = memberRepository.findById(id).get();

        CustomUserDetails customUserDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }


//    @Test
////    @Rollback(value = false)
//    void 에타매핑() throws IOException {
//        saveUserDetails(7L);
//
//        String xml = """
//            <?xml version="1.0" encoding="UTF-8"?>
//            <response>
//              <table id="51972939" is_deleted="0" name="시간표1" year="2025" semester="1" priv="0" primary="1" created_at="2025-02-12 16:07:39" updated_at="2025-05-20 11:25:39">
//                <subject id="7215635">
//                  <internal value="GEDC010-01"/>
//                  <name value="성균논어"/>
//                  <professor value="길훈섭"/>
//                  <time value="월09:00-09:50【50307】&lt;br>월10:00-10:50【50307】">
//                    <data day="0" starttime="108" endtime="130" place="50307"/>
//                  </time>
//                  <place value=""/>
//                  <credit value="2"/>
//                  <closed value="0"/>
//                </subject>
//                <!-- 이하 생략 -->
//              </table>
//            </response>
//            """;
//
//        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());
//        timetableService.everytimeMapping(doc, true);
//    }

    @Test
    @Rollback(value = false)
    void 에타모든시간표조회() throws IOException {
        saveUserDetails(1L);
        List<CreateTimetableWithExternalLecturesDto> dtos = timetableService.allEverytimeMapping("https://everytime.kr/@1eMc2T1GfAQBsE4LK7gb");
        for(CreateTimetableWithExternalLecturesDto dto : dtos) {
            timetableService.createTimetableWithExternalLectures(dto);
        }

    }

}