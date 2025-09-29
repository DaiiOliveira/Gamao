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
    private static final int ORIGEM_BARRA = -2; // origem especial para entrada pela barra

    // Dados
    private final List<Integer> passosRestantes = new ArrayList<>(); // multiconjunto
    private boolean podeSomaNoTurno = false; // só para NÃO-double e enquanto ambos dados existem
    private Integer valorDouble = null;      // != null quando é double (ex.: 3)

    // Botão Pontuar (bear-off)
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

        // Ações de clique nas casas
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            final int idx = i;
            view.addAcaoCasa(i, () -> cliqueCasa(idx));
        }

        // Rolar 2 dados
        view.addAcaoRolarDados(e -> {
            rolarDoisDados();
            origemSelecionada = -1;
            limparDestaques();
            atualizarEstadoPontuar();
            view.setBotaoAtivo(false);

            // se começar a vez com peça na barra, já guia a entrada
            if (temPecaNaBarra(estadoJogo.getJogadorAtual())) {
                origemSelecionada = ORIGEM_BARRA;
                destacarEntradasPossiveis();
            }
        });

        // Botão Pontuar
        view.addAcaoPontuar(e -> pontuarSePossivel());

        view.mostrarMensagem("Jogo iniciado! Jogador 1 começa.");
    }

    // ===================== Configuração / Reinício =====================

    private void configurarPecasIniciais() {
        tabuleiro = new Tabuleiro();

        // Ajuste aqui conforme seu cenário de teste
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

        // zera estado de dados/seleção
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

    // ===================== Dados =====================

    private void rolarDoisDados() {
        Dado d = new Dado();
        int d1 = d.rolar();
        int d2 = d.rolar();
        passosRestantes.clear();
        valorDouble = null;
        podeSomaNoTurno = false;

        if (d1 == d2) {
            valorDouble = d1;
            for (int i = 0; i < 4; i++) passosRestantes.add(d1);
            view.mostrarMensagem("Jogador " + estadoJogo.getJogadorAtual()
                    + " rolou DOUBLE: " + d1 + " e " + d2 + " (4 movimentos de " + d1 + ")");
        } else {
            passosRestantes.add(d1);
            passosRestantes.add(d2);
            podeSomaNoTurno = true; // soma é possível, mas precisará validar intermediário
            view.mostrarMensagem("Jogador " + estadoJogo.getJogadorAtual()
                    + " rolou: " + d1 + " e " + d2);
        }
    }

    // ===================== Helpers de estado =====================

    private boolean temPecaNaBarra(int jogador) {
        return tabuleiro.getBar(jogador) > 0;
    }

    private List<Integer> passosIndividuaisDisponiveis() {
        // únicos, ordem não importa
        List<Integer> res = new ArrayList<>();
        for (int v : passosRestantes) if (!res.contains(v)) res.add(v);
        return res;
    }

    private int contar(int v) { int c=0; for (int x: passosRestantes) if (x==v) c++; return c; }

    private int somaAtual() {
        // só faz sentido quando há exatamente 2 dados restantes e não é double
        return (valorDouble == null && passosRestantes.size() == 2)
                ? (passosRestantes.get(0) + passosRestantes.get(1)) : -1;
    }

    /**
     * Lista passos "clicáveis":
     * - Se há peça na barra: SOMENTE valores individuais (1..6) que existam.
     * - Se é double: SOMENTE individuais (nada de 2×d no clique).
     * - Se é não-double: individuais + (opcional) "soma" (para UI),
     *   mas a soma só será aceita se os dois passos intermediários forem válidos (checado na hora).
     */
    private List<Integer> listarPassosCliquaveis() {
        List<Integer> ops = new ArrayList<>(passosIndividuaisDisponiveis());

        if (temPecaNaBarra(estadoJogo.getJogadorAtual())) {
            return ops; // barra usa apenas individuais
        }

        if (valorDouble != null) {
            return ops; // double fiel: sem 2×d no clique
        }

        if (podeSomaNoTurno) {
            int s = somaAtual();
            if (s > 0 && !ops.contains(s)) ops.add(s); // soma aparece, mas só será validada com intermediário
        }
        return ops;
    }

    // ===================== Clique (entrada + normal) =====================

    private void cliqueCasa(int index) {
        if (passosRestantes.isEmpty()) {
            view.mostrarMensagem("Role os dados para jogar.");
            return;
        }

        int jogador = estadoJogo.getJogadorAtual();

        // ---------- MODO BARRA ----------
        if (temPecaNaBarra(jogador)) {
            if (origemSelecionada == -1) {
                origemSelecionada = ORIGEM_BARRA;
                destacarEntradasPossiveis();
                view.mostrarMensagem("Você tem peças na barra. Entre usando um dado disponível.");
                view.setPontuarAtivo(false);
                return;
            }

            // usuário clicou em uma casa para entrada
            Integer passoEscolhido = null;
            int destinoLog = tabuleiro.posicaoLogica(jogador, index);
            for (int p : passosIndividuaisDisponiveis()) {
                int destFis = tabuleiro.entradaDestinoPorDado(jogador, p);
                if (destFis == index || destinoLog == (p - 1)) { passoEscolhido = p; break; }
            }
            if (passoEscolhido == null) {
                view.mostrarMensagem("Essa casa não é uma entrada válida com os dados atuais.");
                return;
            }

            if (tabuleiro.podeEntrarDaBarraCom(jogador, passoEscolhido)) {
                boolean hit = tabuleiro.entrarDaBarra(jogador, passoEscolhido);
                consumirPassoApenasIndividual(passoEscolhido);

                view.mostrarMensagem("Entrada da barra com " + passoEscolhido + (hit ? " (bateu 1 inimiga)." : "."));
                atualizarView();

                // segue turno
                if (!passosRestantes.isEmpty() && (temPecaNaBarra(jogador) ? haEntradaPossivel(jogador) : haAlgumMovimentoPossivel(jogador))) {
                    if (temPecaNaBarra(jogador)) {
                        origemSelecionada = ORIGEM_BARRA;
                        destacarEntradasPossiveis();
                        view.setPontuarAtivo(false);
                    } else {
                        origemSelecionada = -1;
                        limparDestaques();
                        atualizarEstadoPontuar();
                    }
                    view.setBotaoAtivo(false);
                } else {
                    origemSelecionada = -1;
                    limparDestaques();
                    atualizarEstadoPontuar();
                    finalizarTurno();
                }
            } else {
                view.mostrarMensagem("Entrada bloqueada (torre inimiga).");
            }
            return;
        }

        // ---------- FLUXO NORMAL (sem peça na barra) ----------

        // cancelar seleção
        if (origemSelecionada == index) {
            view.mostrarMensagem("Seleção cancelada. Escolha outra casa.");
            origemSelecionada = -1;
            limparDestaques();
            passoPontuar = null;
            atualizarEstadoPontuar();
            return;
        }

        // selecionar origem
        if (origemSelecionada == -1) {
            if (tabuleiro.getDonoCasa(index) != jogador) {
                view.mostrarMensagem("Selecione uma casa com suas peças!");
                return;
            }
            origemSelecionada = index;
            destacarCasasPossiveis(origemSelecionada);
            view.mostrarMensagem("Origem selecionada: C" + index);
            atualizarEstadoPontuar();
            return;
        }

        // destino
        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);
        int destinoLog = tabuleiro.posicaoLogica(jogador, index);
        int delta = destinoLog - origemLog;

        List<Integer> cliques = listarPassosCliquaveis();
        Integer passoEscolhido = null;
        for (int p : cliques) {
            if (delta == p) { passoEscolhido = p; break; }
        }
        if (passoEscolhido == null) {
            view.mostrarMensagem("Destino não corresponde aos dados disponíveis.");
            return;
        }

        // ===== Caso 1: passo individual =====
        if (valorDouble != null || passoEscolhido != somaAtual()) {
            Movimento mv = new Movimento(origemSelecionada, index);
            if (tabuleiro.movimentoValido(mv, jogador, passoEscolhido)) {
                boolean bearOff = tabuleiro.aplicarMovimento(mv, jogador);
                consumirPasso(passoEscolhido);

                posJogadaAtualizarUI(jogador, bearOff, "Movimento: C" + origemSelecionada + " → C" + index + " (passo " + passoEscolhido + ")");
            } else {
                view.mostrarMensagem("Movimento inválido para este passo.");
            }
            return;
        }

        // ===== Caso 2: jogada longa (d1+d2) em NÃO-double — precisa validar intermediário =====
        int dA = passosRestantes.get(0), dB = passosRestantes.get(1);
        // garantir que dA+dB == passoEscolhido (robustez)
        if (dA + dB != passoEscolhido) { int tmp = dA; dA = dB; dB = tmp; }

        if (tentarAplicarLongaComIntermediario(jogador, origemSelecionada, index, dA, dB)) {
            posJogadaAtualizarUI(jogador, false, "Movimento longo (ordem " + dA + " depois " + dB + "): C" + origemSelecionada + " → C" + index);
            return;
        }
        if (tentarAplicarLongaComIntermediario(jogador, origemSelecionada, index, dB, dA)) {
            posJogadaAtualizarUI(jogador, false, "Movimento longo (ordem " + dB + " depois " + dA + "): C" + origemSelecionada + " → C" + index);
            return;
        }

        view.mostrarMensagem("Jogada longa inválida: ponto intermediário está bloqueado.");
    }

    private void posJogadaAtualizarUI(int jogador, boolean bearOff, String msgMov) {
        view.atualizarBarra(1, tabuleiro.getBar(1));
        view.atualizarBarra(2, tabuleiro.getBar(2));
        if (bearOff) {
            view.mostrarMensagem("Bear-off! Jogador " + jogador + " pontuou 1 peça.");
            view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
            checarVencedor();
        } else {
            view.mostrarMensagem(msgMov);
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
    }

    // ===== Jogada longa (não-double) com validação de intermediário, sem aplicar antes de validar =====
    private boolean tentarAplicarLongaComIntermediario(int jogador, int origem, int destinoFinal, int dA, int dB) {
        int origemLog = tabuleiro.posicaoLogica(jogador, origem);
        int interLog = origemLog + dA;
        int interFis = encontrarCasaPorPosicaoLogica(jogador, interLog);
        if (interFis == -1) return false; // não permitimos bear-off no meio de uma longa

        Movimento mv1 = new Movimento(origem, interFis);
        if (!tabuleiro.movimentoValido(mv1, jogador, dA)) return false;

        int destLogEsperado = tabuleiro.posicaoLogica(jogador, destinoFinal);
        if (destLogEsperado != interLog + dB) return false;

        Movimento mv2 = new Movimento(interFis, destinoFinal);
        if (!tabuleiro.movimentoValido(mv2, jogador, dB)) return false;

        // Ambos válidos -> aplicar os dois passos e consumir os dois dados
        boolean b1 = tabuleiro.aplicarMovimento(mv1, jogador);
        consumirPassoApenasValor(dA);
        boolean b2 = tabuleiro.aplicarMovimento(mv2, jogador);
        consumirPassoApenasValor(dB);
        return true;
    }

    // ===================== Destaques =====================

    private void destacarEntradasPossiveis() {
        limparDestaques();
        int jogador = estadoJogo.getJogadorAtual();
        for (int p : passosIndividuaisDisponiveis()) {
            int dest = tabuleiro.entradaDestinoPorDado(jogador, p);
            if (dest == -1) continue;
            if (!tabuleiro.podeEntrarDaBarraCom(jogador, p)) continue;

            int dono = tabuleiro.getDonoCasa(dest);
            int qtd = tabuleiro.getNumPecas(dest);
            if (dono == 0 || dono == jogador) view.atualizarCorFundoCasa(dest, Color.GREEN);
            else if (qtd == 1) view.atualizarCorFundoCasa(dest, new Color(128, 0, 128));
        }
        view.setPontuarAtivo(false);
    }

    private void destacarCasasPossiveis(int origem) {
        limparDestaques();
        view.destacarCasa(origem, true);

        int jogador = estadoJogo.getJogadorAtual();
        int origemLog = tabuleiro.posicaoLogica(jogador, origem);

        // Sempre destacar individuais
        for (int p : passosIndividuaisDisponiveis()) {
            int destinoLog = origemLog + p;
            int destinoFis = encontrarCasaPorPosicaoLogica(jogador, destinoLog);
            if (destinoFis == -1) continue;
            int dono = tabuleiro.getDonoCasa(destinoFis);
            int qtd = tabuleiro.getNumPecas(destinoFis);
            if (dono == 0 || dono == jogador) view.atualizarCorFundoCasa(destinoFis, Color.GREEN);
            else if (qtd == 1) view.atualizarCorFundoCasa(destinoFis, new Color(128, 0, 128));
        }

        // Longa (somente não-double): destacar destino FINAL se ambos os passos forem válidos (em alguma ordem)
        if (valorDouble == null && podeSomaNoTurno && passosRestantes.size() == 2) {
            int dA = passosRestantes.get(0), dB = passosRestantes.get(1);

            // ordem dA -> dB
            int interLogA = origemLog + dA;
            int interFisA = encontrarCasaPorPosicaoLogica(jogador, interLogA);
            if (interFisA != -1) {
                Movimento mv1 = new Movimento(origem, interFisA);
                if (tabuleiro.movimentoValido(mv1, jogador, dA)) {
                    int destLogA = interLogA + dB;
                    int destFisA = encontrarCasaPorPosicaoLogica(jogador, destLogA);
                    if (destFisA != -1) {
                        Movimento mv2 = new Movimento(interFisA, destFisA);
                        if (tabuleiro.movimentoValido(mv2, jogador, dB)) {
                            pintarFinalLonga(destFisA, jogador);
                        }
                    }
                }
            }

            // ordem dB -> dA
            int interLogB = origemLog + dB;
            int interFisB = encontrarCasaPorPosicaoLogica(jogador, interLogB);
            if (interFisB != -1) {
                Movimento mv1b = new Movimento(origem, interFisB);
                if (tabuleiro.movimentoValido(mv1b, jogador, dB)) {
                    int destLogB = interLogB + dA;
                    int destFisB = encontrarCasaPorPosicaoLogica(jogador, destLogB);
                    if (destFisB != -1) {
                        Movimento mv2b = new Movimento(interFisB, destFisB);
                        if (tabuleiro.movimentoValido(mv2b, jogador, dA)) {
                            pintarFinalLonga(destFisB, jogador);
                        }
                    }
                }
            }
        }
    }

    private void pintarFinalLonga(int destinoFis, int jogador) {
        int dono = tabuleiro.getDonoCasa(destinoFis);
        int qtd = tabuleiro.getNumPecas(destinoFis);
        if (dono == 0 || dono == jogador) view.atualizarCorFundoCasa(destinoFis, Color.GREEN);
        else if (qtd == 1) view.atualizarCorFundoCasa(destinoFis, new Color(128, 0, 128));
    }

    // ===================== Pontuar =====================

    private void pontuarSePossivel() {
        if (temPecaNaBarra(estadoJogo.getJogadorAtual())) {
            view.mostrarMensagem("Primeiro entre as peças da barra.");
            return;
        }
        if (origemSelecionada == -1 || passoPontuar == null) return;

        int jogador = estadoJogo.getJogadorAtual();
        int destinoSentinela = (jogador == 1) ? 24 : -1;

        Movimento mv = new Movimento(origemSelecionada, destinoSentinela);
        if (tabuleiro.movimentoValido(mv, jogador, passoPontuar)) {
            boolean bearOff = tabuleiro.aplicarMovimento(mv, jogador);
            consumirPasso(passoPontuar);

            view.mostrarMensagem("Pontuar: C" + origemSelecionada + " → fora (passo " + passoPontuar + ")");
            if (bearOff) {
                view.atualizarPlacar(tabuleiro.getPontuacao(1), tabuleiro.getPontuacao(2));
                checarVencedor();
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

        if (temPecaNaBarra(estadoJogo.getJogadorAtual())) {
            origemSelecionada = ORIGEM_BARRA;
            destacarEntradasPossiveis();
        } else {
            origemSelecionada = -1;
        }
    }

    // ===================== Vitória =====================

    private void checarVencedor() {
        int restantesJ1 = tabuleiro.contarPecasNoTabuleiro(1) + tabuleiro.getBar(1);
        int restantesJ2 = tabuleiro.contarPecasNoTabuleiro(2) + tabuleiro.getBar(2);
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

    // ===================== Consumo de passos =====================

    private void consumirPassoApenasIndividual(int passo) {
        removerUma(passo);
        if (passosRestantes.size() < 2) podeSomaNoTurno = false;
    }

    private void consumirPassoApenasValor(int passo) {
        consumirPassoApenasIndividual(passo);
    }

    private void consumirPasso(int passo) {
        if (temPecaNaBarra(estadoJogo.getJogadorAtual())) {
            consumirPassoApenasIndividual(passo);
            return;
        }

        if (valorDouble != null) {
            // fiel: só individuais; nada de 2×d
            removerUma(passo);
            if (passosRestantes.size() < 2) podeSomaNoTurno = false;
        } else {
            // se for soma, consome os dois; senão, consome o individual
            if (podeSomaNoTurno && somaAtual() == passo) {
                passosRestantes.clear();
                podeSomaNoTurno = false;
            } else {
                removerUma(passo);
                if (passosRestantes.size() < 2) podeSomaNoTurno = false;
            }
        }
    }

    private void removerUma(int v) {
        for (int i = 0; i < passosRestantes.size(); i++) {
            if (passosRestantes.get(i) == v) { passosRestantes.remove(i); break; }
        }
        if (passosRestantes.size() < 2) podeSomaNoTurno = false;
    }

    // ===================== Habilitar "Pontuar" =====================

    private void atualizarEstadoPontuar() {
        passoPontuar = null;
        view.setPontuarAtivo(false);

        if (temPecaNaBarra(estadoJogo.getJogadorAtual())) return;
        if (origemSelecionada == -1 || passosRestantes.isEmpty()) return;

        int jogador = estadoJogo.getJogadorAtual();
        if (!tabuleiro.todasPecasNoQuadranteFinal(jogador)) return;

        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);

        // candidatos: só individuais (bear-off é por passo individual)
        List<Integer> cand = passosIndividuaisDisponiveis();
        cand.sort(Integer::compareTo);

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

    // ===================== Verificações auxiliares =====================

    private int encontrarCasaPorPosicaoLogica(int jogador, int posLog) {
        if (posLog < 0 || posLog > 23) return -1;
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            if (tabuleiro.posicaoLogica(jogador, i) == posLog) return i;
        }
        return -1;
    }

    private boolean haEntradaPossivel(int jogador) {
        for (int p : passosIndividuaisDisponiveis()) {
            if (tabuleiro.podeEntrarDaBarraCom(jogador, p)) return true;
        }
        return false;
    }

    private boolean haAlgumMovimentoPossivel(int jogador) {
        if (passosRestantes.isEmpty()) return false;

        // se tem peça na barra, ver só entradas
        if (temPecaNaBarra(jogador)) return haEntradaPossivel(jogador);

        // individuais
        for (int casa = 0; casa < tabuleiro.getNumCasas(); casa++) {
            if (tabuleiro.getDonoCasa(casa) != jogador || tabuleiro.getNumPecas(casa) == 0) continue;
            int origemLog = tabuleiro.posicaoLogica(jogador, casa);
            for (int p : passosIndividuaisDisponiveis()) {
                int destinoLog = origemLog + p;
                int destinoFis = encontrarCasaPorPosicaoLogica(jogador, destinoLog);
                int destinoValid = (destinoFis == -1) ? ((jogador == 1) ? 24 : -1) : destinoFis;
                Movimento mv = new Movimento(casa, destinoValid);
                if (tabuleiro.movimentoValido(mv, jogador, p)) return true;
            }
        }

        // longa não-double com intermediário
        if (valorDouble == null && podeSomaNoTurno && passosRestantes.size() == 2) {
            int dA = passosRestantes.get(0), dB = passosRestantes.get(1);
            for (int casa = 0; casa < tabuleiro.getNumCasas(); casa++) {
                if (tabuleiro.getDonoCasa(casa) != jogador || tabuleiro.getNumPecas(casa) == 0) continue;
                int origemLog = tabuleiro.posicaoLogica(jogador, casa);

                // ordem dA -> dB
                int interLogA = origemLog + dA;
                int interFisA = encontrarCasaPorPosicaoLogica(jogador, interLogA);
                if (interFisA != -1) {
                    Movimento m1 = new Movimento(casa, interFisA);
                    if (tabuleiro.movimentoValido(m1, jogador, dA)) {
                        int destLogA = interLogA + dB;
                        int destFisA = encontrarCasaPorPosicaoLogica(jogador, destLogA);
                        if (destFisA != -1) {
                            Movimento m2 = new Movimento(interFisA, destFisA);
                            if (tabuleiro.movimentoValido(m2, jogador, dB)) return true;
                        }
                    }
                }

                // ordem dB -> dA
                int interLogB = origemLog + dB;
                int interFisB = encontrarCasaPorPosicaoLogica(jogador, interLogB);
                if (interFisB != -1) {
                    Movimento m1b = new Movimento(casa, interFisB);
                    if (tabuleiro.movimentoValido(m1b, jogador, dB)) {
                        int destLogB = interLogB + dA;
                        int destFisB = encontrarCasaPorPosicaoLogica(jogador, destLogB);
                        if (destFisB != -1) {
                            Movimento m2b = new Movimento(interFisB, destFisB);
                            if (tabuleiro.movimentoValido(m2b, jogador, dA)) return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // ===================== UI helpers =====================

    private void atualizarView() {
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            int n = tabuleiro.getNumPecas(i);
            int d = tabuleiro.getDonoCasa(i);
            view.atualizarCasa(i, (d != 0) ? n : 0, d == 1);
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