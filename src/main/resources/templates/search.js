// search.html 分离出的全部 JS 脚本
// 包含示例数据、渲染、过滤、分页等逻辑

const sampleData = [
    { id: 'J001', name: '中国科学: 信息科学', field: '信息科学', country: '中国', language: '中文', openAccess: '是', impact: 'Q1' },
    { id: 'J002', name: '国际学术期刊', field: '物理学', country: '美国', language: '英文', openAccess: '否', impact: 'Q2' },
    { id: 'J003', name: '人工智能前沿', field: '人工智能', country: '英国', language: '英文', openAccess: '是', impact: 'Q1' },
    { id: 'J004', name: '计算机科学综述', field: '计算机科学', country: '德国', language: '德文', openAccess: '否', impact: 'Q3' },
    { id: 'J005', name: '生物医学进展', field: '生物医学', country: '法国', language: '法文', openAccess: '是', impact: 'Q2' },
    { id: 'J006', name: '经济管理评论', field: '经济管理', country: '中国', language: '中文', openAccess: '否', impact: 'Q4' },
    { id: 'J007', name: '日本科技杂志', field: '信息科学', country: '日本', language: '日文', openAccess: '是', impact: 'Q3' },
    { id: 'J008', name: 'AI Research', field: '人工智能', country: '美国', language: '英文', openAccess: '是', impact: 'Q1' },
    { id: 'J009', name: 'Management Science', field: '经济管理', country: '英国', language: '英文', openAccess: '否', impact: 'Q2' },
    { id: 'J010', name: 'Physics Letters', field: '物理学', country: '德国', language: '英文', openAccess: '是', impact: 'Q1' },
    { id: 'J011', name: 'BioMed Central', field: '生物医学', country: '美国', language: '英文', openAccess: '是', impact: 'Q1' },
    { id: 'J012', name: '中国管理科学', field: '经济管理', country: '中国', language: '中文', openAccess: '否', impact: 'Q2' }
];

const resultsContainer = document.getElementById('search-results');
const paginationContainer = document.getElementById('pagination');
const pageSize = 5;
let currentPage = 1;
let filteredData = sampleData;

function renderResults(data, page = 1) {
    resultsContainer.innerHTML = '';
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const pageData = data.slice(start, end);
    pageData.forEach(item => {
        const resultItem = document.createElement('div');
        resultItem.className = 'result-item';
        resultItem.innerHTML = `
            <h3>${item.name}</h3>
            <p>唯一标识: ${item.id}</p>
            <p>研究领域: ${item.field}</p>
            <p>国家: ${item.country}</p>
            <p>语言: ${item.language}</p>
            <p>开放获取: ${item.openAccess}</p>
            <p>影响因子分区: ${item.impact}</p>
        `;
        resultItem.style.cursor = 'pointer';
        resultItem.addEventListener('click', () => {
            window.location.href = `journal_detail.html?id=${item.id}`;
        });
        resultsContainer.appendChild(resultItem);
    });
    renderPagination(data.length, page);
}

function renderPagination(total, page) {
    paginationContainer.innerHTML = '';
    const pageCount = Math.ceil(total / pageSize);
    for (let i = 1; i <= pageCount; i++) {
        const btn = document.createElement('button');
        btn.textContent = i;
        if (i === page) btn.classList.add('active');
        btn.addEventListener('click', () => {
            currentPage = i;
            renderResults(filteredData, currentPage);
        });
        paginationContainer.appendChild(btn);
    }
}

function filterData() {
    const keyword = document.getElementById('keyword').value.trim().toLowerCase();
    const country = document.getElementById('country').value;
    const field = document.getElementById('field').value;
    const language = document.getElementById('language').value;
    const openAccess = document.getElementById('open-access').value;
    const impact = document.getElementById('impact').value;
    filteredData = sampleData.filter(item => {
        const matchesKeyword = !keyword || item.name.toLowerCase().includes(keyword);
        const matchesCountry = !country || item.country === country;
        const matchesField = !field || item.field === field;
        const matchesLanguage = !language || item.language === language;
        const matchesOpen = !openAccess || item.openAccess === openAccess;
        const matchesImpact = !impact || item.impact === impact;
        return matchesKeyword && matchesCountry && matchesField && matchesLanguage && matchesOpen && matchesImpact;
    });
    currentPage = 1;
    renderResults(filteredData, currentPage);
}

document.getElementById('search-button').addEventListener('click', filterData);
document.getElementById('keyword').addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        e.preventDefault();
        filterData();
    }
});
document.getElementById('country').addEventListener('change', filterData);
document.getElementById('field').addEventListener('change', filterData);
document.getElementById('language').addEventListener('change', filterData);
document.getElementById('open-access').addEventListener('change', filterData);
document.getElementById('impact').addEventListener('change', filterData);

document.addEventListener('DOMContentLoaded', () => {
    renderResults(sampleData, 1);
});
