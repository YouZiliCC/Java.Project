// Analysis Page Logic

let uploadedFilename = null;
let analysisContext = null;

// 获取当前用户名
function getUsername() {
    const user = getCurrentUser();
    return user ? user.uname : null;
}

// 文件上传处理
function initUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    
    uploadArea.addEventListener('click', () => fileInput.click());
    
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileUpload(e.target.files[0]);
        }
    });
    
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });
    
    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });
    
    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        if (e.dataTransfer.files.length > 0) {
            handleFileUpload(e.dataTransfer.files[0]);
        }
    });
}

// 处理文件上传
async function handleFileUpload(file) {
    const allowedExtensions = ['.json', '.csv'];
    const extension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!allowedExtensions.includes(extension)) {
        showToast('请上传 JSON 或 CSV 格式的文件', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    const username = getUsername();
    if (username) {
        formData.append('username', username);
    }
    
    try {
        showToast('正在上传...', 'info');
        
        const response = await fetch('/analysis/upload', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('文件上传成功', 'success');
            uploadedFilename = data.filename;
            displayUploadedFile(data.originalName, data.filename, data.size);
        } else {
            showToast(data.message || '上传失败', 'error');
        }
    } catch (error) {
        console.error('上传失败:', error);
        showToast('上传请求失败', 'error');
    }
}

// 显示已上传文件
function displayUploadedFile(originalName, filename, size) {
    const container = document.getElementById('uploadedFiles');
    const sizeStr = size > 1024 * 1024 
        ? (size / (1024 * 1024)).toFixed(2) + ' MB'
        : (size / 1024).toFixed(2) + ' KB';
    
    container.innerHTML = `
        <div class="uploaded-file">
            <div class="file-info">
                <i class="fas fa-file-alt"></i>
                <span class="file-name">${originalName}</span>
                <span class="file-size">(${sizeStr})</span>
            </div>
            <button class="btn-delete" onclick="deleteFile('${filename}')">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
}

// 删除文件
async function deleteFile(filename) {
    try {
        const formData = new FormData();
        formData.append('filename', filename);
        
        const response = await fetch('/analysis/delete', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('文件已删除', 'success');
            uploadedFilename = null;
            document.getElementById('uploadedFiles').innerHTML = '';
        } else {
            showToast(data.message || '删除失败', 'error');
        }
    } catch (error) {
        console.error('删除失败:', error);
        showToast('删除请求失败', 'error');
    }
}

// 运行数据分析
async function runAnalysis(useDatabase = false) {
    const resultCard = document.getElementById('resultCard');
    const analysisResult = document.getElementById('analysisResult');
    const loader = document.getElementById('analysisLoader');
    
    resultCard.style.display = 'block';
    loader.style.display = 'block';
    analysisResult.innerHTML = '';
    
    try {
        const formData = new FormData();
        if (!useDatabase && uploadedFilename) {
            formData.append('filename', uploadedFilename);
        }
        
        const username = getUsername();
        if (username) {
            formData.append('username', username);
        }
        
        const response = await fetch('/analysis/run', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            displayAnalysisResult(data);
            analysisContext = JSON.stringify(data.analysis);
            // 刷新历史记录
            loadHistory();
        } else {
            analysisResult.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    ${data.message || '分析失败'}
                </div>
            `;
        }
    } catch (error) {
        console.error('分析失败:', error);
        analysisResult.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                分析请求失败，请稍后重试
            </div>
        `;
    } finally {
        loader.style.display = 'none';
    }
}

// 显示分析结果
function displayAnalysisResult(data) {
    const container = document.getElementById('analysisResult');
    const analysis = data.analysis || {};
    
    // 基础统计卡片
    let html = `
        <div class="result-summary">
            <div class="stat-card">
                <div class="stat-value">${analysis.total_records || analysis.total_papers || data.totalPapers || 0}</div>
                <div class="stat-label">论文总数</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${analysis.journal_count || 0}</div>
                <div class="stat-label">期刊数量</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${analysis.total_files || 0}</div>
                <div class="stat-label">处理文件数</div>
            </div>
        </div>
    `;
    
    // 年份范围
    if (analysis.year_range) {
        html += `
            <div class="result-section">
                <h4><i class="fas fa-calendar"></i> 时间跨度</h4>
                <p>论文发表年份：${analysis.year_range.min} - ${analysis.year_range.max}</p>
            </div>
        `;
    }
    
    // Top期刊分布
    if (analysis.top_journals && Object.keys(analysis.top_journals).length > 0) {
        html += `
            <div class="result-section">
                <h4><i class="fas fa-book"></i> 期刊分布 TOP 10</h4>
                <div class="distribution-list">
                    ${Object.entries(analysis.top_journals).slice(0, 10).map(([key, value]) => `
                        <div class="distribution-item">
                            <span class="dist-label">${key}</span>
                            <span class="dist-value">${value}</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    // 五大指标展示
    html += '<div class="metrics-section"><h3><i class="fas fa-chart-line"></i> 期刊评价指标</h3>';
    
    // 1. 颠覆性指数 (Disruption)
    if (analysis.disruption && analysis.disruption.length > 0) {
        html += renderMetricCard(
            'disruption',
            '颠覆性指数 (Disruption)',
            'fas fa-bolt',
            '衡量期刊发表论文的创新突破程度',
            sortByScore(analysis.disruption, 'percent_score'),
            ['期刊', '百分位分数'],
            (item) => [item.journal || item.Journal || '-', formatScore(item.percent_score || item.score)]
        );
    }
    
    // 2. 跨学科性 (Interdisciplinary)
    if (analysis.interdisciplinary && analysis.interdisciplinary.length > 0) {
        html += renderMetricCard(
            'interdisciplinary',
            '跨学科性 (Interdisciplinary)',
            'fas fa-project-diagram',
            '衡量期刊研究的跨领域融合程度',
            sortByScore(analysis.interdisciplinary, 'percent_score'),
            ['期刊', '百分位分数'],
            (item) => [item.journal || item.Journal || '-', formatScore(item.percent_score || item.score)]
        );
    }
    
    // 3. 新颖性 (Novelty)
    if (analysis.novelty && analysis.novelty.length > 0) {
        html += renderMetricCard(
            'novelty',
            '新颖性 (Novelty)',
            'fas fa-lightbulb',
            '衡量期刊发表内容的创新程度',
            sortByScore(analysis.novelty, 'percent_score'),
            ['期刊', '百分位分数'],
            (item) => [item.journal || item.Journal || '-', formatScore(item.percent_score || item.score)]
        );
    }
    
    // 4. 主题复杂度 (Topic Entropy)
    if (analysis.topic && analysis.topic.length > 0) {
        html += renderMetricCard(
            'topic',
            '主题复杂度 (Topic)',
            'fas fa-puzzle-piece',
            '衡量期刊研究主题的多样性',
            sortByScore(analysis.topic, 'percent_score'),
            ['期刊', '百分位分数'],
            (item) => [item.journal || item.Journal || '-', formatScore(item.percent_score || item.score)]
        );
    }
    
    // 5. 主题热度 (Theme)
    if (analysis.theme && analysis.theme.length > 0) {
        html += renderMetricCard(
            'theme',
            '主题热度 (Theme)',
            'fas fa-fire',
            '衡量期刊对热门主题的响应度',
            sortByScore(analysis.theme, 'theme_concentration'),
            ['期刊', '主题集中度', '热点响应度'],
            (item) => [item.journal || item.Journal || '-', formatScore(item.theme_concentration), formatScore(item.hot_response)]
        );
    }
    
    html += '</div>';  // end metrics-section
    
    // AI 总结区域
    html += `
        <div class="ai-summary-section" id="aiSummarySection">
            <h4><i class="fas fa-robot"></i> AI 智能总结</h4>
            <div id="aiSummaryContent">
                <button class="btn-primary" onclick="generateAISummary()">
                    <i class="fas fa-magic"></i> 生成AI总结
                </button>
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

// 排序函数：按分数从高到低
function sortByScore(arr, scoreField) {
    if (!arr || !Array.isArray(arr)) return [];
    return [...arr].sort((a, b) => {
        const scoreA = parseFloat(a[scoreField] || a.score || a.percent_score || 0);
        const scoreB = parseFloat(b[scoreField] || b.score || b.percent_score || 0);
        return scoreB - scoreA;  // 从高到低
    });
}

// 渲染可展开的指标卡片
function renderMetricCard(id, title, iconClass, desc, items, headers, rowRenderer) {
    const previewCount = 5;
    const hasMore = items.length > previewCount;
    const totalCount = items.length;
    
    return `
        <div class="result-section metric-card" id="metric-${id}">
            <div class="metric-header" onclick="toggleMetricExpand('${id}')">
                <div class="metric-title-area">
                    <h4><i class="${iconClass}"></i> ${title}</h4>
                </div>
            </div>
            <p class="metric-desc">${desc}</p>
            <div class="metric-table">
                <table>
                    <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead>
                    <tbody id="metric-body-${id}">
                        ${items.slice(0, previewCount).map((item, idx) => {
                            const cells = rowRenderer(item);
                            return `<tr class="rank-${idx < 3 ? idx + 1 : 'normal'}">
                                <td><span class="rank-badge">${idx + 1}</span> ${cells[0]}</td>
                                ${cells.slice(1).map(c => `<td>${c}</td>`).join('')}
                            </tr>`;
                        }).join('')}
                    </tbody>
                </table>
                ${hasMore ? `
                    <div class="metric-expand-area" id="expand-area-${id}" style="display: none;">
                        <table>
                            <tbody>
                                ${items.slice(previewCount).map((item, idx) => {
                                    const cells = rowRenderer(item);
                                    const rank = previewCount + idx + 1;
                                    return `<tr>
                                        <td><span class="rank-badge rank-other">${rank}</span> ${cells[0]}</td>
                                        ${cells.slice(1).map(c => `<td>${c}</td>`).join('')}
                                    </tr>`;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>
                    <button class="btn-expand" id="btn-expand-${id}" onclick="toggleMetricExpand('${id}')">
                        <i class="fas fa-chevron-down"></i> 展开全部 (${totalCount - previewCount} 更多)
                    </button>
                ` : ''}
            </div>
        </div>
    `;
}

// 切换展开/折叠
function toggleMetricExpand(id) {
    const expandArea = document.getElementById(`expand-area-${id}`);
    const btn = document.getElementById(`btn-expand-${id}`);
    const icon = document.getElementById(`expand-icon-${id}`);
    
    if (!expandArea) return;
    
    const isExpanded = expandArea.style.display !== 'none';
    
    if (isExpanded) {
        expandArea.style.display = 'none';
        if (btn) btn.innerHTML = `<i class="fas fa-chevron-down"></i> 展开全部`;
        if (icon) icon.innerHTML = `<i class="fas fa-chevron-down"></i>`;
    } else {
        expandArea.style.display = 'block';
        if (btn) btn.innerHTML = `<i class="fas fa-chevron-up"></i> 收起`;
        if (icon) icon.innerHTML = `<i class="fas fa-chevron-up"></i>`;
    }
}

// 格式化分数显示
function formatScore(score) {
    if (score === null || score === undefined || isNaN(score)) return '-';
    return (typeof score === 'number') ? score.toFixed(2) : score;
}

// 生成AI总结
async function generateAISummary() {
    const summaryContent = document.getElementById('aiSummaryContent');
    summaryContent.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> AI正在分析中...</div>';
    
    try {
        const username = getUsername();
        const response = await fetch('/analysis/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                message: '请根据以上分析结果，给出一个专业的学术期刊评价总结报告，包括：1. 数据概况 2. 各指标特点分析 3. 综合评价建议',
                context: analysisContext,
                username: username
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            summaryContent.innerHTML = `
                <div class="ai-summary-text">${formatAIResponse(data.reply)}</div>
                <button class="btn-secondary" onclick="generateAISummary()">
                    <i class="fas fa-redo"></i> 重新生成
                </button>
            `;
        } else {
            summaryContent.innerHTML = `
                <div class="error-message">${data.message || 'AI总结生成失败'}</div>
                <button class="btn-primary" onclick="generateAISummary()">
                    <i class="fas fa-redo"></i> 重试
                </button>
            `;
        }
    } catch (error) {
        console.error('AI总结失败:', error);
        summaryContent.innerHTML = `
            <div class="error-message">请求失败，请稍后重试</div>
            <button class="btn-primary" onclick="generateAISummary()">
                <i class="fas fa-redo"></i> 重试
            </button>
        `;
    }
}

// 格式化AI响应（支持markdown简单格式）
function formatAIResponse(text) {
    if (!text) return '';
    return text
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>');
}

// 加载分析历史
async function loadHistory() {
    const username = getUsername();
    if (!username) return;
    
    const historyContainer = document.getElementById('historyContainer');
    if (!historyContainer) return;
    
    try {
        const response = await fetch(`/analysis/history?username=${encodeURIComponent(username)}`);
        const data = await response.json();
        
        if (data.success && data.history && data.history.length > 0) {
            historyContainer.style.display = 'block';
            const historyList = document.getElementById('historyList');
            
            historyList.innerHTML = data.history.map(item => {
                const paperCount = item.totalPapers || 0;
                const journalCount = item.journalCount || 0;
                const displayName = item.originalName || '用户数据分析';
                
                return `
                <div class="history-item" onclick="loadHistoryDetail('${item.filename}')">
                    <div class="history-name" title="${displayName}">${truncateText(displayName, 25)}</div>
                    <div class="history-meta">
                        <span><i class="fas fa-file-alt"></i> ${paperCount} 篇</span>
                        ${journalCount > 0 ? `<span><i class="fas fa-book"></i> ${journalCount} 刊</span>` : ''}
                        <span><i class="fas fa-clock"></i> ${formatDate(item.createdAt)}</span>
                    </div>
                </div>
            `}).join('');
        } else {
            historyContainer.style.display = 'none';
        }
    } catch (error) {
        console.error('加载历史失败:', error);
    }
}

// 截断文本
function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// 加载历史详情
async function loadHistoryDetail(filename) {
    try {
        const response = await fetch(`/analysis/detail?filename=${encodeURIComponent(filename)}`);
        const data = await response.json();
        
        if (data.success) {
            const resultCard = document.getElementById('resultCard');
            resultCard.style.display = 'block';
            
            displayAnalysisResult({ analysis: data.analysis });
            analysisContext = JSON.stringify(data.analysis);
            
            showToast(`已加载: ${data.originalName}`, 'info');
        } else {
            showToast(data.message || '加载失败', 'error');
        }
    } catch (error) {
        console.error('加载详情失败:', error);
        showToast('加载失败', 'error');
    }
}

// 格式化日期
function formatDate(dateStr) {
    if (!dateStr) return '未知';
    try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch {
        return dateStr;
    }
}

// AI对话
async function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    addChatMessage(message, 'user');
    input.value = '';
    
    try {
        const username = getUsername();
        
        const response = await fetch('/analysis/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                message: message,
                context: analysisContext,
                username: username
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            addChatMessage(data.reply, 'bot');
        } else {
            addChatMessage('抱歉，我遇到了一些问题。请稍后再试。', 'bot');
        }
    } catch (error) {
        console.error('发送消息失败:', error);
        addChatMessage('网络错误，请检查您的连接。', 'bot');
    }
}

// 添加聊天消息
function addChatMessage(content, type) {
    const container = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${type}`;
    
    const icon = type === 'user' ? '<i class="fas fa-user"></i>' : '<span class="ds-logo">DS</span>';
    
    messageDiv.innerHTML = `
        <div class="message-avatar">${icon}</div>
        <div class="message-content">${content}</div>
    `;
    
    container.appendChild(messageDiv);
    container.scrollTop = container.scrollHeight;
}

// 检查 AI 配置状态
async function checkAIStatus() {
    try {
        const response = await fetch('/analysis/ai-status');
        const data = await response.json();
        
        if (data.success && data.data) {
            const aiStatus = data.data;
            
            if (!aiStatus.configured) {
                const warningDiv = document.createElement('div');
                warningDiv.className = 'ai-warning';
                warningDiv.innerHTML = `
                    <i class="fas fa-exclamation-triangle"></i>
                    <span>AI 功能未启用（使用简单问答模式）</span>
                `;
                warningDiv.style.cssText = 'background: #fff3cd; color: #856404; padding: 10px; border-radius: 8px; margin-top: 10px; font-size: 12px;';
                
                const cardHeader = document.querySelector('.chat-card > h3');
                if (cardHeader && cardHeader.nextSibling) {
                    cardHeader.parentNode.insertBefore(warningDiv, cardHeader.nextSibling.nextSibling);
                }
            } else {
                const welcomeMsg = document.querySelector('.chat-message.bot .message-content');
                if (welcomeMsg) {
                    welcomeMsg.innerHTML = `您好！我是期刊分析AI助手。您可以问我任何关于论文分析的问题，我能够阅读您的分析结果并为您解答。`;
                }
            }
        }
    } catch (error) {
        console.error('检查AI状态失败:', error);
    }
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    initUpload();
    checkAIStatus();
    loadHistory();
    loadLatestContext(); // 加载最新的分析上下文
    
    document.getElementById('runAnalysisBtn').addEventListener('click', () => {
        // 直接启动分析，后端会自动处理用户目录下的数据
        runAnalysis(false);
    });
    
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    
    document.getElementById('chatInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});

// 下载分析结果
async function downloadResults() {
    const username = getUsername();
    const downloadBtn = document.getElementById('downloadBtn');
    
    try {
        // 显示加载状态
        downloadBtn.disabled = true;
        downloadBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 打包中...';
        
        const url = `/analysis/download${username ? '?username=' + encodeURIComponent(username) : ''}`;
        const response = await fetch(url);
        
        if (!response.ok) {
            if (response.status === 404) {
                showToast('暂无分析结果可下载', 'warning');
            } else {
                showToast('下载失败，请稍后重试', 'error');
            }
            return;
        }
        
        // 获取文件名
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'analysis_results.zip';
        if (contentDisposition) {
            const match = contentDisposition.match(/filename=(.+)/);
            if (match) {
                filename = match[1].replace(/"/g, '');
            }
        }
        
        // 下载文件
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(downloadUrl);
        
        showToast('下载成功！', 'success');
        
    } catch (error) {
        console.error('下载失败:', error);
        showToast('下载失败: ' + error.message, 'error');
    } finally {
        // 恢复按钮状态
        downloadBtn.disabled = false;
        downloadBtn.innerHTML = '<i class="fas fa-download"></i> 下载结果';
    }
}

// 加载最新的分析上下文（用于AI对话）
async function loadLatestContext() {
    const username = getUsername();
    if (!username) return;
    
    try {
        const response = await fetch(`/analysis/history?username=${encodeURIComponent(username)}`);
        const data = await response.json();
        
        if (data.success && data.history && data.history.length > 0) {
            // 获取最新记录的详情
            const latest = data.history[0];
            const detailResponse = await fetch(`/analysis/detail?filename=${encodeURIComponent(latest.filename)}`);
            const detailData = await detailResponse.json();
            
            if (detailData.success && detailData.analysis) {
                analysisContext = JSON.stringify(detailData.analysis);
                console.log('已加载最新分析上下文');
            }
        }
    } catch (error) {
        console.error('加载分析上下文失败:', error);
    }
}
