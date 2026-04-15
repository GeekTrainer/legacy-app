DROP TABLE IF EXISTS assignment_history;
DROP TABLE IF EXISTS assets;
DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
  employee_id INTEGER PRIMARY KEY AUTOINCREMENT,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  email TEXT NOT NULL,
  department TEXT NOT NULL,
  title TEXT,
  hire_date TEXT,
  is_active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE assets (
  asset_id INTEGER PRIMARY KEY AUTOINCREMENT,
  asset_tag TEXT NOT NULL,
  asset_type TEXT NOT NULL,
  manufacturer TEXT,
  model TEXT,
  serial_number TEXT,
  purchase_date TEXT,
  warranty_expiry TEXT,
  status TEXT NOT NULL,
  notes TEXT
);

CREATE TABLE assignment_history (
  assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
  asset_id INTEGER NOT NULL,
  employee_id INTEGER NOT NULL,
  assigned_date TEXT,
  returned_date TEXT,
  assigned_by TEXT,
  notes TEXT,
  FOREIGN KEY(asset_id) REFERENCES assets(asset_id),
  FOREIGN KEY(employee_id) REFERENCES employees(employee_id)
);
