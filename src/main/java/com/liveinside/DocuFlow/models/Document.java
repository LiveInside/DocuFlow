package com.liveinside.DocuFlow.models;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Entity
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name, description, type;
    private Date date;
    @Column(unique = true)
    private int number;

    @Lob
    private byte[] data;

}
