// Auth Logic

async function handleLogin(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            body: formData // Send as multipart/form-data or x-www-form-urlencoded automatically
        });
        
        const result = await response.text(); // Backend returns plain string
        
        if (result.includes('登录成功')) {
            showToast('登录成功！正在跳转...', 'success');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        } else {
            showToast(result, 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showToast('登录请求失败', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Validate passwords match
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    if (password !== confirmPassword) {
        showToast('两次输入的密码不一致', 'error');
        return;
    }
    
    try {
        // Using register-direct as verifycode is disabled
        const response = await fetch('/auth/register-direct', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.text();
        
        if (result.includes('成功') || result.includes('success')) {
            showToast('注册成功！请登录', 'success');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 1500);
        } else {
            showToast(result, 'error');
        }
    } catch (error) {
        console.error('Register error:', error);
        showToast('注册请求失败', 'error');
    }
}

// Attach listeners
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
});
