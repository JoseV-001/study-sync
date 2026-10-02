CREATE TABLE books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author TEXT,
    total_pages INTEGER NOT NULL CHECK (total_pages BETWEEN 1 AND 1000000),
    current_page INTEGER NOT NULL DEFAULT 0 CHECK (current_page BETWEEN 0 AND total_pages),
    status TEXT NOT NULL CHECK (status IN ('PLANNED', 'READING', 'PAUSED', 'COMPLETED')),
    target_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_books_status ON books (status);
CREATE INDEX idx_books_target_date ON books (target_date);
