package listeners;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetryCount = 2;

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
