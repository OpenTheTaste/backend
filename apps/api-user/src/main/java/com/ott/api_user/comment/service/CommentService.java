package com.ott.api_user.comment.service;

import com.ott.api_user.comment.dto.request.CreateCommentRequest;
import com.ott.api_user.comment.dto.request.UpdateCommentRequest;
import com.ott.api_user.comment.dto.response.CommentResponse;
import com.ott.api_user.comment.dto.response.MyCommentResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.common.web.response.PageInfo;
import com.ott.common.web.response.PageResponse;
import com.ott.domain.comment.domain.Comment;
import com.ott.domain.comment.repository.CommentRepository;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));

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


}
