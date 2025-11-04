package org.fiddich.api.domain.friend.dto.response;

import lombok.Builder;
import org.fiddich.coreinfradomain.domain.Member.Member;

@Builder
public record SearchMemberResponse(
    Long id,
    String studentId,
    String profileImage,
    String username,
    SearchFriendStatus status
) {
    public static SearchMemberResponse from(Member member, SearchFriendStatus status) {
        return SearchMemberResponse.builder()
                .id(member.getId())
                .studentId(member.getStudentId())
                .profileImage(member.getProfileImage())
                .username(member.getUsername())
                .status(status)
                .build();
    }
}
