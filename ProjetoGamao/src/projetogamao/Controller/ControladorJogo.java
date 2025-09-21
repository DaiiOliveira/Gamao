package projetogamao.Controller;

import projetogamao.View.ViewSwing;
import projetogamao.Model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ControladorJogo {

    private Tabuleiro tabuleiro;
    private EstadoJogo estadoJogo;
    private final ViewSwing view;

    private int origemSelecionada = -1;

    // Dados: suporta doubles (4 passos), soma quando não-double
    private final List<Integer> passosRestantes = new ArrayList<>();
    private boolean podeSomaNoTurno = false;
    private Integer valorDouble = null;

    // Botão Pontuar: passo que será consumido
    private Integer passoPontuar = null;

    public ControladorJogo(ViewSwing view) {
        this.view = view;
        this.estadoJogo = new EstadoJogo();
        this.tabuleiro = new Tabuleiro();

        configurarPecasIniciais();
        atualizarView();
        view.atualizarBarra(1, tabuleiro.getBar(1));
        view.atualizarBarra(2, tabuleiro.getBar(2));
        view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
        view.setPontuarAtivo(false);

        // ações
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            final int idx = i;
            view.addAcaoCasa(i, () -> cliqueCasa(idx));
        }
        view.addAcaoRolarDados(e -> {
            rolarDoisDados();
            origemSelecionada = -1;
            limparDestaques();
            atualizarEstadoPontuar(); // << importante
            view.setBotaoAtivo(false);
        });
        view.addAcaoPontuar(e -> pontuarSePossivel());

        view.mostrarMensagem("Jogo iniciado! Jogador 1 começa.");
    }

    // ===== Setup / Reinício =====
    private void configurarPecasIniciais() {
        tabuleiro = new Tabuleiro();
        // Jogador 1
//        tabuleiro.adicionarPecas(11, 2, 1);
//        tabuleiro.adicionarPecas(0, 5, 1);
//        tabuleiro.adicionarPecas(16, 3, 1);
        tabuleiro.adicionarPecas(18, 5, 1);
        // Jogador 2
//        tabuleiro.adicionarPecas(23, 2, 2);
//        tabuleiro.adicionarPecas(12, 5, 2);
//        tabuleiro.adicionarPecas(4, 3, 2);
        tabuleiro.adicionarPecas(6, 5, 2);

        passosRestantes.clear();
        podeSomaNoTurno = false;
        valorDouble = null;
        passoPontuar = null;
        origemSelecionada = -1;
        estadoJogo = new EstadoJogo();
    }

    private void reiniciarJogo() {
        configurarPecasIniciais();
        atualizarView();
        view.atualizarBarra(1, tabuleiro.getBar(1));
        view.atualizarBarra(2, tabuleiro.getBar(2));
        view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
        view.setBotaoAtivo(true);
        view.setPontuarAtivo(false);
        view.limparLog();
        view.mostrarMensagem("Novo jogo iniciado! Jogador 1 começa.");
    }

    // ===== Dados =====
    private void rolarDoisDados() {
        Dado d = new Dado();
        int d1 = d.rolar();
        int d2 = d.rolar();
        passosRestantes.clear();
        valorDouble = null;
        podeSomaNoTurno = false;

        if (d1 == d2) {
            valorDouble = d1;
            for (int i = 0; i < 4; i++) {
                passosRestantes.add(d1);
            }
            view.mostrarMensagem("Jogador " + estadoJogo.getJogadorAtual() + " rolou DOUBLE: " + d1 + " e " + d2);
        } else {
            passosRestantes.add(d1);
            passosRestantes.add(d2);
            podeSomaNoTurno = true;
            view.mostrarMensagem("Jogador " + estadoJogo.getJogadorAtual() + " rolou: " + d1 + " e " + d2);
        }
    }

    // ===== Clique =====
    private void cliqueCasa(int index) {
        if (passosRestantes.isEmpty()) {
            view.mostrarMensagem("Role os dados para jogar.");
            return;
        }

        if (origemSelecionada == index) {
            view.mostrarMensagem("Seleção cancelada. Escolha outra casa.");
            origemSelecionada = -1;
            limparDestaques();
            passoPontuar = null;
            atualizarEstadoPontuar();
            return;
        }

        if (origemSelecionada == -1) {
            if (tabuleiro.getDonoCasa(index) != estadoJogo.getJogadorAtual()) {
                view.mostrarMensagem("Selecione uma casa com suas peças!");
                return;
            }
            origemSelecionada = index;
            destacarCasasPossiveis(origemSelecionada);
            view.mostrarMensagem("Origem selecionada: C" + index);
            atualizarEstadoPontuar(); // << importante
            return;
        }

        // destino
        int jogador = estadoJogo.getJogadorAtual();
        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);
        int destinoLog = tabuleiro.posicaoLogica(jogador, index);

        Integer passoEscolhido = null;
        for (int p : listarPassosCliquaveis()) {
            if (destinoLog == origemLog + p) {
                passoEscolhido = p;
                break;
            }
        }
        if (passoEscolhido == null) {
            view.mostrarMensagem("Destino não corresponde aos dados disponíveis.");
            return;
        }

        Movimento mv = new Movimento(origemSelecionada, index);
        if (tabuleiro.movimentoValido(mv, jogador, passoEscolhido)) {
            boolean bearOff = tabuleiro.aplicarMovimento(mv, jogador);
            consumirPasso(passoEscolhido);

            // UI
            view.atualizarBarra(1, tabuleiro.getBar(1));
            view.atualizarBarra(2, tabuleiro.getBar(2));
            if (bearOff) {
                view.mostrarMensagem("Bear-off! Jogador " + jogador + " pontuou 1 peça.");
                view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
                checarVencedor(); // << garante tela de vencedor
            } else {
                view.mostrarMensagem("Movimento: C" + origemSelecionada + " → C" + index + " (passo " + passoEscolhido + ")");
            }

            origemSelecionada = -1;
            limparDestaques();
            atualizarView();
            atualizarEstadoPontuar();

            if (!passosRestantes.isEmpty() && haAlgumMovimentoPossivel(jogador)) {
                view.mostrarMensagem("Ainda há movimentos disponíveis. Continue jogando.");
                view.setBotaoAtivo(false);
            } else {
                finalizarTurno();
            }
        } else {
            view.mostrarMensagem("Movimento inválido para este passo.");
        }
    }

    // ===== Pontuar =====
    private void pontuarSePossivel() {
        if (origemSelecionada == -1 || passoPontuar == null) {
            return;
        }

        int jogador = estadoJogo.getJogadorAtual();
        int destinoSentinela = (jogador == 1) ? 24 : -1;

        Movimento mv = new Movimento(origemSelecionada, destinoSentinela);
        if (tabuleiro.movimentoValido(mv, jogador, passoPontuar)) {
            boolean bearOff = tabuleiro.aplicarMovimento(mv, jogador);
            consumirPasso(passoPontuar);

            view.mostrarMensagem("Pontuar: C" + origemSelecionada + " → fora (passo " + passoPontuar + ")");
            if (bearOff) {
                view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
                checarVencedor(); // << garante tela de vencedor
            }

            origemSelecionada = -1;
            limparDestaques();
            atualizarView();
            atualizarEstadoPontuar();

            if (!passosRestantes.isEmpty() && haAlgumMovimentoPossivel(jogador)) {
                view.mostrarMensagem("Ainda há movimentos disponíveis. Continue jogando.");
                view.setBotaoAtivo(false);
            } else {
                finalizarTurno();
            }
        } else {
            view.mostrarMensagem("Não é possível pontuar com o passo selecionado.");
        }
    }

    private void finalizarTurno() {
        passoPontuar = null;
        view.setPontuarAtivo(false);
        view.setBotaoAtivo(true);
        estadoJogo.proximoTurno();
        view.mostrarMensagem("Vez do Jogador " + estadoJogo.getJogadorAtual());
    }

    // ===== Vitória =====
    private void checarVencedor() {
        int restantesJ1 = tabuleiro.contarPecasNoTabuleiro(1) + tabuleiro.getBar(1);
        int restantesJ2 = tabuleiro.contarPecasNoTabuleiro(2) + tabuleiro.getBar(2);

        // Vitória dinâmica: quando não há mais peças nem no tabuleiro nem na barra
        if (restantesJ1 == 0) {
            view.mostrarTelaVencedor("Jogador 1",
                    tabuleiro.getPontuacao(1),
                    tabuleiro.getPontuacao(2),
                    this::reiniciarJogo);
            return;
        }
        if (restantesJ2 == 0) {
            view.mostrarTelaVencedor("Jogador 2",
                    tabuleiro.getPontuacao(1),
                    tabuleiro.getPontuacao(2),
                    this::reiniciarJogo);
        }
    }

    // ===== Dados / Doubles =====
    private void consumirPasso(int passo) {
        if (valorDouble != null) {
            // longa 2*d consome dois passos
            if (passo == 2 * valorDouble) {
                removerUma(valorDouble);
                removerUma(valorDouble);
            } else {
                removerUma(passo);
            }
        } else {
            // soma consome ambos
            if (podeSomaNoTurno && somaAtual() == passo) {
                passosRestantes.clear();
                podeSomaNoTurno = false;
            } else {
                removerUma(passo);
                if (passosRestantes.size() < 2) {
                    podeSomaNoTurno = false;
                }
            }
        }
    }

    private void removerUma(int v) {
        for (int i = 0; i < passosRestantes.size(); i++) {
            if (passosRestantes.get(i) == v) {
                passosRestantes.remove(i);
                break;
            }
        }
        if (passosRestantes.size() < 2) {
            podeSomaNoTurno = false;
        }
    }

    private int somaAtual() {
        return (passosRestantes.size() == 2) ? (passosRestantes.get(0) + passosRestantes.get(1)) : -1;
    }

    private List<Integer> listarPassosCliquaveis() {
        List<Integer> ops = new ArrayList<>();
        for (int v : passosRestantes) {
            if (!ops.contains(v)) {
                ops.add(v);
            }
        }
        if (valorDouble == null && podeSomaNoTurno) {
            int s = somaAtual();
            if (s > 0) {
                ops.add(s);
            }
        }
        if (valorDouble != null && contar(valorDouble) >= 2) {
            int s2 = 2 * valorDouble;
            if (!ops.contains(s2)) {
                ops.add(s2);
            }
        }
        return ops;
    }

    private int contar(int v) {
        int c = 0;
        for (int x : passosRestantes) {
            if (x == v) {
                c++;
            }
        }
        return c;
    }

    // ===== Destaques / Pontuar enable =====
    private void destacarCasasPossiveis(int origem) {
        limparDestaques();
        view.destacarCasa(origem, true);

        int jogador = estadoJogo.getJogadorAtual();
        int origemLog = tabuleiro.posicaoLogica(jogador, origem);

        for (int p : listarPassosCliquaveis()) {
            int destinoLog = origemLog + p;
            int destinoFis = encontrarCasaPorPosicaoLogica(jogador, destinoLog);
            if (destinoFis == -1) {
                continue; // bear off indicado pelo botão
            }
            int dono = tabuleiro.getDonoCasa(destinoFis);
            int qtd = tabuleiro.getNumPecas(destinoFis);
            if (dono == 0 || dono == jogador) {
                view.atualizarCorFundoCasa(destinoFis, Color.GREEN);
            } else if (qtd == 1) {
                view.atualizarCorFundoCasa(destinoFis, new Color(128, 0, 128));
            }
        }
    }

    private void atualizarEstadoPontuar() {
        // sempre visível; liga/desliga aqui
        passoPontuar = null;
        view.setPontuarAtivo(false);

        if (origemSelecionada == -1 || passosRestantes.isEmpty()) {
            return;
        }

        int jogador = estadoJogo.getJogadorAtual();
        if (!tabuleiro.todasPecasNoQuadranteFinal(jogador)) {
            return;
        }

        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);

        List<Integer> cand = listarPassosCliquaveis();
        cand.sort(Integer::compareTo); // prefere menor passo

        for (int p : cand) {
            if (origemLog + p >= 24) {
                int destinoSentinela = (jogador == 1) ? 24 : -1;
                Movimento mv = new Movimento(origemSelecionada, destinoSentinela);
                if (tabuleiro.movimentoValido(mv, jogador, p)) {
                    passoPontuar = p;
                    view.setPontuarAtivo(true);
                    return;
                }
            }
        }
    }

    private int encontrarCasaPorPosicaoLogica(int jogador, int posLog) {
        if (posLog < 0 || posLog > 23) {
            return -1;
        }
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            if (tabuleiro.posicaoLogica(jogador, i) == posLog) {
                return i;
            }
        }
        return -1;
    }

    private boolean haAlgumMovimentoPossivel(int jogador) {
        if (passosRestantes.isEmpty()) {
            return false;
        }
        for (int casa = 0; casa < tabuleiro.getNumCasas(); casa++) {
            if (tabuleiro.getDonoCasa(casa) != jogador || tabuleiro.getNumPecas(casa) == 0) {
                continue;
            }
            int origemLog = tabuleiro.posicaoLogica(jogador, casa);
            for (int p : listarPassosCliquaveis()) {
                int destinoLog = origemLog + p;
                int destinoFis = encontrarCasaPorPosicaoLogica(jogador, destinoLog);
                int destinoValid = (destinoFis == -1) ? ((jogador == 1) ? 24 : -1) : destinoFis;
                Movimento mv = new Movimento(casa, destinoValid);
                if (tabuleiro.movimentoValido(mv, jogador, p)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===== UI helpers =====
    private void atualizarView() {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int n = tabuleiro.getNumPecas(i);
            int d = tabuleiro.getDonoCasa(i);
            if (d != 0) {
                view.atualizarCasa(i, n, d == 1);
            } else {
                view.atualizarCasa(i, 0, true);
            }
        }
        view.atualizarBarra(1, tabuleiro.getBar(1));
        view.atualizarBarra(2, tabuleiro.getBar(2));
        view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
    }

    private void limparDestaques() {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int n = tabuleiro.getNumPecas(i);
            int d = tabuleiro.getDonoCasa(i);
            view.atualizarCasa(i, (d != 0) ? n : 0, d == 1);
        }
    }
}
