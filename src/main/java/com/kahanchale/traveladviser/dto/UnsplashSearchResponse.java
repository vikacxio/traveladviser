package com.kahanchale.traveladviser.dto;


public class UnsplashSearchResponse {
    private int total;
    private int total_pages;
    private UnsplashPhotoDTO[] results;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    public UnsplashPhotoDTO[] getResults() {
        return results;
    }

    public void setResults(UnsplashPhotoDTO[] results) {
        this.results = results;
    }
}