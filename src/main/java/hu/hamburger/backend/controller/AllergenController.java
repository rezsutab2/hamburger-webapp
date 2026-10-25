package hu.hamburger.backend.controller;

import hu.hamburger.backend.model.Allergen;
import hu.hamburger.backend.repository.AllergenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/allergens")
public class AllergenController {

    @Autowired
    private AllergenRepository allergenRepository;

    @GetMapping
    public List<Allergen> getAllAllergens() {
        return allergenRepository.findAll();
    }
}