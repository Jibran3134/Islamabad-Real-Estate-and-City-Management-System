module com.myapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;

    opens com.myapp.controller to javafx.fxml;
    opens com.myapp.model to javafx.base;
    opens com.myapp.launcher to javafx.fxml;

    exports com.myapp;
    exports com.myapp.launcher;
    exports com.myapp.controller;
    exports com.myapp.model;
    exports com.myapp.service;
    exports com.myapp.repository;
    exports com.myapp.adapter;
}
