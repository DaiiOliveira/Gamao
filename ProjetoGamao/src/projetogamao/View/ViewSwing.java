package projetogamao.View;

import javax.swing.*;
import java.awt.*;

public class ViewSwing extends JFrame implements View {
    private CasaView[] casas;
    private JTextArea log;
    private JButton botaoRolarDados;

    private JPanel painelBar1;
    private JPanel painelBar2;

    public ViewSwing(int numCasas) {
        setTitle("Gamão - MVC");
        setSize(1300, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        casas = new CasaView[numCasas];

        JPanel painelTabuleiro = new JPanel(new GridLayout(2, numCasas / 2));
        for (int i = 0; i < numCasas; i++) {
            casas[i] = new CasaView(i);
            painelTabuleiro.add(casas[i]);
        }

        // Barras de peças comidas
        painelBar1 = new JPanel();
        painelBar1.setBorder(BorderFactory.createTitledBorder("Barra Jogador 1"));
        painelBar1.setPreferredSize(new Dimension(100, 600));
        painelBar1.setLayout(new GridLayout(0,1));

        painelBar2 = new JPanel();
        painelBar2.setBorder(BorderFactory.createTitledBorder("Barra Jogador 2"));
        painelBar2.setPreferredSize(new Dimension(100, 600));
        painelBar2.setLayout(new GridLayout(0,1));

        log = new JTextArea(5, 20);
        log.setEditable(false);
        JScrollPane scroll = new JScrollPane(log);

        botaoRolarDados = new JButton("Rolar Dados");

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(painelBar1, BorderLayout.WEST);
        centro.add(painelTabuleiro, BorderLayout.CENTER);
        centro.add(painelBar2, BorderLayout.EAST);

        add(centro, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
        add(botaoRolarDados, BorderLayout.NORTH);

        setVisible(true);
    }

    @Override
    public void mostrarMensagem(String mensagem) { log.append(mensagem + "\n"); }

    @Override
    public void mostrarTabuleiro(String tabuleiroStr) { log.append(tabuleiroStr + "\n"); }

    @Override
    public String lerEntrada() { return null; }

    public void addAcaoCasa(int index, Runnable acao) { casas[index].setOnClick(acao); }

    public void addAcaoRolarDados(java.awt.event.ActionListener listener) { botaoRolarDados.addActionListener(listener); }

    public void atualizarCasa(int index, int numPecas, boolean jogador1) {
        casas[index].setPecas(numPecas, jogador1 ? Color.RED : Color.BLUE);
        casas[index].setBackground(null);
    }

    public void atualizarCorFundoCasa(int index, Color cor) {
        casas[index].setBackground(cor);
        casas[index].repaint();
    }

    public void destacarCasa(int index, boolean destaque) {
        casas[index].setBackground(destaque ? Color.YELLOW : null);
        casas[index].repaint();
    }

    public void setBotaoAtivo(boolean ativo) { botaoRolarDados.setEnabled(ativo); }

    public void atualizarBearOff(int jogador, int pontuacao) {
        if (jogador == 1) painelBar1.removeAll();
        else painelBar2.removeAll();

        JPanel painel = jogador == 1 ? painelBar1 : painelBar2;
        for (int i = 0; i < pontuacao; i++) {
            JLabel peca = new JLabel("●");
            peca.setForeground(jogador == 1 ? Color.RED : Color.BLUE);
            painel.add(peca);
        }
        painel.revalidate();
        painel.repaint();
    }

    public void atualizarBarra(int jogador, int quantidade) {
        JPanel painel = jogador == 1 ? painelBar1 : painelBar2;
        painel.removeAll();

        CasaView casaBar = new CasaView(-1); // índice -1 para barras
        casaBar.setPreferredSize(new Dimension(60, 200));
        casaBar.setPecas(quantidade, jogador == 1 ? Color.RED : Color.BLUE);

        painel.add(casaBar);

        painel.revalidate();
        painel.repaint();
    }

}
