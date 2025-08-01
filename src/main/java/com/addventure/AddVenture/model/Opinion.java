package com.addventure.AddVenture.model;

import jakarta.persistence.*;

@Entity
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que escribe la opinión
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    // Usuario que recibe la opinión
    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    private String fecha;
    private String descripcion;
    private String imagenEstrellas;

    public Opinion() {
    }

    public Opinion(Long id, Usuario autor, Usuario destinatario, String fecha, String descripcion,
            String imagenEstrellas) {
        this.id = id;
        this.autor = autor;
        this.destinatario = destinatario;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.imagenEstrellas = imagenEstrellas;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenEstrellas() {
        return imagenEstrellas;
    }

    public void setImagenEstrellas(String imagenEstrellas) {
        this.imagenEstrellas = imagenEstrellas;
    }
}
