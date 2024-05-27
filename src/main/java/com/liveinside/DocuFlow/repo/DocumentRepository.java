package com.liveinside.DocuFlow.repo;

import com.liveinside.DocuFlow.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByName(String name);
    Optional<Document> findByNumber(int number);
    Optional<Document> findByNumberAndIdNot(int number, Long id);
    Optional<Document> findByNameAndIdNot(String name, Long id);

    Iterable<Document> findAllByDate(Date date);
    Iterable<Document> findAllByType(String type);
    Iterable<Document> findAllByName(String name);
    Iterable<Document> findAllByNumber(int number);
}
