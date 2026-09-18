const state = { weeks: [], history: [], analytics: null };

const elements = {
    analyticsActiveDays: document.querySelector('#analytics-active-days'),
    analyticsAverage: document.querySelector('#analytics-average'),
    analyticsFrom: document.querySelector('#analytics-from'),
    analyticsGranularity: document.querySelector('#analytics-granularity'),
    analyticsLeastDay: document.querySelector('#analytics-least-day'),
    analyticsLeastDayDetail: document.querySelector('#analytics-least-day-detail'),
    analyticsMessage: document.querySelector('#analytics-message'),
    analyticsMostDay: document.querySelector('#analytics-most-day'),
    analyticsMostDayDetail: document.querySelector('#analytics-most-day-detail'),
    analyticsPeakHour: document.querySelector('#analytics-peak-hour'),
    analyticsPeakHourDetail: document.querySelector('#analytics-peak-hour-detail'),
    analyticsRange: document.querySelector('#analytics-range'),
    analyticsState: document.querySelector('#analytics-state'),
    analyticsTo: document.querySelector('#analytics-to'),
    analyticsTopSubject: document.querySelector('#analytics-top-subject'),
    analyticsTopSubjectDetail: document.querySelector('#analytics-top-subject-detail'),
    analyticsTotal: document.querySelector('#analytics-total'),
    applyAnalyticsButton: document.querySelector('#apply-analytics-button'),
    averageHours: document.querySelector('#average-hours'),
    clockifyApiKey: document.querySelector('#clockify-api-key'),
    clockifyTestMessage: document.querySelector('#clockify-test-message'),
    testClockifyButton: document.querySelector('#test-clockify-button'),
    currentWeekButton: document.querySelector('#current-week-button'),
    historyState: document.querySelector('#history-state'),
    historyTableBody: document.querySelector('#history-table-body'),
    hourChart: document.querySelector('#hour-chart'),
    importAnalyticsButton: document.querySelector('#import-analytics-button'),
    lastRefresh: document.querySelector('#last-refresh'),
    lastStatus: document.querySelector('#last-status'),
    lastStatusDetail: document.querySelector('#last-status-detail'),
    notionApiKey: document.querySelector('#notion-api-key'),
    pageSubtitle: document.querySelector('#page-subtitle'),
    pageTitle: document.querySelector('#page-title'),
    periodHours: document.querySelector('#period-hours'),
    periodRange: document.querySelector('#period-range'),
    previousWeekButton: document.querySelector('#previous-week-button'),
    refreshButton: document.querySelector('#refresh-button'),
    saveSettingsButton: document.querySelector('#save-settings-button'),
    settingsForm: document.querySelector('#settings-form'),
    settingsMessage: document.querySelector('#settings-message'),
    settingsState: document.querySelector('#settings-state'),
    setupGuide: document.querySelector('#setup-guide'),
    sidebarConnection: document.querySelector('#sidebar-connection'),
    subjectChart: document.querySelector('#subject-chart'),
    syncMessage: document.querySelector('#sync-message'),
    trendChart: document.querySelector('#trend-chart'),
    trendChartTitle: document.querySelector('#trend-chart-title'),
    trendChartTotal: document.querySelector('#trend-chart-total'),
    weekdayChart: document.querySelector('#weekday-chart'),
    weeksCount: document.querySelector('#weeks-count'),
    weeksState: document.querySelector('#weeks-state'),
    weeksTableBody: document.querySelector('#weeks-table-body')
};

const viewMeta = {
    overview: ['Visao geral', 'Acompanhe seu ritmo e sua constancia.'],
    analytics: ['Analises', 'Entenda os dias e horarios em que voce rende melhor.'],
    syncs: ['Sincronizacoes', 'Atualize os registros e acompanhe suas semanas.'],
    history: ['Historico', 'Consulte todas as execucoes e eventuais falhas.'],
    settings: ['Configuracoes', 'Gerencie as integracoes usadas pelo Study Sync.']
};

function dateToInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function initializeAnalyticsFilters() {
    const today = new Date();
    const start = new Date(today);
    start.setDate(today.getDate() - 29);
    elements.analyticsFrom.value = dateToInput(start);
    elements.analyticsTo.value = dateToInput(today);
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
    return `${Math.floor(totalMinutes / 60)}h ${String(totalMinutes % 60).padStart(2, '0')}min`;
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

function renderSettings(settings) {
    const clockify = settings.clockifyConfigured ? 'Clockify configurado' : 'Clockify pendente';
    const notion = settings.notionConfigured ? 'Notion configurado' : 'Notion opcional';
    elements.settingsState.textContent = `${clockify} - ${notion}`;
    elements.sidebarConnection.textContent = settings.clockifyConfigured ? 'Clockify conectado' : 'Configuracao pendente';
    elements.setupGuide.hidden = settings.clockifyConfigured;
    elements.testClockifyButton.disabled = !settings.clockifyConfigured;
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
    elements.periodHours.textContent = formatHours(totalMinutes);
    elements.averageHours.textContent = formatHours(totalMinutes / weeks.length);
    elements.periodRange.textContent = `${formatDate(weeks[weeks.length - 1].weekStartDate)} a ${formatDate(weeks[0].weekEndDate)}`;
    elements.weeksTableBody.innerHTML = '';
    weeks.forEach((week) => {
        const row = document.createElement('tr');
        const values = [`${formatDate(week.weekStartDate)} - ${formatDate(week.weekEndDate)}`, formatMinutes(week.totalMinutes), week.notionTime || '--', formatDateTime(week.syncedAt)];
        values.forEach((value, index) => {
            const cell = document.createElement('td');
            if (index === 1) {
                const strong = document.createElement('strong');
                strong.textContent = value;
                cell.appendChild(strong);
            } else {
                cell.textContent = value;
            }
            row.appendChild(cell);
        });
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
        const week = document.createElement('td');
        week.textContent = `${formatDate(run.weekStartDate)} - ${formatDate(run.weekEndDate)}`;
        const trigger = document.createElement('td');
        trigger.textContent = run.triggeredBy || '--';
        const statusCell = document.createElement('td');
        const status = document.createElement('span');
        status.className = `status status-${String(run.status || '').toLowerCase()}`;
        status.textContent = statusLabel(run.status);
        statusCell.appendChild(status);
        const total = document.createElement('td');
        total.textContent = run.totalMinutes == null ? '--' : formatMinutes(run.totalMinutes);
        const created = document.createElement('td');
        created.textContent = formatDateTime(run.createdAt);
        row.append(week, trigger, statusCell, total, created);
        elements.historyTableBody.appendChild(row);
    });
}

function formatChartLabel(label, granularity) {
    if (granularity === 'monthly') {
        const [year, month] = label.split('-');
        return `${month}/${year.slice(2)}`;
    }
    return label.length === 10 ? formatDate(label) : label;
}

function switchView(view) {
    const selectedView = viewMeta[view] ? view : 'overview';
    document.querySelectorAll('[data-panel]').forEach((panel) => {
        panel.hidden = panel.dataset.panel !== selectedView;
    });
    document.querySelectorAll('[data-view]').forEach((button) => {
        button.classList.toggle('active', button.dataset.view === selectedView);
    });
    elements.pageTitle.textContent = viewMeta[selectedView][0];
    elements.pageSubtitle.textContent = viewMeta[selectedView][1];

    const showAnalyticsFilters = selectedView === 'overview' || selectedView === 'analytics';
    document.querySelector('#analytics-toolbar').classList.toggle('is-hidden', !showAnalyticsFilters);
    elements.analyticsMessage.classList.toggle('is-hidden', !showAnalyticsFilters);
    window.history.replaceState(null, '', `#${selectedView}`);
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function renderBarChart(container, points, options = {}) {
    container.innerHTML = '';
    const orderedPoints = options.horizontal
        ? [...points].sort((first, second) => second.totalMinutes - first.totalMinutes)
        : points;
    const visiblePoints = options.limit ? orderedPoints.slice(0, options.limit) : orderedPoints;
    const max = Math.max(...visiblePoints.map((point) => point.totalMinutes), 0);
    if (!visiblePoints.length || max === 0) {
        const empty = document.createElement('p');
        empty.className = 'chart-empty';
        empty.textContent = 'Sem registros para este periodo.';
        container.appendChild(empty);
        return;
    }

    visiblePoints.forEach((point) => {
        const item = document.createElement('div');
        item.className = 'bar-item';
        item.title = `${point.label}: ${formatMinutes(point.totalMinutes)}`;
        const track = document.createElement('div');
        track.className = 'bar-track';
        const fill = document.createElement('div');
        fill.className = 'bar-fill';
        const percentage = point.totalMinutes > 0 ? Math.max((point.totalMinutes / max) * 100, 4) : 0;
        fill.style.setProperty('--bar-size', `${percentage}%`);
        track.appendChild(fill);
        const label = document.createElement('span');
        label.className = 'bar-label';
        label.textContent = options.labelFormatter ? options.labelFormatter(point.label) : point.label;
        const value = document.createElement('span');
        value.className = 'bar-value';
        value.textContent = formatHours(point.totalMinutes);
        if (options.horizontal) item.append(label, track, value);
        else item.append(track, label, value);
        container.appendChild(item);
    });
}

function renderAnalytics(analytics) {
    state.analytics = analytics;
    elements.analyticsTotal.textContent = formatHours(analytics.totalMinutes);
    elements.analyticsAverage.textContent = formatMinutes(analytics.averageDailyMinutes);
    elements.analyticsActiveDays.textContent = analytics.activeDays;
    elements.analyticsRange.textContent = `${formatDate(analytics.from)} a ${formatDate(analytics.to)}`;
    elements.analyticsTopSubject.textContent = analytics.topSubject.label;
    elements.analyticsTopSubjectDetail.textContent = formatMinutes(analytics.topSubject.totalMinutes);
    elements.analyticsMostDay.textContent = analytics.mostStudiedDay.label;
    elements.analyticsMostDayDetail.textContent = formatMinutes(analytics.mostStudiedDay.totalMinutes);
    elements.analyticsLeastDay.textContent = analytics.leastStudiedDay.label;
    elements.analyticsLeastDayDetail.textContent = formatMinutes(analytics.leastStudiedDay.totalMinutes);
    elements.analyticsPeakHour.textContent = analytics.peakStudyHour.label;
    elements.analyticsPeakHourDetail.textContent = formatMinutes(analytics.peakStudyHour.totalMinutes);

    const granularity = elements.analyticsGranularity.value;
    const title = { daily: 'Horas por dia', weekly: 'Horas por semana', monthly: 'Horas por mes' }[granularity];
    elements.trendChartTitle.textContent = title;
    elements.trendChartTotal.textContent = formatHours(analytics.totalMinutes);
    renderBarChart(elements.trendChart, analytics[granularity], { labelFormatter: (label) => formatChartLabel(label, granularity) });
    renderBarChart(elements.weekdayChart, analytics.weekday);
    renderBarChart(elements.hourChart, analytics.hourly, { labelFormatter: (label) => label.slice(0, 3) });
    renderBarChart(elements.subjectChart, analytics.subjects, { horizontal: true, limit: 8 });
    elements.analyticsState.textContent = `${analytics.activeDays} dia${analytics.activeDays === 1 ? '' : 's'} com estudo`;
}

async function fetchJson(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Falha ao consultar ${url}`);
    return response.json();
}

function analyticsUrl() {
    const params = new URLSearchParams({ from: elements.analyticsFrom.value, to: elements.analyticsTo.value });
    return `/sync/analytics?${params}`;
}

async function loadDashboard() {
    elements.lastRefresh.textContent = 'Atualizando...';
    elements.weeksState.textContent = 'Carregando...';
    elements.historyState.textContent = 'Carregando...';
    elements.settingsState.textContent = 'Verificando...';
    elements.analyticsState.textContent = 'Carregando...';
    try {
        const [weeks, history, settings, analytics] = await Promise.all([fetchJson('/sync/weeks'), fetchJson('/sync/history'), fetchJson('/sync/settings'), fetchJson(analyticsUrl())]);
        state.weeks = weeks;
        state.history = history;
        renderSettings(settings);
        renderWeeks();
        renderHistory();
        renderAnalytics(analytics);
        elements.analyticsMessage.classList.remove('error-text');
        elements.lastRefresh.textContent = `Atualizado as ${new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`;
    } catch (error) {
        elements.weeksState.textContent = 'Indisponivel';
        elements.historyState.textContent = 'Indisponivel';
        elements.settingsState.textContent = 'Indisponivel';
        elements.analyticsState.textContent = 'Indisponivel';
        setEmptyState(elements.weeksTableBody, 4, 'Nao foi possivel carregar os registros.', true);
        setEmptyState(elements.historyTableBody, 5, 'Nao foi possivel carregar o historico.', true);
        elements.analyticsMessage.textContent = 'Nao foi possivel carregar os analytics para o periodo selecionado.';
        elements.analyticsMessage.classList.add('error-text');
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
        const response = await fetch('/sync/settings', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ clockifyApiKey: elements.clockifyApiKey.value, notionApiKey: elements.notionApiKey.value }) });
        if (!response.ok) throw new Error('Nao foi possivel salvar as integracoes.');
        renderSettings(await response.json());
        elements.clockifyApiKey.value = '';
        elements.notionApiKey.value = '';
        elements.settingsMessage.textContent = 'Integracoes salvas com sucesso.';
        elements.clockifyTestMessage.textContent = '';
    } catch (error) {
        elements.settingsMessage.textContent = error.message;
        elements.settingsMessage.className = 'muted settings-message error-text';
    } finally {
        elements.saveSettingsButton.disabled = false;
    }
}

async function testClockifyConnection() {
    elements.testClockifyButton.disabled = true;
    elements.clockifyTestMessage.textContent = 'Validando conexao com o Clockify...';
    elements.clockifyTestMessage.className = 'muted settings-message';
    try {
        const response = await fetch('/sync/settings/test-clockify', { method: 'POST' });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Nao foi possivel validar a conexao.');
        elements.clockifyTestMessage.textContent = result.message;
        elements.clockifyTestMessage.className = 'muted settings-message success-text';
    } catch (error) {
        elements.clockifyTestMessage.textContent = error.message;
        elements.clockifyTestMessage.className = 'muted settings-message error-text';
    } finally {
        elements.testClockifyButton.disabled = false;
    }
}

async function importAnalyticsPeriod() {
    elements.importAnalyticsButton.disabled = true;
    elements.analyticsMessage.textContent = 'Importando registros do Clockify...';
    elements.analyticsMessage.classList.remove('error-text');
    try {
        const params = new URLSearchParams({ from: elements.analyticsFrom.value, to: elements.analyticsTo.value });
        const response = await fetch(`/sync/import?${params}`, { method: 'POST' });
        if (!response.ok) throw new Error('Nao foi possivel importar o periodo. Verifique a chave do Clockify.');
        const result = await response.json();
        elements.analyticsMessage.textContent = `${result.importedEntries} registro${result.importedEntries === 1 ? '' : 's'} importado${result.importedEntries === 1 ? '' : 's'}: ${formatMinutes(result.totalMinutes)}.`;
        await loadDashboard();
    } catch (error) {
        elements.analyticsMessage.textContent = error.message;
        elements.analyticsMessage.classList.add('error-text');
    } finally {
        elements.importAnalyticsButton.disabled = false;
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

function applyQuickFilter(days, button) {
    const end = new Date();
    const start = new Date(end);
    start.setDate(end.getDate() - (days - 1));
    elements.analyticsFrom.value = dateToInput(start);
    elements.analyticsTo.value = dateToInput(end);
    document.querySelectorAll('.filter-button').forEach((item) => item.classList.remove('active'));
    button.classList.add('active');
    loadDashboard();
}

initializeAnalyticsFilters();
document.querySelectorAll('[data-view]').forEach((button) => button.addEventListener('click', () => switchView(button.dataset.view)));
elements.refreshButton.addEventListener('click', loadDashboard);
elements.settingsForm.addEventListener('submit', saveSettings);
elements.testClockifyButton.addEventListener('click', testClockifyConnection);
elements.applyAnalyticsButton.addEventListener('click', loadDashboard);
elements.analyticsGranularity.addEventListener('change', () => state.analytics && renderAnalytics(state.analytics));
elements.importAnalyticsButton.addEventListener('click', importAnalyticsPeriod);
document.querySelectorAll('.filter-button').forEach((button) => button.addEventListener('click', () => applyQuickFilter(Number(button.dataset.days), button)));
elements.previousWeekButton.addEventListener('click', () => runSync('/sync/previous-week', 'Sincronizacao da semana anterior'));
elements.currentWeekButton.addEventListener('click', () => runSync('/sync/current-week', 'Sincronizacao da semana atual'));
switchView(window.location.hash.slice(1));
loadDashboard();
