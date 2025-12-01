package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.constants.Status;
import com.example.coursemanagementsystem.dto.AddCourseDto;
import com.example.coursemanagementsystem.dto.ApiResponseDto;
import com.example.coursemanagementsystem.dto.CourseDto;
import com.example.coursemanagementsystem.dto.UpdateCourseDto;
import com.example.coursemanagementsystem.entity.Course;
import com.example.coursemanagementsystem.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstructorService {

    @Autowired
    private CourseRepository courseRepository;

    public ApiResponseDto<Void> addCourse(@Valid AddCourseDto addCourseDto, User instructor) {
        Course course=new Course();
        course.setTitle(addCourseDto.getTitle());
        course.setDescription(addCourseDto.getDescription());
        course.setInstructor(instructor);
        course.setStatus(Status.ACTIVE);
        courseRepository.save(course);
        return new ApiResponseDto<>(true,"Course added successfully",null);

    }

    public ApiResponseDto<CourseDto> updateCourse(@Valid UpdateCourseDto updateCourseDto,Long courseId, User instructor) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new RuntimeException("Course Not Found Exception"));

        if(course.getStatus()==Status.INACTIVE)
            throw new RuntimeException("cannot update inactive course");

        if(!course.getInstructor().getId().equals(instructor.getId()))
            throw new RuntimeException("you are not authorised to update other courses");

        course.setTitle(updateCourseDto.getTitle());
        course.setDescription(updateCourseDto.getDescription());
        courseRepository.save(course);
        CourseDto courseDto=new CourseDto();
        courseDto.setId(course.getId());
        courseDto.setTitle(course.getTitle());
        courseDto.setDescription(course.getDescription());
        courseDto.setCreatedAt(course.getCreatedAt());
        courseDto.setUpdatedAt(course.getUpdatedAt());
        return new ApiResponseDto<>(true,"Updated course successfully",courseDto);
    }

    public ApiResponseDto<Void> deleteCourse(Long courseId,User instructor) {
        Course course=courseRepository.findById(courseId)
                .orElseThrow(()->new RuntimeException("Course Not Found"));

        if(!course.getInstructor().getId().equals(instructor.getId()))
            throw new RuntimeException("You cannot delete others courses");

        course.setStatus(Status.INACTIVE);
        courseRepository.save(course);
        return new ApiResponseDto<>(true,"Course deleted successfully",null);
    }

    public ApiResponseDto<List<CourseDto>> getAllCourses(User instructor) {
        List<Course> courses=courseRepository.findByInstructorId(instructor.getId());

        List<CourseDto> courseDtos=courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ApiResponseDto<>(true,"fetched courses of a particular instructor",courseDtos);


    }

    private CourseDto convertToDto(Course course){
       return CourseDto.builder()
               .id(course.getId())
               .title(course.getTitle())
               .description(course.getDescription())
               .createdAt(course.getCreatedAt())
               .updatedAt(course.getUpdatedAt())
               .build();
    }
}
