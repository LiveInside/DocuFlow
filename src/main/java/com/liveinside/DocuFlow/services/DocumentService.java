package com.liveinside.DocuFlow.services;

import com.liveinside.DocuFlow.encoders.FilenameEncoder;
import com.liveinside.DocuFlow.generators.GeneratorUniqNumber;
import com.liveinside.DocuFlow.models.Document;
import com.liveinside.DocuFlow.repo.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DocumentService {
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("^\\.\\w+$");

    @Autowired
    private DocumentRepository documentRepository;

    public ResponseEntity<Resource> downloadDocument(Document document) {
        ByteArrayResource resource = new ByteArrayResource(document.getData());
        String fileName = document.getName() + document.getType();
        String encodedFilename = FilenameEncoder.encodeFilename(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Transactional
    public void updateDocument(String name, String type, String description,
                               Date date, int number, Long id) throws IllegalArgumentException {

        Document document  = documentRepository.findById(id).orElseThrow();

        document.setName(name);
        document.setType(type);
        document.setDescription(description);
        document.setDate(date);
        document.setNumber(number);

        Optional<Document> existingDocumentNumber = documentRepository.findByNumberAndIdNot(number, document.getId());
        Optional<Document> existingDocumentName = documentRepository.findByNameAndIdNot(name, document.getId());
        if (existingDocumentNumber.isPresent() || existingDocumentName.isPresent()) {
            throw new DuplicateKeyException("Документ с таким номером или названием уже существует");
        }

        documentRepository.save(document);
    }

    @Transactional
    public void uploadDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Пожалуйста выберите файл для загрузки");
        }

        String documentName = file.getOriginalFilename();
        Document document = new Document();
        document.setData(file.getBytes());
        setUploadInfo(document, documentName);

        Optional<Document> existingDocument = documentRepository.findByName(document.getName());
        if (existingDocument.isPresent()) {
            throw new DuplicateKeyException("Документ с таким именем уже существует");
        }

        documentRepository.save(document);
    }

    @Transactional
    public Iterable<Document> searchDocuments(String query) {
        String criteria = determineSearchCriteria(query);

        if (criteria.equals("date")) {
            return documentRepository.findAllByDate(Date.valueOf(query));
        }
        if (criteria.equals("number")) {
            return documentRepository.findAllByNumber(Integer.parseInt(query));
        }
        if (criteria.equals("type")) {
            return documentRepository.findAllByType(query);
        }
        return documentRepository.findAllByName(query);
        }

    public void setUploadInfo(Document document, String documentName) {
        Date currentDate = new Date(System.currentTimeMillis()) ;

        String documentExtension = "";
        int dotIndex = documentName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < documentName.length() - 1) {
            documentExtension = documentName.substring(dotIndex);
        }

        if (dotIndex > 0) {
            documentName = documentName.substring(0, dotIndex);
        }

        document.setName(documentName);
        document.setType(documentExtension);
        document.setDate(currentDate);

        int documentNumber;
        GeneratorUniqNumber generatorUniqNumber = new GeneratorUniqNumber();
        do {
            documentNumber = generatorUniqNumber.generateDocNumber();
        } while (documentRepository.findByNumber(documentNumber).isPresent());

        document.setNumber(documentNumber);
    }

    // Метод для определения критерия поиска
    private String determineSearchCriteria(String query) {
        try {
            LocalDate.parse(query);
            return "date";
        } catch (DateTimeParseException e) {}

        if (query.matches("\\d+")) {
            return "number";
        }

        if (FILE_EXTENSION_PATTERN.matcher(query).matches()) {
            return "type";
        }

        return "name";
    }

}
