-- IRCTC Admin Dashboard schema.
-- Safe to run more than once. No sample bookings are inserted here.

CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    UNIQUE KEY uk_admins_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_status (status),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_code VARCHAR(20) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    department VARCHAR(80) NOT NULL,
    designation VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_employees_code (employee_code),
    UNIQUE KEY uk_employees_email (email),
    KEY idx_employees_status (status),
    CONSTRAINT chk_employees_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    station_code VARCHAR(10) NOT NULL,
    station_name VARCHAR(120) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state_name VARCHAR(80) NOT NULL,
    UNIQUE KEY uk_stations_code (station_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trains (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    train_number VARCHAR(10) NOT NULL,
    train_name VARCHAR(120) NOT NULL,
    source_station_id BIGINT NOT NULL,
    destination_station_id BIGINT NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    running_days VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_trains_number (train_number),
    KEY idx_trains_status (status),
    CONSTRAINT fk_trains_source FOREIGN KEY (source_station_id) REFERENCES stations (id),
    CONSTRAINT fk_trains_destination FOREIGN KEY (destination_station_id) REFERENCES stations (id),
    CONSTRAINT chk_trains_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_trains_route CHECK (source_station_id <> destination_station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS train_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    train_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    stop_order INT NOT NULL,
    arrival_time TIME NULL,
    departure_time TIME NULL,
    distance_km DECIMAL(8,1) NOT NULL DEFAULT 0.0,
    day_offset INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_schedule_order (train_id, stop_order),
    KEY idx_schedule_train (train_id),
    CONSTRAINT fk_schedule_train FOREIGN KEY (train_id) REFERENCES trains (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_station FOREIGN KEY (station_id) REFERENCES stations (id),
    CONSTRAINT chk_schedule_order CHECK (stop_order > 0),
    CONSTRAINT chk_schedule_day CHECK (day_offset >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS service_types (
    code VARCHAR(40) PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NOT NULL,
    offered TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pnr CHAR(10) NOT NULL,
    user_id BIGINT NOT NULL,
    train_id BIGINT NOT NULL,
    source_station VARCHAR(10) NOT NULL,
    destination_station VARCHAR(10) NOT NULL,
    travel_class VARCHAR(10) NOT NULL,
    service_type VARCHAR(40) NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    travel_date DATE NOT NULL,
    booking_status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bookings_pnr (pnr),
    KEY idx_bookings_date (booking_date),
    KEY idx_bookings_status (booking_status),
    KEY idx_bookings_user (user_id),
    KEY idx_bookings_train (train_id),
    KEY idx_bookings_route (source_station, destination_station),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_train FOREIGN KEY (train_id) REFERENCES trains (id),
    CONSTRAINT fk_bookings_service FOREIGN KEY (service_type) REFERENCES service_types (code),
    CONSTRAINT chk_bookings_status CHECK (booking_status IN ('CONFIRMED', 'WAITING', 'RAC', 'CANCELLED')),
    CONSTRAINT chk_bookings_class CHECK (travel_class IN ('1A', '2A', '3A', 'SL', 'CC')),
    CONSTRAINT chk_bookings_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_bookings_route CHECK (source_station <> destination_station)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NULL,
    activity_type VARCHAR(40) NOT NULL,
    message VARCHAR(255) NOT NULL,
    entity_type VARCHAR(40) NULL,
    entity_id BIGINT NULL,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_activity_created (created_at),
    KEY idx_activity_type (activity_type),
    KEY idx_activity_read (read_at, created_at),
    CONSTRAINT fk_activity_admin FOREIGN KEY (admin_id) REFERENCES admins (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key VARCHAR(80) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quick_actions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    label VARCHAR(80) NOT NULL,
    icon_name VARCHAR(40) NOT NULL,
    destination VARCHAR(160) NOT NULL,
    visible TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_classes (
    code VARCHAR(10) PRIMARY KEY,
    display_name VARCHAR(80) NOT NULL,
    color_hex VARCHAR(7) NOT NULL,
    visible TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reference data used even when there are no bookings.
INSERT IGNORE INTO service_types (code, name, description, offered, sort_order) VALUES
    ('TRAIN_TICKET', 'Train tickets', 'Reserved passenger tickets', 1, 1),
    ('TATKAL', 'Tatkal bookings', 'Emergency quota tickets', 1, 2),
    ('TOURISM', 'Tourism packages', 'Tourism packages are not offered in this module', 0, 3),
    ('CATERING', 'Onboard catering', 'Catering sales are not tracked in this module', 0, 4);

-- Existing installs created this column as VARCHAR. Banner text needs more room.
ALTER TABLE app_settings MODIFY setting_value TEXT NOT NULL;

INSERT IGNORE INTO app_settings (setting_key, setting_value) VALUES
    ('portal_name', 'IRCTC'),
    ('support_email', 'admin-desk@example.com'),
    ('page_size', '10'),
    ('banner_title', 'Welcome Back'),
    ('banner_description', 'Your dedication keeps IRCTC moving forward. Together, we serve millions.'),
    ('banner_badge', 'Employee Dashboard'),
    ('banner_image', '/assets/images/train-banner.jpg'),
    ('dashboard_activity_limit', '5');

INSERT IGNORE INTO booking_classes (code, display_name, color_hex, visible, sort_order) VALUES
    ('SL', 'Sleeper', '#16a34a', 1, 1),
    ('3A', 'AC 3 Tier', '#2563eb', 1, 2),
    ('2A', 'AC 2 Tier', '#7c3aed', 1, 3),
    ('1A', 'First Class', '#d97706', 1, 4),
    ('CC', 'Chair Car', '#64748b', 1, 5);

-- Navigation shortcuts. Counts on the dashboard still come from bookings, not from this table.
INSERT INTO quick_actions (label, icon_name, destination, visible, sort_order)
SELECT 'Create New Booking', 'ticket', '/bookings/new', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM app_settings WHERE setting_key = 'content_defaults_loaded');
INSERT INTO quick_actions (label, icon_name, destination, visible, sort_order)
SELECT 'Check PNR Status', 'search', '/bookings', 1, 2
WHERE NOT EXISTS (SELECT 1 FROM app_settings WHERE setting_key = 'content_defaults_loaded');
INSERT INTO quick_actions (label, icon_name, destination, visible, sort_order)
SELECT 'Passenger Enquiry', 'users', '/users', 1, 3
WHERE NOT EXISTS (SELECT 1 FROM app_settings WHERE setting_key = 'content_defaults_loaded');
INSERT INTO quick_actions (label, icon_name, destination, visible, sort_order)
SELECT 'Train Schedule', 'train-front', '/trains/schedule', 1, 4
WHERE NOT EXISTS (SELECT 1 FROM app_settings WHERE setting_key = 'content_defaults_loaded');
INSERT INTO quick_actions (label, icon_name, destination, visible, sort_order)
SELECT 'Generate Report', 'file-bar-chart', '/reports', 1, 5
WHERE NOT EXISTS (SELECT 1 FROM app_settings WHERE setting_key = 'content_defaults_loaded');
INSERT IGNORE INTO app_settings (setting_key, setting_value) VALUES ('content_defaults_loaded', 'true');
