package com.example.pdp.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TreeElement {

    @Id
    @GeneratedValue
    private long id;
    private String filename = null;

    @OneToMany
    private List<TreeElement> children = new ArrayList<>();

    private Boolean wasDeleted = false;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<TreeElement> getChildren() {
        return children;
    }

    public void setChildren(List<TreeElement> children) {
        this.children = children;
    }

    public Boolean getWasDeleted() {
        return wasDeleted;
    }

    public void setWasDeleted(Boolean wasDeleted) {
        this.wasDeleted = wasDeleted;
    }
}
