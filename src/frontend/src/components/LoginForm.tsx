import { FormEvent, useState } from "react";
import { login } from "../services/authService";
import type { LoginResponse } from "../types/auth";

interface LoginFormProps {
  onLoginSuccess: (response: LoginResponse) => void;
}

export function LoginForm({ onLoginSuccess }: LoginFormProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(true);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage(null);

    if (!email.trim() || !password.trim()) {
      setErrorMessage("Vui lòng nhập đầy đủ email và mật khẩu.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await login({ email: email.trim(), password });
      onLoginSuccess(response);
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Không thể đăng nhập. Vui lòng thử lại sau.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="auth-card" onSubmit={handleSubmit} id="auth-panel">
      <div
        className="auth-card__glow auth-card__glow--pink"
        aria-hidden="true"
      />
      <div
        className="auth-card__glow auth-card__glow--blue"
        aria-hidden="true"
      />

      <div className="login-heading">
        <div className="login-heading__icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" role="img" aria-hidden="true">
            <defs>
              <linearGradient
                id="loginHeadingGradient"
                x1="0%"
                y1="0%"
                x2="100%"
                y2="100%"
              >
                <stop offset="0%" stopColor="#8b5cf6" />
                <stop offset="100%" stopColor="#38bdf8" />
              </linearGradient>
            </defs>
            <path
              d="M6.75 6.5h7.1l3.4 3.4v7.6a1.5 1.5 0 0 1-1.5 1.5H6.75a1.5 1.5 0 0 1-1.5-1.5V8a1.5 1.5 0 0 1 1.5-1.5Z"
              fill="url(#loginHeadingGradient)"
              opacity="0.2"
            />
            <path
              d="M8 6.5h5.85L17.5 10v7.5A1.5 1.5 0 0 1 16 19H8a1.5 1.5 0 0 1-1.5-1.5V8A1.5 1.5 0 0 1 8 6.5Z"
              fill="none"
              stroke="url(#loginHeadingGradient)"
              strokeWidth="1.6"
              strokeLinejoin="round"
            />
            <path
              d="M14.1 6.5V9.6h3.1"
              fill="none"
              stroke="url(#loginHeadingGradient)"
              strokeWidth="1.6"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M9.4 11.1h4.5M9.4 13.1h3.25"
              fill="none"
              stroke="url(#loginHeadingGradient)"
              strokeWidth="1.6"
              strokeLinecap="round"
            />
            <path
              d="M12.65 15.25 18 19"
              fill="none"
              stroke="url(#loginHeadingGradient)"
              strokeWidth="1.6"
              strokeLinecap="round"
            />
            <circle cx="12" cy="16.7" r="1.2" fill="#09121f" opacity="0.72" />
          </svg>
        </div>
        <h1>Welcome</h1>
        <p>Sign in to manage your manga workflow</p>
      </div>

      <label className="form-field" htmlFor="email">
        <span className="field-label">Email</span>
        <span className="input-shell">
          <span className="input-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" role="img" aria-hidden="true">
              <path
                d="M4.5 7.5h15v9h-15z"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.7"
                strokeLinejoin="round"
              />
              <path
                d="m5.5 8.5 6.5 5 6.5-5"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.7"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </span>
          <input
            id="email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="name@company.com"
          />
        </span>
      </label>

      <label className="form-field" htmlFor="password">
        <span className="field-label">Password</span>
        <span className="input-shell">
          <span className="input-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" role="img" aria-hidden="true">
              <rect
                x="5.5"
                y="10"
                width="13"
                height="8.5"
                rx="2"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.7"
              />
              <path
                d="M8.25 10V8.25a3.75 3.75 0 0 1 7.5 0V10"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.7"
                strokeLinecap="round"
              />
            </svg>
          </span>
          <input
            id="password"
            type={isPasswordVisible ? "text" : "password"}
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Your password"
          />
          <button
            className="password-toggle"
            type="button"
            onClick={() => setIsPasswordVisible((visible) => !visible)}
            aria-label={isPasswordVisible ? "Hide password" : "Show password"}
            aria-pressed={isPasswordVisible}
          >
            <svg viewBox="0 0 24 24" role="img" aria-hidden="true">
              {isPasswordVisible ? (
                <>
                  <path
                    d="M3.8 12s3.1-5.8 8.2-5.8S20.2 12 20.2 12s-3.1 5.8-8.2 5.8S3.8 12 3.8 12Z"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                    strokeLinejoin="round"
                  />
                  <circle
                    cx="12"
                    cy="12"
                    r="2.3"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                  />
                  <path
                    d="M5 5 19 19"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                    strokeLinecap="round"
                  />
                </>
              ) : (
                <>
                  <path
                    d="M3.8 12s3.1-5.8 8.2-5.8S20.2 12 20.2 12s-3.1 5.8-8.2 5.8S3.8 12 3.8 12Z"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                    strokeLinejoin="round"
                  />
                  <circle
                    cx="12"
                    cy="12"
                    r="2.3"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                  />
                </>
              )}
            </svg>
          </button>
        </span>
      </label>

      <div className="form-row">
        <label className="remember-me" htmlFor="rememberMe">
          <input
            id="rememberMe"
            type="checkbox"
            checked={rememberMe}
            onChange={(event) => setRememberMe(event.target.checked)}
          />
          <span>Remember me</span>
        </label>

        <a className="forgot-link" href="#forgot-password">
          Forgot password?
        </a>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <button className="primary-button" type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Signing in..." : "Sign in"}
      </button>

      <div className="auth-footer">
        <p>Need access? Contact the administrator</p>
      </div>
    </form>
  );
}
