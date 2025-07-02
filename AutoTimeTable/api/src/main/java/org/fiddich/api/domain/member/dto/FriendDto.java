package org.fiddich.api.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.coreinfradomain.domain.Member.Member;

@Getter
@AllArgsConstructor
public class FriendDto {

    private Long id;

    private String studentId;
    private String profileImage;
    private String username;
//    private String school;
    private String department;


    public static FriendDto memberToFriendDto(Member member) {
        return new FriendDto(member.getId(), member.getStudentId(), member.getProfileImage(), member.getUsername(), member.getDepartment());
    }

}
