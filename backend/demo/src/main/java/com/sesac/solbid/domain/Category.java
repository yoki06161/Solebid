package com.sesac.solbid.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="category")
public class Category  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(unique = true, nullable = true)
    private String name;

    @Column(unique = true, nullable = true)
    private String description;

    @Column(unique = true, nullable = true)
    private Long parentId;

    @Column(unique = true, nullable = false)
    private Boolean isActive;

    private LocalDateTime createdAt;

}
