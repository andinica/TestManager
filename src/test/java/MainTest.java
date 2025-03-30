import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MainTest {

  WebDriver driver;

  @BeforeClass
  public void setup() {
    driver = new FirefoxDriver();
  }

  @Test
  public void Test1() {
    driver.get("https://google.com");
  }
}
