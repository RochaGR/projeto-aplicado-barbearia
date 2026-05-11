INSERT INTO administradores (nome, email, senha, telefone)
SELECT 'Admin', 'admin.souza@gmail.com', '$2a$10$2mEpSiktdueKBjUl7Y7A.OkrBw.f/DKl4.OWxf796a5IdH8EwXkQq', NULL
WHERE NOT EXISTS (SELECT 1 FROM administradores WHERE email = 'admin.souza@gmail.com');

INSERT INTO barbeiros (nome, email, senha, telefone, ativo)
SELECT 'Barbeiro', 'barbeiro@gmail.com', '$2a$10$2mEpSiktdueKBjUl7Y7A.OkrBw.f/DKl4.OWxf796a5IdH8EwXkQq', NULL, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM barbeiros WHERE email = 'barbeiro@gmail.com');

INSERT INTO servico (ativo, descricao, duracao_minutos, image_url, nome, preco)
SELECT TRUE, 'Corte de cabelo masculino clássico com máquina e tesoura', 45, 'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?w=400', 'Corte Clássico', 40.00
WHERE NOT EXISTS (SELECT 1 FROM servico WHERE nome = 'Corte Clássico');

INSERT INTO servico (ativo, descricao, duracao_minutos, image_url, nome, preco)
SELECT TRUE, 'Barba desenhada e aparada com toalha quente', 30, 'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?w=400', 'Barba Desenhada', 30.00
WHERE NOT EXISTS (SELECT 1 FROM servico WHERE nome = 'Barba Desenhada');

INSERT INTO servico (ativo, descricao, duracao_minutos, image_url, nome, preco)
SELECT TRUE, 'Combo de corte de cabelo e barba com tratamento especial', 75, 'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?w=400', 'Combo Corte e Barba', 65.00
WHERE NOT EXISTS (SELECT 1 FROM servico WHERE nome = 'Combo Corte e Barba');

INSERT INTO configuracao_fidelidade (id, percentual_desconto, cortes_para_desconto, ultima_atualizacao, atualizado_por)
VALUES (1, 40.0, 5, NOW(), 'Sistema')
ON CONFLICT (id) DO NOTHING;






