package com.addventure.AddVenture.model;

import jakarta.persistence.*;

@Entity
public class Pasaporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pais;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Pasaporte() {
    }

    public Pasaporte(Long id, String pais, Usuario usuario) {
        this.id = id;
        this.pais = pais;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
