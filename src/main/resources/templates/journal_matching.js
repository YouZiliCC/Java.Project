// Tailwind 配置
if (typeof tailwind !== 'undefined') {
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
}

// 移动端菜单切换
if (document.getElementById('mobile-menu-button')) {
    document.getElementById('mobile-menu-button').addEventListener('click', function() {
        const mobileMenu = document.getElementById('mobile-menu');
        mobileMenu.classList.toggle('hidden');
    });
}

// ...existing code for all JS逻辑（464行起）...
// 由于内容较多，建议将464行后的所有<script>内容整体粘贴到此文件
// 包括getSurveyDataFromUrl、getOptionLabel、displayUserSelections、generateUserProfile、recommendJournal等所有函数
// 并在html中用<script src="journal_matching.js"></script>替换原有内联js
