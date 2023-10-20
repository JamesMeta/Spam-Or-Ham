package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {

    // Create a SpamDetector object to perform spam detection
    SpamDetector detector = new SpamDetector();

    // Create a logger to log messages
    private static final Logger LOGGER = Logger.getLogger(SpamResource.class.getName());

    // Constructor to train and test the model on startup
    public SpamResource() {
        LOGGER.info("Training and testing the model, please wait");
        try {
            this.trainAndTest();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    // Endpoint to get the spam detection results in JSON format
    @GET
    @Path("/data")
    @Produces("application/json")
    public Response getSpamResults() throws IOException {
        // Get the test results from the SpamDetector object
        ArrayList<TestFile> testResults = this.detector.getSpamObj();

        // Convert the results to JSON format using Jackson library
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = objectMapper.writeValueAsString(testResults);

        // Return the JSON response
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(json)
                .build();
    }

    // Endpoint to get the accuracy of the spam detection model
    @GET
    @Path("/accuracy")
    @Produces("text/plain")
    public Response getAccuracy() throws IOException {
        // Get the accuracy from the SpamDetector object
        double accuracy = this.detector.getAccuracy();

        // Return the accuracy as a plain text response
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "text/plain")
                .entity(Double.toString(accuracy))
                .build();
    }

    // Endpoint to get the precision of the spam detection model
    @GET
    @Path("/precision")
    @Produces("text/plain")
    public Response getPrecision() throws IOException {
        // Get the precision from the SpamDetector object
        double precision = this.detector.getPrecision();

        // Return the precision as a plain text response
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "text/plain")
                .entity(Double.toString(precision))
                .build();
    }

    // Method to train and test the spam detection model
    private List<TestFile> trainAndTest() throws IOException {
        if (this.detector==null){
            this.detector = new SpamDetector();
        }

        // Get the main directory containing the data files
        URL resourceUrl = SpamDetector.class.getResource("/data");
        File mainDirectory = new File(resourceUrl.getFile());
        this.detector.trainAndTest(mainDirectory);
        return this.detector.getSpamObj();
    }


}