package org.fiddich.coreinfradomain.domain.Member;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.Lecture.School;
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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "school_id")
//    private School school;

//    private String school;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timetable> timetables = new ArrayList<>();

    private String department;
    private String role;

//    @Builder.Default
//    @OneToMany(mappedBy = "member")
//    private List<Timetable> timetables = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> requestFriendships = new ArrayList<>();

    // 편의 메서드
    public List<Member> getFriends() {
        List<Member> received = this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.ACCEPTED)
                .map(Friendship::getRequester)
                .toList();

        List<Member> requested = this.requestFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.ACCEPTED)
                .map(Friendship::getReceiver)
                .toList();

        // 두 리스트를 합치기
        List<Member> friends = new ArrayList<>();
        friends.addAll(received);
        friends.addAll(requested);
        return friends;
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
