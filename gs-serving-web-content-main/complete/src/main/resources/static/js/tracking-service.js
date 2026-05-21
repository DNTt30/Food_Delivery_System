/**
 * TrackingService Module: Điều phối giữa MapTracker, WebSocket và Polling.
 * Logic: WS sống -> Tắt Polling. WS chết -> Bật Polling + Reconnect WS.
 */
const TrackingService = (function() {
    let currentOrderId = null;
    let currentToken = null;
    let pollingTimer = null;
    let isWsConnected = false;

    function init(orderId, token) {
        currentOrderId = orderId;
        currentToken = token;
        initWebSocket();
    }

    function initWebSocket() {
        const topic = '/topic/tracking/' + currentOrderId;
        
        WebSocketManager.connect(
            currentToken,
            topic,
            handleLocationData, 
            function() { 
                isWsConnected = true;
                stopFallbackPolling();
                console.log("WebSocket connected. Stopped polling.");
                
                // Lắng nghe sự kiện thay đổi trạng thái đơn hàng (để reload thanh progress bar)
                WebSocketManager.subscribe('/topic/order-status.' + currentOrderId, function(data) {
                    console.log("Order status changed! Reloading page...", data);
                    window.location.reload();
                });
            },
            function() {
                isWsConnected = false;
                startFallbackPolling();
                console.warn("WebSocket disconnected. Started polling.");
                
                // Thử kết nối lại sau 10s
                setTimeout(initWebSocket, 10000);
            }
        );
    }

    function startFallbackPolling() {
        if (!pollingTimer) {
            fetchLatestLocation();
            pollingTimer = setInterval(fetchLatestLocation, 5000);
        }
    }

    function stopFallbackPolling() {
        if (pollingTimer) {
            clearInterval(pollingTimer);
            pollingTimer = null;
        }
    }

    function fetchLatestLocation() {
        if (isWsConnected) return;

        fetch(`/api/customer/orders/${currentOrderId}/live-location`, {
            headers: { 'Authorization': 'Bearer ' + currentToken }
        })
        .then(res => {
            if (res.status === 200) return res.json();
            throw new Error("No data or error");
        })
        .then(data => handleLocationData(data))
        .catch(err => console.debug("Polling update skipped:", err.message));
    }

    function handleLocationData(data) {
        // Cập nhật UI Overlay
        const nameLabel = document.getElementById('driverNameLabel');
        const badge = document.getElementById('orderPhaseBadge');
        
        if (nameLabel) nameLabel.innerText = "Tài xế: " + data.driverName;
        if (badge) {
            badge.innerText = formatPhase(data.phase);
            // Cập nhật màu sắc badge dựa trên phase
            if (data.phase === 'DELIVERING') badge.className = 'badge bg-success mb-1';
            else if (data.phase === 'ARRIVED') badge.className = 'badge bg-dark mb-1';
            else badge.className = 'badge bg-primary mb-1';
        }

        // Cập nhật Bản đồ
        const coords = window.ORDER_COORDS;
        if (coords) {
            MapTracker.updateDriverPosition(
                data.latitude, data.longitude, data.phase,
                coords.rLat, coords.rLng, coords.cLat, coords.cLng
            );
        }
    }

    function formatPhase(phase) {
        const phases = {
            'DRIVER_ACCEPTED': 'TÀI XẾ VỪA NHẬN',
            'GOING_TO_RESTAURANT': 'ĐANG ĐẾN QUÁN',
            'WAITING_AT_RESTAURANT': 'ĐANG CHỜ MÓN',
            'DELIVERING': 'ĐANG GIAO HÀNG',
            'ARRIVED': 'ĐÃ ĐẾN NƠI'
        };
        return phases[phase] || 'ĐANG CẬP NHẬT';
    }

    return { init };
})();
