package com.ott.domain.comment.repository;

import com.ott.domain.comment.domain.Comment;
import com.ott.domain.common.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    @EntityGraph(attributePaths = {"member", "contents", "contents.media"})
    Optional<Comment> findByIdAndStatus(Long id, Status status);
}
