CREATE DATABASE IF NOT EXISTS fitness_tracking
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitness_tracking;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  activity_date DATE NOT NULL,
  activity_type VARCHAR(80) NOT NULL,
  duration_minutes INT NOT NULL DEFAULT 0,
  calories INT NOT NULL DEFAULT 0,
  steps INT NOT NULL DEFAULT 0,
  notes VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS goals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_type VARCHAR(50) NOT NULL,
  target_value INT NOT NULL,
  current_value INT NOT NULL DEFAULT 0,
  target_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_activities_user_date ON activities(user_id, activity_date);
CREATE INDEX idx_goals_user ON goals(user_id);
