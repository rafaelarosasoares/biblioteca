package t1;
import java.util.Scanner;

public class Search {
    private static final int nome = 1;
    private static final int login = 2;
    private static final int titulo_livro = 1;
    private static final int isbn_livro = 2;
    private static final int livro = 1;
    private static final int usuario = 2;
    public Search(AcessoObjeto dao) {
        this.dao = dao;
    }
    private final AcessoObjeto dao;

    public Emprestimo buscaEmprestimo(Scanner scanner) {
        Emprestimo emprestimo = null;
        do {
            System.out.println("1 - Buscar por livro \n2 - Buscar por usuario");
            int campo = scanner.nextInt();
            if (campo == livro) {
                System.out.println("Livro do empréstimo: ");
                Livro livro = buscaLivro(scanner);
                emprestimo = (Emprestimo) dao.consultaUnica(Emprestimo.class, "id_livro", livro.id.toString(), "=");
            } else if (campo == usuario) {
                System.out.println("Usuário do empréstimo: ");
                Usuario usuario = buscaUsuario(scanner);
                emprestimo = (Emprestimo) dao.consultaUnica(Emprestimo.class, "id_usuario", usuario.id.toString(), "=");
            }
            if (emprestimo == null) {
                System.out.println("Nenhum empréstimo encontrado");
            }
        } while (emprestimo == null);
        System.out.println("Empréstrimo selecionado: ");
        System.out.println("Usuário: " + emprestimo.usuario.login);
        System.out.println("Livro: " + emprestimo.livro.titulo);
        System.out.println("Prazo: " + emprestimo.prazo + "\n\n");

        return emprestimo;
    }

    public Usuario buscaUsuario(Scanner scanner) {
        Usuario usuario = null;
        do {
            System.out.println("1 - Buscar por nome (ou parte) \n2 - Buscar por login");
            int campo = scanner.nextInt();
            System.out.println("Valor de busca: ");
            String valorBusca = scanner.next();
            if (campo == nome) {
                usuario = (Usuario) dao.consultaUnica(Usuario.class, "nome", "%" + valorBusca + "%", "like");
            } else if (campo == login) {
                usuario = (Usuario) dao.consultaUnica(Usuario.class, "login", valorBusca, "=");
            }
            if (usuario == null) {
                System.out.println("Nenhum usuario encontrado");
            }
        } while (usuario == null);
        System.out.println("Usuario selecionado: ");
        System.out.println("Login: " + usuario.login);
        System.out.println("Nome: " + usuario.nome);
        System.out.println("Tipo: " + usuario.tipo + "\n\n");

        return usuario;
    }

    public Livro buscaLivro(Scanner scanner) {
        Livro livro = null;
        do {
            System.out.println("1 - Buscar por titulo (ou parte) \n2 - Buscar por ISBN");
            int campo = scanner.nextInt();
            System.out.println("Valor de busca: ");
            String valorBusca = scanner.next();
            if (campo == titulo_livro) {
                livro = (Livro) dao.consultaUnica(Livro.class, "titulo", "%" + valorBusca + "%", "like");
            } else if (campo == isbn_livro) {
                livro = (Livro) dao.consultaUnica(Livro.class, "isbn", valorBusca, "=");
            }
            if (livro == null) {
                System.out.println("Nenhum livro encontrado");
            }
        } while (livro == null);
        System.out.println("Livro selecionado: ");
        System.out.println("Titulo: " + livro.titulo);
        System.out.println("ISBN: " + livro.isbn);
        System.out.println("Autores: " + livro.autores);
        System.out.println("Edicao: " + livro.edicao);
        System.out.println("Ano: " + livro.ano + "\n\n");

        return livro;
    }
}
