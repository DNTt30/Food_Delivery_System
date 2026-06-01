// ===== Dashboard Analytics JavaScript =====

const API_BASE = '/api/restaurant/analytics';
let revenueComparisonChart = null;
let bestSellersChart = null;
let currentStats = null;

/**
 * Load statistics theo preset (this-week, this-month, etc.)
 */
function loadStatistics(preset) {
    const url = `${API_BASE}/${preset}`;
    
    showLoading();
    
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${getJWTToken()}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(data => {
        currentStats = data;
        updateDashboard(data);
        hideLoading();
    })
    .catch(error => {
        console.error('Lỗi:', error);
        showError('Không thể tải dữ liệu. Vui lòng thử lại.');
        hideLoading();
    });
}

/**
 * Load statistics cho custom date range
 */
function loadCustomRange() {
    const startDate = document.getElementById('customStartDate').value;
    const endDate = document.getElementById('customEndDate').value;
    
    if (!startDate || !endDate) {
        showError('Vui lòng chọn cả hai ngày.');
        return;
    }
    
    const url = `${API_BASE}/custom?startDate=${startDate}&endDate=${endDate}`;
    
    showLoading();
    
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${getJWTToken()}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(data => {
        currentStats = data;
        updateDashboard(data);
        hideLoading();
        // Close collapse
        bootstrap.Collapse.getInstance(document.getElementById('customRange')).hide();
    })
    .catch(error => {
        console.error('Lỗi:', error);
        showError('Không thể tải dữ liệu. Vui lòng thử lại.');
        hideLoading();
    });
}

/**
 * Update dashboard UI with stats data
 */
function updateDashboard(stats) {
    // Update revenue metrics
    document.getElementById('currentRevenue').textContent = formatCurrency(stats.currentPeriodRevenue);
    document.getElementById('previousRevenue').textContent = formatCurrency(stats.previousPeriodRevenue);
    document.getElementById('periodLabel').textContent = stats.periodLabel || 'Tuần này';
    
    // Update growth indicator
    const growthPercent = (stats.revenueGrowthPercent || 0).toFixed(1);
    const growthIcon = stats.growthTrendIcon || '↑';
    const growthColor = stats.growthTrendColor || 'text-success';
    
    document.getElementById('growthPercent').textContent = `${growthPercent}%`;
    document.getElementById('growthPercent').className = `fw-bold ${growthColor}`;
    document.getElementById('growthIcon').textContent = growthIcon;
    document.getElementById('growthIcon').className = `display-6 ${growthColor}`;
    
    // Update order metrics
    document.getElementById('currentOrders').textContent = stats.currentPeriodOrders || 0;
    document.getElementById('cancelledOrders').textContent = stats.cancelledOrders || 0;
    document.getElementById('completionRate').textContent = `${(stats.completionRate || 0).toFixed(1)}%`;
    
    // Update order growth
    const orderGrowth = (stats.orderGrowthPercent || 0).toFixed(1);
    const orderIcon = stats.orderTrendIcon || '↑';
    const orderColor = stats.orderTrendColor || 'text-success';
    
    document.getElementById('orderPercent').textContent = `${orderGrowth}%`;
    document.getElementById('orderPercent').className = `fw-bold ${orderColor}`;
    document.getElementById('orderIcon').textContent = orderIcon;
    document.getElementById('orderIcon').className = `display-6 ${orderColor}`;
    
    // Update charts
    updateRevenueComparisonChart(stats.chartDataCurrent, stats.chartDataPrevious);
    
    // Update best sellers
    updateBestSellersChart(stats.topFiveBestSellers);
    updateBestSellersTable(stats.topFiveBestSellers);
    
    // Update slow moving items
    updateSlowMovingItemsTable(stats.slowMovingItems);
}

/**
 * Update revenue comparison chart (Current vs Previous)
 */
function updateRevenueComparisonChart(currentData, previousData) {
    const ctx = document.getElementById('revenueComparisonChart');
    if (!ctx) return;
    
    // Get dates from current data
    const dates = (currentData || []).map(d => d.date.slice(5)); // YYYY-MM-DD -> MM-DD
    
    // Pad previous data to match current data length
    const paddedPrevious = Array(dates.length).fill(0);
    (previousData || []).forEach((d, i) => {
        if (i < paddedPrevious.length) {
            paddedPrevious[i] = d.revenue;
        }
    });
    
    if (revenueComparisonChart) {
        revenueComparisonChart.destroy();
    }
    
    revenueComparisonChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [
                {
                    label: 'Kỳ Này',
                    data: (currentData || []).map(d => d.revenue),
                    borderColor: '#27ae60',
                    backgroundColor: 'rgba(39, 174, 96, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: '#27ae60'
                },
                {
                    label: 'Kỳ Trước',
                    data: paddedPrevious,
                    borderColor: '#e74c3c',
                    backgroundColor: 'rgba(231, 76, 60, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: '#e74c3c'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    display: true,
                    labels: {
                        font: { size: 14, weight: '600' },
                        padding: 15,
                        usePointStyle: true
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrencyShort(value);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Update best sellers bar chart
 */
function updateBestSellersChart(bestSellers) {
    const ctx = document.getElementById('bestSellersChart');
    if (!ctx) return;
    
    const sellers = bestSellers || [];
    const labels = sellers.map(s => s.itemName);
    const data = sellers.map(s => s.totalRevenue);
    const colors = ['#f39c12', '#3498db', '#27ae60', '#e74c3c', '#9b59b6'];
    
    if (bestSellersChart) {
        bestSellersChart.destroy();
    }
    
    bestSellersChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh Thu (đ)',
                data: data,
                backgroundColor: colors.slice(0, sellers.length),
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    ticks: {
                        callback: function(value) {
                            return formatCurrencyShort(value);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Update best sellers table
 */
function updateBestSellersTable(bestSellers) {
    const tbody = document.getElementById('bestSellersList');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    (bestSellers || []).forEach((seller, index) => {
        const rankClass = index === 0 ? 'gold' : index === 1 ? 'silver' : index === 2 ? 'bronze' : '';
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><span class="rank-badge ${rankClass}">${seller.rank || index + 1}</span></td>
            <td>${seller.itemName}</td>
            <td><strong>${seller.soldCount}</strong></td>
            <td>${formatCurrency(seller.totalRevenue)}</td>
        `;
        tbody.appendChild(row);
    });
}

/**
 * Update slow moving items table
 */
function updateSlowMovingItemsTable(slowItems) {
    const tbody = document.getElementById('slowMovingList');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    (slowItems || []).forEach((item, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><span class="rank-badge" style="background: #95a5a6;">${item.rank || index + 1}</span></td>
            <td>${item.itemName}</td>
            <td><span class="badge bg-warning text-dark">${item.soldCount}</span></td>
            <td><span class="badge bg-danger">${(item.cancellationRate || 0).toFixed(1)}%</span></td>
        `;
        tbody.appendChild(row);
    });
}

// ===== UTILITY FUNCTIONS =====

function formatCurrency(value) {
    if (!value) return '0 đ';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(value);
}

function formatCurrencyShort(value) {
    if (!value) return '0đ';
    if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M';
    if (value >= 1000) return (value / 1000).toFixed(1) + 'K';
    return value.toFixed(0) + 'đ';
}

function getJWTToken() {
    // Get JWT token from localStorage or cookies
    return localStorage.getItem('jwtToken') || '';
}

function showLoading() {
    // Optionally add loading spinner
}

function hideLoading() {
    // Hide loading spinner
}

function showError(message) {
    alert(message);
}

// ===== INIT ON PAGE LOAD =====
document.addEventListener('DOMContentLoaded', function() {
    // Set default custom dates to this month
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    document.getElementById('customStartDate').valueAsDate = firstDay;
    document.getElementById('customEndDate').valueAsDate = today;
    
    // Load this week data by default
    loadStatistics('this-week');
});
