-- =================================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS E TABELAS PARA O SISTEMA DA ASSANDEF
-- =================================================================

CREATE DATABASE IF NOT EXISTS `assandef_db`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE `assandef_db`;

-- =================================================================
-- 1. FUNCIONÁRIOS
-- Armazena os usuários internos do sistema e sua hierarquia de acesso.
-- Hierarquia usada pelo sistema: 1=Admin/Diretoria, 2=Secretaria/Atendimento, 3=Administrativo/Auxiliar.
-- Entidade: Funcionario.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `funcionarios` (
  `id_funcionario` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do funcionário.',
  `nome_completo` VARCHAR(255) NOT NULL COMMENT 'Nome completo do funcionário.',
  `email` VARCHAR(255) NOT NULL COMMENT 'E-mail institucional ou de contato do funcionário.',
  `login` VARCHAR(80) NOT NULL COMMENT 'Login usado para autenticação no sistema.',
  `senha_hash` VARCHAR(255) NOT NULL COMMENT 'Senha criptografada do funcionário.',
  `hierarquia` INT NOT NULL COMMENT 'Nível de acesso: 1=Admin/Diretoria, 2=Secretaria/Atendimento, 3=Administrativo/Auxiliar.',
  PRIMARY KEY (`id_funcionario`),
  UNIQUE INDEX `uk_funcionarios_login` (`login`),
  INDEX `idx_funcionarios_email` (`email`),
  INDEX `idx_funcionarios_hierarquia` (`hierarquia`)
) ENGINE=InnoDB COMMENT='Usuários internos do sistema e seus níveis de permissão.';

-- =================================================================
-- 2. PACIENTES
-- Cadastro completo dos pacientes atendidos pela instituição.
-- Entidade: Paciente.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `pacientes` (
  `id_paciente` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do paciente.',
  `nome_completo` VARCHAR(255) NOT NULL COMMENT 'Nome completo do paciente.',
  `cpf` VARCHAR(11) NULL COMMENT 'CPF do paciente, armazenado preferencialmente apenas com números.',
  `rg` VARCHAR(20) NULL COMMENT 'RG do paciente.',
  `n_sus` VARCHAR(15) NULL COMMENT 'Número do Cartão Nacional de Saúde/SUS.',
  `data_nascimento` DATE NULL COMMENT 'Data de nascimento do paciente.',
  `sexo` VARCHAR(15) NULL COMMENT 'Sexo informado no cadastro.',
  `endereco` TEXT NULL COMMENT 'Endereço completo do paciente.',
  `nome_responsavel` VARCHAR(255) NULL COMMENT 'Nome do responsável pelo paciente, quando aplicável.',
  `contato_responsavel` VARCHAR(255) NULL COMMENT 'Contato do responsável pelo paciente.',
  PRIMARY KEY (`id_paciente`),
  UNIQUE INDEX `uk_pacientes_cpf` (`cpf`),
  INDEX `idx_pacientes_nome` (`nome_completo`)
) ENGINE=InnoDB COMMENT='Cadastro dos pacientes acompanhados pela ASSANDEF.';

-- =================================================================
-- 3. TELEFONES
-- Armazena um ou mais telefones vinculados a um paciente.
-- Entidade: Telefone.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `telefones` (
  `id_telefone` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do telefone.',
  `id_paciente` INT NOT NULL COMMENT 'Paciente dono do telefone.',
  `numero` VARCHAR(20) NOT NULL COMMENT 'Número de telefone ou celular.',
  `descricao` VARCHAR(50) NULL COMMENT 'Descrição do telefone, como Celular, Residencial ou Emergência.',
  PRIMARY KEY (`id_telefone`),
  INDEX `fk_telefones_pacientes_idx` (`id_paciente`),
  CONSTRAINT `fk_telefones_pacientes`
    FOREIGN KEY (`id_paciente`)
    REFERENCES `pacientes` (`id_paciente`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Telefones vinculados ao cadastro de pacientes.';

-- =================================================================
-- 4. ATENDIMENTOS
-- Registra cada atendimento realizado para um paciente por um funcionário.
-- Entidade: Atendimento.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `atendimentos` (
  `id_atendimento` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do atendimento.',
  `id_paciente` INT NOT NULL COMMENT 'Paciente atendido.',
  `id_funcionario` INT NOT NULL COMMENT 'Funcionário responsável pelo atendimento.',
  `data_hora_inicio` DATETIME NULL COMMENT 'Data e hora de início do atendimento.',
  `data_hora_fim` DATETIME NULL COMMENT 'Data e hora de fim do atendimento.',
  `data_final_atendimento` DATETIME NULL COMMENT 'Data e hora em que o atendimento foi efetivamente finalizado.',
  `status` VARCHAR(50) NULL COMMENT 'Status do atendimento, como EM_ANDAMENTO ou FINALIZADO.',
  `tipo_encaminhamento` VARCHAR(50) NULL COMMENT 'Tipo de encaminhamento associado ao atendimento.',
  PRIMARY KEY (`id_atendimento`),
  INDEX `fk_atendimentos_pacientes_idx` (`id_paciente`),
  INDEX `fk_atendimentos_funcionarios_idx` (`id_funcionario`),
  INDEX `idx_atendimentos_status` (`status`),
  CONSTRAINT `fk_atendimentos_pacientes`
    FOREIGN KEY (`id_paciente`)
    REFERENCES `pacientes` (`id_paciente`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_atendimentos_funcionarios`
    FOREIGN KEY (`id_funcionario`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Atendimentos realizados com pacientes da instituição.';

-- =================================================================
-- 5. EVOLUÇÕES
-- Descreve a evolução do paciente dentro de um atendimento específico.
-- Entidade: Evolucao.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `evolucoes` (
  `id_evolucao` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da evolução.',
  `id_atendimento` INT NOT NULL COMMENT 'Atendimento ao qual a evolução pertence.',
  `descricao` TEXT NULL COMMENT 'Descrição da evolução registrada no atendimento.',
  `data_hora_registro` DATETIME NULL COMMENT 'Data e hora em que a evolução foi registrada.',
  PRIMARY KEY (`id_evolucao`),
  INDEX `fk_evolucoes_atendimentos_idx` (`id_atendimento`),
  CONSTRAINT `fk_evolucoes_atendimentos`
    FOREIGN KEY (`id_atendimento`)
    REFERENCES `atendimentos` (`id_atendimento`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Evoluções registradas durante atendimentos.';

-- =================================================================
-- 6. PRESCRIÇÕES
-- Armazena prescrições vinculadas a uma evolução.
-- Entidade: Prescricao.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `prescricoes` (
  `id_prescricao` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da prescrição.',
  `id_evolucao` INT NOT NULL COMMENT 'Evolução à qual a prescrição pertence.',
  `tipo` VARCHAR(50) NULL COMMENT 'Tipo da prescrição, como medicamento, atividade ou orientação.',
  `descricao` TEXT NULL COMMENT 'Descrição da prescrição.',
  PRIMARY KEY (`id_prescricao`),
  INDEX `fk_prescricoes_evolucoes_idx` (`id_evolucao`),
  CONSTRAINT `fk_prescricoes_evolucoes`
    FOREIGN KEY (`id_evolucao`)
    REFERENCES `evolucoes` (`id_evolucao`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Prescrições vinculadas às evoluções dos atendimentos.';

-- =================================================================
-- 7. DOADORES
-- Cadastro de doadores, sejam pessoas físicas ou jurídicas.
-- Entidade: Doador.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `doadores` (
  `id_doador` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do doador.',
  `nome` VARCHAR(255) NULL COMMENT 'Nome completo ou razão social do doador.',
  `cpf_cnpj` VARCHAR(14) NULL COMMENT 'CPF ou CNPJ do doador, preferencialmente apenas com números.',
  `email` VARCHAR(255) NULL COMMENT 'E-mail de contato do doador.',
  `telefone` VARCHAR(20) NULL COMMENT 'Telefone de contato do doador.',
  `sexo` VARCHAR(15) NULL COMMENT 'Sexo do doador, quando aplicável.',
  `endereco` TEXT NULL COMMENT 'Endereço do doador.',
  `data_nascimento` DATE NULL COMMENT 'Data de nascimento do doador, quando pessoa física.',
  `data_cadastro` DATE NULL COMMENT 'Data em que o doador foi cadastrado.',
  `mensalidade` DECIMAL(10,2) NULL COMMENT 'Valor de mensalidade ou contribuição recorrente.',
  `dia_vencimento` INT NULL COMMENT 'Dia de vencimento preferencial para cobranças.',
  PRIMARY KEY (`id_doador`),
  INDEX `idx_doadores_nome` (`nome`),
  INDEX `idx_doadores_cpf_cnpj` (`cpf_cnpj`),
  INDEX `idx_doadores_email` (`email`)
) ENGINE=InnoDB COMMENT='Cadastro de pessoas e empresas doadoras.';

-- =================================================================
-- 8. BOLETOS
-- Registra boletos de cobrança vinculados a doadores.
-- Entidade: Boleto.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `boletos` (
  `id_boleto` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do boleto.',
  `id_doador` INT NULL COMMENT 'Doador vinculado ao boleto.',
  `status` ENUM('PENDENTE', 'PAGO', 'VENCIDO') NULL COMMENT 'Status do boleto.',
  `data_emissao` DATE NULL COMMENT 'Data de emissão do boleto.',
  `data_vencimento` DATE NULL COMMENT 'Data de vencimento do boleto.',
  `valor` DECIMAL(10,2) NULL COMMENT 'Valor cobrado no boleto.',
  `pdf_boleto` LONGTEXT NULL COMMENT 'Conteúdo ou referência do PDF do boleto, conforme implementação do sistema.',
  PRIMARY KEY (`id_boleto`),
  INDEX `fk_boletos_doadores_idx` (`id_doador`),
  INDEX `idx_boletos_status` (`status`),
  CONSTRAINT `fk_boletos_doadores`
    FOREIGN KEY (`id_doador`)
    REFERENCES `doadores` (`id_doador`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Boletos gerados para doadores.';

-- =================================================================
-- 9. CATEGORIAS
-- Categorias usadas para organizar materiais no almoxarifado.
-- Entidade: Categoria.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `categorias` (
  `id_categoria` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da categoria.',
  `nome` VARCHAR(100) NOT NULL COMMENT 'Nome da categoria de material.',
  PRIMARY KEY (`id_categoria`),
  UNIQUE INDEX `uk_categorias_nome` (`nome`)
) ENGINE=InnoDB COMMENT='Categorias de materiais do almoxarifado.';

-- =================================================================
-- 10. MATERIAIS
-- Cadastro de itens do almoxarifado.
-- Entidade: Material.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `materiais` (
  `id_material` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do material.',
  `nome` VARCHAR(255) NOT NULL COMMENT 'Nome do material.',
  `id_categoria` INT NOT NULL COMMENT 'Categoria à qual o material pertence.',
  `quantidade_atual` INT NOT NULL DEFAULT 0 COMMENT 'Quantidade atual disponível em estoque.',
  `fornecedor` VARCHAR(255) NULL COMMENT 'Fornecedor do material.',
  `data_validade` DATE NULL COMMENT 'Data de validade do material, quando aplicável.',
  PRIMARY KEY (`id_material`),
  INDEX `fk_materiais_categorias_idx` (`id_categoria`),
  INDEX `idx_materiais_nome` (`nome`),
  CONSTRAINT `fk_materiais_categorias`
    FOREIGN KEY (`id_categoria`)
    REFERENCES `categorias` (`id_categoria`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Itens cadastrados no almoxarifado.';

-- =================================================================
-- 11. SOLICITAÇÕES DE MATERIAL
-- Registra pedidos de materiais feitos por funcionários.
-- Entidade: SolicitacoesMaterial.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `solicitacoes_material` (
  `id_solicitacao` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da solicitação de material.',
  `id_funcionario_solicitante` INT NULL COMMENT 'Funcionário que realizou a solicitação.',
  `id_material` INT NULL COMMENT 'Material solicitado.',
  `quantidade_solicitada` INT NULL COMMENT 'Quantidade solicitada do material.',
  `tipo_saida` VARCHAR(120) NULL COMMENT 'Tipo ou finalidade da saída do material.',
  `data_solicitacao` DATETIME NULL COMMENT 'Data e hora em que a solicitação foi registrada.',
  `status` ENUM('PENDENTE', 'APROVADA', 'REJEITADA', 'ENTREGUE') NULL COMMENT 'Status da solicitação de material.',
  `descricao` TEXT NULL COMMENT 'Descrição ou observação da solicitação.',
  PRIMARY KEY (`id_solicitacao`),
  INDEX `fk_solicitacoes_material_funcionarios_idx` (`id_funcionario_solicitante`),
  INDEX `fk_solicitacoes_material_materiais_idx` (`id_material`),
  INDEX `idx_solicitacoes_material_status` (`status`),
  CONSTRAINT `fk_solicitacoes_material_funcionarios`
    FOREIGN KEY (`id_funcionario_solicitante`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_solicitacoes_material_materiais`
    FOREIGN KEY (`id_material`)
    REFERENCES `materiais` (`id_material`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Solicitações de materiais realizadas por funcionários.';

-- =================================================================
-- 12. CONTAS BANCÁRIAS
-- Armazena as contas bancárias cadastradas no módulo financeiro.
-- Entidade: ContaBancaria.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `contas_bancarias` (
  `id_conta` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da conta bancária.',
  `nome_banco` VARCHAR(100) NULL COMMENT 'Nome do banco ou caixa interno.',
  `agencia` VARCHAR(20) NULL COMMENT 'Agência bancária.',
  `numero_conta` VARCHAR(30) NULL COMMENT 'Número da conta bancária.',
  `tipo_conta` ENUM('CORRENTE', 'POUPANCA', 'CAIXA') NULL COMMENT 'Tipo da conta conforme enum da aplicação.',
  `saldo` DECIMAL(10,2) NULL COMMENT 'Saldo atual da conta, atualizado pelas movimentações financeiras.',
  `status` ENUM('ATIVA', 'INATIVA') NULL COMMENT 'Status da conta bancária.',
  `descricao` TEXT NULL COMMENT 'Descrição ou observação sobre a conta.',
  PRIMARY KEY (`id_conta`),
  INDEX `idx_contas_bancarias_status` (`status`)
) ENGINE=InnoDB COMMENT='Contas bancárias e caixas usados no controle financeiro.';

-- =================================================================
-- 13. CATEGORIA FINANCEIRA
-- Organiza categorias de receitas e despesas do módulo financeiro.
-- Entidade: CategoriaFinanceira.java
-- IMPORTANTE: o nome correto da tabela no projeto é `categoria_financeira`.
-- =================================================================
CREATE TABLE IF NOT EXISTS `categoria_financeira` (
  `id_categoria_financeira` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da categoria financeira.',
  `nome` VARCHAR(100) NOT NULL COMMENT 'Nome da categoria financeira.',
  `tipo` ENUM('RECEITA', 'DESPESA') NOT NULL COMMENT 'Tipo da categoria: RECEITA ou DESPESA.',
  `descricao` TEXT NULL COMMENT 'Descrição da categoria financeira.',
  PRIMARY KEY (`id_categoria_financeira`),
  UNIQUE INDEX `uk_categoria_financeira_nome_tipo` (`nome`, `tipo`),
  INDEX `idx_categoria_financeira_tipo` (`tipo`)
) ENGINE=InnoDB COMMENT='Categorias financeiras usadas em entradas e saídas.';

-- =================================================================
-- 14. MOVIMENTAÇÕES FINANCEIRAS
-- Registra entradas e saídas financeiras vinculadas a uma conta, categoria e funcionário.
-- Entidade: MovimentacaoFinanceira.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `movimentacoes_financeiras` (
  `id_movimentacao` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da movimentação financeira.',
  `id_conta` INT NOT NULL COMMENT 'Conta bancária movimentada.',
  `id_categoria_financeira` INT NOT NULL COMMENT 'Categoria financeira da movimentação.',
  `id_funcionario` INT NULL COMMENT 'Funcionário responsável pelo registro.',
  `tipo_movimentacao` ENUM('ENTRADA', 'SAIDA') NOT NULL COMMENT 'Tipo da movimentação financeira.',
  `valor` DECIMAL(10,2) NOT NULL COMMENT 'Valor movimentado.',
  `data_movimentacao` DATE NOT NULL COMMENT 'Data da movimentação.',
  `descricao` TEXT NULL COMMENT 'Descrição ou observação da movimentação.',
  PRIMARY KEY (`id_movimentacao`),
  INDEX `fk_movimentacoes_contas_idx` (`id_conta`),
  INDEX `fk_movimentacoes_categoria_financeira_idx` (`id_categoria_financeira`),
  INDEX `fk_movimentacoes_funcionarios_idx` (`id_funcionario`),
  INDEX `idx_movimentacoes_tipo` (`tipo_movimentacao`),
  INDEX `idx_movimentacoes_data` (`data_movimentacao`),
  CONSTRAINT `fk_movimentacoes_contas`
    FOREIGN KEY (`id_conta`)
    REFERENCES `contas_bancarias` (`id_conta`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_movimentacoes_categoria_financeira`
    FOREIGN KEY (`id_categoria_financeira`)
    REFERENCES `categoria_financeira` (`id_categoria_financeira`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_movimentacoes_funcionarios`
    FOREIGN KEY (`id_funcionario`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Entradas e saídas registradas no módulo financeiro.';

-- =================================================================
-- 15. PUBLICAÇÕES
-- Armazena notícias, eventos e artigos exibidos no site/página pública.
-- Entidade: Publicacao.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `publicacoes` (
  `id_publicacao` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da publicação.',
  `id_funcionario_autor` INT NULL COMMENT 'Funcionário autor da publicação.',
  `titulo` VARCHAR(180) NOT NULL COMMENT 'Título da publicação.',
  `descricao` TEXT NOT NULL COMMENT 'Descrição resumida da publicação.',
  `conteudo` LONGTEXT NULL COMMENT 'Conteúdo completo da publicação.',
  `tipo_conteudo` ENUM('NOTICIA', 'EVENTO', 'ARTIGO') NOT NULL COMMENT 'Tipo de conteúdo da publicação.',
  `data_criacao` DATETIME NULL COMMENT 'Data e hora em que a publicação foi criada.',
  `data_atualizacao` DATETIME NULL COMMENT 'Data e hora da última atualização.',
  `data_publicacao` DATETIME NULL COMMENT 'Data e hora em que a publicação foi publicada.',
  `data_evento` DATETIME NULL COMMENT 'Data e hora do evento, quando a publicação for do tipo EVENTO.',
  `local_evento` VARCHAR(255) NULL COMMENT 'Local do evento, quando aplicável.',
  PRIMARY KEY (`id_publicacao`),
  INDEX `fk_publicacoes_funcionarios_idx` (`id_funcionario_autor`),
  INDEX `idx_publicacoes_tipo` (`tipo_conteudo`),
  INDEX `idx_publicacoes_data_publicacao` (`data_publicacao`),
  CONSTRAINT `fk_publicacoes_funcionarios`
    FOREIGN KEY (`id_funcionario_autor`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Publicações de notícias, eventos e artigos da instituição.';

-- =================================================================
-- 16. IMAGENS DAS PUBLICAÇÕES
-- Armazena metadados das imagens anexadas às publicações.
-- Entidade: PublicacaoImagem.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `publicacoes_imagens` (
  `id_imagem` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da imagem.',
  `id_publicacao` INT NOT NULL COMMENT 'Publicação à qual a imagem pertence.',
  `caminho_arquivo` VARCHAR(500) NOT NULL COMMENT 'Caminho do arquivo de imagem salvo no servidor.',
  `nome_original` VARCHAR(255) NULL COMMENT 'Nome original do arquivo enviado.',
  `tipo_mime` VARCHAR(50) NOT NULL COMMENT 'Tipo MIME da imagem.',
  `tamanho_bytes` BIGINT NOT NULL COMMENT 'Tamanho do arquivo em bytes.',
  `ordem_exibicao` SMALLINT NOT NULL DEFAULT 1 COMMENT 'Ordem de exibição da imagem na publicação.',
  `imagem_principal` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica se a imagem é a imagem principal da publicação.',
  `texto_alternativo` VARCHAR(255) NULL COMMENT 'Texto alternativo usado para acessibilidade.',
  `data_upload` DATETIME NULL COMMENT 'Data e hora do upload da imagem.',
  PRIMARY KEY (`id_imagem`),
  INDEX `fk_publicacoes_imagens_publicacoes_idx` (`id_publicacao`),
  INDEX `idx_publicacoes_imagens_ordem` (`id_publicacao`, `ordem_exibicao`),
  CONSTRAINT `fk_publicacoes_imagens_publicacoes`
    FOREIGN KEY (`id_publicacao`)
    REFERENCES `publicacoes` (`id_publicacao`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Imagens vinculadas às publicações.';

-- =================================================================
-- 17. VÍDEOS DAS PUBLICAÇÕES
-- Armazena links de vídeos do YouTube vinculados às publicações.
-- Entidade: PublicacaoVideo.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `publicacoes_videos` (
  `id_video` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do vídeo.',
  `id_publicacao` INT NOT NULL COMMENT 'Publicação à qual o vídeo pertence.',
  `url_youtube` VARCHAR(500) NOT NULL COMMENT 'URL do vídeo no YouTube.',
  `titulo_video` VARCHAR(180) NULL COMMENT 'Título opcional do vídeo.',
  `ordem_exibicao` SMALLINT NOT NULL DEFAULT 1 COMMENT 'Ordem de exibição do vídeo na publicação.',
  `data_cadastro` DATETIME NULL COMMENT 'Data e hora em que o vídeo foi cadastrado.',
  PRIMARY KEY (`id_video`),
  INDEX `fk_publicacoes_videos_publicacoes_idx` (`id_publicacao`),
  INDEX `idx_publicacoes_videos_ordem` (`id_publicacao`, `ordem_exibicao`),
  UNIQUE INDEX `uk_publicacoes_videos_publicacao_url` (`id_publicacao`, `url_youtube`),
  CONSTRAINT `fk_publicacoes_videos_publicacoes`
    FOREIGN KEY (`id_publicacao`)
    REFERENCES `publicacoes` (`id_publicacao`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Vídeos do YouTube vinculados às publicações.';

-- =================================================================
-- 18. TOKENS DE RECUPERAÇÃO DE SENHA
-- Armazena tokens temporários para redefinição de senha de funcionários.
-- Entidade: PasswordResetToken.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
  `id_token` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único do token.',
  `id_funcionario` INT NOT NULL COMMENT 'Funcionário que solicitou a recuperação de senha.',
  `token_hash` VARCHAR(128) NOT NULL COMMENT 'Hash SHA-256 do token de recuperação.',
  `expires_at` DATETIME NOT NULL COMMENT 'Data e hora de expiração do token.',
  `used_at` DATETIME NULL COMMENT 'Data e hora de uso do token, quando utilizado.',
  `created_at` DATETIME NOT NULL COMMENT 'Data e hora de criação do token.',
  PRIMARY KEY (`id_token`),
  UNIQUE INDEX `uk_password_reset_tokens_token_hash` (`token_hash`),
  INDEX `fk_password_reset_tokens_funcionarios_idx` (`id_funcionario`),
  INDEX `idx_password_reset_tokens_expires_at` (`expires_at`),
  CONSTRAINT `fk_password_reset_tokens_funcionarios`
    FOREIGN KEY (`id_funcionario`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Tokens usados no fluxo de recuperação de senha.';

-- =================================================================
-- 19. SOLICITAÇÕES DE ALUGUEL DO SALÃO
-- Armazena as solicitações enviadas pelo formulário público de aluguel do salão.
-- Novo fluxo: o usuário informa a data e o horário desejados, sem depender
-- de cadastro prévio de disponibilidades pela secretaria.
-- Entidade: SolicitacaoAluguelSalao.java
-- =================================================================
CREATE TABLE IF NOT EXISTS `solicitacoes_aluguel_salao` (
  `id_solicitacao` INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único da solicitação de aluguel.',
  `nome_responsavel` VARCHAR(255) NOT NULL COMMENT 'Nome do responsável pela solicitação.',
  `tipo_documento` ENUM('CPF', 'CNPJ') NOT NULL COMMENT 'Tipo de documento informado pelo solicitante.',
  `documento` VARCHAR(18) NOT NULL COMMENT 'CPF ou CNPJ informado pelo solicitante.',
  `celular` VARCHAR(20) NOT NULL COMMENT 'Celular para contato com o solicitante.',
  `email` VARCHAR(255) NOT NULL COMMENT 'E-mail para contato com o solicitante.',
  `data_desejada` DATE NOT NULL COMMENT 'Data desejada pelo usuário para aluguel do salão.',
  `hora_inicio_desejada` TIME NOT NULL COMMENT 'Horário inicial desejado para a locação.',
  `hora_fim_desejada` TIME NOT NULL COMMENT 'Horário final desejado para a locação.',
  `motivo_aluguel` TEXT NOT NULL COMMENT 'Finalidade ou motivo do aluguel do salão.',
  `valor_apresentado` DECIMAL(10,2) NULL COMMENT 'Valor informado/apresentado pela secretaria, quando aplicável.',
  `status` ENUM('PENDENTE', 'EM_CONTATO', 'ALUGADO', 'RECUSADA', 'CANCELADA') NOT NULL DEFAULT 'PENDENTE' COMMENT 'Status da solicitação de aluguel.',
  `observacao_secretaria` TEXT NULL COMMENT 'Observações internas da secretaria sobre a solicitação.',
  `id_funcionario_responsavel` INT NULL COMMENT 'Funcionário que analisou ou acompanhou a solicitação.',
  `data_solicitacao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data e hora em que a solicitação foi enviada.',
  `data_analise` DATETIME NULL COMMENT 'Data e hora em que a solicitação foi analisada.',
  `data_atualizacao` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Data e hora da última atualização.',
  PRIMARY KEY (`id_solicitacao`),
  INDEX `fk_solicitacoes_aluguel_funcionarios_idx` (`id_funcionario_responsavel`),
  INDEX `idx_solicitacoes_aluguel_status` (`status`),
  INDEX `idx_solicitacoes_aluguel_documento` (`documento`),
  INDEX `idx_solicitacoes_aluguel_email` (`email`),
  INDEX `idx_solicitacoes_aluguel_data` (`data_desejada`),
  INDEX `idx_solicitacoes_aluguel_data_horario` (`data_desejada`, `hora_inicio_desejada`, `hora_fim_desejada`),
  CONSTRAINT `fk_solicitacoes_aluguel_funcionarios`
    FOREIGN KEY (`id_funcionario_responsavel`)
    REFERENCES `funcionarios` (`id_funcionario`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_solicitacoes_aluguel_horario` CHECK (`hora_fim_desejada` > `hora_inicio_desejada`),
  CONSTRAINT `chk_solicitacoes_aluguel_valor` CHECK (`valor_apresentado` IS NULL OR `valor_apresentado` >= 0)
) ENGINE=InnoDB COMMENT='Solicitações públicas de aluguel do salão da ASSANDEF.';
