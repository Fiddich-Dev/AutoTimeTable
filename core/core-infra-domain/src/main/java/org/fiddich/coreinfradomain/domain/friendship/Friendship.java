package org.fiddich.coreinfradomain.domain.friendship;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.Member.Member;

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

    // 도메인 메서드
    public void accept() {
        this.friendshipStatus = FriendshipStatus.ACCEPTED;
    }

    public void sendFriendshipRequest(Member requester, Member receiver) {
//        this.requester = requester;
//        this.receiver = receiver;
        receiver.getReceivedFriendships().add(this);
        requester.getRequestFriendships().add(this);
    }

}
