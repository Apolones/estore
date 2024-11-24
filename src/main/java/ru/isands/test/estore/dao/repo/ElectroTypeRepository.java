package ru.isands.test.estore.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isands.test.estore.dao.entity.ElectroType;

public interface ElectroTypeRepository extends JpaRepository<ElectroType, Long> {
}
