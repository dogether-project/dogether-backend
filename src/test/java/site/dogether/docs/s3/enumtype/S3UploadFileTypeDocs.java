package site.dogether.docs.s3.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.docs.util.RestDocsEnumType;
import site.dogether.s3.service.S3UploadFileType;

@Getter
@RequiredArgsConstructor
public enum S3UploadFileTypeDocs implements RestDocsEnumType {

    IMAGE("이미지", "IMAGE"),
    VIDEO("동영상", "VIDEO")
    ;

    private static final int enumValueCount = S3UploadFileType.values().length;

    private final String description;
    private final String requestValue;

    public static RestDocsEnumType[] getValues() {
        final S3UploadFileTypeDocs[] values = S3UploadFileTypeDocs.values();
        RestDocsEnumType.checkDocsValueCountIsEqualToEnumValueCount(enumValueCount, values.length);
        return values;
    }
}
