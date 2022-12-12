package com.example.pdp.models;

public class FolderDTO {
    private String folderName = null;

    public FolderDTO() { }

    public FolderDTO(TreeElement treeElement) {
        this.folderName = treeElement.getElementName();
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
