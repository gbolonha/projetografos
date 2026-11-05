/*
 * ======================================================================
 *
 *
 *  Integrantes:
 *    Gabriel Teixeira Bolonha (10426937)
 *    Geovana Bomfim Rodrigues (10410514)
 *    Rodrigo Daiske Uehara    (10440295)
 *    Yating Zheng             (10439511)
 *
 * 
 */

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    private static final String ARQUIVO = "grafo.txt";
    private static Scanner teclado;
    private static boolean fimDeEntrada = false;

    // Le uma linha; no fim da entrada (EOF) marca fimDeEntrada e devolve null
    private static String lerLinha(String pergunta) {
        System.out.print(pergunta);
        if (!teclado.hasNextLine()) {
            fimDeEntrada = true;
            return null;
        }
        return teclado.nextLine().trim();
    }

    private static Integer lerInt(String pergunta) {
        while (true) {
            String s = lerLinha(pergunta);
            if (s == null) return null;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido: digite um numero inteiro.");
            }
        }
    }

    private static Float lerFloat(String pergunta) {
        while (true) {
            String s = lerLinha(pergunta);
            if (s == null) return null;
            try {
                float f = Float.parseFloat(s.replace(",", "."));
                if (Float.isNaN(f) || Float.isInfinite(f) || f < 0f) {
                    System.out.println("Valor invalido: digite um numero maior ou igual a zero.");
                    continue;
                }
                return f;
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido: digite um numero (ex.: 350 ou 12,5).");
            }
        }
    }

    private static void mostrarMenu(TGrafo g) {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println(" SIMULADOR DE ORCAMENTO FAMILIAR COM RENDA MINIMA ");
        System.out.println("\n");
        System.out.println(" Em memoria: tipo " + g.getTipoGrafo() + " | " + g.getN() +
                " vertices | " + g.getM() + " arestas");
        System.out.println("==============================================================");
        System.out.println(" a) Ler dados do arquivo " + ARQUIVO);
        System.out.println(" b) Gravar dados no arquivo " + ARQUIVO);
        System.out.println(" c) Inserir vertice");
        System.out.println(" d) Inserir aresta");
        System.out.println(" e) Remover vertice");
        System.out.println(" f) Remover aresta");
        System.out.println(" g) Mostrar conteudo do arquivo");
        System.out.println(" h) Mostrar grafo");
        System.out.println(" i) Apresentar a conexidade do grafo e o reduzido");
        System.out.println(" j) Encerrar a aplicacao");
    }

    public static void main(String[] args) {
        
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        teclado = new Scanner(System.in, StandardCharsets.UTF_8);

        TGrafo g = new TGrafo();
        boolean rodando = true;

        while (rodando) {
            mostrarMenu(g);
            String op = lerLinha("Opcao: ");
            if (op == null) {
                System.out.println("\nFim da entrada. Aplicacao encerrada.");
                break;
            }
            if (op.isEmpty()) continue;

            try {
                switch (op.toLowerCase().charAt(0)) {
                    case 'a':
                        g.lerArquivo(ARQUIVO);
                        break;
                    case 'b':
                        g.gravarArquivo(ARQUIVO);
                        break;
                    case 'c': {
                        Integer rot = lerInt("Rotulo do novo vertice: ");
                        if (rot == null) break;
                        String ape = lerLinha("Apelido (nome do local): ");
                        if (ape == null) break;
                        float peso = 0f;
                        if (g.temPesoVertice()) {
                            Float p = lerFloat("Peso do vertice (custo em R$): ");
                            if (p == null) break;
                            peso = p;
                        }
                        g.insereVertice(rot, ape, peso);
                        break;
                    }
                    case 'd': {
                        Integer a = lerInt("Rotulo do vertice de origem: ");
                        if (a == null) break;
                        Integer b = lerInt("Rotulo do vertice de destino: ");
                        if (b == null) break;
                        float peso = 1f;
                        if (g.temPesoAresta()) {
                            Float p = lerFloat("Peso da aresta (distancia em metros): ");
                            if (p == null) break;
                            peso = p;
                        }
                        g.insereAresta(a, b, peso);
                        break;
                    }
                    case 'e': {
                        Integer rot = lerInt("Rotulo do vertice a remover: ");
                        if (rot == null) break;
                        g.removeVertice(rot);
                        break;
                    }
                    case 'f': {
                        Integer a = lerInt("Rotulo do vertice de origem: ");
                        if (a == null) break;
                        Integer b = lerInt("Rotulo do vertice de destino: ");
                        if (b == null) break;
                        g.removeAresta(a, b);
                        break;
                    }
                    case 'g':
                        TGrafo.mostrarConteudoArquivo(ARQUIVO);
                        break;
                    case 'h':
                        g.mostrarGrafo();
                        break;
                    case 'i':
                        g.mostrarConexidade();
                        break;
                    case 'j':
                        System.out.println("Aplicacao encerrada.");
                        rodando = false;
                        break;
                    default:
                        System.out.println("Opcao invalida. Use as letras de a ate j.");
                }
            } catch (java.io.IOException e) {
                // Arquivo ausente, incompleto ou invalido: avisa e volta ao menu.
                // O grafo que ja estava na memoria continua intacto.
                System.out.println("ERRO no arquivo \"" + ARQUIVO + "\": " + e.getMessage());
                System.out.println("(O grafo em memoria nao foi alterado.)");
            } catch (RuntimeException e) {
                System.out.println("ERRO inesperado: " + e + " (voltando ao menu).");
            }

            if (fimDeEntrada) {
                System.out.println("\nFim da entrada. Aplicacao encerrada.");
                break;
            }
        }
        teclado.close();
    }
}