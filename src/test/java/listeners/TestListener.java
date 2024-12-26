package listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.example.framework.TestAutomationFramework;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("Test Started: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("Test Passed: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("Test Failed: " + result.getMethod().getMethodName());

        // Generate a retry count (or use 0 if no retry logic is used here)
        int retryCount = result.getMethod().getCurrentInvocationCount() - 1;

        // Capture the screenshot with the new method signature
        TestAutomationFramework.captureScreenshot(result.getMethod().getMethodName(), retryCount);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("Test Skipped: " + result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Test Suite Started: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("Test Suite Finished: " + context.getName());
    }
}
