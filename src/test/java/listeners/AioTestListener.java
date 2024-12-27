package listeners;

import okhttp3.*;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.framework.TestAutomationFramework;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AioTestListener implements ITestListener {

    private static final String BASE_URL = "https://tcms.aiojiraapps.com/aio-tcms/api/v1/project";
    private static final String CREATE_RUN_ENDPOINT_TEMPLATE = BASE_URL + "/SCRUM/testcycle/{testCycleId}/testcase/{testCaseId}/testrun?createNewRun=true";
    private static final String UPLOAD_ATTACHMENT_ENDPOINT_TEMPLATE = BASE_URL + "/{jiraProjectId}/testcycle/{testCycleId}/testcase/{testCaseId}/attachment";
    private static final String AUTH_TOKEN = "AioAuth ZGE4NmMxYjctMjM2MC0zNWRhLTgzNDMtMWJmNzNiYzdlYmJkLjlhZDA4MWM1LWNlODMtNGNlYS1hOTI3LWFiOWMxMTc3OWYxMg==";
    private final Map<String, String> testCaseMap = new HashMap<>();
    private final String testCycleId = "SCRUM-CY-Adhoc";
    private final String jiraProjectId = "SCRUM";

    public AioTestListener() {
        testCaseMap.put("testValidLogin", "SCRUM-TC-1");
        testCaseMap.put("testInvalidLogin", "SCRUM-TC-2");
        testCaseMap.put("testNoCredentialLogin", "SCRUM-TC-3");
        testCaseMap.put("testCreateNewLead", "SCRUM-TC-4");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testCaseId = testCaseMap.get(result.getMethod().getMethodName());
        if (testCaseId != null) {
            createRunInAio(testCaseId, "Passed", "Test passed successfully");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testCaseId = testCaseMap.get(result.getMethod().getMethodName());
        if (testCaseId != null) {
            // Create a failed test run
            createRunInAio(testCaseId, "Failed", "Test failed: " + result.getThrowable().getMessage());

            // Capture and upload screenshot
            int retryCount = result.getMethod().getCurrentInvocationCount();
            String screenshotPath = TestAutomationFramework.captureScreenshot(result.getMethod().getMethodName(), retryCount);

            if (screenshotPath != null) {
                uploadScreenshotToAio(testCaseId, screenshotPath);
            } else {
                System.err.println("Failed to capture screenshot for Test Case: " + testCaseId);
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testCaseId = testCaseMap.get(result.getMethod().getMethodName());
        if (testCaseId != null) {
            createRunInAio(testCaseId, "Blocked", "Test skipped due to: " + result.getThrowable().getMessage());
        }
    }

    private void createRunInAio(String testCaseId, String status, String comment) {
        String endpoint = CREATE_RUN_ENDPOINT_TEMPLATE
                .replace("{testCycleId}", testCycleId)
                .replace("{testCaseId}", testCaseId);

        OkHttpClient client = createHttpClient();

        // Prepare request body
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("testCaseKey", testCaseId);
        bodyMap.put("testCaseVersion", 1);
        bodyMap.put("testRunStatus", status);
        bodyMap.put("effort", 60);
        bodyMap.put("isAutomated", true);

        if (!"Passed".equalsIgnoreCase(status)) {
            String trimmedComment = comment.split("\n")[0]; // Extract only the first line of the comment
            bodyMap.put("comments", new String[]{trimmedComment});
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String requestBody = objectMapper.writeValueAsString(bodyMap);

            RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", AUTH_TOKEN)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("Test run created successfully for Test Case: " + testCaseId + " with status: " + status);
                } else {
                    System.err.println("Failed to create test run for Test Case: " + testCaseId);
                    System.err.println("Response Code: " + response.code());
                    System.err.println("Response Body: " + response.body().string());
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating test run for Test Case: " + testCaseId);
            e.printStackTrace();
        }
    }


    private void uploadScreenshotToAio(String testCaseId, String filePath) {
        String endpoint = UPLOAD_ATTACHMENT_ENDPOINT_TEMPLATE
                .replace("{jiraProjectId}", jiraProjectId)
                .replace("{testCycleId}", testCycleId)
                .replace("{testCaseId}", testCaseId);

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Screenshot file not found: " + filePath);
            return;
        }

        System.out.println("Attempting to upload screenshot: " + filePath + " for Test Case: " + testCaseId);

        OkHttpClient client = createHttpClient();

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/png"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", AUTH_TOKEN)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Screenshot uploaded successfully for Test Case: " + testCaseId);
                System.out.println("Response Body: " + response.body().string());
            } else {
                System.err.println("Failed to upload screenshot for Test Case: " + testCaseId);
                System.err.println("Response Code: " + response.code());
                System.err.println("Response Body: " + response.body().string());
            }
        } catch (IOException e) {
            System.err.println("Error uploading screenshot for Test Case: " + testCaseId);
            e.printStackTrace();
        }
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Test Execution Started!");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("Test Execution Finished!");
    }
}
