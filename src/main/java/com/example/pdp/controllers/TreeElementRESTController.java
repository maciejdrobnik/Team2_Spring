package com.example.pdp.controllers;


import com.example.pdp.models.*;
import com.example.pdp.repositories.TagRepository;
import com.example.pdp.repositories.TreeElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/")
public class TreeElementRESTController {

    private final TreeElementRepository treeElementRepository;
    private final TagRepository tagRepository;

    @Autowired
    public TreeElementRESTController(TreeElementRepository treeElementRepository, TagRepository tagRepository) {
        this.treeElementRepository = treeElementRepository;
        this.tagRepository = tagRepository;
    }


    @GetMapping("/all")
    // GetAll without filtering of deleted children
    public List<TreeElement> findAllTreeElements(String lang) {
        if(lang.isEmpty()){
            return treeElementRepository.findAll().stream().filter(e -> e.getRoot() & !e.getWasDeleted()).toList();
        }

        List<TreeElement> roots = new ArrayList<>(treeElementRepository.findAll().stream().filter(e -> e.getRoot() & !e.getWasDeleted()).toList());
        List<TreeElement> toReturn = new ArrayList<>();
        for(TreeElement element : roots){
            for(Tag tag: element.getTags()){
                if(tag.getName().equals(lang)){
                    toReturn.add(element);
                }
            }
        }

        return toReturn;
    }

    @GetMapping("/menu/{lang}")
    public List<MenuElementDTO> getMenu(@PathVariable("lang") String lang) {
        List<MenuElementDTO> menu = new ArrayList<>();
        for(TreeElement element: findAllTreeElements(lang)){
            if(getMenuElementWithChildren(element.getId()) != null){
                menu.add(getMenuElementWithChildren(element.getId()));
            }
        }
        return menu;
    }

    private MenuElementDTO getMenuElementWithChildren(long id){
        TreeElement element = treeElementRepository.findById(id).orElse(null);
        if (element == null || element.getWasDeleted()) return null;

        MenuElementDTO menuElement = new MenuElementDTO();
        menuElement.setId(element.getId());
        menuElement.setName(element.getElementName());

        for(Tag tag: element.getTags()){
            menuElement.addTag(tag.getName());
        }

        for(TreeElement child: element.getChildren()){
            if(!child.getWasDeleted()) {
                menuElement.addChild(getMenuElementWithChildren(child.getId()));
            }
        }

        return menuElement;
    }

    @GetMapping
    // GetAll with filtering of deleted children
    public List<TreeElement> newFindAllElements() {
        List<TreeElement> listToReturn = new ArrayList<>();
        for (TreeElement element: findAllTreeElements("")) {
            if(getElementWithImmediateChildren(element.getId()) != null){
                listToReturn.add(getElementWithImmediateChildren(element.getId()));
            }
        }
        return listToReturn;
    }

    public TreeElement getElementWithImmediateChildren(long id){
        TreeElement element = treeElementRepository.findById(id).orElse(null);
        if (element == null) return null;

        List<TreeElement> children = new ArrayList<>(element.getChildren());

        for(TreeElement child: element.getChildren()){
            if(element.getWasDeleted()){
                return null;
            }
            if(child.getWasDeleted()){
                children.remove(child);
            }
            else{
                 children.set(children.indexOf(child), getElementWithImmediateChildren(child.getId()));
            }
        }
        element.setChildren(children);

        return element;
    }


    @GetMapping("page/{id}")
    public ResponseEntity<PageDTO> findPage(@PathVariable("id") long id) {
        TreeElement element = treeElementRepository.findById(id).orElse(null);

        if (element == null || element.getWasDeleted())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(new PageDTO(element), HttpStatus.OK);
    }



    @PostMapping("page/parent/{id}")
    public ResponseEntity<Long> addNewPage(@RequestBody PageDTO newPageDTO, @PathVariable("id") long id) {
        TreeElement parent = treeElementRepository.findById(id).orElse(null);

        if (parent == null || parent.getWasDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (parent.isPage()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (newPageDTO.getPageName() == null || newPageDTO.getContent() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String filename = UUID.randomUUID() + ".txt";

        Path filePath = Path.of("src/main/resources/pages/" + filename);

        TreeElement element = new TreeElement();

        element.setRoot(false);
        element.setFileName(filename);
        element.setElementName(newPageDTO.getPageName());

        for (String tag: newPageDTO.getTags()) {
            if(tagRepository.findByName(tag.toLowerCase()) == null){
                Tag newTag = new Tag();
                newTag.setName(tag.toLowerCase());
                List<Tag> tags = element.getTags();
                tags.add(newTag);
                element.setTags(tags);
                tagRepository.save(newTag);
            }
            else{
                Tag newTag = new Tag();
                newTag.setName(tag);
                newTag.setId(tagRepository.findByName(tag.toLowerCase()).getId());
                element.getTags().add(newTag);
                tagRepository.save(newTag);
            }
        }

        try {
            Files.createFile(filePath);
            Files.write(filePath, newPageDTO.getContent().getBytes());
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
        }

        parent.getChildren().add(element);

        treeElementRepository.save(element);

        return new ResponseEntity<>(element.getId(),HttpStatus.OK);
    }


    @PostMapping("/folder/{lang}")
    public ResponseEntity<FolderDTO> addNewRootFolder(@RequestBody FolderDTO newFolderDTO, @PathVariable("lang") String lang) {
        if (newFolderDTO.getFolderName() == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        List<String> languages = Arrays.asList("polish", "english", "french");
        if(!languages.contains(lang)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        TreeElement newElement = new TreeElement();

        if(tagRepository.findByName(lang) == null){
            Tag newTag = new Tag(lang);
            List<Tag> tags = List.of(newTag);
            newElement.setTags(tags);
            tagRepository.save(newTag);
        }
        else{
            Tag newTag = new Tag(lang);
            newTag.setId(tagRepository.findByName(lang).getId());
            newElement.getTags().add(newTag);
            tagRepository.save(newTag);
        }

        newElement.setRoot(true);
        newElement.setElementName(newFolderDTO.getFolderName());
        treeElementRepository.save(newElement);

        return new ResponseEntity<>(new FolderDTO(newElement), HttpStatus.CREATED);
    }

    @PostMapping("/folder/parent/{id}")
    public ResponseEntity<FolderDTO> addNewChildFolder(@RequestBody FolderDTO newFolderDTO, @PathVariable("id") long id) {
        TreeElement parent = treeElementRepository.findById(id).orElse(null);
        TreeElement element = new TreeElement();

        if (parent == null || parent.getWasDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (newFolderDTO.getFolderName() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!parent.isFolder()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        List<TreeElement> children = parent.getChildren();
        element.setRoot(false);

        element.setElementName(newFolderDTO.getFolderName());
        children.add(element);
        parent.setChildren(children);
        treeElementRepository.save(element);
        return new ResponseEntity<>(new FolderDTO(element) ,HttpStatus.CREATED);
    }

    @PatchMapping("page/{id}")
    public ResponseEntity<PageDTO> updatePage(@RequestBody PageDTO updatedDTO, @PathVariable("id") long id) {
        TreeElement elementToUpdate = treeElementRepository.findById(id).orElse(null);

        if (elementToUpdate == null || elementToUpdate.getWasDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (elementToUpdate.isFolder()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (updatedDTO.getPageName() != null) {
            elementToUpdate.setElementName(updatedDTO.getPageName());
        }

        if (updatedDTO.getTags() != null && !updatedDTO.getTags().isEmpty() ) {
            List<String> elementTags = new ArrayList<>();
            for (Tag tag: elementToUpdate.getTags()) {
                elementTags.add(tag.getName());
            }

            elementToUpdate.setTags(new ArrayList<>());

            for (String tag: updatedDTO.getTags()) {
                if(tagRepository.findByName(tag.toLowerCase()) == null){
                    Tag newTag = new Tag();
                    newTag.setName(tag.toLowerCase());
                    tagRepository.save(newTag);
                    List<Tag> tags = elementToUpdate.getTags();
                    tags.add(newTag);
                    elementToUpdate.setTags(tags);
                }
                else if(elementTags.contains(tag)){
                    Tag newTag = new Tag();
                    newTag.setName(tag);
                    newTag.setId(tagRepository.findByName(tag.toLowerCase()).getId());
                    elementToUpdate.getTags().add(newTag);
                    tagRepository.save(newTag);
                }
            }
        }
        else if(updatedDTO.getTags().isEmpty()){
            elementToUpdate.setTags(new ArrayList<>());
        }

        if (updatedDTO.getContent() != null) {
            Path filePath = Path.of("src/main/resources/pages/" + elementToUpdate.getFileName());
            try {
                Files.write(filePath, updatedDTO.getContent().getBytes());
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
            }
        }

        treeElementRepository.save(elementToUpdate);

        return new ResponseEntity<>(new PageDTO(elementToUpdate) ,HttpStatus.OK);
    }

    @PatchMapping("folder/{id}")
    public ResponseEntity<FolderDTO> updateFolder(@RequestBody FolderDTO updatedDTO, @PathVariable("id") long id) {
        TreeElement elementToUpdate = treeElementRepository.findById(id).orElse(null);

        if (elementToUpdate == null || elementToUpdate.getWasDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (elementToUpdate.isPage()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (updatedDTO.getFolderName() != null) {
            elementToUpdate.setElementName(updatedDTO.getFolderName());
        }

        treeElementRepository.save(elementToUpdate);

        return new ResponseEntity<>(new FolderDTO(elementToUpdate), HttpStatus.OK);
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<TreeElement> deleteTreeElement(@PathVariable("id") long id) {
        TreeElement elementToDelete = treeElementRepository.findById(id).orElse(null);

        if (elementToDelete == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        markDeleted(elementToDelete);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private void markDeleted(TreeElement element) {
        element.setWasDeleted(true);
        for (var child : element.getChildren()) {
            markDeleted(child);
        }
        treeElementRepository.save(element);
    }
}
