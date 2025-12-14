// home.html 分离出的全部 JS 脚本

// Tailwind 配置
window.tailwind = window.tailwind || {};
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#3B82F6',
                secondary: '#10B981',
                accent: '#8B5CF6',
            },
            fontFamily: {
                inter: ['Inter', 'system-ui', 'sans-serif'],
            },
        }
    }
};

// 移动端菜单切换
if (document.getElementById('mobile-menu-button')) {
    document.getElementById('mobile-menu-button').addEventListener('click', function() {
        const mobileMenu = document.getElementById('mobile-menu');
        if (mobileMenu) mobileMenu.classList.toggle('hidden');
    });
}

// 平滑滚动
if (document.querySelectorAll('a[href^="#"]').length > 0) {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80,
                    behavior: 'smooth'
                });
                if (document.getElementById('mobile-menu')) {
                    document.getElementById('mobile-menu').classList.add('hidden');
                }
            }
        });
    });
}

// 示例分析图表示例
window.addEventListener('DOMContentLoaded', () => {
    // 获取 canvas 元素
    const lineCanvas = document.getElementById('lineChart');
    const barCanvas = document.getElementById('barChart');
    if (lineCanvas && barCanvas) {
        const lineCtx = lineCanvas.getContext('2d');
        const barCtx = barCanvas.getContext('2d');
        new Chart(lineCtx, {
            type: 'line',
            data: {
                labels: ['一月', '二月', '三月', '四月', '五月'],
                datasets: [
                    {
                        label: '示例数据1',
                        data: [10, 20, 15, 25, 30],
                        borderColor: '#3B82F6',
                        backgroundColor: 'rgba(59, 130, 246, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
        new Chart(barCtx, {
            type: 'bar',
            data: {
                labels: ['A', 'B', 'C', 'D', 'E'],
                datasets: [
                    {
                        label: '示例数据',
                        data: [12, 19, 3, 5, 2],
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.2)',
                            'rgba(54, 162, 235, 0.2)',
                            'rgba(255, 206, 86, 0.2)',
                            'rgba(75, 192, 192, 0.2)',
                            'rgba(153, 102, 255, 0.2)'
                        ],
                        borderColor: [
                            'rgba(255, 99, 132, 1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)'
                        ],
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
    // 示例分析展示区图表
    const themeCanvas = document.getElementById('themeChart');
    const noveltyCanvas = document.getElementById('noveltyChart');
    if (themeCanvas && noveltyCanvas) {
        const themeCtx = themeCanvas.getContext('2d');
        const noveltyCtx = noveltyCanvas.getContext('2d');
        new Chart(themeCtx, {
            type: 'bar',
            data: {
                labels: ['主题A', '主题B', '主题C', '主题D'],
                datasets: [
                    {
                        label: '中国期刊',
                        data: [35, 25, 15, 25],
                        backgroundColor: 'rgba(59, 130, 246, 0.8)',
                    },
                    {
                        label: '国外期刊',
                        data: [20, 30, 25, 25],
                        backgroundColor: 'rgba(16, 185, 129, 0.8)',
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
        new Chart(noveltyCtx, {
            type: 'line',
            data: {
                labels: ['2019', '2020', '2021', '2022', '2023'],
                datasets: [
                    {
                        label: '新颖性指数',
                        data: [10, 15, 20, 25, 30],
                        borderColor: '#8B5CF6',
                        backgroundColor: 'rgba(139, 92, 246, 0.2)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
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
    document.querySelectorAll('section > div').forEach(section => {
        observer.observe(section);
    });
});
