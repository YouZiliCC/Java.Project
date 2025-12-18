// Analysis Page Logic

let uploadedFilename = null;
let analysisContext = null;

// 文件上传处理
function initUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    
    // 点击上传
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });
    
    // 文件选择
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileUpload(e.target.files[0]);
        }
    });
    
    // 拖拽上传
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
    const allowedTypes = ['application/json', 'text/csv'];
    const allowedExtensions = ['.json', '.csv'];
    
    const extension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!allowedExtensions.includes(extension)) {
        showToast('请上传 JSON 或 CSV 格式的文件', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
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
            
            // 显示已上传文件
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
        
        const response = await fetch('/analysis/run', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (data.success) {
            displayAnalysisResult(data);
            analysisContext = JSON.stringify(data.analysis);
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
    
    // 最高被引论文
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
    
    // 领域分布
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
    
    // 国家分布
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

// AI对话
async function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    // 添加用户消息
    addChatMessage(message, 'user');
    input.value = '';
    
    try {
        const response = await fetch('/analysis/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: message,
                context: analysisContext
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
    
    const icon = type === 'user' ? 'fa-user' : 'fa-robot';
    
    messageDiv.innerHTML = `
        <div class="message-avatar"><i class="fas ${icon}"></i></div>
        <div class="message-content">${content}</div>
    `;
    
    container.appendChild(messageDiv);
    
    // 滚动到底部
    container.scrollTop = container.scrollHeight;
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    initUpload();
    
    // 运行分析按钮
    document.getElementById('runAnalysisBtn').addEventListener('click', () => {
        if (!uploadedFilename) {
            showToast('请先上传数据文件', 'error');
            return;
        }
        runAnalysis(false);
    });
    
    // 分析数据库按钮
    document.getElementById('analyzeDbBtn').addEventListener('click', () => {
        runAnalysis(true);
    });
    
    // 发送消息按钮
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    
    // 回车发送
    document.getElementById('chatInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});
