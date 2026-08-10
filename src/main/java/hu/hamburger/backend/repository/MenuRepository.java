package hu.hamburger.backend.repository;
import hu.hamburger.backend.model.MenuItem;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MenuRepository extends JpaRepository<MenuItem, Long> {
}
