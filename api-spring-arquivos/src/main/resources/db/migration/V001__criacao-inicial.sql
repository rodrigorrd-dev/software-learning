CREATE TABLE arquivo (
     id SERIAL PRIMARY KEY,
     codigoatividadefirebase VARCHAR(255) NOT NULL,
     nome VARCHAR(255),
     responsavel VARCHAR(255),
     tipoarquivo VARCHAR(90),
     conteudo BYTEA,
     datacriacao TIMESTAMP DEFAULT now(),
     dataatualizacao TIMESTAMP DEFAULT now()
);