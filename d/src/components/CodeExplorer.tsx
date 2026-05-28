import { useState } from "react";
import { codeSnippets, dbSchemaDetails } from "../data";
import { FileCode, Database, Code, Shield, HelpCircle, Layers, CheckCircle } from "lucide-react";

export default function CodeExplorer() {
  const [activeTab, setActiveTab] = useState<"database" | "service" | "controller" | "frontend">("database");
  const [activeSubIndex, setActiveSubIndex] = useState<number>(0);
  const [copied, setCopied] = useState<boolean>(false);

  const snippets = codeSnippets[activeTab] || [];
  const activeSnippet = snippets[activeSubIndex] || null;

  const handleCopy = () => {
    if (activeSnippet) {
      navigator.clipboard.writeText(activeSnippet.code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="bg-slate-900 text-slate-100 rounded-2xl border border-slate-800 shadow-xl overflow-hidden flex flex-col h-[650px] md:h-[700px]">
      {/* Header Tabs */}
      <div className="bg-slate-950 border-b border-slate-800 px-4 pt-4 flex flex-wrap gap-2">
        <button
          onClick={() => { setActiveTab("database"); setActiveSubIndex(0); }}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-t-lg text-sm font-medium transition-all ${
            activeTab === "database"
              ? "bg-slate-900 text-amber-400 border-t-2 border-amber-400"
              : "text-slate-400 hover:text-slate-200"
          }`}
          id="tab-db"
        >
          <Database className="w-4 h-4" />
          A. Database & JPA Entity
        </button>
        <button
          onClick={() => { setActiveTab("service"); setActiveSubIndex(0); }}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-t-lg text-sm font-medium transition-all ${
            activeTab === "service"
              ? "bg-slate-900 text-amber-400 border-t-2 border-amber-400"
              : "text-slate-400 hover:text-slate-200"
          }`}
          id="tab-service"
        >
          <Layers className="w-4 h-4" />
          B. Service Layer (Nghiệp vụ)
        </button>
        <button
          onClick={() => { setActiveTab("controller"); setActiveSubIndex(0); }}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-t-lg text-sm font-medium transition-all ${
            activeTab === "controller"
              ? "bg-slate-900 text-amber-400 border-t-2 border-amber-400"
              : "text-slate-400 hover:text-slate-200"
          }`}
          id="tab-controller"
        >
          <Code className="w-4 h-4" />
          C. Controller/API Layer
        </button>
        <button
          onClick={() => { setActiveTab("frontend"); setActiveSubIndex(0); }}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-t-lg text-sm font-medium transition-all ${
            activeTab === "frontend"
              ? "bg-slate-900 text-amber-400 border-t-2 border-amber-400"
              : "text-slate-400 hover:text-slate-200"
          }`}
          id="tab-frontend"
        >
          <FileCode className="w-4 h-4" />
          D. Giao diện Frontend
        </button>
      </div>

      {/* Main Panel split in Code Files (Left) and Content (Right) */}
      <div className="flex flex-1 overflow-hidden flex-col md:flex-row">
        {/* Sidebar file listing */}
        <div className="w-full md:w-64 bg-slate-950/50 border-r border-slate-800 p-4 flex flex-col gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1 px-2 block">
            Danh sách tệp tin
          </span>
          {activeTab === "database" ? (
            <div className="flex flex-col gap-1.5">
              <button
                onClick={() => setActiveSubIndex(0)}
                className={`text-left px-3 py-2 rounded-lg text-xs font-semibold flex items-center justify-between transition-all ${
                  activeSubIndex === 0
                    ? "bg-slate-800 text-amber-400 font-bold"
                    : "text-slate-400 hover:bg-slate-900/50"
                }`}
              >
                <span>Entity: Review.java</span>
                <span className="text-[10px] uppercase bg-amber-950 text-amber-300 font-normal px-1.5 py-0.5 rounded">JPA</span>
              </button>
              <div className="mt-4 p-3 bg-slate-900/50 rounded-xl border border-dotted border-slate-800">
                <span className="text-[10px] uppercase block text-indigo-400 font-bold mb-1">Hibernate Auto:</span>
                <div className="text-[11px] text-slate-400 leading-relaxed font-mono">
                  spring.jpa.hibernate.ddl-auto=update
                </div>
                <p className="text-[11px] text-slate-400 mt-2 leading-relaxed">
                  Tự động sinh các cột <code className="text-amber-400 font-mono text-xs">image_url</code>, <code className="text-amber-400 font-mono text-xs">restaurant_reply</code>, <code className="text-amber-400 font-mono text-xs">replied_at</code> khi khởi chạy ứng dụng Spring Boot.
                </p>
              </div>
            </div>
          ) : (
            <div className="flex flex-col gap-1">
              {snippets.map((snip, idx) => (
                <button
                  key={idx}
                  onClick={() => setActiveSubIndex(idx)}
                  className={`text-left px-3 py-2 rounded-lg text-xs font-semibold flex items-center justify-between transition-all ${
                    idx === activeSubIndex
                      ? "bg-slate-800 text-amber-400 font-bold"
                      : "text-slate-300 hover:bg-slate-900/50"
                  }`}
                >
                  <span className="truncate">{snip.title}</span>
                  <span className="text-[10px] uppercase bg-indigo-950 text-indigo-300 font-normal px-1 py-0.5 rounded ml-2 shrink-0">
                    {snip.language}
                  </span>
                </button>
              ))}
            </div>
          )}

          {activeTab === "database" && (
            <div className="mt-auto hidden md:block">
              <div className="flex items-center gap-1.5 text-xs text-amber-400 font-medium mb-1">
                <Shield className="w-3.5 h-3.5" />
                <span>DB Designer Note</span>
              </div>
              <p className="text-[10px] text-slate-400 leading-relaxed">
                Thiết kế bởi <strong>Ngô Minh Quân</strong>. Sử dụng cơ chế tự sinh schema để bảo toàn và update cột mượt mà trên môi trường.
              </p>
            </div>
          )}
          {activeTab === "frontend" && (
            <div className="mt-auto hidden md:block">
              <div className="flex items-center gap-1.5 text-xs text-emerald-400 font-medium mb-1">
                <CheckCircle className="w-3.5 h-3.5" />
                <span>Thymeleaf Inline Note</span>
              </div>
              <p className="text-[10px] text-slate-400 leading-relaxed">
                Biên soạn bởi <strong>Đinh Thị Như Quỳnh</strong>. Sử dụng JavaScript Fetch API kết hợp với Authorization Bearer Token.
              </p>
            </div>
          )}
        </div>

        {/* Code Content Panel */}
        <div className="flex-1 flex flex-col min-w-0 bg-slate-900">
          {/* Header Code Panel */}
          {activeSnippet && (
            <div className="bg-slate-950/70 p-3.5 border-b border-slate-800/80 flex items-center justify-between">
              <div>
                <h4 className="text-xs font-bold text-slate-200 font-mono">
                  {activeSnippet.title}
                </h4>
                <p className="text-[11px] text-slate-400 mt-1 max-w-xl">
                  {activeSnippet.description}
                </p>
              </div>
              <button
                onClick={handleCopy}
                className="text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium px-2.5 py-1.5 rounded transition"
              >
                {copied ? "Copied!" : "Copy Code"}
              </button>
            </div>
          )}

          {/* Code Scrolling Area / Database Visualizer */}
          <div className="flex-1 overflow-y-auto p-4 font-mono text-xs">
            {activeTab === "database" ? (
              <div className="space-y-6">
                {/* Visual Schema Database */}
                <div>
                  <div className="flex items-center gap-2 mb-3">
                    <Database className="w-4 h-4 text-amber-400 animate-pulse" />
                    <h4 className="text-xs font-bold font-sans text-slate-200">
                      BẢNG CƠ SỞ DỮ LIỆU THỰC TẾ: <span className="bg-amber-950/60 border border-amber-900 text-amber-400 px-2 py-0.5 rounded text-xs">{dbSchemaDetails.tableName}</span>
                    </h4>
                  </div>
                  <p className="text-[11px] font-sans text-slate-400 mb-4 leading-relaxed">
                    {dbSchemaDetails.description}
                  </p>

                  <div className="border border-slate-800 rounded-xl overflow-hidden bg-slate-950/30">
                    <table className="w-full text-left font-sans text-xs border-collapse">
                      <thead>
                        <tr className="bg-slate-950 border-b border-slate-800 text-slate-400 uppercase tracking-wider text-[10px] font-bold">
                          <th className="p-3">TÊN CỘT (COLUMN)</th>
                          <th className="p-3">KIỂU DỮ LIỆU (TYPE)</th>
                          <th className="p-3">MÔ TẢ (DESCRIPTION)</th>
                          <th className="p-3 text-right">TRẠNG THÁI</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/50">
                        {dbSchemaDetails.columns.map((col, index) => (
                          <tr
                            key={index}
                            className={`hover:bg-slate-850/50 transition ${
                              col.isNew ? "bg-amber-950/20 text-slate-100" : "text-slate-300"
                            }`}
                          >
                            <td className="p-3 font-mono font-semibold text-amber-400">
                              {col.name}
                            </td>
                            <td className="p-3 font-mono text-[11px] text-slate-400">
                              {col.type}
                            </td>
                            <td className="p-3 text-[11px]">
                              {col.description}
                            </td>
                            <td className="p-3 text-right">
                              {col.isNew ? (
                                <span className="inline-block bg-amber-500/15 text-amber-400 border border-amber-500/30 text-[10px] font-bold px-2 py-0.5 rounded-full animate-pulse">
                                  MỚI (ADD)
                                </span>
                              ) : (
                                <span className="text-[10px] text-slate-500 font-semibold">GỐC</span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>

                {/* DB Config details */}
                <div className="bg-slate-950/40 border border-slate-800 p-4 rounded-xl font-sans text-xs leading-relaxed text-slate-300">
                  <div className="flex items-center gap-2 mb-2 text-slate-200 font-semibold">
                    <HelpCircle className="w-4 h-4 text-emerald-400" />
                    <span>Lưu ý về đồng bộ Spring JPA & DDL-Auto update</span>
                  </div>
                  <p className="mb-2">
                    Trong cấu hình <code className="text-amber-400 font-mono bg-slate-950 px-1 py-0.5 rounded">application.properties</code>, dòng thiết lập sau giúp hệ thống tự phát triển schema:
                  </p>
                  <pre className="bg-slate-950 p-2.5 rounded border border-slate-800 font-mono text-emerald-400 text-xs flex justify-between">
                    <span>spring.jpa.hibernate.ddl-auto=update</span>
                  </pre>
                  <p className="mt-2 text-slate-400">
                    Nhờ vậy, khi chạy server, 3 cột bổ sung <code className="text-amber-400 font-mono">image_url</code>, <code className="text-amber-400 font-mono">restaurant_reply</code>, và <code className="text-amber-400 font-mono">replied_at</code> tự động chèn thêm vào bảng MySQL database thực tế chứ không mất mát dữ liệu của các đơn hàng cũ.
                  </p>
                </div>
              </div>
            ) : (
              activeSnippet && (
                <div className="relative">
                  <pre className="text-slate-300 leading-5 text-[11px] select-all scrollbar-thin scrollbar-thumb-slate-800">
                    <code>{activeSnippet.code}</code>
                  </pre>
                </div>
              )
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
