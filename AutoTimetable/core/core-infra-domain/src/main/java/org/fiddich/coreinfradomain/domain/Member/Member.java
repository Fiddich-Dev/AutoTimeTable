package org.fiddich.coreinfradomain.domain.Member;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String studentId;
    private String password;
    private String profileImage;
    private String username;
    private String school;
    private String department;
    private String role;

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Timetable> timetables = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "receiver")
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requester")
    private List<Friendship> requestFriendships = new ArrayList<>();

    // 편의 메서드
    public List<Member> getFriends() {
        return this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.ACCEPTED)
                .map(Friendship::getRequester)
                .toList();
    }

    public List<Member> getPendingFriends() {

        return this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.PENDING)
                .map(Friendship::getRequester)
                .toList();
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
