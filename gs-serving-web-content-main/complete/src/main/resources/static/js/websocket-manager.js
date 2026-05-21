/**
 * WebSocketManager Module: Xử lý đóng/mở kết nối STOMP.
 */
const WebSocketManager = (function() {
    let stompClient = null;

    function connect(token, topic, onMessageCallback, onConnectCallback, onDisconnectCallback) {
        // Gửi token qua URL query parameter cho SockJS
        const socket = new SockJS('/ws?token=' + token);
        stompClient = Stomp.over(socket);
        
        stompClient.debug = null; 

        stompClient.connect({}, function (frame) {
            console.log('⚡ STOMP WebSocket Connected!');
            if (onConnectCallback) onConnectCallback();

            stompClient.subscribe(topic, function (message) {
                if (onMessageCallback) {
                    onMessageCallback(JSON.parse(message.body));
                }
            });

        }, function (error) {
            console.error('❌ STOMP Disconnected/Error:', error);
            if (onDisconnectCallback) onDisconnectCallback();
        });
    }

    function disconnect() {
        if (stompClient !== null) {
            stompClient.disconnect();
            console.log("Đã chủ động ngắt kết nối STOMP.");
        }
    }

    function subscribe(topic, callback) {
        if (stompClient && stompClient.connected) {
            stompClient.subscribe(topic, function (message) {
                if (callback) callback(JSON.parse(message.body));
            });
        }
    }

    return { connect, disconnect, subscribe, getClient: () => stompClient };
})();
