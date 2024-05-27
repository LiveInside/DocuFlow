package com.liveinside.DocuFlow.controllers;

import com.liveinside.DocuFlow.models.Document;
import com.liveinside.DocuFlow.repo.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



@Controller
public class MainController {
    @Autowired
    private DocumentRepository documentRepository;

    @GetMapping("/")
    public String home(Model model) {
        Iterable<Document> documents = documentRepository.findAll();
        model.addAttribute("documents", documents);
        model.addAttribute("title", "Главная страница"); // передаю в шаблон
        return "home"; // вызов шаблона
    }
}
