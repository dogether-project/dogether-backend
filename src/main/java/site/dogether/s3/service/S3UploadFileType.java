package site.dogether.s3.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3UploadFileType {

    IMAGE("png", "image/png"),
    VIDEO("mp4", "video/mp4")
    ;

    private final String extension;
    private final String contentType;
}
