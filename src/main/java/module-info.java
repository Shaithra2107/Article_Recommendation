module com.example.article {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.article to javafx.fxml;
    exports com.example.article;
}