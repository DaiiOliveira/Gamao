package projetogamao.Controller;

import projetogamao.Model.Movimento;
import projetogamao.Model.Tabuleiro;

public class MovementController {
    private final Tabuleiro board;
    
    public MovementController(Tabuleiro board) {
        this.board = board;
    }
    
    public boolean moverIndividual(int player, int origemFis, int destinoFis, int passo) {
        Movimento mv = new Movimento(origemFis, destinoFis);
        if (!board.movimentoValido(mv, player, passo)) return false;
        
        board.aplicarMovimento(mv, player);
        return true;
    }
    
    public boolean moverLongaComIntermediario(int jogador, int origemFis, int destinoFis, int d1, int d2) {
         int origemLog = board.posicaoLogica(jogador, origemFis);
        int interLog = origemLog + d1;
        int interFis = encontrarCasaPorPosicaoLogica(jogador, interLog);
        if (interFis == -1) return false;

        Movimento mv1 = new Movimento(origemFis, interFis);
        if (!board.movimentoValido(mv1, jogador, d1)) return false;

        int destLogEsperado = board.posicaoLogica(jogador, destinoFis);
        if (destLogEsperado != interLog + d2) return false;

        Movimento mv2 = new Movimento(interFis, destinoFis);
        if (!board.movimentoValido(mv2, jogador, d2)) return false;

        board.aplicarMovimento(mv1, jogador);
        board.aplicarMovimento(mv2, jogador);
        return true;
    }

    public int encontrarCasaPorPosicaoLogica(int jogador, int posLog) {
        if (posLog < 0 || posLog > 23) return -1;
        for (int i = 0; i < board.getNumCasas(); i++) {
            if (board.posicaoLogica(jogador, i) == posLog) return i;
        }
        return -1;
    }
}
