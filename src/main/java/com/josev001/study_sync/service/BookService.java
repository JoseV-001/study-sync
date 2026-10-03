package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.BookDto;
import com.josev001.study_sync.dto.BookRequest;
import com.josev001.study_sync.persistence.Book;
import com.josev001.study_sync.persistence.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
public class BookService {

    private static final Clock CLOCK = Clock.system(ZoneId.of("America/Sao_Paulo"));
    private static final List<String> STATUSES = List.of("PLANNED", "READING", "PAUSED", "COMPLETED");

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookDto> findAll() {
        return bookRepository.findAllByOrderByStatusAscTargetDateAscTitleAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BookDto save(BookRequest request) {
        validate(request);
        Instant now = Instant.now(CLOCK);
        Book book = new Book(
                request.title().trim(), clean(request.author()), request.totalPages(), request.currentPage(), request.weeklyPageGoal(),
                normalizeStatus(request.status()), request.targetDate(), now, now
        );
        return toDto(bookRepository.save(book));
    }

    @Transactional
    public BookDto update(Long id, BookRequest request) {
        validate(request);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro nao encontrado."));
        book.update(
                request.title().trim(), clean(request.author()), request.totalPages(), request.currentPage(), request.weeklyPageGoal(),
                normalizeStatus(request.status()), request.targetDate(), Instant.now(CLOCK)
        );
        return toDto(book);
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Livro nao encontrado.");
        }
        bookRepository.deleteById(id);
    }

    private void validate(BookRequest request) {
        if (request.currentPage() > request.totalPages()) {
            throw new IllegalArgumentException("A pagina atual nao pode ser maior que o total de paginas.");
        }
        normalizeStatus(request.status());
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Status de livro invalido.");
        }
        return normalized;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BookDto toDto(Book book) {
        int progress = (int) Math.round((book.getCurrentPage() * 100.0) / book.getTotalPages());
        int remainingPages = Math.max(book.getTotalPages() - book.getCurrentPage(), 0);
        int estimatedWeeks = book.getWeeklyPageGoal() > 0
                ? (int) Math.ceil(remainingPages / (double) book.getWeeklyPageGoal())
                : 0;
        return new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getTotalPages(),
                book.getCurrentPage(), book.getWeeklyPageGoal(), remainingPages, estimatedWeeks,
                Math.min(progress, 100), book.getStatus(), book.getTargetDate(), book.getUpdatedAt());
    }
}
