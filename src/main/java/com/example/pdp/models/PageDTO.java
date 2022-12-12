package com.example.pdp.models;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public class PageDTO {
    private String pageName = null;
    private String content = null;

    private List<String> tags = new ArrayList<>();

    public PageDTO() {
    }

    public PageDTO(TreeElement treeElement) {
        this.pageName = treeElement.getElementName();
        this.tags = treeElement.getTags().stream().map(Tag::getName).toList();

        Path filePath = Path.of("src/main/resources/pages/" + treeElement.getFileName());
        try {
            this.content = Files.readString(filePath);
        } catch (IOException e) {
            this.content = "";
        }
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String filename) {
        this.pageName = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
