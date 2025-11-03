-- Inserir administrador padrão
-- Email: admin.souza@gmail.com
-- Senha: Senha@123 (criptografada com BCrypt)
INSERT INTO administradores (nome, email, senha, telefone)
SELECT 'Admin', 'admin.souza@gmail.com', '$2a$10$2mEpSiktdueKBjUl7Y7A.OkrBw.f/DKl4.OWxf796a5IdH8EwXkQq', NULL
WHERE NOT EXISTS (SELECT 1 FROM administradores WHERE email = 'admin.souza@gmail.com');

