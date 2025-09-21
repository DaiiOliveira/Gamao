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

    public int getNumCasas() {
        return casas.length;
    }

    public int getNumPecas(int index) {
        return casas[index][0];
    }

    public int getDonoCasa(int index) {
        return casas[index][1];
    }

    public int getPontuacao(int jogador) {
        return jogador == 1 ? pontuacaoJogador1 : pontuacaoJogador2;
    }

    public int getBar(int jogador) {
        return jogador == 1 ? barJogador1 : barJogador2;
    }

    public void adicionarPecas(int index, int quantidade, int jogador) {
        casas[index][0] += quantidade;
        casas[index][1] = jogador;
    }

    public int contarPecasNoTabuleiro(int jogador) {
        int total = 0;
        for (int i = 0; i < 24; i++) {
            if (casas[i][1] == jogador) {
                total += casas[i][0];
            }
        }
        return total;
    }

    // Mapeamento do trajeto lógico (0..23)
    public int posicaoLogica(int jogador, int index) {
        if (jogador == 1) {
            if (index <= 11) {
                return 11 - index;   // topo: direita -> esquerda
            }
            return index;                          // baixo: esquerda -> direita
        } else { // jogador 2 (oposto do 1)
            if (index >= 12) {
                return 23 - index;   // baixo: direita -> esquerda
            }
            return 12 + index;                    // topo: esquerda -> direita
        }
    }

    // Todas as peças do jogador no quadrante final lógico [18..23]
    public boolean todasPecasNoQuadranteFinal(int jogador) {
        boolean temAlguma = false;
        for (int i = 0; i < 24; i++) {
            if (casas[i][1] == jogador && casas[i][0] > 0) {
                temAlguma = true;
                int pl = posicaoLogica(jogador, i);
                if (pl < 18) {
                    return false;
                }
            }
        }
        return temAlguma; // true se ainda há peças e todas estão 18..23
    }

    // Validação de movimento (curto/longa e bear off)
    public boolean movimentoValido(Movimento movimento, int jogador, int valorDado) {
        int origem = movimento.getOrigem();
        int destino = movimento.getDestino();

        if (origem < 0 || origem >= 24) {
            return false;
        }
        if (casas[origem][1] != jogador || casas[origem][0] == 0) {
            return false;
        }

        int origemLog = posicaoLogica(jogador, origem);

        // Bear off com destino sentinela (>=24 p/ J1, <0 p/ J2)
        boolean isBearOff = (jogador == 1 && destino >= 24) || (jogador == 2 && destino < 0);
        if (isBearOff) {
            if (!todasPecasNoQuadranteFinal(jogador)) {
                return false;
            }
            // pode tirar se alcançar ou passar do fim lógico
            return (origemLog + valorDado) >= 24;
        }

        if (destino < 0 || destino >= 24) {
            return false;
        }

        int destinoLog = posicaoLogica(jogador, destino);
        if (destinoLog - origemLog != valorDado) {
            return false;
        }

        // Permitido mover para casa vazia, própria, ou com 1 inimiga (hit)
        return true;
    }

    // Aplica movimento; retorna true se foi Bear Off
    public boolean aplicarMovimento(Movimento movimento, int jogador) {
        int origem = movimento.getOrigem();
        int destino = movimento.getDestino();

        // Bear off
        if ((jogador == 1 && destino >= 24) || (jogador == 2 && destino < 0)) {
            casas[origem][0]--;
            if (casas[origem][0] == 0) {
                casas[origem][1] = 0;
            }
            if (jogador == 1) {
                pontuacaoJogador1++;
            } else {
                pontuacaoJogador2++;
            }
            return true;
        }

        // Comer peça inimiga (exatamente 1)
        int donoDestino = casas[destino][1];
        int qtdDestino = casas[destino][0];
        if (qtdDestino == 1 && donoDestino != 0 && donoDestino != jogador) {
            if (donoDestino == 1) {
                barJogador1++;
            } else {
                barJogador2++;
            }
            casas[destino][0] = 0;
            casas[destino][1] = 0;
        }

        // Mover peça
        casas[origem][0]--;
        if (casas[origem][0] == 0) {
            casas[origem][1] = 0;
        }
        casas[destino][0]++;
        casas[destino][1] = jogador;
        return false;
    }
}
