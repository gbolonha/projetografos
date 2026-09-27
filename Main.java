import java.io.IOException;
import java.util.Scanner;

//Gabriel Teixeira Bolonha (10426937)
//Geovana Bomfim Rodrigues (10410514)
//Rodrigo Daiske Uehara (10440295)
//Yating Zheng (10439511)

public class Main {

    private static final String NOME_ARQUIVO_PADRAO = "grafo.txt";
    private static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            // segue com o encoding padrao caso UTF-8 nao esteja disponivel
        }
        teclado = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8);

        TGrafo grafo = new TGrafo();
        boolean encerrar = false;

        exibirTitulo();

        while (!encerrar) {
            exibirMenu();
            String opcao = lerOpcao();

            switch (opcao) {
                case "a":
                    opcaoLerArquivo(grafo);
                    break;
                case "b":
                    opcaoGravarArquivo(grafo);
                    break;
                case "c":
                    opcaoInserirVertice(grafo);
                    break;
                case "d":
                    opcaoInserirAresta(grafo);
                    break;
                case "e":
                    opcaoRemoverVertice(grafo);
                    break;
                case "f":
                    opcaoRemoverAresta(grafo);
                    break;
                case "g":
                    grafo.mostrarConteudo();
                    break;
                case "h":
                    grafo.mostrarGrafo();
                    break;
                case "i":
                    opcaoConexidade(grafo);
                    break;
                case "j":
                    encerrar = true;
                    System.out.println("\nEncerrando a aplicacao. Ate mais!");
                    break;
                default:
                    System.out.println("\nOpcao invalida. Escolha uma letra de 'a' a 'j'.");
            }
        }

        teclado.close();
    }

    private static void exibirTitulo() {
        System.out.println("================================================================");
        System.out.println(" REDE DE SERVICOS ESSENCIAIS DE ITAQUERA");
        System.out.println(" Custo (R$) por local x Distancia (m) entre locais");
        System.out.println(" Grafo nao orientado, peso no vertice e na aresta (tipo 3)");
        System.out.println("================================================================");
    }

    private static void exibirMenu() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("MENU");
        System.out.println("a) Ler dados do arquivo " + NOME_ARQUIVO_PADRAO);
        System.out.println("b) Gravar dados no arquivo " + NOME_ARQUIVO_PADRAO);
        System.out.println("c) Inserir vertice");
        System.out.println("d) Inserir aresta");
        System.out.println("e) Remover vertice");
        System.out.println("f) Remover aresta");
        System.out.println("g) Mostrar conteudo do arquivo (vertices e arestas)");
        System.out.println("h) Mostrar grafo (matriz de adjacencia)");
        System.out.println("i) Apresentar a conexidade do grafo");
        System.out.println("j) Encerrar a aplicacao");
        System.out.print("Escolha uma opcao: ");
    }

    private static String lerOpcao() {
        String linha = teclado.nextLine().trim().toLowerCase();
        if (!linha.isEmpty()) linha = linha.substring(0, 1);
        return linha;
    }

    private static void opcaoLerArquivo(TGrafo grafo) {
        System.out.print("\nNome do arquivo a ler (ENTER para \"" + NOME_ARQUIVO_PADRAO + "\"): ");
        String nome = teclado.nextLine().trim();
        if (nome.isEmpty()) nome = NOME_ARQUIVO_PADRAO;
        try {
            grafo.lerArquivo(nome);
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo \"" + nome + "\": " + e.getMessage());
        }
    }

    private static void opcaoGravarArquivo(TGrafo grafo) {
        System.out.print("\nNome do arquivo para gravar (ENTER para \"" + NOME_ARQUIVO_PADRAO + "\"): ");
        String nome = teclado.nextLine().trim();
        if (nome.isEmpty()) nome = NOME_ARQUIVO_PADRAO;
        try {
            grafo.gravarArquivo(nome);
        } catch (IOException e) {
            System.out.println("Erro ao gravar o arquivo \"" + nome + "\": " + e.getMessage());
        }
    }

    private static void opcaoInserirVertice(TGrafo grafo) {
        try {
            System.out.print("\nRotulo (numero) do novo vertice: ");
            int rot = Integer.parseInt(teclado.nextLine().trim());

            System.out.print("Apelido/nome do local (ex: UBS Santo Estevao): ");
            String apelido = teclado.nextLine().trim();

            System.out.print("Peso do vertice (custo em R$; use 0 se for gratuito): ");
            float peso = Float.parseFloat(teclado.nextLine().trim().replace(",", "."));

            grafo.insereVertice(rot, apelido, peso);
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida: rotulo e peso devem ser numeros.");
        }
    }

    private static void opcaoInserirAresta(TGrafo grafo) {
        try {
            System.out.print("\nRotulo do vertice 1: ");
            int rotV = Integer.parseInt(teclado.nextLine().trim());

            System.out.print("Rotulo do vertice 2: ");
            int rotW = Integer.parseInt(teclado.nextLine().trim());

            System.out.print("Peso da aresta (distancia em metros): ");
            float peso = Float.parseFloat(teclado.nextLine().trim().replace(",", "."));

            grafo.insereAresta(rotV, rotW, peso);
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida: rotulos e peso devem ser numeros.");
        }
    }

    private static void opcaoRemoverVertice(TGrafo grafo) {
        try {
            System.out.print("\nRotulo do vertice a remover: ");
            int rot = Integer.parseInt(teclado.nextLine().trim());
            grafo.removeVertice(rot);
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida: o rotulo deve ser um numero.");
        }
    }

    private static void opcaoRemoverAresta(TGrafo grafo) {
        try {
            System.out.print("\nRotulo do vertice 1: ");
            int rotV = Integer.parseInt(teclado.nextLine().trim());

            System.out.print("Rotulo do vertice 2: ");
            int rotW = Integer.parseInt(teclado.nextLine().trim());

            grafo.removeAresta(rotV, rotW);
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida: os rotulos devem ser numeros.");
        }
    }

    private static void opcaoConexidade(TGrafo grafo) {
        if (grafo.getN() == 0) {
            System.out.println("\nGrafo vazio - leia ou monte um grafo primeiro (opcao 'a' ou 'c').");
            return;
        }

        System.out.println("\n-- Conexidade do grafo --");
        if (grafo.ehConexo()) {
            System.out.println("O grafo E CONEXO: existe pelo menos um caminho entre qualquer par de vertices.");
        } else {
            System.out.println("O grafo NAO E CONEXO: existem componentes ou vertices isolados que nao se alcancam.");
        }
    }
}
