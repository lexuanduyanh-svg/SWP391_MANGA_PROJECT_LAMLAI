import VotingPanel from "./VotingPanel";

export default function BoardPages({ page, memberEmail }: { page: string; memberEmail: string }) {
  if (page === "dashboard") return <div className="p-6"><h1 className="text-xl font-bold text-gray-900">Editorial Board Overview</h1></div>;
  if (page === "voting") return <VotingPanel memberEmail={memberEmail} />;
  if (page === "analytics") return <div className="p-6">Rankings & Analytics</div>;
  if (page === "decisions") return <div className="p-6">Series Decisions</div>;
  if (page === "schedule") return <div className="p-6">Publishing Schedule</div>;
  if (page === "reader-data") return <div className="p-6">Reader Data Input</div>;
  return <div className="p-6">Board: {page}</div>;
}
