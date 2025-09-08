package com.petsaudedigital.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "contato_principal")
    private String contatoPrincipal;
    @Column(name = "contatos_json")
    private String contatosJson;
    private String formacao;
    private String vinculo;
    @Column(nullable = false)
    private Integer ativo = 1;
}

