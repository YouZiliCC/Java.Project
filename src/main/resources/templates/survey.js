// survey.js
// 迁移自 survey.html 的 JS 逻辑

window.addEventListener('DOMContentLoaded', function() {
    // 移动端菜单切换
    var mobileMenuBtn = document.getElementById('mobile-menu-button');
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', function() {
            const mobileMenu = document.getElementById('mobile-menu');
            if (mobileMenu) mobileMenu.classList.toggle('hidden');
        });
    }

    // 表单提交处理 - 修改后的逻辑
    var surveyForm = document.getElementById('survey-form');
    if (surveyForm) {
        surveyForm.addEventListener('submit', function(e) {
            e.preventDefault();
            // 获取表单数据
            const formData = new FormData(this);
            const surveyData = {
                innovation: formData.get('innovation'),
                discipline: formData.get('discipline'),
                theme: formData.get('theme')
            };
            // 打印数据到控制台
            console.log('问卷数据:', surveyData);
            // 构建跳转URL
            const params = new URLSearchParams();
            params.append('innovation', surveyData.innovation);
            params.append('discipline', surveyData.discipline);
            params.append('theme', surveyData.theme);
            // 跳转到期刊匹配页面
            window.location.href = `journal_matching.html?${params.toString()}`;
        });
    }

    // 关闭模态框
    var closeModalBtn = document.getElementById('close-modal');
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', function() {
            var successModal = document.getElementById('success-modal');
            if (successModal) successModal.classList.add('hidden');
        });
    }

    // 点击模态框外部关闭
    var successModal = document.getElementById('success-modal');
    if (successModal) {
        successModal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.add('hidden');
            }
        });
    }

    // 滚动动画
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.1
    };
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    document.querySelectorAll('form > div').forEach(element => {
        observer.observe(element);
    });
});
