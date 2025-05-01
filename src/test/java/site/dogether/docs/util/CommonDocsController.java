package site.dogether.docs.util;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.docs.challengegroup.enumtype.ChallengeGroupDurationOptionDocs;
import site.dogether.docs.challengegroup.enumtype.ChallengeGroupStartAtOptionDocs;
import site.dogether.docs.challengegroup.enumtype.ChallengeGroupStatusDocs;
import site.dogether.docs.dailytodo.enumtype.DailyTodoStatusDocs;
import site.dogether.docs.dailytodocertification.enumtype.DailyTodoCertificationReviewResultDocs;
import site.dogether.docs.s3.enumtype.S3UploadFileTypeDocs;

@RequestMapping("/test")
@RestController
public class CommonDocsController {

    @GetMapping("/enums")
    public EnumDocs findEnums() {
        final Map<String, String> challengeGroupStartAtOption = convertToMap(ChallengeGroupStartAtOptionDocs.getValues());
        final Map<String, String> challengeGroupDurationOption = convertToMap(ChallengeGroupDurationOptionDocs.getValues());
        final Map<String, String> challengeGroupStatus = convertToMap(ChallengeGroupStatusDocs.getValues());
        final Map<String, String> dailyTodoCertificationReviewResult = convertToMap(DailyTodoCertificationReviewResultDocs.getValues());
        final Map<String, String> s3UploadFileType = convertToMap(S3UploadFileTypeDocs.getValues());
        final Map<String, String> dailyTodoStatus = convertToMap(DailyTodoStatusDocs.getValues());
        return EnumDocs.builder()
            .challengeGroupStartAtOption(challengeGroupStartAtOption)
            .challengeGroupDurationOption(challengeGroupDurationOption)
            .challengeGroupStatus(challengeGroupStatus)
            .dailyTodoCertificationReviewResult(dailyTodoCertificationReviewResult)
            .s3UploadFileType(s3UploadFileType)
            .dailyTodoStatus(dailyTodoStatus)
            .build();
    }

    private Map<String, String> convertToMap(final RestDocsEnumType[] restDocsEnumTypes) {
        return Arrays.stream(restDocsEnumTypes)
                     .collect(toMap(RestDocsEnumType::getRequestValue, RestDocsEnumType::getDescription));
    }
}
