CREATE DATABASE biblioteca;
USE biblioteca;

CREATE TABLE usuario (
    id INT NOT NULL AUTO_INCREMENT,
    login VARCHAR(255) DEFAULT NULL,
    tipo VARCHAR(255) DEFAULT NULL,
    nome VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE livro (
    id INT NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(255) DEFAULT NULL,
    ano INT DEFAULT NULL,
    autores VARCHAR(255) DEFAULT NULL,
    editora VARCHAR(255) DEFAULT NULL,
    edicao INT DEFAULT NULL,
    titulo VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE emprestimo (
    id INT NOT NULL AUTO_INCREMENT,
    id_livro INT DEFAULT NULL,
    id_usuario INT DEFAULT NULL,
    prazo DATE DEFAULT NULL,
    devolucao DATE DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_emprestimo_livro FOREIGN KEY (id_livro)
        REFERENCES livro (id),
    CONSTRAINT fk_emprestimo_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id)
);

CREATE TABLE reserva (
    id INT NOT NULL AUTO_INCREMENT,
    id_livro INT DEFAULT NULL,
    id_usuario INT DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reserva_livro FOREIGN KEY (id_livro)
        REFERENCES livro (id),
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id)
);

CREATE TABLE multa (
    id INT NOT NULL AUTO_INCREMENT,
    valor DOUBLE DEFAULT NULL,
    pago TINYINT(1) DEFAULT NULL,
    id_usuario INT DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_multa_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id)
);