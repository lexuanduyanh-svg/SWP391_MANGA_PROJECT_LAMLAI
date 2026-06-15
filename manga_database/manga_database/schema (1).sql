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
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE assistant_profiles (
    user_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    monthly_earnings DECIMAL(10, 2) DEFAULT 0.00 CHECK (monthly_earnings >= 0)
);

CREATE TABLE skills (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_skills (
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    skill_id INTEGER REFERENCES skills(skill_id) ON DELETE CASCADE,
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

    -- [ADDED 'REVISION_REQUESTED' HERE]
    CONSTRAINT chk_series_status CHECK (status IN (
        'DRAFT', 
        'SUBMITTED_TO_EDITOR', 
        'REVISION_REQUESTED', -- Sent back by Editor to Mangaka
        'UNDER_BOARD_REVIEW', 
        'APPROVED', 
        'REJECTED'
    )),
    CONSTRAINT chk_publishing_frequency CHECK (publishing_frequency IN ('WEEKLY', 'MONTHLY'))
);

CREATE TABLE chapters (
    chapter_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    chapter_number INTEGER NOT NULL,
    title VARCHAR(255),
    print_deadline TIMESTAMP,
    CONSTRAINT unique_series_chapter UNIQUE (series_id, chapter_number)
);

CREATE TABLE pages (
    page_id SERIAL PRIMARY KEY,
    chapter_id INTEGER REFERENCES chapters(chapter_id) ON DELETE CASCADE,
    page_number INTEGER NOT NULL,
    manuscript_file_path VARCHAR(512),
    CONSTRAINT unique_chapter_page UNIQUE (chapter_id, page_number)
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
    
    -- [NEW COLUMN]: Holds Mangaka's revision feedback for the Assistant
    feedback_notes TEXT, 
    
    -- [UPDATED STATUS STATES]
    CONSTRAINT chk_task_status CHECK (status IN (
        'ASSIGNED', 
        'PENDING_REVIEW', 
        'APPROVED', 
        'REVISION_REQUESTED' -- Sent back by Mangaka to Assistant
    ))
);

CREATE TABLE submissions (
    submission_id SERIAL PRIMARY KEY,
    task_id INTEGER REFERENCES tasks(task_id) ON DELETE CASCADE,
    asset_file_path VARCHAR(512) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE annotations (
    markup_id SERIAL PRIMARY KEY,
    page_id INTEGER REFERENCES pages(page_id) ON DELETE CASCADE,
    editor_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    spatial_coordinates JSONB, -- For pinning comments on a specific location
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- 4. STRATEGY & METRICS (Enforcing Business Rules)
-- ====================================================================

CREATE TABLE reader_metrics (
    metric_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    publication_cycle DATE NOT NULL,
    
    -- [UPDATED]: Added missing columns from Report 1 (LI-3) to feed the ranking service
    sales_figures INTEGER DEFAULT 0 CHECK (sales_figures >= 0),
    likes_count INTEGER DEFAULT 0 CHECK (likes_count >= 0),
    shares_count INTEGER DEFAULT 0 CHECK (shares_count >= 0),
    votes_count INTEGER DEFAULT 0 CHECK (votes_count >= 0),
    
    -- [BR-56 Guard]: Prevents negative inputs right at the database layer
    CONSTRAINT unique_series_cycle UNIQUE (series_id, publication_cycle)
);

CREATE TABLE board_votes (
    vote_id SERIAL PRIMARY KEY,
    series_id INTEGER REFERENCES series(series_id) ON DELETE CASCADE,
    board_member_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    decision VARCHAR(50) NOT NULL,
    
    -- [BR-53 Guard]: Strictly ensures a board member can only vote ONCE per series
    CONSTRAINT unique_board_member_vote UNIQUE (series_id, board_member_id),
    CONSTRAINT chk_vote_decision CHECK (decision IN ('APPROVE', 'REJECT'))
);

-- ====================================================================
-- 5. SEED DATA (Initial structural defaults)
-- ====================================================================
INSERT INTO roles (role_name) VALUES ('Admin'), ('Mangaka'), ('Assistant'), ('Editor'), ('Board');