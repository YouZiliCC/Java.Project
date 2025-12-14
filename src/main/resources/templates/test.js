// test.html 分离出的全部 JS 脚本
document.addEventListener('DOMContentLoaded', () => {
    const ctx = document.getElementById('testChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['January', 'February', 'March', 'April', 'May'],
            datasets: [{
                label: 'Test Data',
                data: [10, 20, 15, 25, 30],
                borderColor: 'blue',
                backgroundColor: 'rgba(0, 0, 255, 0.1)',
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });
});
