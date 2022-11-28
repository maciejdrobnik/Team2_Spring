package com.example.pdp.controllers;


import com.example.pdp.models.FolderDTO;
import com.example.pdp.models.PageDTO;
import com.example.pdp.repositories.TreeElementRepository;
import com.example.pdp.models.TreeElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

@RestController
@RequestMapping("/tree")
public class TreeElementRESTController {

    private TreeElementRepository treeElementRepository;
    //private UserRepository userRepository;

    @Autowired
    public TreeElementRESTController(TreeElementRepository treeElementRepository) {
        this.treeElementRepository = treeElementRepository;
        //this.userRepository = userRepository;
    }


    @GetMapping
    // GetAll without filtering of deleted children
    public List<TreeElement> findAllTreeElements() {
        return treeElementRepository.findAll().stream().filter(e -> e.getRoot() & !e.getWasDeleted()).toList();
    }

    @GetMapping("/getAll")
    // GetAll with filtering of deleted children
    public List<TreeElement> newFindAllElements() {
        List<TreeElement> listToReturn = new ArrayList<>();
        for (TreeElement element: findAllTreeElements()) {
            if(getElementWithImmediateChildren(element.getId()) != null){
                listToReturn.add(getElementWithImmediateChildren(element.getId()));
            }
        }
        return listToReturn;
    }

    public TreeElement getElementWithImmediateChildren(long id){
        TreeElement element = treeElementRepository.findById(id);
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


    @GetMapping("/{id}")
    public ResponseEntity<PageDTO> findTreeElement(@PathVariable("id") long id) {
        TreeElement element = treeElementRepository.findById(id);
        if(element == null || element.getWasDeleted() || element.getFileName() == null){
            System.out.println("Tree element was not found, was deleted, or is a folder!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PageDTO page = new PageDTO();
        page.setPageName(element.getElementName());
        Path filePath = Path.of("src/main/resources/pages/" + element.getFileName() + ".txt");

        try{
            page.setContent(Files.readString(filePath));
        }
        catch(IOException e){
            System.out.println("IOException has occured, something's wrong");
            return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
        }

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    // Provide PageDTO with "pageName" and "content" fields to create a new tree element (a page)
    // attaches the new page as a child to an existing folder with the given id if it exists
    // otherwise, adds the new page as a "root" with no children or parents.
    // Creates a text file in the resources/pages directory which stores the content
    // which is then read while using the GET request.
    @PutMapping("page/{id}")
    public ResponseEntity<TreeElement> addNewPage(@RequestBody PageDTO newPageDTO, @PathVariable("id") long id) {
        TreeElement foundElement = treeElementRepository.findById(id);

        // generating a unique fileName for the new page
        String filename = UUID.randomUUID().toString();
        Path filePath = Path.of("src/main/resources/pages/" + filename + ".txt");

        // this element represents the new page
        TreeElement newElement = new TreeElement();

        // if no element with given ID was found, the new element will be a root
        if(foundElement == null){
            // if element is a page it has to have content and page name
            if(newPageDTO.getPageName() != null & newPageDTO.getContent() != null){
                newElement.setFileName(filename);
                newElement.setElementName(newPageDTO.getPageName());
                newElement.setRoot(true);

                // create text file and write content to it
                try {
                    if (Files.exists(filePath)) {
                        System.out.println("File already exists");
                    } else {
                        Files.createFile(filePath);
                        Files.write(filePath, newPageDTO.getContent().getBytes());
                        System.out.println("File created");
                    }
                }
                catch (IOException e){
                    System.out.println("An IOException has occured, something's wrong");
                    return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
                }
            }
            // if either the content or the page name is missing, the element is not created
            else{
                System.out.println("The given element lacks content or page name - no page created");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            treeElementRepository.save(newElement);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        //else, if the found element is a non-deleted folder, it will be the new page's parent
        else if(!foundElement.getWasDeleted() & foundElement.getFileName() == null){
            List<TreeElement> children = foundElement.getChildren();
            newElement.setRoot(false);

            //if the child to be added is a page
            if(newPageDTO.getPageName() != null & newPageDTO.getContent() != null){
                newElement.setFileName(filename);
                newElement.setElementName(newPageDTO.getPageName());

                try {
                    if (Files.exists(filePath)) {
                        System.out.println("File already exists");
                    } else {
                        Files.createFile(filePath);
                        Files.write(filePath, newPageDTO.getContent().getBytes());
                        System.out.println("File created");
                    }
                }
                catch (IOException e){
                    System.out.println("An IOException has occured, something's wrong");
                    return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
                }
            }
            // otherwise, the found element cannot be a parent, so no page is created
            else{
                System.out.println("Element matching given ID (parent for new page) isn't a page or was deleted - no page created");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            children.add(newElement);
            foundElement.setChildren(children);

            treeElementRepository.save(newElement);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            System.out.println("Element with the given ID was found, was deleted or is a page (cannot have children)");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    // this function takes a FolderDTO (folder name) and ID (from URL) and adds a new folder to the database
    // it can never add new files with page content, only folders
    // the created folder has to be given a name, otherwise nothing is created
    // if the given ID already exists and is a non-deleted folder, it will be the new element's parent
    @PutMapping("folder/{id}")
    public ResponseEntity<PageDTO> addNewFolder(@RequestBody FolderDTO newFolderDTO, @PathVariable("id") long id){
        TreeElement foundElement = treeElementRepository.findById(id);
        TreeElement newElement = new TreeElement();

        // no element with given id found -> adding a root folder
        if(foundElement == null){
            //the new element is a root
            newElement.setRoot(true);

            // if the new folder was given a name it is assigned and the element is saved
            if(newFolderDTO.getFolderName() != null){
                // assigning the name and saving
                newElement.setElementName(newFolderDTO.getFolderName());
                treeElementRepository.save(newElement);
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            // else, the element is not created - a folder has to have a name
            else{
                System.out.println("The given folder to create doesn't have a name, no folder created.");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        // if an element with a given ID is found, wasn't deleted, and is a folder, it will be the new element's parent
        else if(!foundElement.getWasDeleted() & foundElement.getFileName() == null){
            // getting the parent's children and setting the new element as not root
            List<TreeElement> children = foundElement.getChildren();
            newElement.setRoot(false);

            // if the given new folder has a name, it is assigned, the element is added to parent's children, and saved
            if(newFolderDTO.getFolderName() != null){
                newElement.setElementName(newFolderDTO.getFolderName());
                children.add(newElement);
                foundElement.setChildren(children);
                treeElementRepository.save(newElement);
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            // else, the folder is not created - a folder has to have a name
            else{
                System.out.println("The given folder to create doesn't have a name, no folder created");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        else{
            System.out.println("The element with givn ID cannot be a parent (was deleted or is a page) - no folder created");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    // Provide a TreeElementDTO and id (in url) to update a tree element.
    // This updates the page name of the tree element as well as the content
    // of the associated text file.
    @PatchMapping("/{id}")
    public ResponseEntity<PageDTO> updateTreeElement(@RequestBody PageDTO updatedDTO, @PathVariable("id") long id) {
        TreeElement elementToUpdate = treeElementRepository.findById(id);

        if(elementToUpdate == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else if(!elementToUpdate.getWasDeleted()){
            if(updatedDTO.getPageName() != null){
                elementToUpdate.setElementName(updatedDTO.getPageName());
                treeElementRepository.save(elementToUpdate);
            }
            if(updatedDTO.getContent() != null & elementToUpdate.getFileName() != null){
                Path filePath = Path.of("src/main/resources/pages/" + elementToUpdate.getFileName() + ".txt");
                try{
                    Files.write(filePath, updatedDTO.getContent().getBytes());
                }
                catch(IOException e){
                    System.out.println("An IOException has occured, something's wrong");
                    return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            System.out.println("Element with the given ID was found, was deleted or is a folder (cannot be modified)");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
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
