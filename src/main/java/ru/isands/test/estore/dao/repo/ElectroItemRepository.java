package ru.isands.test.estore.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isands.test.estore.dao.entity.ElectroItem;

public interface ElectroItemRepository extends JpaRepository<ElectroItem, Long> {

}
