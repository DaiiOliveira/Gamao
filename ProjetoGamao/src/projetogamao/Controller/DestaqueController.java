package projetogamao.Controller;

import java.awt.Color;
import projetogamao.Model.Movimento;
import projetogamao.Model.Tabuleiro;
import projetogamao.View.ViewSwing;

public class DestaqueController {
    private final Tabuleiro tabuleiro;
    private final ViewSwing view;
    private final DiceController dados;
    private final MovementController mov;

    public DestaqueController(Tabuleiro tabuleiro, ViewSwing view, DiceController dados, MovementController mov) {
        this.tabuleiro = tabuleiro;
        this.view = view;
        this.dados = dados;
        this.mov = mov;
    }

    public void limpar() {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int n = tabuleiro.getNumPecas(i);
            int d = tabuleiro.getDonoCasa(i);
            view.atualizarCasa(i, (d != 0) ? n : 0, d == 1);
        }
    }

    public void destacarEntradasPossiveis(int jogador) {
        limpar();
        for (int p : dados.individuaisDisponiveis()) {
            int dest = tabuleiro.entradaDestinoPorDado(jogador, p);
            if (dest == -1) continue;
            if (!tabuleiro.podeEntrarDaBarraCom(jogador, p)) continue;
            pintar(dest, jogador);
        }
    }

    public void destacarDaOrigem(int jogador, int origemFis) {
        limpar();
        view.destacarCasa(origemFis, true);

        // individuais
        int origemLog = tabuleiro.posicaoLogica(jogador, origemFis);
        for (int p : dados.individuaisDisponiveis()) {
            int destLog = origemLog + p;
            int destFis = mov.encontrarCasaPorPosicaoLogica(jogador, destLog);
            if (destFis == -1) continue;

            Movimento mv = new Movimento(origemFis, destFis);
            if (tabuleiro.movimentoValido(mv, jogador, p)) pintar(destFis, jogador);
        }

        // longa (não-double) – destacar destino final se ambos passos forem válidos (qualquer ordem)
        if (!dados.isDouble() && dados.podeSoma()) {
            int dA = dados.snapshotPassos().get(0);
            int dB = dados.snapshotPassos().get(1);

            // ordem dA -> dB
            int interLogA = origemLog + dA;
            int interFisA = mov.encontrarCasaPorPosicaoLogica(jogador, interLogA);
            if (interFisA != -1) {
                Movimento mv1 = new Movimento(origemFis, interFisA);
                if (tabuleiro.movimentoValido(mv1, jogador, dA)) {
                    int destLogA = interLogA + dB;
                    int destFisA = mov.encontrarCasaPorPosicaoLogica(jogador, destLogA);
                    if (destFisA != -1) {
                        Movimento mv2 = new Movimento(interFisA, destFisA);
                        if (tabuleiro.movimentoValido(mv2, jogador, dB)) pintar(destFisA, jogador);
                    }
                }
            }

            // ordem dB -> dA
            int interLogB = origemLog + dB;
            int interFisB = mov.encontrarCasaPorPosicaoLogica(jogador, interLogB);
            if (interFisB != -1) {
                Movimento mv1b = new Movimento(origemFis, interFisB);
                if (tabuleiro.movimentoValido(mv1b, jogador, dB)) {
                    int destLogB = interLogB + dA;
                    int destFisB = mov.encontrarCasaPorPosicaoLogica(jogador, destLogB);
                    if (destFisB != -1) {
                        Movimento mv2b = new Movimento(interFisB, destFisB);
                        if (tabuleiro.movimentoValido(mv2b, jogador, dA)) pintar(destFisB, jogador);
                    }
                }
            }
        }
    }

    private void pintar(int destinoFis, int jogador) {
        int dono = tabuleiro.getDonoCasa(destinoFis);
        int qtd = tabuleiro.getNumPecas(destinoFis);
        if (dono == 0 || dono == jogador) view.atualizarCorFundoCasa(destinoFis, Color.GREEN);
        else if (qtd == 1) view.atualizarCorFundoCasa(destinoFis, new Color(128, 0, 128));
    }
}

