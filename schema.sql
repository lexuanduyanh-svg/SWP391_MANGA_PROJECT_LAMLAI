-- ====================================================================
-- CLEANUP (Allows you to safely re-run this script if you make a mistake)
-- ====================================================================
DROP TABLE IF EXISTS board_votes CASCADE;
DROP TABLE IF EXISTS reader_metrics CASCADE;
DROP TABLE IF EXISTS annotations CASCADE;
DROP TABLE IF EXISTS submissions CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS pages CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS series CASCADE;
DROP TABLE IF EXISTS user_skills CASCADE;
DROP TABLE IF EXISTS skills CASCADE;
DROP TABLE IF EXISTS assistant_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- ====================================================================
-- UTILITY
-- ====================================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================
-- 1. USER & ACCESS MANAGEMENT
-- ====================================================================

CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    action_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id INTEGER REFERENCES roles(role_id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permissions(permission_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role_id INTEGER NOT NULL REFERENCES roles(role_id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE assistant_profiles (
    user_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    monthly_earnings DECIMAL(10, 2) DEFAULT 0.00 CHECK (monthly_earnings >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skills (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_skills (
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    skill_id INTEGER REFERENCES skills(skill_id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, skill_id)
);

-- ====================================================================
-- 2. CONTENT & SERIALIZATION (Unified Series & Proposal Lifecycle)
-- ====================================================================

CREATE TABLE series (
    series_id SERIAL PRIMARY KEY,
    mangaka_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    tantou_editor_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    synopsis TEXT,
    genre VARCHAR(100),
    status VARCHAR(50) DEFAULT 'DRAFT',
    publishing_frequency VARCHAR(50) DEFAULT 'WEEKLY',
    editor_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_series_status CHECK (status IN (
        'DRAFT',
        'SUBMITTED_TO_EDITOR',
        'REVISION_REQUESTED',
        'UNDER_BOARD_REVIEW',
        'APPROVED',
        'REJECTED',
        'SERIALIZING',
        'CANCELLED'
    )),
    CONSTRAINT chk_publishing_frequency CHECK (publishing_frequency IN ('WEEKLY', 'MONTHLY'))
);

CREATE TABLE chapters (
    chapter_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    chapter_number INTEGER NOT NULL,
    title VARCHAR(255),
    status VARCHAR(50) DEFAULT 'DRAFT',
    print_deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_series_chapter UNIQUE (series_id, chapter_number),
    CONSTRAINT chk_chapter_number CHECK (chapter_number > 0),
    CONSTRAINT chk_chapter_status CHECK (status IN (
        'DRAFT',
        'IN_PROGRESS',
        'PAGES_UPLOADED',
        'TASKS_IN_PROGRESS',
        'READY_FOR_EDITOR',
        'PUBLISHED',
        'CANCELLED'
    ))
);

CREATE TABLE pages (
    page_id SERIAL PRIMARY KEY,
    chapter_id INTEGER REFERENCES chapters(chapter_id) ON DELETE CASCADE,
    page_number INTEGER NOT NULL,
    manuscript_file_path VARCHAR(512),
    status VARCHAR(50) DEFAULT 'UPLOADED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_chapter_page UNIQUE (chapter_id, page_number),
    CONSTRAINT chk_page_number CHECK (page_number > 0),
    CONSTRAINT chk_page_status CHECK (status IN (
        'UPLOADED',
        'TASKS_ASSIGNED',
        'TASKS_IN_PROGRESS',
        'ALL_TASKS_APPROVED',
        'FINALIZED'
    ))
);

-- ====================================================================
-- 3. PRODUCTION WORKFLOW
-- ====================================================================

CREATE TABLE tasks (
    task_id SERIAL PRIMARY KEY,
    page_id INTEGER REFERENCES pages(page_id) ON DELETE CASCADE,
    assistant_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    task_type INTEGER REFERENCES skills(skill_id),
    region_coordinates JSONB,
    payment DECIMAL(10, 2) DEFAULT 0.00 CHECK (payment >= 0),
    status VARCHAR(50) DEFAULT 'ASSIGNED',
    feedback_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_task_status CHECK (status IN (
        'ASSIGNED',
        'PENDING_REVIEW',
        'APPROVED',
        'REVISION_REQUESTED',
        'REJECTED',
        'CANCELLED'
    ))
);

CREATE TABLE submissions (
    submission_id SERIAL PRIMARY KEY,
    task_id INTEGER REFERENCES tasks(task_id) ON DELETE CASCADE,
    asset_file_path VARCHAR(512) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE annotations (
    markup_id SERIAL PRIMARY KEY,
    page_id INTEGER REFERENCES pages(page_id) ON DELETE CASCADE,
    editor_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    spatial_coordinates JSONB,
    content TEXT NOT NULL,
    resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- 4. STRATEGY & METRICS (Enforcing Business Rules)
-- ====================================================================

CREATE TABLE reader_metrics (
    metric_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    publication_cycle DATE NOT NULL,
    sales_figures INTEGER DEFAULT 0 CHECK (sales_figures >= 0),
    likes_count INTEGER DEFAULT 0 CHECK (likes_count >= 0),
    shares_count INTEGER DEFAULT 0 CHECK (shares_count >= 0),
    votes_count INTEGER DEFAULT 0 CHECK (votes_count >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_series_cycle UNIQUE (series_id, publication_cycle)
);

CREATE TABLE board_votes (
    vote_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    board_member_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    decision VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_board_member_vote UNIQUE (series_id, board_member_id),
    CONSTRAINT chk_vote_decision CHECK (decision IN ('APPROVE', 'REJECT'))
);

-- ====================================================================
-- 5. UPDATED_AT TRIGGERS
-- ====================================================================
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_series_updated_at
BEFORE UPDATE ON series
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_chapters_updated_at
BEFORE UPDATE ON chapters
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_pages_updated_at
BEFORE UPDATE ON pages
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_tasks_updated_at
BEFORE UPDATE ON tasks
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_reader_metrics_updated_at
BEFORE UPDATE ON reader_metrics
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ====================================================================
-- 6. INDEXES FOR COMMON SCREENS AND WORKFLOW CHECKS
-- ====================================================================
CREATE INDEX ix_users_role_status ON users(role_id, status);
CREATE INDEX ix_user_skills_user ON user_skills(user_id);
CREATE INDEX ix_series_mangaka_status ON series(mangaka_id, status);
CREATE INDEX ix_series_editor_status ON series(tantou_editor_id, status);
CREATE INDEX ix_chapters_series_status ON chapters(series_id, status);
CREATE INDEX ix_pages_chapter_status ON pages(chapter_id, status);
CREATE INDEX ix_tasks_assistant_status ON tasks(assistant_id, status);
CREATE INDEX ix_submissions_task ON submissions(task_id, submitted_at DESC);
CREATE INDEX ix_annotations_page_resolved ON annotations(page_id, resolved);
CREATE INDEX ix_reader_metrics_cycle ON reader_metrics(publication_cycle);
CREATE INDEX ix_board_votes_series ON board_votes(series_id);

-- ====================================================================
-- 7. SEED DATA (Initial structural defaults)
-- ====================================================================
INSERT INTO roles (role_name) VALUES
    ('Admin'),
    ('Mangaka'),
    ('Assistant'),
    ('Editor'),
    ('Board');

INSERT INTO permissions (action_name) VALUES
    ('MANAGE_USERS'),
    ('MANAGE_ROLES'),
    ('MANAGE_SKILLS'),
    ('SUBMIT_SERIES'),
    ('REVISE_SERIES'),
    ('REVIEW_SERIES'),
    ('SEND_TO_BOARD'),
    ('VOTE_SERIES'),
    ('CREATE_CHAPTERS'),
    ('UPLOAD_PAGES'),
    ('ASSIGN_TASKS'),
    ('SUBMIT_TASKS'),
    ('REVIEW_TASKS'),
    ('VIEW_RANKINGS'),
    ('MANAGE_READER_METRICS');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'Admin';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.action_name IN (
    'SUBMIT_SERIES',
    'REVISE_SERIES',
    'CREATE_CHAPTERS',
    'UPLOAD_PAGES',
    'ASSIGN_TASKS',
    'REVIEW_TASKS',
    'VIEW_RANKINGS'
)
WHERE r.role_name = 'Mangaka';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.action_name IN (
    'SUBMIT_TASKS'
)
WHERE r.role_name = 'Assistant';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.action_name IN (
    'REVIEW_SERIES',
    'SEND_TO_BOARD',
    'VIEW_RANKINGS'
)
WHERE r.role_name = 'Editor';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.action_name IN (
    'VOTE_SERIES',
    'VIEW_RANKINGS',
    'MANAGE_READER_METRICS'
)
WHERE r.role_name = 'Board';

INSERT INTO skills (skill_name) VALUES
    ('Background'),
    ('Inking'),
    ('Shading'),
    ('Screentone'),
    ('Effect Line'),
    ('Speech Bubble'),
    ('Cleanup'),
    ('Coloring');
