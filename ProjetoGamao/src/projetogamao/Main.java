package projetogamao;

import projetogamao.Controller.ControladorJogo;
import projetogamao.View.ViewSwing;

public class Main {
    public static void main(String[] args) {
        ViewSwing view = new ViewSwing(24); // Tabuleiro com 24 casas
        new ControladorJogo(view);
    }
}
