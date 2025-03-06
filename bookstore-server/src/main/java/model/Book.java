package model;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Book implements Serializable {
    private String isbn;
    private String title;
    private String author;
    private int year;
    private double price;
    private int quantity;

    // Required no-arg constructor for JAXB
    public Book() {}

    // Parameterized constructor
    public Book(String isbn, String title, String author, int year, double price, int quantity) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}