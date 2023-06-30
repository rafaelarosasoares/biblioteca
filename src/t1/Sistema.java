package t1;
import java.util.Scanner;
import java.sql.Date;
import java.util.List;

public class Sistema {
    public Sistema (Search buscador, Scanner scanner, AcessoObjeto dao) {
        this.buscador = buscador;
        this.scanner = scanner;
        this.dao = dao;
    }

    private final Scanner scanner;
    private final Search buscador;
    private final AcessoObjeto dao;

    public static final int OPCAO_BUSCAR = 1;
    public static final int OPCAO_CADASTRAR = 2;

    public static final int ACAO_EXCLUIR = 1;
    public static final int ACAO_PAGAR_MULTAS = 2;
    public static final int ACAO_RESERVAR_EMPRESTAR = 2;
    public static final int ACAO_RENOVAR  = 2;
    public static final int ACAO_DEVOLVER = 3;

    public static final int TIPO_ALUNO = 1;
    public static final int TIPO_PROFESSOR = 2;


    public void manipulaEmprestimo() {
        System.out.println("Buscar empréstimo: ");
        Emprestimo emprestimo = buscador.buscaEmprestimo(scanner);

        System.out.println("1 - Excluir \n2 - Renovar \n3 - Devolver");

        int opcao = scanner.nextInt();
        if (opcao == ACAO_EXCLUIR) {
            dao.excluir(emprestimo.id, Emprestimo.class);
            System.out.println("Empréstimo excluído com sucesso");
        } else if (opcao == ACAO_RENOVAR) {
            renovarEmprestimo(emprestimo);
        } else if (opcao == ACAO_DEVOLVER) {
            devolverEmprestimo(emprestimo);
        }

    }

    private void devolverEmprestimo(Emprestimo emprestimo) {
        emprestimo.devolucao = new Date(new java.util.Date().getTime());
        if (emprestimo.devolucao.after(emprestimo.prazo)) {
            Multa multa = new Multa();
            multa.valor = Math.ceil((emprestimo.devolucao.getTime() - emprestimo.prazo.getTime()) / 86400000.d);
            dao.insertLista(multa, Multa.class, Usuario.class.getName().toLowerCase(), emprestimo.usuario.id);
            System.out.println("O atraso na devolução gerou uma multa de R$" + multa.valor + ".");
        }
        dao.update(emprestimo, Emprestimo.class);
        System.out.println("Livro devolvido com sucesso");
    }

    private void renovarEmprestimo(Emprestimo emprestimo) {
        Reserva reserva = (Reserva) dao.consultaUnica(Reserva.class, "id_livro", emprestimo.livro.id.toString(), "=");
        if (reserva != null) {
            System.out.println("Não é possível renovar, livro já reservado");
        } else {
            emprestimo.prazo = new Date(emprestimo.prazo.getTime() + (emprestimo.usuario.get_prazo_emprestimos() * 86400000));
            dao.update(emprestimo, Emprestimo.class);
            System.out.println("Empréstimo renovado até: " + emprestimo.prazo);
        }
    }

    private Integer opcaoManipular() {
        System.out.println("1 - Buscar \n2 - Cadastrar");
        return scanner.nextInt();
    }

    public void manipulaLivros() {
        int opcao = opcaoManipular();
        if (opcao == OPCAO_BUSCAR) {
            manipulaLivro();
        } else if (opcao == OPCAO_CADASTRAR) {
            cadastraLivro();
        }
    }

    public void cadastraLivro() {
        System.out.println("Novo livro:");
        Livro livro = new Livro();
        System.out.println("Título: ");
        livro.titulo = scanner.next();
        System.out.println("ISBN: ");
        livro.isbn = scanner.next();
        System.out.println("Autores: ");
        livro.autores = scanner.next();
        System.out.println("Edicao: ");
        livro.edicao = scanner.nextInt();
        System.out.println("Ano: ");
        livro.ano = scanner.nextInt();
        dao.insert(livro, Livro.class);
    }

    public void manipulaLivro() {
        Livro livro = buscador.buscaLivro(scanner);
        List<Emprestimo> emprestimos = (List<Emprestimo>) (Object) dao.consulta(Emprestimo.class, "id_livro", livro.id.toString(), "=");
        Reserva reserva = (Reserva) dao.consultaUnica(Reserva.class, "id_livro", livro.id.toString(), "=");
        Emprestimo emprestimoAtual = emprestimos.stream().filter(e -> e.devolucao == null).findFirst().orElse(null);
        System.out.println("1 - Excluir");
        if (emprestimoAtual == null) {
            System.out.println("2 - Emprestar");
        } else if (reserva == null) {
            System.out.println("2 - Reservar");
        } else {
            System.out.println("O livro encontra-se emprestado e reservado");
        }
        int opcao = scanner.nextInt();
        if (opcao == ACAO_EXCLUIR) {
            dao.excluir(livro.id, Livro.class);
            System.out.println("Livro excluído com sucesso");
        } else if (opcao == ACAO_RESERVAR_EMPRESTAR) {
            if (emprestimoAtual == null) {
                emprestarLivro(livro);
            } else {
                reservarLivro(livro);
            }
        }
    }

    public void reservarLivro(Livro livro) {
        System.out.println("Usuário da reserva: ");
        Usuario usuario = buscador.buscaUsuario(scanner);

        if (usuario.multas_pendentes().isEmpty()) {
            Reserva reserva = new Reserva();
            reserva.livro = livro;
            reserva.usuario = usuario;
            dao.insert(reserva, Reserva.class);
            System.out.println("Reserva feita com sucesso.");
        } else {
            System.out.println("O usuário possui multas pendentes, não é possível realizar reservas.");
        }
    }

    public void emprestarLivro(Livro livro) {
        System.out.println("Usuário do empréstimo: ");
        Usuario usuario = buscador.buscaUsuario(scanner);

        List<Emprestimo> emprestimosUsuario = (List<Emprestimo>) (Object) dao.consulta(Emprestimo.class, "id_usuario", usuario.id.toString(), "=");

        if (!usuario.multas_pendentes().isEmpty() && !emprestimosUsuario.isEmpty()) {
            System.out.println("O usuário possui multas pendentes, não é possível realizar mais de um empréstimo.");
        } else if (emprestimosUsuario.size() >= usuario.get_maximo_emprestimos()) {
            System.out.println("O usuário já possui o máximo de empréstimos ativos.");
        } else {
            Emprestimo emprestimo = new Emprestimo();
            emprestimo.livro = livro;
            emprestimo.usuario = usuario;
            emprestimo.prazo = new Date((new java.util.Date()).getTime() + (usuario.get_prazo_emprestimos() * 86400000));
            dao.insert(emprestimo, Emprestimo.class);
            System.out.println("Empréstimo feito, prazo até: " + emprestimo.prazo);
        }
    }

    public void manipulaUsuarios() {
        int opcao = opcaoManipular();
        if (opcao == OPCAO_BUSCAR) {
            manipulaUsuario();
        } else {
            cadastraUsuario();
        }
    }

    public void cadastraUsuario() {
        System.out.println("Novo usuario:");
        System.out.println("Tipo: 1 - Aluno, 2 - Professor");
        int tipo = scanner.nextInt();
        Usuario usuario;
        if (tipo == TIPO_ALUNO) {
            usuario = new Aluno();
        } else if (tipo == TIPO_PROFESSOR) {
            usuario = new Professor();
        } else {
            return;
        }
        System.out.println("Nome: ");
        usuario.nome = scanner.next();
        System.out.println("Login: ");
        usuario.login = scanner.next();
        dao.insert(usuario, Usuario.class);
    }

    public void manipulaUsuario() {
        Usuario usuario = buscador.buscaUsuario(scanner);

        System.out.println("1 - Excluir");
        if (!usuario.multas_pendentes().isEmpty()) {
            System.out.println("2 - Pagar multas pendentes");
        }

        int acao = scanner.nextInt();
        if (acao == ACAO_EXCLUIR) {
            dao.excluir(usuario.id, Usuario.class);
            System.out.println("Usuário excluído com sucesso");
        } else if (acao == ACAO_PAGAR_MULTAS) {
            pagarMultas(usuario, scanner);
        }
    }

    public void pagarMultas(Usuario usuario, Scanner scanner) {
        for (Multa multa: usuario.multas) {
            System.out.println("Valor da multa: " + multa.valor);
            System.out.println("Paga: " + (multa.pago ? "Sim" : "Não"));
            System.out.println("\n");
        }
        System.out.println("Total pendente: " + usuario.get_soma());
        System.out.println("Pagar? (s/n): ");
        String resposta = scanner.next();
        if (resposta.equals("s")) {
            for (Multa multaPendente: usuario.multas_pendentes()) {
                multaPendente.pago = true;
                dao.update(multaPendente, Multa.class);
            }
            System.out.println("Multas pendentes pagas");
        } else {
            System.out.println("Pagamento não efetuado");
        }
    }

}
