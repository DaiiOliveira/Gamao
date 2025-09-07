package projetogamao.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CasaView extends JPanel {
    private int numPecas;
    private Color corPeca;
    private int index;
    private Runnable onClick;

    public CasaView(int index) {
        this.index = index;
        this.numPecas = 0;
        this.corPeca = Color.GRAY;

        setPreferredSize(new Dimension(60, 200));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });
    }

    public void setOnClick(Runnable onClick) { this.onClick = onClick; }

    public void setPecas(int num, Color cor) {
        this.numPecas = num;
        this.corPeca = cor;
        repaint();
    }

    public void setNumPecas(int num) {
        this.numPecas = num;
        repaint();
    }

    public void setCorPeca(Color cor) {
        this.corPeca = cor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        int diametro = 40;
        for (int i = 0; i < numPecas; i++) {
            g.setColor(corPeca);
            int y = getHeight() - (i + 1) * (diametro + 5);
            g.fillOval(10, y, diametro, diametro);
            g.setColor(Color.BLACK);
            g.drawOval(10, y, diametro, diametro);
        }

        g.setColor(Color.BLACK);
        g.drawString("C" + index, 10, 15);
    }
}
