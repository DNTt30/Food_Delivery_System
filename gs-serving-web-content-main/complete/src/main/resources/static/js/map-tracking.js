/**
 * MapTracker Module: Đóng gói toàn bộ logic Leaflet
 */
const MapTracker = (function() {
    let map;
    let driverMarker;
    let routingControl;

    const ICONS = {
        restaurant: L.icon({ iconUrl: 'https://cdn-icons-png.flaticon.com/512/3170/3170733.png', iconSize: [40, 40], iconAnchor: [20, 40] }),
        customer: L.icon({ iconUrl: 'https://cdn-icons-png.flaticon.com/512/2944/2944364.png', iconSize: [40, 40], iconAnchor: [20, 40] }),
        driver: L.icon({ iconUrl: 'https://cdn-icons-png.flaticon.com/512/3209/3209800.png', iconSize: [48, 48], iconAnchor: [24, 48], className: 'driver-marker-icon' })
    };

    function init(mapId, centerLat, centerLng) {
        const container = document.getElementById(mapId);
        if (container) container.innerHTML = ''; // Xóa sạch Spinner và Loading text ngay lập tức
        
        if (map) map.remove(); 
        map = L.map(mapId, { zoomControl: false }).setView([centerLat, centerLng], 14);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 19
        }).addTo(map);
    }

    function initCustomerMap(restLat, restLng, custLat, custLng) {
        L.marker([restLat, restLng], { icon: ICONS.restaurant }).addTo(map).bindPopup("<b>Nhà hàng</b>");
        L.marker([custLat, custLng], { icon: ICONS.customer }).addTo(map).bindPopup("<b>Bạn ở đây</b>");

        drawRoute(restLat, restLng, custLat, custLng, '#94a3b8');
        
        map.fitBounds([[restLat, restLng], [custLat, custLng]], { padding: [50, 50] });
    }

    function updateDriverPosition(lat, lng, phase, restLat, restLng, custLat, custLng) {
        if (!driverMarker) {
            driverMarker = L.marker([lat, lng], { icon: ICONS.driver, zIndexOffset: 1000 }).addTo(map);
        } else {
            driverMarker.setLatLng([lat, lng]);
        }

        if (routingControl) map.removeControl(routingControl);

        if (phase === 'GOING_TO_RESTAURANT' || phase === 'WAITING_AT_RESTAURANT' || phase === 'DRIVER_ACCEPTED') {
            drawRoute(lat, lng, restLat, restLng, '#3b82f6');
        } else if (phase === 'DELIVERING') {
            drawRoute(lat, lng, custLat, custLng, '#10b981');
        }
    }

    function drawRoute(startLat, startLng, endLat, endLng, color) {
        routingControl = L.Routing.control({
            waypoints: [ L.latLng(startLat, startLng), L.latLng(endLat, endLng) ],
            lineOptions: { styles: [{ color: color, weight: 5, opacity: 0.8 }] },
            createMarker: function() { return null; },
            fitSelectedRoutes: false,
            show: false,
            addWaypoints: false,
            routeWhileDragging: false
        }).addTo(map);
    }

    function focusOnDriver() {
        if (driverMarker) {
            map.flyTo(driverMarker.getLatLng(), 16, { animate: true, duration: 1.5 });
        }
    }

    return { init, initCustomerMap, updateDriverPosition, focusOnDriver };
})();
