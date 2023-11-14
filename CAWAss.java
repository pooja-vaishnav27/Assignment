import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.time.Duration;
import java.util.List;

public class CAWAss {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Set the path to your ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\PoojaVaishnav\\chromedriver-win64\\chromedriver.exe");

        // Initialize WebDriver
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

        // Navigate to the URL
        driver.get("https://testpages.herokuapp.com/styled/tag/dynamic-table.html");

        // Expand the collapsible element
        driver.findElement(By.xpath("//summary")).click();
    }

    @Test
    public void testDynamicTableWithDataFromJson() {
        // Read JSON content from file as a JsonArray
        String jsonFilePath = "C:\\Users\\PoojaVaishnav\\Desktop\\file.json";
        JsonArray jsonArray = readJsonFile(jsonFilePath);

        // Find the text box element
        WebElement textBox = driver.findElement(By.id("jsondata"));
        textBox.clear();

        // Paste JSON data into the text box
        pasteJsonArrayIntoTextBox(jsonArray, textBox);

        // press Enter to trigger an action
        textBox.sendKeys(Keys.ENTER);

        // Click on the "Refresh Table" button
        driver.findElement(By.id("refreshtable")).click();

        // Perform assertions
        assertInsertedValuesMatchJsonArray(jsonArray);
    }

    @AfterMethod
    public void tearDown() {
        // Close the browser
        if (driver != null) {
            driver.quit();
        }
    }

    private JsonArray readJsonFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(reader);

            if (jsonElement.isJsonArray()) {
                return jsonElement.getAsJsonArray();
            } else {
                throw new RuntimeException("The content of the file is not a JSON array");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading JSON file");
        }
    }

    private void pasteJsonArrayIntoTextBox(JsonArray jsonArray, WebElement textBox) {
        StringBuilder jsonAsString = new StringBuilder("[");
        for (JsonElement element : jsonArray) {
            jsonAsString.append(element).append(",");
        }
        // Remove the trailing comma and close the JSON array
        jsonAsString.deleteCharAt(jsonAsString.length() - 1);
        jsonAsString.append("]");

        // Paste the formatted JSON data into the text box
        textBox.sendKeys(jsonAsString.toString());
    }

    private void assertInsertedValuesMatchJsonArray(JsonArray jsonArray) {
        // Wait for the table to be present
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tablehere")));

        // Get the rows from the table directly
        List<WebElement> rows = table.findElements(By.xpath(".//tr"));

        // Check if there are any rows in the table
        if (rows.isEmpty()) {
            throw new RuntimeException("No rows found in the table.");
        }

        // Iterate through the rows
        for (int i = 0; i < Math.min(rows.size(), jsonArray.size()); i++) {
            WebElement row = rows.get(i);

            // Check if the row has any columns
            List<WebElement> columns = row.findElements(By.xpath(".//td"));

            // Check if the row has the expected number of columns
            if (columns.size() < 3) {
                System.out.println("Row " + i + " does not have enough columns. Skipping assertion for this row.");
                continue;
            }

            // Handle the header row separately
            if (i == 0) {
                // Check if the header row has the expected column names
                assert columns.get(0).getText().equals("Name") : "Header mismatch for Name column";
                assert columns.get(1).getText().equals("Age") : "Header mismatch for Age column";
                assert columns.get(2).getText().equals("Gender") : "Header mismatch for Gender column";
                continue;
            }

            // Extract values from the table
            String nameFromTable = columns.get(0).getText();
            String ageFromTable = columns.get(1).getText();
            String genderFromTable = columns.get(2).getText();

            // Extract expected values from the JSON array
            JsonElement jsonElement = jsonArray.get(i - 1); // Adjust index to account for header row
            String nameFromJson = jsonElement.getAsJsonObject().get("name").getAsString();
            String ageFromJson = jsonElement.getAsJsonObject().get("age").getAsString();
            String genderFromJson = jsonElement.getAsJsonObject().get("gender").getAsString();

          
            // Assert that values match
            assert nameFromTable.equals(nameFromJson) : "Name mismatch at row " + i;
            assert ageFromTable.equals(ageFromJson) : "Age mismatch at row " + i;
            assert genderFromTable.equals(genderFromJson) : "Gender mismatch at row " + i;
        }
    }
}