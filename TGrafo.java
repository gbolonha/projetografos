import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
//Gabriel Teixeira Bolonha (10426937)
//Geovana Bomfim Rodrigues (10410514)
//Rodrigo Daiske Uehara (10440295)
//Yating Zheng (10439511)

public class TGrafo {

    private int n;                 // quantidade de vértices
    private int m;                 // quantidade de arestas
    private float adj[][];         // matriz de adjacência (peso da aresta, ou infinito se não existe)

    private int rotulo[];          // rotulo[posicao]      = identificador do vértice
    private String apelido[];      // apelido[posicao]     = localidade/nome do vértice
    private float pesoVertice[];  // pesoVertice[posicao] = peso do vértice

    private int tipoGrafo;         // tipo do grafo (lido na 1 linha do arquivo txt)

    // Construtor: grafo vazio inicial
    public TGrafo() {
        this.n = 0;
        this.m = 0;
        this.adj = new float[0][0];
        this.rotulo = new int[0];
        this.apelido = new String[0];
        this.pesoVertice = new float[0];
        this.tipoGrafo = 3; // Valor padrão inicial caso nenhum arquivo tenha sido lido ainda
    }

    // Auxiliar interno: descobre a POSIÇÃO (0..n-1) do vértice pelo RÓTULO
    private int posicaoPorRotulo(int rot) {
        for (int i = 0; i < n; i++) {
            if (rotulo[i] == rot) return i;
        }
        return -1;
    }

    // Insere um novo vértice, isolado (sem arestas)
    public void insereVertice(int rot, String ape, float pesoVert) {
        if (posicaoPorRotulo(rot) != -1) {
            System.out.println("Erro: ja existe um vertice com rotulo " + rot + ".");
            return;
        }

        int novoN = n + 1;
        float novaAdj[][] = new float[novoN][novoN];
        for (int i = 0; i < novoN; i++)
            for (int j = 0; j < novoN; j++)
                novaAdj[i][j] = Float.POSITIVE_INFINITY;

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                novaAdj[i][j] = adj[i][j];

        int novoRotulo[] = new int[novoN];
        String novoApelido[] = new String[novoN];
        float novoPeso[] = new float[novoN];
        for (int i = 0; i < n; i++) {
            novoRotulo[i] = rotulo[i];
            novoApelido[i] = apelido[i];
            novoPeso[i] = pesoVertice[i];
        }
        novoRotulo[n] = rot;
        novoApelido[n] = ape;
        novoPeso[n] = pesoVert;

        adj = novaAdj;
        rotulo = novoRotulo;
        apelido = novoApelido;
        pesoVertice = novoPeso;
        n = novoN;

        System.out.println("Vertice " + rot + " (" + ape + ") inserido com sucesso.");
    }

    // Remove um vértice (pelo rótulo) e TODAS as arestas associadas a ele
    public void removeVertice(int rot) {
        int v = posicaoPorRotulo(rot);
        if (v == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return;
        }

        int novoN = n - 1;
        float novaAdj[][] = new float[novoN][novoN];
        for (int i = 0; i < novoN; i++)
            for (int j = 0; j < novoN; j++)
                novaAdj[i][j] = Float.POSITIVE_INFINITY;

        int li = 0;
        for (int i = 0; i < n; i++) {
            if (i == v) continue;
            int lj = 0;
            for (int j = 0; j < n; j++) {
                if (j == v) continue;
                novaAdj[li][lj] = adj[i][j];
                lj++;
            }
            li++;
        }

        int novoRotulo[] = new int[novoN];
        String novoApelido[] = new String[novoN];
        float novoPeso[] = new float[novoN];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (i == v) continue;
            novoRotulo[idx] = rotulo[i];
            novoApelido[idx] = apelido[i];
            novoPeso[idx] = pesoVertice[i];
            idx++;
        }

        adj = novaAdj;
        rotulo = novoRotulo;
        apelido = novoApelido;
        pesoVertice = novoPeso;
        n = novoN;

        // Recalcula m para grafo não-orientado (parte superior da matriz)
        m = 0;
        for (int i = 0; i < n; i++)
            for (int j = i; j < n; j++)
                if (adj[i][j] != Float.POSITIVE_INFINITY) m++;

        System.out.println("Vertice " + rot + " removido com sucesso (junto com suas arestas).");
    }

    // Insere uma aresta não-orientada entre rotV e rotW com determinado peso
    public void insereAresta(int rotV, int rotW, float peso) {
        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);
        if (v == -1 || w == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return;
        }
        if (adj[v][w] == Float.POSITIVE_INFINITY) {
            m++;
        }
        adj[v][w] = peso;
        adj[w][v] = peso; // Simetria na matriz
        System.out.println("Aresta " + rotV + " - " + rotW + " (peso " + peso + ") inserida.");
    }

    // Remove a aresta não-orientada entre rotV e rotW
    public void removeAresta(int rotV, int rotW) {
        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);
        if (v == -1 || w == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return;
        }
        if (adj[v][w] != Float.POSITIVE_INFINITY) {
            adj[v][w] = Float.POSITIVE_INFINITY;
            adj[w][v] = Float.POSITIVE_INFINITY; // Remoção simétrica
            m--;
        }
        System.out.println("Aresta " + rotV + " - " + rotW + " removida.");
    }

    // PADRÕES DE LEITURA
    private static final java.util.regex.Pattern PADRAO_VERTICE =
            java.util.regex.Pattern.compile("^\\s*(-?\\d+)\\s+\"([^\"]*)\"\\s+\"([^\"]*)\"\\s*$");
    private static final java.util.regex.Pattern PADRAO_ARESTA =
            java.util.regex.Pattern.compile("^\\s*(-?\\d+)\\s+(-?\\d+)\\s+(-?[\\d.,]+)\\s*$");

    private String proximaLinhaValida(java.io.BufferedReader br) throws IOException {
        String linha;
        while ((linha = br.readLine()) != null) {
            if (!linha.trim().isEmpty()) return linha;
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Lê o arquivo do grafo e extrai o tipo na PRIMEIRA LINHA
    // ----------------------------------------------------------------------
    public void lerArquivo(String nomeArq) throws IOException {
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(
                new java.io.FileInputStream(nomeArq), java.nio.charset.StandardCharsets.UTF_8));

        // 1ª LINHA: Leitura do Tipo do Grafo que está no arquivo .txt
        this.tipoGrafo = Integer.parseInt(proximaLinhaValida(br).trim());

        // 2ª LINHA: Quantidade de vértices (n)
        int novoN = Integer.parseInt(proximaLinhaValida(br).trim());

        adj = new float[novoN][novoN];
        for (int i = 0; i < novoN; i++)
            for (int j = 0; j < novoN; j++)
                adj[i][j] = Float.POSITIVE_INFINITY;

        rotulo = new int[novoN];
        apelido = new String[novoN];
        pesoVertice = new float[novoN];
        n = novoN;
        m = 0;

        for (int i = 0; i < novoN; i++) {
            String linha = proximaLinhaValida(br);
            java.util.regex.Matcher mat = PADRAO_VERTICE.matcher(linha);
            if (!mat.matches()) {
                br.close();
                throw new IOException("Linha de vertice em formato invalido: \"" + linha + "\"");
            }
            rotulo[i] = Integer.parseInt(mat.group(1));
            apelido[i] = mat.group(2);
            pesoVertice[i] = Float.parseFloat(mat.group(3).replace(",", "."));
        }

        int totalArestas = Integer.parseInt(proximaLinhaValida(br).trim());
        for (int i = 0; i < totalArestas; i++) {
            String linha = proximaLinhaValida(br);
            java.util.regex.Matcher mat = PADRAO_ARESTA.matcher(linha);
            if (!mat.matches()) {
                br.close();
                throw new IOException("Linha de aresta em formato invalido: \"" + linha + "\"");
            }
            int rotV = Integer.parseInt(mat.group(1));
            int rotW = Integer.parseInt(mat.group(2));
            float peso = Float.parseFloat(mat.group(3).replace(",", "."));

            int v = posicaoPorRotulo(rotV);
            int w = posicaoPorRotulo(rotW);
            if (v == -1 || w == -1) {
                System.out.println("Aviso: aresta " + rotV + " - " + rotW +
                        " ignorada (rotulo de vertice inexistente no arquivo).");
                continue;
            }
            if (adj[v][w] == Float.POSITIVE_INFINITY) {
                adj[v][w] = peso;
                adj[w][v] = peso; // Garante a não-orientação
                m++;
            }
        }

        br.close();
        System.out.println("Arquivo \"" + nomeArq + "\" lido com sucesso (Tipo " + tipoGrafo + "): " +
                n + " vertice(s), " + m + " aresta(s).");
    }

    // Grava o arquivo mantendo o tipo do grafo lido na 1ª linha
    public void gravarArquivo(String nomeArq) throws IOException {
        PrintWriter pw = new PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(nomeArq), java.nio.charset.StandardCharsets.UTF_8));

        pw.println(tipoGrafo); // Escreve na primeira linha o tipo lido originalmente
        pw.println(n);
        for (int i = 0; i < n; i++) {
            pw.println(rotulo[i] + " \"" + apelido[i] + "\" \"" + pesoVertice[i] + "\"");
        }

        StringBuilder linhasArestas = new StringBuilder();
        int totalArestas = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (adj[i][j] != Float.POSITIVE_INFINITY) {
                    linhasArestas.append(rotulo[i]).append(" ")
                                 .append(rotulo[j]).append(" ")
                                 .append(adj[i][j]).append("\n");
                    totalArestas++;
                }
            }
        }
        pw.println(totalArestas);
        pw.print(linhasArestas);

        pw.close();
        System.out.println("Grafo gravado com sucesso em \"" + nomeArq + "\".");
    }

    public void mostrarConteudo() {
        System.out.println("\n===================== CONTEUDO DO GRAFO =====================");
        System.out.println("Tipo do grafo : " + tipoGrafo);
        System.out.println("Vertices (n)  : " + n);
        System.out.println("Arestas  (m)  : " + m);

        System.out.println("\n-- Vertices --");
        System.out.printf("%-8s %-20s %-10s%n", "Rotulo", "Apelido", "Peso");
        for (int i = 0; i < n; i++) {
            System.out.printf("%-8d %-20s %-10.2f%n", rotulo[i], apelido[i], pesoVertice[i]);
        }

        System.out.println("\n-- Arestas --");
        System.out.printf("%-10s %-10s %-10s%n", "Vertice 1", "Vertice 2", "Peso");
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (adj[i][j] != Float.POSITIVE_INFINITY) {
                    System.out.printf("%-10d %-10d %-10.2f%n", rotulo[i], rotulo[j], adj[i][j]);
                }
            }
        }
        System.out.println("===============================================================\n");
    }

    public void mostrarGrafo() {
        System.out.println("n: " + n);
        System.out.println("m: " + m);
        for (int i = 0; i < n; i++) {
            System.out.print("\n");
            for (int j = 0; j < n; j++) {
                if (adj[i][j] != Float.POSITIVE_INFINITY)
                    System.out.print("Adj[" + rotulo[i] + "," + rotulo[j] + "]= " + adj[i][j] + " ");
                else
                    System.out.print("Adj[" + rotulo[i] + "," + rotulo[j] + "]= inf ");
            }
        }
        System.out.println("\n\nfim da impressao do grafo.");
    }

    // Auxiliar DFS para verificar conexidade
    private int dfsComPilha(int origem, boolean visitado[]) {
        Pilha pilha = new Pilha(n);
        pilha.push(origem);
        visitado[origem] = true;
        int totalVisitados = 1;

        while (!pilha.isEmpty()) {
            int atual = pilha.pop();
            for (int j = 0; j < n; j++) {
                if (adj[atual][j] != Float.POSITIVE_INFINITY && !visitado[j]) {
                    visitado[j] = true;
                    totalVisitados++;
                    pilha.push(j);
                }
            }
        }
        return totalVisitados;
    }

    // Verifica se o grafo não-orientado é conexo
    public boolean ehConexo() {
        if (n == 0) return false;

        boolean visitado[] = new boolean[n];
        int alcancados = dfsComPilha(0, visitado);

        return alcancados == n;
    }

    public int getN() { return n; }
    public int getM() { return m; }
    public int getTipoGrafo() { return tipoGrafo; }
}
