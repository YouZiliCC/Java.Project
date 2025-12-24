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
    
    let html = `
        <div class="result-summary">
            <div class="stat-card">
                <div class="stat-value">${analysis.total_papers || data.totalPapers || 0}</div>
                <div class="stat-label">论文总数</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${analysis.avg_citations || 0}</div>
                <div class="stat-label">平均引用</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${analysis.avg_refs || 0}</div>
                <div class="stat-label">平均参考文献</div>
            </div>
        </div>
    `;
    
    if (analysis.most_cited_paper) {
        html += `
            <div class="result-section">
                <h4><i class="fas fa-trophy"></i> 最高被引论文</h4>
                <div class="highlight-card">
                    <div class="paper-title">${analysis.most_cited_paper.title}</div>
                    <div class="paper-meta">
                        <span><i class="fas fa-user"></i> ${analysis.most_cited_paper.author}</span>
                        <span><i class="fas fa-quote-right"></i> 被引: ${analysis.most_cited_paper.citations}</span>
                    </div>
                </div>
            </div>
        `;
    }
    
    if (analysis.target_distribution && Object.keys(analysis.target_distribution).length > 0) {
        html += `
            <div class="result-section">
                <h4><i class="fas fa-tags"></i> 领域分布</h4>
                <div class="distribution-list">
                    ${Object.entries(analysis.target_distribution).map(([key, value]) => `
                        <div class="distribution-item">
                            <span class="dist-label">${key}</span>
                            <span class="dist-value">${value}</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    if (analysis.country_distribution && Object.keys(analysis.country_distribution).length > 0) {
        html += `
            <div class="result-section">
                <h4><i class="fas fa-globe"></i> 国家/地区分布</h4>
                <div class="distribution-list">
                    ${Object.entries(analysis.country_distribution).slice(0, 10).map(([key, value]) => `
                        <div class="distribution-item">
                            <span class="dist-label">${key}</span>
                            <span class="dist-value">${value}</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    container.innerHTML = html;
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
            
            historyList.innerHTML = data.history.map(item => `
                <div class="history-item" onclick="loadHistoryDetail('${item.filename}')">
                    <div class="history-name">${item.originalName || '未命名'}</div>
                    <div class="history-meta">
                        <span><i class="fas fa-file"></i> ${item.totalPapers || 0} 篇论文</span>
                        <span><i class="fas fa-clock"></i> ${formatDate(item.createdAt)}</span>
                    </div>
                </div>
            `).join('');
        } else {
            historyContainer.style.display = 'none';
        }
    } catch (error) {
        console.error('加载历史失败:', error);
    }
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
        if (!uploadedFilename) {
            showToast('请先上传数据文件', 'error');
            return;
        }
        runAnalysis(false);
    });
    
    document.getElementById('analyzeDbBtn').addEventListener('click', () => {
        runAnalysis(true);
    });
    
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    
    document.getElementById('chatInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});

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
