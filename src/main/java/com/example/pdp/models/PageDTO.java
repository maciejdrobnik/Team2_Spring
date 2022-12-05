package com.example.pdp.models;

import java.util.List;
import java.util.ArrayList;

public class PageDTO {
    private String pageName = null;
    private String content = null;

    private List<String> tags = new ArrayList<>();

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String filename) {
        this.pageName = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {this.content = content;}

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {this.tags = tags;}
}
