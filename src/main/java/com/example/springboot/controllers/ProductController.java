package com.example.springboot.controllers;

import com.example.springboot.dtos.ProductsRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    ProductRepository repository;

    @PostMapping()
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductsRecordDto productsRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productsRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(productModel));
    }

    @GetMapping()
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        List<ProductModel> productModelList = repository.findAll();
        if (!productModelList.isEmpty()) {
            productModelList.forEach(productModel -> {
                UUID id = productModel.getIdProduct();
                productModel.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            });
        }
        return ResponseEntity.status(HttpStatus.OK).body(productModelList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable("id") UUID uuid) {
        Optional<ProductModel> model = repository.findById(uuid);
        if (model.isEmpty()) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        model.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products List"));
        return ResponseEntity.status(HttpStatus.OK).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable("id") UUID uuid,
                                                @RequestBody @Valid ProductsRecordDto productsRecordDto) {
        Optional<ProductModel> product0 = repository.findById(uuid);
        if (product0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        var productModel = product0.get();
        BeanUtils.copyProperties(productsRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(repository.save(productModel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("id") UUID uuid) {
        Optional<ProductModel> product0 = repository.findById(uuid);
        if (product0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        repository.delete(product0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted sucessfully.");
    }

    @DeleteMapping()
    public ResponseEntity<Object> deleteAllProducts() {
        repository.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("All Products deleted sucessfully.");
    }

}
