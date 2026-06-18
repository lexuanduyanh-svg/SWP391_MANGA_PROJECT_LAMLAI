import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  changeAdminAccountStatus,
  createAdminAccount,
  deleteAdminAccount,
  listAdminAccounts,
  updateAdminAccountSkills,
  updateAdminAccount,
} from "../services/adminAccountService";
import {
  createSkill,
  deleteSkill,
  listSkills,
  toggleSkillStatus,
  updateSkill,
} from "../services/skillService";
import type {
  AccountStatus,
  AdminAccount,
  SkillCategory,
} from "../types/admin";
import type { LoginResponse, UserRole } from "../types/auth";

const ROLE_OPTIONS: UserRole[] = [
  "Admin",
  "Mangaka",
  "Assistant",
  "TantouEditor",
  "EditorialBoardMember",
];
const STATUS_OPTIONS: AccountStatus[] = ["Active", "Inactive", "Suspended"];

interface AdminFormState {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
  status: AccountStatus;
}

interface SkillFormState {
  name: string;
  description: string;
}

const emptyAccountForm: AdminFormState = {
  fullName: "",
  email: "",
  password: "",
  role: "Assistant",
  status: "Active",
};

const emptySkillForm: SkillFormState = {
  name: "",
  description: "",
};

export function AdminAccountDashboard({
  session,
  onLogout,
}: {
  session: LoginResponse;
  onLogout?: () => void;
}) {
  const [accounts, setAccounts] = useState<AdminAccount[]>([]);
  const [skills, setSkills] = useState<SkillCategory[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [accountForm, setAccountForm] = useState(emptyAccountForm);
  const [skillForm, setSkillForm] = useState(emptySkillForm);
  const [editingAccountId, setEditingAccountId] = useState<string | null>(null);
  const [editingSkillId, setEditingSkillId] = useState<string | null>(null);
  const [pendingAccountSkills, setPendingAccountSkills] = useState<
    Record<string, string[]>
  >({});

  const selectedAccount = useMemo(
    () => accounts.find((account) => account.id === editingAccountId),
    [accounts, editingAccountId],
  );

  const editingSkill = useMemo(
    () => skills.find((skill) => skill.id === editingSkillId),
    [editingSkillId, skills],
  );

  const isEditingAccount = Boolean(selectedAccount);
  const accountCount = accounts.length;
  const activeAccountCount = accounts.filter(
    (account) => account.status === "Active",
  ).length;
  const skillCount = skills.length;
  const activeSkillCount = skills.filter((skill) => skill.active).length;

  useEffect(() => {
    let isMounted = true;

    async function loadBoard() {
      setIsLoading(true);

      try {
        const [loadedAccounts, loadedSkills] = await Promise.all([
          listAdminAccounts(),
          listSkills(),
        ]);

        if (!isMounted) {
          return;
        }

        setAccounts(loadedAccounts);
        setSkills(loadedSkills);
        setPendingAccountSkills(
          Object.fromEntries(
            loadedAccounts.map((account) => [
              account.id,
              account.skills.map((skill) => skill.id),
            ]),
          ),
        );
      } catch (error) {
        if (isMounted) {
          setErrorMessage(getErrorMessage(error));
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadBoard();

    return () => {
      isMounted = false;
    };
  }, []);

  async function refreshAccounts() {
    const updated = await listAdminAccounts();
    setAccounts(updated);
    setPendingAccountSkills(
      Object.fromEntries(
        updated.map((account) => [
          account.id,
          account.skills.map((skill) => skill.id),
        ]),
      ),
    );
  }

  async function refreshSkills() {
    setSkills(await listSkills());
  }

  async function handleAccountSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage(null);

    try {
      if (selectedAccount) {
        await updateAdminAccount(selectedAccount.id, {
          fullName: accountForm.fullName,
          email: accountForm.email,
          role: accountForm.role,
          status: accountForm.status,
        });
      } else {
        await createAdminAccount({
          fullName: accountForm.fullName,
          email: accountForm.email,
          password: accountForm.password,
          role: accountForm.role,
        });
      }

      await refreshAccounts();
      setAccountForm(emptyAccountForm);
      setEditingAccountId(null);
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    }
  }

  async function handleToggleAccountStatus(account: AdminAccount) {
    const nextStatus: AccountStatus =
      account.status === "Active" ? "Inactive" : "Active";
    await changeAdminAccountStatus(account.id, nextStatus);
    await refreshAccounts();
  }

  async function handleDeleteAccount(account: AdminAccount) {
    await deleteAdminAccount(account.id);
    await refreshAccounts();
  }

  async function handleSaveAccountSkills(accountId: string) {
    setErrorMessage(null);
    try {
      const updated = await updateAdminAccountSkills(
        accountId,
        pendingAccountSkills[accountId] ?? [],
      );
      setAccounts((current) =>
        current.map((account) =>
          account.id === updated.id ? updated : account,
        ),
      );
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    }
  }

  function handleEditAccount(account: AdminAccount) {
    setEditingAccountId(account.id);
    setAccountForm({
      fullName: account.fullName,
      email: account.email,
      password: "",
      role: account.role,
      status: account.status,
    });
  }

  async function handleSkillSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage(null);

    try {
      if (editingSkill) {
        await updateSkill(editingSkill.id, skillForm);
      } else {
        await createSkill(skillForm);
      }

      await refreshSkills();
      setSkillForm(emptySkillForm);
      setEditingSkillId(null);
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    }
  }

  async function handleToggleSkill(skill: SkillCategory) {
    await toggleSkillStatus(skill.id, !skill.active);
    await refreshSkills();
  }

  async function handleDeleteSkill(skill: SkillCategory) {
    await deleteSkill(skill.id);
    await refreshSkills();
  }

  function handleEditSkill(skill: SkillCategory) {
    setEditingSkillId(skill.id);
    setSkillForm({
      name: skill.name,
      description: skill.description ?? "",
    });
  }

  return (
    <div className="admin-dashboard">
      <aside className="board-rail section-card">
        <div className="board-rail__brand">
          <span className="board-rail__badge" aria-hidden="true">
            MP
          </span>
          <div>
            <span className="eyebrow">Manga project</span>
            <strong>SWP391 Control Board</strong>
          </div>
        </div>

        <nav className="board-rail__nav" aria-label="Dashboard sections">
          <a href="#accounts-board">Accounts</a>
          <a href="#skills-board">Skills & categories</a>
          <a href="#dashboard-summary">Workspace summary</a>
        </nav>

        <div className="board-rail__stats">
          <div className="board-stat">
            <span>Total accounts</span>
            <strong>{accountCount}</strong>
          </div>
          <div className="board-stat">
            <span>Active accounts</span>
            <strong>{activeAccountCount}</strong>
          </div>
          <div className="board-stat">
            <span>Skills online</span>
            <strong>
              {activeSkillCount}/{skillCount || 0}
            </strong>
          </div>
        </div>
      </aside>

      <div className="board-main">
        <header className="dashboard-hero section-card" id="dashboard-summary">
          <div className="dashboard-hero__copy">
            <span className="eyebrow">Admin workspace</span>
            <h1>Hi, {session.user.fullName}</h1>
            <p>
              Manage manga project access in a cleaner board layout with clear
              separation for accounts, skill categories, and system status.
            </p>
          </div>

          <div className="dashboard-hero__meta">
            <div className="dashboard-chip">
              <span>Signed in as</span>
              <strong>{session.user.role}</strong>
            </div>
            <div className="dashboard-chip dashboard-chip--accent">
              <span>Focus</span>
              <strong>Accounts + skills</strong>
            </div>
            <div className="dashboard-chip dashboard-chip--muted">
              <span>Destination</span>
              <strong>{session.dashboardPath}</strong>
            </div>
            <button
              className="button button-secondary dashboard-logout"
              type="button"
              onClick={onLogout}
            >
              Log out
            </button>
          </div>
        </header>

        {errorMessage && (
          <div className="board-alert" role="alert">
            {errorMessage}
          </div>
        )}

        <div className="board-grid">
          <section
            className="section-card admin-card admin-card--board"
            id="accounts-board"
            aria-labelledby="accounts-title"
          >
            <div className="admin-card__header">
              <div>
                <span className="eyebrow">Accounts</span>
                <h2 id="accounts-title">Account roster</h2>
                <p>
                  Create, update, activate, or remove access without leaving the
                  board.
                </p>
              </div>

              <div className="admin-card__stats">
                <div>
                  <span>Records</span>
                  <strong>{accountCount}</strong>
                </div>
                <div>
                  <span>Active</span>
                  <strong>{activeAccountCount}</strong>
                </div>
              </div>
            </div>

            <div className="admin-card__content admin-card__content--split">
              <form
                onSubmit={handleAccountSubmit}
                className="admin-form admin-form--panel"
              >
                <div className="form-grid form-grid--two">
                  <label className="admin-field">
                    <span>Full name</span>
                    <input
                      placeholder="Nguyen Van A"
                      value={accountForm.fullName}
                      onChange={(event) =>
                        setAccountForm({
                          ...accountForm,
                          fullName: event.target.value,
                        })
                      }
                    />
                  </label>

                  <label className="admin-field">
                    <span>Email</span>
                    <input
                      placeholder="name@company.com"
                      value={accountForm.email}
                      onChange={(event) =>
                        setAccountForm({
                          ...accountForm,
                          email: event.target.value,
                        })
                      }
                    />
                  </label>
                </div>

                <div className="form-grid form-grid--two">
                  <label className="admin-field">
                    <span>
                      Password{" "}
                      {isEditingAccount ? "(optional when editing)" : ""}
                    </span>
                    <input
                      placeholder={
                        isEditingAccount
                          ? "Leave blank to keep current password"
                          : "Temporary password"
                      }
                      type="password"
                      value={accountForm.password}
                      onChange={(event) =>
                        setAccountForm({
                          ...accountForm,
                          password: event.target.value,
                        })
                      }
                    />
                  </label>

                  <label className="admin-field">
                    <span>Role</span>
                    <select
                      value={accountForm.role}
                      onChange={(event) =>
                        setAccountForm({
                          ...accountForm,
                          role: event.target.value as UserRole,
                        })
                      }
                    >
                      {ROLE_OPTIONS.map((role) => (
                        <option key={role} value={role}>
                          {role}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <label className="admin-field">
                  <span>Status</span>
                  <select
                    value={accountForm.status}
                    onChange={(event) =>
                      setAccountForm({
                        ...accountForm,
                        status: event.target.value as AccountStatus,
                      })
                    }
                  >
                    {STATUS_OPTIONS.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </label>

                <div className="admin-form__actions">
                  <button className="primary-button" type="submit">
                    {isEditingAccount ? "Update account" : "Create account"}
                  </button>
                  <button
                    className="button button-secondary"
                    type="button"
                    onClick={() => {
                      setAccountForm(emptyAccountForm);
                      setEditingAccountId(null);
                    }}
                  >
                    Clear form
                  </button>
                </div>
              </form>

              <div
                className="admin-list admin-list--compact"
                aria-label="Account list"
                aria-busy={isLoading}
              >
                {isLoading ? (
                  <div className="admin-empty-state admin-empty-state--loading">
                    Loading account roster…
                  </div>
                ) : accounts.length === 0 ? (
                  <div className="admin-empty-state">
                    <strong>No accounts yet</strong>
                    <p>
                      Add the first manga project access record to populate the
                      board.
                    </p>
                  </div>
                ) : (
                  accounts.map((account) => (
                    <article className="admin-row-card" key={account.id}>
                      <div className="admin-row-card__copy">
                        <div className="admin-row-card__title">
                          <strong>{account.fullName}</strong>
                          <span
                            className={`status-badge status-badge--${account.status.toLowerCase()}`}
                          >
                            {account.status}
                          </span>
                        </div>
                        <p>{account.email}</p>
                        <div className="admin-row-card__meta">
                          <span>{account.role}</span>
                          <span>•</span>
                          <span>ID {account.id}</span>
                        </div>
                        <div className="chip-list">
                          {account.skills.length > 0 ? (
                            account.skills.map((skill) => (
                              <span className="chip" key={skill.id}>
                                {skill.name}
                              </span>
                            ))
                          ) : (
                            <span className="chip chip--muted">No skills</span>
                          )}
                        </div>
                        <div className="admin-skill-picker">
                          {skills.map((skill) => {
                            const current =
                              pendingAccountSkills[account.id] ??
                              account.skills.map((item) => item.id);
                            const checked = current.includes(skill.id);
                            return (
                              <label
                                key={skill.id}
                                className="admin-skill-picker__item"
                              >
                                <input
                                  type="checkbox"
                                  checked={checked}
                                  onChange={(event) => {
                                    const next = event.target.checked
                                      ? [...current, skill.id]
                                      : current.filter((id) => id !== skill.id);
                                    setPendingAccountSkills((state) => ({
                                      ...state,
                                      [account.id]: next,
                                    }));
                                  }}
                                />
                                <span>{skill.name}</span>
                              </label>
                            );
                          })}
                          <button
                            type="button"
                            onClick={() => handleSaveAccountSkills(account.id)}
                          >
                            Save skills
                          </button>
                        </div>
                      </div>

                      <div className="admin-actions">
                        <button
                          type="button"
                          onClick={() => handleEditAccount(account)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => handleToggleAccountStatus(account)}
                        >
                          {account.status === "Active"
                            ? "Deactivate"
                            : "Activate"}
                        </button>
                        <button
                          type="button"
                          className="danger-action"
                          onClick={() => handleDeleteAccount(account)}
                        >
                          Delete
                        </button>
                      </div>
                    </article>
                  ))
                )}
              </div>
            </div>
          </section>

          <section
            className="section-card admin-card admin-card--board"
            id="skills-board"
            aria-labelledby="skills-title"
          >
            <div className="admin-card__header">
              <div>
                <span className="eyebrow">Skills / categories</span>
                <h2 id="skills-title">Skill taxonomy board</h2>
                <p>
                  Keep the manga workflow taxonomy readable with clear
                  active/inactive states.
                </p>
              </div>

              <div className="admin-card__stats">
                <div>
                  <span>Categories</span>
                  <strong>{skillCount}</strong>
                </div>
                <div>
                  <span>Enabled</span>
                  <strong>{activeSkillCount}</strong>
                </div>
              </div>
            </div>

            <div className="admin-card__content admin-card__content--split">
              <form
                onSubmit={handleSkillSubmit}
                className="admin-form admin-form--panel"
              >
                <label className="admin-field">
                  <span>Skill name</span>
                  <input
                    placeholder="Storyboarding"
                    value={skillForm.name}
                    onChange={(event) =>
                      setSkillForm({ ...skillForm, name: event.target.value })
                    }
                  />
                </label>

                <label className="admin-field">
                  <span>Description</span>
                  <input
                    placeholder="Short note for the board"
                    value={skillForm.description}
                    onChange={(event) =>
                      setSkillForm({
                        ...skillForm,
                        description: event.target.value,
                      })
                    }
                  />
                </label>

                <div className="admin-form__actions">
                  <button className="primary-button" type="submit">
                    {editingSkill ? "Update skill" : "Create skill"}
                  </button>
                  <button
                    className="button button-secondary"
                    type="button"
                    onClick={() => {
                      setSkillForm(emptySkillForm);
                      setEditingSkillId(null);
                    }}
                  >
                    Clear form
                  </button>
                </div>
              </form>

              <div
                className="admin-list admin-list--compact"
                aria-label="Skill list"
                aria-busy={isLoading}
              >
                {isLoading ? (
                  <div className="admin-empty-state admin-empty-state--loading">
                    Loading skill taxonomy…
                  </div>
                ) : skills.length === 0 ? (
                  <div className="admin-empty-state">
                    <strong>No skill categories yet</strong>
                    <p>
                      Define the first workflow category so assignments stay
                      easy to scan.
                    </p>
                  </div>
                ) : (
                  skills.map((skill) => (
                    <article className="admin-row-card" key={skill.id}>
                      <div className="admin-row-card__copy">
                        <div className="admin-row-card__title">
                          <strong>{skill.name}</strong>
                          <span
                            className={`status-badge status-badge--${skill.active ? "active" : "inactive"}${
                              skill.active ? "" : " status-badge--muted"
                            }`}
                          >
                            {skill.active ? "Active" : "Inactive"}
                          </span>
                        </div>
                        <p>{skill.description ?? "No description yet"}</p>
                        <div className="admin-row-card__meta">
                          <span>Skill ID {skill.id}</span>
                        </div>
                      </div>

                      <div className="admin-actions">
                        <button
                          type="button"
                          onClick={() => handleEditSkill(skill)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => handleToggleSkill(skill)}
                        >
                          {skill.active ? "Disable" : "Enable"}
                        </button>
                        <button
                          type="button"
                          className="danger-action"
                          onClick={() => handleDeleteSkill(skill)}
                        >
                          Delete
                        </button>
                      </div>
                    </article>
                  ))
                )}
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Không thể lưu dữ liệu.";
}
