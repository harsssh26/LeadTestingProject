package listeners;

import okhttp3.*;
import org.testng.ITestContext;
import org.testng.ITestListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AioTestListener implements ITestListener {

    private static final String UPLOAD_ENDPOINT = "https://tcms.aiojiraapps.com/aio-tcms/api/v1/project/SCRUM/testcycle/SCRUM-CY-Adhoc/import/results";
    private static final String AUTH_TOKEN = "AioAuth NDAwYzc4NTQtNWNmNy0zMDU3LWIzYWItYTdhOGU3NzFiNTBmLmNiMzEzMTM5LTkzNGItNGY3OC1hZTUzLTc5ZDRjZDM5YmIzYQ==";
    private static final String TEST_RESULTS_FILE_PATH = "target/surefire-reports/testng-results.xml";

    @Override
    public void onStart(ITestContext context) {
        File reportsDir = new File("target/surefire-reports");
        if (!reportsDir.exists()) {
            boolean created = reportsDir.mkdirs();
            if (created) {
                System.out.println("Created surefire-reports directory.");
            } else {
                System.err.println("Failed to create surefire-reports directory.");
            }
        }

        File resultsFile = new File(TEST_RESULTS_FILE_PATH);
        try {
            if (resultsFile.createNewFile()) {
                System.out.println("Created placeholder testng-results.xml file.");
                try (FileWriter writer = new FileWriter(resultsFile)) {
                    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testng-results></testng-results>");
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating placeholder testng-results.xml file: " + e.getMessage());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        File testResultsFile = new File(TEST_RESULTS_FILE_PATH);

        if (!testResultsFile.exists()) {
            System.err.println("Test results file not found: " + testResultsFile.getAbsolutePath());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", testResultsFile.getName(),
                        RequestBody.create(testResultsFile, MediaType.parse("application/xml")))
                .build();

        HttpUrl.Builder httpBuilder = HttpUrl.parse(UPLOAD_ENDPOINT).newBuilder();
        httpBuilder.addQueryParameter("type", "TestNG");

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .addHeader("Authorization", AUTH_TOKEN)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Test results uploaded successfully!");
            } else {
                System.err.println("Failed to upload test results.");
                System.err.println("Response Code: " + response.code());
                System.err.println("Response Body: " + response.body().string());
            }
        } catch (IOException e) {
            System.err.println("An error occurred while uploading the test results.");
            e.printStackTrace();
        }
    }

    @Override
    public void onTestStart(org.testng.ITestResult result) {}

    @Override
    public void onTestSuccess(org.testng.ITestResult result) {}

    @Override
    public void onTestFailure(org.testng.ITestResult result) {}

    @Override
    public void onTestSkipped(org.testng.ITestResult result) {}

    @Override
    public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult result) {}
}
