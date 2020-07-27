package com.example.tv;

public class Chaine {

    int id_Chaine;
    String nom_Chaine;
    int   pos_Chaine;

    public Chaine(int id_Chaine, String nom_Chaine, int pos_Chaine) {
        this.id_Chaine = id_Chaine;
        this.nom_Chaine = nom_Chaine;
        this.pos_Chaine = pos_Chaine;
    }

    public int getId_Chaine() {
        return id_Chaine;
    }

    public void setId_Chaine(int id_Chaine) {
        this.id_Chaine = id_Chaine;
    }

    public String getNom_Chaine() {
        return nom_Chaine;
    }

    public void setNom_Chaine(String nom_Chaine) {
        this.nom_Chaine = nom_Chaine;
    }

    public int getPos_Chaine() {
        return pos_Chaine;
    }

    public void setPos_Chaine(int pos_Chaine) {
        this.pos_Chaine = pos_Chaine;
    }
}
