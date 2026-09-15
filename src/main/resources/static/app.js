const state = {
    weeks: [],
    history: []
};

const elements = {
    averageHours: document.querySelector('#average-hours'),
    clockifyApiKey: document.querySelector('#clockify-api-key'),
    currentWeekButton: document.querySelector('#current-week-button'),
    historyState: document.querySelector('#history-state'),
    historyTableBody: document.querySelector('#history-table-body'),
    lastRefresh: document.querySelector('#last-refresh'),
    lastStatus: document.querySelector('#last-status'),
    lastStatusDetail: document.querySelector('#last-status-detail'),
    periodHours: document.querySelector('#period-hours'),
    periodRange: document.querySelector('#period-range'),
    previousWeekButton: document.querySelector('#previous-week-button'),
    refreshButton: document.querySelector('#refresh-button'),
    saveSettingsButton: document.querySelector('#save-settings-button'),
    settingsForm: document.querySelector('#settings-form'),
    settingsMessage: document.querySelector('#settings-message'),
    settingsState: document.querySelector('#settings-state'),
    syncMessage: document.querySelector('#sync-message'),
    notionApiKey: document.querySelector('#notion-api-key'),
    weeksCount: document.querySelector('#weeks-count'),
    weeksState: document.querySelector('#weeks-state'),
    weeksTableBody: document.querySelector('#weeks-table-body')
};

function renderSettings(settings) {
    const clockify = settings.clockifyConfigured ? 'Clockify configurado' : 'Clockify pendente';
    const notion = settings.notionConfigured ? 'Notion configurado' : 'Notion opcional';
    elements.settingsState.textContent = `${clockify} - ${notion}`;
}

function parseDate(value) {
    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
}

function formatDate(value) {
    if (!value) return '--';
    return parseDate(value).toLocaleDateString('pt-BR');
}

function formatDateTime(value) {
    if (!value) return '--';
    return new Date(value).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

function formatMinutes(minutes) {
    const totalMinutes = Number(minutes || 0);
    const hours = Math.floor(totalMinutes / 60);
    const remainingMinutes = totalMinutes % 60;
    return `${hours}h ${String(remainingMinutes).padStart(2, '0')}min`;
}

function formatHours(minutes) {
    return `${(Number(minutes || 0) / 60).toLocaleString('pt-BR', { maximumFractionDigits: 1 })}h`;
}

function statusLabel(status) {
    return { SUCCESS: 'Concluida', RUNNING: 'Em andamento', FAILED: 'Falhou' }[status] || status || '--';
}

function setEmptyState(element, colspan, message, error = false) {
    element.innerHTML = '';
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = colspan;
    cell.className = error ? 'empty-state error-text' : 'empty-state';
    cell.textContent = message;
    row.appendChild(cell);
    element.appendChild(row);
}

function renderWeeks() {
    const weeks = state.weeks;
    elements.weeksCount.textContent = weeks.length;
    elements.weeksState.textContent = `${weeks.length} registro${weeks.length === 1 ? '' : 's'}`;

    if (!weeks.length) {
        setEmptyState(elements.weeksTableBody, 4, 'Nenhuma semana sincronizada no periodo.');
        return;
    }

    const totalMinutes = weeks.reduce((sum, week) => sum + Number(week.totalMinutes || 0), 0);
    const averageMinutes = totalMinutes / weeks.length;
    elements.periodHours.textContent = formatHours(totalMinutes);
    elements.averageHours.textContent = formatHours(averageMinutes);
    elements.periodRange.textContent = `${formatDate(weeks[weeks.length - 1].weekStartDate)} a ${formatDate(weeks[0].weekEndDate)}`;

    elements.weeksTableBody.innerHTML = '';
    weeks.forEach((week) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(week.weekStartDate)} - ${formatDate(week.weekEndDate)}</td>
            <td><strong>${formatMinutes(week.totalMinutes)}</strong></td>
            <td>${week.notionTime || '--'}</td>
            <td>${formatDateTime(week.syncedAt)}</td>`;
        elements.weeksTableBody.appendChild(row);
    });
}

function renderHistory() {
    const history = state.history;
    elements.historyState.textContent = `${history.length} execucao${history.length === 1 ? '' : 'es'}`;
    if (!history.length) {
        elements.lastStatus.textContent = '--';
        elements.lastStatusDetail.textContent = 'Nenhum historico encontrado';
        setEmptyState(elements.historyTableBody, 5, 'Nenhuma execucao registrada.');
        return;
    }

    const latest = history[0];
    elements.lastStatus.textContent = statusLabel(latest.status);
    elements.lastStatusDetail.textContent = `${latest.triggeredBy || 'manual'} - ${formatDateTime(latest.createdAt)}`;
    elements.historyTableBody.innerHTML = '';
    history.forEach((run) => {
        const row = document.createElement('tr');
        const statusClass = String(run.status || '').toLowerCase();
        row.innerHTML = `
            <td>${formatDate(run.weekStartDate)} - ${formatDate(run.weekEndDate)}</td>
            <td>${run.triggeredBy || '--'}</td>
            <td><span class="status status-${statusClass}">${statusLabel(run.status)}</span></td>
            <td>${run.totalMinutes == null ? '--' : formatMinutes(run.totalMinutes)}</td>
            <td>${formatDateTime(run.createdAt)}</td>`;
        elements.historyTableBody.appendChild(row);
    });
}

async function fetchJson(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Falha ao consultar ${url}`);
    return response.json();
}

async function loadDashboard() {
    elements.lastRefresh.textContent = 'Atualizando...';
    elements.weeksState.textContent = 'Carregando...';
    elements.historyState.textContent = 'Carregando...';
    elements.settingsState.textContent = 'Verificando...';
    try {
        const [weeks, history, settings] = await Promise.all([
            fetchJson('/sync/weeks'),
            fetchJson('/sync/history'),
            fetchJson('/sync/settings')
        ]);
        state.weeks = weeks;
        state.history = history;
        renderSettings(settings);
        renderWeeks();
        renderHistory();
        elements.lastRefresh.textContent = `Atualizado as ${new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`;
    } catch (error) {
        elements.weeksState.textContent = 'Indisponivel';
        elements.historyState.textContent = 'Indisponivel';
        elements.settingsState.textContent = 'Indisponivel';
        setEmptyState(elements.weeksTableBody, 4, 'Nao foi possivel carregar os registros.', true);
        setEmptyState(elements.historyTableBody, 5, 'Nao foi possivel carregar o historico.', true);
        elements.syncMessage.textContent = 'Verifique se a aplicacao e o banco estao em execucao.';
        elements.syncMessage.className = 'muted error-text';
        elements.lastRefresh.textContent = 'Falha na atualizacao';
    }
}

async function saveSettings(event) {
    event.preventDefault();
    elements.saveSettingsButton.disabled = true;
    elements.settingsMessage.textContent = 'Salvando integracoes...';
    elements.settingsMessage.className = 'muted settings-message';
    try {
        const response = await fetch('/sync/settings', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                clockifyApiKey: elements.clockifyApiKey.value,
                notionApiKey: elements.notionApiKey.value
            })
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.detail || 'Nao foi possivel salvar as integracoes.');
        }
        renderSettings(await response.json());
        elements.clockifyApiKey.value = '';
        elements.notionApiKey.value = '';
        elements.settingsMessage.textContent = 'Integracoes salvas com sucesso.';
    } catch (error) {
        elements.settingsMessage.textContent = error.message;
        elements.settingsMessage.className = 'muted settings-message error-text';
    } finally {
        elements.saveSettingsButton.disabled = false;
    }
}

async function runSync(url, label) {
    const buttons = [elements.previousWeekButton, elements.currentWeekButton, elements.refreshButton];
    buttons.forEach((button) => { button.disabled = true; });
    elements.syncMessage.textContent = `${label} em andamento...`;
    elements.syncMessage.className = 'muted';
    try {
        const response = await fetch(url, { method: 'POST' });
        if (!response.ok) throw new Error('A sincronizacao falhou.');
        const result = await response.json();
        elements.syncMessage.textContent = `Sincronizacao concluida: ${result.syncedTime || '--'}.`;
        await loadDashboard();
    } catch (error) {
        elements.syncMessage.textContent = error.message;
        elements.syncMessage.className = 'muted error-text';
    } finally {
        buttons.forEach((button) => { button.disabled = false; });
    }
}

elements.refreshButton.addEventListener('click', loadDashboard);
elements.settingsForm.addEventListener('submit', saveSettings);
elements.previousWeekButton.addEventListener('click', () => runSync('/sync/previous-week', 'Sincronizacao da semana anterior'));
elements.currentWeekButton.addEventListener('click', () => runSync('/sync/current-week', 'Sincronizacao da semana atual'));
loadDashboard();
