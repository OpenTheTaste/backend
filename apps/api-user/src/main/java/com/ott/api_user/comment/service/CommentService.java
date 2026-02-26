package com.ott.api_user.comment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ott.api_user.comment.dto.CommentResponse;
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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final ContentsRepository contentsRepository;

    public PageResponse<CommentResponse> getContentsCommentList(Long contentsId, int page, int size) {

        if (!contentsRepository.findByIdAndStatus(contentsId, Status.ACTIVE)) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Comment> commentPage = commentRepository.findByContentsIdAndStatusWithMember(contentsId, Status.ACTIVE,
                pageable);

        List<CommentResponse> responseList = commentPage.getContent().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        PageInfo pageInfo = PageInfo.toPageInfo(
                commentPage.getNumber(),
                commentPage.getTotalPages(),
                commentPage.getSize());

        return PageResponse.toPageResponse(pageInfo, responseList);
    }
}
