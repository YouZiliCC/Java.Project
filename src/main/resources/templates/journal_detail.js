// journal_detail.js
// 期刊详情与对比分析逻辑
let allJournals = [];
let mainJournal = null;
let compareJournal = null;
let radarMain = null;
let radarCompare = null;

async function loadJournalDetail() {
    const urlParams = new URLSearchParams(window.location.search);
    const journalId = urlParams.get('id') || 'J001';
    const response = await fetch('journal_data.json');
    allJournals = await response.json();
    mainJournal = allJournals.find(j => j.id === journalId);
    if (!mainJournal) return;
    document.getElementById('journal-image').src = mainJournal.image;
    document.getElementById('journal-name').textContent = mainJournal.name;
    document.getElementById('journal-field').textContent = mainJournal.field;
    document.getElementById('journal-wos').textContent = mainJournal.wos_category;
    document.getElementById('journal-id').textContent = mainJournal.id;
    // 热点图
    renderHotwordCharts(mainJournal);
    // 指标条形图
    renderIndexBar(mainJournal);
    // 期刊对比分析雷达图
    renderRadarCharts();
    // 填充对比下拉框
    const select = document.getElementById('compare-select');
    select.innerHTML = '<option value="">请选择</option>' + allJournals.filter(j => j.id !== mainJournal.id).map(j => `<option value="${j.id}">${j.name}</option>`).join('');
    select.addEventListener('change', function() {
        const compareId = this.value;
        compareJournal = allJournals.find(j => j.id === compareId);
        renderRadarCharts();
        document.getElementById('ai-compare-result').innerHTML = '（请点击下方按钮进行AI对比分析）';
    });
}

function renderHotwordCharts(journal) {
    const hotwordCharts = document.getElementById('hotword-charts');
    hotwordCharts.innerHTML = '';
    const chartColors = [
        'rgba(59,130,246,0.7)',
        'rgba(16,185,129,0.7)',
        'rgba(139,92,246,0.7)',
        'rgba(245,158,42,0.7)',
        'rgba(239,68,68,0.7)'
    ];
    Object.entries(journal.hotwords).forEach(([year, words], idx) => {
        const chartDiv = document.createElement('div');
        chartDiv.className = 'bg-gradient-to-br from-blue-50 to-white rounded-xl shadow flex flex-col items-center p-3';
        chartDiv.style.height = '170px';
        chartDiv.innerHTML = `<div class='font-bold text-blue-600 mb-1 text-sm'>${year}年</div><canvas id='hotword-bar-${year}' class='hotword-chart' style='height:110px;width:100%;'></canvas>`;
        hotwordCharts.appendChild(chartDiv);
        setTimeout(() => {
            new Chart(document.getElementById(`hotword-bar-${year}`), {
                type: 'bar',
                data: {
                    labels: words.map(w => w.word),
                    datasets: [{
                        label: '出现频次',
                        data: words.map(w => w.count),
                        backgroundColor: chartColors,
                        borderRadius: 8,
                        maxBarThickness: 18
                    }]
                },
                options: {
                    indexAxis: 'y',
                    plugins: {
                        legend: { display: false },
                        title: { display: false },
                        tooltip: {
                            backgroundColor: '#3B82F6',
                            titleColor: '#fff',
                            bodyColor: '#fff',
                            borderColor: '#fff',
                            borderWidth: 1
                        }
                    },
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            beginAtZero: true,
                            grid: { color: 'rgba(59,130,246,0.08)' },
                            ticks: { color: '#64748b', font: { size: 12 } }
                        },
                        y: {
                            grid: { color: 'rgba(59,130,246,0.08)' },
                            ticks: { color: '#64748b', font: { size: 12 } }
                        }
                    }
                }
            });
        }, 0);
    });
}

function renderIndexBar(journal) {
    new Chart(document.getElementById('index-bar-chart'), {
        type: 'bar',
        data: {
            labels: ['新颖性', '颠覆性', '跨学科性', '主题复杂度'],
            datasets: [{
                label: '分值',
                data: [journal.novelty, journal.disruptiveness, journal.interdisciplinarity, journal.topic_complexity],
                backgroundColor: [
                    'rgba(59,130,246,0.7)',
                    'rgba(139,92,246,0.7)',
                    'rgba(16,185,129,0.7)',
                    'rgba(245,158,42,0.7)'
                ],
                borderRadius: 10,
                maxBarThickness: 30
            }]
        },
        options: {
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#8B5CF6',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    borderColor: '#fff',
                    borderWidth: 1
                }
            },
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    grid: { color: 'rgba(139,92,246,0.08)' },
                    ticks: { color: '#64748b', font: { size: 13 } }
                },
                x: {
                    grid: { color: 'rgba(139,92,246,0.08)' },
                    ticks: { color: '#64748b', font: { size: 13 } }
                }
            }
        }
    });
}

function getRadarData(journal) {
    if (!journal) return [0,0,0,0];
    const allWords = Object.values(journal.hotwords).flat().map(w => w.word);
    const uniqueWords = Array.from(new Set(allWords));
    return [
        (journal.novelty + journal.disruptiveness) / 2,
        journal.interdisciplinarity,
        100 - journal.topic_complexity,
        uniqueWords.length * 4
    ];
}

function renderRadarCharts() {
    const ctxMain = document.getElementById('radar-chart-main').getContext('2d');
    const ctxCompare = document.getElementById('radar-chart-compare').getContext('2d');
    if (radarMain) radarMain.destroy();
    if (radarCompare) radarCompare.destroy();
    radarMain = new Chart(ctxMain, {
        type: 'radar',
        data: {
            labels: ['内容前沿性', '学科开放性', '主题集中度', '热点响应度'],
            datasets: [{
                label: mainJournal ? mainJournal.name : '主期刊',
                data: getRadarData(mainJournal),
                backgroundColor: 'rgba(59,130,246,0.15)',
                borderColor: '#3B82F6',
                pointBackgroundColor: '#8B5CF6',
                pointBorderColor: '#fff',
                pointRadius: 5,
                borderWidth: 2
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    min: 0,
                    max: 100,
                    angleLines: { color: 'rgba(59,130,246,0.08)' },
                    grid: { color: 'rgba(59,130,246,0.08)' },
                    pointLabels: { color: '#64748b', font: { size: 14 } },
                    ticks: { color: '#64748b', font: { size: 12 } }
                }
            }
        }
    });
    radarCompare = new Chart(ctxCompare, {
        type: 'radar',
        data: {
            labels: ['内容前沿性', '学科开放性', '主题集中度', '热点响应度'],
            datasets: [{
                label: compareJournal ? compareJournal.name : '对比期刊',
                data: getRadarData(compareJournal),
                backgroundColor: 'rgba(245,158,42,0.15)',
                borderColor: '#F59E2A',
                pointBackgroundColor: '#F59E2A',
                pointBorderColor: '#fff',
                pointRadius: 5,
                borderWidth: 2
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    min: 0,
                    max: 100,
                    angleLines: { color: 'rgba(245,158,42,0.08)' },
                    grid: { color: 'rgba(245,158,42,0.08)' },
                    pointLabels: { color: '#64748b', font: { size: 14 } },
                    ticks: { color: '#64748b', font: { size: 12 } }
                }
            }
        }
    });
}

// AI对比分析按钮点击
function doAICompare() {
    const aiDiv = document.getElementById('ai-compare-result');
    if (!mainJournal || !compareJournal) {
        aiDiv.innerHTML = '（请选择对比期刊，AI分析结果将在此展示）';
        return;
    }
    aiDiv.innerHTML = '（正在生成AI对比分析...）';
    // TODO: 调用后端AI对比接口，传递两个期刊id，渲染结果
    // fetch('/api/journal/compare/ai', { method: 'POST', body: JSON.stringify({id1: mainJournal.id, id2: compareJournal.id}), headers: {'Content-Type': 'application/json'}})
    //   .then(res=>res.json()).then(data=>{ aiDiv.innerHTML = data.html })
    setTimeout(() => {
        aiDiv.innerHTML = `<b>${mainJournal.name}</b> 与 <b>${compareJournal.name}</b> 在研究热点方面：<br>
        <span style='color:#3B82F6'>${mainJournal.name}</span> 近年关键词集中于“${Object.values(mainJournal.hotwords)[0]?.[0]?.word||'人工智能'}”等领域，
        <span style='color:#F59E2A'>${compareJournal.name}</span> 则以“${Object.values(compareJournal.hotwords)[0]?.[0]?.word||'大数据'}”为主。<br>
        在评价体系方面，${mainJournal.name} 在内容前沿性（${getRadarData(mainJournal)[0]}）、学科开放性（${getRadarData(mainJournal)[1]}）等维度表现突出，
        ${compareJournal.name} 则在主题集中度（${getRadarData(compareJournal)[2]}）和热点响应度（${getRadarData(compareJournal)[3]}）方面更具优势。<br>
        综合来看，两者在学科交叉与创新性上各有侧重，适合不同研究需求。`;
    }, 1200);
}

document.addEventListener('DOMContentLoaded', loadJournalDetail);
