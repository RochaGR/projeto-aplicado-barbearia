-- Criar registros de Cliente para administradores (para usar o chat)
INSERT INTO clientes (ativo, nome, telefone, email, senha, auth_provider)
SELECT true, nome, telefone, email, senha, 'LOCAL'
FROM administradores
WHERE NOT EXISTS (SELECT 1 FROM clientes c WHERE c.email = administradores.email);

-- Criar registros de Cliente para barbeiros (para usar o chat)
INSERT INTO clientes (ativo, nome, telefone, email, senha, auth_provider)
SELECT true, nome, telefone, email, senha, 'LOCAL'
FROM barbeiros
WHERE NOT EXISTS (SELECT 1 FROM clientes c WHERE c.email = barbeiros.email);
