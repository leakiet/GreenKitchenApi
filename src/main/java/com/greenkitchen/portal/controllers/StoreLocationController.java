package com.greenkitchen.portal.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.greenkitchen.portal.dtos.StoreLocationDTO;
import com.greenkitchen.portal.entities.StoreLocation;
import com.greenkitchen.portal.services.StoreLocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/stores")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true")
public class StoreLocationController {

    @Autowired
    private StoreLocationService service;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<StoreLocationDTO> create(@Valid @RequestBody StoreLocationDTO request) {
        System.out.println("Received request: " + request);
        StoreLocation entity = modelMapper.map(request, StoreLocation.class);
        System.out.println("Mapped entity: " + entity);
        StoreLocation saved = service.create(entity);
        System.out.println("Saved entity: " + saved);
        StoreLocationDTO dto = modelMapper.map(saved, StoreLocationDTO.class);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
        public ResponseEntity<StoreLocationDTO> update(@PathVariable("id") Long id, @Valid @RequestBody StoreLocationDTO request) {
        System.out.println("Update request for ID: " + id);
        System.out.println("Update request data: " + request);
        StoreLocation updated = service.update(id, modelMapper.map(request, StoreLocation.class));
        System.out.println("Updated entity: " + updated);
        StoreLocationDTO dto = modelMapper.map(updated, StoreLocationDTO.class);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<StoreLocationDTO>> findAll() {
        List<StoreLocationDTO> result = service.findAll().stream()
            .map(s -> modelMapper.map(s, StoreLocationDTO.class))
            .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreLocationDTO> findById(@PathVariable("id") Long id) {
        StoreLocation found = service.findById(id);
        if (found == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(modelMapper.map(found, StoreLocationDTO.class));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errors.append(error.getDefaultMessage()).append("; ");
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.toString());
    }
}


