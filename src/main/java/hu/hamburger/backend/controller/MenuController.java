package hu.hamburger.backend.controller;

import hu.hamburger.backend.model.MenuItem;
import hu.hamburger.backend.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public List<MenuItem> getAllMenuItems() {
        return menuService.getAllItems();
    }

    @GetMapping("/{id}")
    public MenuItem getMenuItemById(@PathVariable long id) {
        return menuService.getMenuItemById(id);
    }

    @PostMapping
    public MenuItem saveMenuItem(@RequestBody MenuItem menuItem) {
        return menuService.saveMenuItem(menuItem);
    }

    @DeleteMapping("/{id}")
    public void deleteMenuItem(@PathVariable long id) {
        menuService.deleteMenuItem(id);
    }

    @PutMapping("/{id}")
    public MenuItem updateMenuItem(@PathVariable long id,@RequestBody MenuItem updatedMenuItem) {
        return menuService.updateMenuItem(id, updatedMenuItem);
    }
}
