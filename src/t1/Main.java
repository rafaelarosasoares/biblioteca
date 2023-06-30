// JOSIANE AGGENS
// RAFAELA DA ROSA

package t1;
import java.sql.DriverManager;
import java.util.Scanner;
import java.sql.Connection;

public class Main {
    private static final int acessa_usuarios = 1;
    private static final int acessa_livros = 2;
    private static final int acessa_emprestimos = 3;
    private static boolean sair_sistema = false;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "admin");

        if (connection != null) {
            System.out.println("Conexão realizada com sucesso");
            connection.close();
        }

        AcessoObjeto dao = new AcessoObjeto(connection);
        Search buscador = new Search(dao);
        Sistema controle = new Sistema(buscador, scanner, dao);

        int opcao;
        do {
            System.out.println("Acessar:\n 1 - Usuários\n 2 - Livros\n 3 - Empréstimos\n 4 - Sair");
            opcao = scanner.nextInt();
            if (opcao == acessa_usuarios) {
                controle.manipulaUsuarios();
            } else if (opcao == acessa_livros) {
                controle.manipulaLivros();
            } else if (opcao == acessa_emprestimos) {
                controle.manipulaEmprestimo();
            } else {
                sair_sistema = true;
            }
        } while (!sair_sistema);
    }
}

