import { useState, useRef, useEffect, ChangeEvent } from "react";
import { Order, Review, Notification, OrderStatus } from "../types";
import { initialOrders } from "../data";
import { 
  Star, Image as ImageIcon, MessageSquare, Bell, Smartphone, User, Store, 
  ChevronRight, Calendar, ShoppingBag, Send, ThumbsUp, Trash2, Maximize2, X, PlusCircle, AlertCircle
} from "lucide-react";

// Preset food images to make testing quick and beautiful
const PRESET_PHOTOS = [
  { name: "Phở bò chín tái", url: "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500&auto=format&fit=crop&q=80" },
  { name: "Nem rán giòn", url: "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=500&auto=format&fit=crop&q=80" },
  { name: "Cơm tấm bì sườn", url: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=80" }
];

export default function InteractiveApp() {
  // Application State
  const [activeRole, setActiveRole] = useState<"CUSTOMER" | "RESTAURANT">("CUSTOMER");
  const [orders, setOrders] = useState<Order[]>(initialOrders);
  const [notifications, setNotifications] = useState<Notification[]>([
    {
      id: "notif_0",
      title: "🎁 Khuyến mãi hấp dẫn",
      content: "Nhận voucher giảm 20k cho đơn hàng Phở Hà Nội hôm nay!",
      type: "REPLY",
      orderId: "2408",
      isRead: false,
      createdAt: "2026-05-28T07:00:00Z"
    }
  ]);
  
  // Terminal logs
  const [logs, setLogs] = useState<string[]>([
    "⚙️ [System] Khởi tạo môi trường Food Delivery Simulator v1.0",
    "⚙️ [JPA] Hibernate ddl-auto=update đã đồng bộ: reviews (image_url, restaurant_reply, replied_at)",
    "👥 [Auth] Khách hàng logged in: customer_uuid_a983"
  ]);

  // Review Modal State
  const [isReviewOpen, setIsReviewOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [modalRating, setModalRating] = useState<number>(5);
  const [modalComment, setModalComment] = useState<string>("");
  const [modalImageUrl, setModalImageUrl] = useState<string>("");
  const [uploadProgress, setUploadProgress] = useState<string>("");
  const [isPreviewExpanded, setIsPreviewExpanded] = useState<string | null>(null);

  // View Review Modal State (Watch replied status)
  const [isViewReviewOpen, setIsViewReviewOpen] = useState(false);
  
  // Restaurant Reply Inline State
  const [replyInputs, setReplyInputs] = useState<Record<string, string>>({});
  const [expandedReviewId, setExpandedReviewId] = useState<string | null>(null);

  // Active testing guide step state
  const [testStep, setTestStep] = useState<number>(1);

  // Notification bell popover state
  const [showNotifPopover, setShowNotifPopover] = useState(false);

  // Helper to add system logs
  const pushLog = (msg: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs(prev => [`[${timestamp}] ${msg}`, ...prev]);
  };

  // Switch Role Helper
  const switchRole = (role: "CUSTOMER" | "RESTAURANT") => {
    setActiveRole(role);
    pushLog(`👥 [Auth] Chuyển đổi quyền người dùng sang: ${role === "CUSTOMER" ? "Khách hàng" : "Chủ Nhà hàng"}`);
    if (role === "CUSTOMER" && testStep === 1) {
      setTestStep(2);
    } else if (role === "RESTAURANT" && testStep === 2) {
      setTestStep(3);
    }
  };

  // Simulating image upload
  const handleUploadImageMock = (url: string) => {
    setUploadProgress("⏳ Đang gửi file lên /api/upload/image...");
    pushLog("🌐 [API] Gửi POST /api/upload/image (Multipart Form)");
    
    setTimeout(() => {
      setModalImageUrl(url);
      setUploadProgress("Tải ảnh thành công!");
      pushLog(`🌐 [API] 200 OK - Nhận URL ảnh: ${url}`);
    }, 800);
  };

  // Custom Local File Upload
  const handleLocalFileUpload = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setUploadProgress("⏳ Đang gửi file lên /api/upload/image...");
      pushLog(`🌐 [API] Gửi POST /api/upload/image (${file.name})`);
      
      // Simulate reading/uploading file
      const reader = new FileReader();
      reader.onload = (event) => {
        if (event.target?.result) {
          setTimeout(() => {
            setModalImageUrl(event.target!.result as string);
            setUploadProgress("Tải ảnh thành công!");
            pushLog("🌐 [API] 200 OK - Nhận URL ảnh dạng Base64");
          }, 850);
        }
      };
      reader.readAsDataURL(file);
    }
  };

  // Submitting review
  const submitReview = () => {
    if (!selectedOrder) return;
    if (modalComment.trim() === "") {
      alert("Vui lòng ghi nhận xét ý kiến của bạn!");
      return;
    }

    pushLog(`🌐 [API] Gửi POST /api/customer/orders/${selectedOrder.id}/review`);
    pushLog(`🗄️ [MySQL] Chèn bản ghi mới: rating=${modalRating}, comment="${modalComment}", image_url="${modalImageUrl || 'NULL'}"`);

    // Update state
    setOrders(prev => prev.map(o => {
      if (o.id === selectedOrder.id) {
        return {
          ...o,
          review: {
            id: `rev_${selectedOrder.id}`,
            orderId: selectedOrder.id,
            rating: modalRating,
            comment: modalComment,
            imageUrl: modalImageUrl || undefined
          }
        };
      }
      return o;
    }));

    setIsReviewOpen(false);
    pushLog("✅ [Client] Đã gửi nhận xét thành công!");
    
    // Auto progress test steps
    if (testStep === 2) {
      setTestStep(3);
    }
  };

  // Submitting restaurant reply
  const submitReply = (reviewId: string, orderId: string) => {
    const replyText = replyInputs[reviewId];
    if (!replyText || replyText.trim() === "") {
      alert("Vui lòng nhập nội dung trả lời khách!");
      return;
    }

    pushLog(`🌐 [API] Gửi POST /api/restaurant/reviews/${reviewId}/reply`);
    pushLog(`🗄️ [MySQL] UPDATE reviews SET restaurant_reply="${replyText}", replied_at=NOW() WHERE id=${reviewId}`);

    const replyTime = new Date().toISOString();

    // Update orders review
    setOrders(prev => prev.map(o => {
      if (o.id === orderId && o.review) {
        return {
          ...o,
          review: {
            ...o.review,
            restaurantReply: replyText,
            repliedAt: replyTime
          }
        };
      }
      return o;
    }));

    // Generate notification for Customer
    const newNotif: Notification = {
      id: `notif_${Date.now()}`,
      title: "💬 Phản hồi đánh giá mới!",
      content: `Nhà hàng đã phản hồi đánh giá của bạn cho đơn #${orderId}.`,
      type: "REPLY",
      orderId: orderId,
      isRead: false,
      createdAt: replyTime
    };

    setNotifications(prev => [newNotif, ...prev]);
    pushLog(`🔔 [WebSocket] Đăng tải thông báo thời gian thực đến Client topic: /topic/notifications`);
    
    // Reset reply text
    setReplyInputs(prev => ({ ...prev, [reviewId]: "" }));
    setExpandedReviewId(null);

    // Auto progress test steps
    if (testStep === 3) {
      setTestStep(4);
    }
  };

  // Read notifications
  const handleReadNotification = (notifId: string) => {
    setNotifications(prev => prev.map(n => n.id === notifId ? { ...n, isRead: true } : n));
    const matchedNotif = notifications.find(n => n.id === notifId);
    if (matchedNotif) {
      const order = orders.find(o => o.id === matchedNotif.orderId);
      if (order) {
        setSelectedOrder(order);
        setIsViewReviewOpen(true);
        pushLog(`👥 [Client] Nhấp xem thông tin phản hồi từ đơn hàng #${order.id}`);
      }
    }
    setShowNotifPopover(false);
  };

  const completedOrders = orders.filter(o => o.status === "COMPLETED");
  const unreadNotifCount = notifications.filter(n => !n.isRead).length;

  return (
    <div className="grid grid-cols-1 xl:grid-cols-12 gap-6 items-start">
      
      {/* 1. Testing Playbook & Logs (Left, 4 cols) */}
      <div className="xl:col-span-4 space-y-6">
        
        {/* Playbook checklist */}
        <div className="bg-slate-50 border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-100">
            <span className="p-1 px-2.5 rounded-md bg-amber-100 text-amber-800 font-bold text-xs font-mono">STEP-BY-STEP</span>
            <h3 className="text-sm font-extrabold text-slate-800">Sổ Tay Kiểm Thử (Testing Guide)</h3>
          </div>

          <div className="space-y-3">
            {/* Step 1 */}
            <div className={`p-3 rounded-xl border transition-all ${
              testStep === 1 
                ? "bg-amber-100/50 border-amber-300 text-slate-900" 
                : "bg-slate-55 border-transparent text-slate-500"
            }`}>
              <div className="flex items-start gap-2.5">
                <div className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 mt-0.5 ${
                  testStep > 1 ? "bg-emerald-500 text-white" : testStep === 1 ? "bg-amber-500 text-white" : "bg-slate-200 text-slate-400"
                }`}>
                  {testStep > 1 ? "✓" : "1"}
                </div>
                <div>
                  <h4 className="text-xs font-bold leading-tight">Bấm Đăng Nhập Khách Hàng</h4>
                  <p className="text-[11px] mt-1 leading-relaxed">
                    Chọn vai trò <strong>Khách hàng (Customer)</strong> để xem trang lịch sử /customer/history.
                  </p>
                </div>
              </div>
            </div>

            {/* Step 2 */}
            <div className={`p-3 rounded-xl border transition-all ${
              testStep === 2 
                ? "bg-amber-100/50 border-amber-300 text-slate-900" 
                : "bg-slate-55 border-transparent text-slate-500"
            }`}>
              <div className="flex items-start gap-2.5">
                <div className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 mt-0.5 ${
                  testStep > 2 ? "bg-emerald-500 text-white" : testStep === 2 ? "bg-amber-500 text-white" : "bg-slate-200 text-slate-400"
                }`}>
                  {testStep > 2 ? "✓" : "2"}
                </div>
                <div>
                  <h4 className="text-xs font-bold leading-tight">Đăng đánh giá kèm ảnh chụp</h4>
                  <p className="text-[11px] mt-1 leading-relaxed">
                    Tìm đơn hàng Phở (<strong>#2408</strong>), bấm <strong>Đánh giá</strong>. Viết ý kiến, bấm chọn ảnh mẫu/tải lên và chọn <strong>Gửi đánh giá</strong>.
                  </p>
                </div>
              </div>
            </div>

            {/* Step 3 */}
            <div className={`p-3 rounded-xl border transition-all ${
              testStep === 3 
                ? "bg-amber-100/50 border-amber-300 text-slate-900" 
                : "bg-slate-55 border-transparent text-slate-500"
            }`}>
              <div className="flex items-start gap-2.5">
                <div className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 mt-0.5 ${
                  testStep > 3 ? "bg-emerald-500 text-white" : testStep === 3 ? "bg-amber-500 text-white" : "bg-slate-200 text-slate-400"
                }`}>
                  {testStep > 3 ? "✓" : "3"}
                </div>
                <div>
                  <h4 className="text-xs font-bold leading-tight">Chuyển sang Nhà hàng & phản hồi</h4>
                  <p className="text-[11px] mt-1 leading-relaxed">
                    Click nút <strong>Chủ Nhà hàng</strong> phía trên điện thoại. Xem đánh giá kèm ảnh vừa đăng, bấm <strong>Phản hồi</strong> inline và gửi câu trả lời.
                  </p>
                </div>
              </div>
            </div>

            {/* Step 4 */}
            <div className={`p-3 rounded-xl border transition-all ${
              testStep === 4 
                ? "bg-amber-100/50 border-amber-300 text-slate-900" 
                : "bg-slate-55 border-transparent text-slate-500"
            }`}>
              <div className="flex items-start gap-2.5">
                <div className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 mt-0.5 ${
                  testStep === 4 ? "bg-amber-500 text-white animate-pulse" : "bg-slate-200 text-slate-400"
                }`}>
                  4
                </div>
                <div>
                  <h4 className="text-xs font-bold leading-tight">Xem Chuông và Nhận Phản Hồi</h4>
                  <p className="text-[11px] mt-1 leading-relaxed">
                    Đăng nhập lại <strong>Khách hàng</strong>. Click <strong>Chuông thông báo</strong> đỏ nhấp nháy, kiểm tra lịch sử xem khối thông báo màu vàng nhạt.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="text-center pt-2">
            <button 
              onClick={() => {
                setOrders(initialOrders);
                setNotifications([
                  {
                    id: "notif_0",
                    title: "🎁 Khuyến mãi hấp dẫn",
                    content: "Nhận voucher giảm 20k cho đơn hàng Phở Hà Nội hôm nay!",
                    type: "REPLY",
                    orderId: "2408",
                    isRead: false,
                    createdAt: "2026-05-28T07:00:00Z"
                  }
                ]);
                setTestStep(1);
                pushLog("🔄 [System] Khôi phục toàn bộ trạng thái dữ liệu mô phỏng về mặc định.");
              }}
              className="text-[11px] text-red-600 hover:text-red-700 font-semibold underline"
            >
              Đặt lại kịch bản kiểm thử (Reset)
            </button>
          </div>
        </div>

        {/* Live Code/DB System Logs Console */}
        <div className="bg-slate-950 rounded-2xl p-4.5 shadow-xl border border-slate-800 text-slate-300 flex flex-col h-[280px]">
          <div className="flex items-center justify-between pb-2 border-b border-slate-800 mb-3">
            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-400">
              <span className="w-2.5 h-2.5 rounded-full bg-red-500 block"></span>
              <span className="w-2.5 h-2.5 rounded-full bg-yellow-500 block"></span>
              <span className="w-2.5 h-2.5 rounded-full bg-green-500 block"></span>
              <span className="ml-2 font-mono text-xs">Terminal Logs</span>
            </div>
            <span className="text-[10px] text-slate-500 font-mono italic">Real-time</span>
          </div>

          <div className="flex-1 overflow-y-auto font-mono text-[10.5px] space-y-2 scrollbar-thin scrollbar-thumb-slate-800 pr-1 select-all">
            {logs.map((log, index) => (
              <div 
                key={index} 
                className={`py-0.5 leading-relaxed border-l-2 pl-2 ${
                  log.includes("MySQL") ? "border-emerald-600 text-emerald-400" :
                  log.includes("API") ? "border-indigo-600 text-indigo-400" :
                  log.includes("System") ? "border-slate-700 text-slate-400" :
                  "border-amber-600 text-amber-400"
                }`}
              >
                {log}
              </div>
            ))}
          </div>
        </div>

      </div>

      {/* 2. Phone Screen Simulator (Right, 8 cols) */}
      <div className="xl:col-span-8 flex flex-col items-center">
        
        {/* Toggle Roles Switcher on Phone Border top */}
        <div className="flex items-center gap-4 bg-slate-100 p-2.5 rounded-2xl border border-slate-200 shadow-inner mb-4 w-full max-w-sm">
          <button
            onClick={() => switchRole("CUSTOMER")}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-xl transition-all font-semibold text-xs ${
              activeRole === "CUSTOMER"
                ? "bg-white shadow-sm text-slate-900 border border-slate-200"
                : "text-slate-500 hover:text-slate-700"
            }`}
          >
            <User className="w-4 h-4 text-amber-500" />
            Khách hàng (Customer)
          </button>
          <button
            onClick={() => switchRole("RESTAURANT")}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-xl transition-all font-semibold text-xs ${
              activeRole === "RESTAURANT"
                ? "bg-white shadow-sm text-slate-900 border border-slate-200"
                : "text-slate-500 hover:text-slate-700"
            }`}
          >
            <Store className="w-4 h-4 text-indigo-500" />
            Nhà hàng (Quán ăn)
          </button>
        </div>

        {/* Gorgeous Phone Shell Simulator Container */}
        <div className="relative w-full max-w-sm bg-slate-950 p-4 rounded-[42px] border-4 border-slate-900 shadow-2xl h-[610px] flex flex-col">
          {/* Top Speaker & Camera punch */}
          <div className="absolute top-2 left-1/2 -translate-x-1/2 h-4 w-28 bg-slate-900 rounded-full flex justify-center items-center gap-1.5 z-20">
            <span className="w-2.5 h-1 bg-slate-800 rounded-full"></span>
            <span className="w-1.5 h-1.5 bg-slate-800 rounded-full"></span>
          </div>

          {/* App inside screen */}
          <div className="flex-1 bg-white rounded-[28px] overflow-hidden flex flex-col text-slate-800 relative select-none">
            
            {/* 2A. CUSTOMER APPPLET APP SCREEN */}
            {activeRole === "CUSTOMER" && (
              <div className="flex-1 flex flex-col bg-slate-50">
                {/* Header navbar space */}
                <div className="bg-slate-900 text-white p-3 pt-6 flex items-center justify-between shrink-0">
                  <div className="flex items-center gap-1">
                    <ShoppingBag className="w-4 h-4 text-amber-400" />
                    <span className="text-xs font-black tracking-tight select-none">FoodNow!</span>
                  </div>
                  
                  {/* Notifications bell icon inside navigation */}
                  <div className="relative">
                    <button 
                      onClick={() => setShowNotifPopover(!showNotifPopover)}
                      className="p-1 text-slate-300 hover:text-white rounded-full transition outline-none relative"
                    >
                      <Bell className={`w-4 h-4 ${unreadNotifCount > 0 ? "animate-bounce text-amber-400" : ""}`} />
                      {unreadNotifCount > 0 && (
                        <span className="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full animate-ping"></span>
                      )}
                      {unreadNotifCount > 0 && (
                        <span className="absolute -top-1 -right-1 w-3.5 h-3.5 bg-red-600 text-[8px] text-white font-extrabold flex items-center justify-center rounded-full">
                          {unreadNotifCount}
                        </span>
                      )}
                    </button>

                    {/* Notification Dropdown Popover */}
                    {showNotifPopover && (
                      <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-xl border border-slate-200 z-50 text-slate-800 overflow-hidden">
                        <div className="p-3 bg-slate-50 border-b border-slate-150 font-bold text-xs flex justify-between">
                          <span>Thông báo của tôi</span>
                          <button onClick={() => setShowNotifPopover(false)} className="text-[10px] text-slate-400 hover:text-slate-500">Đóng</button>
                        </div>
                        <div className="max-h-56 overflow-y-auto divide-y divide-slate-100">
                          {notifications.length === 0 ? (
                            <p className="p-4 text-xs text-center text-slate-400 font-semibold">Chưa có thông báo mới</p>
                          ) : (
                            notifications.map((notif) => (
                              <div 
                                key={notif.id} 
                                onClick={() => handleReadNotification(notif.id)}
                                className={`p-3 text-xs cursor-pointer hover:bg-slate-50 transition ${!notif.isRead ? "bg-amber-50/50" : ""}`}
                              >
                                <h5 className="font-bold flex items-center gap-1 text-slate-900">
                                  {!notif.isRead && <span className="w-1.5 h-1.5 rounded-full bg-red-500"></span>}
                                  {notif.title}
                                </h5>
                                <p className="text-[11px] text-slate-600 mt-1 leading-relaxed">{notif.content}</p>
                              </div>
                            ))
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                {/* Content body customer/history.html list */}
                <div className="flex-1 overflow-y-auto p-3 space-y-3">
                  <div className="flex items-center justify-between">
                    <h3 className="text-xs font-extrabold text-slate-800 uppercase tracking-wider">Lịch sử đơn hàng</h3>
                    <span className="text-[10px] bg-slate-200 px-2 py-0.5 rounded text-slate-600 font-semibold font-mono">/customer/history</span>
                  </div>

                  {orders.map((o) => (
                    <div key={o.id} className="bg-white rounded-2xl border border-slate-200/80 p-3 shadow-xs space-y-2.5">
                      {/* Name & status */}
                      <div className="flex items-center justify-between pb-2 border-b border-slate-100">
                        <div>
                          <h4 className="text-xs font-bold text-slate-900 truncate max-w-[170px]">{o.restaurantName}</h4>
                          <span className="text-[10px] text-slate-400 font-mono">#{o.id} • {new Date(o.createdAt).toLocaleDateString()}</span>
                        </div>
                        <span className={`text-[9px] font-extrabold px-2 py-0.5 rounded-full ${
                          o.status === "COMPLETED" 
                            ? "bg-emerald-50 text-emerald-700 border border-emerald-200/65" 
                            : o.status === "PREPARING"
                            ? "bg-amber-50 text-amber-700 border border-amber-200"
                            : "bg-slate-100 text-slate-600"
                        }`}>
                          {o.status === "COMPLETED" ? "ĐÃ GIAO" : "ĐANG CHUẨN BỊ"}
                        </span>
                      </div>

                      {/* Items */}
                      <div className="space-y-1.5">
                        {o.items.map((item) => (
                          <div key={item.id} className="flex gap-2 items-center text-xs text-slate-600">
                            <img src={item.imageUrl} alt={item.foodName} className="w-8 h-8 rounded-lg object-cover" referrerPolicy="no-referrer" />
                            <div className="flex-1">
                              <p className="font-bold text-slate-800 text-[11px] truncate max-w-[130px]">{item.foodName}</p>
                              <p className="text-[10px] text-slate-400">SL: {item.quantity}</p>
                            </div>
                            <span className="font-mono text-slate-700 font-bold">{(item.price * item.quantity).toLocaleString()}đ</span>
                          </div>
                        ))}
                      </div>

                      {/* Total and review key CTA */}
                      <div className="pt-2 border-t border-slate-100 flex items-center justify-between bg-slate-50/50 -mx-3 -mb-3 p-3 rounded-b-2xl">
                        <div>
                          <span className="text-[10px] text-slate-500 block">Tổng thanh toán</span>
                          <span className="font-mono text-xs font-black text-slate-800">{o.totalPrice.toLocaleString()}đ</span>
                        </div>

                        {o.status === "COMPLETED" ? (
                          !o.review ? (
                            <button
                              onClick={() => {
                                setSelectedOrder(o);
                                setModalRating(5);
                                setModalComment("");
                                setModalImageUrl("");
                                setUploadProgress("");
                                setIsReviewOpen(true);
                              }}
                              className="bg-amber-500 hover:bg-amber-600 text-slate-950 font-extrabold text-[11px] px-3.5 py-1.5 rounded-xl shadow-xs transition"
                            >
                              Đánh giá ⭐
                            </button>
                          ) : (
                            <button
                              onClick={() => {
                                setSelectedOrder(o);
                                setIsViewReviewOpen(true);
                              }}
                              className="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-[11px] px-3.5 py-1.5 rounded-xl border border-slate-200 transition"
                            >
                              Xem nhận xét
                            </button>
                          )
                        ) : (
                          <span className="text-[10px] text-slate-400 font-semibold italic">Đang làm món...</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* 2B. RESTAURANT APP SCREEN */}
            {activeRole === "RESTAURANT" && (
              <div className="flex-1 flex flex-col bg-slate-50">
                {/* Header navbar space */}
                <div className="bg-indigo-950 text-white p-3 pt-6 flex items-center justify-between shrink-0">
                  <div className="flex items-center gap-1.5">
                    <Store className="w-4 h-4 text-indigo-400" />
                    <span className="text-xs font-black tracking-tight">Đối Tác Quán Ăn</span>
                  </div>
                  <span className="text-[10px] bg-indigo-900 px-2 py-0.5 rounded text-indigo-200 font-mono">restaurant/reviews</span>
                </div>

                {/* Content body restaurant/reviews.html */}
                <div className="flex-1 overflow-y-auto p-3 space-y-3">
                  <h3 className="text-xs font-extrabold text-slate-800 uppercase tracking-wider">Quản lý nhận xét của khách</h3>

                  {orders.filter(o => o.review).length === 0 ? (
                    <div className="text-center py-10 bg-white rounded-2xl border border-dashed border-slate-200 p-5 text-slate-400">
                      <AlertCircle className="w-8 h-8 text-slate-300 mx-auto mb-2" />
                      <p className="text-xs font-bold">Chưa có đánh giá nào từ khách hàng</p>
                      <p className="text-[10px] text-slate-400 mt-1">Quay lại vai trò Khách hàng để viết đánh giá cho quán!</p>
                    </div>
                  ) : (
                    orders.filter(o => o.review).map((o) => {
                      const rev = o.review!;
                      const isExpanded = expandedReviewId === rev.id;
                      
                      return (
                        <div key={rev.id} className="bg-white rounded-2xl border border-slate-200 p-3 shadow-xs space-y-2.5">
                          {/* Title Stars & Customer ID */}
                          <div className="flex justify-between items-center pb-2 border-b border-slate-100">
                            <div>
                              <span className="text-[10px] text-slate-400 block font-mono">Đơn #{o.id} • Customer</span>
                              <div className="flex text-amber-400 mt-0.5">
                                {[...Array(5)].map((_, i) => (
                                  <Star key={i} className={`w-3.5 h-3.5 ${i < rev.rating ? "fill-amber-400 text-amber-500" : "text-slate-200"}`} />
                                ))}
                              </div>
                            </div>
                            <span className="text-[9px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded font-bold font-mono">MySQL ID: {rev.id}</span>
                          </div>

                          {/* Comment Content */}
                          <div>
                            <p className="text-xs text-slate-800 leading-relaxed italic">"{rev.comment}"</p>
                          </div>

                          {/* INCLUDED REVIEW PHOTO WITH CLICK FULLSCREEN ZOOM (NEW FEATURE) */}
                          {rev.imageUrl && (
                            <div className="mt-1">
                              <div className="text-[10px] text-slate-400 mb-1 font-semibold flex items-center gap-1">
                                <ImageIcon className="w-3 h-3 text-indigo-400" /> Image Uploaded:
                              </div>
                              <div className="relative group w-20 h-20 rounded-xl overflow-hidden border border-slate-200 cursor-zoom-in">
                                <img
                                  src={rev.imageUrl}
                                  alt="Review customer"
                                  className="w-full h-full object-cover group-hover:scale-105 transition"
                                  onClick={() => setIsPreviewExpanded(rev.imageUrl || null)}
                                  referrerPolicy="no-referrer"
                                />
                                <div 
                                  onClick={() => setIsPreviewExpanded(rev.imageUrl || null)}
                                  className="absolute inset-0 bg-black/25 opacity-0 group-hover:opacity-100 transition flex items-center justify-center text-white"
                                >
                                  <Maximize2 className="w-3.5 h-3.5" />
                                </div>
                              </div>
                            </div>
                          )}

                          {/* RESTAURANT CHỦ QUÁN PHẢN HỒI (NEW INLINE REPLY PORTAL) */}
                          {rev.restaurantReply ? (
                            <div className="bg-amber-50 border border-amber-200 p-2.5 rounded-xl text-[11px] leading-relaxed">
                              <p className="font-bold text-amber-800 text-[10px] uppercase mb-0.5">Phản hồi của Chủ Quán:</p>
                              <p className="text-slate-700 italic">"{rev.restaurantReply}"</p>
                              <span className="text-[9px] text-slate-400 font-mono mt-1 block">⏱ {new Date(rev.repliedAt!).toLocaleTimeString()} - {new Date(rev.repliedAt!).toLocaleDateString()}</span>
                            </div>
                          ) : (
                            <div className="pt-2">
                              {!isExpanded ? (
                                <button
                                  onClick={() => setExpandedReviewId(rev.id)}
                                  className="text-xs border border-indigo-600 hover:bg-indigo-50 text-indigo-600 font-bold px-3 py-1.5 rounded-xl transition flex items-center gap-1"
                                >
                                  <MessageSquare className="w-3.5 h-3.5" /> Phản hồi khách
                                </button>
                              ) : (
                                <div className="space-y-2 border border-indigo-100 bg-slate-50/55 p-2 rounded-xl">
                                  <textarea
                                    value={replyInputs[rev.id] || ""}
                                    onChange={(e) => setReplyInputs(prev => ({ ...prev, [rev.id]: e.target.value }))}
                                    placeholder="Viết câu trả lời của bạn, ví dụ: 'Cảm ơn quý khách đã tin cậy...'"
                                    className="w-full text-xs p-2 rounded-lg border border-slate-200 focus:border-indigo-400 focus:outline-none bg-white font-sans text-slate-800"
                                    rows={2.5}
                                  />
                                  <div className="flex justify-end gap-1.5 pt-1">
                                    <button
                                      onClick={() => setExpandedReviewId(null)}
                                      className="text-[10px] text-slate-500 font-bold hover:bg-slate-100 px-2 py-1 rounded"
                                    >
                                      Hủy
                                    </button>
                                    <button
                                      onClick={() => submitReply(rev.id, o.id)}
                                      className="text-[10px] bg-indigo-600 hover:bg-indigo-700 text-white font-black px-3 py-1 rounded shadow-xs"
                                    >
                                      Gửi trả lời
                                    </button>
                                  </div>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            )}

            {/* Simulated notification bell indicator absolute tooltip */}
            {testStep === 4 && activeRole === "CUSTOMER" && (
              <div className="absolute top-12 right-2 bg-red-600 text-white rounded-xl p-2 shadow-lg max-w-[200px] z-40 text-[10px] animate-bounce">
                <p className="font-bold">⚠️ Có phản hồi mới!</p>
                <p className="text-[9px] text-red-100">Bấm chuông đỏ để xem câu trả lời của quán ăn.</p>
              </div>
            )}

          </div>
        </div>

        {/* 2C. MODAL POPUPS INSIDE SIMULATION SHELL */}
        
        {/* CUSTOMER GỬI ĐÁNH GIÁ MODAL */}
        {isReviewOpen && selectedOrder && (
          <div className="fixed inset-0 bg-slate-900/60 z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl w-full max-w-sm p-4.5 border border-slate-200 shadow-2xl space-y-4">
              <div className="flex justify-between items-center pb-2 border-b border-slate-100">
                <h3 className="text-sm font-black text-slate-800">Đánh giá Đơn hàng</h3>
                <button onClick={() => setIsReviewOpen(false)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div>
                <p className="text-[11px] text-slate-400 font-mono mb-1">Mã đơn hàng: #{selectedOrder.id}</p>
                <p className="text-xs text-slate-800 font-extrabold">{selectedOrder.restaurantName}</p>
              </div>

              {/* Stars ratings */}
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">1. Số sao nhận xét</label>
                <div className="flex text-amber-400 gap-1 pt-0.5">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      onClick={() => setModalRating(star)}
                      className="hover:scale-110 transition"
                    >
                      <Star className={`w-6 h-6 ${star <= modalRating ? "fill-amber-400 text-amber-500" : "text-slate-200"}`} />
                    </button>
                  ))}
                </div>
              </div>

              {/* Text comment */}
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">2. Bình luận ý kiến</label>
                <textarea
                  value={modalComment}
                  onChange={(e) => setModalComment(e.target.value)}
                  placeholder="Hương vị món thế nào? Shop đóng gói và giao hàng có tốc độ ra sao..."
                  className="w-full text-xs p-2.5 rounded-xl border border-slate-200 focus:border-amber-400 focus:outline-none"
                  rows={2}
                />
              </div>

              {/* IMAGE UPLOAD W/ PRESETS OR LOCAL FILE (NEW REQUIRED FEATURE) */}
              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 block">3. Đính kèm ảnh (Tải ảnh lên)</label>
                
                {/* Method 1: Selector Presets */}
                <span className="text-[10px] text-slate-400 font-semibold block">Cách 1: Chọn ảnh mẫu thức ăn ngon chuẩn bị sẵn:</span>
                <div className="flex gap-2">
                  {PRESET_PHOTOS.map((p, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => handleUploadImageMock(p.url)}
                      className="bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-lg p-1 text-center flex-1 text-[9px] font-bold text-slate-700 truncate"
                    >
                      {p.name}
                    </button>
                  ))}
                </div>

                {/* Method 2: Real Local File Selector */}
                <div className="pt-1.5">
                  <span className="text-[10px] text-slate-400 font-semibold block mb-1">Cách 2: Chọn tập tin ảnh từ thiết bị của bạn:</span>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleLocalFileUpload}
                    className="block w-full text-[10px] text-slate-500
                      file:mr-2 file:py-1 file:px-2
                      file:rounded-full file:border-0
                      file:text-[10px] file:font-semibold
                      file:bg-indigo-50 file:text-indigo-700
                      hover:file:bg-indigo-100"
                  />
                </div>

                {/* Simulated feedback progress line */}
                {uploadProgress && (
                  <div className="flex items-center gap-1.5 text-[10px] text-slate-500 font-mono mt-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 block animate-ping"></span>
                    <span>{uploadProgress}</span>
                  </div>
                )}

                {modalImageUrl && (
                  <div className="border border-slate-200 p-1 bg-slate-50 rounded-xl max-w-[120px] mx-auto text-center mt-2 relative">
                    <img src={modalImageUrl} alt="Review upload preview" className="w-20 h-20 rounded-lg object-cover mx-auto" referrerPolicy="no-referrer" />
                    <button 
                      onClick={() => {
                        setModalImageUrl("");
                        setUploadProgress("");
                      }}
                      className="absolute -top-1.5 -right-1.5 p-0.5 bg-red-100 text-red-600 rounded-full hover:bg-red-200"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                )}
              </div>

              {/* Submit Review Form Action */}
              <div className="pt-2 border-t border-slate-100 flex gap-2">
                <button
                  onClick={() => setIsReviewOpen(false)}
                  className="flex-1 py-1.5 border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold rounded-xl text-xs"
                >
                  Bỏ qua
                </button>
                <button
                  onClick={submitReview}
                  className="flex-1 py-1.5 bg-amber-400 hover:bg-amber-500 font-black text-slate-900 rounded-xl text-xs shadow-xs"
                >
                  Gửi đánh giá
                </button>
              </div>

            </div>
          </div>
        )}

        {/* CUSTOMER XEM ĐÁNH GIÁ (VIEW MODAL WITH GOLDEN COLOR REPLY HIGHLIGHT) */}
        {isViewReviewOpen && selectedOrder && selectedOrder.review && (
          <div className="fixed inset-0 bg-slate-900/60 z-50 flex items-center justify-center p-4 overflow-y-auto">
            <div className="bg-white rounded-2xl w-full max-w-sm p-4.5 border border-slate-200 shadow-2xl space-y-4">
              <div className="flex justify-between items-center pb-2 border-b border-slate-100">
                <h3 className="text-sm font-black text-slate-800">Thông tin phản hồi</h3>
                <button onClick={() => setIsViewReviewOpen(false)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div>
                <p className="text-[11px] text-slate-400 font-mono">Đơn hàng: #{selectedOrder.id}</p>
                <p className="text-xs font-extrabold text-slate-900">{selectedOrder.restaurantName}</p>
              </div>

              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-1">Mức điểm và Ý kiến:</span>
                <div className="flex text-amber-400 mb-1">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} className={`w-4 h-4 ${i < selectedOrder.review!.rating ? "fill-amber-400 text-amber-500" : "text-slate-200"}`} />
                  ))}
                </div>
                <p className="text-slate-800 italic text-xs bg-slate-50 p-2.5 rounded-xl border border-slate-100 font-serif">
                  "{selectedOrder.review.comment}"
                </p>
              </div>

              {selectedOrder.review.imageUrl && (
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-1">Ảnh thực tế đính kèm:</span>
                  <img
                    src={selectedOrder.review.imageUrl}
                    alt="Customer review uploaded file"
                    className="w-28 h-28 object-cover rounded-xl border border-slate-200 hover:scale-105 transition duration-300"
                    referrerPolicy="no-referrer"
                  />
                </div>
              )}

              {/* YELLOW HIGHLIGHTED RESPONSE PANEL (CRITICAL REQUIRED FRONTEND LAYOUT) */}
              {selectedOrder.review.restaurantReply ? (
                <div className="bg-amber-50 border-2 border-amber-300 p-3 rounded-xl shadow-inner space-y-1">
                  <span className="text-[10px] font-black text-amber-800 uppercase tracking-wider block">
                    💬 Phản hồi từ Nhà hàng:
                  </span>
                  <p className="text-xs text-slate-700 italic leading-relaxed font-serif">
                    "{selectedOrder.review.restaurantReply}"
                  </p>
                  <div className="pt-1.5 flex items-center justify-between text-[9px] text-slate-400 font-mono">
                    <span>⏱ Thời gian phản hồi:</span>
                    <span>{new Date(selectedOrder.review.repliedAt!).toLocaleTimeString()} - {new Date(selectedOrder.review.repliedAt!).toLocaleDateString()}</span>
                  </div>
                </div>
              ) : (
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 text-center text-xs text-slate-400">
                  <p className="font-bold">Chưa có phản hồi từ nhà hàng...</p>
                  <p className="text-[10.5px] text-slate-400 mt-1">Đổi vai trò sang "Chủ Nhà hàng" để viết phản hồi trả lời khách hàng ngay!</p>
                </div>
              )}

              <div className="pt-2 border-t border-slate-100 flex">
                <button
                  onClick={() => setIsViewReviewOpen(false)}
                  className="flex-1 py-1.5 bg-slate-900 hover:bg-slate-800 text-white font-extrabold rounded-xl text-xs shadow-xs"
                >
                  Xác nhận
                </button>
              </div>

            </div>
          </div>
        )}

        {/* EXPANDED IMAGE OVERLAY VIEW PORTAL (MỞ TAB MỚI HOẶC PHÓNG TO) */}
        {isPreviewExpanded && (
          <div className="fixed inset-0 bg-black/85 z-55 flex items-center justify-center p-4" onClick={() => setIsPreviewExpanded(null)}>
            <div className="relative max-w-lg w-full max-h-[85vh] flex flex-col justify-center bg-slate-900 rounded-3xl overflow-hidden p-2" onClick={(e) => e.stopPropagation()}>
              <button 
                onClick={() => setIsPreviewExpanded(null)}
                className="absolute top-4 right-4 p-2 bg-black/50 hover:bg-black/80 text-white rounded-full transition duration-200 outline-none"
              >
                <X className="w-5 h-5" />
              </button>
              <img 
                src={isPreviewExpanded} 
                alt="Expanded review customer" 
                className="w-full h-auto max-h-[75vh] object-contain rounded-2xl" 
                referrerPolicy="no-referrer"
              />
              <div className="p-3 text-center bg-slate-950 text-slate-300 font-mono text-xs">
                <span>Trình xem ảnh full-size (Đường dẫn lưu tại DB: {isPreviewExpanded.substring(0, 48)}...)</span>
              </div>
            </div>
          </div>
        )}

      </div>

    </div>
  );
}
