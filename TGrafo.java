/*
 * ======================================================================
 *  TGrafo.java
 * 
 *
 *  Integrantes:
 *    Gabriel Teixeira Bolonha (10426937)
 *    Geovana Bomfim Rodrigues (10410514)
 *    Rodrigo Daiske Uehara    (10440295)
 *    Yating Zheng             (10439511)
 *
 *
 *  
 *    javac *.java
 *    java Main
 *
 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TGrafo {

    // Limite de seguranca para o numero de vertices lido de um arquivo
    public static final int MAX_VERTICES = 1000;

    // ------------------------------------------------------------------
    // Estrutura temporaria usada na leitura: o arquivo inteiro e validado
    // aqui ANTES de qualquer mudanca no grafo da memoria.
    // ------------------------------------------------------------------
    public static class Dados {
        public int tipo;
        public int n;
        public int rotulo[];
        public String apelido[];
        public float pesoVertice[];
        public List<int[]> arestas = new ArrayList<>();      // {rotuloOrigem, rotuloDestino}
        public List<Float> pesoArestas = new ArrayList<>();
    }

    private int n;                 // quantidade de vertices
    private int m;                 // quantidade de arestas
    private float adj[][];         // matriz de adjacencia (peso da aresta, ou infinito se nao existe)

    private int rotulo[];          // rotulo[posicao]  = identificador do vertice
    private String apelido[];      // apelido[posicao] = localidade/nome do vertice
    private float pesoVertice[];   // pesoVertice[posicao] = peso do vertice

    private int tipoGrafo;         // tipo do grafo (1a linha do arquivo)

    // Construtor: grafo vazio inicial
    public TGrafo() {
        this.n = 0;
        this.m = 0;
        this.adj = new float[0][0];
        this.rotulo = new int[0];
        this.apelido = new String[0];
        this.pesoVertice = new float[0];
        this.tipoGrafo = 3; // padrao ate que um arquivo seja lido
    }

    // ------------------------------------------------------------------
    // Auxiliares sobre o tipo do grafo
    // ------------------------------------------------------------------
    public static boolean tipoOrientado(int t)   { return t >= 4; }
    public static boolean tipoPesoVertice(int t) { return t % 2 == 1; }
    public static boolean tipoPesoAresta(int t)  { return (t % 4) >= 2; }

    public static String descricaoTipo(int t) {
        String o = tipoOrientado(t) ? "orientado" : "nao orientado";
        boolean pv = tipoPesoVertice(t), pa = tipoPesoAresta(t);
        String p;
        if (pv && pa) p = "com peso nos vertices e nas arestas";
        else if (pv)  p = "com peso nos vertices";
        else if (pa)  p = "com peso nas arestas";
        else          p = "sem peso";
        return "grafo " + o + " " + p;
    }

    public boolean isOrientado()     { return tipoOrientado(tipoGrafo); }
    public boolean temPesoVertice()  { return tipoPesoVertice(tipoGrafo); }
    public boolean temPesoAresta()   { return tipoPesoAresta(tipoGrafo); }

    // Formata numero sem "1.0E7" e sem ".0" quando e inteiro
    public static String fmt(float v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e9f) return String.valueOf((long) v);
        return new java.math.BigDecimal(Float.toString(v)).toPlainString();
    }

    private static boolean pesoValido(float p) {
        return !Float.isNaN(p) && !Float.isInfinite(p) && p >= 0f;
    }

    // Descobre a POSICAO (0..n-1) do vertice pelo ROTULO
    private int posicaoPorRotulo(int rot) {
        for (int i = 0; i < n; i++) {
            if (rotulo[i] == rot) return i;
        }
        return -1;
    }

    private boolean existe(int v, int w) {
        return adj[v][w] != Float.POSITIVE_INFINITY;
    }

    private void recalculaM() {
        m = 0;
        for (int i = 0; i < n; i++) {
            for (int j = isOrientado() ? 0 : i; j < n; j++) {
                if (existe(i, j)) m++;
            }
        }
    }

    // ------------------------------------------------------------------
    // Insercao e remocao
    // ------------------------------------------------------------------

    // Insere um novo vertice, isolado (sem arestas). Retorna true se inseriu.
    public boolean insereVertice(int rot, String ape, float pesoVert) {
        if (ape == null || ape.trim().isEmpty()) {
            System.out.println("Erro: o apelido do vertice nao pode ser vazio.");
            return false;
        }
        if (ape.indexOf('"') >= 0 || ape.indexOf('\n') >= 0 || ape.indexOf('\r') >= 0) {
            System.out.println("Erro: o apelido nao pode conter aspas nem quebra de linha.");
            return false;
        }
        if (temPesoVertice() && !pesoValido(pesoVert)) {
            System.out.println("Erro: peso de vertice invalido (use um numero finito maior ou igual a zero).");
            return false;
        }
        if (n >= MAX_VERTICES) {
            System.out.println("Erro: limite de " + MAX_VERTICES + " vertices atingido.");
            return false;
        }
        if (posicaoPorRotulo(rot) != -1) {
            System.out.println("Erro: ja existe um vertice com rotulo " + rot + ".");
            return false;
        }
        if (!temPesoVertice()) pesoVert = 0f;

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
        return true;
    }

    // Remove um vertice (pelo rotulo) e TODAS as arestas associadas a ele
    public boolean removeVertice(int rot) {
        int v = posicaoPorRotulo(rot);
        if (v == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return false;
        }

        int novoN = n - 1;
        float novaAdj[][] = new float[novoN][novoN];
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
        recalculaM();

        System.out.println("Vertice " + rot + " removido com sucesso (junto com suas arestas).");
        return true;
    }

    // Insere aresta rotV -> rotW (se nao orientado, tambem rotW -> rotV)
    public boolean insereAresta(int rotV, int rotW, float peso) {
        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);
        if (v == -1 || w == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return false;
        }
        if (v == w) {
            System.out.println("Erro: laco (aresta de um vertice para ele mesmo) nao e aceito.");
            return false;
        }
        if (temPesoAresta()) {
            if (!pesoValido(peso)) {
                System.out.println("Erro: peso de aresta invalido (use um numero finito maior ou igual a zero).");
                return false;
            }
        } else {
            peso = 1f; // grafo sem peso na aresta: valor interno apenas para marcar a existencia
        }
        boolean jaExistia = existe(v, w);
        adj[v][w] = peso;
        if (!isOrientado()) adj[w][v] = peso;
        if (!jaExistia) m++;
        System.out.println("Aresta " + rotV + (isOrientado() ? " -> " : " - ") + rotW +
                (temPesoAresta() ? " (peso " + fmt(peso) + ")" : "") +
                (jaExistia ? " ja existia, peso atualizado." : " inserida."));
        return true;
    }

    // Remove a aresta rotV -> rotW (se nao orientado, remove nos dois sentidos)
    public boolean removeAresta(int rotV, int rotW) {
        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);
        if (v == -1 || w == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return false;
        }
        if (!existe(v, w)) {
            System.out.println("Erro: a aresta " + rotV + (isOrientado() ? " -> " : " - ") + rotW + " nao existe.");
            return false;
        }
        adj[v][w] = Float.POSITIVE_INFINITY;
        if (!isOrientado()) adj[w][v] = Float.POSITIVE_INFINITY;
        m--;
        System.out.println("Aresta " + rotV + (isOrientado() ? " -> " : " - ") + rotW + " removida.");
        return true;
    }

    // ------------------------------------------------------------------
    // LEITURA SEGURA
    // ------------------------------------------------------------------
    private static final Pattern PADRAO_VERTICE =
            Pattern.compile("^\\s*(-?\\d+)\\s+\"([^\"]*)\"(?:\\s+\"([^\"]*)\")?\\s*$");
    private static final Pattern PADRAO_ARESTA =
            Pattern.compile("^\\s*(-?\\d+)\\s+(-?\\d+)(?:\\s+(\\S+))?\\s*$");

    private static String proximaLinhaValida(BufferedReader br) throws IOException {
        String linha;
        while ((linha = br.readLine()) != null) {
            if (!linha.trim().isEmpty()) return linha;
        }
        return null;
    }

    private static String exige(String linha, String o_que) throws IOException {
        if (linha == null) throw new IOException("Arquivo incompleto: faltou " + o_que + ".");
        return linha;
    }

    private static int inteiro(String s, String o_que) throws IOException {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Valor invalido para " + o_que + ": \"" + s.trim() + "\".");
        }
    }

    private static float peso(String s, String o_que) throws IOException {
        float p;
        try {
            p = Float.parseFloat(s.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IOException("Peso invalido (" + o_que + "): \"" + s.trim() + "\".");
        }
        if (!pesoValido(p)) {
            throw new IOException("Peso invalido (" + o_que + "): \"" + s.trim() +
                    "\" (deve ser um numero finito maior ou igual a zero).");
        }
        return p;
    }

    /**
     * Le e VALIDA o arquivo, devolvendo os dados numa estrutura temporaria.
     * Nao mexe em nenhum grafo. Qualquer problema vira IOException.
     */
    public static Dados lerDados(String nomeArq) throws IOException {
        Dados d = new Dados();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Files.newInputStream(Paths.get(nomeArq)), StandardCharsets.UTF_8))) {

            d.tipo = inteiro(exige(proximaLinhaValida(br), "o tipo do grafo (1a linha)"), "o tipo do grafo");
            if (d.tipo < 0 || d.tipo > 7)
                throw new IOException("Tipo de grafo invalido: " + d.tipo + " (aceitos: 0 a 7).");

            d.n = inteiro(exige(proximaLinhaValida(br), "a quantidade de vertices"), "a quantidade de vertices");
            if (d.n < 0 || d.n > MAX_VERTICES)
                throw new IOException("Quantidade de vertices invalida: " + d.n +
                        " (aceito: 0 a " + MAX_VERTICES + ").");

            d.rotulo = new int[d.n];
            d.apelido = new String[d.n];
            d.pesoVertice = new float[d.n];
            java.util.HashSet<Integer> usados = new java.util.HashSet<>();

            for (int i = 0; i < d.n; i++) {
                String linha = exige(proximaLinhaValida(br),
                        "o vertice " + (i + 1) + " de " + d.n + " (arquivo incompleto)");
                Matcher mat = PADRAO_VERTICE.matcher(linha);
                if (!mat.matches())
                    throw new IOException("Linha de vertice em formato invalido: \"" + linha.trim() + "\"");
                int rot = inteiro(mat.group(1), "o rotulo do vertice");
                if (!usados.add(rot))
                    throw new IOException("Rotulo de vertice repetido: " + rot + ".");
                d.rotulo[i] = rot;
                d.apelido[i] = mat.group(2);
                if (tipoPesoVertice(d.tipo)) {
                    if (mat.group(3) == null)
                        throw new IOException("Vertice " + rot + " sem peso, mas o tipo " + d.tipo +
                                " exige peso no vertice.");
                    d.pesoVertice[i] = peso(mat.group(3), "vertice " + rot);
                }
            }

            int totalArestas = inteiro(exige(proximaLinhaValida(br), "a quantidade de arestas"),
                    "a quantidade de arestas");
            if (totalArestas < 0)
                throw new IOException("Quantidade de arestas invalida: " + totalArestas + ".");

            java.util.HashSet<Long> vistas = new java.util.HashSet<>();
            for (int i = 0; i < totalArestas; i++) {
                String linha = exige(proximaLinhaValida(br),
                        "a aresta " + (i + 1) + " de " + totalArestas + " (arquivo incompleto)");
                Matcher mat = PADRAO_ARESTA.matcher(linha);
                if (!mat.matches())
                    throw new IOException("Linha de aresta em formato invalido: \"" + linha.trim() + "\"");
                int a = inteiro(mat.group(1), "o vertice de origem da aresta");
                int b = inteiro(mat.group(2), "o vertice de destino da aresta");
                if (!usados.contains(a) || !usados.contains(b))
                    throw new IOException("Aresta " + a + " " + b + " usa rotulo de vertice inexistente.");
                if (a == b)
                    throw new IOException("Aresta " + a + " " + b + " e um laco (nao aceito).");
                long chave;
                if (tipoOrientado(d.tipo)) chave = ((long) a << 32) ^ (b & 0xffffffffL);
                else chave = ((long) Math.min(a, b) << 32) ^ (Math.max(a, b) & 0xffffffffL);
                if (!vistas.add(chave))
                    throw new IOException("Aresta repetida: " + a + " " + b + ".");
                float p = 1f;
                if (tipoPesoAresta(d.tipo)) {
                    if (mat.group(3) == null)
                        throw new IOException("Aresta " + a + " " + b + " sem peso, mas o tipo " + d.tipo +
                                " exige peso na aresta.");
                    p = peso(mat.group(3), "aresta " + a + " " + b);
                }
                d.arestas.add(new int[]{a, b});
                d.pesoArestas.add(p);
            }

            if (proximaLinhaValida(br) != null)
                throw new IOException("Ha linhas sobrando depois das " + totalArestas +
                        " arestas declaradas (a quantidade de arestas nao confere com o arquivo).");
        } catch (java.nio.file.NoSuchFileException e) {
            throw new IOException("Arquivo \"" + nomeArq + "\" nao encontrado (grave um grafo com a opcao b ou coloque o arquivo na pasta da aplicacao).");
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("Falha ao interpretar o arquivo: " + e.getMessage());
        }
        return d;
    }

    // Le o arquivo e, SOMENTE se estiver tudo valido, substitui o grafo da memoria.
    public void lerArquivo(String nomeArq) throws IOException {
        Dados d = lerDados(nomeArq);   // se falhar, lanca excecao e o grafo atual fica intacto

        int novoN = d.n;
        float novaAdj[][] = new float[novoN][novoN];
        for (int i = 0; i < novoN; i++)
            for (int j = 0; j < novoN; j++)
                novaAdj[i][j] = Float.POSITIVE_INFINITY;

        java.util.HashMap<Integer, Integer> pos = new java.util.HashMap<>();
        for (int i = 0; i < novoN; i++) pos.put(d.rotulo[i], i);

        int novoM = 0;
        for (int k = 0; k < d.arestas.size(); k++) {
            int v = pos.get(d.arestas.get(k)[0]);
            int w = pos.get(d.arestas.get(k)[1]);
            float p = d.pesoArestas.get(k);
            novaAdj[v][w] = p;
            if (!tipoOrientado(d.tipo)) novaAdj[w][v] = p;
            novoM++;
        }

        // Tudo validado: troca de uma vez so
        this.tipoGrafo = d.tipo;
        this.n = novoN;
        this.m = novoM;
        this.adj = novaAdj;
        this.rotulo = d.rotulo;
        this.apelido = d.apelido;
        this.pesoVertice = d.pesoVertice;

        System.out.println("Arquivo \"" + nomeArq + "\" lido com sucesso.");
        System.out.println("Tipo " + tipoGrafo + " (" + descricaoTipo(tipoGrafo) + ") | " +
                n + " vertice(s) | " + m + " aresta(s).");
    }

    // ------------------------------------------------------------------
    // GRAVACAO (grava em arquivo temporario e so entao substitui o original)
    // ------------------------------------------------------------------
    public void gravarArquivo(String nomeArq) throws IOException {
        Path destino = Paths.get(nomeArq).toAbsolutePath();
        Path tmp = Files.createTempFile(destino.getParent(), "grafo", ".tmp");
        try {
            try (PrintWriter pw = new PrintWriter(new java.io.OutputStreamWriter(
                    Files.newOutputStream(tmp), StandardCharsets.UTF_8))) {
                pw.print(tipoGrafo + "\n");
                pw.print(n + "\n");
                for (int i = 0; i < n; i++) {
                    pw.print(rotulo[i] + " \"" + apelido[i] + "\"");
                    if (temPesoVertice()) pw.print(" \"" + fmt(pesoVertice[i]) + "\"");
                    pw.print("\n");
                }
                pw.print(m + "\n");
                for (int i = 0; i < n; i++) {
                    for (int j = isOrientado() ? 0 : i; j < n; j++) {
                        if (existe(i, j)) {
                            pw.print(rotulo[i] + " " + rotulo[j]);
                            if (temPesoAresta()) pw.print(" " + fmt(adj[i][j]));
                            pw.print("\n");
                        }
                    }
                }
                if (pw.checkError()) throw new IOException("Erro de escrita no arquivo temporario.");
            }
            Files.move(tmp, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
        System.out.println("Grafo gravado com sucesso em \"" + nomeArq + "\" (" + n + " vertices, " + m + " arestas).");
    }

    // ------------------------------------------------------------------
    // OPCAO G: mostra o conteudo do ARQUIVO salvo no disco (nao a memoria)
    // ------------------------------------------------------------------
    public static void mostrarConteudoArquivo(String nomeArq) throws IOException {
        Dados d = lerDados(nomeArq);  // le do disco; nao altera nenhum grafo
        boolean pv = tipoPesoVertice(d.tipo), pa = tipoPesoAresta(d.tipo);
        String seta = tipoOrientado(d.tipo) ? " -> " : " - ";

        System.out.println();
        System.out.println("=============== CONTEUDO DO ARQUIVO \"" + nomeArq + "\" ===============");
        System.out.println("Tipo do grafo : " + d.tipo + " (" + descricaoTipo(d.tipo) + ")");
        System.out.println("Vertices (n)  : " + d.n);
        System.out.println("Arestas  (m)  : " + d.arestas.size());

        System.out.println("\n-- Vertices --");
        System.out.printf("%-8s %-45s %s%n", "Rotulo", "Apelido", pv ? "Peso" : "");
        for (int i = 0; i < d.n; i++) {
            System.out.printf("%-8d %-45s %s%n", d.rotulo[i], d.apelido[i], pv ? fmt(d.pesoVertice[i]) : "");
        }

        java.util.HashMap<Integer, String> nome = new java.util.HashMap<>();
        for (int i = 0; i < d.n; i++) nome.put(d.rotulo[i], d.apelido[i]);

        System.out.println("\n-- Arestas --");
        for (int k = 0; k < d.arestas.size(); k++) {
            int a = d.arestas.get(k)[0], b = d.arestas.get(k)[1];
            System.out.printf("%4d%s%-4d %s%s%s%s%n", a, seta, b,
                    nome.get(a), seta, nome.get(b),
                    pa ? "  [peso " + fmt(d.pesoArestas.get(k)) + "]" : "");
        }
       
        System.out.println("==============================================================================\n");
    }

    // ------------------------------------------------------------------
    // OPCAO H: mostra o grafo da memoria como LISTA de adjacencia
    // ------------------------------------------------------------------
    public void mostrarGrafo() {
        System.out.println("\nGrafo em memoria | Tipo " + tipoGrafo + " (" + descricaoTipo(tipoGrafo) + ")");
        System.out.println("n = " + n + " | m = " + m);
        System.out.println("Lista de adjacencia (vertice: vizinho[peso da aresta], ...)\n");
        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(rotulo[i]).append(" \"").append(apelido[i]).append("\"");
            if (temPesoVertice()) sb.append(" {peso ").append(fmt(pesoVertice[i])).append("}");
            sb.append(":");
            boolean algum = false;
            for (int j = 0; j < n; j++) {
                if (existe(i, j)) {
                    sb.append(algum ? ", " : " ").append(rotulo[j]);
                    if (temPesoAresta()) sb.append("[").append(fmt(adj[i][j])).append("]");
                    algum = true;
                }
            }
            if (!algum) sb.append(" (sem vizinhos)");
            System.out.println(sb);
        }
        System.out.println("\nfim da impressao do grafo.");
    }

    // ------------------------------------------------------------------
    // CONEXIDADE
    // ------------------------------------------------------------------

    // DFS (com pilha) ignorando a orientacao: usa adj[i][j] ou adj[j][i]
    private int dfsComPilha(int origem, boolean visitado[]) {
        Pilha pilha = new Pilha(n);
        pilha.push(origem);
        visitado[origem] = true;
        int total = 1;
        while (!pilha.isEmpty()) {
            int atual = pilha.pop();
            for (int j = 0; j < n; j++) {
                if (!visitado[j] && (existe(atual, j) || existe(j, atual))) {
                    visitado[j] = true;
                    total++;
                    pilha.push(j);
                }
            }
        }
        return total;
    }

    // Conexo (para nao orientado) ou fracamente conexo (base da categoria C1)
    public boolean ehConexo() {
        if (n == 0) return false;
        boolean visitado[] = new boolean[n];
        return dfsComPilha(0, visitado) == n;
    }

    // Matriz de alcancabilidade (Warshall)
    private boolean[][] calculaAlcancabilidade() {
        boolean r[][] = new boolean[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                r[i][j] = (i == j) || existe(i, j);
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                if (r[i][k])
                    for (int j = 0; j < n; j++)
                        if (r[k][j]) r[i][j] = true;
        return r;
    }

    // Categoria de conexidade de grafo orientado: 3, 2, 1 ou 0
    public int categoriaConexidade() {
        if (n == 0) return 0;
        boolean r[][] = calculaAlcancabilidade();
        boolean c3 = true, c2 = true;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!(r[i][j] && r[j][i])) c3 = false;
                if (!(r[i][j] || r[j][i])) c2 = false;
            }
        }
        if (c3) return 3;
        if (c2) return 2;
        return ehConexo() ? 1 : 0;
    }

    // Componentes fortemente conexas: comp[i] = indice da componente do vertice na posicao i
    private int[] calculaComponentes(boolean r[][]) {
        int comp[] = new int[n];
        java.util.Arrays.fill(comp, -1);
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (comp[i] != -1) continue;
            comp[i] = k;
            for (int j = i + 1; j < n; j++)
                if (comp[j] == -1 && r[i][j] && r[j][i]) comp[j] = k;
            k++;
        }
        return comp;
    }

    // Mostra componentes fortemente conexas e o grafo reduzido
    public void mostrarGrafoReduzido() {
        boolean r[][] = calculaAlcancabilidade();
        int comp[] = calculaComponentes(r);
        int k = 0;
        for (int i = 0; i < n; i++) k = Math.max(k, comp[i] + 1);

        System.out.println("\nComponentes fortemente conexas (" + k + "):");
        for (int c = 0; c < k; c++) {
            StringBuilder sb = new StringBuilder("  C" + (c + 1) + " = {");
            boolean primeiro = true;
            for (int i = 0; i < n; i++) {
                if (comp[i] == c) {
                    sb.append(primeiro ? "" : ", ").append(rotulo[i]);
                    primeiro = false;
                }
            }
            System.out.println(sb.append("}"));
        }

        boolean red[][] = new boolean[k][k];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (existe(i, j) && comp[i] != comp[j]) red[comp[i]][comp[j]] = true;

        System.out.println("\nGrafo reduzido (arcos entre componentes):");
        boolean algum = false;
        for (int a = 0; a < k; a++)
            for (int b = 0; b < k; b++)
                if (red[a][b]) {
                    System.out.println("  C" + (a + 1) + " -> C" + (b + 1));
                    algum = true;
                }
        if (!algum) System.out.println("  (sem arcos: nenhuma ligacao entre componentes)");

        System.out.println("\nMatriz de adjacencia do grafo reduzido:");
        StringBuilder cab = new StringBuilder("      ");
        for (int b = 0; b < k; b++) cab.append(String.format("%-5s", "C" + (b + 1)));
        System.out.println(cab);
        for (int a = 0; a < k; a++) {
            StringBuilder sb = new StringBuilder(String.format("%-6s", "C" + (a + 1)));
            for (int b = 0; b < k; b++) sb.append(String.format("%-5d", red[a][b] ? 1 : 0));
            System.out.println(sb);
        }
    }

    // OPCAO I
    public void mostrarConexidade() {
        if (n == 0) {
            System.out.println("Grafo vazio: nao ha o que analisar.");
            return;
        }
        if (!isOrientado()) {
            System.out.println(ehConexo() ? "O grafo e CONEXO." : "O grafo e DESCONEXO.");
        } else {
            System.out.println("Categoria de conexidade: C" + categoriaConexidade());
            mostrarGrafoReduzido();
        }
    }

    // ==================================================================
    //  PARTE 3 - ITEM 1: rota de menor distancia dentro de um orcamento
    //  Tecnica da disciplina: algoritmo de Dijkstra.
    //
    //  Modelo: peso da aresta = distancia (m); peso do vertice = custo (R$).
    //  Custo da rota = soma dos pesos dos vertices visitados, exceto a
    //  origem (quem sai de casa nao paga por estar la). Distancia e custo
    //  tem unidades diferentes e NAO sao somados: a distancia e o que se
    //  minimiza e o custo e uma restricao (custo total <= orcamento).
    //
    //  Como o custo depende do caminho todo, um unico rotulo de distancia
    //  por vertice nao basta. Cada vertice guarda varias etiquetas
    //  (distancia, custo) e uma etiqueta so e descartada quando outra ja
    //  fechada naquele vertice e melhor ou igual nas DUAS medidas.
    //  A fila de prioridade ordena por distancia, como no Dijkstra.
    // ==================================================================

    // Etiqueta de um caminho parcial: chegou em v com distancia e custo acumulados
    private static class Etiqueta {
        int v;
        float dist;
        float custo;
        Etiqueta pai;
        Etiqueta(int v, float dist, float custo, Etiqueta pai) {
            this.v = v; this.dist = dist; this.custo = custo; this.pai = pai;
        }
    }

    private float custoDoVertice(int i) {
        return temPesoVertice() ? pesoVertice[i] : 0f;
    }

    private static boolean dominada(List<Etiqueta> fechadas, float dist, float custo) {
        for (Etiqueta f : fechadas)
            if (f.dist <= dist && f.custo <= custo) return true;
        return false;
    }

    // Menor distancia de o ate d com custo total <= orcamento (null se impossivel)
    private Etiqueta menorRota(int o, int d, float orcamento) {
        List<List<Etiqueta>> fechadas = new ArrayList<>();
        for (int i = 0; i < n; i++) fechadas.add(new ArrayList<>());
        java.util.PriorityQueue<Etiqueta> fila = new java.util.PriorityQueue<>(
                (a, b) -> a.dist != b.dist ? Float.compare(a.dist, b.dist) : Float.compare(a.custo, b.custo));
        fila.add(new Etiqueta(o, 0f, 0f, null));

        while (!fila.isEmpty()) {
            Etiqueta e = fila.poll();
            if (dominada(fechadas.get(e.v), e.dist, e.custo)) continue;
            fechadas.get(e.v).add(e);
            if (e.v == d) return e; // primeira etiqueta fechada no destino = menor distancia viavel
            for (int u = 0; u < n; u++) {
                if (!existe(e.v, u)) continue;
                float nd = e.dist + adj[e.v][u];
                float nc = e.custo + custoDoVertice(u);
                if (nc > orcamento) continue;
                if (dominada(fechadas.get(u), nd, nc)) continue;
                fila.add(new Etiqueta(u, nd, nc, e));
            }
        }
        return null;
    }

    public boolean existeRotulo(int rot) { return posicaoPorRotulo(rot) != -1; }

    public String apelidoDoRotulo(int rot) {
        int p = posicaoPorRotulo(rot);
        return p == -1 ? null : apelido[p];
    }

    private static String semAcento(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
    }

    // Rotulos dos vertices cujo apelido contem o trecho (sem diferenciar acento/maiuscula)
    public int[] rotulosPorNome(String trecho) {
        String t = semAcento(trecho.trim());
        List<Integer> achados = new ArrayList<>();
        for (int i = 0; i < n; i++)
            if (semAcento(apelido[i]).contains(t)) achados.add(rotulo[i]);
        int r[] = new int[achados.size()];
        for (int i = 0; i < r.length; i++) r[i] = achados.get(i);
        return r;
    }

    // Corta nomes longos para a tabela nao desalinhar
    private static String curto(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }

    private void imprimeRota(Etiqueta fim, float orcamento) {
        List<Etiqueta> passos = new ArrayList<>();
        for (Etiqueta e = fim; e != null; e = e.pai) passos.add(0, e);
        System.out.printf("  %-4s %-6s %-42s %10s %12s %10s %10s%n",
                "Pas.", "Rot.", "Local", "Trecho(m)", "Acum.(m)", "Custo R$", "Acum. R$");
        for (int k = 0; k < passos.size(); k++) {
            Etiqueta e = passos.get(k);
            float trecho = k == 0 ? 0f : e.dist - passos.get(k - 1).dist;
            float cv = k == 0 ? 0f : e.custo - passos.get(k - 1).custo;
            System.out.printf("  %-4d %-6d %-42s %10s %12s %10s %10s%n",
                    k, rotulo[e.v], curto(apelido[e.v], 42), fmt(trecho), fmt(e.dist), fmt(cv), fmt(e.custo));
        }
        System.out.println("  Total: " + fmt(fim.dist) + " m (" + String.format(java.util.Locale.US, "%.2f", fim.dist / 1000f)
                + " km), custo R$ " + fmt(fim.custo)
                + (orcamento >= fim.custo ? ", sobra do orcamento R$ " + fmt(orcamento - fim.custo)
                                          : ", faltam R$ " + fmt(fim.custo - orcamento) + " no orcamento"));
    }

    // OPCAO K: rota de menor distancia respeitando o orcamento (uma unica versao de Dijkstra)
    public void mostrarRotaComOrcamento(int rotOrigem, int rotDestino, float orcamento) {
        int o = posicaoPorRotulo(rotOrigem), d = posicaoPorRotulo(rotDestino);
        if (o == -1 || d == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return;
        }
        if (!pesoValido(orcamento)) {
            System.out.println("Erro: orcamento invalido.");
            return;
        }
        System.out.println("\nOrigem : " + rotOrigem + " " + apelido[o]);
        System.out.println("Destino: " + rotDestino + " " + apelido[d]);
        System.out.println("Orcamento: R$ " + fmt(orcamento) + " (custo = soma dos pesos dos vertices visitados, sem contar a origem)");
        System.out.println("\nMenor distancia com custo <= R$ " + fmt(orcamento) + " (Dijkstra com restricao de orcamento):");

        Etiqueta rota = menorRota(o, d, orcamento);
        if (rota == null)
            System.out.println("  Nao existe rota entre os dois locais dentro desse orcamento.");
        else
            imprimeRota(rota, orcamento);
    }

    // ==================================================================
    //  PARTE 3 - ITEM 2: caracteristicas do problema modelado
    //  (1) grau dos vertices  (2) euleriano  (3) hamiltoniano  (4) coloracao
    // ==================================================================

    private String listaRotulos(List<Integer> posicoes) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < posicoes.size(); k++) {
            if (k > 0) sb.append(", ");
            sb.append(rotulo[posicoes.get(k)]);
        }
        return sb.toString();
    }

    // OPCAO L
    public void mostrarCaracteristicas() {
        if (n == 0) {
            System.out.println("Grafo vazio: leia o arquivo (opcao a) antes.");
            return;
        }
        System.out.println("\n============ CARACTERISTICAS DO GRAFO ============");
        System.out.println("Tipo " + tipoGrafo + " (" + descricaoTipo(tipoGrafo) + ") | n = " + n + " | m = " + m);
        caracGraus();
        caracEuler();
        caracHamilton();
        caracColoracao();
        System.out.println("==================================================\n");
    }

    // ---------- (1) grau dos vertices ----------
    private void caracGraus() {
        System.out.println("\n--- 1. Grau dos vertices ---");
        int grauS[] = new int[n], grauE[] = new int[n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (existe(i, j)) { grauS[i]++; grauE[j]++; }
        if (!isOrientado()) {
            resumoGrau("Grau", grauS);
            int soma = 0;
            for (int g : grauS) soma += g;
            System.out.println("Soma dos graus = " + soma + " = 2 x m (" + (2 * m) + "): " + (soma == 2 * m ? "confere (lema do aperto de maos)." : "NAO confere!"));
        } else {
            resumoGrau("Grau de saida", grauS);
            resumoGrau("Grau de entrada", grauE);
        }
    }

    private void resumoGrau(String nomeGrau, int g[]) {
        int min = Integer.MAX_VALUE, max = 0, soma = 0;
        for (int x : g) { min = Math.min(min, x); max = Math.max(max, x); soma += x; }
        System.out.println(nomeGrau + ": minimo " + min + ", maximo " + max + ", medio " +
                String.format(java.util.Locale.US, "%.2f", soma / (double) n));
        java.util.TreeMap<Integer, Integer> dist = new java.util.TreeMap<>();
        for (int x : g) dist.merge(x, 1, Integer::sum);
        StringBuilder sb = new StringBuilder("Distribuicao (grau:quantidade): ");
        for (java.util.Map.Entry<Integer, Integer> e : dist.entrySet())
            sb.append(e.getKey()).append(":").append(e.getValue()).append("  ");
        System.out.println(sb);

        Integer idx[] = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> g[b] - g[a]);
        System.out.println("Mais conectados:");
        for (int k = 0; k < Math.min(5, n); k++)
            System.out.println("   " + rotulo[idx[k]] + " " + apelido[idx[k]] + " (" + g[idx[k]] + ")");
        List<Integer> baixos = new ArrayList<>();
        for (int i = 0; i < n; i++) if (g[i] <= 1) baixos.add(i);
        if (!baixos.isEmpty()) {
            System.out.println("Vertices com grau 0 ou 1 (pendentes):");
            for (int i : baixos) System.out.println("   " + rotulo[i] + " " + apelido[i] + " (" + g[i] + ")");
        }
    }

    // ---------- (2) euleriano ----------
    private void caracEuler() {
        System.out.println("\n--- 2. Grafo euleriano / percurso euleriano ---");
        boolean conexo = ehConexo();
        if (!isOrientado()) {
            List<Integer> impares = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int g = 0;
                for (int j = 0; j < n; j++) if (existe(i, j)) g++;
                if (g % 2 == 1) impares.add(i);
            }
            System.out.println("Conexo: " + (conexo ? "sim" : "nao") + " | vertices de grau impar: " + impares.size());
            if (!impares.isEmpty() && impares.size() <= 30)
                System.out.println("Impares: " + listaRotulos(impares));
            if (conexo && impares.isEmpty())
                System.out.println("Resultado: grafo EULERIANO (existe ciclo euleriano).");
            else if (conexo && impares.size() == 2)
                System.out.println("Resultado: NAO e euleriano, mas TEM percurso euleriano aberto, de " +
                        rotulo[impares.get(0)] + " ate " + rotulo[impares.get(1)] + ".");
            else {
                System.out.println("Resultado: NAO e euleriano e NAO tem percurso euleriano" +
                        (conexo ? " (exige 0 ou 2 vertices de grau impar)." : " (o grafo nao e conexo)."));
                if (conexo)
                    System.out.println("Para virar euleriano seria preciso repetir ou acrescentar ao menos " +
                            (impares.size() / 2) + " arestas, uma para cada par de vertices impares.");
            }
        } else {
            int saiMais = 0, entraMais = 0, outros = 0;
            for (int i = 0; i < n; i++) {
                int s = 0, e = 0;
                for (int j = 0; j < n; j++) { if (existe(i, j)) s++; if (existe(j, i)) e++; }
                if (s - e == 1) saiMais++;
                else if (e - s == 1) entraMais++;
                else if (s != e) outros++;
            }
            if (conexo && outros == 0 && saiMais == 0 && entraMais == 0)
                System.out.println("Resultado: grafo EULERIANO (grau de entrada = saida em todos os vertices).");
            else if (conexo && outros == 0 && saiMais == 1 && entraMais == 1)
                System.out.println("Resultado: NAO e euleriano, mas TEM percurso euleriano.");
            else
                System.out.println("Resultado: NAO e euleriano e NAO tem percurso euleriano.");
        }
    }

    // ---------- (3) hamiltoniano ----------
    private int tempoDfs;

    // DFS de Tarjan: acha pontos de articulacao e blocos (componentes biconexas)
    private void dfsBlocos(int u, int pai, int disc[], int low[], boolean art[],
                           java.util.ArrayDeque<int[]> pilha, List<java.util.Set<Integer>> blocos) {
        disc[u] = low[u] = ++tempoDfs;
        int filhos = 0;
        for (int w = 0; w < n; w++) {
            if (w == u || !existe(u, w)) continue;
            if (disc[w] == 0) {
                filhos++;
                pilha.push(new int[]{u, w});
                dfsBlocos(w, u, disc, low, art, pilha, blocos);
                low[u] = Math.min(low[u], low[w]);
                if ((pai == -1 && filhos > 1) || (pai != -1 && low[w] >= disc[u])) art[u] = true;
                if (low[w] >= disc[u]) {
                    java.util.Set<Integer> bloco = new java.util.HashSet<>();
                    int ar[];
                    do {
                        ar = pilha.pop();
                        bloco.add(ar[0]);
                        bloco.add(ar[1]);
                    } while (!(ar[0] == u && ar[1] == w));
                    blocos.add(bloco);
                }
            } else if (w != pai && disc[w] < disc[u]) {
                pilha.push(new int[]{u, w});
                low[u] = Math.min(low[u], disc[w]);
            }
        }
    }

    // Busca exaustiva (com limite de nos) por caminho/ciclo hamiltoniano
    private long nosBusca, limiteBusca;
    private boolean usadoHam[];
    private int caminhoHam[];
    private boolean buscaCiclo;

    private boolean hamDfs(int u, int prof) {
        if (prof == n) return !buscaCiclo || existe(u, caminhoHam[0]);
        if (++nosBusca > limiteBusca) return false;
        for (int w = 0; w < n; w++) {
            if (!usadoHam[w] && existe(u, w)) {
                usadoHam[w] = true;
                caminhoHam[prof] = w;
                if (hamDfs(w, prof + 1)) return true;
                usadoHam[w] = false;
                if (nosBusca > limiteBusca) return false;
            }
        }
        return false;
    }

    // 1 = encontrou, 0 = nao existe (busca completa), -1 = inconclusivo (estourou o limite)
    private int buscaHamilton(boolean ciclo) {
        buscaCiclo = ciclo;
        nosBusca = 0;
        limiteBusca = 3_000_000L;
        caminhoHam = new int[n];
        for (int s = 0; s < (ciclo ? 1 : n); s++) {
            usadoHam = new boolean[n];
            usadoHam[s] = true;
            caminhoHam[0] = s;
            if (hamDfs(s, 1)) return 1;
            if (nosBusca > limiteBusca) return -1;
        }
        return 0;
    }

    private void caracHamilton() {
        System.out.println("\n--- 3. Hamiltoniano (ciclo e caminho) ---");
        boolean refutaCiclo = false, refutaCaminho = false;

        if (n < 3) refutaCiclo = true;
        if (!ehConexo()) {
            System.out.println("O grafo nao e conexo: nao tem ciclo nem caminho hamiltoniano.");
            return;
        }

        if (!isOrientado()) {
            int grauMin = Integer.MAX_VALUE;
            List<Integer> pendentes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int g = 0;
                for (int j = 0; j < n; j++) if (existe(i, j)) g++;
                grauMin = Math.min(grauMin, g);
                if (g < 2) pendentes.add(i);
            }
            int disc[] = new int[n], low[] = new int[n];
            boolean art[] = new boolean[n];
            List<java.util.Set<Integer>> blocos = new ArrayList<>();
            tempoDfs = 0;
            dfsBlocos(0, -1, disc, low, art, new java.util.ArrayDeque<>(), blocos);
            List<Integer> arts = new ArrayList<>();
            for (int i = 0; i < n; i++) if (art[i]) arts.add(i);
            int folhas = 0;
            for (java.util.Set<Integer> b : blocos) {
                int c = 0;
                for (int v : b) if (art[v]) c++;
                if (c == 1) folhas++;
            }
            System.out.println("Grau minimo: " + grauMin + " | pontos de articulacao: " + arts.size() +
                    (arts.isEmpty() ? "" : " (" + listaRotulos(arts) + ")") + " | blocos-folha: " + folhas);

            if (!pendentes.isEmpty()) {
                refutaCiclo = true;
                System.out.println("Ciclo: impossivel, pois ha vertice de grau < 2 (" + listaRotulos(pendentes) + "), e todo vertice de um ciclo precisa de 2 vizinhos.");
            }
            if (!arts.isEmpty()) {
                refutaCiclo = true;
                System.out.println("Ciclo: impossivel, pois ha ponto de articulacao (um ciclo hamiltoniano nao pode ser desconectado por um unico vertice).");
            }
            if (folhas > 2) {
                refutaCaminho = true;
                System.out.println("Caminho: impossivel, pois ha " + folhas + " blocos-folha; cada um precisa conter uma extremidade do caminho e so existem 2.");
            }
            if (!refutaCiclo && grauMin * 2 >= n && n >= 3) {
                System.out.println("Teorema de Dirac satisfeito (grau minimo >= n/2): o grafo TEM ciclo hamiltoniano.");
                return;
            }
        }

        if (!refutaCiclo) {
            int r = buscaHamilton(true);
            System.out.println(r == 1 ? "Resultado do ciclo: TEM ciclo hamiltoniano."
                    : r == 0 ? "Resultado do ciclo: NAO tem ciclo hamiltoniano (busca completa)."
                    : "Resultado do ciclo: INCONCLUSIVO (a busca passou do limite de " + limiteBusca + " passos).");
            if (r == 1) { System.out.println("O grafo tambem tem caminho hamiltoniano."); return; }
        } else {
            System.out.println("Resultado do ciclo: NAO admite ciclo hamiltoniano.");
        }

        if (refutaCaminho) {
            System.out.println("Resultado do caminho: NAO admite caminho hamiltoniano.");
        } else {
            int r = buscaHamilton(false);
            System.out.println(r == 1 ? "Resultado do caminho: TEM caminho hamiltoniano."
                    : r == 0 ? "Resultado do caminho: NAO tem caminho hamiltoniano (busca completa)."
                    : "Resultado do caminho: INCONCLUSIVO (a busca passou do limite de " + limiteBusca + " passos).");
        }
    }

    // ---------- (4) coloracao ----------
    private boolean vizinhos(int i, int j) {
        return i != j && (existe(i, j) || existe(j, i));
    }

    // Colore com DSATUR (guloso por grau de saturacao). Devolve cor[i] de 0 em diante.
    private int[] coloreDsatur() {
        int cor[] = new int[n];
        java.util.Arrays.fill(cor, -1);
        for (int passo = 0; passo < n; passo++) {
            int melhor = -1, melhorSat = -1, melhorGrau = -1;
            for (int i = 0; i < n; i++) {
                if (cor[i] != -1) continue;
                java.util.Set<Integer> cores = new java.util.HashSet<>();
                int grau = 0;
                for (int j = 0; j < n; j++) {
                    if (!vizinhos(i, j)) continue;
                    grau++;
                    if (cor[j] != -1) cores.add(cor[j]);
                }
                if (cores.size() > melhorSat || (cores.size() == melhorSat && grau > melhorGrau)) {
                    melhor = i; melhorSat = cores.size(); melhorGrau = grau;
                }
            }
            boolean usada[] = new boolean[n + 1];
            for (int j = 0; j < n; j++) if (vizinhos(melhor, j) && cor[j] != -1) usada[cor[j]] = true;
            int c = 0;
            while (usada[c]) c++;
            cor[melhor] = c;
        }
        return cor;
    }

    // Clique maxima (Bron-Kerbosch com pivo); serve de limite inferior para o numero cromatico
    private List<Integer> melhorClique;
    private long nosClique;

    private void bronKerbosch(List<Integer> r, List<Integer> p, List<Integer> x) {
        if (++nosClique > 2_000_000L) return;
        if (p.isEmpty() && x.isEmpty()) {
            if (r.size() > melhorClique.size()) melhorClique = new ArrayList<>(r);
            return;
        }
        if (r.size() + p.size() <= melhorClique.size()) return;
        int pivo = -1, melhorCont = -1;
        List<Integer> uniao = new ArrayList<>(p);
        uniao.addAll(x);
        for (int u : uniao) {
            int cont = 0;
            for (int v : p) if (vizinhos(u, v)) cont++;
            if (cont > melhorCont) { melhorCont = cont; pivo = u; }
        }
        List<Integer> candidatos = new ArrayList<>();
        for (int v : p) if (!vizinhos(pivo, v)) candidatos.add(v);
        for (int v : candidatos) {
            List<Integer> r2 = new ArrayList<>(r); r2.add(v);
            List<Integer> p2 = new ArrayList<>(), x2 = new ArrayList<>();
            for (int w : p) if (vizinhos(v, w)) p2.add(w);
            for (int w : x) if (vizinhos(v, w)) x2.add(w);
            bronKerbosch(r2, p2, x2);
            p.remove(Integer.valueOf(v));
            x.add(v);
        }
    }

    private void caracColoracao() {
        System.out.println("\n--- 4. Coloracao (particoes sem vertices vizinhos) ---");
        int cor[] = coloreDsatur();
        int ub = 0;
        for (int c : cor) ub = Math.max(ub, c + 1);

        boolean valida = true;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                if (vizinhos(i, j) && cor[i] == cor[j]) valida = false;

        melhorClique = new ArrayList<>();
        nosClique = 0;
        List<Integer> p = new ArrayList<>();
        for (int i = 0; i < n; i++) p.add(i);
        bronKerbosch(new ArrayList<>(), p, new ArrayList<>());
        int lb = melhorClique.size();

        System.out.println("Coloracao DSATUR usa " + ub + " cores (limite superior). Verificacao: " +
                (valida ? "nenhum par de vizinhos com a mesma cor." : "ERRO, ha vizinhos com a mesma cor!"));
        System.out.println("Maior clique encontrada: " + lb + " vertices (limite inferior): " +
                listaRotulos(melhorClique));
        if (lb == ub)
            System.out.println("Numero cromatico = " + ub + " (limite inferior = limite superior).");
        else
            System.out.println("Numero cromatico entre " + lb + " e " + ub + ".");

        for (int c = 0; c < ub; c++) {
            List<Integer> classe = new ArrayList<>();
            for (int i = 0; i < n; i++) if (cor[i] == c) classe.add(i);
            System.out.println("Particao " + (c + 1) + " (" + classe.size() + " vertice(s)): " + listaRotulos(classe));
        }
    }

    // ------------------------------------------------------------------
    public int getN() { return n; }
    public int getM() { return m; }
    public int getTipoGrafo() { return tipoGrafo; }
}