package listeners;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult; // ITestResult provides details about the test results
public class RetryAnalyzer implements IRetryAnalyzer // Here IRetryAnalyzer is a TestNG interface that allows defining retry logic for failed test cases
{
    private int retryCount = 0;
    private static final int maxRetryCount = 1;

    @Override
    public boolean retry(ITestResult result) {
        String methodName=result.getMethod().getMethodName();
        if(methodName.equals("testValidLogin"))// Skip Retry for testValidLogin
        {
            return false;
        }
        if (retryCount < maxRetryCount) {
            retryCount++;
            System.out.println("Retrying test: " + result.getMethod().getMethodName() + " | Attempt: " + (retryCount + 1));
            return true;
        }
        return false;
    }
}
