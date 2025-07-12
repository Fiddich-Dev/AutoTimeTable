package org.fiddich.api.domain.member.dto;

import lombok.Data;
import org.fiddich.coreinfradomain.domain.Member.Member;

@Data
public class SearchMemberDto {

    private Long id;

    private String studentId;
    private String profileImage;
    private String username;
    private SearchFriendStatus status;

    public SearchMemberDto(Member member, SearchFriendStatus status) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.studentId = member.getStudentId();
        this.status = status;
    }

}
