package site.dogether.docs.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnumDocs {

    Map<String, String> challengeGroupStartAtOption;
    Map<String, String> challengeGroupDurationOption;
    Map<String, String> challengeGroupStatus;
    Map<String, String> dailyTodoCertificationReviewResult;
    Map<String, String> s3UploadFileType;
    Map<String, String> dailyTodoStatus;
}
