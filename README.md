# Study Sync

Sincroniza as horas estudadas no Clockify com o registro semanal do Notion.

## Como funciona

Por padrão, ao abrir a aplicação ela sincroniza a semana anterior. Isso permite abrir o projeto na segunda à tarde ou à noite e já deixar o Notion atualizado. Como segunda camada, a aplicação também tenta sincronizar automaticamente toda segunda-feira às 20:00, no horário de São Paulo.

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
- PostgreSQL instalado e em execução.
- Um banco chamado `study_sync`.
- Um usuário do PostgreSQL com permissão para criar e atualizar tabelas.
- Uma chave da API do Clockify para concluir a configuracao inicial.

Java e Maven não são necessários para usar o executável portátil. Eles só são necessários para executar o código-fonte ou gerar um novo pacote.

O Notion é opcional. A integração atual depende de um modelo específico de workspace e funciona no ambiente original do projeto, mas ainda não é compatível com workspaces de outras pessoas. Sem a chave do Notion, o sistema continua importando, analisando e armazenando os estudos localmente.

## Configuração

### PostgreSQL

O projeto usa PostgreSQL para manter os estudos, o resumo semanal e o histórico das sincronizações. O serviço precisa estar iniciado antes de abrir a aplicação. Para subir o banco localmente com Docker:

```text
docker compose up -d postgres
```

Configure a conexão com variáveis de ambiente. Os valores esperados são:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/study_sync
DATABASE_USERNAME=study_sync
DATABASE_PASSWORD=sua_senha_do_postgresql
```

As tabelas são criadas e atualizadas automaticamente pelo Flyway na inicialização da aplicação.

Se você usar outro endereço, banco, usuário ou senha, defina `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` antes de iniciar. A senha deve ser a senha do usuário do PostgreSQL configurado no seu computador.

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

Na primeira importação, o sistema também consulta os nomes de projetos, tarefas e tags do Clockify para evitar que os gráficos exibam identificadores internos.

O horário automático pode ser alterado em `application.properties`:

```properties
study-sync.schedule.cron=0 0 20 * * MON
study-sync.schedule.zone=America/Sao_Paulo
study-sync.sync-on-startup=true
```

## Como iniciar

### Executável portátil no Windows

1. Inicie o serviço do PostgreSQL.
2. Configure `DATABASE_PASSWORD` nas variáveis de ambiente do Windows.
3. Abra `dist\\StudySync\\StudySync.exe` com duplo clique.
4. Na primeira abertura, conecte sua conta do Clockify pelo assistente.
5. Acesse `http://localhost:8080/` quando a aplicação estiver pronta.

O executável já inclui o Java e não exige Maven. Ele ainda depende do PostgreSQL local e das configurações de ambiente descritas acima.

### Código-fonte

Para iniciar localmente:

```text
./mvnw spring-boot:run
```

No Windows, também é possível iniciar com duplo clique em `start-study-sync.bat`. O script verifica o PostgreSQL, compila o JAR na primeira execução, inicia a aplicação e abre a dashboard automaticamente.

Com a aplicação iniciada, abra `http://localhost:8080/` para acessar a dashboard. Ela mostra as semanas salvas, o histórico das execuções e permite disparar manualmente a sincronização da semana atual ou anterior.

Se o sistema não abrir, confira primeiro se o PostgreSQL está em execução e se `DATABASE_PASSWORD` corresponde ao usuário configurado no banco.

Para gerar uma versão nativa portátil com Java incluído, execute `package-study-sync.ps1`. O executável será criado em `dist\StudySync\StudySync.exe`. Essa distribuição não exige Maven nem Java instalado no computador de destino, mas ainda utiliza o PostgreSQL local.
