package projetogamao.Controller;

import projetogamao.Model.Tabuleiro;
import projetogamao.View.ViewSwing;

public class TurnoVitoriaController {
    private final Tabuleiro tabuleiro;
    private final ViewSwing view;
    private final Runnable reiniciar;

    private int jogadorAtual = 1;

    public TurnoVitoriaController(Tabuleiro tabuleiro, ViewSwing view, Runnable reiniciar) {
        this.tabuleiro = tabuleiro;
        this.view = view;
        this.reiniciar = reiniciar;
    }

    public int getJogadorAtual() { return jogadorAtual; }
    public void proximoTurno() {
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
        view.mostrarMensagem("Vez do Jogador " + jogadorAtual);
    }

    public void checarVencedor() {
        int restantesJ1 = tabuleiro.contarPecasNoTabuleiro(1) + tabuleiro.getBar(1);
        int restantesJ2 = tabuleiro.contarPecasNoTabuleiro(2) + tabuleiro.getBar(2);
        if (restantesJ1 == 0) {
            view.mostrarTelaVencedor("Jogador 1",
                    tabuleiro.getPontuacao(1),
                    tabuleiro.getPontuacao(2),
                    reiniciar);
        } else if (restantesJ2 == 0) {
            view.mostrarTelaVencedor("Jogador 2",
                    tabuleiro.getPontuacao(1),
                    tabuleiro.getPontuacao(2),
                    reiniciar);
        }
    }
}
