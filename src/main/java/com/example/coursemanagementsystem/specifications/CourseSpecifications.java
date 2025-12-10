package com.example.coursemanagementsystem.specifications;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.entity.Course;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CourseSpecifications {

    //Title filter
    public static Specification<Course> hasTitle(String title) {
        return (root, query, cb) ->
                title == null || title.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    //Instructor filter
    public static Specification<Course> hasInstructorName(String instructorName) {
        return (root, query, cb) -> {
            if (instructorName == null || instructorName.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("instructor").get("name")), "%" + instructorName.toLowerCase() + "%");};
    }


    //description filter
    public static Specification<Course> hasdescription(String description){
        return (root,query,cb)->
                description==null||description.isBlank()
                        ?null
                        :cb.like(cb.lower(root.get("description")),"%"+description.toLowerCase()+"%");
    }

    //Status filter
    public static Specification<Course> hasStatus(Status status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    //Data range filter
    public static Specification<Course> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            if (from != null && to != null)
                return cb.between(root.get("createdAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    //Global search-> searches title,description,instructor name
    public static Specification<Course> globalSearch(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String search = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), search),
                    cb.like(cb.lower(root.get("description")), search)

            );
        };
    }



}
