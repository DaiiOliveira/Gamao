package projetogamao.Controller;

import projetogamao.Model.Movimento;
import projetogamao.Model.Tabuleiro;
import projetogamao.View.ViewSwing;

public class GameController {
    private final ViewSwing view;

    private Tabuleiro tabuleiro;

    private final DiceController dados;
    private final MovementController mov;
    private final EntradaBarraController barra;
    private final DestaqueController destaque;
    private final TurnoVitoriaController turno;

    private int origemSelecionada = -1;
    private static final int ORIGEM_BARRA = -2;

    public GameController(ViewSwing view) {
        this.view = view;
        this.tabuleiro = new Tabuleiro();
        this.dados = new DiceController();
        this.mov = new MovementController(tabuleiro);
        this.barra = new EntradaBarraController(tabuleiro, dados);
        this.turno = new TurnoVitoriaController(tabuleiro, view, this::reiniciarJogo);
        this.destaque = new DestaqueController(tabuleiro, view, dados, mov);

        configurarPecasIniciais();
        atualizarView();
        view.setPontuarAtivo(false);

        // binds
        for (int i = 0; i < tabuleiro.getNumCasas(); i++) {
            final int idx = i;
            view.addAcaoCasa(i, () -> cliqueCasa(idx));
        }
        view.addAcaoRolarDados(e -> rolarDados());
        view.addAcaoPontuar(e -> pontuar());

        view.mostrarMensagem("Jogo iniciado! Jogador 1 começa.");
    }

    // ===== Setup =====
    private void configurarPecasIniciais() {
        tabuleiro = new Tabuleiro();

        // Exemplo (mude como quiser)
        tabuleiro.adicionarPecas(18, 5, 1);
        tabuleiro.adicionarPecas(6, 5, 2);

        origemSelecionada = -1;
        // atualiza controllers que dependem do tabuleiro
        // (mov, barra, destaque, turno) já apontam pro mesmo `tabuleiro` (referência)
    }

    private void reiniciarJogo() {
        configurarPecasIniciais();
        atualizarView();
        view.setBotaoAtivo(true);
        view.setPontuarAtivo(false);
        view.limparLog();
        view.mostrarMensagem("Novo jogo iniciado! Jogador 1 começa.");
    }

    // ===== View Sync =====
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

    // ===== Regras de UI (Rolar/Pontuar/Clique) =====
    private void rolarDados() {
        dados.rolar();
        origemSelecionada = -1;
        destaque.limpar();
        atualizarEstadoPontuar();
        view.setBotaoAtivo(false);

        if (barra.temPecaNaBarra(turno.getJogadorAtual())) {
            origemSelecionada = ORIGEM_BARRA;
            destaque.destacarEntradasPossiveis(turno.getJogadorAtual());
        }
    }

    private void pontuar() {
        if (barra.temPecaNaBarra(turno.getJogadorAtual())) {
            view.mostrarMensagem("Primeiro entre as peças da barra.");
            return;
        }
        if (origemSelecionada == -1) return;

        int jogador = turno.getJogadorAtual();
        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);

        // candidatos: só individuais
        int passoSelecionado = -1;
        for (int p : dados.individuaisDisponiveis()) {
            if (origemLog + p >= 24) {
                Movimento mv = new Movimento(origemSelecionada, (jogador == 1) ? 24 : -1);
                if (tabuleiro.movimentoValido(mv, jogador, p)) { passoSelecionado = p; break; }
            }
        }
        if (passoSelecionado == -1) {
            view.mostrarMensagem("Não é possível pontuar desta casa com os dados atuais.");
            return;
        }

        boolean bear = tabuleiro.aplicarMovimento(new Movimento(origemSelecionada, (jogador == 1) ? 24 : -1), jogador);
        dados.consumirIndividual(passoSelecionado);

        if (bear) {
            view.mostrarMensagem("Pontuar: C" + origemSelecionada + " → fora (passo " + passoSelecionado + ")");
            atualizarView();
            turno.checarVencedor();
        }

        origemSelecionada = -1;
        destaque.limpar();
        atualizarView();
        atualizarEstadoPontuar();

        if (dados.temPassos() && haAlgumMovimentoPossivel()) {
            view.mostrarMensagem("Ainda há movimentos disponíveis. Continue jogando.");
            view.setBotaoAtivo(false);
        } else {
            finalizarTurno();
        }
    }

    private void cliqueCasa(int index) {
        if (!dados.temPassos()) {
            view.mostrarMensagem("Role os dados para jogar.");
            return;
        }
        int jogador = turno.getJogadorAtual();

        // Barra: só entrada
        if (barra.temPecaNaBarra(jogador)) {
            if (origemSelecionada == -1) {
                origemSelecionada = ORIGEM_BARRA;
                destaque.destacarEntradasPossiveis(jogador);
                view.setPontuarAtivo(false);
                return;
            }
            if (barra.tentarEntrarNaCasa(jogador, index)) {
                view.mostrarMensagem("Entrada realizada.");
                atualizarView();
                if (dados.temPassos() && (barra.temPecaNaBarra(jogador) ? barra.haEntradaPossivel(jogador) : haAlgumMovimentoPossivel())) {
                    if (barra.temPecaNaBarra(jogador)) {
                        origemSelecionada = ORIGEM_BARRA;
                        destaque.destacarEntradasPossiveis(jogador);
                        view.setPontuarAtivo(false);
                    } else {
                        origemSelecionada = -1;
                        destaque.limpar();
                        atualizarEstadoPontuar();
                    }
                    view.setBotaoAtivo(false);
                } else {
                    origemSelecionada = -1;
                    destaque.limpar();
                    atualizarEstadoPontuar();
                    finalizarTurno();
                }
            } else {
                view.mostrarMensagem("Casa inválida para a entrada com os dados atuais.");
            }
            return;
        }

        // cancelar seleção
        if (origemSelecionada == index) {
            origemSelecionada = -1;
            destaque.limpar();
            atualizarEstadoPontuar();
            view.mostrarMensagem("Seleção cancelada.");
            return;
        }

        // selecionar origem
        if (origemSelecionada == -1) {
            if (tabuleiro.getDonoCasa(index) != jogador) {
                view.mostrarMensagem("Selecione uma casa com suas peças!");
                return;
            }
            origemSelecionada = index;
            destaque.destacarDaOrigem(jogador, origemSelecionada);
            atualizarEstadoPontuar();
            view.mostrarMensagem("Origem selecionada: C" + index);
            return;
        }

        // destino (individual ou longa não-double)
        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);
        int destinoLog = tabuleiro.posicaoLogica(jogador, index);
        int delta = destinoLog - origemLog;

        Integer passoEscolhido = null;
        for (int p : dados.passosClicaveis(false)) {
            if (delta == p) { passoEscolhido = p; break; }
        }
        if (passoEscolhido == null) {
            view.mostrarMensagem("Destino não corresponde aos dados disponíveis.");
            return;
        }

        // individual
        if (dados.isDouble() || passoEscolhido != dados.somaAtual()) {
            if (mov.moverIndividual(jogador, origemSelecionada, index, passoEscolhido)) {
                dados.consumirIndividual(passoEscolhido);
                posJogada("Movimento: C" + origemSelecionada + " → C" + index + " (passo " + passoEscolhido + ")");
            } else {
                view.mostrarMensagem("Movimento inválido.");
            }
            return;
        }

        // longa (não-double) com intermediário
        int dA = dados.snapshotPassos().get(0);
        int dB = dados.snapshotPassos().get(1);
        if (dA + dB != passoEscolhido) { int t = dA; dA = dB; dB = t; } // robustez

        if (mov.moverLongaComIntermediario(jogador, origemSelecionada, index, dA, dB)) {
            dados.consumirSoma();
            posJogada("Movimento longo: C" + origemSelecionada + " → C" + index + " (ordem " + dA + " + " + dB + ")");
        } else if (mov.moverLongaComIntermediario(jogador, origemSelecionada, index, dB, dA)) {
            dados.consumirSoma();
            posJogada("Movimento longo: C" + origemSelecionada + " → C" + index + " (ordem " + dB + " + " + dA + ")");
        } else {
            view.mostrarMensagem("Jogada longa inválida (intermediário bloqueado).");
        }
    }

    private void posJogada(String msg) {
        view.mostrarMensagem(msg);
        origemSelecionada = -1;
        destaque.limpar();
        atualizarView();
        atualizarEstadoPontuar();

        if (dados.temPassos() && haAlgumMovimentoPossivel()) {
            view.mostrarMensagem("Ainda há movimentos disponíveis. Continue jogando.");
            view.setBotaoAtivo(false);
        } else {
            finalizarTurno();
        }
        turno.checarVencedor();
    }

    private boolean haAlgumMovimentoPossivel() {
        int jogador = turno.getJogadorAtual();

        // individuais
        for (int casa = 0; casa < tabuleiro.getNumCasas(); casa++) {
            if (tabuleiro.getDonoCasa(casa) != jogador || tabuleiro.getNumPecas(casa) == 0) continue;
            int origemLog = tabuleiro.posicaoLogica(jogador, casa);
            for (int p : dados.individuaisDisponiveis()) {
                int destinoLog = origemLog + p;
                int destinoFis = mov.encontrarCasaPorPosicaoLogica(jogador, destinoLog);
                int destinoValid = (destinoFis == -1) ? ((jogador == 1) ? 24 : -1) : destinoFis;
                Movimento mv = new Movimento(casa, destinoValid);
                if (tabuleiro.movimentoValido(mv, jogador, p)) return true;
            }
        }

        // longa não-double (se houver 2 dados remanescentes)
        if (!dados.isDouble() && dados.podeSoma()) {
            int dA = dados.snapshotPassos().get(0), dB = dados.snapshotPassos().get(1);
            for (int casa = 0; casa < tabuleiro.getNumCasas(); casa++) {
                if (tabuleiro.getDonoCasa(casa) != jogador || tabuleiro.getNumPecas(casa) == 0) continue;
                int origemLog = tabuleiro.posicaoLogica(jogador, casa);
                int interLogA = origemLog + dA;
                int interFisA = mov.encontrarCasaPorPosicaoLogica(jogador, interLogA);
                if (interFisA != -1) {
                    Movimento m1 = new Movimento(casa, interFisA);
                    if (tabuleiro.movimentoValido(m1, jogador, dA)) {
                        int destLogA = interLogA + dB;
                        int destFisA = mov.encontrarCasaPorPosicaoLogica(jogador, destLogA);
                        if (destFisA != -1) {
                            Movimento m2 = new Movimento(interFisA, destFisA);
                            if (tabuleiro.movimentoValido(m2, jogador, dB)) return true;
                        }
                    }
                }
                int interLogB = origemLog + dB;
                int interFisB = mov.encontrarCasaPorPosicaoLogica(jogador, interLogB);
                if (interFisB != -1) {
                    Movimento m1b = new Movimento(casa, interFisB);
                    if (tabuleiro.movimentoValido(m1b, jogador, dB)) {
                        int destLogB = interLogB + dA;
                        int destFisB = mov.encontrarCasaPorPosicaoLogica(jogador, destLogB);
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

    private void atualizarEstadoPontuar() {
        view.setPontuarAtivo(false);
        if (barra.temPecaNaBarra(turno.getJogadorAtual())) return;
        if (origemSelecionada == -1) return;
        if (!tabuleiro.todasPecasNoQuadranteFinal(turno.getJogadorAtual())) return;

        int jogador = turno.getJogadorAtual();
        int origemLog = tabuleiro.posicaoLogica(jogador, origemSelecionada);
        for (int p : dados.individuaisDisponiveis()) {
            if (origemLog + p >= 24) {
                Movimento mv = new Movimento(origemSelecionada, (jogador == 1) ? 24 : -1);
                if (tabuleiro.movimentoValido(mv, jogador, p)) {
                    view.setPontuarAtivo(true);
                    return;
                }
            }
        }
    }

    private void finalizarTurno() {
        view.setPontuarAtivo(false);
        view.setBotaoAtivo(true);
        turno.proximoTurno();
        if (barra.temPecaNaBarra(turno.getJogadorAtual())) {
            origemSelecionada = ORIGEM_BARRA;
            destaque.destacarEntradasPossiveis(turno.getJogadorAtual());
        } else {
            origemSelecionada = -1;
        }
    }
}
