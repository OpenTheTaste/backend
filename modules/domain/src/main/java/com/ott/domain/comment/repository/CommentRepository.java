package com.ott.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ott.domain.comment.domain.Comment;
import com.ott.domain.common.Status;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 작성자, 댓글
    @Query("SELECT c FROM Comment c JOIN FETCH c.member WHERE c.contents.id = :contentsId AND c.status = :status")
    Page<Comment> findByContentsIdAndStatusWithMember(
            @Param("contentsId") Long contentsId,
            @Param("status") Status status,
            Pageable pageable);
}