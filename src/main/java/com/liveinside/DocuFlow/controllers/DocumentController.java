package com.liveinside.DocuFlow.controllers;

import com.liveinside.DocuFlow.models.Document;
import com.liveinside.DocuFlow.repo.DocumentRepository;
import com.liveinside.DocuFlow.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.Date;
import java.util.Optional;


@Controller
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/{id}")
    public String document(@PathVariable Long id, Model model) {
        if (!documentRepository.existsById(id)) {
            return "redirect:/";
        }

        Optional<Document> optionalDocument  = documentRepository.findById(id);
        Document document = optionalDocument.get();
        model.addAttribute("document", document);
        model.addAttribute("title", "Документ: " + document.getNumber());
        return "document";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Optional<Document> optionalDocument = documentRepository.findById(id);

        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();
            return documentService.downloadDocument(document);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/update/{id}")
    public String updateDocument(@PathVariable Long id, Model model) {
        if (!documentRepository.existsById(id)) {
            return "redirect:/";
        }

        Optional<Document> optionalDocument  = documentRepository.findById(id);
        Document document = optionalDocument.get();
        model.addAttribute("document", document);
        model.addAttribute("title", "Редактирование документа: " + document.getNumber());
        return "update-document";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("document") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {

        try {
            documentService.uploadDocument(file);
            redirectAttributes.addFlashAttribute("success",
                    "Файл " + "\"" + file.getOriginalFilename() + "\""
                            + " успешно загружен");
        } catch (DuplicateKeyException | IllegalArgumentException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Загрузка файла - " + file.getOriginalFilename() + " не удалась");
        }
        return "redirect:/";
    }

    @PostMapping("/update/{id}")
    public String postUpdate(@RequestParam String name, @RequestParam String type,
                             @RequestParam String description, @RequestParam Date date,
                             @RequestParam int number, @PathVariable Long id,
                             RedirectAttributes redirectAttributes) throws IllegalArgumentException {

        if (!documentRepository.existsById(id)) {
            return "redirect:/";
        }

        try {
            documentService.updateDocument(name, type, description, date,
                    number, id);
            redirectAttributes.addFlashAttribute("success",
                    "Файл успешно изменён. Новый номер: " + number);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteDocument(RedirectAttributes redirectAttributes, @PathVariable Long id) {
        documentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Документ успешно удален");
        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchDocuments(@RequestParam("query") String query, Model model) {
        Iterable<Document> documents = documentService.searchDocuments(query);

        if (!documents.iterator().hasNext()) {
            model.addAttribute("message", "Ничего не найдено по запросу: " + query);
        } else {
            model.addAttribute("documents", documents);
        }
        return "home";
    }
}
