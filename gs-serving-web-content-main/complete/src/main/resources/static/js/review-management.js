// ===== Review Management JavaScript =====

const API_BASE = '/api/restaurant/reviews';
let currentPage = 0;
let currentSort = 'newest';
let currentPageSize = 10;
let totalPages = 0;
let currentReplyReviewId = null;

/**
 * Load reviews with pagination
 */
function loadReviews(page = 0) {
    currentPage = page;
    currentSort = document.getElementById('sortSelect').value;
    currentPageSize = parseInt(document.getElementById('pageSizeSelect').value);
    
    const url = `${API_BASE}?page=${page}&size=${currentPageSize}&sort=${currentSort}`;
    
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
        renderReviews(data.reviews);
        renderPagination(data);
        loadStatistics();
        hideLoading();
    })
    .catch(error => {
        console.error('Lỗi:', error);
        showError('Không thể tải dữ liệu. Vui lòng thử lại.');
        hideLoading();
    });
}

/**
 * Load statistics
 */
function loadStatistics() {
    const statsUrl = `${API_BASE}/statistics`;
    
    fetch(statsUrl, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${getJWTToken()}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('totalReviews').textContent = data.totalReviews || 0;
        document.getElementById('unrepliedReviews').textContent = data.unrepliedReviews || 0;
        document.getElementById('averageRating').textContent = (data.averageRating || 0).toFixed(1);
        document.getElementById('reviewsWithImages').textContent = data.reviewsWithImages || 0;
    })
    .catch(error => console.error('Lỗi tải thống kê:', error));
}

/**
 * Render reviews list
 */
function renderReviews(reviews) {
    const container = document.getElementById('reviewsList');
    
    if (!reviews || reviews.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📝</div>
                <h3>Chưa có đánh giá</h3>
                <p>Hãy chờ khách hàng gửi những đánh giá đầu tiên!</p>
            </div>
        `;
        return;
    }
    
    const html = reviews.map(review => renderReviewItem(review)).join('');
    container.innerHTML = html;
}

/**
 * Render single review item
 */
function renderReviewItem(review) {
    const starHtml = generateStars(review.rating);
    const imagesHtml = review.imageUrls && review.imageUrls.length > 0 
        ? renderReviewImages(review.imageUrls) 
        : '';
    const replyHtml = review.hasReply ? renderShopReply(review) : '';
    const inappropriateHtml = review.hasInappropriateWords 
        ? '<span class="inappropriate-badge">⚠️ Chứa từ không lành mạnh</span>' 
        : '';
    const verifiedBadge = review.isVerifiedPurchase 
        ? '<span class="badge bg-success ms-2">✓ Mua hàng xác thực</span>' 
        : '';
    
    return `
        <div class="review-item ${review.hasReply ? 'has-reply' : ''}">
            <!-- Header -->
            <div class="review-header">
                <div class="reviewer-info">
                    <div class="reviewer-avatar">${getInitials(review.customerName)}</div>
                    <div>
                        <div class="reviewer-name">${review.customerName} ${verifiedBadge}</div>
                        <div class="review-time">${review.createdAtFormatted}</div>
                    </div>
                </div>
                <div class="rating-badge">${review.rating} ⭐</div>
            </div>
            
            <!-- Stars -->
            <div class="rating-stars">${starHtml}</div>
            
            <!-- Comment -->
            <div class="review-comment">${escapeHtml(review.comment)}</div>
            ${inappropriateHtml}
            
            <!-- Images -->
            ${imagesHtml}
            
            <!-- Shop Reply -->
            ${replyHtml}
            
            <!-- Actions -->
            <div class="review-actions">
                ${!review.hasReply ? `
                    <button class="btn-reply" onclick="openReplyModal(${review.id})">
                        <i class="fas fa-reply"></i> Phản Hồi
                    </button>
                ` : ''}
                <button class="btn-helpful">
                    <i class="fas fa-thumbs-up"></i> Hữu Ích (${review.helpfulCount || 0})
                </button>
            </div>
        </div>
    `;
}

/**
 * Render review images
 */
function renderReviewImages(imageUrls) {
    if (!imageUrls || imageUrls.length === 0) return '';
    
    const imageThumbs = imageUrls.map((url, index) => `
        <div class="review-image-thumb" onclick="openImageLightbox('${url}')">
            <img src="${url}" alt="Review image ${index + 1}" loading="lazy">
        </div>
    `).join('');
    
    return `
        <div class="review-images">
            ${imageThumbs}
        </div>
    `;
}

/**
 * Render shop reply section
 */
function renderShopReply(review) {
    return `
        <div class="shop-reply-section">
            <div class="shop-reply-header">
                <i class="fas fa-store"></i>
                <strong>Trả lời từ nhà hàng</strong>
            </div>
            <div class="shop-reply-text">${escapeHtml(review.restaurantReply)}</div>
            <div class="reply-time">Phản hồi ${review.repliedAtFormatted}</div>
        </div>
    `;
}

/**
 * Render pagination
 */
function renderPagination(data) {
    const navContainer = document.getElementById('paginationNav');
    totalPages = data.totalPages;
    
    if (totalPages <= 1) {
        navContainer.innerHTML = '';
        return;
    }
    
    let html = '<ul class="pagination">';
    
    // Previous button
    if (data.hasPrevious) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="loadReviews(${currentPage - 1}); return false;">← Trước</a></li>`;
    }
    
    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        const isActive = i === currentPage ? 'active' : '';
        html += `<li class="page-item ${isActive}"><a class="page-link" href="#" onclick="loadReviews(${i}); return false;">${i + 1}</a></li>`;
    }
    
    // Next button
    if (data.hasNext) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="loadReviews(${currentPage + 1}); return false;">Sau →</a></li>`;
    }
    
    html += '</ul>';
    navContainer.innerHTML = html;
}

/**
 * Open reply modal
 */
function openReplyModal(reviewId) {
    currentReplyReviewId = reviewId;
    const modal = new bootstrap.Modal(document.getElementById('replyModal'));
    document.getElementById('replyText').value = '';
    modal.show();
}

/**
 * Submit reply
 */
function submitReply() {
    const reply = document.getElementById('replyText').value.trim();
    
    if (!reply) {
        showError('Vui lòng nhập nội dung phản hồi');
        return;
    }
    
    const btn = document.getElementById('submitReplyBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';
    
    fetch(`${API_BASE}/${currentReplyReviewId}/reply`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${getJWTToken()}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reply: reply })
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(data => {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-paper-plane"></i> Gửi Phản Hồi';
        
        bootstrap.Modal.getInstance(document.getElementById('replyModal')).hide();
        showSuccess('Phản hồi thành công!');
        loadReviews(currentPage);
    })
    .catch(error => {
        console.error('Lỗi:', error);
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-paper-plane"></i> Gửi Phản Hồi';
        showError('Không thể gửi phản hồi. Vui lòng thử lại.');
    });
}

/**
 * Open image lightbox
 */
function openImageLightbox(imageUrl) {
    document.getElementById('lightboxImage').src = imageUrl;
    new bootstrap.Modal(document.getElementById('imageLightbox')).show();
}

// ===== UTILITY FUNCTIONS =====

function generateStars(rating) {
    let html = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            html += '<i class="fas fa-star"></i>';
        } else {
            html += '<i class="far fa-star"></i>';
        }
    }
    return html;
}

function getInitials(name) {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function getJWTToken() {
    return localStorage.getItem('jwtToken') || '';
}

function showLoading() {
    document.getElementById('reviewsList').innerHTML = `
        <div class="loading-spinner">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Đang tải...</span>
            </div>
            <p class="text-muted mt-3">Đang tải đánh giá...</p>
        </div>
    `;
}

function hideLoading() {
    // Loading is replaced by actual content
}

function showError(message) {
    alert('❌ ' + message);
}

function showSuccess(message) {
    // Toast or alert
    alert('✅ ' + message);
}

// ===== INIT ON PAGE LOAD =====
document.addEventListener('DOMContentLoaded', function() {
    loadReviews();
});
