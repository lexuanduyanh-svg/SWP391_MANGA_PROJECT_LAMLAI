-- ====================================================================
-- CLEANUP (Allows you to safely re-run this script)
-- ====================================================================
DROP TABLE IF EXISTS board_votes CASCADE;
DROP TABLE IF EXISTS reader_metrics CASCADE;
DROP TABLE IF EXISTS annotations CASCADE;
DROP TABLE IF EXISTS submissions CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS pages CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS series_decisions CASCADE;
DROP TABLE IF EXISTS series CASCADE;
DROP TABLE IF EXISTS proposals CASCADE;
DROP TABLE IF EXISTS user_skills CASCADE;
DROP TABLE IF EXISTS skills CASCADE;
DROP TABLE IF EXISTS assistant_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

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
    role_id INTEGER REFERENCES roles(role_id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE assistant_profiles (
    user_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    monthly_earnings DECIMAL(10, 2) DEFAULT 0.00 CHECK (monthly_earnings >= 0)
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
-- 2. PROPOSALS (Flow 1: Pitch & Review)
-- ====================================================================

CREATE TABLE proposals (
    proposal_id SERIAL PRIMARY KEY,
    mangaka_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    tantou_editor_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    target_audience VARCHAR(100),
    synopsis TEXT,
    manuscript_title VARCHAR(255),
    manuscript_summary TEXT,
    manuscript_file_name VARCHAR(512),
    manuscript_version INTEGER,
    manuscript_uploaded_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'DRAFT',
    editor_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_proposal_status CHECK (status IN (
        'DRAFT',
        'SUBMITTED_TO_EDITOR',
        'REVISION_REQUESTED',
        'UNDER_BOARD_REVIEW',
        'APPROVED',
        'REJECTED'
    ))
);

-- ====================================================================
-- 3. SERIES (Flow 2: Production — created AFTER proposal approval)
-- ====================================================================

CREATE TABLE series (
    series_id SERIAL PRIMARY KEY,
    proposal_id INTEGER REFERENCES proposals(proposal_id) ON DELETE SET NULL,
    mangaka_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    tantou_editor_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    synopsis TEXT,
    publishing_frequency VARCHAR(50) DEFAULT 'WEEKLY',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_series_status CHECK (status IN (
        'ACTIVE', 'HIATUS', 'COMPLETED', 'CANCELLED'
    )),
    CONSTRAINT chk_publishing_frequency CHECK (publishing_frequency IN ('WEEKLY', 'MONTHLY'))
);

-- ====================================================================
-- 4. PRODUCTION WORKFLOW (Chapters / Pages / Tasks)
-- ====================================================================

CREATE TABLE chapters (
    chapter_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    chapter_number INTEGER NOT NULL,
    title VARCHAR(255),
    status VARCHAR(50) DEFAULT 'Draft',
    print_deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_series_chapter UNIQUE (series_id, chapter_number),
    CONSTRAINT chk_chapter_status CHECK (status IN (
        'DRAFT', 'IN_PROGRESS', 'COMPLETED'
    ))
);

CREATE TABLE pages (
    page_id SERIAL PRIMARY KEY,
    chapter_id INTEGER REFERENCES chapters(chapter_id) ON DELETE CASCADE,
    page_number INTEGER NOT NULL,
    manuscript_file_path VARCHAR(512),
    status VARCHAR(50) DEFAULT 'Uploaded',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_chapter_page UNIQUE (chapter_id, page_number)
);

CREATE TABLE tasks (
    task_id SERIAL PRIMARY KEY,
    page_id INTEGER REFERENCES pages(page_id) ON DELETE CASCADE,
    assistant_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    deadline TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ASSIGNED',
    feedback_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_task_status CHECK (status IN (
        'ASSIGNED',
        'IN_PROGRESS',
        'PENDING_REVIEW',
        'APPROVED',
        'REVISION_REQUESTED'
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
-- 5. STRATEGY & METRICS
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

-- ====================================================================
-- 6. BOARD VOTES (on proposals, not on series)
-- ====================================================================

CREATE TABLE board_votes (
    vote_id SERIAL PRIMARY KEY,
    proposal_id INTEGER REFERENCES proposals(proposal_id) ON DELETE CASCADE,
    board_member_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    decision VARCHAR(50) NOT NULL,
    CONSTRAINT unique_board_member_vote UNIQUE (proposal_id, board_member_id),
    CONSTRAINT chk_vote_decision CHECK (decision IN ('APPROVE', 'REJECT'))
);

-- ====================================================================
-- 7. SERIES DECISIONS (strategic decisions by board on active series)
-- ====================================================================

CREATE TABLE series_decisions (
    series_decision_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    board_member_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    decision_type VARCHAR(50) NOT NULL CHECK (decision_type IN ('MAINTAIN', 'RESCHEDULE', 'CANCEL', 'CHANGE_FORMAT')),
    reason TEXT,
    new_frequency VARCHAR(50),
    new_format VARCHAR(50),
    decided_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- 8. SEED DATA
-- ====================================================================
INSERT INTO roles (role_name) VALUES ('Admin'), ('Mangaka'), ('Assistant'), ('Editor'), ('Board');
