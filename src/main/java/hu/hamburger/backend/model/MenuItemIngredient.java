package hu.hamburger.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "menu_item_ingredient")
public class MenuItemIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;
    private double quantity;

    public Long getId() {
        return id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
