-- =================================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS E TABELAS PARA O SISTEMA DA ASSANDEF
-- =================================================================

CREATE DATABASE IF NOT EXISTS `assandef_db` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE `assandef_db`;



-- =================================================================
# 1. Armazena os dados dos usuários do sistema, com referência à sua função.
CREATE TABLE `funcionarios` (
  `id_funcionario` INT NOT NULL AUTO_INCREMENT,
  `nome_completo` VARCHAR(255) NOT NULL,
  `login` VARCHAR(80) NOT NULL UNIQUE,
  `senha_hash` VARCHAR(255) NOT NULL,
  `hierarquia` INT(11) NOT NULL COMMENT 'Ex: 1=Diretoria, 2=Secretaria...',
  PRIMARY KEY (`id_funcionario`)
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 2. Cadastro completo dos pacientes atendidos.
CREATE TABLE `pacientes` (
  `id_paciente` INT NOT NULL AUTO_INCREMENT,
  `nome_completo` VARCHAR(255) NOT NULL,
  `cpf` VARCHAR(11) NULL UNIQUE,
  `rg` VARCHAR(20) NULL,
  `n_sus` VARCHAR(15) NULL,
  `data_nascimento` DATE NULL,
  `sexo` VARCHAR(15) NULL,
  `endereco` TEXT NULL,
  `nome_responsavel` VARCHAR(255) NULL,
  `contato_responsavel` VARCHAR(255) NULL COMMENT 'Pode conter o nome e o parentesco do responsável.',
  PRIMARY KEY (`id_paciente`)
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 3. Tabela para armazenar mais de um telefone de uma pessoa.
CREATE TABLE `telefones` (
  `id_telefone` INT NOT NULL AUTO_INCREMENT,
  `id_paciente` INT NOT NULL,
  `numero` VARCHAR(20) NOT NULL,
  `descricao` VARCHAR(50) NULL COMMENT 'Ex: Celular, Contato de Emergência',
  PRIMARY KEY (`id_telefone`),
  INDEX `fk_telefones_pacientes_idx` (`id_paciente` ASC),
  CONSTRAINT `fk_telefones_pacientes`
    FOREIGN KEY (`id_paciente`)
    REFERENCES `pacientes` (`id_paciente`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 4. Registra cada atendimento, vinculando um paciente a um profissional.
CREATE TABLE `atendimentos` (
  `id_atendimento` INT NOT NULL AUTO_INCREMENT,
  `id_paciente` INT NOT NULL,
  `id_funcionario` INT NOT NULL,
  `data_hora_inicio` DATETIME NULL,
  `data_hora_fim` DATETIME NULL,
  `data_final_atendimento` DATETIME NULL COMMENT 'Data e hora em que o atendimento foi finalizado',
  `status` VARCHAR(50) NOT NULL,
  `tipo_encaminhamento` VARCHAR(50) NULL, 
  PRIMARY KEY (`id_atendimento`),
  INDEX `fk_atendimentos_pacientes_idx` (`id_paciente` ASC),
  INDEX `fk_atendimentos_funcionarios_idx` (`id_funcionario` ASC),
  CONSTRAINT `fk_atendimentos_pacientes`
    FOREIGN KEY (`id_paciente`)
    REFERENCES `pacientes` (`id_paciente`) 
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_atendimentos_funcionarios`
    FOREIGN KEY (`id_funcionario`)
    REFERENCES `funcionarios` (`id_funcionario`) 
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 5. Descreve a evolução do paciente dentro de um atendimento específico.
CREATE TABLE `evolucoes` (
  `id_evolucao` INT NOT NULL AUTO_INCREMENT,
  `id_atendimento` INT NOT NULL,
  `descricao` TEXT NOT NULL,
  `data_hora_registro` DATETIME NOT NULL,
  PRIMARY KEY (`id_evolucao`),
  INDEX `fk_evolucoes_atendimentos_idx` (`id_atendimento` ASC),
  CONSTRAINT `fk_evolucoes_atendimentos`
    FOREIGN KEY (`id_atendimento`)
    REFERENCES `atendimentos` (`id_atendimento`) 
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 6. Armazena as prescrições (medicamentos, atividades) vinculadas a uma evolução.
CREATE TABLE `prescricoes` (
  `id_prescricao` INT NOT NULL AUTO_INCREMENT,
  `id_evolucao` INT NOT NULL,
  `tipo` VARCHAR(50) NOT NULL,
  `descricao` TEXT NOT NULL,
  PRIMARY KEY (`id_prescricao`),
  INDEX `fk_prescricoes_evolucoes_idx` (`id_evolucao` ASC),
  CONSTRAINT `fk_prescricoes_evolucoes`
    FOREIGN KEY (`id_evolucao`)
    REFERENCES `evolucoes` (`id_evolucao`) 
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 7. Armazena o cadastro de doadores, sejam pessoas físicas ou jurídicas.
CREATE TABLE `doadores` (
  `id_doador` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(255) NOT NULL,
  `cpf_cnpj` VARCHAR(14) NOT NULL UNIQUE,
  `email` VARCHAR(255) NULL,
  `telefone` VARCHAR(20) NULL,
  `sexo` VARCHAR(15) NULL,
  `endereco` TEXT NULL,
  `data_nascimento` DATE NULL,
  `mensalidade` DECIMAL(10, 2) NULL DEFAULT 0.00,
  `dia_vencimento` INT NULL COMMENT 'Dia preferencial para vencimento dos boletos',
  PRIMARY KEY (`id_doador`)
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 8. Registra os boletos de cobrança vinculados a um doador.
CREATE TABLE `boletos` (
  `id_boleto` INT NOT NULL AUTO_INCREMENT,
  `id_doador` INT NOT NULL,
  `status` ENUM('PENDENTE', 'PAGO', 'VENCIDO') NOT NULL,
  `data_emissao` DATE NULL,
  `data_vencimento` DATE NULL,
  `valor` DECIMAL(10, 2) NULL,
  `pdf_boleto` LONGTEXT NULL COMMENT 'Armazena o caminho do arquivo no servidor (ex: uploads/boletos/doador_1/boleto_3.pdf)',
  PRIMARY KEY (`id_boleto`),
  INDEX `fk_boletos_doadores_idx` (`id_doador` ASC),
  CONSTRAINT `fk_boletos_doadores`
    FOREIGN KEY (`id_doador`)
    REFERENCES `doadores` (`id_doador`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 9. Armazena as categorias dos materiais para organização.
CREATE TABLE `categorias` (
  `id_categoria` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 10. Cadastro de itens do almoxarifado
CREATE TABLE `materiais` (
  `id_material` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(255) NOT NULL,
  `id_categoria` INT NOT NULL,
  `quantidade_atual` INT NOT NULL DEFAULT 0,
  `fornecedor` VARCHAR(255) NULL,
  `data_validade` DATE NULL, 
  PRIMARY KEY (`id_material`),
  INDEX `fk_materiais_categorias_idx` (`id_categoria` ASC),
  CONSTRAINT `fk_materiais_categorias`
    FOREIGN KEY (`id_categoria`)
    REFERENCES `categorias` (`id_categoria`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================



-- =================================================================
# 11. Registra os pedidos de material feitos pelos funcionários.
CREATE TABLE `solicitacoes_material` (
  `id_solicitacao` INT NOT NULL AUTO_INCREMENT,
  `id_funcionario_solicitante` INT NOT NULL,
  `id_material` INT NOT NULL,
  `quantidade_solicitada` INT NOT NULL,
  `tipo_saida` VARCHAR(120) NOT NULL,
  `data_solicitacao` DATETIME NOT NULL,
  `status` ENUM('Pendente', 'Aprovada', 'Recusada') NOT NULL,
  `descricao` TEXT NULL COMMENT 'Adicionado para substituir a tabela de movimentações.',
  PRIMARY KEY (`id_solicitacao`),
  INDEX `fk_solicitacoes_funcionarios_idx` (`id_funcionario_solicitante` ASC),
  INDEX `fk_solicitacoes_materiais_idx` (`id_material` ASC),
  CONSTRAINT `fk_solicitacoes_funcionarios`
    FOREIGN KEY (`id_funcionario_solicitante`)
    REFERENCES `funcionarios` (`id_funcionario`),
  CONSTRAINT `fk_solicitacoes_materiais`
    FOREIGN KEY (`id_material`)
    REFERENCES `materiais` (`id_material`)
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 12. Armazena as contas bancárias cadastradas
CREATE TABLE `contas_bancarias` (
  `id_conta` INT NOT NULL AUTO_INCREMENT,
  `nome_banco` VARCHAR(100) NOT NULL,
  `agencia` VARCHAR(20) NULL,
  `numero_conta` VARCHAR(30) NOT NULL,
  `tipo_conta` ENUM('Conta Corrente', 'Poupança', 'Conta Pagamento', 'Outra') NOT NULL,
  `saldo_inicial` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` ENUM('Ativa', 'Inativa') NOT NULL DEFAULT 'Ativa',
  `descricao` TEXT NULL,
  PRIMARY KEY (`id_conta`)
) ENGINE=InnoDB;
-- =================================================================

-- =================================================================
# 13. Organiza os tipos de entradas e despesas
CREATE TABLE `categorias_financeiras` (
  `id_categoria_financeira` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(100) NOT NULL,
  `tipo` ENUM('ENTRADA', 'SAIDA') NOT NULL,
  `descricao` TEXT NULL,
  PRIMARY KEY (`id_categoria_financeira`),
  UNIQUE (`nome`, `tipo`)
) ENGINE=InnoDB;
-- =================================================================


-- =================================================================
# 14. Registra entradas e saídas financeiras
CREATE TABLE `movimentacoes_financeiras` (
  `id_movimentacao` INT NOT NULL AUTO_INCREMENT,
  `id_conta` INT NOT NULL,
  `id_categoria_financeira` INT NOT NULL,
  `id_funcionario` INT NOT NULL,
  `tipo_movimentacao` ENUM('ENTRADA', 'SAIDA') NOT NULL,
  `valor` DECIMAL(10,2) NOT NULL,
  `data_movimentacao` DATE NOT NULL,
  `descricao` TEXT NULL,
  `forma_pagamento` VARCHAR(80) NULL,
  `comprovante` LONGTEXT NULL COMMENT 'Armazena o caminho do arquivo do comprovante',
  PRIMARY KEY (`id_movimentacao`),

  INDEX `fk_movimentacoes_contas_idx` (`id_conta`),
  INDEX `fk_movimentacoes_categorias_idx` (`id_categoria_financeira`),
  INDEX `fk_movimentacoes_funcionarios_idx` (`id_funcionario`),

  CONSTRAINT `fk_movimentacoes_contas`
    FOREIGN KEY (`id_conta`)
    REFERENCES `contas_bancarias` (`id_conta`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  CONSTRAINT `fk_movimentacoes_categorias`
    FOREIGN KEY (`id_categoria_financeira`)
    REFERENCES `categorias_financeiras` (`id_categoria_financeira`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  CONSTRAINT `fk_movimentacoes_funcionarios`
    FOREIGN KEY (`id_funcionario`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB;
-- =================================================================