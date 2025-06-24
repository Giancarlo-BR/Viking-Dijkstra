import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dimension;

public class BuscadorCaminhoViking {
    static final int[] dx = { -1, 1, 0, 0 };
    static final int[] dy = { 0, 0, +1, -1 };
    static final int INF = Integer.MAX_VALUE / 4;

    static class BFSHelper {
        boolean ehAlcancavel(int sx, int sy, int tx, int ty, char[][] grade, int linhas, int colunas) {
            boolean[][] visitado = new boolean[linhas][colunas];
            Deque<int[]> fila = new ArrayDeque<>();
            visitado[sx][sy] = true;
            fila.offer(new int[] { sx, sy });
            while (!fila.isEmpty()) {
                int[] p = fila.poll();
                int x = p[0], y = p[1];
                if (x == tx && y == ty) return true;
                for (int d = 0; d < 4; d++) {
                    int nx = x + dx[d], ny = y + dy[d];
                    if (nx < 0 || nx >= linhas || ny < 0 || ny >= colunas) continue;
                    if (visitado[nx][ny]) continue;
                    if (grade[nx][ny] == '*') continue;
                    visitado[nx][ny] = true;
                    fila.offer(new int[] { nx, ny });
                }
            }
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        long inicioTempo = System.currentTimeMillis();

        BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in));
        String linha;

        while ((linha = leitor.readLine()) != null) {
            linha = linha.trim();
            if (linha.isEmpty()) continue;

            String[] partes = linha.split("\\s+");
            if (partes.length < 2) continue;

            int linhas = Integer.parseInt(partes[0]);
            int colunas = Integer.parseInt(partes[1]);

            char[][] grade = new char[linhas][colunas];
            for (int i = 0; i < linhas; i++) {
                String row = leitor.readLine();
                if (row.length() < colunas) {
                    row = String.format("%-" + colunas + "s", row);
                }
                grade[i] = row.toCharArray();
            }

            int[][] posicoesPortos = new int[10][2];
            for (int i = 1; i <= 9; i++) {
                posicoesPortos[i][0] = -1;
                posicoesPortos[i][1] = -1;
            }
            for (int i = 0; i < linhas; i++) {
                for (int j = 0; j < colunas; j++) {
                    char ch = grade[i][j];
                    if (ch >= '1' && ch <= '9') {
                        int id = ch - '0';
                        posicoesPortos[id][0] = i;
                        posicoesPortos[id][1] = j;
                    }
                }
            }

            if (posicoesPortos[1][0] < 0) {
                System.out.println(0);
                exibirMapa(grade, posicoesPortos, new ArrayList<>(), 0, new ArrayList<>(), "Mapa Inicial - Porto 1 não encontrado");
                continue;
            }

            int custoTotal = 0;
            int atualX = posicoesPortos[1][0];
            int atualY = posicoesPortos[1][1];
            List<String> relatorio = new ArrayList<>();
            List<Integer> portosInacessiveis = new ArrayList<>();
            List<Point> pontosCaminhoTotal = new ArrayList<>();
            pontosCaminhoTotal.add(new Point(atualX, atualY));

            int ultimoPortoAcessivel = 1;
            BFSHelper bfsHelper = new BFSHelper();

            exibirMapa(grade, posicoesPortos, new ArrayList<>(pontosCaminhoTotal), 0, new ArrayList<>(),
                    "Mapa Inicial - Porto 1");

            for (int proxId = 2; proxId <= 9; proxId++) {
                int destinoX = posicoesPortos[proxId][0];
                int destinoY = posicoesPortos[proxId][1];

                if (destinoX < 0) {
                    relatorio.add("Porto " + proxId + ": INACESSÍVEL (não encontrado no mapa)");
                    portosInacessiveis.add(proxId);
                    continue;
                }

                if (!bfsHelper.ehAlcancavel(atualX, atualY, destinoX, destinoY, grade, linhas, colunas)) {
                    relatorio.add("Porto " + proxId + ": INACESSÍVEL");
                    portosInacessiveis.add(proxId);
                    continue;
                }

                List<Point> segmentoCaminho = new ArrayList<>();
                int custo = calcularCaminhoMinimo(grade, linhas, colunas, atualX, atualY, destinoX, destinoY, segmentoCaminho);
                custoTotal += custo;
                relatorio.add("Trecho " + ultimoPortoAcessivel + " -> " + proxId + " - Custo: " + custo);

                List<Point> caminhoTrecho = new ArrayList<>();
                caminhoTrecho.add(new Point(atualX, atualY));
                caminhoTrecho.addAll(segmentoCaminho);
                exibirMapa(grade, posicoesPortos, caminhoTrecho, custoTotal, portosInacessiveis,
                        "Trecho " + ultimoPortoAcessivel + " → " + proxId + " (Custo: " + custo + ")");

                atualX = destinoX;
                atualY = destinoY;
                ultimoPortoAcessivel = proxId;

                if (!segmentoCaminho.isEmpty()) {
                    if (!pontosCaminhoTotal.isEmpty() && pontosCaminhoTotal.get(pontosCaminhoTotal.size() - 1).equals(segmentoCaminho.get(0))) {
                        segmentoCaminho.remove(0);
                    }
                    pontosCaminhoTotal.addAll(segmentoCaminho);
                }
            }

            int retornoX = posicoesPortos[1][0];
            int retornoY = posicoesPortos[1][1];
            if (bfsHelper.ehAlcancavel(atualX, atualY, retornoX, retornoY, grade, linhas, colunas)) {
                List<Point> caminhoRetorno = new ArrayList<>();
                int custoRetorno = calcularCaminhoMinimo(grade, linhas, colunas, atualX, atualY, retornoX, retornoY, caminhoRetorno);
                custoTotal += custoRetorno;
                relatorio.add("Trecho " + ultimoPortoAcessivel + " -> Porto 1 (retorno) - Custo: " + custoRetorno);

                List<Point> caminhoTrechoRetorno = new ArrayList<>();
                caminhoTrechoRetorno.add(new Point(atualX, atualY));
                caminhoTrechoRetorno.addAll(caminhoRetorno);
                exibirMapa(grade, posicoesPortos, caminhoTrechoRetorno, custoTotal, portosInacessiveis,
                        "Trecho " + ultimoPortoAcessivel + " → Porto 1 (Retorno, Custo: " + custoRetorno + ")");

                if (!caminhoRetorno.isEmpty()) {
                    if (!pontosCaminhoTotal.isEmpty() && pontosCaminhoTotal.get(pontosCaminhoTotal.size() - 1).equals(caminhoRetorno.get(0))) {
                        caminhoRetorno.remove(0);
                    }
                    pontosCaminhoTotal.addAll(caminhoRetorno);
                }
            }

            System.out.println("\n--- RELATÓRIO DE NAVEGAÇÃO ---");
            System.out.println("Combustível total: " + custoTotal);
            System.out.println("\nDetalhes por trecho:");
            for (String linhaRelatorio : relatorio) {
                System.out.println(linhaRelatorio);
            }

            System.out.println("\nPortos inacessíveis:");
            if (portosInacessiveis.isEmpty()) {
                System.out.println("Todos os portos foram acessados com sucesso!");
            } else {
                for (int porto : portosInacessiveis) {
                    System.out.println("Porto " + porto);
                }
            }
            long fimTempo = System.currentTimeMillis();
            long tempoExecucao = fimTempo - inicioTempo;
            System.out.println("\nTempo de execução: " + tempoExecucao + " ms");

            System.out.println("\nCusto total final: " + custoTotal);

            exibirMapa(grade, posicoesPortos, pontosCaminhoTotal, custoTotal, portosInacessiveis,
                    "Mapa Final - Caminho Completo (Custo Total: " + custoTotal + ")");
        }


    }

    static int calcularCaminhoMinimo(
            char[][] grade,
            int linhas,
            int colunas,
            int sx,
            int sy,
            int tx,
            int ty,
            List<Point> caminho
    ) {
        int[][][] dist = new int[linhas][colunas][5];
        int[][][][] parent = new int[linhas][colunas][5][3];

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Arrays.fill(dist[i][j], INF);
            }
        }

        class Estado implements Comparable<Estado> {
            int x, y, dirPrev, custo;
            Estado(int x, int y, int dirPrev, int custo) {
                this.x = x;
                this.y = y;
                this.dirPrev = dirPrev;
                this.custo = custo;
            }
            @Override
            public int compareTo(Estado o) {
                return Integer.compare(this.custo, o.custo);
            }
        }

        PriorityQueue<Estado> fila = new PriorityQueue<>();
        dist[sx][sy][4] = 0;
        fila.offer(new Estado(sx, sy, 4, 0));

        int melhorCustoFinal = INF;
        int endX = -1, endY = -1, endDir = -1;

        while (!fila.isEmpty()) {
            Estado atual = fila.poll();
            if (atual.custo > melhorCustoFinal) continue;
            if (atual.custo > dist[atual.x][atual.y][atual.dirPrev]) continue;

            if (atual.x == tx && atual.y == ty) {
                if (atual.custo < melhorCustoFinal) {
                    melhorCustoFinal = atual.custo;
                    endX = atual.x;
                    endY = atual.y;
                    endDir = atual.dirPrev;
                }
            }

            for (int dir = 0; dir < 4; dir++) {
                int nx = atual.x + dx[dir];
                int ny = atual.y + dy[dir];
                if (nx < 0 || nx >= linhas || ny < 0 || ny >= colunas) continue;
                if (grade[nx][ny] == '*') continue;

                int custoMover = (atual.dirPrev == 4 || atual.dirPrev == dir) ? 1 : 3;
                int novoCusto = atual.custo + custoMover;

                if (novoCusto < dist[nx][ny][dir]) {
                    dist[nx][ny][dir] = novoCusto;
                    parent[nx][ny][dir][0] = atual.x;
                    parent[nx][ny][dir][1] = atual.y;
                    parent[nx][ny][dir][2] = atual.dirPrev;
                    fila.offer(new Estado(nx, ny, dir, novoCusto));
                }
            }
        }

        caminho.clear();
        if (melhorCustoFinal != INF && endX != -1) {
            LinkedList<Point> temp = new LinkedList<>();
            int cx = endX, cy = endY, cdir = endDir;
            while (cx != sx || cy != sy) {
                temp.addFirst(new Point(cx, cy));
                int px = parent[cx][cy][cdir][0];
                int py = parent[cx][cy][cdir][1];
                int pdir = parent[cx][cy][cdir][2];
                cx = px;
                cy = py;
                cdir = pdir;
            }
            temp.addFirst(new Point(sx, sy));
            caminho.addAll(temp);
        }

        return (melhorCustoFinal == INF ? INF : melhorCustoFinal);
    }

    private static void exibirMapa(
            char[][] grade,
            int[][] posicoesPortos,
            List<Point> pontosCaminho,
            int custoTotal,
            List<Integer> portosInacessiveis,
            String titulo
    ) {
        SwingUtilities.invokeLater(() -> {
            PainelMapa painel = new PainelMapa(grade, posicoesPortos, pontosCaminho, portosInacessiveis);
            JFrame frame = new JFrame("Viking - " + titulo + " - Custo: " + custoTotal);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JScrollPane scroll = new JScrollPane(painel);
            frame.getContentPane().add(scroll);

            Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
            int largura = Math.min(painel.getPreferredSize().width + 25, tela.width - 50);
            int altura = Math.min(painel.getPreferredSize().height + 40, tela.height - 100);
            frame.setSize(new Dimension(largura, altura));

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}