INSERT INTO administradores (nome, email, senha, telefone)
SELECT 'Admin', 'admin.souza@gmail.com', '$2a$10$2mEpSiktdueKBjUl7Y7A.OkrBw.f/DKl4.OWxf796a5IdH8EwXkQq', NULL
WHERE NOT EXISTS (SELECT 1 FROM administradores WHERE email = 'admin.souza@gmail.com');

INSERT INTO barbeiros (nome, email, senha, telefone, ativo)
SELECT 'Barbeiro', 'barbeiro@gmail.com', '$2a$10$2mEpSiktdueKBjUl7Y7A.OkrBw.f/DKl4.OWxf796a5IdH8EwXkQq', NULL, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM barbeiros WHERE email = 'barbeiro@gmail.com');

INSERT INTO servico (ativo, descricao, duracao_minutos, image_url, nome, preco)
SELECT TRUE, 'Corte de cabelo masculino clássico com máquina e tesoura', 45, 'https://www.bing.com/th/id/OIP.pr84A6M_i6vBzlRYSShxPAHaIx?w=193&h=229&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2', 'Corte Clássico', 40.00
WHERE NOT EXISTS (SELECT 1 FROM servico WHERE nome = 'Corte Clássico');

INSERT INTO servico (ativo, descricao, duracao_minutos, image_url, nome, preco)
SELECT TRUE, 'Barba desenhada e aparada com toalha quente', 30, 'https://thfvnext.bing.com/th/id/OIP.ZAxePWkB4pxuTkrPeWU0EAHaJ4?w=203&h=271&c=7&r=0&o=7&cb=thfvnext&pid=1.7&rm=3', 'Barba Desenhada', 30.00
WHERE NOT EXISTS (SELECT 1 FROM servico WHERE nome = 'Barba Desenhada');

INSERT INTO servico (ativo, descricao, duracao_minutos, image_url, nome, preco)
SELECT TRUE, 'Combo de corte de cabelo e barba com tratamento especial', 45, 'https://thfvnext.bing.com/th/id/OIP.fLEdaFnIjS2MMWMA9GbgMgHaHa?w=264&h=198&c=7&r=0&o=7&cb=thfvnext&pid=1.7&rm=3', 'Combo Corte e Barba', 65.00
WHERE NOT EXISTS (SELECT 1 FROM servico WHERE nome = 'Combo Corte e Barba');

INSERT INTO configuracao_fidelidade (id, percentual_desconto, cortes_para_desconto, ultima_atualizacao, atualizado_por)
VALUES (1, 40.0, 5, NOW(), 'Sistema')
ON CONFLICT (id) DO NOTHING;

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 1, 'Domingo', FALSE, NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 1);

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 2, 'Segunda-feira', TRUE, '08:00', '19:00'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 2);

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 3, 'Terça-feira', TRUE, '08:00', '19:00'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 3);

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 4, 'Quarta-feira', TRUE, '08:00', '19:00'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 4);

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 5, 'Quinta-feira', TRUE, '08:00', '19:00'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 5);

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 6, 'Sexta-feira', TRUE, '08:00', '19:00'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 6);

INSERT INTO configuracao_horario (dia_semana, dia_nome, ativo, abertura, fechamento)
SELECT 7, 'Sábado', TRUE, '08:00', '12:00'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_horario WHERE dia_semana = 7);


