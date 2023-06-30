package t1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Usuario extends Biblioteca {
	public String login;
	public String nome;
	public String tipo;

	public List<Multa> multas = new ArrayList<>();

	public Integer get_maximo_emprestimos () {
		return null;
	}

	public Integer get_prazo_emprestimos () {
		return null;
	}
	public List<Multa> multas_pendentes() {
		return multas.stream().filter(m -> !m.pago).collect(Collectors.toList());
	}
	public Double get_soma() {
		return multas_pendentes().stream().mapToDouble(m -> m.valor).sum();
	}
}
