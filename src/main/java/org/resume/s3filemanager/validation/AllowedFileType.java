package org.resume.s3filemanager.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum AllowedFileType {

    // Документы
    PDF("application/pdf", "pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
    ODT("application/vnd.oasis.opendocument.text", "odt"),
    RTF("application/rtf", "rtf"),

    // Изображения
    JPEG("image/jpeg", "jpg"),
    JPEG_ALT("image/jpeg", "jpeg"),
    PNG("image/png", "png"),
    GIF("image/gif", "gif"),
    WEBP("image/webp", "webp"),
    SVG("image/svg+xml", "svg"),
    BMP("image/bmp", "bmp"),
    ICO("image/x-icon", "ico"),

    // Видео
    MP4("video/mp4", "mp4"),
    WEBM("video/webm", "webm"),

    // Аудио
    MP3("audio/mpeg", "mp3"),
    WAV("audio/wav", "wav"),
    OGG("audio/ogg", "ogg"),

    // Архивы
    ZIP("application/zip", "zip"),
    RAR("application/x-rar-compressed", "rar"),
    SEVEN_ZIP("application/x-7z-compressed", "7z"),

    // Текст/Код
    TXT("text/plain", "txt"),
    CSV("text/csv", "csv"),
    JSON("application/json", "json"),
    XML("application/xml", "xml"),
    MARKDOWN("text/markdown", "md");

    private final String contentType;
    private final String extension;

    public static boolean isAllowed(String extension, String contentType) {
        return Arrays.stream(values())
                .anyMatch(type ->
                        type.extension.equalsIgnoreCase(extension) &&
                                type.contentType.equalsIgnoreCase(contentType)
                );
    }

    public static Set<String> getAllowedContentTypes() {
        return Arrays.stream(values())
                .map(AllowedFileType::getContentType)
                .collect(Collectors.toSet());
    }
}
