// import_analysis.html 分离出的全部 JS 脚本
// 表单上传与分析

document.getElementById('import-form').addEventListener('submit', function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    document.getElementById('report-section').style.display = 'block';
    document.getElementById('report-status').innerHTML = '正在生成报告，请稍候...';
    document.getElementById('report-result').innerHTML = '';
    // TODO: 调用后端接口 /api/import/analyze 上传文件并返回分析结果
    // 示例：
    /*
    fetch('/api/import/analyze', {
        method: 'POST',
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        document.getElementById('report-status').innerHTML = '报告已生成';
        document.getElementById('report-result').innerHTML = data.resultHtml;
    })
    .catch(() => {
        document.getElementById('report-status').innerHTML = '报告生成失败，请重试。';
    });
    */
    setTimeout(() => {
        document.getElementById('report-status').innerHTML = '报告已生成';
        document.getElementById('report-result').innerHTML = '<p>（模拟报告）您的数据分析报告已生成，见下方详细内容。</p>';
    }, 2000);
});
// 渲染示例Markdown报告
const exampleMd = `# 期刊内容分析报告\n\n---\n\n## 数据概览\n- 上传文件：example.pdf\n- 记录数：1200\n- 涉及期刊：15\n\n## 主要发现\n1. **新颖性指数**：整体高于行业平均水平。\n2. **跨学科性**：涉及3个以上学科，交叉度显著。\n3. **主题分布**：以人工智能、信息管理为主。\n\n## 详细分析\n\n| 指标         | 分值 | 说明           |\n| ------------ | ---- | -------------- |\n| 新颖性       | 87   | 高于平均       |\n| 颠覆性       | 72   | 行业中等偏上   |\n| 跨学科性     | 91   | 非常突出       |\n| 主题复杂度   | 78   | 多主题融合     |\n\n> 本报告由系统自动生成，仅供参考。`;
document.getElementById('example-markdown').innerHTML = marked.parse(exampleMd);
