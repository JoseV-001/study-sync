# Study Sync

Importa estudos do Clockify, guarda tudo localmente e mostra seu progresso em uma dashboard. O Notion e opcional.

## Como funciona

Por padrão, ao abrir a aplicação ela sincroniza a semana anterior. Isso permite abrir o projeto na segunda à tarde ou à noite e já deixar o Notion atualizado. Tambem e possivel configurar tarefas do Windows para deixar a dashboard disponivel no login e sincronizar automaticamente toda segunda-feira as 20:00, mesmo que ela nao esteja aberta.

Também é possível executar manualmente enquanto a aplicação estiver rodando:

```text
POST http://localhost:8080/sync/previous-week
POST http://localhost:8080/sync/current-week
POST http://localhost:8080/sync/week?startDate=2026-09-07
```

Consultas disponíveis:

```text
GET http://localhost:8080/sync/history
GET http://localhost:8080/sync/weeks
GET http://localhost:8080/sync/weeks?from=2026-01-01&to=2026-09-14
GET http://localhost:8080/sync/analytics?from=2026-08-17&to=2026-09-15
```

Para trazer os registros detalhados do Clockify para um período, use:

```text
POST http://localhost:8080/sync/import?from=2026-08-17&to=2026-09-15
```

A dashboard oferece os mesmos controles, com filtros de 7, 30 e 90 dias ou intervalo personalizado. Ela mostra totais e média diária, dias ativos, dias da semana com mais e menos estudo, horário de maior foco, matérias e tópicos mais estudados e gráficos por dia, semana, mês, horário, dia da semana e matéria. A importação solicita todos os registros do período ao Clockify, inclusive quando há mais de uma página de resultados.

Para identificar a matéria, o sistema resolve os nomes reais do Clockify e usa, nesta ordem: tópico (tarefa), tags, descrição e projeto. Identificadores internos não são exibidos. Reimporte um período já salvo para atualizar os registros antigos com os nomes resolvidos.

As sincronizações tentam novamente em caso de erro. O padrão é de 3 tentativas, com intervalo de 30 segundos, configurável em `application.properties`:

```properties
study-sync.retry.max-attempts=3
study-sync.retry.delay-ms=30000
```

O endpoint `/sync/week` aceita qualquer data da semana; o sistema encontra automaticamente a segunda-feira daquela semana.

## Requisitos

Para usar o executável no Windows, você precisa de:

- Windows 10 ou superior.
- Uma chave da API do Clockify para concluir a configuracao inicial.

Nao e necessario instalar Java, Maven ou PostgreSQL para usar o executavel portatil. Eles so sao necessarios para executar o codigo-fonte ou gerar um novo pacote.

O Notion é opcional. Sem ele, o sistema continua importando, analisando e armazenando os estudos localmente.

## Configuração

### Banco local

Por padrao, o Study Sync usa SQLite. O arquivo do banco e criado automaticamente em:

```text
%USERPROFILE%\.study-sync\study-sync.db
```

Esse arquivo guarda estudos, metas, integracoes e historico. Para usar outra pasta, defina a variavel de ambiente abaixo antes de iniciar:

```text
STUDY_SYNC_DATA_DIR=C:\caminho\para\seus-dados
```

As tabelas sao criadas e atualizadas automaticamente pelo Flyway na inicializacao.

### PostgreSQL avancado

O PostgreSQL continua disponivel para desenvolvimento ou instalacoes que precisem dele. Ative o perfil `postgresql` e configure:

```text
SPRING_PROFILES_ACTIVE=postgresql
DATABASE_URL=jdbc:postgresql://localhost:5432/study_sync
DATABASE_USERNAME=study_sync
DATABASE_PASSWORD=sua_senha_do_postgresql
```

Para subir um banco de desenvolvimento com Docker:

```text
docker compose up -d postgres
```

### Clockify e Notion

Na primeira abertura, o Study Sync mostra um assistente para conectar o Clockify. Cole a chave da API, e o sistema valida a conexao, identifica automaticamente seu usuario e seu workspace, e salva essa configuracao no banco local.

Voce nao precisa informar manualmente IDs de usuario ou workspace do Clockify.

Tambem e possivel definir as credenciais por variaveis de ambiente antes de iniciar, caso prefira uma configuracao avancada:

```text
CLOCKIFY_API_KEY=sua_chave_do_clockify
CLOCKIFY_USER_ID=seu_id_de_usuario_do_clockify
CLOCKIFY_WORKSPACE_ID=seu_id_de_workspace_do_clockify
NOTION_API_KEY=sua_chave_do_notion
```

Essa configuracao avancada tambem exige `CLOCKIFY_USER_ID` e `CLOCKIFY_WORKSPACE_ID`. Para a instalacao comum, use o assistente e nao sera necessario informar esses IDs. `NOTION_API_KEY` pode ser deixada vazia quando voce quiser usar apenas o armazenamento e os graficos locais. A chave do Notion tambem pode ser adicionada depois em **Configuracoes**.

Para conectar outro workspace do Notion, crie uma integration interna no Notion, compartilhe a fonte de dados semanal com ela e, em **Configuracoes**, informe a chave, o ID da fonte e os nomes de duas colunas: uma coluna de data para o inicio da semana e uma coluna de texto para as horas estudadas. Assim, o projeto nao depende mais dos nomes ou IDs do workspace original.

Na primeira importação, o sistema também consulta os nomes de projetos, tarefas e tags do Clockify para evitar que os gráficos exibam identificadores internos.

O horário automático pode ser alterado em `application.properties`:

```properties
study-sync.schedule.cron=0 0 20 * * MON
study-sync.schedule.zone=America/Sao_Paulo
study-sync.schedule.enabled=true
study-sync.sync-on-startup=true
```

## Como iniciar

### Executável portátil no Windows

1. Abra `release\\StudySync\\StudySync.exe` com duplo clique.
2. Na primeira abertura, conecte sua conta do Clockify pelo assistente.
3. Acesse `http://localhost:8080/` quando a aplicacao estiver pronta.

O executavel inclui o Java e o SQLite. Nenhum banco externo e necessario.

### Backup e exportacao

Na aba **Configuracoes**, a secao **Backup e exportacao** permite:

- baixar um backup JSON com estudos, metas, semanas sincronizadas e historico;
- restaurar esse mesmo arquivo, substituindo os dados locais apos confirmacao;
- exportar os registros detalhados em CSV, compativel com planilhas.

As chaves do Clockify e do Notion nao fazem parte do backup. Depois de restaurar em outro computador, conecte o Clockify novamente pelo assistente inicial.

### Inicio automatico no Windows

Depois de gerar o executavel, abra `enable-windows-autostart.bat` com duplo clique e aceite a confirmacao do Windows. Ele cria duas tarefas para o usuario atual:

- inicia a dashboard silenciosamente quando voce entra no Windows;
- executa uma sincronizacao sem abrir o navegador toda segunda-feira as 20:00 e encerra ao terminar.

Para remover as tarefas, abra `disable-windows-autostart.bat`. Os arquivos usam o executavel em `release\StudySync\StudySync.exe`; se voce mover o pacote, execute novamente o arquivo de ativacao na nova pasta.

### Código-fonte

Para iniciar localmente:

```text
./mvnw spring-boot:run
```

No Windows, tambem e possivel iniciar com duplo clique em `start-study-sync.bat`. O script compila o JAR na primeira execucao, inicia a aplicacao e abre a dashboard automaticamente.

Com a aplicação iniciada, abra `http://localhost:8080/` para acessar a dashboard. Ela mostra as semanas salvas, o histórico das execuções e permite disparar manualmente a sincronização da semana atual ou anterior.

Se o sistema nao abrir, confira se a pasta configurada em `STUDY_SYNC_DATA_DIR` permite criacao de arquivos. Sem essa variavel, o sistema usa a pasta local do seu usuario.

Para gerar uma versao nativa portatil com Java incluido, execute `package-study-sync.ps1`. O executavel sera criado em `release\StudySync\StudySync.exe` e funcionara sem PostgreSQL no computador de destino.
