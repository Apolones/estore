package ru.isands.test.estore.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.isands.test.estore.dao.entity.Employee;
import ru.isands.test.estore.dto.BestEmployeeDTO;

import java.util.Date;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT new ru.isands.test.estore.dto.BestEmployeeDTO(" +
            "e.id, e.firstName, e.lastName, e.position.name, " +
            "COUNT(p.electroItem.id) AS itemsSold, SUM(p.electroItem.price) AS totalSales) " +
            "FROM Employee e " +
            "JOIN Purchase p ON p.employee.id = e.id " +
            "WHERE p.purchaseDate >= :startDate AND e.position.id = :positionId " +
            "GROUP BY e.id, e.firstName, e.lastName, e.position.name " +
            "ORDER BY totalSales DESC")
    List<BestEmployeeDTO> findBestEmployeesByTotalSales(@Param("startDate") Date startDate, @Param("positionId") Long positionId);

    @Query("SELECT new ru.isands.test.estore.dto.BestEmployeeDTO(" +
            "e.id, e.firstName, e.lastName, e.position.name, " +
            "COUNT(p.electroItem.id) AS itemsSold, SUM(p.electroItem.price) AS totalSales) " +
            "FROM Employee e " +
            "JOIN Purchase p ON p.employee.id = e.id " +
            "WHERE p.purchaseDate >= :startDate AND e.position.id = :positionId " +
            "GROUP BY e.id, e.firstName, e.lastName, e.position.name " +
            "ORDER BY itemsSold DESC")
    List<BestEmployeeDTO> findBestEmployeesByItemSold(@Param("startDate") Date startDate, @Param("positionId") Long positionId);

    @Query("SELECT new ru.isands.test.estore.dto.BestEmployeeDTO(" +
            "e.id, e.firstName, e.lastName, e.position.name, " +
            "COUNT(p.electroItem.id) AS itemsSold, " +
            "SUM(p.electroItem.price) AS totalSales) " +
            "FROM Employee e " +
            "JOIN Purchase p ON p.employee.id = e.id " +
            "WHERE e.position.name = :empoyeePosition AND p.electroItem.eType.name = :electoItem " +
            "GROUP BY e.id, e.firstName, e.lastName, e.position.name " +
            "ORDER BY itemsSold DESC")
    List<BestEmployeeDTO> findBestJuniorConsultantBySmartWatches(
            @Param("empoyeePosition") String empoyeePosition,
            @Param("electoItem") String electoItem);


}