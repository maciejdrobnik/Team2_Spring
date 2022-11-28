package com.example.pdp.controllers;


import com.example.pdp.models.FolderDTO;
import com.example.pdp.models.PageDTO;
import com.example.pdp.repositories.TreeElementRepository;
import com.example.pdp.models.TreeElement;
import com.example.pdp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

@RestController
@RequestMapping("/")
public class TreeElementRESTController {

    private final TreeElementRepository treeElementRepository;

    @Autowired
    public TreeElementRESTController(TreeElementRepository treeElementRepository) {
        this.treeElementRepository = treeElementRepository;
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


    @GetMapping("page/{id}")
    public ResponseEntity<PageDTO> findPage(@PathVariable("id") long id) {
        TreeElement element = treeElementRepository.findById(id).orElse(null);

        if (element == null || element.getWasDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PageDTO page = new PageDTO();
        page.setPageName(element.getElementName());
        Path filePath = Path.of("src/main/resources/pages/" + element.getFileName());

        try {
            page.setContent(Files.readString(filePath));
        } catch (IOException e) {
            System.out.println("IOException has occured, something's wrong");
            return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
        }

        return new ResponseEntity<>(page, HttpStatus.OK);
    }



    @PostMapping("page/parent/{id}")
    public ResponseEntity<TreeElement> addNewPage(@RequestBody PageDTO newPageDTO, @PathVariable("id") long id) {
        TreeElement parent = treeElementRepository.findById(id).orElse(null);

        // generating a unique fileName for the new page
        String filename = UUID.randomUUID().toString();
        Path filePath = Path.of("src/main/resources/pages/" + filename + ".txt");

        if (parent == null || parent.getWasDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (parent.isPage()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
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

        try {
            Files.createFile(filePath);
            Files.write(filePath, newPageDTO.getContent().getBytes());
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
        }

        parent.getChildren().add(element);

        treeElementRepository.save(element);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/folder")
    public ResponseEntity<PageDTO> addNewRootFolder(@RequestBody FolderDTO newFolderDTO) {
        if (newFolderDTO.getFolderName() == null) {
            System.out.println("The given folder to create doesn't have a name, no folder created.");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        TreeElement newElement = new TreeElement();

        newElement.setRoot(true);
        newElement.setElementName(newFolderDTO.getFolderName());
        treeElementRepository.save(newElement);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/folder/parent/{id}")
    public ResponseEntity<PageDTO> addNewChildFolder(@RequestBody FolderDTO newFolderDTO, @PathVariable("id") long id) {
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
        return new ResponseEntity<>(HttpStatus.CREATED);
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

        if (updatedDTO.getContent() != null) {
            Path filePath = Path.of("src/main/resources/pages/" + elementToUpdate.getFileName());
            try {
                Files.write(filePath, updatedDTO.getContent().getBytes());
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
            }
        }

        treeElementRepository.save(elementToUpdate);

        return new ResponseEntity<>(HttpStatus.OK);
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

        return new ResponseEntity<>(HttpStatus.OK);
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
