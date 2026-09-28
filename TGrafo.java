
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

// Gabriel Teixeira Bolonha (10426937)
// Geovana Bomfim Rodrigues (10410514)
// Rodrigo Daiske Uehara (10440295)
// Yating Zheng (10439511)
// javac *.java
//  java Main
public class TGrafo {

    private int n;                 // quantidade de vertices
    private int m;                 // quantidade de arestas
    private float adj[][];         // matriz de adjacencia

    private int rotulo[];          // rotulo[posicao] = identificador
    private String apelido[];      // apelido[posicao] = localidade/nome
    private float pesoVertice[];   // peso do vertice

    private int tipoGrafo;         // tipo do grafo (0 a 7)


    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public TGrafo() {
        this.n = 0;
        this.m = 0;
        this.adj = new float[0][0];
        this.rotulo = new int[0];
        this.apelido = new String[0];
        this.pesoVertice = new float[0];

        // Tipo 3:
        // nao orientado, com peso nos vertices e arestas
        this.tipoGrafo = 3;
    }


    // ============================================================
    // METODOS AUXILIARES SOBRE O TIPO DO GRAFO
    // ============================================================

    // Tipos:
    //
    // 0 - nao orientado sem peso
    // 1 - nao orientado com peso no vertice
    // 2 - nao orientado com peso na aresta
    // 3 - nao orientado com peso nos vertices e arestas
    // 4 - orientado sem peso
    // 5 - orientado com peso no vertice
    // 6 - orientado com peso na aresta
    // 7 - orientado com peso nos vertices e arestas

    private boolean ehOrientado() {
        return tipoGrafo >= 4;
    }

    private boolean temPesoVertice() {
        return tipoGrafo == 1 ||
               tipoGrafo == 3 ||
               tipoGrafo == 5 ||
               tipoGrafo == 7;
    }

    private boolean temPesoAresta() {
        return tipoGrafo == 2 ||
               tipoGrafo == 3 ||
               tipoGrafo == 6 ||
               tipoGrafo == 7;
    }


    // ============================================================
    // AUXILIAR: ENCONTRA POSICAO PELO ROTULO
    // ============================================================

    private int posicaoPorRotulo(int rot) {
        for (int i = 0; i < n; i++) {
            if (rotulo[i] == rot)
                return i;
        }

        return -1;
    }


    // ============================================================
    // INSERE VERTICE
    // ============================================================

    public void insereVertice(int rot, String ape, float pesoVert) {

        if (posicaoPorRotulo(rot) != -1) {
            System.out.println(
                "Erro: ja existe um vertice com rotulo " + rot + "."
            );
            return;
        }

        int novoN = n + 1;

        float novaAdj[][] = new float[novoN][novoN];

        for (int i = 0; i < novoN; i++) {
            for (int j = 0; j < novoN; j++) {
                novaAdj[i][j] = Float.POSITIVE_INFINITY;
            }
        }

        // Copia matriz antiga
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                novaAdj[i][j] = adj[i][j];
            }
        }

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

        if (temPesoVertice())
            novoPeso[n] = pesoVert;
        else
            novoPeso[n] = 0;

        adj = novaAdj;
        rotulo = novoRotulo;
        apelido = novoApelido;
        pesoVertice = novoPeso;

        n = novoN;

        System.out.println(
            "Vertice " + rot + " (" + ape + ") inserido com sucesso."
        );
    }


    // ============================================================
    // REMOVE VERTICE
    // ============================================================

    public void removeVertice(int rot) {

        int v = posicaoPorRotulo(rot);

        if (v == -1) {
            System.out.println(
                "Erro: rotulo de vertice inexistente."
            );
            return;
        }

        int novoN = n - 1;

        float novaAdj[][] = new float[novoN][novoN];

        for (int i = 0; i < novoN; i++) {
            for (int j = 0; j < novoN; j++) {
                novaAdj[i][j] = Float.POSITIVE_INFINITY;
            }
        }

        int li = 0;

        for (int i = 0; i < n; i++) {

            if (i == v)
                continue;

            int lj = 0;

            for (int j = 0; j < n; j++) {

                if (j == v)
                    continue;

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

            if (i == v)
                continue;

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

        // Recalcula quantidade de arestas
        m = 0;

        for (int i = 0; i < n; i++) {

            int inicioJ;

            if (ehOrientado())
                inicioJ = 0;
            else
                inicioJ = i;

            for (int j = inicioJ; j < n; j++) {

                if (adj[i][j] != Float.POSITIVE_INFINITY)
                    m++;
            }
        }

        System.out.println(
            "Vertice " + rot +
            " removido com sucesso (junto com suas arestas)."
        );
    }


    // ============================================================
    // INSERE ARESTA
    // ============================================================

    public void insereAresta(int rotV, int rotW, float peso) {

        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);

        if (v == -1 || w == -1) {
            System.out.println(
                "Erro: rotulo de vertice inexistente."
            );
            return;
        }

        // Se o tipo nao possui peso na aresta,
        // usamos 1 apenas para representar a existencia da aresta.
        if (!temPesoAresta())
            peso = 1;

        if (adj[v][w] == Float.POSITIVE_INFINITY)
            m++;

        adj[v][w] = peso;

        // Se nao for orientado, cria a aresta nos dois sentidos
        if (!ehOrientado())
            adj[w][v] = peso;

        if (ehOrientado()) {
            System.out.println(
                "Aresta " + rotV + " -> " + rotW +
                " inserida."
            );
        }
        else {
            System.out.println(
                "Aresta " + rotV + " - " + rotW +
                " inserida."
            );
        }
    }


    // ============================================================
    // REMOVE ARESTA
    // ============================================================

    public void removeAresta(int rotV, int rotW) {

        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);

        if (v == -1 || w == -1) {
            System.out.println(
                "Erro: rotulo de vertice inexistente."
            );
            return;
        }

        if (adj[v][w] != Float.POSITIVE_INFINITY) {

            adj[v][w] = Float.POSITIVE_INFINITY;

            if (!ehOrientado())
                adj[w][v] = Float.POSITIVE_INFINITY;

            m--;

            if (ehOrientado()) {
                System.out.println(
                    "Aresta " + rotV + " -> " + rotW +
                    " removida."
                );
            }
            else {
                System.out.println(
                    "Aresta " + rotV + " - " + rotW +
                    " removida."
                );
            }

        }
        else {
            System.out.println(
                "Erro: aresta inexistente."
            );
        }
    }


    // ============================================================
    // PADROES DE LEITURA
    // ============================================================

    private static final java.util.regex.Pattern PADRAO_VERTICE =
        java.util.regex.Pattern.compile(
            "^\\s*(-?\\d+)\\s+\"([^\"]*)\"\\s+\"([^\"]*)\"\\s*$"
        );

    private static final java.util.regex.Pattern PADRAO_ARESTA =
        java.util.regex.Pattern.compile(
            "^\\s*(-?\\d+)\\s+(-?\\d+)\\s+(-?[\\d.,]+)\\s*$"
        );


    // ============================================================
    // PROXIMA LINHA VALIDA
    // ============================================================

    private String proximaLinhaValida(
        java.io.BufferedReader br
    ) throws IOException {

        String linha;

        while ((linha = br.readLine()) != null) {

            if (!linha.trim().isEmpty())
                return linha;
        }

        return null;
    }


    // ============================================================
    // LE ARQUIVO
    // ============================================================

    public void lerArquivo(String nomeArq) throws IOException {

        java.io.BufferedReader br =
            new java.io.BufferedReader(
                new java.io.InputStreamReader(
                    new java.io.FileInputStream(nomeArq),
                    java.nio.charset.StandardCharsets.UTF_8
                )
            );


        // Primeira linha = tipo do grafo
        this.tipoGrafo =
            Integer.parseInt(
                proximaLinhaValida(br).trim()
            );


        // Segunda linha = quantidade de vertices
        int novoN =
            Integer.parseInt(
                proximaLinhaValida(br).trim()
            );


        adj = new float[novoN][novoN];

        for (int i = 0; i < novoN; i++) {
            for (int j = 0; j < novoN; j++) {
                adj[i][j] =
                    Float.POSITIVE_INFINITY;
            }
        }


        rotulo = new int[novoN];
        apelido = new String[novoN];
        pesoVertice = new float[novoN];

        n = novoN;
        m = 0;


        // Leitura dos vertices
        for (int i = 0; i < novoN; i++) {

            String linha =
                proximaLinhaValida(br);

            java.util.regex.Matcher mat =
                PADRAO_VERTICE.matcher(linha);

            if (!mat.matches()) {

                br.close();

                throw new IOException(
                    "Linha de vertice em formato invalido: \"" +
                    linha + "\""
                );
            }

            rotulo[i] =
                Integer.parseInt(mat.group(1));

            apelido[i] =
                mat.group(2);

            pesoVertice[i] =
                Float.parseFloat(
                    mat.group(3).replace(",", ".")
                );
        }


        // Quantidade de arestas
        int totalArestas =
            Integer.parseInt(
                proximaLinhaValida(br).trim()
            );


        // Leitura das arestas
        for (int i = 0; i < totalArestas; i++) {

            String linha =
                proximaLinhaValida(br);

            java.util.regex.Matcher mat =
                PADRAO_ARESTA.matcher(linha);

            if (!mat.matches()) {

                br.close();

                throw new IOException(
                    "Linha de aresta em formato invalido: \"" +
                    linha + "\""
                );
            }


            int rotV =
                Integer.parseInt(mat.group(1));

            int rotW =
                Integer.parseInt(mat.group(2));

            float peso =
                Float.parseFloat(
                    mat.group(3).replace(",", ".")
                );


            int v = posicaoPorRotulo(rotV);
            int w = posicaoPorRotulo(rotW);


            if (v == -1 || w == -1) {

                System.out.println(
                    "Aviso: aresta " +
                    rotV + " - " + rotW +
                    " ignorada (rotulo de vertice inexistente)."
                );

                continue;
            }


            if (!temPesoAresta())
                peso = 1;


            if (adj[v][w] ==
                Float.POSITIVE_INFINITY) {

                adj[v][w] = peso;

                // Somente grafos nao orientados
                // possuem a aresta nos dois sentidos
                if (!ehOrientado())
                    adj[w][v] = peso;

                m++;
            }
        }


        br.close();


        System.out.println(
            "Arquivo \"" + nomeArq +
            "\" lido com sucesso."
        );

        System.out.println(
            "Tipo " + tipoGrafo +
            " | " + n +
            " vertice(s) | " +
            m + " aresta(s)."
        );
    }


    // ============================================================
    // GRAVA ARQUIVO
    // ============================================================

    public void gravarArquivo(
        String nomeArq
    ) throws IOException {

        PrintWriter pw =
            new PrintWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(nomeArq),
                    java.nio.charset.StandardCharsets.UTF_8
                )
            );


        // Tipo
        pw.println(tipoGrafo);

        // Quantidade de vertices
        pw.println(n);


        // Vertices
        for (int i = 0; i < n; i++) {

            float peso;

            if (temPesoVertice())
                peso = pesoVertice[i];
            else
                peso = 0;

            pw.println(
                rotulo[i] +
                " \"" +
                apelido[i] +
                "\" \"" +
                peso +
                "\""
            );
        }


        StringBuilder linhasArestas =
            new StringBuilder();

        int totalArestas = 0;


        for (int i = 0; i < n; i++) {

            int inicioJ;

            // Grafo orientado:
            // precisamos analisar a matriz inteira.
            //
            // Grafo nao orientado:
            // basta a parte superior.
            if (ehOrientado())
                inicioJ = 0;
            else
                inicioJ = i;


            for (int j = inicioJ; j < n; j++) {

                if (adj[i][j] !=
                    Float.POSITIVE_INFINITY) {

                    float peso;

                    if (temPesoAresta())
                        peso = adj[i][j];
                    else
                        peso = 1;


                    linhasArestas
                        .append(rotulo[i])
                        .append(" ")
                        .append(rotulo[j])
                        .append(" ")
                        .append(peso)
                        .append("\n");

                    totalArestas++;
                }
            }
        }


        // Quantidade de arestas
        pw.println(totalArestas);

        // Arestas
        pw.print(linhasArestas);

        pw.close();


        System.out.println(
            "Grafo gravado com sucesso em \"" +
            nomeArq + "\"."
        );
    }


    // ============================================================
    // MOSTRA CONTEUDO
    // ============================================================

    public void mostrarConteudo() {

        System.out.println(
            "\n===================== CONTEUDO DO GRAFO ====================="
        );

        System.out.println(
            "Tipo do grafo : " + tipoGrafo
        );

        if (ehOrientado())
            System.out.println(
                "Orientacao    : Orientado"
            );
        else
            System.out.println(
                "Orientacao    : Nao orientado"
            );

        System.out.println(
            "Peso vertice  : " +
            (temPesoVertice() ? "Sim" : "Nao")
        );

        System.out.println(
            "Peso aresta   : " +
            (temPesoAresta() ? "Sim" : "Nao")
        );

        System.out.println(
            "Vertices (n)  : " + n
        );

        System.out.println(
            "Arestas  (m)  : " + m
        );


        System.out.println("\n-- Vertices --");

        System.out.printf(
            "%-8s %-30s %-10s%n",
            "Rotulo",
            "Apelido",
            "Peso"
        );


        for (int i = 0; i < n; i++) {

            System.out.printf(
                "%-8d %-30s %-10.2f%n",
                rotulo[i],
                apelido[i],
                pesoVertice[i]
            );
        }


        System.out.println("\n-- Arestas --");

        System.out.printf(
            "%-10s %-10s %-10s%n",
            "Vertice 1",
            "Vertice 2",
            "Peso"
        );


        for (int i = 0; i < n; i++) {

            int inicioJ;

            if (ehOrientado())
                inicioJ = 0;
            else
                inicioJ = i;


            for (int j = inicioJ; j < n; j++) {

                if (adj[i][j] !=
                    Float.POSITIVE_INFINITY) {

                    System.out.printf(
                        "%-10d %-10d %-10.2f%n",
                        rotulo[i],
                        rotulo[j],
                        adj[i][j]
                    );
                }
            }
        }


        System.out.println(
            "===============================================================\n"
        );
    }


    // ============================================================
    // MOSTRA MATRIZ DE ADJACENCIA
    // ============================================================

    public void mostrarGrafo() {

        System.out.println("\nn: " + n);
        System.out.println("m: " + m);


        for (int i = 0; i < n; i++) {

            System.out.println();

            for (int j = 0; j < n; j++) {

                if (adj[i][j] !=
                    Float.POSITIVE_INFINITY) {

                    System.out.print(
                        "Adj[" +
                        rotulo[i] +
                        "," +
                        rotulo[j] +
                        "]= " +
                        adj[i][j] +
                        " "
                    );
                }
                else {

                    System.out.print(
                        "Adj[" +
                        rotulo[i] +
                        "," +
                        rotulo[j] +
                        "]= inf "
                    );
                }
            }
        }


        System.out.println(
            "\n\nfim da impressao do grafo."
        );
    }


    // ============================================================
    // DFS DIRECIONADA
    // ============================================================

    private int dfsComPilha(
        int origem,
        boolean visitado[]
    ) {

        Pilha pilha = new Pilha(n);

        pilha.push(origem);

        visitado[origem] = true;

        int totalVisitados = 1;


        while (!pilha.isEmpty()) {

            int atual = pilha.pop();


            for (int j = 0; j < n; j++) {

                if (adj[atual][j] !=
                    Float.POSITIVE_INFINITY
                    &&
                    !visitado[j]) {

                    visitado[j] = true;

                    totalVisitados++;

                    pilha.push(j);
                }
            }
        }


        return totalVisitados;
    }


    // ============================================================
    // DFS IGNORANDO A DIRECAO
    // ============================================================

    private int dfsComPilhaNaoDirigido(
        int origem,
        boolean visitado[]
    ) {

        Pilha pilha = new Pilha(n);

        pilha.push(origem);

        visitado[origem] = true;

        int totalVisitados = 1;


        while (!pilha.isEmpty()) {

            int atual = pilha.pop();


            for (int j = 0; j < n; j++) {

                boolean existeArestaEmAlgumSentido =
                    adj[atual][j] !=
                    Float.POSITIVE_INFINITY
                    ||
                    adj[j][atual] !=
                    Float.POSITIVE_INFINITY;


                if (existeArestaEmAlgumSentido
                    &&
                    !visitado[j]) {

                    visitado[j] = true;

                    totalVisitados++;

                    pilha.push(j);
                }
            }
        }


        return totalVisitados;
    }


    // ============================================================
    // VERIFICA SE U ALCANCA V
    // ============================================================

    private boolean alcanca(
        int u,
        int v
    ) {

        boolean visitado[] =
            new boolean[n];

        Pilha pilha =
            new Pilha(n);


        pilha.push(u);

        visitado[u] = true;


        while (!pilha.isEmpty()) {

            int atual = pilha.pop();


            if (atual == v)
                return true;


            for (int j = 0; j < n; j++) {

                if (adj[atual][j] !=
                    Float.POSITIVE_INFINITY
                    &&
                    !visitado[j]) {

                    visitado[j] = true;

                    pilha.push(j);
                }
            }
        }


        return false;
    }


    // ============================================================
    // CATEGORIA DE CONEXIDADE
    //
    // C3 = fortemente conexo
    // C2 = unilateralmente conexo
    // C1 = fracamente conexo
    // C0 = desconexo
    // ============================================================

    public int categoriaConexidade() {

        if (!ehOrientado()) {

            if (ehConexo())
                return 3;

            return 0;
        }


        // --------------------------------------------------------
        // C3: fortemente conexo
        // --------------------------------------------------------

        boolean fortementeConexo = true;


        for (int i = 0;
             i < n && fortementeConexo;
             i++) {

            boolean visitado[] =
                new boolean[n];


            if (dfsComPilha(i, visitado) < n)
                fortementeConexo = false;
        }


        if (fortementeConexo)
            return 3;


        // --------------------------------------------------------
        // C2: unilateralmente conexo
        // --------------------------------------------------------

        boolean unilateralmenteConexo = true;


        for (int i = 0;
             i < n && unilateralmenteConexo;
             i++) {

            for (int j = i + 1;
                 j < n;
                 j++) {

                if (!alcanca(i, j)
                    &&
                    !alcanca(j, i)) {

                    unilateralmenteConexo = false;

                    break;
                }
            }
        }


        if (unilateralmenteConexo)
            return 2;


        // --------------------------------------------------------
        // C1: fracamente conexo
        // --------------------------------------------------------

        if (n > 0) {

            boolean visitado[] =
                new boolean[n];


            if (dfsComPilhaNaoDirigido(
                    0,
                    visitado
                ) == n) {

                return 1;
            }
        }


        // --------------------------------------------------------
        // C0: desconexo
        // --------------------------------------------------------

        return 0;
    }


    // ============================================================
    // FCONEX
    //
    // Encontra as componentes fortemente conexas.
    //
    // A ideia utilizada aqui é:
    //
    // 1. Para cada vertice ainda nao pertencente a uma
    //    componente, verifica quais vertices sao alcancaveis
    //    a partir dele.
    //
    // 2. Verifica quais desses vertices tambem conseguem
    //    alcancar a origem.
    //
    // 3. Esses vertices formam uma componente fortemente conexa.
    // ============================================================

    public int[][] FCONEX() {

        boolean pertencente[] =
            new boolean[n];

        int componentes[][] =
            new int[n][n];

        int tamanhoComponente[] =
            new int[n];

        int qtdComponentes = 0;


        for (int origem = 0;
             origem < n;
             origem++) {

            if (pertencente[origem])
                continue;


            boolean alcancaOrigem[] =
                new boolean[n];

            boolean alcancadoPorOrigem[] =
                new boolean[n];


            // Vertices que a origem consegue alcancar
            dfsComPilha(
                origem,
                alcancadoPorOrigem
            );


            // Vertices que conseguem chegar na origem
            for (int i = 0; i < n; i++) {

                if (!pertencente[i]
                    &&
                    alcanca(i, origem)) {

                    alcancaOrigem[i] = true;
                }
            }


            // Um vertice pertence a mesma componente
            // se pode ser alcancado pela origem e tambem
            // consegue alcancar a origem.
            for (int i = 0; i < n; i++) {

                if (!pertencente[i]
                    &&
                    alcancadoPorOrigem[i]
                    &&
                    alcancaOrigem[i]) {

                    componentes[qtdComponentes]
                                [tamanhoComponente[qtdComponentes]]
                        = i;

                    tamanhoComponente[qtdComponentes]++;

                    pertencente[i] = true;
                }
            }


            qtdComponentes++;
        }


        // Cria uma matriz exatamente com a quantidade
        // de componentes encontradas
        int resultado[][] =
            new int[qtdComponentes][];


        for (int i = 0;
             i < qtdComponentes;
             i++) {

            resultado[i] =
                new int[tamanhoComponente[i]];


            for (int j = 0;
                 j < tamanhoComponente[i];
                 j++) {

                resultado[i][j] =
                    componentes[i][j];
            }
        }


        return resultado;
    }


    // ============================================================
    // MOSTRA AS COMPONENTES FORTEMENTE CONEXAS
    // ============================================================

    public void mostrarFCONEX() {

        if (!ehOrientado()) {

            System.out.println(
                "FCONEX e utilizado para grafos orientados."
            );

            return;
        }


        int componentes[][] =
            FCONEX();


        System.out.println(
            "\n===== COMPONENTES FORTEMENTE CONEXAS ====="
        );


        for (int i = 0;
             i < componentes.length;
             i++) {

            System.out.print(
                "C" + (i + 1) + " = { "
            );


            for (int j = 0;
                 j < componentes[i].length;
                 j++) {

                int pos =
                    componentes[i][j];


                System.out.print(
                    rotulo[pos]
                );


                if (j <
                    componentes[i].length - 1) {

                    System.out.print(", ");
                }
            }


            System.out.println(" }");
        }


        System.out.println(
            "===========================================\n"
        );
    }


    // ============================================================
    // MOSTRA O GRAFO REDUZIDO
    // ============================================================

    public void mostrarGrafoReduzido() {

        if (!ehOrientado()) {

            System.out.println(
                "Grafo reduzido e utilizado para grafos orientados."
            );

            return;
        }


        int componentes[][] =
            FCONEX();


        int quantidade =
            componentes.length;


        System.out.println(
            "\n========== GRAFO REDUZIDO =========="
        );


        // Mostra os vertices do grafo reduzido
        System.out.println(
            "\nVertices:"
        );


        for (int i = 0;
             i < quantidade;
             i++) {

            System.out.print(
                "C" + (i + 1) +
                " = { "
            );


            for (int j = 0;
                 j < componentes[i].length;
                 j++) {

                int pos =
                    componentes[i][j];


                System.out.print(
                    rotulo[pos]
                );


                if (j <
                    componentes[i].length - 1) {

                    System.out.print(", ");
                }
            }


            System.out.println(" }");
        }


        // Matriz do grafo reduzido
        boolean reduzido[][] =
            new boolean[quantidade][quantidade];


        // Verifica se existe uma aresta entre
        // duas componentes diferentes
        for (int i = 0;
             i < quantidade;
             i++) {

            for (int j = 0;
                 j < componentes[i].length;
                 j++) {

                int origem =
                    componentes[i][j];


                for (int destino = 0;
                     destino < n;
                     destino++) {

                    if (adj[origem][destino] ==
                        Float.POSITIVE_INFINITY) {

                        continue;
                    }


                    // Descobre a qual componente
                    // pertence o destino
                    int componenteDestino = -1;


                    for (int k = 0;
                         k < quantidade;
                         k++) {

                        for (int l = 0;
                             l < componentes[k].length;
                             l++) {

                            if (componentes[k][l]
                                == destino) {

                                componenteDestino = k;

                                break;
                            }
                        }


                        if (componenteDestino != -1)
                            break;
                    }


                    if (componenteDestino != -1
                        &&
                        componenteDestino != i) {

                        reduzido[i]
                                [componenteDestino]
                            = true;
                    }
                }
            }
        }


        System.out.println(
            "\nArestas:"
        );


        boolean possuiArestas = false;


        for (int i = 0;
             i < quantidade;
             i++) {

            for (int j = 0;
                 j < quantidade;
                 j++) {

                if (reduzido[i][j]) {

                    System.out.println(
                        "C" + (i + 1) +
                        " -> C" + (j + 1)
                    );

                    possuiArestas = true;
                }
            }
        }


        if (!possuiArestas) {

            System.out.println(
                "Nenhuma aresta entre componentes."
            );
        }


        System.out.println(
            "\nMatriz de adjacencia reduzida:"
        );


        for (int i = 0;
             i < quantidade;
             i++) {

            for (int j = 0;
                 j < quantidade;
                 j++) {

                if (reduzido[i][j])
                    System.out.print("1 ");
                else
                    System.out.print("0 ");
            }

            System.out.println();
        }


        System.out.println(
            "====================================\n"
        );
    }


    // ============================================================
    // MOSTRA CONEXIDADE
    // ============================================================

    public void mostrarConexidade() {

        if (n == 0) {

            System.out.println(
                "Grafo vazio."
            );

            return;
        }


        if (!ehOrientado()) {

            if (ehConexo()) {

                System.out.println(
                    "O grafo e conexo."
                );
            }
            else {

                System.out.println(
                    "O grafo e desconexo."
                );
            }

            return;
        }


        int categoria =
            categoriaConexidade();


        System.out.println(
            "Categoria de conexidade: C" +
            categoria
        );


        if (categoria == 3) {

            System.out.println(
                "O grafo e fortemente conexo."
            );
        }
        else if (categoria == 2) {

            System.out.println(
                "O grafo e unilateralmente conexo."
            );
        }
        else if (categoria == 1) {

            System.out.println(
                "O grafo e fracamente conexo."
            );
        }
        else {

            System.out.println(
                "O grafo e desconexo."
            );
        }


        // Para grafos orientados, mostra tambem
        // as componentes fortemente conexas
        // e o grafo reduzido.
        mostrarFCONEX();

        mostrarGrafoReduzido();
    }


    // ============================================================
    // VERIFICA CONEXIDADE DE GRAFO NAO ORIENTADO
    // ============================================================

    public boolean ehConexo() {

        if (n == 0)
            return false;


        boolean visitado[] =
            new boolean[n];


        int alcancados =
            dfsComPilha(
                0,
                visitado
            );


        return alcancados == n;
    }


    // ============================================================
    // GETTERS
    // ============================================================

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    public int getTipoGrafo() {
        return tipoGrafo;
    }
}

