package hu.hamburger.backend.repository;

import hu.hamburger.backend.model.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {
    List<MenuItemIngredient> findByMenuItemId(Long menuItemId);
    void deleteByMenuItemId(Long menuItemId);
    boolean existsByIngredientId(Long ingredientId);
}