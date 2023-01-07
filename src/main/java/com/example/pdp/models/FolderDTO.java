package com.example.pdp.models;

import java.util.ArrayList;
import java.util.List;

public class FolderDTO {
    private String folderName = null;

    private List<String> tags = new ArrayList<>();

    public FolderDTO() { }

    public FolderDTO(TreeElement treeElement) {
        this.folderName = treeElement.getElementName();
        this.tags = treeElement.getTags().stream().map(Tag::getName).toList();
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
