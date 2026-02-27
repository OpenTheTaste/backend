package com.ott.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.comment.domain.Comment;
import com.ott.domain.common.Status;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 1. 스포 포함 토글 ON -> 전체 댓글 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.member WHERE c.contents.id = :contentsId AND c.status = :status")
    Page<Comment> findByContentsIdAndStatusWithMember(
            @Param("contentsId") Long contentsId,
            @Param("status") Status status,
            Pageable pageable);

    // 2. 스포 포함 토글 OFF -> 스포 없는 댓글만 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.member WHERE c.contents.id = :contentsId AND c.status = :status AND c.isSpoiler = false")
    Page<Comment> findByContentsIdAndStatusAndIsSpoilerFalseWithMember(
            @Param("contentsId") Long contentsId,
            @Param("status") Status status,
            Pageable pageable);
}