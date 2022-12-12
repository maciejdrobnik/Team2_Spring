package com.example.pdp.models;

import javax.persistence.*;

@Entity
public class Tag {

    @Id
    @GeneratedValue
    private long id;

    private String name = null;

    public Tag(){

    }

    public Tag(String name){
        this.setName(name);
    }
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
}
