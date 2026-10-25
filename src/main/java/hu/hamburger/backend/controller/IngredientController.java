package hu.hamburger.backend.controller;

import hu.hamburger.backend.exception.ResourceNotFoundException;
import hu.hamburger.backend.model.Allergen;
import hu.hamburger.backend.model.Ingredient;
import hu.hamburger.backend.repository.AllergenRepository;
import hu.hamburger.backend.repository.IngredientRepository;
import hu.hamburger.backend.repository.MenuItemIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private AllergenRepository allergenRepository;

    @Autowired
    private MenuItemIngredientRepository menuItemIngredientRepository;

    @GetMapping
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    @PostMapping
    public Ingredient createIngredient(@RequestBody Ingredient request) {
        validateName(request.getName());
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName().trim());
        ingredient.setUnit(request.getUnit() == null ? "" : request.getUnit().trim());
        return ingredientRepository.save(ingredient);
    }

    @PutMapping("/{id}")
    public Ingredient updateIngredient(@PathVariable Long id, @RequestBody Ingredient request) {
        validateName(request.getName());
        Ingredient ingredient = findIngredient(id);
        ingredient.setName(request.getName().trim());
        ingredient.setUnit(request.getUnit() == null ? "" : request.getUnit().trim());
        return ingredientRepository.save(ingredient);
    }

    @PutMapping("/{id}/allergens")
    public Ingredient updateAllergens(@PathVariable Long id, @RequestBody List<Long> allergenIds) {
        Ingredient ingredient = findIngredient(id);
        List<Allergen> allergens = allergenRepository.findAllById(allergenIds);
        ingredient.setAllergens(allergens);
        return ingredientRepository.save(ingredient);
    }

    @DeleteMapping("/{id}")
    public void deleteIngredient(@PathVariable Long id) {
        Ingredient ingredient = findIngredient(id);
        if (menuItemIngredientRepository.existsByIngredientId(id)) {
            throw new IllegalStateException("Az összetevő nem törölhető, mert szerepel legalább egy menütételben.");
        }
        ingredientRepository.delete(ingredient);
    }

    private Ingredient findIngredient(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Összetevő nem található: " + id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Az összetevő neve nem lehet üres.");
        }
    }
}