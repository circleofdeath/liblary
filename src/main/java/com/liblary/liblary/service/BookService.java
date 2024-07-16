package com.liblary.liblary.service;

import com.liblary.lib.dto.BookDTO;
import com.liblary.liblary.entity.BookEntity;
import com.liblary.liblary.repo.BookRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService
{
    @Autowired
    BookRepo repo;

    public BookDTO get(BookEntity entity)
    {
        BookDTO dto = new BookDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAuthor(entity.getAuthor());
        return dto;
    }

    public void removeBook(BookEntity entity)
    {
        repo.delete(entity);
    }

    public BookEntity saveBook(BookEntity entity)
    {
        return repo.save(entity);
    }

    public List<BookEntity> getAllBooks()
    {
        return repo.findAll();
    }

    public List<BookEntity> findByTitle(String title)
    {
        return repo.findByTitle(title);
    }

    public List<BookEntity> findByAuthor(String author)
    {
        return repo.findByAuthor(author);
    }

    public BookEntity getBookById(Long id)
    {
        return repo.findById(id).orElse(null);
    }
}