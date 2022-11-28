package com.example.pdp.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TreeElement {

    @Id
    @GeneratedValue
    private long id;
    private String fileName = null;

    private String elementName = null;

    private Boolean wasDeleted = false;

    private Boolean isRoot = false;

    @OneToMany
    private List<TreeElement> children = new ArrayList<>();

    public boolean isFolder() {
        return getFileName() == null;
    }

    public boolean isPage() {
        return getFileName() != null;
    }

    public Boolean getRoot() {
        return isRoot;
    }

    public void setRoot(Boolean root) {
        isRoot = root;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String filename) {
        this.fileName = filename;
    }

    public List<TreeElement> getChildren() {
        return children;
    }

    public void setChildren(List<TreeElement> children) {
        this.children = children;
    }

    public Boolean getWasDeleted() {return wasDeleted;}

    public void setWasDeleted(Boolean wasDeleted) {this.wasDeleted = wasDeleted;}

    public String getElementName() {return elementName;}

    public void setElementName(String elementName) {this.elementName = elementName;}
}
