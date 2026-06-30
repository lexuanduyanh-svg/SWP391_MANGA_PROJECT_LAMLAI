import { useState } from "react";
import { PenTool, Mail, Lock, ChevronRight } from "lucide-react";
import { Role, roleColors, roleLabels } from "../shared";
import { api } from "../api";

const demoAccounts: Record<string, Role> = {
  "mangaka@manga.local": "mangaka",
  "assistant@manga.local": "assistant",
  "editor@manga.local": "editor",
  "board@manga.local": "board",
  "board2@manga.local": "board",
  "board3@manga.local": "board",
};

export default function LoginPage({ onLogin }: { onLogin: (role: Role, email: string) => void }) {
  const [email, setEmail] = useState("mangaka@manga.local");
  const [password, setPassword] = useState("Mangaka@123");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [forgotPw, setForgotPw] = useState(false);

  if (forgotPw) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-blue-50 flex items-center justify-center p-4">
        <div className="w-full max-w-sm bg-white rounded-2xl shadow-xl border border-purple-100 p-8">
          <button onClick={() => setForgotPw(false)} className="text-purple-500 hover:text-purple-700 text-xs font-medium mb-6">← Back to login</button>
          <div className="text-center mb-6">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-purple-500 to-blue-500 flex items-center justify-center mx-auto mb-3 text-white"><Lock size={20} /></div>
            <h2 className="text-xl font-bold text-gray-900">Reset Password</h2>
            <p className="text-sm text-gray-500 mt-1">Enter your email to receive a reset link.</p>
          </div>
          <div className="space-y-4">
            <div><label className="block text-xs font-medium text-gray-500 mb-1.5">Email Address</label><input className="w-full h-10 px-3 bg-gray-50 border border-gray-200 rounded-xl text-sm" placeholder="your@email.com" /></div>
            <button className="w-full py-2.5 bg-gradient-to-r from-purple-600 to-blue-600 text-white text-sm font-semibold rounded-xl">Send Reset Link</button>
          </div>
        </div>
      </div>
    );
  }

  const handleLogin = async () => {
    setError(""); setLoading(true);
    try {
      const res = await api.login(email, password);
      onLogin(res.role ?? demoAccounts[email] ?? "mangaka", email);
    } catch {
      const fallbackRole = demoAccounts[email];
      if (fallbackRole) {
        onLogin(fallbackRole, email);
      } else {
        setError("Invalid credentials. Try a demo account.");
      }
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-blue-50 flex items-center justify-center p-4">
      <div className="w-full max-w-sm bg-white rounded-2xl shadow-xl border border-purple-100 p-8">
        <div className="text-center mb-7">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-purple-600 to-blue-600 flex items-center justify-center mx-auto mb-3 shadow-lg"><PenTool size={24} className="text-white" /></div>
          <h1 className="text-2xl font-bold text-gray-900">MangaFlow</h1>
          <p className="text-sm text-gray-400 mt-1">Manga Creation & Publishing System</p>
        </div>
        <div className="space-y-4">
          <div><label className="block text-xs font-medium text-gray-500 mb-1.5">Email</label><div className="relative"><Mail size={14} className="absolute left-3 top-3 text-gray-400" /><input value={email} onChange={e => setEmail(e.target.value)} className="w-full h-10 pl-9 pr-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-200" /></div></div>
          <div><label className="block text-xs font-medium text-gray-500 mb-1.5">Password</label><div className="relative"><Lock size={14} className="absolute left-3 top-3 text-gray-400" /><input type="password" value={password} onChange={e => setPassword(e.target.value)} className="w-full h-10 pl-9 pr-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-200" /></div></div>
          {error && <p className="text-xs text-red-500">{error}</p>}
          <button onClick={() => setForgotPw(true)} className="text-xs text-purple-600 hover:text-purple-700 font-medium">Forgot password?</button>
          <button onClick={handleLogin} disabled={loading} className="w-full h-10 bg-gradient-to-r from-purple-600 to-blue-600 text-white text-sm font-semibold rounded-xl shadow-sm hover:shadow-md transition-shadow disabled:opacity-50">{loading ? "Signing in..." : "Sign in"}</button>
        </div>
        <div className="mt-5 pt-4 border-t border-gray-100 text-center"><p className="text-xs text-gray-400">Demo: mangaka@manga.local / Assistant@123</p></div>
      </div>
    </div>
  );
}
