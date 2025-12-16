package com.example.coursemanagementsystem.entity;

import com.example.coursemanagementsystem.constants.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private Status status;

    @Column(name = "price", nullable = false)
    private Double price;   // 0 = free, >0 = paid

    @Column(name="max_capacity")
    private Integer maxCapacity;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="instructor_id")
    private User instructor;

    @OneToMany(mappedBy = "course",cascade = CascadeType.ALL)
    private List<Enrollment> enrollments=new ArrayList<>();

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt=LocalDateTime.now();
    }


}
