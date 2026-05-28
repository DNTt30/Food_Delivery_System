import { useState } from "react";
import { ArrowRight, Send, UploadCloud, Server, Database, Smartphone, CheckSquare, Bell } from "lucide-react";

interface FlowStep {
  title: string;
  sender: string;
  receiver: string;
  action: string;
  apiRoute?: string;
  payload?: string;
  dbEffect?: string;
  description: string;
}

const FLOW_STEPS: FlowStep[] = [
  {
    title: "1. Tải ảnh lên Máy chủ",
    sender: "Khách hàng (Trình duyệt)",
    receiver: "Mã nguồn Upload Controller",
    action: "Gửi tệp ảnh (Multipart Form-Data)",
    apiRoute: "POST /api/upload/image",
    payload: "FormData { 'file': image.png }",
    description: "Khách hàng chọn tệp ảnh từ máy mình. Trình duyệt gửi tệp này qua API Upload chung của hệ thống trước khi nộp Đánh giá toàn bộ.",
  },
  {
    title: "2. Nhận URL ảnh tương đối",
    sender: "Máy chủ (Spring Boot)",
    receiver: "Khách hàng (Trình duyệt)",
    action: "Trả về đường dẫn tệp ảnh và lưu cục bộ trên máy chủ",
    payload: '{ "url": "/images/review-uuid-99a.jpg" }',
    description: "Máy chủ lưu tệp tin ảnh vào thư mục cấu hình và sinh ra một tên tệp ngẫu nhiên (UUID). Máy chủ phản hồi trạng thái 200 OK kèm URL tương đối để hiển thị preview dạng thu nhỏ.",
  },
  {
    title: "3. Gửi đánh giá toàn bộ đơn hàng",
    sender: "Khách hàng (Trình duyệt)",
    receiver: "Order & Review Controller",
    action: "Gửi JSON chứa rating, nhận xét và URL ảnh tương đối",
    apiRoute: "POST /api/customer/orders/{id}/review",
    payload: '{\n  "rating": 5,\n  "comment": "Đồ ăn ngon tuyệt vời!",\n  "imageUrl": "/images/review-uuid-99a.jpg"\n}',
    dbEffect: "INSERT INTO reviews (order_id, rating, comment, image_url) VALUES (2408, 5, '...', '/images/...')",
    description: "Khách hàng bấm 'Gửi đánh giá'. Trình duyệt gửi gói tin JSON bọc giá trị đánh giá sao, ý kiến và liên kết ảnh phía trên về cho OrderService xử lý, lưu mới một Entity Review liên kết tới Order.",
  },
  {
    title: "4. Nhà hàng truy cập & tải list review",
    sender: "Nhà hàng (Trình duyệt)",
    receiver: "Máy chủ (Spring Boot)",
    action: "Yêu cầu danh sách đánh giá của quán",
    apiRoute: "GET /api/restaurant/reviews",
    payload: '{\n  "status": "success",\n  "data": [ { "id": 19, "comment": "...", "imageUrl": "/images/..." } ]\n}',
    description: "Nhà hàng xem các phản hồi của khách hàng. Nếu Review có chứa 'imageUrl', Hệ thống sẽ hiển thị một khối ảnh thumbnail nhỏ bên dưới ý kiến phản hồi cho nhà hàng xem trực tiếp.",
  },
  {
    title: "5. Nhà hàng gửi phản hồi nhanh",
    sender: "Nhà hàng (Trình duyệt)",
    receiver: "Restaurant Management Controller",
    action: "Gửi phản hồi nhanh qua JSON Body",
    apiRoute: "POST /api/restaurant/reviews/19/reply",
    payload: '{\n  "reply": "Cảm ơn quý khách đã tin dùng đồ ăn của quán ạ!"\n}',
    dbEffect: "UPDATE reviews SET restaurant_reply = '...', replied_at = NOW() WHERE id = 19",
    description: "Nhà hàng nhập nội dung phản hồi trong ô Textarea và bấm Gửi. Trình duyệt gọi API tương ứng viết đè lưu trường phản hồi và lưu giờ phản hồi xuống bản ghi (Review Entity) trong CSDL MySQL.",
  },
  {
    title: "6. Kích hoạt thông báo cho khách",
    sender: "Máy chủ (Spring Boot)",
    receiver: "Hộp thư Khách hàng",
    action: "Gọi NotificationService lưu thông báo mới",
    dbEffect: "INSERT INTO notifications (user_id, title, content, type) VALUES (...)",
    payload: '"💬 Phản hồi đánh giá mới! Nhà hàng đã phản hồi đánh giá của bạn cho đơn #2408."',
    description: "Service kích hoạt NotificationService tự sinh thông báo liên kết cho tài khoản khách hàng. Hệ thống đẩy dòng thông báo ra chuông báo hiệu của người dùng (có thể kết nối WebSocket theo thời gian thực).",
  }
];

export default function FlowDiagram() {
  const [currentStep, setCurrentStep] = useState<number>(0);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl flex flex-col lg:flex-row gap-6 min-h-[500px]">
      
      {/* Left Pane: Interactive steps checklist & description */}
      <div className="w-full lg:w-[45%] flex flex-col justify-between">
        <div>
          <div className="flex items-center gap-2 mb-4">
            <span className="p-1 px-2.5 rounded-md bg-amber-950/80 border border-amber-800 text-amber-400 font-mono text-xs">FLOW</span>
            <h3 className="text-sm font-bold text-slate-200">Sơ đồ luồng dữ liệu (Data Flow)</h3>
          </div>
          <p className="text-xs text-slate-400 mb-4 leading-relaxed">
            Click qua từng bước bên dưới để gỡ lỗi và phân tích luồng di chuyển dữ liệu từ Client - Server - Database MySQL:
          </p>

          <div className="space-y-2">
            {FLOW_STEPS.map((step, idx) => (
              <button
                key={idx}
                onClick={() => setCurrentStep(idx)}
                className={`w-full text-left p-2.5 rounded-xl border transition-all flex items-center gap-3 ${
                  idx === currentStep
                    ? "bg-amber-950/20 border-amber-400/80 text-amber-400 font-medium"
                    : "bg-slate-950/30 border-slate-800 text-slate-400 hover:bg-slate-900/50 hover:text-slate-200"
                }`}
              >
                <div className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 ${
                  idx === currentStep ? "bg-amber-400 text-slate-950" : "bg-slate-800 text-slate-400"
                }`}>
                  {idx + 1}
                </div>
                <div className="text-xs font-semibold truncate">{step.title}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Step Guide Controller */}
        <div className="mt-6 pt-4 border-t border-slate-800 flex items-center justify-between">
          <button
            disabled={currentStep === 0}
            onClick={() => setCurrentStep(currentStep - 1)}
            className="text-xs bg-slate-800 hover:bg-slate-700 disabled:opacity-45 text-slate-300 font-medium px-3 py-2 rounded-lg transition"
          >
            Quay lại
          </button>
          <span className="text-xs text-slate-500 font-mono">
            Bước {currentStep + 1} / {FLOW_STEPS.length}
          </span>
          <button
            disabled={currentStep === FLOW_STEPS.length - 1}
            onClick={() => setCurrentStep(currentStep + 1)}
            className="text-xs bg-amber-400 text-slate-950 hover:bg-amber-500 disabled:opacity-45 font-bold px-3 py-2 rounded-lg transition flex items-center gap-1"
          >
            Tiếp theo <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Right Pane: Visual representation of the Step */}
      <div className="flex-1 bg-slate-950/60 rounded-2xl border border-slate-800 p-5 flex flex-col justify-between">
        <div>
          <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Chi tiết bước hiện tại</div>
          <h4 className="text-xs font-bold text-slate-100 flex items-center gap-1.5 mb-3">
            <span className="w-2 h-2 rounded-full bg-amber-400"></span>
            {FLOW_STEPS[currentStep].title}
          </h4>
          <p className="text-[12px] text-slate-300 leading-relaxed bg-slate-900/50 p-3 rounded-xl border border-slate-800/60 mb-4">
            {FLOW_STEPS[currentStep].description}
          </p>

          {/* Actor Source -> Target visual arrow */}
          <div className="bg-slate-900 border border-slate-800 p-3 rounded-xl mb-4 flex items-center justify-between">
            <div className="text-center p-2 bg-slate-950 rounded-lg w-[40%] border border-slate-800">
              <span className="text-[10px] text-slate-500 font-bold block mb-0.5">SENDER</span>
              <span className="text-xs text-amber-400 font-semibold truncate block">{FLOW_STEPS[currentStep].sender}</span>
            </div>
            <ArrowRight className="w-5 h-5 text-slate-500 animate-pulse shrink-0" />
            <div className="text-center p-2 bg-slate-950 rounded-lg w-[40%] border border-slate-800">
              <span className="text-[10px] text-slate-500 font-bold block mb-0.5">RECEIVER</span>
              <span className="text-xs text-indigo-400 font-semibold truncate block">{FLOW_STEPS[currentStep].receiver}</span>
            </div>
          </div>

          {/* Technical properties */}
          <div className="space-y-2 font-mono text-[11px]">
            {FLOW_STEPS[currentStep].apiRoute && (
              <div className="flex flex-col bg-slate-950 p-2 rounded-lg border border-slate-800/80">
                <span className="text-[9px] text-indigo-500 font-bold uppercase tracking-wider mb-0.5">API ENDPOINT:</span>
                <span className="text-indigo-300 font-bold">{FLOW_STEPS[currentStep].apiRoute}</span>
              </div>
            )}
            
            <div className="flex flex-col bg-slate-950 p-2 rounded-lg border border-slate-800/80">
              <span className="text-[9px] text-amber-500 font-bold uppercase tracking-wider mb-0.5">PAYLOAD / DỮ LIỆU CHUYỂN:</span>
              <pre className="text-slate-300 whitespace-pre scrollbar-none max-h-24 overflow-y-auto">
                {FLOW_STEPS[currentStep].payload}
              </pre>
            </div>

            {FLOW_STEPS[currentStep].dbEffect && (
              <div className="flex flex-col bg-slate-950 p-2 rounded-lg border border-emerald-950">
                <span className="text-[9px] text-emerald-500 font-bold uppercase tracking-wider mb-0.5">ẢNH HƯỞNG DATABASE (MYSQL):</span>
                <span className="text-emerald-400 font-semibold break-all">{FLOW_STEPS[currentStep].dbEffect}</span>
              </div>
            )}
          </div>
        </div>

        {/* Dynamic graphics */}
        <div className="mt-5 p-3.5 bg-slate-900 rounded-xl border border-dashed border-slate-800 text-center">
          <div className="flex justify-center gap-6 items-center">
            <div className={`p-2 rounded-xl transition ${currentStep < 3 ? "bg-amber-955 text-amber-400 border border-amber-900" : "bg-slate-950 text-slate-600"}`}>
              <Smartphone className="w-5 h-5 mx-auto" />
              <span className="text-[10px] block font-semibold mt-1">Khách hàng UI</span>
            </div>
            <div className={`w-3.5 h-[1px] border-t border-dashed ${currentStep < 3 ? "border-amber-700 font-bold" : "border-slate-800"}`} />
            
            <div className={`p-2 rounded-xl transition ${currentStep === 1 || currentStep === 2 || currentStep === 5 ? "bg-indigo-955 text-indigo-400 border border-indigo-900" : "bg-slate-950 text-slate-600"}`}>
              <Server className="w-5 h-5 mx-auto" />
              <span className="text-[10px] block font-semibold mt-1">Spring Boot</span>
            </div>
            <div className={`w-3.5 h-[1px] border-t border-dashed ${currentStep >= 2 ? "border-indigo-700" : "border-slate-800"}`} />

            <div className={`p-2 rounded-xl transition ${currentStep === 2 || currentStep === 4 || currentStep === 5 ? "bg-emerald-955 text-emerald-400 border border-emerald-900" : "bg-slate-950 text-slate-600"}`}>
              <Database className="w-5 h-5 mx-auto" />
              <span className="text-[10px] block font-semibold mt-1">MySQL DB</span>
            </div>
          </div>
        </div>

      </div>

    </div>
  );
}
