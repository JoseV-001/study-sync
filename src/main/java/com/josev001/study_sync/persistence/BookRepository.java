package com.josev001.study_sync.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAllByOrderByStatusAscTargetDateAscTitleAsc();
}
