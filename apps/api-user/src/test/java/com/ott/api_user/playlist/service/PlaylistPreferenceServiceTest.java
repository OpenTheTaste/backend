package com.ott.api_user.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ott.domain.category.domain.Category;
import com.ott.domain.common.Status;
import com.ott.domain.likes.repository.LikesRepository;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.playback.repository.PlaybackRepository;
import com.ott.domain.preferred_tag.repository.PreferredTagRepository;
import com.ott.domain.tag.domain.Tag;
import com.ott.domain.tag.repository.TagRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PlaylistPreferenceServiceTest {

    @Mock
    private PreferredTagRepository preferredTagRepository;

    @Mock
    private PlaybackRepository playbackRepository;

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private MediaTagRepository mediaTagRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PlaylistPreferenceService playlistPreferenceService;

    @Test
    void getTopTags_combinesPreferredAndPlaybackScores() {
        Long memberId = 10L;
        List<Long> preferredTagIds = List.of(1L, 2L);
        when(preferredTagRepository.findTagIdsByMemberId(memberId, Status.ACTIVE)).thenReturn(preferredTagIds);

        List<Long> playedMediaIds = List.of(100L, 200L);
        when(playbackRepository.findRecentPlayedMediaIds(eq(memberId), eq(Status.ACTIVE), any(Pageable.class)))
                .thenReturn(playedMediaIds);

        when(mediaTagRepository.findTagIdsByMediaIds(playedMediaIds))
                .thenReturn(List.of(2L, 3L, 3L));

        Category category = Category.builder().id(99L).name("category").build();
        Tag tag1 = Tag.builder().id(1L).category(category).name("drama").build();
        Tag tag2 = Tag.builder().id(2L).category(category).name("comedy").build();
        Tag tag3 = Tag.builder().id(3L).category(category).name("documentary").build();

        when(tagRepository.findAllById(anyList())).thenReturn(List.of(tag1, tag2, tag3));

        List<Tag> result = playlistPreferenceService.getTopTags(memberId);

        assertThat(result).containsExactly(tag2, tag3, tag1);
        verify(tagRepository, never()).findAll();
    }

    @Test
    void getTopTags_withNoHistoryReturnsFallbackTags() {
        Long memberId = 99L;
        when(preferredTagRepository.findTagIdsByMemberId(memberId, Status.ACTIVE)).thenReturn(List.of());
        when(playbackRepository.findRecentPlayedMediaIds(eq(memberId), eq(Status.ACTIVE), any(Pageable.class)))
                .thenReturn(List.of());
        when(likesRepository.findRecentLikedMediaIds(eq(memberId), eq(Status.ACTIVE), any(Pageable.class)))
                .thenReturn(List.of());

        Tag fallbackTag1 = Tag.builder().id(11L)
                .category(Category.builder().id(5L).name("cat").build())
                .name("A")
                .build();
        Tag fallbackTag2 = Tag.builder().id(12L)
                .category(Category.builder().id(6L).name("cat").build())
                .name("B")
                .build();
        Tag fallbackTag3 = Tag.builder().id(13L)
                .category(Category.builder().id(7L).name("cat").build())
                .name("C")
                .build();
        Tag fallbackTag4 = Tag.builder().id(14L)
                .category(Category.builder().id(8L).name("cat").build())
                .name("D")
                .build();

        List<Tag> availableTags = List.of(fallbackTag1, fallbackTag2, fallbackTag3, fallbackTag4);
        when(tagRepository.findAll()).thenReturn(availableTags);

        List<Tag> result = playlistPreferenceService.getTopTags(memberId);

        assertThat(result).hasSize(3);
        assertThat(availableTags).containsAll(result);
        verify(tagRepository, never()).findAllById(anyList());
    }

    @Test
    void getTotalTagScores_accumulatesPreferencesPlaybackAndLikes() {
        Long memberId = 2L;
        when(preferredTagRepository.findTagIdsByMemberId(memberId, Status.ACTIVE)).thenReturn(List.of(1L));

        List<Long> playedMediaIds = List.of(10L);
        when(playbackRepository.findRecentPlayedMediaIds(eq(memberId), eq(Status.ACTIVE), any(Pageable.class)))
                .thenReturn(playedMediaIds);
        when(mediaTagRepository.findTagIdsByMediaIds(playedMediaIds)).thenReturn(List.of(1L, 2L));

        List<Long> likedMediaIds = List.of(20L);
        when(likesRepository.findRecentLikedMediaIds(eq(memberId), eq(Status.ACTIVE), any(Pageable.class)))
                .thenReturn(likedMediaIds);
        when(mediaTagRepository.findTagIdsByMediaIds(likedMediaIds)).thenReturn(List.of(2L, 3L));

        Map<Long, Integer> scores = playlistPreferenceService.getTotalTagScores(memberId);

        assertThat(scores).containsExactlyInAnyOrderEntriesOf(Map.of(1L, 8, 2L, 5, 3L, 2));
    }
}
