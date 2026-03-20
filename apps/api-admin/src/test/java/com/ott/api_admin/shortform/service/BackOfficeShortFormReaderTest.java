package com.ott.api_admin.shortform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ott.api_admin.shortform.mapper.BackOfficeShortFormMapper;
import com.ott.api_admin.upload.support.UploadHelper;
import com.ott.common.web.exception.ErrorCode;
//import com.ott.domain.common.MediaStatus;
import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.contents.repository.ContentsRepository;
import com.ott.domain.media.domain.Media;
import com.ott.domain.media.repository.MediaRepository;
import com.ott.domain.media_tag.repository.MediaTagRepository;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.domain.Provider;
import com.ott.domain.member.domain.Role;
import com.ott.domain.series.repository.SeriesRepository;
import com.ott.domain.short_form.domain.ShortForm;
import com.ott.domain.short_form.repository.ShortFormRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@ExtendWith(MockitoExtension.class)
class BackOfficeShortFormReaderTest {

    @Mock
    private BackOfficeShortFormMapper backOfficeShortFormMapper;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaTagRepository mediaTagRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private ContentsRepository contentsRepository;

    @Mock
    private ShortFormRepository shortFormRepository;

    @Mock
    private UploadHelper uploadHelper;

    @InjectMocks
    private BackOfficeShortFormReader reader;

    // 숏폼 업로드 정보 조회 시 origin URL 검증과 파트 계산이 순차적으로 수행되는지 확인
    @Test
    void getShortFormUploadInfo_validatesOriginAndReturnsCount() {
        Long shortFormId = 99L;
        String objectKey = "short-forms/99/origin";
        ShortForm shortForm = ShortForm.builder()
                .id(shortFormId)
                .media(buildMedia(88L))
                .duration(30)
                .videoSize(1200)
                .originUrl("http://origin/key")
                .masterPlaylistUrl("http://master")
                .build();

        when(shortFormRepository.findWithMediaAndUploaderByShortFormId(shortFormId)).thenReturn(Optional.of(shortForm));
        when(uploadHelper.getMultipartPartCount(shortForm.getVideoSize())).thenReturn(8);

        List<GrantedAuthority> authorities = List.of((GrantedAuthority) () -> Role.MEMBER.getKey());
        var auth = new UsernamePasswordAuthenticationToken(55L, null, authorities);
        int totalParts = reader.getShortFormUploadInfo(shortFormId, objectKey, auth);

        verify(uploadHelper).validateOriginObjectKey(objectKey, shortForm.getOriginUrl(), ErrorCode.SHORTFORM_ORIGIN_OBJECT_KEY_MISMATCH);
        assertThat(totalParts).isEqualTo(8);
    }

    private static Media buildMedia(Long id) {
        Member uploader = Member.builder()
                .id(1L)
                .email("uploader@ott.com")
                .nickname("uploader")
                .role(Role.MEMBER)
                .provider(Provider.KAKAO)
                .build();

        return Media.builder()
                .id(id)
                .uploader(uploader)
                .title("short")
                .description("desc")
                .posterUrl("poster")
                .thumbnailUrl("thumb")
                .bookmarkCount(0L)
                .likesCount(0L)
                .mediaType(MediaType.SHORT_FORM)
                .publicStatus(PublicStatus.PUBLIC)
                //.mediaStatus(MediaStatus.COMPLETED)
                .build();
    }
}
