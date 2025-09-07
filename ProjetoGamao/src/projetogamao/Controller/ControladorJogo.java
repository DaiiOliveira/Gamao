package projetogamao.Controller;

import projetogamao.View.ViewSwing;
import projetogamao.Model.*;

import java.awt.*;

public class ControladorJogo {
    private Tabuleiro tabuleiro;
    private EstadoJogo estadoJogo;
    private ViewSwing view;
    private int origemSelecionada = -1;
    private int valorDado = 0;

    public ControladorJogo(ViewSwing view) {
        this.view = view;
        this.tabuleiro = new Tabuleiro();
        this.estadoJogo = new EstadoJogo();

        // Peças iniciais
        // Jogador 1
        tabuleiro.adicionarPecas(11, 2, 1);
        tabuleiro.adicionarPecas(0, 5, 1);
        tabuleiro.adicionarPecas(16, 3, 1);
        tabuleiro.adicionarPecas(18, 5, 1);

        // Jogador 2
        tabuleiro.adicionarPecas(23, 2, 2);
        tabuleiro.adicionarPecas(12, 5, 2);
        tabuleiro.adicionarPecas(4, 3, 2);
        tabuleiro.adicionarPecas(6, 5, 2);

        atualizarView();

        // Ação das casas
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            final int index = i;
            view.addAcaoCasa(i, () -> cliqueCasa(index));
        }

        // Botão rolar dados
        view.addAcaoRolarDados(e -> {
            Dado dado = new Dado();
            valorDado = dado.rolar();
            view.mostrarMensagem("Jogador " + estadoJogo.getJogadorAtual() + " rolou: " + valorDado);
            view.setBotaoAtivo(false);
        });

        view.mostrarMensagem("Jogo iniciado! Jogador 1 começa. Boa sorte!");
    }

    private void cliqueCasa(int index) {
        if (valorDado == 0) {
            view.mostrarMensagem("Role o dado antes de mover!");
            return;
        }

        if (origemSelecionada == index) {
            view.mostrarMensagem("Seleção cancelada. Escolha outra casa.");
            origemSelecionada = -1;
            limparDestaques();
            return;
        }

        if (origemSelecionada == -1) {
            if (tabuleiro.getDonoCasa(index) != estadoJogo.getJogadorAtual()) {
                view.mostrarMensagem("Selecione uma casa com suas peças!");
                return;
            }
            origemSelecionada = index;
            view.destacarCasa(index, true);
            destacarCasasPossiveis(origemSelecionada);
            view.mostrarMensagem("Origem selecionada: " + index);
        } else {
            int destino = index;
            Movimento movimento = new Movimento(origemSelecionada, destino);

            if (tabuleiro.movimentoValido(movimento, estadoJogo.getJogadorAtual(), valorDado)) {
                boolean bearOff = tabuleiro.aplicarMovimento(movimento, estadoJogo.getJogadorAtual());

                // Atualiza barra se alguma peça foi comida
                view.atualizarBarra(1, tabuleiro.getBar(1));
                view.atualizarBarra(2, tabuleiro.getBar(2));

                if (bearOff) {
                    view.mostrarMensagem("Peça retirada do tabuleiro! Pontuação do jogador " +
                            estadoJogo.getJogadorAtual() + ": " +
                            tabuleiro.getPontuacao(estadoJogo.getJogadorAtual()));
                    view.atualizarBearOff(estadoJogo.getJogadorAtual(),
                            tabuleiro.getPontuacao(estadoJogo.getJogadorAtual()));
                } else {
                    view.mostrarMensagem("Movimento de " + origemSelecionada + " para " + destino + " realizado!");
                }

                estadoJogo.proximoTurno();
                atualizarView();
                view.mostrarMensagem("Agora é a vez do jogador " + estadoJogo.getJogadorAtual());
            } else {
                view.mostrarMensagem("Movimento inválido! Tente outra casa.");
            }

            origemSelecionada = -1;
            valorDado = 0;
            limparDestaques();
            view.setBotaoAtivo(true);
        }
    }

    private void atualizarView() {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int numPecas = tabuleiro.getNumPecas(i);
            int dono = tabuleiro.getDonoCasa(i);
            if (dono != 0) view.atualizarCasa(i, numPecas, dono == 1);
            else view.atualizarCasa(i, 0, true);
        }
    }

    private void destacarCasasPossiveis(int origem) {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int origemLogica = tabuleiro.posicaoLogica(estadoJogo.getJogadorAtual(), origem);
            int destinoLogica = tabuleiro.posicaoLogica(estadoJogo.getJogadorAtual(), i);

            if (destinoLogica - origemLogica != valorDado) continue;

            int dono = tabuleiro.getDonoCasa(i);
            int qtd = tabuleiro.getNumPecas(i);

            if (dono == 0 || dono == estadoJogo.getJogadorAtual()) view.atualizarCorFundoCasa(i, Color.GREEN);
            else if (qtd == 1) view.atualizarCorFundoCasa(i, new Color(128, 0, 128));
        }
    }

    private void limparDestaques() {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int numPecas = tabuleiro.getNumPecas(i);
            int dono = tabuleiro.getDonoCasa(i);
            view.atualizarCasa(i, numPecas, dono == 1);
        }
    }
}
