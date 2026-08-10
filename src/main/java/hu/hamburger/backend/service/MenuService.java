package hu.hamburger.backend.service;

import hu.hamburger.backend.exception.ResourceNotFoundException;
import hu.hamburger.backend.model.MenuItem;
import hu.hamburger.backend.model.MenuItemIngredient;
import hu.hamburger.backend.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {
    @Autowired
    private MenuRepository menuRepository;

    public List<MenuItem> getAllItems(){
        return menuRepository.findAll();
    }

    public MenuItem getMenuItemById(long id){return menuRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Nem létezik menüelem ezen az azonosítón: " + id));}

    public MenuItem saveMenuItem(MenuItem menuItem){return menuRepository.save(menuItem);}

    public void  deleteMenuItem(long id){menuRepository.deleteById(id);}

    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        MenuItem menuItem = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nem létezik menüelem ezen az azonosítón: " + id));
        menuItem.setName(updatedItem.getName());
        menuItem.setDescription(updatedItem.getDescription());
        menuItem.setPrice(updatedItem.getPrice());
        menuItem.setAvailable(updatedItem.isAvailable());
        return menuRepository.save(menuItem);
    }
}
