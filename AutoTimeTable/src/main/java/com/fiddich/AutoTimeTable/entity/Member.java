package com.fiddich.AutoTimeTable.entity;

import com.fiddich.AutoTimeTable.repository.MemberRepository;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String studentId;
    private String profileImage;
    private String name;
    private String school;
    private String department;

    @OneToMany(mappedBy = "member")
    private List<Timetable> timetables = new ArrayList<>();

    @OneToMany(mappedBy = "receiver")
    private List<Friendship> receivedFriendships = new ArrayList<>();

    // 편의 메서드
    public List<Member> getFriends() {
        List<Member> friends = new ArrayList<>();

        this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.ACCEPTED)
                .map(f -> f.getReceiver())
                .forEach(m -> friends.add(m));

        return friends;
    }

    public List<Member> getPendingFriends() {
        List<Member> pendingFriends = new ArrayList<>();

        this.receivedFriendships.stream()
                .filter(f -> f.getFriendshipStatus() == FriendshipStatus.PENDING)
                .map(f -> f.getReceiver())
                .forEach(m -> pendingFriends.add(m));

        return pendingFriends;
    }

//    public Member() {
//    }
}


