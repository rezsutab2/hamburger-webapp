package hu.hamburger.backend.repository;

import hu.hamburger.backend.model.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
}