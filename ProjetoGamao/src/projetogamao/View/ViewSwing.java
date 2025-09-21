package projetogamao.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ViewSwing extends JFrame implements View {
    private CasaView[] casas;
    private JTextArea log;
    private JButton botaoRolarDados;

    private JPanel painelBar1Container, painelBar2Container;
    private JPanel painelBar1, painelBar2;

    private JLabel placarValor;
    private JButton botaoPontuar;

    public ViewSwing(int numCasas) {
        setTitle("Gamão - MVC");
        setSize(1400, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Centro (barras + tabuleiro)
        casas = new CasaView[numCasas];
        JPanel painelTabuleiro = new JPanel(new GridLayout(2, numCasas / 2));
        for (int i = 0; i < numCasas; i++) {
            casas[i] = new CasaView(i);
            painelTabuleiro.add(casas[i]);
        }

        painelBar1 = new JPanel(new GridBagLayout());
        painelBar2 = new JPanel(new GridBagLayout());

        painelBar1Container = new JPanel(new BorderLayout());
        painelBar1Container.setBorder(BorderFactory.createTitledBorder("Barra Jogador 1"));
        painelBar1Container.setPreferredSize(new Dimension(180, 620));
        painelBar1Container.add(new JScrollPane(painelBar1,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        painelBar2Container = new JPanel(new BorderLayout());
        painelBar2Container.setBorder(BorderFactory.createTitledBorder("Barra Jogador 2"));
        painelBar2Container.setPreferredSize(new Dimension(180, 620));
        painelBar2Container.add(new JScrollPane(painelBar2,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(painelBar1Container, BorderLayout.WEST);
        centro.add(painelTabuleiro, BorderLayout.CENTER);
        centro.add(painelBar2Container, BorderLayout.EAST);

        // Topo
        botaoRolarDados = new JButton("Rolar Dados");

        // Rodapé (log à esquerda, placar + pontuar à direita)
        log = new JTextArea(8, 20);
        log.setEditable(false);
        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBorder(BorderFactory.createTitledBorder("Jogadas"));
        painelLog.add(new JScrollPane(log), BorderLayout.CENTER);

        JPanel painelPlacar = new JPanel(new GridBagLayout());
        painelPlacar.setBorder(BorderFactory.createTitledBorder("Placar (Peças Pontuadas)"));
        JLabel placarTitulo = new JLabel("Bear-Off");
        placarTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));

        placarValor = new JLabel("0  x  0");
        placarValor.setFont(new Font("SansSerif", Font.BOLD, 36));

        botaoPontuar = new JButton("Pontuar");
        botaoPontuar.setEnabled(false); // sempre visível; só habilita quando possível

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.gridx = 0; gbc.gridy = 0; painelPlacar.add(placarTitulo, gbc);
        gbc.gridy = 1; painelPlacar.add(placarValor, gbc);
        gbc.gridy = 2; painelPlacar.add(botaoPontuar, gbc);

        JPanel rodape = new JPanel(new GridLayout(1, 2));
        rodape.add(painelLog);
        rodape.add(painelPlacar);

        add(centro, BorderLayout.CENTER);
        add(botaoRolarDados, BorderLayout.NORTH);
        add(rodape, BorderLayout.SOUTH);

        setVisible(true);
    }

    // View interface
    @Override public void mostrarMensagem(String mensagem) {
        log.append(mensagem + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }
    @Override public void mostrarTabuleiro(String t) { mostrarMensagem("Tabuleiro:\n" + t); }
    @Override public String lerEntrada() { return null; }

    // Hooks
    public void addAcaoCasa(int index, Runnable acao) { casas[index].setOnClick(acao); }
    public void addAcaoRolarDados(ActionListener l) { botaoRolarDados.addActionListener(l); }
    public void addAcaoPontuar(ActionListener l) { botaoPontuar.addActionListener(l); }

    public void setBotaoAtivo(boolean ativo) { botaoRolarDados.setEnabled(ativo); }
    public void setPontuarAtivo(boolean ativo) { botaoPontuar.setEnabled(ativo); }

    // Tabuleiro
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

    // Barras
    public void atualizarBarra(int jogador, int quantidade) {
        JPanel painel = (jogador == 1) ? painelBar1 : painelBar2;
        painel.removeAll();
        CasaView casaBar = new CasaView(-1);
        casaBar.setPreferredSize(new Dimension(120, 260));
        casaBar.setPecas(quantidade, jogador == 1 ? Color.RED : Color.BLUE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        painel.add(casaBar, gbc);
        painel.revalidate();
        painel.repaint();
    }

    // Placar
    public void atualizarPlacar(int j1, int j2) {
        placarValor.setText(j1 + "  x  " + j2);
        placarValor.repaint();
    }

    // Tela do vencedor
    public void mostrarTelaVencedor(String vencedor, int pontJ1, int pontJ2, Runnable onJogarNovamente) {
        JDialog dialog = new JDialog(this, "Fim de Jogo", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new GridBagLayout());
        JLabel titulo = new JLabel("Vencedor: " + vencedor);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel placar = new JLabel("Placar final: " + pontJ1 + "  x  " + pontJ2);
        placar.setFont(new Font("SansSerif", Font.PLAIN, 18));
        JButton jogarNovamente = new JButton("Jogar novamente");
        jogarNovamente.addActionListener(e -> {
            dialog.dispose();
            if (onJogarNovamente != null) onJogarNovamente.run();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0; root.add(titulo, gbc);
        gbc.gridy = 1; root.add(placar, gbc);
        gbc.gridy = 2; root.add(jogarNovamente, gbc);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // Utilitário opcional
    public void limparLog() { log.setText(""); }
}
