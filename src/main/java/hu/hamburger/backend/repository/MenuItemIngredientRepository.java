package hu.hamburger.backend.repository;

import hu.hamburger.backend.model.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {
    java.util.List<MenuItemIngredient> findByMenuItemId(Long menuItemId);
    void deleteByMenuItemId(Long menuItemId);
}