import { useState } from "react";
import InteractiveApp from "./components/InteractiveApp";
import FlowDiagram from "./components/FlowDiagram";
import CodeExplorer from "./components/CodeExplorer";
import { BookOpen, RefreshCw, Send, ShieldAlert, Cpu, Heart, CheckCircle2 } from "lucide-react";

export default function App() {
  const [activeSegment, setActiveSegment] = useState<"simulator" | "flow" | "code">("simulator");

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans antialiased pb-12 selection:bg-amber-100 selection:text-amber-900">
      
      {/* 1. Header Banner containing branding and team info */}
      <header className="bg-white border-b border-slate-205/85 shadow-xs sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 py-4 md:py-5 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          
          {/* Title & Brand */}
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="p-1 px-2.5 rounded-full bg-amber-500 text-slate-950 font-black text-[10px] tracking-widest uppercase">
                Food Delivery System
              </span>
              <span className="text-[11px] text-slate-400 font-mono">Module 4</span>
            </div>
            <h1 className="text-xl md:text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
              <BookOpen className="w-6 h-6 text-amber-500 shrink-0" />
              Upload Ảnh Đánh Giá & Nhà Hàng Phản Hồi
            </h1>
            <p className="text-xs text-slate-500 font-medium">
              Chương trình hướng dẫn chi tiết, sơ đồ luồng hoạt động & môi trường giả lập kỹ thuật (Testing Simulator)
            </p>
          </div>

          {/* Authors Credits Frame */}
          <div className="bg-slate-50 rounded-xl p-3 border border-slate-200 text-xs flex items-center gap-3">
            <div className="text-center border-r border-slate-200 pr-3">
              <span className="text-[9px] uppercase font-bold text-slate-400 block tracking-wider">Lập trình Backend</span>
              <span className="font-bold text-slate-800">Ngô Minh Quân</span>
            </div>
            <div className="text-center pr-3 border-r border-slate-200">
              <span className="text-[9px] uppercase font-bold text-slate-400 block tracking-wider">Thiết kế Frontend</span>
              <span className="font-bold text-slate-800">Đinh Thị Như Quỳnh</span>
            </div>
            <div className="text-center">
              <span className="text-[9px] uppercase font-bold text-slate-400 block tracking-wider">Hỗ trợ</span>
              <span className="font-bold text-slate-800">Dương Ngọc Tú</span>
            </div>
          </div>

        </div>
      </header>

      {/* 2. Main Page Scope Navigation Tabs */}
      <main className="max-w-7xl mx-auto px-4 mt-6">
        
        {/* Navigation Selector segments */}
        <div className="flex bg-slate-200 p-1 rounded-xl max-w-lg mb-6 shadow-inner">
          <button
            onClick={() => setActiveSegment("simulator")}
            className={`flex-1 py-2.5 text-center rounded-lg text-xs font-black transition-all ${
              activeSegment === "simulator"
                ? "bg-white text-slate-900 shadow-sm"
                : "text-slate-650 hover:text-slate-900"
            }`}
          >
            🕹️ Trình mô phỏng (Simulator)
          </button>
          <button
            onClick={() => setActiveSegment("flow")}
            className={`flex-1 py-2.5 text-center rounded-lg text-xs font-black transition-all ${
              activeSegment === "flow"
                ? "bg-white text-slate-900 shadow-sm"
                : "text-slate-650 hover:text-slate-900"
            }`}
          >
            🗺️ Sơ đồ luồng (Flow of Data)
          </button>
          <button
            onClick={() => setActiveSegment("code")}
            className={`flex-1 py-2.5 text-center rounded-lg text-xs font-black transition-all ${
              activeSegment === "code"
                ? "bg-white text-slate-900 shadow-sm"
                : "text-slate-650 hover:text-slate-900"
            }`}
          >
            📂 Tài liệu & Mã nguồn (Code)
          </button>
        </div>

        {/* 3. Render Segments */}
        <div className="transition-all duration-300">
          {activeSegment === "simulator" && (
            <div className="space-y-4">
              <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-3xs">
                <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wide flex items-center gap-1.5 mb-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                  Mô phỏng Giao dịch Toàn diện (End-to-End Simulation)
                </h2>
                <p className="text-xs text-slate-500 leading-relaxed max-w-4xl">
                  Bấm thử nghiệm quy trình <strong>Đăng Đánh giá kèm Ảnh chụp thực phẩm</strong> từ phía Khách hàng, ghi lại lịch sử SQL, sau đó đăng nhập phía <strong>Đối tác Nhà hàng</strong> phản hồi nhanh và theo dõi luồng thông báo WebSocket thời gian thực tự động đồng bộ.
                </p>
              </div>
              <InteractiveApp />
            </div>
          )}

          {activeSegment === "flow" && (
            <div className="space-y-4">
              <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-3xs">
                <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wide flex items-center gap-1.5 mb-1.5">
                  <Cpu className="w-4 h-4 text-amber-500 animate-pulse" />
                  Sơ đồ xử lý dữ liệu và Endpoint tuần tự
                </h2>
                <p className="text-xs text-slate-500 leading-relaxed max-w-4xl">
                  Mô hình hóa chi tiết di chuyển của luồng Binary File và các bản ghi JSON trung chuyển từ Client (Thymeleaf/Javascript) qua Spring Boot Controllers đến cơ sở dữ liệu MySQL thông qua JPA / Hibernate.
                </p>
              </div>
              <FlowDiagram />
            </div>
          )}

          {activeSegment === "code" && (
            <div className="space-y-4">
              <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-3xs">
                <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wide flex items-center gap-1.5 mb-1.5">
                  <BookOpen className="w-4 h-4 text-indigo-500" />
                  Bản vẽ Cấu trúc Project & File hệ thống
                </h2>
                <p className="text-xs text-slate-500 leading-relaxed max-w-4xl">
                  Tra cứu chi tiết các đoạn mã nguồn bổ sung do bộ phận <strong>Quản trị Cơ sở dữ liệu Ngô Minh Quân</strong> và <strong>Thiết kế Giao diện Đinh Thị Như Quỳnh</strong> triển khai cho thực thể, tầng Service Logic, Controller điều hướng, và giao diện HTML.
                </p>
              </div>
              <CodeExplorer />
            </div>
          )}
        </div>

      </main>

      {/* 4. Footnote Footer explanation */}
      <footer className="max-w-7xl mx-auto px-4 mt-12 pt-6 border-t border-slate-200 flex flex-col sm:flex-row justify-between items-center gap-4 text-xs text-slate-400">
        <div className="flex items-center gap-1.5 font-medium">
          <span>Project: Online Food Delivery Management System</span>
          <span className="w-1 h-1 rounded-full bg-slate-300"></span>
          <span>Báo cáo kiểm duyệt 2026</span>
        </div>
        <div className="flex items-center gap-1">
          <span>Phát triển bằng tình yêu</span>
          <Heart className="w-3 h-3 text-red-500 fill-red-500" />
          <span>bởi Đội ngũ Kỹ thuật Team</span>
        </div>
      </footer>

    </div>
  );
}
