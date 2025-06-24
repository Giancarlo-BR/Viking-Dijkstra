
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

public class PainelMapa extends JPanel {
    private char[][] grade;
    private int[][] posicoesPortos;
    private List<Point> pontosCaminho;
    private int tamanhoCelula;
    private List<Integer> portosInacessiveis;

    public PainelMapa(char[][] grade, int[][] posicoesPortos, List<Point> pontosCaminho, List<Integer> portosInacessiveis) {
        this.grade = grade;
        this.posicoesPortos = posicoesPortos;
        this.pontosCaminho = pontosCaminho;
        this.portosInacessiveis = portosInacessiveis;

        int linhas = grade.length;
        int colunas = (grade.length > 0 ? grade[0].length : 0);

        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        int larguraMaxima = tela.width - 50;
        int alturaMaxima = tela.height - 100;

        int tamanhoLargura = (colunas > 0 ? larguraMaxima / colunas : 1);
        int tamanhoAltura = (linhas > 0 ? alturaMaxima / linhas : 1);
        this.tamanhoCelula = Math.max(1, Math.min(tamanhoLargura, tamanhoAltura));
    }

    @Override
    public Dimension getPreferredSize() {
        int linhas = grade.length;
        int colunas = (grade.length > 0 ? grade[0].length : 0);
        return new Dimension(colunas * tamanhoCelula, linhas * tamanhoCelula);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int linhas = grade.length;
        int colunas = (grade.length > 0 ? grade[0].length : 0);


        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                char cel = grade[i][j];
                if (cel == '.') {
                    g2d.setColor(new Color(0, 255, 255));
                } else if (cel == ' ') {
                    g2d.setColor(new Color(144, 238, 144));
                } else if (cel == '*') {
                    g2d.setColor(Color.GREEN);
                } else if (cel >= '1' && cel <= '9') {
                    if (portosInacessiveis.contains(cel - '0')) {
                        g2d.setColor(Color.GRAY);
                    } else {
                        g2d.setColor(new Color(144, 238, 144));
                    }
                } else {
                    g2d.setColor(Color.LIGHT_GRAY);
                }
                g2d.fillRect(j * tamanhoCelula, i * tamanhoCelula, tamanhoCelula, tamanhoCelula);
            }
        }


        for (int i = 1; i <= 9; i++) {
            if (posicoesPortos[i][0] != -1) {
                int px = posicoesPortos[i][0];
                int py = posicoesPortos[i][1];
                
                if (portosInacessiveis.contains(i)) {
                    g2d.setColor(Color.GRAY);
                } else {
                    g2d.setColor(Color.RED);
                }
                g2d.fillRect(py * tamanhoCelula, px * tamanhoCelula, tamanhoCelula, tamanhoCelula);

                if (tamanhoCelula > 10) {
                    g2d.setColor(Color.WHITE);
                    int fonte = Math.max(10, tamanhoCelula);
                    g2d.setFont(new Font("Arial", Font.BOLD, fonte));
                    String texto = String.valueOf(i);
                    FontMetrics fm = g2d.getFontMetrics();
                    int larguraTexto = fm.stringWidth(texto);
                    int alturaTexto = fm.getHeight();
                    g2d.drawString(
                            texto,
                            py * tamanhoCelula + tamanhoCelula / 2 - larguraTexto / 2,
                            px * tamanhoCelula + tamanhoCelula / 2 + alturaTexto / 3
                    );
                }
            }
        }


        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(Math.max(1.0f, tamanhoCelula / 10.0f)));
        if (pontosCaminho != null && pontosCaminho.size() > 1) {
            for (int i = 0; i < pontosCaminho.size() - 1; i++) {
                Point p1 = pontosCaminho.get(i);
                Point p2 = pontosCaminho.get(i + 1);

                int x1 = p1.y * tamanhoCelula + tamanhoCelula / 2;
                int y1 = p1.x * tamanhoCelula + tamanhoCelula / 2;
                int x2 = p2.y * tamanhoCelula + tamanhoCelula / 2;
                int y2 = p2.x * tamanhoCelula + tamanhoCelula / 2;

                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }
    }
}
