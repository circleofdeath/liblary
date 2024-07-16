package com.liblary.liblary.repo;

import com.liblary.liblary.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepo extends JpaRepository<BookEntity, Long>
{
    List<BookEntity> findByAuthor(String author);
    List<BookEntity> findByTitle(String title);
}