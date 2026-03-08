package com.ott.api_user.comment.service;

import com.ott.api_user.comment.dto.request.CreateCommentRequest;
import com.ott.api_user.comment.dto.request.UpdateCommentRequest;
import com.ott.api_user.comment.dto.response.CommentResponse;
import com.ott.api_user.comment.dto.response.ContentsCommentResponse;
import com.ott.api_user.comment.dto.response.MyCommentResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.comment.domain.Comment;
import com.ott.domain.comment.repository.CommentRepository;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class CommentService {

        private final CommentRepository commentRepository;
        private final ContentsRepository contentsRepository;
        private final MemberRepository memberRepository;

        // 댓글 작성
        @Transactional
        public CommentResponse createComment(Long memberId, CreateCommentRequest request) {

                Member findMember = memberRepository.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                Contents contents = contentsRepository.findByIdAndStatus(request.getContentId(), Status.ACTIVE)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CONTENTS_NOT_FOUND));

                // 한 유저가 한 콘텐츠에 여러 댓글 허용?
                Comment saved = commentRepository.save(
                        Comment.builder()
                                .member(findMember)
                                .contents(contents)
                                .content(request.getContent())
                                .isSpoiler(request.getIsSpoiler())
                                .build()
                );

                return CommentResponse.from(saved);

        }

        // 댓글 수정 - 본인 댓글만 수정 가능
        @Transactional
        public CommentResponse updateComment(Long memberId, Long commentId, @Valid UpdateCommentRequest request) {
                Comment comment = commentRepository.findByIdAndStatus(commentId, Status.ACTIVE)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

                // 본인만 수정 가능
                if (!comment.getMember().getId().equals(memberId)) {
                throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
                }

                comment.update(request.getContent(), request.getIsSpoiler());

                return CommentResponse.from(comment);
        }

        // 댓글 삭제 - 본인 댓글만 삭제 가능
        @Transactional
        public void deleteComment(Long memberId, Long commentId) {
                Comment comment = commentRepository.findByIdAndStatus(commentId, Status.ACTIVE)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

                // 본인만 삭제 가능 -> softDelete
                if (!comment.getMember().getId().equals(memberId)) {
                throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
                }

                comment.updateStatus(Status.DELETE);
        }

        // 댓글 조회 - 본인 댓글만 조회 가능(최신순)
        @Transactional(readOnly = true)
        public PageResponse<MyCommentResponse> getMyComments(
                Long memberId,
                Integer page,
                Integer size) {

                PageRequest pageable = PageRequest.of(page, size);

                Page<Comment> commentPage = commentRepository.findMyComments(memberId, Status.ACTIVE, pageable);

                List<MyCommentResponse> responseList = commentPage.getContent().stream()
                        .map(MyCommentResponse::from)
                        .toList();

                PageInfo pageInfo = PageInfo.toPageInfo(
                        commentPage.getNumber(),
                        commentPage.getTotalPages(),
                        commentPage.getSize()
                );

                return PageResponse.toPageResponse(pageInfo, responseList);
        }
        
        // 콘텐츠 상세 조회 댓글 목록
        @Transactional(readOnly = true)
        public PageResponse<ContentsCommentResponse> getContentsCommentList(Long mediaId, int page, int size, boolean includeSpoiler) {

                // mediaId를 기준으로 Contents 엔티티 조회
                Contents contents = contentsRepository.findByMediaIdAndStatusAndMedia_PublicStatus(mediaId, Status.ACTIVE, PublicStatus.PUBLIC)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENTS_NOT_FOUND));

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

                Page<Comment> commentPage = commentRepository.findByContents_IdAndStatusWithSpoilerCondition(contents.getId(), Status.ACTIVE, includeSpoiler, pageable);

                List<ContentsCommentResponse> responseList = commentPage.getContent().stream()
                        .map(ContentsCommentResponse::from)
                        .collect(Collectors.toList());

                PageInfo pageInfo = PageInfo.toPageInfo(
                        commentPage.getNumber(),
                        commentPage.getTotalPages(),
                        commentPage.getSize());

                return PageResponse.toPageResponse(pageInfo, responseList);
        }
}
