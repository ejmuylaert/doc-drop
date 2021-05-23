package org.ej.docdrop.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

public class RemarkableContent {

    private final ObjectNode extraMetadata;
    private final RemarkableFileType fileType;
    private final String fontName;
    private final int lastOpenedPage;
    private final int lineHeight;
    private final int margins;
    private final int pageCount;
    private final int textScale;
    private final RemarkableTransform transform;

    private final static ObjectMapper mapper = new ObjectMapper();

    @JsonCreator
    public RemarkableContent(@JsonProperty("extraMetadata") ObjectNode extraMetadata,
                             @JsonProperty("fileType") RemarkableFileType fileType,
                             @JsonProperty("fontName") String fontName,
                             @JsonProperty("lastOpenedPage") int lastOpenedPage,
                             @JsonProperty("lineHeight") int lineHeight,
                             @JsonProperty("margins") int margins,
                             @JsonProperty("pageCount") int pageCount,
                             @JsonProperty("textScale") int textScale,
                             @JsonProperty("transform") RemarkableTransform transform) {

        this.extraMetadata = extraMetadata;
        this.fileType = fileType;
        this.fontName = fontName;
        this.lastOpenedPage = lastOpenedPage;
        this.lineHeight = lineHeight;
        this.margins = margins;
        this.pageCount = pageCount;
        this.textScale = textScale;
        this.transform = transform;
    }

    public static RemarkableContent defaultContent() {
        return new RemarkableContent(
                mapper.createObjectNode(),
                RemarkableFileType.PDF,
                "",
                0,
                -1,
                100,
                1,
                1,
                new RemarkableTransform(1, 1, 1, 1, 1, 1, 1, 1, 1));
    }

    public ObjectNode getExtraMetadata() {
        return extraMetadata;
    }

    public RemarkableFileType getFileType() {
        return fileType;
    }

    public String getFontName() {
        return fontName;
    }

    public int getLastOpenedPage() {
        return lastOpenedPage;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getMargins() {
        return margins;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getTextScale() {
        return textScale;
    }

    public RemarkableTransform getTransform() {
        return transform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemarkableContent content = (RemarkableContent) o;
        return lastOpenedPage == content.lastOpenedPage && lineHeight == content.lineHeight && margins == content.margins && pageCount == content.pageCount && textScale == content.textScale && Objects.equals(extraMetadata, content.extraMetadata) && fileType == content.fileType && Objects.equals(fontName, content.fontName) && Objects.equals(transform, content.transform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extraMetadata, fileType, fontName, lastOpenedPage, lineHeight, margins, pageCount,
                textScale, transform);
    }

    @Override
    public String toString() {
        return "RemarkableContent{" +
                "extraMetadata=" + extraMetadata +
                ", fileType=" + fileType +
                ", fontName='" + fontName + '\'' +
                ", lastOpenedPage=" + lastOpenedPage +
                ", lineHeight=" + lineHeight +
                ", margins=" + margins +
                ", pageCount=" + pageCount +
                ", textScale=" + textScale +
                ", transform=" + transform +
                '}';
    }
}
