// Base URL configuration
const API_BASE = '';

// Helper for showing toast notifications
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    // Trigger reflow
    toast.offsetHeight;
    
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

// Search Functionality
async function handleSearch(event) {
    event.preventDefault();
    
    const searchInput = document.getElementById('searchInput');
    const keyword = searchInput.value.trim();
    
    if (!keyword) {
        showToast('请输入搜索关键词', 'error');
        return;
    }
    
    const loader = document.getElementById('loader');
    const resultsContainer = document.getElementById('resultsContainer');
    const resultsList = document.getElementById('resultsList');
    const statsText = document.getElementById('statsText');
    const analysisPanel = document.getElementById('analysisPanel');
    
    // Show loader, hide results
    loader.style.display = 'block';
    resultsContainer.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE}/search/result?keyword=${encodeURIComponent(keyword)}`);
        const data = await response.json();
        
        if (data.error) {
            showToast(data.error, 'error');
            return;
        }
        
        // Update stats
        statsText.textContent = `找到 ${data.totalResults} 条结果 (用时 ${data.searchTime} 秒)`;
        
        // Render Results
        resultsList.innerHTML = '';
        if (data.results && data.results.length > 0) {
            data.results.forEach(paper => {
                const card = document.createElement('div');
                card.className = 'paper-card';
                
                // Format keywords as tags
                const keywordsHtml = paper.keywords ? 
                    paper.keywords.split(';').map(k => `<span class="tag">${k.trim()}</span>`).join('') : '';
                
                card.innerHTML = `
                    <a href="#" class="paper-title">${paper.title || '无标题'}</a>
                    <div class="paper-meta">
                        <span><i class="fas fa-user"></i> ${paper.author || '未知作者'}</span>
                        <span><i class="fas fa-book"></i> ${paper.journal || '未知期刊'}</span>
                        <span><i class="fas fa-calendar"></i> ${paper.publishDate || '未知日期'}</span>
                        <span><i class="fas fa-quote-right"></i> 被引: ${paper.citations}</span>
                    </div>
                    <div class="paper-abstract">${paper.abstractText || '暂无摘要'}</div>
                    <div class="paper-tags">${keywordsHtml}</div>
                `;
                resultsList.appendChild(card);
            });
        } else {
            resultsList.innerHTML = '<div style="text-align:center; padding: 2rem; color: #7f8c8d;">未找到相关论文</div>';
        }
        
        // Render Analysis (if available)
        if (data.analysis) {
            analysisPanel.style.display = 'block';
            const analysisContent = document.getElementById('analysisContent');
            // Simple JSON dump for now, can be enhanced with charts if structure is known
            analysisContent.innerHTML = `<pre style="background:#eee; padding:1rem; border-radius:4px; overflow:auto;">${JSON.stringify(data.analysis, null, 2)}</pre>`;
        } else {
            analysisPanel.style.display = 'none';
        }
        
        resultsContainer.style.display = 'block';
        
    } catch (error) {
        console.error('Search error:', error);
        showToast('搜索请求失败，请稍后重试', 'error');
    } finally {
        loader.style.display = 'none';
    }
}

// Attach event listener if on search page
document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', handleSearch);
        
        // Check for URL params to auto-search
        const urlParams = new URLSearchParams(window.location.search);
        const keyword = urlParams.get('keyword');
        if (keyword) {
            document.getElementById('searchInput').value = keyword;
            searchForm.dispatchEvent(new Event('submit'));
        }
    }
});
