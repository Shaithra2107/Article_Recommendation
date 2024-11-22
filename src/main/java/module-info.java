module com.example.article {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;


    opens com.example.article to javafx.fxml;
    exports com.example.article;
}