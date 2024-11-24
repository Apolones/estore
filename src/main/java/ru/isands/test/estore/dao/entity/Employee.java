package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee implements Serializable {

    /**
     * Идентификатор сотрудника
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_", unique = true, nullable = false)
    private Long id;

    /**
     * Фамилия сотрудника
     */
    @Column(name = "lastname", nullable = false, length = 100)
    private String lastName;

    /**
     * Имя сотрудника
     */
    @Column(name = "firstname", nullable = false, length = 100)
    private String firstName;

    /**
     * Отчество сотрудника
     */
    @Column(name = "patronymic", nullable = false, length = 100)
    private String patronymic;

    /**
     * Дата рождения сотрудника
     */
    @Column(name = "birthDate", nullable = false)
    private Date birthDate;

    /**
     * Ссылка на должность сотрудника
     */
    @ManyToOne
    @JoinColumn(name = "positionId", nullable = false)
    private PositionType position;

    /**
     * Ссылка на магазин, где работает сотрудник
     */
    @ManyToOne
    @JoinColumn(name = "shopId")
    private Shop shop;

    /**
     * Пол сотрудника (true - мужской, false - женский)
     */
    @Column(name = "gender", nullable = false)
    private boolean gender;

}