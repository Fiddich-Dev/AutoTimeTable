package com.fiddich.AutoTimeTable.entity;

import com.fiddich.AutoTimeTable.repository.MemberRepository;
import jakarta.persistence.*;
import lombok.*;

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
    private String profileImage;
    private String name;
    private String school;
    private String department;

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
        List<Member> pendingFriends = new ArrayList<>();

        this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.PENDING)
                .map(f -> f.getReceiver())
                .forEach(m -> pendingFriends.add(m));

        return pendingFriends;
    }

}
