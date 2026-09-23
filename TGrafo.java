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

    // Tipo de grafo fixo neste projeto: 7 = orientado, peso no vértice e na aresta
    public static final int TIPO_ORIENTADO_PESO_VERTICE_ARESTA = 7;

    private int n;                // quantidade de vértices
    private int m;                // quantidade de arestas
    private float adj[][];        // matriz de adjacência (peso da aresta, ou infinito se não existe)

    private int rotulo[];         // rotulo[posicao]      = identificador do vértice
    private String apelido[];     // apelido[posicao]     = localidade/nome do vértice
    private float pesoVertice[];  // pesoVertice[posicao] = peso do vértice

    private int tipoGrafo;        // tipo do grafo (fixo em 7 neste projeto)

    // Construtor: grafo vazio (nenhum vértice ainda), pronto para ler um
    // arquivo ou para receber vértices inseridos manualmente pelo menu
    public TGrafo() {
        this.n = 0;
        this.m = 0;
        this.adj = new float[0][0];
        this.rotulo = new int[0];
        this.apelido = new String[0];
        this.pesoVertice = new float[0];
        this.tipoGrafo = TIPO_ORIENTADO_PESO_VERTICE_ARESTA;
    }

    // --------------------------------------------------------------------
    // Auxiliar interno: descobre a POSIÇÃO (0..n-1, usada pela matriz) de
    // um vértice a partir do seu RÓTULO (o identificador que o usuário vê
    // e digita, escolhido livremente — não precisa ser sequencial nem
    // começar em 0). Retorna -1 se o rótulo não existir.
    // --------------------------------------------------------------------
    private int posicaoPorRotulo(int rot) {
        for (int i = 0; i < n; i++) {
            if (rotulo[i] == rot) return i;
        }
        return -1;
    }

    // ----------------------------------------------------------------------
    // Insere um novo vértice, isolado (sem arestas), com rótulo/apelido/peso.
    // Cresce a matriz de n para n+1 e os vetores de metadados junto.
    // ----------------------------------------------------------------------
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

    // ----------------------------------------------------------------------
    // Remove um vértice (pelo rótulo), junto com seu apelido/peso e TODAS
    // as arestas associadas a ele (linha e coluna correspondentes na matriz)
    // ----------------------------------------------------------------------
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

        // recalcula m contando as arestas que restaram
        m = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (adj[i][j] != Float.POSITIVE_INFINITY) m++;

        System.out.println("Vertice " + rot + " removido com sucesso (junto com suas arestas).");
    }

    // ----------------------------------------------------------------------
    // Insere uma aresta v->w com peso, informando os RÓTULOS dos vértices
    // ----------------------------------------------------------------------
    public void insereAresta(int rotV, int rotW, float peso) {
        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);
        if (v == -1 || w == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return;
        }
        if (adj[v][w] == Float.POSITIVE_INFINITY) {
            adj[v][w] = peso;
            m++;
        }
        System.out.println("Aresta " + rotV + " -> " + rotW + " (peso " + peso + ") inserida.");
    }

    // ----------------------------------------------------------------------
    // Remove a aresta v->w (informando os RÓTULOS dos vértices)
    // ----------------------------------------------------------------------
    public void removeAresta(int rotV, int rotW) {
        int v = posicaoPorRotulo(rotV);
        int w = posicaoPorRotulo(rotW);
        if (v == -1 || w == -1) {
            System.out.println("Erro: rotulo de vertice inexistente.");
            return;
        }
        if (adj[v][w] != Float.POSITIVE_INFINITY) {
            adj[v][w] = Float.POSITIVE_INFINITY;
            m--;
        }
        System.out.println("Aresta " + rotV + " -> " + rotW + " removida.");
    }

    // ----------------------------------------------------------------------
    // Lê o arquivo grafo.txt e monta o grafo (matriz + metadados)
    //
    // Formato esperado (Tipo do Grafo = 7):
    //   linha 1: tipo do grafo (7)
    //   linha 2: n (quantidade de vértices)
    //   próximas n linhas: rotulo apelido peso_do_vertice
    //   próxima linha: m (quantidade de arestas)
    //   próximas m linhas: rotulo_origem rotulo_destino peso_da_aresta
    // ----------------------------------------------------------------------
    public void lerArquivo(String nomeArq) throws IOException {
        Scanner sc = new Scanner(new File(nomeArq));

        tipoGrafo = sc.nextInt();
        int novoN = sc.nextInt();

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
            rotulo[i] = sc.nextInt();
            apelido[i] = sc.next();
            pesoVertice[i] = sc.nextFloat();
        }

        int totalArestas = sc.nextInt();
        for (int i = 0; i < totalArestas; i++) {
            int rotV = sc.nextInt();
            int rotW = sc.nextInt();
            float peso = sc.nextFloat();

            int v = posicaoPorRotulo(rotV);
            int w = posicaoPorRotulo(rotW);
            if (v == -1 || w == -1) {
                System.out.println("Aviso: aresta " + rotV + " -> " + rotW +
                        " ignorada (rotulo de vertice inexistente no arquivo).");
                continue;
            }
            if (adj[v][w] == Float.POSITIVE_INFINITY) {
                adj[v][w] = peso;
                m++;
            }
        }

        sc.close();
        System.out.println("Arquivo \"" + nomeArq + "\" lido com sucesso: " +
                n + " vertice(s), " + m + " aresta(s).");
    }

    // ----------------------------------------------------------------------
    // Grava o grafo atual (da memória) no arquivo grafo.txt, no MESMO
    // formato usado na leitura
    // ----------------------------------------------------------------------
    public void gravarArquivo(String nomeArq) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(nomeArq));

        pw.println(tipoGrafo);
        pw.println(n);
        for (int i = 0; i < n; i++) {
            pw.println(rotulo[i] + " " + apelido[i] + " " + pesoVertice[i]);
        }

        StringBuilder linhasArestas = new StringBuilder();
        int totalArestas = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
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

    // ----------------------------------------------------------------------
    // Mostra o conteúdo atual (vértices e arestas) em formato de tabela
    // ----------------------------------------------------------------------
    public void mostrarConteudo() {
        System.out.println("\n===================== CONTEUDO DO GRAFO =====================");
        System.out.println("Tipo do grafo : " + tipoGrafo + " (orientado, peso no vertice e na aresta)");
        System.out.println("Vertices (n)  : " + n);
        System.out.println("Arestas  (m)  : " + m);

        System.out.println("\n-- Vertices --");
        System.out.printf("%-8s %-20s %-10s%n", "Rotulo", "Apelido", "Peso");
        for (int i = 0; i < n; i++) {
            System.out.printf("%-8d %-20s %-10.2f%n", rotulo[i], apelido[i], pesoVertice[i]);
        }

        System.out.println("\n-- Arestas --");
        System.out.printf("%-10s %-10s %-10s%n", "Origem", "Destino", "Peso");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adj[i][j] != Float.POSITIVE_INFINITY) {
                    System.out.printf("%-10d %-10d %-10.2f%n", rotulo[i], rotulo[j], adj[i][j]);
                }
            }
        }
        System.out.println("===============================================================\n");
    }

    // ----------------------------------------------------------------------
    // Mostra o grafo como matriz de adjacência
    // ----------------------------------------------------------------------
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

    // ----------------------------------------------------------------------
    // Auxiliar de conexidade: DFS a partir de "origem" usando a Pilha,
    // marcando em "visitado" todos os vértices alcançados. Retorna quantos
    // vértices foram visitados no total (respeitando o sentido das arestas).
    // ----------------------------------------------------------------------
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

    // Auxiliar: mesma DFS, mas ignorando o sentido das arestas (usada para
    // testar conexidade fraca - C1)
    private int dfsComPilhaNaoDirigido(int origem, boolean visitado[]) {
        Pilha pilha = new Pilha(n);
        pilha.push(origem);
        visitado[origem] = true;
        int totalVisitados = 1;

        while (!pilha.isEmpty()) {
            int atual = pilha.pop();
            for (int j = 0; j < n; j++) {
                boolean existeArestaEmAlgumSentido =
                        adj[atual][j] != Float.POSITIVE_INFINITY ||
                        adj[j][atual] != Float.POSITIVE_INFINITY;
                if (existeArestaEmAlgumSentido && !visitado[j]) {
                    visitado[j] = true;
                    totalVisitados++;
                    pilha.push(j);
                }
            }
        }
        return totalVisitados;
    }

    // Auxiliar: existe caminho dirigido de u até v?
    private boolean alcanca(int u, int v) {
        boolean visitado[] = new boolean[n];
        Pilha pilha = new Pilha(n);
        pilha.push(u);
        visitado[u] = true;

        while (!pilha.isEmpty()) {
            int atual = pilha.pop();
            if (atual == v) return true;
            for (int j = 0; j < n; j++) {
                if (adj[atual][j] != Float.POSITIVE_INFINITY && !visitado[j]) {
                    visitado[j] = true;
                    pilha.push(j);
                }
            }
        }
        return visitado[v];
    }

    // ----------------------------------------------------------------------
    // Categoria de conexidade do grafo dirigido: retorna 3 (C3-fortemente
    // conexo), 2 (C2-unilateral), 1 (C1-fraco) ou 0 (C0-desconexo)
    // ----------------------------------------------------------------------
    public int categoriaConexidade() {
        boolean fortementeConexo = true;
        for (int i = 0; i < n && fortementeConexo; i++) {
            boolean visitado[] = new boolean[n];
            if (dfsComPilha(i, visitado) < n) fortementeConexo = false;
        }
        if (fortementeConexo) return 3;

        boolean unilateralmenteConexo = true;
        for (int i = 0; i < n && unilateralmenteConexo; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!alcanca(i, j) && !alcanca(j, i)) {
                    unilateralmenteConexo = false;
                    break;
                }
            }
        }
        if (unilateralmenteConexo) return 2;

        boolean visitado[] = new boolean[n];
        if (n > 0 && dfsComPilhaNaoDirigido(0, visitado) == n) return 1;

        return 0;
    }

    // Auxiliar do grafo reduzido: DFS no grafo original que empilha o
    // vértice na Pilha assim que termina de visitar todos os seus vizinhos
    // (pós-ordem) — 1a etapa do algoritmo FCONEX/Kosaraju
    private void dfsOrdemFinalizacao(int v, boolean visitado[], Pilha pilhaOrdem) {
        visitado[v] = true;
        for (int j = 0; j < n; j++) {
            if (adj[v][j] != Float.POSITIVE_INFINITY && !visitado[j]) {
                dfsOrdemFinalizacao(j, visitado, pilhaOrdem);
            }
        }
        pilhaOrdem.push(v);
    }

    // Auxiliar do grafo reduzido: DFS no grafo transposto que rotula cada
    // vértice alcançado com o id da componente fortemente conexa atual
    private void dfsComponente(int v, boolean visitado[], int comp[], int idComp, float adjT[][]) {
        visitado[v] = true;
        comp[v] = idComp;
        for (int j = 0; j < n; j++) {
            if (adjT[v][j] != Float.POSITIVE_INFINITY && !visitado[j]) {
                dfsComponente(j, visitado, comp, idComp, adjT);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Grafo reduzido (condensação das componentes fortemente conexas),
    // via algoritmo FCONEX / Kosaraju. Mostra direto o resultado.
    // ----------------------------------------------------------------------
    public void mostrarGrafoReduzido() {
        if (n == 0) {
            System.out.println("Grafo vazio.");
            return;
        }

        // 1) DFS no grafo original, empilhando por ordem de finalização
        boolean visitado[] = new boolean[n];
        Pilha pilhaOrdem = new Pilha(n);
        for (int i = 0; i < n; i++) {
            if (!visitado[i]) {
                dfsOrdemFinalizacao(i, visitado, pilhaOrdem);
            }
        }

        // 2) monta o grafo transposto (inverte todas as arestas)
        float adjT[][] = new float[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                adjT[i][j] = adj[j][i];

        // 3) desempilha e faz DFS no transposto: cada DFS = 1 componente
        int comp[] = new int[n];
        for (int i = 0; i < n; i++) comp[i] = -1;
        boolean visitado2[] = new boolean[n];
        int idComp = 0;

        while (!pilhaOrdem.isEmpty()) {
            int v = pilhaOrdem.pop();
            if (!visitado2[v]) {
                dfsComponente(v, visitado2, comp, idComp, adjT);
                idComp++;
            }
        }

        // 4) monta e mostra o grafo reduzido: 1 vértice por componente
        System.out.println("Grafo reduzido: " + idComp + " componente(s) fortemente conexa(s).");
        System.out.println("n: " + idComp);
        for (int c = 0; c < idComp; c++) {
            System.out.print("Componente " + c + " = { ");
            for (int i = 0; i < n; i++) {
                if (comp[i] == c) System.out.print(rotulo[i] + " ");
            }
            System.out.println("}");
        }

        boolean temAresta[][] = new boolean[idComp][idComp];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (adj[i][j] != Float.POSITIVE_INFINITY && comp[i] != comp[j])
                    temAresta[comp[i]][comp[j]] = true;

        System.out.println("\nArestas do grafo reduzido:");
        for (int i = 0; i < idComp; i++)
            for (int j = 0; j < idComp; j++)
                if (temAresta[i][j])
                    System.out.println("Componente " + i + " -> Componente " + j);
    }

    public int getN() { return n; }
    public int getM() { return m; }
}
