package org.fiddich.api.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.coreinfradomain.domain.Member.Member;

@Getter
@AllArgsConstructor
public class InquiryMemberDto {

    private Long id;

    private String studentId;
    private String profileImage;
    private String username;

    public InquiryMemberDto(Member member) {
        this.id = member.getId();
        this.studentId = member.getStudentId();
        this.profileImage = member.getProfileImage();
        this.username = member.getUsername();
    }

    public static InquiryMemberDto memberToFriendDto(Member member) {
        return new InquiryMemberDto(member.getId(), member.getStudentId(), member.getProfileImage(), member.getUsername());
    }

}
