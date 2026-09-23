# Study Sync

Dashboard local para importar seus registros do Clockify, acompanhar sua rotina de estudos e definir metas diárias, semanais e por matéria.

O Study Sync funciona com SQLite e guarda os dados no próprio computador. Para usar o executável no Windows, não é necessário instalar Java, Maven, PostgreSQL ou criar uma conta no sistema.

## Principais recursos

- importação de todo o histórico ou de um período específico do Clockify;
- dashboard com filtros de 7, 30 e 90 dias, além de intervalo personalizado;
- análises por dia, semana, mês, horário, dia da semana e matéria;
- identificação de matérias por tarefa, tag, descrição ou projeto do Clockify;
- metas diárias, semanais e por matéria ou tópico;
- histórico das sincronizações e mensagens de erro;
- banco SQLite local, sem servidor externo obrigatório;
- backup em JSON, restauração e exportação dos estudos em CSV;
- inicialização automática e sincronização semanal opcionais no Windows;
- integração pessoal e opcional com Notion.

## Início rápido no Windows

### O que você precisa

- Windows 10 ou superior;
- uma chave de API do Clockify;
- acesso à internet durante as importações e sincronizações;
- a pasta completa do pacote `StudySync`, não apenas o arquivo `.exe`.

O pacote pronto já inclui o Java e usa SQLite. PostgreSQL não é necessário.

> O executável gerado não fica versionado neste repositório. Para usar sem compilar, obtenha o pacote portátil distribuído pelo desenvolvedor. Para gerar seu próprio pacote, consulte [Gerando o executável](#gerando-o-executável).

### Primeira configuração

1. Extraia a pasta do pacote em um local definitivo.
2. Abra `StudySync.exe`.
3. Aguarde a dashboard abrir em `http://localhost:8080/`.
4. No assistente inicial, cole sua chave de API do Clockify.
5. Depois que a conexão for validada, abra **Sincronizações** e use **Importar tudo**.
6. Acesse **Visão geral** e **Análises** para conferir seu histórico.
7. Em **Configurações**, ajuste suas metas e faça um primeiro backup.

A primeira importação pode demorar mais, dependendo da quantidade de registros no Clockify. As próximas importações atualizam registros pelo ID e não criam duplicações.

Se o Study Sync já estiver em execução, abrir o executável novamente apenas abre a dashboard existente no navegador.

## Como usar

### Visão geral

Mostra o progresso das metas, os principais indicadores e um resumo recente dos estudos.

### Análises

Permite escolher períodos prontos ou datas personalizadas. A dashboard apresenta:

- total estudado e média diária;
- quantidade de dias ativos;
- matéria ou tópico mais estudado;
- dias da semana com mais e menos estudo;
- horário de maior foco;
- evolução diária, semanal ou mensal;
- distribuição por horário, dia da semana e matéria.

Use **Importar período** quando o intervalo escolhido ainda não estiver salvo no banco local.

### Sincronizações

- **Semana anterior:** processa a semana passada.
- **Semana atual:** processa a semana em andamento.
- **Importar tudo:** busca todo o histórico finalizado do usuário no workspace conectado.

A importação completa alimenta a dashboard local. Ela não envia todo o histórico retroativo para o Notion e não cria sincronizações semanais antigas.

### Metas

Em **Configurações**, você pode definir:

- uma meta diária;
- uma meta semanal, calculada de segunda a domingo;
- metas semanais por matéria ou tópico.

No campo de matéria, selecione um tópico, uma tag ou um projeto já identificado no histórico, ou digite um nome novo. As sugestões são agrupadas por origem e não incluem descrições livres. Para o progresso ser calculado corretamente, o nome precisa corresponder ao identificador usado nos registros importados.

### Como as matérias são identificadas

O sistema resolve os nomes do Clockify nesta ordem:

1. tarefa ou tópico;
2. tags;
3. descrição do registro;
4. projeto;
5. `Sem materia`, quando nenhum nome estiver disponível.

IDs internos do Clockify não são usados como nome de matéria. Depois de corrigir uma tarefa, tag, descrição ou projeto no Clockify, reimporte o período para atualizar o registro local.

## Seus dados

Por padrão, o banco SQLite é criado em:

```text
%USERPROFILE%\.study-sync\study-sync.db
```

O banco guarda estudos, metas, configurações e histórico de execução. Para usar outra pasta, defina a variável antes de iniciar:

```text
STUDY_SYNC_DATA_DIR=C:\caminho\para\seus-dados
```

### Backup e exportação

Na seção **Configurações > Backup e exportação**, você pode:

- baixar um backup JSON dos estudos, metas, semanas e histórico;
- restaurar um backup JSON;
- exportar os registros detalhados em CSV.

Restaurar um backup substitui os dados locais atuais. As chaves de API não são incluídas no arquivo de backup; ao migrar para outro computador, será necessário conectar o Clockify novamente.

## Clockify

A chave de API é solicitada pelo assistente na primeira abertura. O Study Sync valida a chave e identifica automaticamente o usuário e o workspace, portanto não é necessário preencher esses IDs manualmente.

Antes de começar, entre na sua conta do Clockify e copie a chave de API disponível nas configurações do seu perfil. Não compartilhe essa chave nem a publique no repositório.

A chave fica salva somente no banco local e não volta a ser exibida na interface. Ela também pode ser substituída posteriormente em **Configurações**.

Para configuração por variáveis de ambiente, use:

```text
CLOCKIFY_API_KEY=sua_chave_do_clockify
CLOCKIFY_USER_ID=seu_id_de_usuario
CLOCKIFY_WORKSPACE_ID=seu_id_de_workspace
```

Ao usar o assistente, `CLOCKIFY_USER_ID` e `CLOCKIFY_WORKSPACE_ID` são descobertos automaticamente.

## Notion

O Notion é opcional. Sem ele, a importação, a dashboard, as metas, o histórico, o backup e a exportação continuam funcionando normalmente.

Atualmente, essa integração está ligada ao modelo pessoal de Notion do autor e não é um recurso geral para outros usuários. Por isso, os campos, textos e colunas do Notion ficam ocultos em instalações comuns.

No computador do autor, o modo pessoal é ativado automaticamente quando a configuração completa já existe no banco. Depois de uma instalação limpa, ele pode ser liberado com `STUDY_SYNC_PERSONAL_NOTION_ENABLED=true`.

## Início automático no Windows

Esse recurso usa os scripts disponíveis na raiz do projeto. Com o pacote gerado em `release\StudySync`, abra `enable-windows-autostart.bat`. O script cria duas tarefas para o usuário atual:

- inicia silenciosamente a dashboard ao entrar no Windows;
- sincroniza a semana anterior toda segunda-feira às 20:00 e encerra em seguida.

Para remover as tarefas, abra `disable-windows-autostart.bat`.

Se você mover a pasta do executável, remova as tarefas e ative-as novamente no novo local. A versão portátil aberta manualmente não sincroniza apenas por ser iniciada; use os botões da interface ou configure as tarefas do Windows.

## Solução de problemas

### A dashboard não abriu

Abra `http://localhost:8080/` no navegador. Se a página não responder, encerre processos antigos do Study Sync e abra o executável novamente.

### O executável mostra `Failed to launch JVM`

Não mova apenas `StudySync.exe`. O executável depende das pastas `app` e `runtime` que ficam ao lado dele. Extraia e mantenha o pacote completo.

### A porta 8080 já está em uso

Encerre a aplicação que estiver usando a porta ou inicie o Study Sync com outra porta:

```powershell
.\StudySync.exe --server.port=8081
```

Depois, acesse `http://localhost:8081/`.

### Estudos antigos não aparecem

Abra **Sincronizações** e use **Importar tudo**. Para um intervalo específico, selecione as datas em **Análises** e use **Importar período**.

### Uma matéria não aparece corretamente

Confira a tarefa, as tags, a descrição e o projeto do registro no Clockify. Depois da correção, reimporte o período correspondente.

### A conexão com o Clockify falhou

Confira se a chave está correta, se há acesso à internet e se o usuário ainda tem acesso ao workspace. Em **Configurações**, use **Testar Clockify**.

## Desenvolvimento

### Requisitos

- JDK 21;
- Maven 3.9 ou superior;
- Git;
- PostgreSQL 16 ou Docker apenas se quiser testar o perfil PostgreSQL.

### Executando o código-fonte

```powershell
git clone https://github.com/JoseV-001/study-sync.git
cd study-sync
mvn spring-boot:run
```

No Windows, `start-study-sync.bat` também compila o JAR quando necessário, inicia a aplicação e abre a dashboard. O código-fonte usa SQLite por padrão.

Ao executar diretamente pelas propriedades padrão, a aplicação sincroniza a semana anterior na inicialização e mantém o agendamento interno de segunda-feira às 20:00. Para desenvolvimento sem automação:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--study-sync.sync-on-startup=false --study-sync.schedule.enabled=false"
```

### Testes

```powershell
mvn test
```

### Gerando o executável

Com JDK 21, `jpackage` e Maven disponíveis, execute:

```powershell
.\package-study-sync.ps1
```

O pacote será criado em:

```text
release\StudySync\StudySync.exe
release\StudySync-1.0.1-windows-x64.zip
```

Distribua o arquivo ZIP gerado. Ele contém a pasta `StudySync` completa, incluindo o executável e o runtime Java.

### PostgreSQL opcional

O PostgreSQL é suportado para desenvolvimento ou instalações específicas. Ative o perfil e configure:

```text
SPRING_PROFILES_ACTIVE=postgresql
DATABASE_URL=jdbc:postgresql://localhost:5432/study_sync
DATABASE_USERNAME=study_sync
DATABASE_PASSWORD=sua_senha
```

Para iniciar o banco de desenvolvimento com Docker:

```powershell
docker compose up -d postgres
```

As migrações do SQLite e do PostgreSQL são aplicadas automaticamente pelo Flyway.

## Configurações avançadas

As principais propriedades estão em `src/main/resources/application.properties`:

```properties
study-sync.schedule.cron=0 0 20 * * MON
study-sync.schedule.zone=America/Sao_Paulo
study-sync.schedule.enabled=true
study-sync.sync-on-startup=true
study-sync.retry.max-attempts=3
study-sync.retry.delay-ms=30000
```

As sincronizações são repetidas em caso de falha. Por padrão, são feitas até três tentativas, com intervalo de 30 segundos.

## API local

Com a aplicação em execução, os endpoints principais são:

```text
POST /sync/previous-week
POST /sync/current-week
POST /sync/week?startDate=2026-09-07
POST /sync/import?from=2026-08-17&to=2026-09-15
POST /sync/import/all

GET  /sync/analytics?from=2026-08-17&to=2026-09-15
GET  /sync/weeks
GET  /sync/history
GET  /sync/goals
GET  /sync/goals/progress
GET  /sync/goals/subjects
GET  /sync/subjects
GET  /sync/backup
GET  /sync/export/study-entries.csv
```

O endpoint `/sync/week` aceita qualquer data; o sistema encontra automaticamente a segunda-feira correspondente.

## Tecnologias

- Java 21;
- Spring Boot 4;
- Spring Data JPA;
- SQLite e PostgreSQL;
- Flyway;
- HTML, CSS e JavaScript sem framework no frontend;
- Maven e `jpackage`.

## Autor

Desenvolvido por [José Victor](https://github.com/JoseV-001).
