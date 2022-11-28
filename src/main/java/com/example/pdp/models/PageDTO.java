package com.example.pdp.models;

public class PageDTO {
    private String pageName = null;
    private String content = null;

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
}
