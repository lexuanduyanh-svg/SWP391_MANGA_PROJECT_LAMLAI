import { Bell, Search } from "lucide-react";
import { Role } from "../shared";

export default function TopNav({ role, page, onLogout }: { role: Role; page: string; onLogout: () => void }) {
  return (
    <div className="h-20 bg-[#a3a3a3] flex items-center justify-between px-6">
      <div className="flex items-center gap-3">
        <div className="relative">
          <input
            className="w-52 h-9 pl-8 pr-3 bg-[#c0c0c0] border border-transparent rounded-xl text-sm text-gray-700 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-white/50"
            placeholder="Search…"
          />
          <Search size={13} className="absolute left-2.5 top-2.5 text-gray-500" />
        </div>
        <button className="relative w-9 h-9 flex items-center justify-center text-white bg-[#c0c0c0] rounded-xl transition-colors hover:bg-white/30">
          <Bell size={16} />
          <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 bg-red-400 rounded-full" />
        </button>
      </div>
      <button
        onClick={onLogout}
        className="flex items-center gap-2.5 h-[50px] px-6 bg-[#e8e8e8] hover:bg-white rounded-[10px] text-sm font-semibold text-gray-800 transition-colors min-w-[120px] justify-center shadow-sm"
      >
        Profile
      </button>
    </div>
  );
}
