package org.fiddich.coreinfradomain.domain.Member;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Timetable> timetables = new ArrayList<>();

    private String studentId;
    private String password;
    private String profileImage;
    private String username;
    private String role;


    @Builder.Default
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> requestFriendships = new ArrayList<>();

    // ------편의 메서드------

    // 비밀번호 변경
    public void changePassword(String password) {
        this.password = password;
    }

    // 친구 조회
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

    // 대기중인 요청 조회
    public List<Member> getPendingFriends() {
        return this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.PENDING)
                .map(Friendship::getRequester)
                .toList();
    }

    // 친구 요청
    public void requestFriendship(Member receiver) {
        Friendship friendship = Friendship.builder()
                .requester(this)
                .receiver(receiver)
                .friendshipStatus(FriendshipStatus.PENDING)
                .build();

        this.requestFriendships.add(friendship);
        receiver.getReceivedFriendships().add(friendship);
    }

    // 친구 요청 수락
    public void acceptFriendship(Member requester) {
        Friendship friendship = this.receivedFriendships.stream()
                .filter(f -> f.getRequester().equals(requester) && f.getFriendshipStatus() == FriendshipStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 친구 요청을 찾을 수 없습니다."));

        friendship.accept();
    }

    // 친구 요청 거절
    public void rejectFriendship(Member requester) {
        Friendship friendship = this.receivedFriendships.stream()
                .filter(f -> f.getRequester().equals(requester) && f.getFriendshipStatus() == FriendshipStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 친구 요청을 찾을 수 없습니다."));

        this.receivedFriendships.remove(friendship);
        requester.getRequestFriendships().remove(friendship);
    }

    // 친구 삭제
    public void removeFriend(Member friend) {
//        this.getFriends().remove(friend); // 안되는지 테스트 // 진짜 안되네
//        // 요청자로서 제거
        this.requestFriendships.removeIf(f -> f.getReceiver().equals(friend) && f.getFriendshipStatus() == FriendshipStatus.ACCEPTED);
        friend.receivedFriendships.removeIf(f -> f.getRequester().equals(this) && f.getFriendshipStatus() == FriendshipStatus.ACCEPTED);
//
//        // 수신자로서 제거
        this.receivedFriendships.removeIf(f -> f.getRequester().equals(friend) && f.getFriendshipStatus() == FriendshipStatus.ACCEPTED);
        friend.requestFriendships.removeIf(f -> f.getReceiver().equals(this) && f.getFriendshipStatus() == FriendshipStatus.ACCEPTED);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
