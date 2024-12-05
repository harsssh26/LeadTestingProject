package listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.framework.TestAutomationFramework;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AioTestListener implements ITestListener {

    private static final String UPLOAD_RESULTS_ENDPOINT = "https://tcms.aiojiraapps.com/aio-tcms/api/v1/project/SCRUM/testcycle/SCRUM-CY-Adhoc/import/results?type=TestNG";
    private static final String MEDIA_UPLOAD_ENDPOINT_TEMPLATE = "https://tcms.aiojiraapps.com/aio-tcms/api/v1/project/SCRUM//testcycle/SCRUM-CY-Adhoc/testrun/{testRunId}/attachment";
    private static final String AUTH_TOKEN = "AioAuth NDAwYzc4NTQtNWNmNy0zMDU3LWIzYWItYTdhOGU3NzFiNTBmLmNiMzEzMTM5LTkzNGItNGY3OC1hZTUzLTc5ZDRjZDM5YmIzYQ==";
    private static final String FINAL_RESULTS_FILE_PATH = "target/surefire-reports/testng-results.xml";

    private BufferedWriter writer;
    private final Map<String, Integer> runIdMap = new HashMap<>();
    private final Map<String, File> failedTestScreenshots = new HashMap<>();

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Increased connection timeout
                .readTimeout(60, TimeUnit.SECONDS)     // Increased read timeout
                .writeTimeout(60, TimeUnit.SECONDS)    // Increased write timeout
                .build();
    }

    @Override
    public void onStart(ITestContext context) {
        new File("target/surefire-reports").mkdirs();
        try {
            writer = new BufferedWriter(new FileWriter(FINAL_RESULTS_FILE_PATH));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<testng-results ignored=\"0\" total=\"0\" passed=\"0\" failed=\"0\" skipped=\"0\">\n");
            writer.write("<suite>\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        TestAutomationFramework.captureScreenshot(testName);

        File screenshotFile = new File("screenshots/" + testName + ".png");
        if (screenshotFile.exists()) {
            failedTestScreenshots.put(testName, screenshotFile);
        } else {
            System.err.println("Screenshot not found: " + screenshotFile.getAbsolutePath());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        try {
            writer.close();

            String jsonResponse = uploadTestResults(new File(FINAL_RESULTS_FILE_PATH));
            if (jsonResponse != null) {
                populateRunIdMap(jsonResponse);
                for (Map.Entry<String, File> entry : failedTestScreenshots.entrySet()) {
                    uploadScreenshot(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String uploadTestResults(File testResultsFile) {
        OkHttpClient client = createHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", testResultsFile.getName(),
                        RequestBody.create(testResultsFile, MediaType.parse("application/xml")));
        Request request = new Request.Builder()
                .url(UPLOAD_RESULTS_ENDPOINT)
                .addHeader("Authorization", AUTH_TOKEN)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                logErrorDetails(response);
            }
        } catch (IOException e) {
            System.err.println("Failed to upload test results due to: " + e.getMessage());
        }
        return null;
    }

    private void populateRunIdMap(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(jsonResponse, Map.class);
            Map<String, Map<String, Object>> processedData = (Map<String, Map<String, Object>>) responseMap.get("processedData");
            if (processedData != null) {
                for (Map.Entry<String, Map<String, Object>> entry : processedData.entrySet()) {
                    String testName = (String) entry.getValue().get("name");
                    List<Integer> runIds = (List<Integer>) entry.getValue().get("runId");
                    if (runIds != null && !runIds.isEmpty()) {
                        runIdMap.put(testName, runIds.get(0));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadScreenshot(String testName, File screenshotFile) {
        Integer runId = runIdMap.get(testName);
        if (runId == null) {
            System.err.println("No runId found for testName: " + testName);
            return;
        }

        String uploadEndpoint = MEDIA_UPLOAD_ENDPOINT_TEMPLATE.replace("{testRunId}", runId.toString());
        OkHttpClient client = createHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", screenshotFile.getName(),
                        RequestBody.create(screenshotFile, MediaType.parse("image/png")));
        Request request = new Request.Builder()
                .url(uploadEndpoint)
                .addHeader("Authorization", AUTH_TOKEN)
                .post(builder.build())
                .build();

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("Screenshot uploaded successfully for testName: " + testName);
                    return;
                } else {
                    logErrorDetails(response);
                }
            } catch (IOException e) {
                System.err.println("Attempt " + attempt + " failed for testName: " + testName + " due to: " + e.getMessage());
            }
        }

        System.err.println("Failed to upload screenshot for testName: " + testName + " after " + maxRetries + " attempts.");
    }

    private void logErrorDetails(Response response) throws IOException {
        if (response != null && response.body() != null) {
            System.err.println("Error Response Code: " + response.code());
            System.err.println("Error Response Body: " + response.body().string());
        }
    }
}
