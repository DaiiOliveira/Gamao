package projetogamao;

import projetogamao.Controller.GameController;
import projetogamao.View.ViewSwing;

public class Main {
    public static void main(String[] args) {
        ViewSwing view = new ViewSwing(24); // Tabuleiro com 24 casas
        GameController gc = new GameController(view);
    }
}
