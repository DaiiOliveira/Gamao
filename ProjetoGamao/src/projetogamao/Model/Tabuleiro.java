package projetogamao.Model;

public class Tabuleiro {
    private int[][] casas; // [numCasas][2] → [quantidade, dono]
    private int pontuacaoJogador1 = 0;
    private int pontuacaoJogador2 = 0;

    // Peças comidas
    private int barJogador1 = 0;
    private int barJogador2 = 0;

    public Tabuleiro() {
        casas = new int[24][2]; // 24 casas
    }

    public int getNumCasas() { return casas.length; }
    public int getNumPecas(int index) { return casas[index][0]; }
    public int getDonoCasa(int index) { return casas[index][1]; }

    public void adicionarPecas(int index, int quantidade, int jogador) {
        casas[index][0] += quantidade;
        casas[index][1] = jogador;
    }

    public int getBar(int jogador) {
        return jogador == 1 ? barJogador1 : barJogador2;
    }

    // Trajeto correto
    public int posicaoLogica(int jogador, int index) {
        if (jogador == 1) {
            if (index <= 11) return 11 - index;   // superior: direita → esquerda
            else return index;                    // inferior: esquerda → direita
        } else { // jogador 2
            if (index >= 12) return 23 - index;  // inferior: direita → esquerda
            else return 12 + index;              // superior: esquerda → direita
        }
    }

    public boolean todasPecasNoQuadranteFinal(int jogador) {
        if (jogador == 1) {
            for (int i = 0; i < 18; i++) if (casas[i][1] == 1) return false;
        } else {
            for (int i = 6; i < 24; i++) if (casas[i][1] == 2) return false;
        }
        return true;
    }

    public boolean movimentoValido(Movimento movimento, int jogador, int valorDado) {
        int origem = movimento.getOrigem();
        int destino = movimento.getDestino();

        // Bear Off
        if ((jogador == 1 && destino >= casas.length) || (jogador == 2 && destino < 0)) {
            return todasPecasNoQuadranteFinal(jogador);
        }

        if (origem < 0 || origem >= casas.length) return false;
        if (destino < 0 || destino >= casas.length) return false;
        if (casas[origem][1] != jogador || casas[origem][0] == 0) return false;

        int origemLogica = posicaoLogica(jogador, origem);
        int destinoLogica = posicaoLogica(jogador, destino);

        if (destinoLogica - origemLogica != valorDado) return false;

        return true;
    }

    public boolean aplicarMovimento(Movimento movimento, int jogador) {
        int origem = movimento.getOrigem();
        int destino = movimento.getDestino();

        // Bear Off
        if ((jogador == 1 && destino >= casas.length) || (jogador == 2 && destino < 0)) {
            casas[origem][0]--;
            if (casas[origem][0] == 0) casas[origem][1] = 0;
            if (jogador == 1) pontuacaoJogador1++;
            else pontuacaoJogador2++;
            return true;
        }

        // Comer peça inimiga
        int donoDestino = casas[destino][1];
        int qtdDestino = casas[destino][0];
        if (qtdDestino == 1 && donoDestino != jogador && donoDestino != 0) {
            // Manda a peça para a barra do dono
            if (donoDestino == 1) barJogador1++;
            else barJogador2++;

            casas[destino][0] = 0;
            casas[destino][1] = 0;
        }

        // Mover peça
        casas[origem][0]--;
        if (casas[origem][0] == 0) casas[origem][1] = 0;
        casas[destino][0]++;
        casas[destino][1] = jogador;

        return false;
    }

    public int getPontuacao(int jogador) {
        return jogador == 1 ? pontuacaoJogador1 : pontuacaoJogador2;
    }
}
