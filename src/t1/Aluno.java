package t1;

public class Aluno extends Usuario {
    public Aluno () {
        tipo = Aluno.class.getName();
    }

    public static final Integer maximo_emprestimo = 3;
    public static final Integer prazo_emprestimo = 7;

    @Override
    public Integer get_maximo_emprestimos () {
        return maximo_emprestimo;
    }

    @Override
    public Integer get_prazo_emprestimos () {
        return prazo_emprestimo;
    }
}
