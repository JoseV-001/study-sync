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

O endpoint `/sync/week` aceita qualquer data da semana; o sistema encontra automaticamente a segunda-feira daquela semana.

## Configuração

Defina as credenciais antes de iniciar:

```text
CLOCKIFY_API_KEY=...
NOTION_API_KEY=...
```

O projeto usa PostgreSQL para manter o resumo semanal e o histórico das sincronizações. Para subir o banco localmente com Docker:

```text
docker compose up -d postgres
```

Os valores padrão são:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/study_sync
DATABASE_USERNAME=study_sync
DATABASE_PASSWORD=study_sync
```

As tabelas são criadas e atualizadas automaticamente pelo Flyway na inicialização da aplicação.

Para usar outro banco, defina `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` antes de iniciar.

O horário automático pode ser alterado em `application.properties`:

```properties
study-sync.schedule.cron=0 0 20 * * MON
study-sync.schedule.zone=America/Sao_Paulo
study-sync.sync-on-startup=true
```

Para iniciar localmente:

```text
./mvnw spring-boot:run
```
