package projetogamao.Model;

public class Tabuleiro {
    private final int[][] casas; // [24][2] -> [quantidade, dono]
    private int pontuacaoJogador1 = 0;
    private int pontuacaoJogador2 = 0;

    private int barJogador1 = 0;
    private int barJogador2 = 0;

    public Tabuleiro() {
        casas = new int[24][2];
    }

    public int getNumCasas() { return casas.length; }
    public int getNumPecas(int index) { return casas[index][0]; }
    public int getDonoCasa(int index) { return casas[index][1]; }
    public int getPontuacao(int jogador) { return jogador == 1 ? pontuacaoJogador1 : pontuacaoJogador2; }
    public int getBar(int jogador) { return jogador == 1 ? barJogador1 : barJogador2; }

    public void adicionarPecas(int index, int quantidade, int jogador) {
        casas[index][0] += quantidade;
        casas[index][1] = jogador;
    }

    // ===== Trajeto lógico (0..23) =====
    public int posicaoLogica(int jogador, int index) {
        if (jogador == 1) {
            if (index <= 11) return 11 - index;   // topo: direita -> esquerda
            return index;                          // baixo: esquerda -> direita
        } else { // jogador 2 (oposto do 1)
            if (index >= 12) return 23 - index;   // baixo: direita -> esquerda
            return 12 + index;                    // topo: esquerda -> direita
        }
    }

    // ===== Quadrante final (18..23) =====
    public boolean todasPecasNoQuadranteFinal(int jogador) {
        boolean temAlguma = false;
        for (int i = 0; i < 24; i++) {
            if (casas[i][1] == jogador && casas[i][0] > 0) {
                temAlguma = true;
                int pl = posicaoLogica(jogador, i);
                if (pl < 18) return false;
            }
        }
        return temAlguma; // true se há peças e todas estão em 18..23
    }

    // ===== Entrada pela barra =====

    // Retorna o índice físico da casa correspondente ao dado (1..6) para este jogador
    // Entrada usa SEMPRE dado individual: entra no ponto lógico (valorDado-1)
    public int entradaDestinoPorDado(int jogador, int valorDado) {
        int alvoLogico = (valorDado - 1);
        if (alvoLogico < 0 || alvoLogico > 5) return -1;
        for (int idx = 0; idx < 24; idx++) {
            if (posicaoLogica(jogador, idx) == alvoLogico) return idx;
        }
        return -1;
    }

    // Casa aberta para entrar (vazia, sua, ou 1 inimiga)
    public boolean casaAbertaParaJogador(int idx, int jogador) {
        int qtd = casas[idx][0];
        int dono = casas[idx][1];
        return (qtd == 0) || (dono == jogador) || (dono != 0 && dono != jogador && qtd == 1);
    }

    // Pode entrar da barra com este dado?
    public boolean podeEntrarDaBarraCom(int jogador, int valorDado) {
        if (valorDado < 1 || valorDado > 6) return false;
        if (getBar(jogador) <= 0) return false;
        int dest = entradaDestinoPorDado(jogador, valorDado);
        if (dest < 0) return false;
        return casaAbertaParaJogador(dest, jogador);
    }

    // Aplica a entrada da barra; retorna true se "bateu" e mandou inimiga para a barra
    public boolean entrarDaBarra(int jogador, int valorDado) {
        if (!podeEntrarDaBarraCom(jogador, valorDado)) return false;

        int dest = entradaDestinoPorDado(jogador, valorDado);
        int dono = casas[dest][1];
        int qtd = casas[dest][0];

        // "bater" inimiga?
        if (qtd == 1 && dono != 0 && dono != jogador) {
            if (dono == 1) barJogador1++;
            else barJogador2++;
            casas[dest][0] = 0;
            casas[dest][1] = 0;
        }

        // decrementa barra do jogador
        if (jogador == 1) barJogador1--;
        else barJogador2--;

        // coloca a peça
        casas[dest][0]++;
        casas[dest][1] = jogador;
        return true;
    }

    // ===== Validação/aplicação de movimento normal & bear-off =====

    public boolean movimentoValido(Movimento movimento, int jogador, int valorDado) {
        int origem = movimento.getOrigem();
        int destino = movimento.getDestino();

        if (origem < 0 || origem >= 24) return false; // NÃO trata barra aqui (barra é via entrarDaBarra)
        if (casas[origem][1] != jogador || casas[origem][0] == 0) return false;

        int origemLog = posicaoLogica(jogador, origem);

        // Bear off com destino sentinela (>=24 p/ J1, <0 p/ J2)
        boolean isBearOff = (jogador == 1 && destino >= 24) || (jogador == 2 && destino < 0);
        if (isBearOff) {
            if (!todasPecasNoQuadranteFinal(jogador)) return false;
            return (origemLog + valorDado) >= 24;
        }

        if (destino < 0 || destino >= 24) return false;

        int destinoLog = posicaoLogica(jogador, destino);
        if (destinoLog - origemLog != valorDado) return false;

        // Ocupação: livre, próprio, ou 1 inimiga
        int qtd = casas[destino][0];
        int dono = casas[destino][1];
        if (dono != 0 && dono != jogador && qtd >= 2) return false; // bloqueado por torre inimiga

        return true;
    }

    // Aplica o movimento; retorna true se foi Bear Off
    public boolean aplicarMovimento(Movimento movimento, int jogador) {
        int origem = movimento.getOrigem();
        int destino = movimento.getDestino();

        // Bear off
        if ((jogador == 1 && destino >= 24) || (jogador == 2 && destino < 0)) {
            casas[origem][0]--;
            if (casas[origem][0] == 0) casas[origem][1] = 0;
            if (jogador == 1) pontuacaoJogador1++; else pontuacaoJogador2++;
            return true;
        }

        // Comer peça inimiga (exatamente 1)
        int donoDestino = casas[destino][1];
        int qtdDestino = casas[destino][0];
        if (qtdDestino == 1 && donoDestino != 0 && donoDestino != jogador) {
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

    // ===== Suporte ao “fim de jogo” dinâmico =====
    public int contarPecasNoTabuleiro(int jogador) {
        int total = 0;
        for (int i = 0; i < 24; i++) {
            if (casas[i][1] == jogador) total += casas[i][0];
        }
        return total;
    }
}
