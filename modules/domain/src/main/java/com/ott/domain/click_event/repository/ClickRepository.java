package com.ott.domain.click_event.repository;

import com.ott.domain.click_event.domain.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ClickRepository extends JpaRepository<ClickEvent, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.status = 'DELETE' WHERE c.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);

}
