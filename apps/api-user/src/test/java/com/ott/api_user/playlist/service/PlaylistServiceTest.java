package com.ott.api_user.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ott.api_user.playlist.dto.response.TagPlaylistResponse;
import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.category.domain.Category;
import com.ott.domain.common.MediaType;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.domain.Provider;
import com.ott.domain.member.domain.Role;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media.repository.TagContentProjection;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
import com.ott.domain.watch_history.repository.RecentWatchProjection;
import com.ott.domain.watch_history.repository.WatchHistoryRepository;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private ContentsRepository contentsRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private WatchHistoryRepository watchHistoryRepository;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    void getRecommendContentsByTag_returnsMappedResponses() {
        Long memberId = 1L;
        Long tagId = 2L;

        Member member = Member.builder()
                .id(memberId)
                .email("tester@ott.com")
                .nickname("tester")
                .role(Role.MEMBER)
                .provider(Provider.KAKAO)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        Category tagCategory = Category.builder().id(1L).name("default").build();
        Tag tag = Tag.builder().id(tagId).category(tagCategory).name("tag").build();
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        TagContentProjection projection = new TagContentProjection(10L, "poster-url", MediaType.CONTENTS);
        when(mediaRepository.findRecommendContentsByTagId(tagId, 20))
                .thenReturn(List.of(projection));

        List<TagPlaylistResponse> result = playlistService.getRecommendContentsByTag(memberId, tagId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMediaId()).isEqualTo(10L);
        assertThat(result.get(0).getPosterUrl()).isEqualTo("poster-url");
        assertThat(result.get(0).getMediaType()).isEqualTo(MediaType.CONTENTS);
    }

    @Test
    void getRecommendContentsByTag_throwsWhenMemberMissing() {
        Long memberId = 5L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.getRecommendContentsByTag(memberId, 99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(tagRepository, never()).findById(any());
    }

    @Test
    void getWatchHistoryPlaylist_returnsPageResponse() {
        Long memberId = 3L;
        int page = 2;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(
                Member.builder()
                        .id(memberId)
                        .email("watcher@ott.com")
                        .nickname("watcher")
                        .role(Role.MEMBER)
                        .provider(Provider.KAKAO)
                        .build()));

        Pageable pageable = PageRequest.of(page, 10);
        List<RecentWatchProjection> projections = List.of(
                new RecentWatchProjection(11L, MediaType.SERIES, "first-poster", 30, 600),
                new RecentWatchProjection(12L, MediaType.CONTENTS, "second-poster", 0, 360)
        );
        Page<RecentWatchProjection> watchHistoryPage = new PageImpl<>(projections, pageable, 5);

        when(watchHistoryRepository.findWatchHistoryByMemberId(memberId, pageable)).thenReturn(watchHistoryPage);

        var response = playlistService.getWatchHistoryPlaylist(memberId, page);

        assertThat(response.getPageInfo().getCurrentPage()).isEqualTo(page);
        assertThat(response.getPageInfo().getTotalPage()).isEqualTo(1);
        assertThat(response.getDataList()).hasSize(2);
        assertThat(response.getDataList().get(0).getMediaId()).isEqualTo(11L);
        assertThat(response.getDataList().get(1).getDuration()).isEqualTo(360);
    }
}
