import ReviewProposal from "./ReviewProposal";

export default function EditorPages({ page, editorEmail }: { page: string; editorEmail: string }) {
  if (page === "dashboard") return <div className="p-6"><h1 className="text-xl font-bold text-gray-900">Editorial Overview</h1></div>;
  if (page === "proposals") return <ReviewProposal editorEmail={editorEmail} />;
  if (page === "manuscript") return <div className="p-6">Manuscript Annotation Tool</div>;
  if (page === "production") return <div className="p-6">Production Monitor</div>;
  if (page === "reports") return <div className="p-6">Defense Reports</div>;
  return <div className="p-6">Editor: {page}</div>;
}
