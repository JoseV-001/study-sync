const state = { weeks: [], history: [], analytics: null, setupWasOpened: false };

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
    backupMessage: document.querySelector('#backup-message'),
    clockifyApiKey: document.querySelector('#clockify-api-key'),
    clockifyTestMessage: document.querySelector('#clockify-test-message'),
    testClockifyButton: document.querySelector('#test-clockify-button'),
    currentWeekButton: document.querySelector('#current-week-button'),
    dailyGoalCurrent: document.querySelector('#daily-goal-current'),
    downloadBackupButton: document.querySelector('#download-backup-button'),
    exportEntriesButton: document.querySelector('#export-entries-button'),
    dailyGoalHours: document.querySelector('#daily-goal-hours'),
    dailyGoalProgress: document.querySelector('#daily-goal-progress'),
    dailyGoalStatus: document.querySelector('#daily-goal-status'),
    dailyGoalTarget: document.querySelector('#daily-goal-target'),
    goalsForm: document.querySelector('#goals-form'),
    goalsMessage: document.querySelector('#goals-message'),
    goalsSettingsMessage: document.querySelector('#goals-settings-message'),
    settingsSubjectGoalsList: document.querySelector('#settings-subject-goals-list'),
    saveGoalsButton: document.querySelector('#save-goals-button'),
    historyState: document.querySelector('#history-state'),
    historyTableBody: document.querySelector('#history-table-body'),
    hourChart: document.querySelector('#hour-chart'),
    importAnalyticsButton: document.querySelector('#import-analytics-button'),
    lastRefresh: document.querySelector('#last-refresh'),
    lastStatus: document.querySelector('#last-status'),
    lastStatusDetail: document.querySelector('#last-status-detail'),
    notionApiKey: document.querySelector('#notion-api-key'),
    notionDataSourceId: document.querySelector('#notion-data-source-id'),
    notionDateProperty: document.querySelector('#notion-date-property'),
    notionHoursProperty: document.querySelector('#notion-hours-property'),
    pageSubtitle: document.querySelector('#page-subtitle'),
    pageTitle: document.querySelector('#page-title'),
    periodHours: document.querySelector('#period-hours'),
    periodRange: document.querySelector('#period-range'),
    previousWeekButton: document.querySelector('#previous-week-button'),
    importAllButton: document.querySelector('#import-all-button'),
    importAllLabel: document.querySelector('#import-all-label'),
    refreshButton: document.querySelector('#refresh-button'),
    restoreBackupButton: document.querySelector('#restore-backup-button'),
    restoreBackupInput: document.querySelector('#restore-backup-input'),
    saveSettingsButton: document.querySelector('#save-settings-button'),
    setupClockifyApiKey: document.querySelector('#setup-clockify-api-key'),
    setupConnectButton: document.querySelector('#setup-connect-button'),
    setupConnectStep: document.querySelector('#setup-connect-step'),
    setupFinishButton: document.querySelector('#setup-finish-button'),
    setupForm: document.querySelector('#setup-form'),
    setupMessage: document.querySelector('#setup-message'),
    setupModal: document.querySelector('#setup-modal'),
    setupSuccessStep: document.querySelector('#setup-success-step'),
    settingsForm: document.querySelector('#settings-form'),
    settingsMessage: document.querySelector('#settings-message'),
    settingsState: document.querySelector('#settings-state'),
    setupGuide: document.querySelector('#setup-guide'),
    sidebarConnection: document.querySelector('#sidebar-connection'),
    subjectChart: document.querySelector('#subject-chart'),
    subjectGoalHours: document.querySelector('#subject-goal-hours'),
    subjectGoalMessage: document.querySelector('#subject-goal-message'),
    subjectGoalName: document.querySelector('#subject-goal-name'),
    subjectGoalsForm: document.querySelector('#subject-goals-form'),
    subjectGoalsList: document.querySelector('#subject-goals-list'),
    syncMessage: document.querySelector('#sync-message'),
    trendChart: document.querySelector('#trend-chart'),
    trendChartTitle: document.querySelector('#trend-chart-title'),
    trendChartTotal: document.querySelector('#trend-chart-total'),
    weeklyGoalCurrent: document.querySelector('#weekly-goal-current'),
    weeklyGoalProgress: document.querySelector('#weekly-goal-progress'),
    weeklyGoalStatus: document.querySelector('#weekly-goal-status'),
    weeklyGoalTarget: document.querySelector('#weekly-goal-target'),
    weekdayChart: document.querySelector('#weekday-chart'),
    weeklyGoalHours: document.querySelector('#weekly-goal-hours'),
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

function formatGoalInput(minutes) {
    return minutes ? String(Number((minutes / 60).toFixed(2))) : '';
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

function renderSettings(settings, openSetup = false) {
    const clockify = settings.clockifyConfigured ? 'Clockify configurado' : 'Clockify pendente';
    const notion = settings.notionConfigured ? 'Notion configurado' : 'Notion opcional';
    elements.settingsState.textContent = `${clockify} - ${notion}`;
    elements.sidebarConnection.textContent = settings.clockifyConfigured ? 'Clockify conectado' : 'Configuracao pendente';
    elements.setupGuide.hidden = settings.clockifyConfigured;
    elements.testClockifyButton.disabled = !settings.clockifyConfigured;
    if (!settings.clockifyConfigured && openSetup && !state.setupWasOpened) showSetup();
}

function showSetup() {
    state.setupWasOpened = true;
    elements.setupConnectStep.hidden = false;
    elements.setupSuccessStep.hidden = true;
    elements.setupModal.hidden = false;
    document.body.classList.add('is-onboarding');
    window.setTimeout(() => elements.setupClockifyApiKey.focus(), 0);
}

function closeSetup() {
    elements.setupModal.hidden = true;
    document.body.classList.remove('is-onboarding');
}

function renderGoalsSettings(goals) {
    elements.dailyGoalHours.value = formatGoalInput(goals.dailyMinutes);
    elements.weeklyGoalHours.value = formatGoalInput(goals.weeklyMinutes);
}

function renderGoals(progress) {
    const dailyConfigured = progress.dailyGoalMinutes > 0;
    const weeklyConfigured = progress.weeklyGoalMinutes > 0;
    const dailyPercentage = dailyConfigured ? progress.dailyProgressPercentage : 0;
    const weeklyPercentage = weeklyConfigured ? progress.weeklyProgressPercentage : 0;

    elements.dailyGoalCurrent.textContent = formatMinutes(progress.todayMinutes);
    elements.dailyGoalTarget.textContent = dailyConfigured ? `Meta: ${formatMinutes(progress.dailyGoalMinutes)}` : 'Sem meta definida';
    elements.dailyGoalStatus.textContent = dailyConfigured
        ? (progress.dailyGoalReached ? 'Concluida' : `${dailyPercentage}%`)
        : 'Desativada';
    elements.dailyGoalProgress.style.setProperty('--goal-size', `${Math.min(dailyPercentage, 100)}%`);
    elements.dailyGoalProgress.classList.toggle('goal-reached', progress.dailyGoalReached);

    elements.weeklyGoalCurrent.textContent = formatMinutes(progress.weekMinutes);
    elements.weeklyGoalTarget.textContent = weeklyConfigured ? `Meta: ${formatMinutes(progress.weeklyGoalMinutes)}` : 'Sem meta definida';
    elements.weeklyGoalStatus.textContent = weeklyConfigured
        ? (progress.weeklyGoalReached ? 'Concluida' : `${weeklyPercentage}%`)
        : 'Desativada';
    elements.weeklyGoalProgress.style.setProperty('--goal-size', `${Math.min(weeklyPercentage, 100)}%`);
    elements.weeklyGoalProgress.classList.toggle('goal-reached', progress.weeklyGoalReached);

    elements.goalsMessage.textContent = dailyConfigured || weeklyConfigured
        ? `Semana de ${formatDate(progress.weekStart)} a ${formatDate(progress.weekEnd)}.`
        : 'Defina suas metas em Configuracoes para acompanhar seu progresso.';
}

function renderSubjectGoalRows(container, goals) {
    container.innerHTML = '';
    if (!goals.length) {
        const empty = document.createElement('p');
        empty.className = 'goals-message';
        empty.textContent = 'Nenhuma meta por materia configurada.';
        container.appendChild(empty);
        return;
    }

    goals.forEach((goal) => {
        const row = document.createElement('article');
        row.className = 'subject-goal-row';
        const heading = document.createElement('div');
        heading.className = 'subject-goal-heading';
        const subject = document.createElement('strong');
        subject.textContent = goal.subject;
        const status = document.createElement('span');
        status.className = goal.goalReached ? 'goal-status goal-status-reached' : 'goal-status';
        status.textContent = goal.goalReached ? 'Concluida' : `${goal.progressPercentage}%`;
        heading.append(subject, status);
        const track = document.createElement('div');
        track.className = 'goal-progress-track';
        const fill = document.createElement('div');
        fill.className = `goal-progress-fill${goal.goalReached ? ' goal-reached' : ''}`;
        fill.style.setProperty('--goal-size', `${Math.min(goal.progressPercentage, 100)}%`);
        track.appendChild(fill);
        const footer = document.createElement('div');
        footer.className = 'subject-goal-footer';
        const summary = document.createElement('span');
        summary.textContent = `${formatMinutes(goal.studiedMinutes)} de ${formatMinutes(goal.weeklyGoalMinutes)} nesta semana`;
        const remove = document.createElement('button');
        remove.className = 'icon-button icon-button-small subject-goal-remove';
        remove.type = 'button';
        remove.dataset.goalId = goal.id;
        remove.title = `Remover meta de ${goal.subject}`;
        remove.setAttribute('aria-label', `Remover meta de ${goal.subject}`);
        remove.innerHTML = '<span class="icon icon-trash" aria-hidden="true"></span>';
        footer.append(summary, remove);
        row.append(heading, track, footer);
        container.appendChild(row);
    });
}

function renderSubjectGoals(goals) {
    renderSubjectGoalRows(elements.subjectGoalsList, goals);
    renderSubjectGoalRows(elements.settingsSubjectGoalsList, goals);
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
        const [weeks, history, settings, analytics, goals, goalProgress, subjectGoals] = await Promise.all([
            fetchJson('/sync/weeks'),
            fetchJson('/sync/history'),
            fetchJson('/sync/settings'),
            fetchJson(analyticsUrl()),
            fetchJson('/sync/goals'),
            fetchJson('/sync/goals/progress'),
            fetchJson('/sync/goals/subjects')
        ]);
        state.weeks = weeks;
        state.history = history;
        renderSettings(settings, true);
        renderGoalsSettings(goals);
        renderGoals(goalProgress);
        renderSubjectGoals(subjectGoals);
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

async function saveGoals(event) {
    event.preventDefault();
    elements.saveGoalsButton.disabled = true;
    elements.goalsSettingsMessage.textContent = 'Salvando metas...';
    elements.goalsSettingsMessage.className = 'muted settings-message';
    try {
        const dailyHours = Number(elements.dailyGoalHours.value || 0);
        const weeklyHours = Number(elements.weeklyGoalHours.value || 0);
        const response = await fetch('/sync/goals', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                dailyMinutes: Math.round(dailyHours * 60),
                weeklyMinutes: Math.round(weeklyHours * 60)
            })
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Nao foi possivel salvar as metas.');
        renderGoalsSettings(result);
        renderGoals(await fetchJson('/sync/goals/progress'));
        elements.goalsSettingsMessage.textContent = 'Metas salvas com sucesso.';
        elements.goalsSettingsMessage.className = 'muted settings-message success-text';
    } catch (error) {
        elements.goalsSettingsMessage.textContent = error.message;
        elements.goalsSettingsMessage.className = 'muted settings-message error-text';
    } finally {
        elements.saveGoalsButton.disabled = false;
    }
}

async function saveSubjectGoal(event) {
    event.preventDefault();
    elements.subjectGoalMessage.textContent = 'Salvando meta por materia...';
    elements.subjectGoalMessage.className = 'muted settings-message';
    try {
        const subject = elements.subjectGoalName.value.trim();
        const weeklyHours = Number(elements.subjectGoalHours.value || 0);
        const response = await fetch('/sync/goals/subjects', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ subject, weeklyMinutes: Math.round(weeklyHours * 60) })
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Nao foi possivel salvar a meta por materia.');
        renderSubjectGoals(result);
        elements.subjectGoalName.value = '';
        elements.subjectGoalHours.value = '';
        elements.subjectGoalMessage.textContent = 'Meta por materia salva com sucesso.';
        elements.subjectGoalMessage.className = 'muted settings-message success-text';
    } catch (error) {
        elements.subjectGoalMessage.textContent = error.message;
        elements.subjectGoalMessage.className = 'muted settings-message error-text';
    }
}

async function deleteSubjectGoal(goalId) {
    const response = await fetch(`/sync/goals/subjects/${goalId}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('Nao foi possivel remover a meta por materia.');
    renderSubjectGoals(await fetchJson('/sync/goals/subjects'));
    elements.subjectGoalMessage.textContent = 'Meta por materia removida.';
    elements.subjectGoalMessage.className = 'muted settings-message success-text';
}

function downloadFile(url) {
    const link = document.createElement('a');
    link.href = url;
    link.download = '';
    document.body.appendChild(link);
    link.click();
    link.remove();
}

function showBackupMessage(message, isError = false) {
    elements.backupMessage.textContent = message;
    elements.backupMessage.className = `settings-message${isError ? ' error-text' : ' success-text'}`;
}

async function restoreBackup(event) {
    const [file] = event.target.files;
    if (!file) return;

    try {
        const backup = JSON.parse(await file.text());
        const confirmed = window.confirm('Restaurar este backup substitui todos os estudos, metas e historico locais. Deseja continuar?');
        if (!confirmed) return;

        elements.restoreBackupButton.disabled = true;
        showBackupMessage('Restaurando backup...');
        const response = await fetch('/sync/backup/restore', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(backup)
        });
        if (!response.ok) {
            const result = await response.json().catch(() => ({}));
            throw new Error(result.message || 'Nao foi possivel restaurar o backup.');
        }
        await loadDashboard();
        showBackupMessage('Backup restaurado com sucesso.');
    } catch (error) {
        showBackupMessage(error.message, true);
    } finally {
        elements.restoreBackupButton.disabled = false;
        elements.restoreBackupInput.value = '';
    }
}

async function saveSettings(event) {
    event.preventDefault();
    elements.saveSettingsButton.disabled = true;
    elements.settingsMessage.textContent = 'Salvando integracoes...';
    elements.settingsMessage.className = 'muted settings-message';
    try {
        const clockifyApiKey = elements.clockifyApiKey.value.trim();
        const notionApiKey = elements.notionApiKey.value.trim();
        const notionDataSourceId = elements.notionDataSourceId.value.trim();
        const notionDateProperty = elements.notionDateProperty.value.trim();
        const notionHoursProperty = elements.notionHoursProperty.value.trim();
        if (!clockifyApiKey && !notionApiKey && !notionDataSourceId && !notionDateProperty && !notionHoursProperty) {
            throw new Error('Informe ao menos uma configuracao para atualizar as integracoes.');
        }

        let settings;
        if (clockifyApiKey) settings = await connectClockify(clockifyApiKey);
        if (notionApiKey || notionDataSourceId || notionDateProperty || notionHoursProperty) {
            const response = await fetch('/sync/settings', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ notionApiKey, notionDataSourceId, notionDateProperty, notionHoursProperty })
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.message || 'Nao foi possivel salvar a chave do Notion.');
            settings = result;
        }
        renderSettings(settings);
        elements.clockifyApiKey.value = '';
        elements.notionApiKey.value = '';
        elements.notionDataSourceId.value = '';
        elements.notionDateProperty.value = '';
        elements.notionHoursProperty.value = '';
        elements.settingsMessage.textContent = 'Integracoes atualizadas com sucesso.';
        elements.clockifyTestMessage.textContent = '';
    } catch (error) {
        elements.settingsMessage.textContent = error.message;
        elements.settingsMessage.className = 'muted settings-message error-text';
    } finally {
        elements.saveSettingsButton.disabled = false;
    }
}

async function connectClockify(apiKey) {
    const response = await fetch('/sync/settings/connect-clockify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ apiKey })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || 'Nao foi possivel conectar ao Clockify.');
    return result;
}

async function completeSetup(event) {
    event.preventDefault();
    elements.setupConnectButton.disabled = true;
    elements.setupMessage.textContent = 'Validando a chave e localizando seu workspace...';
    elements.setupMessage.className = 'settings-message';
    try {
        const settings = await connectClockify(elements.setupClockifyApiKey.value.trim());
        renderSettings(settings);
        elements.setupConnectStep.hidden = true;
        elements.setupSuccessStep.hidden = false;
    } catch (error) {
        elements.setupMessage.textContent = error.message;
        elements.setupMessage.className = 'settings-message error-text';
    } finally {
        elements.setupConnectButton.disabled = false;
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

function setSyncBusy(busy) {
    [elements.importAllButton, elements.importAnalyticsButton, elements.previousWeekButton,
        elements.currentWeekButton, elements.refreshButton].forEach((button) => { button.disabled = busy; });
}

async function importAllHistory() {
    setSyncBusy(true);
    elements.importAllButton.setAttribute('aria-busy', 'true');
    elements.importAllLabel.textContent = 'Importando...';
    elements.syncMessage.textContent = 'Importando todo o historico do Clockify. Aguarde...';
    elements.syncMessage.className = 'muted';
    try {
        const response = await fetch('/sync/import/all', { method: 'POST' });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Nao foi possivel importar o historico.');
        const processed = result.createdEntries + result.updatedEntries;
        if (processed > 0) {
            elements.analyticsFrom.value = result.from;
            elements.analyticsTo.value = result.to;
            document.querySelectorAll('.filter-button').forEach((button) => button.classList.remove('active'));
            elements.syncMessage.textContent = `Historico importado: ${processed} registros, ${result.createdEntries} novos e ${result.updatedEntries} atualizados. Total: ${formatMinutes(result.totalMinutes)} desde ${formatDate(result.from)}.`;
        } else {
            elements.syncMessage.textContent = 'Nenhum registro finalizado encontrado no Clockify.';
        }
        await loadDashboard();
    } catch (error) {
        elements.syncMessage.textContent = error.message;
        elements.syncMessage.className = 'muted error-text';
    } finally {
        elements.importAllLabel.textContent = 'Importar tudo';
        elements.importAllButton.setAttribute('aria-busy', 'false');
        setSyncBusy(false);
    }
}

async function importAnalyticsPeriod() {
    setSyncBusy(true);
    const startedAt = performance.now();
    elements.analyticsMessage.textContent = 'Consultando registros do Clockify...';
    elements.analyticsMessage.classList.remove('error-text');
    try {
        const params = new URLSearchParams({ from: elements.analyticsFrom.value, to: elements.analyticsTo.value });
        const response = await fetch(`/sync/import?${params}`, { method: 'POST' });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Nao foi possivel importar o periodo.');
        const elapsedSeconds = Math.max(1, Math.round((performance.now() - startedAt) / 1000));
        const processed = result.createdEntries + result.updatedEntries;
        elements.analyticsMessage.textContent = `${processed} registro${processed === 1 ? '' : 's'} processado${processed === 1 ? '' : 's'} em ${elapsedSeconds}s: ${result.createdEntries} novo${result.createdEntries === 1 ? '' : 's'}, ${result.updatedEntries} atualizado${result.updatedEntries === 1 ? '' : 's'} e ${result.skippedEntries} ignorado${result.skippedEntries === 1 ? '' : 's'}. Total: ${formatMinutes(result.totalMinutes)}.`;
        await loadDashboard();
    } catch (error) {
        elements.analyticsMessage.textContent = error.message;
        elements.analyticsMessage.classList.add('error-text');
    } finally {
        setSyncBusy(false);
    }
}

async function runSync(url, label) {
    setSyncBusy(true);
    elements.syncMessage.textContent = `${label} em andamento...`;
    elements.syncMessage.className = 'muted';
    try {
        const response = await fetch(url, { method: 'POST' });
        const result = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(result.message || 'A sincronizacao falhou.');
        elements.syncMessage.textContent = result.notionUpdated
            ? `Notion pessoal atualizado com sucesso: ${result.syncedTime || '--'}.`
            : `Semana salva apenas na dashboard: ${result.syncedTime || '--'}. O Notion pessoal nao esta configurado.`;
        await loadDashboard();
    } catch (error) {
        elements.syncMessage.textContent = error.message;
        elements.syncMessage.className = 'muted error-text';
    } finally {
        setSyncBusy(false);
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
elements.setupForm.addEventListener('submit', completeSetup);
elements.setupFinishButton.addEventListener('click', () => {
    closeSetup();
    switchView('overview');
    loadDashboard();
});
document.querySelectorAll('[data-open-setup]').forEach((button) => button.addEventListener('click', showSetup));
elements.goalsForm.addEventListener('submit', saveGoals);
elements.subjectGoalsForm.addEventListener('submit', saveSubjectGoal);
elements.subjectGoalsList.addEventListener('click', (event) => {
    const button = event.target.closest('[data-goal-id]');
    if (button) deleteSubjectGoal(button.dataset.goalId).catch((error) => {
        elements.goalsMessage.textContent = error.message;
        elements.goalsMessage.className = 'goals-message error-text';
    });
});
elements.settingsSubjectGoalsList.addEventListener('click', (event) => {
    const button = event.target.closest('[data-goal-id]');
    if (button) deleteSubjectGoal(button.dataset.goalId).catch((error) => {
        elements.subjectGoalMessage.textContent = error.message;
        elements.subjectGoalMessage.className = 'muted settings-message error-text';
    });
});
elements.testClockifyButton.addEventListener('click', testClockifyConnection);
elements.downloadBackupButton.addEventListener('click', () => downloadFile('/sync/backup'));
elements.exportEntriesButton.addEventListener('click', () => downloadFile('/sync/export/study-entries.csv'));
elements.restoreBackupButton.addEventListener('click', () => elements.restoreBackupInput.click());
elements.restoreBackupInput.addEventListener('change', restoreBackup);
elements.applyAnalyticsButton.addEventListener('click', loadDashboard);
elements.analyticsGranularity.addEventListener('change', () => state.analytics && renderAnalytics(state.analytics));
elements.importAnalyticsButton.addEventListener('click', importAnalyticsPeriod);
elements.importAllButton.addEventListener('click', importAllHistory);
document.querySelectorAll('.filter-button').forEach((button) => button.addEventListener('click', () => applyQuickFilter(Number(button.dataset.days), button)));
elements.previousWeekButton.addEventListener('click', () => runSync('/sync/previous-week', 'Sincronizacao da semana anterior'));
elements.currentWeekButton.addEventListener('click', () => runSync('/sync/current-week', 'Sincronizacao da semana atual'));
switchView(window.location.hash.slice(1));
loadDashboard();
