package hu.hamburger.backend.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int price;
    private boolean available;
    @OneToMany(mappedBy = "menuItem")
    private List<MenuItemIngredient> menuItemIngredient;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<MenuItemIngredient> getMenuItemIngredient() {return menuItemIngredient;}

    public void setMenuItemIngredient(List<MenuItemIngredient> menuItemIngredient) {this.menuItemIngredient = menuItemIngredient;}
}