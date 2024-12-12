package hooks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.io.output.TeeOutputStream;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;

import factory.BaseClass;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {
	
	WebDriver driver;
	Properties p;
	private ByteArrayOutputStream consoleStream; // To capture logs for the report
    private PrintStream originalSystemOut;      // To keep original System.out
    private PrintStream dualStream;             // Custom PrintStream for both destinations

	
	@Before
	public void setup() throws IOException {
		driver=BaseClass.initilizeBrowser();
		p=BaseClass.getProperties();
		driver.get(p.getProperty("appURL"));
		driver.manage().window().maximize();
		
		// Save the original System.out (Eclipse console output)
        originalSystemOut = System.out;

        // Create a ByteArrayOutputStream to capture console output
        consoleStream = new ByteArrayOutputStream();

        // Create a custom PrintStream that writes to both the original System.out and the ByteArrayOutputStream
        dualStream = new PrintStream(new TeeOutputStream(originalSystemOut, consoleStream));

        // Redirect System.out to the custom PrintStream
        System.setOut(dualStream);
	}
	
	@After
	public void tearDown(Scenario scenario) {
        
        // Attach the captured console logs to the Cucumber report
        String consoleLogs = consoleStream.toString();
        scenario.attach(consoleLogs.getBytes(), "text/plain", "Console Output");
        ExtentCucumberAdapter.addTestStepLog("<b>Print Statement Results:</b><br><pre>" + consoleLogs + "</pre>");

        //scenario.attach("<b>Print Statement Results:</b><br><pre>" + consoleLogs + "</pre>");


        // Restore the original System.out
        System.setOut(originalSystemOut);
		driver.quit();
	}

	@AfterStep
	public void addScreenshot(Scenario scenario) {
		if (scenario.isFailed()) {
		TakesScreenshot ts = (TakesScreenshot)driver;
		byte[] screeshot=ts.getScreenshotAs(OutputType.BYTES);
		scenario.attach(screeshot, "image/png", scenario.getName());
	}
	}
}
