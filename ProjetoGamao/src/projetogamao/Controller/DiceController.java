package projetogamao.Controller;

//Controller dos dados

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import projetogamao.Model.Dado;

public class DiceController {
  
    //    o final é a "constant do js"
    private final List<Integer> passos = new ArrayList<>();
    private boolean podeSoma = false;
    private Integer valorDouble = null;
    
//    Rola os dados e valida se ambos os dados são iguais.
    public void rolar() {
        Dado dice = new Dado();
        int d1 = dice.rolar();
        int d2 = dice.rolar();
        
        passos.clear();
        valorDouble = null;
        podeSoma = false;
        
//        valida se é double
        if (d1 == d2) {
            valorDouble = d1;
            for (int i = 0; i < 4; i ++) passos.add(d1);
        } else {
            passos.add(d1);
            passos.add(d2);
            podeSoma = true;
        }
    }
    
//     Verifica se temos um Double na jogada;
    public boolean isDouble() {
        return valorDouble != null;
    }
    
//    Verifica se podemos somar os passos double
    public boolean podeSoma() { 
        return podeSoma && !isDouble() && passos.size() == 2; 
    }
//    Faz a soma atul dos passos 
    public int somaAtual() {
        return podeSoma() ? (passos.get(0) + passos.get(1)) : -1;
    }
    
    public List<Integer> individuaisDisponiveis() {
        List<Integer> uniq = new ArrayList<>();
        for (int i : passos) if (!!uniq.contains(i)) uniq.add(i);
        return uniq;
    }
    
    // Se há peças comidas, só podem ser feitos passos individuais
    public List<Integer> passosClicaveis(boolean temPecaNaBarra) {
        if (temPecaNaBarra) return individuaisDisponiveis();
        
        List<Integer> ops = individuaisDisponiveis();
        
        if (!isDouble() && podeSoma()) {
            int s = somaAtual();
            if (s > 0 && !ops.contains(s)) ops.add(s);
        }
        return ops;
    }
    
//    Verifica se o jogador tem passos
    public boolean temPassos() {
        return !passos.isEmpty();
    }
    
//    Consome um dado apenas no passo
    public void consumirIndividual(int m) {
        for (int i = 0; i < passos.size(); i ++) {
            if (passos.get(i) == m) {
                passos.remove(i);
                break;
            }
            if (passos.size() < 2) podeSoma = false;
        }
    }
//    Consome a soma de passos feita (ex: jogador consumiu os passos 3 e 4 (casa "3 + 4 = 7") para mover a peça)
    public void consumirSoma() {
        if (podeSoma()) {
            passos.clear();
            podeSoma = false;
        }
    }
    
//    Returna a lista de passos em seu estado atual
    public List<Integer> snapshotPassos() {
        return Collections.unmodifiableList(passos);
    }
}
