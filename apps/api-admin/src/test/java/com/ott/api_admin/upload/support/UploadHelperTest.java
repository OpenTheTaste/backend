package com.ott.api_admin.upload.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.ott.common.web.exception.BusinessException;
import com.ott.common.web.exception.ErrorCode;
import com.ott.domain.member.domain.Member;
import com.ott.domain.member.repository.MemberRepository;
import com.ott.infra.s3.service.S3PresignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UploadHelperTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3PresignService s3PresignService;

    private UploadHelper uploadHelper;

    @BeforeEach
    void setUp() {
        uploadHelper = new UploadHelper(memberRepository, s3PresignService);
        ReflectionTestUtils.setField(uploadHelper, "multipartDefaultPartSizeBytes", 16L * 1024L * 1024L);
        ReflectionTestUtils.setField(uploadHelper, "multipartMaxParts", 2000);
    }

    // 파일명에 특수문자/공백이 들어간 경우에도 허용된 문자로 정리되는지 검증
    @Test
    void sanitizeFileName_removesIllegalCharacters() {
        String sanitized = uploadHelper.sanitizeFileName(" /foo@bar ,test.mp4 ");
        assertThat(sanitized).isEqualTo("foobartest.mp4");
        assertThat(sanitized).endsWith(".mp4");
    }

    // Multipart plan이 음수가 아닌 part count를 반환하는지 확인
    @Test
    void getMultipartPlan_returnsValidPlan() {
        UploadHelper.MultipartUploadPlan plan = uploadHelper.getMultipartPlan(10);
        assertThat(plan.totalPartCount()).isGreaterThan(0);
        assertThat(plan.partSizeBytes()).isGreaterThanOrEqualTo(16L * 1024L * 1024L);
    }

    // origin object key와 기대 URL이 다르면 BusinessException 발생
    @Test
    void validateOriginObjectKey_mismatchedUrlThrows() {
        String objectKey = "short-forms/1/origin";
        when(s3PresignService.toObjectUrl(objectKey)).thenReturn("http://origin-url");

        assertThatThrownBy(() ->
                uploadHelper.validateOriginObjectKey(objectKey, "https://wrong-url", ErrorCode.SHORTFORM_ORIGIN_OBJECT_KEY_MISMATCH))
                .isInstanceOf(BusinessException.class);
    }

    // Multipart part count를 가져올 때, uploadHelper 내부 로직 호출을 보장
    @Test
    void getMultipartPartCount_usesMultipartPlan() {
        uploadHelper = new UploadHelper(memberRepository, s3PresignService);
        ReflectionTestUtils.setField(uploadHelper, "multipartDefaultPartSizeBytes", 5L * 1024L * 1024L);
        ReflectionTestUtils.setField(uploadHelper, "multipartMaxParts", 2000);

        int partCount = uploadHelper.getMultipartPartCount(1);
        assertThat(partCount).isPositive();
    }
}
