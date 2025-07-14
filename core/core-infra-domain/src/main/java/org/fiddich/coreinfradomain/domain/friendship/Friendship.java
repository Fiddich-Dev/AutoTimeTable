package org.fiddich.coreinfradomain.domain.friendship;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.Member.Member;

import java.util.Objects;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Friendship {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Member requester;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus friendshipStatus;

    // 수락
    public void accept() {
        this.friendshipStatus = FriendshipStatus.ACCEPTED;
    }

    // 친구요청 보내기
    public void sendFriendshipRequest(Member requester, Member receiver) {
        receiver.getReceivedFriendships().add(this);
        requester.getRequestFriendships().add(this);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
