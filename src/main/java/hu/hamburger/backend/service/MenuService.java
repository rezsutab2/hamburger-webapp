package hu.hamburger.backend.service;

import hu.hamburger.backend.exception.ResourceNotFoundException;
import hu.hamburger.backend.model.Ingredient;
import hu.hamburger.backend.model.MenuItem;
import hu.hamburger.backend.model.MenuItemIngredient;
import hu.hamburger.backend.repository.IngredientRepository;
import hu.hamburger.backend.repository.MenuItemIngredientRepository;
import hu.hamburger.backend.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {
    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuItemIngredientRepository menuItemIngredientRepository;

    @Transactional(readOnly = true)
    public List<MenuItem> getAllItems(){
        return menuRepository.findAll();
    }

    public MenuItem getMenuItemById(long id){return menuRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Nem létezik menüelem ezen az azonosítón: " + id));}

    @Transactional
    public void deleteMenuItem(long id) {
        menuItemIngredientRepository.deleteByMenuItemId(id);
        menuRepository.deleteById(id);
    }

    @Transactional
    public MenuItem saveMenuItem(MenuItem menuItem) {
        List<MenuItemIngredient> incoming = menuItem.getMenuItemIngredient();
        MenuItem saved = menuRepository.save(menuItem);
        saveIngredients(saved, incoming);
        saved.setMenuItemIngredient(menuItemIngredientRepository.findByMenuItemId(saved.getId()));
        return saved;
    }

    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        MenuItem menuItem = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nem létezik menüelem ezen az azonosítón: " + id));
        menuItem.setName(updatedItem.getName());
        menuItem.setDescription(updatedItem.getDescription());
        menuItem.setPrice(updatedItem.getPrice());
        menuItem.setAvailable(updatedItem.isAvailable());
        MenuItem saved = menuRepository.save(menuItem);

        menuItemIngredientRepository.deleteByMenuItemId(id);
        saveIngredients(saved, updatedItem.getMenuItemIngredient());
        saved.setMenuItemIngredient(menuItemIngredientRepository.findByMenuItemId(saved.getId()));
        return saved;
    }

    private void saveIngredients(MenuItem menuItem, List<MenuItemIngredient> incoming) {
        if (incoming == null) return;
        for (MenuItemIngredient item : incoming) {
            if (item.getIngredient() == null || item.getIngredient().getId() == null) continue;

            Ingredient ingredient = ingredientRepository.findById(item.getIngredient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Összetevő nem található: " + item.getIngredient().getId()));

            MenuItemIngredient mii = new MenuItemIngredient();
            mii.setMenuItem(menuItem);
            mii.setIngredient(ingredient);
            mii.setQuantity(item.getQuantity());
            menuItemIngredientRepository.save(mii);
        }
    }
}
