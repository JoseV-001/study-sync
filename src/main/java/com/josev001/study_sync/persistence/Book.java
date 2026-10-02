package com.josev001.study_sync.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(length = 256)
    private String author;

    @Column(name = "total_pages", nullable = false)
    private int totalPages;

    @Column(name = "current_page", nullable = false)
    private int currentPage;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Book() {
    }

    public Book(String title, String author, int totalPages, int currentPage, String status,
                LocalDate targetDate, Instant createdAt, Instant updatedAt) {
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.status = status;
        this.targetDate = targetDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(String title, String author, int totalPages, int currentPage, String status,
                       LocalDate targetDate, Instant updatedAt) {
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.status = status;
        this.targetDate = targetDate;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalPages() { return totalPages; }
    public int getCurrentPage() { return currentPage; }
    public String getStatus() { return status; }
    public LocalDate getTargetDate() { return targetDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
