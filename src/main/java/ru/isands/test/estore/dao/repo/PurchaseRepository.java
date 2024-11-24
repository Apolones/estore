package ru.isands.test.estore.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isands.test.estore.dao.entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

}
