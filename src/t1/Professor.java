package t1;

public class Professor extends Usuario {

    public Professor () {
        tipo = Professor.class.getName();
    }
    public static final Integer maximo_emprestimo = 5;
    public static final Integer prazo_emprestimo = 15;

    @Override
    public Integer get_maximo_emprestimos () {
        return maximo_emprestimo;
    }

    @Override
    public Integer get_prazo_emprestimos () {
        return prazo_emprestimo;
    }
}
