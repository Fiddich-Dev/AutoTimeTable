package com.fiddich.AutoTimeTable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Friendship {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private Member requester;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus friendshipStatus;

}
