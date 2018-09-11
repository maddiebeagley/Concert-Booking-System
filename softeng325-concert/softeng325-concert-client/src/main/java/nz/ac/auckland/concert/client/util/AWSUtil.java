package nz.ac.auckland.concert.client.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AWSUtil {

    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Download directory - a directory named "images" in the user's home
    // directory.
    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String USER_DIRECTORY = System
            .getProperty("user.home");
    private static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";


    public static Image downloadImageFromAWS(String imageName) {

        File downloadDirectory = new File(DOWNLOAD_DIRECTORY);

        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdir();
        }

        //make a new file to store the image
        File imageFile = new File(downloadDirectory, imageName);
        if (imageFile.exists()) {
            return new ImageIcon(imageFile.toString()).getImage();
        }

        //the file has not yet been downloaded so needs to be retrieved from AWS
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();


        GetObjectRequest req = new GetObjectRequest(AWS_BUCKET, imageName);
        s3.getObject(req, imageFile);

        return new ImageIcon(imageFile.toString()).getImage();
    }

}

