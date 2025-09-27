package projetogamao.Controller;

import projetogamao.Model.Tabuleiro;


public class EntradaBarraController {
    private final Tabuleiro tabuleiro;
    private final DiceController dados;

    public EntradaBarraController(Tabuleiro tabuleiro, DiceController dados) {
        this.tabuleiro = tabuleiro;
        this.dados = dados;
    }

    public boolean temPecaNaBarra(int jogador) { return tabuleiro.getBar(jogador) > 0; }

    // Checa se há pelo menos uma entrada possível com os dados individuais disponíveis
    public boolean haEntradaPossivel(int jogador) {
        for (int p : dados.individuaisDisponiveis()) {
            if (tabuleiro.podeEntrarDaBarraCom(jogador, p)) return true;
        }
        return false;
    }

    // Tenta entrar na casa clicada, retornando true se entrou e consumiu um dado
    public boolean tentarEntrarNaCasa(int jogador, int destinoFis) {
        int destinoLog = tabuleiro.posicaoLogica(jogador, destinoFis);
        Integer passoUsado = null;
        for (int p : dados.individuaisDisponiveis()) {
            int destEsperado = tabuleiro.entradaDestinoPorDado(jogador, p);
            if (destEsperado == destinoFis || destinoLog == (p - 1)) {
                passoUsado = p; break;
            }
        }
        if (passoUsado == null) return false;
        if (!tabuleiro.podeEntrarDaBarraCom(jogador, passoUsado)) return false;

        tabuleiro.entrarDaBarra(jogador, passoUsado);
        dados.consumirIndividual(passoUsado);
        return true;
    }
}
