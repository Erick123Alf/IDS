package com.tiendasara.Tienda.Sara.controllers;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import com.tiendasara.Tienda.Sara.models.Mark;
import com.tiendasara.Tienda.Sara.models.Product;
import com.tiendasara.Tienda.Sara.models.ProductDto;
import com.tiendasara.Tienda.Sara.services.CategoryRepository;
import com.tiendasara.Tienda.Sara.services.MarkRepository;
import com.tiendasara.Tienda.Sara.services.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.tiendasara.Tienda.Sara.models.Category;



@Controller
@RequestMapping("products")
public class ProductController {

    @Autowired
    private ProductRepository repo;
    @Autowired
    private CategoryRepository repo2;
    @Autowired
    private MarkRepository repo3;

    @GetMapping({"","/"})
    public String showProductList(Model model){
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products",products);

        return "products/index";
    }

    public List<Category> getListCategories(){
        List<Category> list = repo2.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return list;
    }

    public List<Mark> getListMarks(){
        List<Mark> list = repo3.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return list;
    }

    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDto productDTO = new ProductDto();
        model.addAttribute("productDTO",productDTO);
        model.addAttribute("categories",getListCategories());
        model.addAttribute("marks",getListMarks());

        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDTO,
            BindingResult result,
            Model model
    ) {
        if (productDTO.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDTO", "imageFile", "The image file is required"));
        }

        model.addAttribute("categories",getListCategories());
        model.addAttribute("marks",getListMarks());

        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        MultipartFile image = productDTO.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try{
            String uploadDir = "src/main/resources/static/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

        Product product = new Product();
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setAmount(productDTO.getAmount());
        product.setCategory(productDTO.getCategory());
        product.setMark(productDTO.getMark());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam("id") Integer id){
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product",product);
            model.addAttribute("categories",getListCategories());
            model.addAttribute("marks",getListMarks());

            ProductDto productDto = new ProductDto();
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());
            productDto.setAmount(product.getAmount());
            productDto.setCategory(product.getCategory());
            productDto.setMark(product.getMark());

            model.addAttribute("productDto", productDto);
        }
        catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }
    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDTO, BindingResult result){
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product",product);
            model.addAttribute("categories", getListCategories());
            model.addAttribute("marks", getListMarks());

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            if (!productDTO.getImageFile().isEmpty()) {
                //Delete old image
                String uploadDir = "src/main/resources/static/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //Save new image file
                MultipartFile image = productDTO.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setAmount(productDTO.getAmount());
            product.setCategory(productDTO.getCategory());
            product.setMark(productDTO.getMark());
            repo.save(product);
        }
        catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = repo.findById(id).get();

            // Primero elimina los registros relacionados en CarritoDetalle
            repo.delete(product);

            // Luego elimina la imagen
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception imagen: " + ex.getMessage());
            }

            // Finalmente elimina el producto
            repo.delete(product);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return "redirect:/products";
    }

}