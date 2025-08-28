package org.fiddich.coreinfradomain.domain.Member.repository;

import org.fiddich.coreinfradomain.domain.Member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    Page<Member> findByStudentIdContaining(String keyword, Pageable pageable);
}
