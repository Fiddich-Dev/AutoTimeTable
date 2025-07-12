package org.fiddich.api.domain.member;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.*;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.friendship.repository.FriendshipRepository;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EntityManager em;

    public Long join(JoinDto joinDto) {

//         학번이 안겹치는지 확인하는 로직
        if(memberRepository.findByStudentId(joinDto.getStudentId()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 회원입니다");
        }

//        School school = memberRepository.findSchoolByName(joinDto.getSchool());

        Member member = Member.builder()
                .studentId(joinDto.getStudentId())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .username(joinDto.getUsername())
                                                .build();

        memberRepository.save(member);
        return member.getId();
    }

    public boolean isDuplicatedMember(String studentId) {
        if(memberRepository.findByStudentId(studentId).isPresent()) {
            return true;
        }
        return false;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public List<Member> findFriendshipRequest(Long id) {
        return memberRepository.findFriendshipRequest(id);
    }

    public void withdrawal() {
         CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // redis에서 studentId + ":refreshToken" 키 삭제
        String studentId = customUserDetails.getStudentId();
//        String school = customUserDetails.getSchool();
        Long id = customUserDetails.getId();
        log.info("탈퇴 요청 PK: {}", id);
//        log.info("school = {}", school);
        log.info("studentId = {}", studentId);

        redisUtil.deleteKey(studentId + ":refreshToken");




        memberRepository.deleteById(id);
    }

    // 친구 요청 보내기
    public void sendFriendRequest(FriendShipDto requestFriendshipDto) {

        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member requester = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member receiver = memberRepository.findById(requestFriendshipDto.getMemberId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        if(friendshipRepository.findByRequesterAndReceiver(requester.getId(), receiver.getId()).isPresent()) {
            throw new DuplicateKeyException("이미 친구 요청된 상태입니다.");
        }
        if(friendshipRepository.findByRequesterAndReceiver(receiver.getId(), requester.getId()).isPresent()) {
            throw new DuplicateKeyException("이미 친구로 부터 요청받은 상태입니다.");
        }

        // 지금은 내가 보낸 요청이 이미 있는지만 확인하지만
        // 상대가 이미 나한테 요청을 보낸 상태도 확인해야한다
        // 해결

        Friendship friendship = Friendship.builder()
                .friendshipStatus(FriendshipStatus.PENDING)
                .requester(requester)
                .receiver(receiver)
                .build();

        System.out.println("reqeuster " + requester.getId() + " : " + "receiver " + receiver.getId());

        friendship.sendFriendshipRequest(requester, receiver);

        friendshipRepository.saveFriendship(friendship);
    }

    // 친구요청 수락
    public void acceptFriendRequest(FriendShipDto receiveFriendshipDto) {

        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member receiver = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member requester = memberRepository.findById(receiveFriendshipDto.getMemberId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(requester.getId(), receiver.getId()).orElseThrow(() -> new NoSuchElementException("해당 요청이 유효하지 않습니다."));

        if(friendship.getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
            throw new DuplicateKeyException("이미 수락된 요청입니다.");
        }

        friendshipRepository.acceptFriendRequest(friendship);
    }

    // 친구요청 거절
    public void rejectFriendRequest(Long requesterId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member receiver = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member requester = memberRepository.findById(requesterId).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(requester.getId(), receiver.getId()).orElseThrow(() -> new NoSuchElementException("해당 요청이 유효하지 않습니다."));

        friendshipRepository.rejectFriendRequest(friendship);
    }

    // 수락한 친구들 조회
    public List<Member> findAcceptedRequests(Member receiver) {
        return friendshipRepository.findAcceptedRequests(receiver);
    }

    // 수락 대기중인 친구들 조회
    public List<Member> findPendingRequests(Member receiver) {
        return friendshipRepository.findPendingRequests(receiver);
    }

    // 내가 보낸요청 보기
    public List<Member> findPendingMyRequest(Member requester) {
        return friendshipRepository.findPendingMyRequest(requester);
    }

    // 내 친구들 조회
    public List<InquiryMemberDto> findAllFriends() {
        log.warn("연관관계 메서드 생각해보기");
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return friendshipRepository.findAllFriends(customUserDetails.getId()).stream().map(InquiryMemberDto::memberToFriendDto).collect(Collectors.toList());
//        return memberRepository.findById(customUserDetails.getId()).get().getFriends().stream().map(FriendDto::memberToFriendDto).collect(Collectors.toList());
    }

    // 받은 요청중 보류중인거
    public List<InquiryMemberDto> findPendingResponse() {
        log.warn("연관관계 메서드 생각해보기");
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return friendshipRepository.findPendingResponse(customUserDetails.getId()).stream().map(InquiryMemberDto::memberToFriendDto).collect(Collectors.toList());
    }

    // 보낸 요청중 보류중인거
    public List<InquiryMemberDto> findPendingRequest() {
        log.warn("연관관계 메서드 생각해보기");
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return friendshipRepository.findPendingRequest(customUserDetails.getId()).stream().map(InquiryMemberDto::memberToFriendDto).collect(Collectors.toList());
    }

    public AuthSchoolResponse authSchool(AuthSchoolDto authSchoolDto) {

        String school = authSchoolDto.getSchool();
        String id = authSchoolDto.getId();
        String password = authSchoolDto.getPassword();
        school = SchoolNameConverter.convertToEng(school);
        String url = "";
        String schoolUrl = "";

        switch (school) {
            case "SKKU":
                url = "https://login.skku.edu/loginAction";
                password = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
                schoolUrl = "https://www.skku.edu/skku/index.do";
                break;

            default:
                throw new NoSuchElementException("입력한 학교 없음");
//                break;
        }

        try {
            String postBody = String.format(
                    "{ \"lang\": \"ko\", \"userid\": \"%s\", \"userpwd\": \"%s\" }",
                    id, password
            );

            // OkHttp 객체 생성
            OkHttpClient client = new OkHttpClient();

            // RequestBody 생성
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), postBody);

            // Post 객체 생성
            Request.Builder builder = new Request.Builder().url(url)
                    .post(requestBody);
            Request request = builder.build();

            // 요청 전송
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                // 응답 Body
                ResponseBody body = response.body();
                if (body != null) {
                    String responseBodyStr = body.string();
                    System.out.println("Response: " + responseBodyStr);
                    Gson gson = new Gson();
                    AuthSchoolResponse authSchoolResponse = gson.fromJson(responseBodyStr, AuthSchoolResponse.class);

                    return authSchoolResponse;
                }
            } else
                System.err.println("Error Occurred");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        Member me = memberRepository.findByStudentId(resetPasswordDto.getStudentId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.setPassword(encodedPassword);
    }

    public void changePassword(ResetPasswordDto resetPasswordDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.setPassword(encodedPassword);
    }

    public List<SearchMemberDto> searchMemberByStudentId(String keyword) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        List<Member> myFriends = me.getFriends();

        // 내가 요청 보냈거나, 받은 친구를 pending으로 표시
        List<Member> pendingFriends = new ArrayList<>();
        pendingFriends.addAll(friendshipRepository.findPendingRequests(me)); // 내가 받은 요청
        pendingFriends.addAll(friendshipRepository.findPendingRequest(me.getId())); // 내가 보낸 요청

        List<Member> members = memberRepository.findByStudentIdContaining(keyword);
        members.remove(me);

        return members.stream()
                .map(member -> {
                    SearchFriendStatus status;
                    if (myFriends.contains(member)) {
                        status = SearchFriendStatus.ALREADY_FRIEND;
                    } else if (pendingFriends.contains(member)) {
                        status = SearchFriendStatus.PENDING;
                    } else {
                        status = SearchFriendStatus.NOT_FRIEND;
                    }
                    return new SearchMemberDto(member, status);
                })
                .toList();
    }


    public void deleteFriend(Long friendId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member friendToRemove = memberRepository.findById(friendId).orElseThrow(() -> new NoSuchElementException("해당 친구가 존재하지 않습니다."));
        // friendship에서 me, friend 이거나 friend, me인 경우를 모두 제거한다
        friendshipRepository.deleteFriend(customUserDetails.getId(), friendId);
    }



}
