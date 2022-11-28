package com.example.pdp.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String password;

    @ManyToMany
    private List<TreeElement> permittedTreeElements = new ArrayList<>();

    private Boolean wasDeleted = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<TreeElement> getPermittedTreeElements() {
        return permittedTreeElements;
    }

    public void setPermittedTreeElements(List<TreeElement> permittedTreeElements) {
        this.permittedTreeElements = permittedTreeElements;
    }

    public Boolean getWasDeleted() {
        return wasDeleted;
    }

    public void setWasDeleted(Boolean wasDeleted) {
        this.wasDeleted = wasDeleted;
    }
}
