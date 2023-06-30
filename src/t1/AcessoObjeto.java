package t1;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class AcessoObjeto {

    public Connection connection;

    public static Map<String, Class<?>> tipos = new HashMap<>();

    static  {
        tipos.put(Aluno.class.getName(), Aluno.class);
        tipos.put(Professor.class.getName(), Professor.class);
    }

    public AcessoObjeto(Connection connection) {
        this.connection = connection;
    }

    public Object getObjeto(Class<?> classe, String id) {
        return consulta(classe, "id", id, "=").get(0);
    }

    public Object consultaUnica(Class<?> classe, String campo, String valor, String operacao) {
        List<Object> consulta = consulta(classe, campo, valor, operacao);
        if (consulta.isEmpty()) {
            return null;
        } else {
            return consulta.get(0);
        }
    }

    public List<Object> consulta(Class<?> classe, String campo, String valor, String operacao) {
        List<Object> objetos = new LinkedList<>();
        try {
            String tabela = classe.getName().toLowerCase();
            String statement = "SELECT * FROM "+ tabela + " WHERE " + campo + " " + operacao + " ?";
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setString(1, valor);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                try {
                    if (resultSet.getString("tipo") != null) {
                        classe = tipos.get(resultSet.getString("tipo"));
                    }
                } catch (Exception ex) { /**/ }
                Object objeto = classe.newInstance();
                for (Field field: Arrays.stream(classe.getFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList())) {
                    if (isPrimitivo(field.getType())) {
                        field.set(objeto, resultSet.getObject(field.getName(), field.getType()));
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        Class<?> classeLista = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        List<Object> lista = consulta(classeLista, "id_" + tabela, resultSet.getString("id"), "=");
                        field.set(objeto, lista);
                    } else {
                        Object filho = getObjeto(field.getType(), resultSet.getString("id_" + field.getType().getName().toLowerCase()));
                        field.set(objeto, filho);
                    }
                }
                objetos.add(objeto);
            }
        } catch (Exception e) { /**/ }
        return objetos;
    }

    public Integer insert(Object objeto, Class<?> classe) {
        StringBuilder campos = new StringBuilder("insert into " + classe.getName().toLowerCase() + " (");
        StringBuilder placeholders = new StringBuilder("(");
        for (Field field: Arrays.stream(classe.getFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList())) {
            if (!field.getName().equals("id") && !List.class.isAssignableFrom(field.getType())) {
                if (isPrimitivo(field.getType())) {
                    campos.append(field.getName().toLowerCase() + ", ");
                } else {
                    campos.append("id_" + field.getName().toLowerCase() + ", ");
                }
                placeholders.append("?, ");
            }
        }
        campos.delete(campos.length() - 2, campos.length() - 1);
        campos.append(") values ");
        placeholders.delete(placeholders.length() - 2, placeholders.length() - 1);
        placeholders.append(")");
        String statement = campos.toString() + placeholders.toString();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            Integer indexValor = 1;
            for (Field field: Arrays.stream(classe.getFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList())) {
                if (!field.getName().equals("id") && !List.class.isAssignableFrom(field.getType())) {
                    if (isPrimitivo(field.getType())) {
                        setPrimitivo(preparedStatement, indexValor, field, objeto);
                    } else {
                        Biblioteca filho = (Biblioteca) field.get(objeto);
                        if (filho.id != null) {
                            preparedStatement.setInt(indexValor, filho.id); //set chave estrangeira
                        } else {
                            preparedStatement.setInt(indexValor, insert(field.get(objeto), field.getType())); //insere elemento e seta id retornado como chave estrangeira
                        }
                    }
                    indexValor++;
                }
            }
            preparedStatement.execute();
            PreparedStatement idInseridoStatement = connection.prepareStatement("select max(id) from " + classe.getName().toLowerCase());
            idInseridoStatement.execute();
            ResultSet resultadoIdInserido = idInseridoStatement.getResultSet();
            resultadoIdInserido.next();
            return resultadoIdInserido.getInt("max(id)");
        } catch (Exception e) {
            return null;
        }
    }

    public void update(Object objeto, Class<?> classe) {
        StringBuilder statement = new StringBuilder("update " + classe.getName().toLowerCase() + " set ");
        for (Field field : Arrays.stream(classe.getFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList())) {
            if (!field.getName().equals("id") && !List.class.isAssignableFrom(field.getType())) {
                if (isPrimitivo(field.getType())) {
                    statement.append(field.getName() + " = ?, ");
                } else {
                    statement.append("id_" + field.getName() + " = ?, ");
                }
            }
        }
        statement.delete(statement.length() - 2, statement.length() - 1);
        statement.append(" ");
        Integer id = ((Biblioteca) objeto).id;
        statement.append("where id = " + id);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(statement.toString());
            Integer indexValor = 1;
            for (Field field: Arrays.stream(classe.getFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList())) {
                if (!field.getName().equals("id") && !List.class.isAssignableFrom(field.getType())) {
                    if (isPrimitivo(field.getType())) {
                        setPrimitivo(preparedStatement, indexValor, field, objeto);
                    } else {
                        Biblioteca filho = (Biblioteca) field.get(objeto);
                        if (filho.id != null) {
                            preparedStatement.setInt(indexValor, filho.id); //set chave estrangeira
                        } else {
                            preparedStatement.setInt(indexValor, insert(field.get(objeto), field.getType())); //insere elemento e seta id retornado como chave estrangeira
                        }
                    }
                    indexValor++;
                }
            }
            preparedStatement.execute();
        } catch (Exception e) { /**/ }
    }

    public void excluir(Integer id, Class<?> classe) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from " + classe.getName().toLowerCase() + " where id = " + id);
            preparedStatement.execute();
        } catch (Exception e) { /**/ }
    }

    public void insertLista(Object elemento, Class<?> classe, String tabelaPai, Integer idPai) {
        Biblioteca elementoEntidade = (Biblioteca) elemento;
        if (elementoEntidade.id == null) {
            elementoEntidade.id = insert(elemento, classe);
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("update " + classe.getName().toLowerCase() + " set id_" + tabelaPai + " = ? where id = ?");
            preparedStatement.setInt(1, idPai);
            preparedStatement.setInt(2, elementoEntidade.id);
            preparedStatement.execute();
        } catch (Exception e) { /**/ }
    }

    private void setPrimitivo(PreparedStatement preparedStatement, Integer index, Field field, Object objeto) {
        try {
            if (field.getType().equals(java.sql.Date.class)) {
                preparedStatement.setDate(index, (java.sql.Date) field.get(objeto));
            } else if (field.getType().equals(String.class)){
                preparedStatement.setString(index, (String) field.get(objeto));
            } else if (field.getType().equals(Integer.class)) {
                preparedStatement.setInt(index, (Integer) field.get(objeto));
            } else if (field.getType().equals(Double.class)) {
                preparedStatement.setDouble(index, (Double) field.get(objeto));
            } else if (field.getType().equals(Boolean.class)) {
                preparedStatement.setBoolean(index, (Boolean) field.get(objeto));
            }
        } catch (Exception e) {
            try {
                preparedStatement.setObject(index, null);
            } catch (Exception e1) { /**/ }
        }
    }

    private Boolean isPrimitivo(Class<?> classe) {
        return classe.equals(String.class) || classe.equals(Integer.class) || classe.equals(java.sql.Date.class) || classe.equals(Boolean.class) || classe.equals(Double.class);
    }

    /*
    TODO:Remover elemento
     */
}
