CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    instructor_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (instructor_id) REFERENCES user(id)
);

CREATE TABLE enrollment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_status ENUM('ENROLLED', 'CANCELLED', 'COMPLETED') DEFAULT 'ENROLLED',
    enrolled_at DATETIME,
    cancelled_at DATETIME,
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (course_id) REFERENCES course(id)
);
