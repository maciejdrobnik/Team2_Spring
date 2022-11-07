package com.example.pdp.controllers;


import com.example.pdp.repositories.TreeElementRepository;
import com.example.pdp.repositories.UserRepository;
import com.example.pdp.models.TreeElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tree")
public class TreeElementRESTController {

    private TreeElementRepository treeElementRepository;
    //private UserRepository userRepository;

    @Autowired
    public TreeElementRESTController(TreeElementRepository treeElementRepository,
                                     UserRepository userRepository) {
        this.treeElementRepository = treeElementRepository;
        //this.userRepository = userRepository;
    }

    @GetMapping
    public List<TreeElement> findAllTreeElements() {
        return treeElementRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreeElement> findTreeElement(@PathVariable("id") long id) {
        TreeElement element = treeElementRepository.findById(id);
        if(element == null){
            System.out.println("Tree element not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(element, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreeElement> addTreeElement(@RequestBody TreeElement newElement, @PathVariable("id") long id) {
        TreeElement foundElement = treeElementRepository.findById(id);
        if(foundElement == null){
            treeElementRepository.save(newElement);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        else if(!foundElement.getWasDeleted()){
            List<TreeElement> children = foundElement.getChildren();
            children.add(newElement);
            foundElement.setChildren(children);

            treeElementRepository.save(newElement);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TreeElement> updateTreeElement(@RequestBody TreeElement updatedElement, @PathVariable("id") long id) {
        TreeElement elementToUpdate = treeElementRepository.findById(id);

        if(elementToUpdate == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else{
            updatedElement.setId(elementToUpdate.getId());
            treeElementRepository.save(updatedElement);

            return new ResponseEntity<>(HttpStatus.OK);
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<TreeElement> deleteTreeElement(@PathVariable("id") long id){
        TreeElement elementToDelete = treeElementRepository.findById(id);

        if(elementToDelete == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else{
            elementToDelete.setWasDeleted(true);

            removeChildren(elementToDelete);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private void removeChildren(TreeElement parent){
        parent.setWasDeleted(true);
        List<TreeElement> children = parent.getChildren();
        for(TreeElement child: children){
            removeChildren(child);
        }
        treeElementRepository.save(parent);
    }
}
