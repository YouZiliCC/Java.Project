// smart_qna.html 分离出的全部 JS 脚本
// 智能问答交互
const chatArea = document.getElementById('chat-area');
const qnaForm = document.getElementById('qna-form');
const questionInput = document.getElementById('question-input');
function appendBubble(text, type) {
    const bubble = document.createElement('div');
    bubble.className = 'chat-bubble ' + (type === 'user' ? 'chat-user' : 'chat-ai');
    bubble.innerHTML = text;
    chatArea.appendChild(bubble);
    chatArea.scrollTop = chatArea.scrollHeight;
}
qnaForm.addEventListener('submit', function(e) {
    e.preventDefault();
    const question = questionInput.value.trim();
    if (!question) return;
    appendBubble(question, 'user');
    questionInput.value = '';
    appendBubble('正在生成答案，请稍候...', 'ai');
    // TODO: 调用后端接口 /api/qna/ask
    /*
    fetch('/api/qna/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question })
    })
    .then(res => res.json())
    .then(data => {
        // 移除“正在生成答案”气泡
        chatArea.lastChild.remove();
        appendBubble(data.answer, 'ai');
    })
    .catch(() => {
        chatArea.lastChild.remove();
        appendBubble('答案生成失败，请重试。', 'ai');
    });
    */
    setTimeout(() => {
        chatArea.lastChild.remove();
        appendBubble('（模拟答案）这是智能问答系统生成的专业解答。', 'ai');
    }, 1500);
});
