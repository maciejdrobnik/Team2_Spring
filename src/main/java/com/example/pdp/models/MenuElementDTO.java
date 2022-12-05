package com.example.pdp.models;

import java.util.ArrayList;
import java.util.List;

public class MenuElementDTO {

    private Long id = null;
    private String name = null;
    private List<String> tags = new ArrayList<>();

    private List<MenuElementDTO> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String filename) {
        this.name = filename;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {this.id = id;}

    public List<String> getTags() {return tags;}

    public void setTags(List<String> tags) {this.tags = tags;}

    public List<MenuElementDTO> getChildren() {return children;}

    public void setChildren(List<MenuElementDTO> children) {this.children = children;}

    public boolean addChild(MenuElementDTO child) {
        if(!children.contains(child) && child != null){
            children.add(child);
            return true;
        }
        return false;
    }

    public boolean addTag(String tag) {
        if(!tags.contains(tag)){
            tags.add(tag);
            return true;
        }
        return false;
    }
}
