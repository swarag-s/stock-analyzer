package com.stockanalyzer.disclaimer;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class disclaimer {
    public static boolean showAndWait() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Market Risk Disclaimer");
        alert.setHeaderText("Welcome to the Stock Market Analyzer");
        alert.setContentText(
                "Trading and investing in financial markets involves substantial risk of loss and is not suitable for every investor. " +
                        "All information provided by this tool is for educational and analytical purposes only and should not be considered financial advice.\n\n" +
                        "By clicking 'Agree', you acknowledge these risks and agree to use this tool at your own discretion."
        );

        // Add "Agree" and "Disagree" buttons
        ButtonType agreeButton = new ButtonType("Agree");
        ButtonType disagreeButton = new ButtonType("Disagree");

        alert.getButtonTypes().setAll(agreeButton, disagreeButton);



        // Show the dialog and wait for the user to click a button
        Optional<ButtonType> result = alert.showAndWait();

        // Return true only if the user clicked the "Agree" button
        return result.isPresent() && result.get() == agreeButton;
    }
}
