INSERT INTO administradores (nome, email, senha, telefone)
SELECT 'Admin', 'admin.souza@gmail.com', '$2a$10$2mEpSiktdueKBjUl7Y7A.OkrBw.f/DKl4.OWxf796a5IdH8EwXkQq', NULL
WHERE NOT EXISTS (SELECT 1 FROM administradores WHERE email = 'admin.souza@gmail.com');

INSERT INTO configuracao_fidelidade (id, percentual_desconto, cortes_para_desconto, ultima_atualizacao, atualizado_por)
VALUES (1, 40.0, 5, NOW(), 'Sistema')
ON CONFLICT (id) DO NOTHING;






