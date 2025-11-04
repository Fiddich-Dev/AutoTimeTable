package org.fiddich.api.domain.friend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.fiddich.coreinfradomain.domain.Member.Member;

@Builder
public record MemberInfoResponse(
    Long id,
    String studentId,
    String profileImage,
    String username
){
    public static MemberInfoResponse from(Member member) {
        return MemberInfoResponse.builder()
                .id(member.getId())
                .studentId(member.getStudentId())
                .profileImage(member.getProfileImage())
                .username(member.getUsername())
                .build();
    }
}
