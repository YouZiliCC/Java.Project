// 公共组件 - 导航栏和页面基础结构

// 渲染导航栏
function renderNavbar() {
    const user = localStorage.getItem('currentUser');
    const isLoggedIn = !!user;
    
    return `
    <nav class="navbar">
        <a href="index.html" class="navbar-brand">
            <i class="fas fa-graduation-cap"></i>
            PaperMaster
        </a>
        <div class="navbar-menu">
            <a href="index.html">首页</a>
            <a href="analysis.html">期刊分析</a>
            ${isLoggedIn ? 
                '<a href="#" id="logoutLink">登出</a>' : 
                '<a href="login.html">登录</a><a href="register.html" class="btn btn-primary">注册</a>'
            }
        </div>
    </nav>`;
}

// 初始化导航栏
function initNavbar() {
    const navContainer = document.getElementById('navbar-container');
    if (navContainer) {
        navContainer.innerHTML = renderNavbar();
        
        // 绑定登出事件
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                localStorage.removeItem('currentUser');
                window.location.href = 'index.html';
            });
        }
    }
}

// 检查是否需要登录
function requireLogin(redirectUrl) {
    const user = localStorage.getItem('currentUser');
    if (!user) {
        window.location.href = 'login.html?redirect=' + encodeURIComponent(redirectUrl);
        return false;
    }
    return true;
}

// 检查登录状态
function isLoggedIn() {
    return localStorage.getItem('currentUser') !== null;
}

// 获取当前用户
function getCurrentUser() {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
}

// Toast 提示
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    toast.offsetHeight;
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 页面加载时初始化导航栏
document.addEventListener('DOMContentLoaded', initNavbar);
