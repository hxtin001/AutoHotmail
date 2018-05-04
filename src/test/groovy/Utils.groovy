import geb.error.GebException
import groovy.util.logging.Log4j
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.chrome.ChromeDriver
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

@Log4j
class Utils {

    private static final String DESTINATION_FOLDER = "target/reports/HotmailSpec/"
    private static final String HTML2CAVAS_PATH = "libs/html2canvas.js"

    static File getDriver()
            throws URISyntaxException, ZipException, IOException {
        URI jarUri = HotmailRegisterMain.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        return new File(getFile(jarUri, getDriverName()))
    }

    private static URI getFile(final URI jarUri,
                               final String fileName) throws ZipException, IOException {

        final URI fileURI

        final File location = new File(jarUri)
        if (location.isDirectory()) {
            fileURI = URI.create(jarUri.toString() + fileName)
        } else {
            final ZipFile zipFile

            zipFile = new ZipFile(location)

            try {
                fileURI = readExecutableFile(zipFile, fileName)
            }
            finally {
                zipFile.close()
            }
        }

        return (fileURI)
    }

    private static URI readExecutableFile(final ZipFile zipFile,
                                          final String fileName)
            throws IOException {

        final File tempFile = File.createTempFile(fileName, Long.toString(System.currentTimeMillis()))
        tempFile.deleteOnExit()
        tempFile.setExecutable(true)
        final ZipEntry zipEntry = zipFile.getEntry(fileName)

        if (zipEntry == null) {
            throw new FileNotFoundException("Cannot find file: ${fileName} in ${zipFile.getName()}")
        }

        final InputStream zipStream = zipFile.getInputStream(zipEntry)
        OutputStream fileStream = null

        try {
            final byte[] buffer
            int i

            fileStream = new FileOutputStream(tempFile)
            buffer = new byte[1024]
            i = 0

            while ((i = zipStream.read(buffer)) != -1) {
                fileStream.write(buffer, 0, i)
            }
        }
        finally {
            close(zipStream)
            close(fileStream)
        }

        return (tempFile.toURI())
    }

    private static void close(final Closeable stream) {
        if (stream != null) {
            try {
                stream.close()
            }
            catch (final IOException ex) {
                log.error(ex.getStackTrace(), ex)
            }
        }
    }

    private static String getDriverName() {
        String name = ""
        if(isWindows()) {
            name = "chromedriver-windows-32bit.exe"
        } else if(isLinux()) {
            name = "chromedriver-linux-64bit"
        } else {
            name = "chromedriver-mac-64bit"
        }
        return name
    }

    private static getOSName() {
        String OS = null
        if(OS == null) {
            OS = System.getProperty("os.name").toLowerCase()
        }
        return OS
    }

    private static boolean isWindows() {
        return getOSName().startsWith("windows")
    }

    private static boolean isLinux() {
        return getOSName().startsWith("linux")
    }

    /**
     * Take screenshot full page (entire page)
     * @param driver: ChromeDriver
     * @param fileName: name of the file
     */
    static void screenshotFullPage(ChromeDriver driver, String fileName, String caseId) {
        try {
            String imgContent = Utils.getImageContent(driver)
            if (imgContent) {
                Utils.saveImage(imgContent, fileName, caseId)
            } else {
                log.error("Can not sreenshot full page. ${fileName}")
            }
        } catch (Exception e) {
            log.error("Can not screenshot full page: ${fileName}", e)
        }
    }

    /**
     * Send js library to page test and get full page image
     * Note that we need html2canvas.js libs before run
     * @param driver: ChromeDriver
     * @return image string
     */
    static getImageContent(ChromeDriver driver) throws Exception {
        def jsLib = HotmailRegisterMain.class.getResource(HTML2CAVAS_PATH).getText("UTF-8")
        // Send js library to page test
        driver.executeScript(jsLib)

        // Generate function screenshot by js
        String generateSreenShotJS = "function genScreenshot () {" +
                "var canvasImgContentDecoded;" +
                "html2canvas(document.body, { " +
                "onrendered: function (canvas) {" +
                "window.canvasImgContentDecoded = canvas.toDataURL('/tmp/image.png'); " +
                "}}); " +
                "} " +
                "genScreenshot();"
        driver.executeScript(generateSreenShotJS)
        Thread.sleep(2000)

        // Get image content from js
        def imgContent = driver.executeScript("return canvasImgContentDecoded;")
        Thread.sleep(3000)
        if (imgContent) {
            imgContent = imgContent.replace("data:image/png;base64,", "")
        }

        return imgContent
    }

    /**
     * Save image
     * @param imageContent: string
     * @param destinationFile
     */
    static void saveImage(imageContent, String fileName, String caseId) throws Exception {
        String folder = Utils.createFolder(caseId)
        String destinationFile = Utils.generateDestinationFile(folder, fileName)
        // Decode image content
        byte[] imageArray = Utils.decodeImage(imageContent)

        // Write a image byte array into file system
        FileOutputStream imageOutFile = new FileOutputStream(destinationFile)
        imageOutFile.write(imageArray)
    }

    /**
     * Create folder if folder not existed
     */
    static String createFolder(String caseId) {
        String testCaseDir = "testcase-" + caseId + "/"
        File folder = new File(DESTINATION_FOLDER + testCaseDir)
        try {
            if (!folder.exists()) {
                folder.mkdirs()
            }
        } catch (Exception e) {
            log.error("Make dir not work.",  e)
        }
        return (DESTINATION_FOLDER + testCaseDir)
    }

    /**
     * generate file path
     * @param fileName
     * @return String
     */
    static generateDestinationFile(String folder, String fileName) {
        return folder + fileName + ".png"
    }

    /**
     * Decode image content from String to byte array
     * @param imageContent
     * @return byte[] decode base 64
     */
    static byte[] decodeImage(String imageContent) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(imageContent)
    }

    /**
     * Check element is display nor visibility
     * @param element
     * @return boolean
     */
    static boolean isElementDisplayed(elements) {
        try {
            if (elements == null) {
                return false
            }
            if (elements.isEmpty()) {
                return false
            }
            if (elements.size() == 0) {
                return false
            } else if (elements.size() == 1) {
                if (elements.displayed) {
                    return true
                }
            } else {
                elements.each {
                    if (it.displayed) {
                        return true
                    }
                }
            }
        } catch(StaleElementReferenceException se) {
            log.warn(se.getMessage())
        } catch (GebException ge) {
            log.warn(ge.getMessage())
        } catch (groovy.lang.MissingMethodException me) {
            log.warn(me.getMessage())
        } catch (geb.waiting.WaitTimeoutException we) {
            log.warn(we.getMessage())
        } catch (geb.error.RequiredPageContentNotPresent re) {
            log.warn(re.getMessage())
        } catch (org.openqa.selenium.ElementNotVisibleException ee) {
            log.warn(ee.getMessage())
        } catch (Exception e) {
            log.warn(e.getMessage())
        }
        return false
    }

    /**
     * Select by value
     * @param element
     * @param value
     * @param message log message
     */
    static void selectByValue(element, value, message="") {
        try {
            element.each {
                if (isElementDisplayed(it)) {
                    it.value(value)
                    if (!message.isEmpty()) {
                        log.info("${message}: ${value}")
                    }
                }
            }
        } catch (GebException ge) {
            log.warn("Cannot select value: ${value}")
        } catch (Exception e) {
            log.warn("Cannot select value: ${value}")
        }
    }

    /**
     * Click element with try-catch it
     * @param element
     * @param message
     * @param isAnyClick
     */
    static void clickElement(element, message, boolean isAnyClick) {
        try {
            if (isAnyClick) {
                element.any {
                    if (isElementDisplayed(it)) {
                        it.click()
                        if (!message.isEmpty()) {
                            log.info("${message}")
                        }
                    }
                }
            } else {
                element.each {
                    if (isElementDisplayed(it)) {
                        it.click()
                        if (!message.isEmpty()) {
                            log.info("${message}")
                        }
                    }
                }
            }
        } catch (StaleElementReferenceException se) {
            log.warn("StaleElementReferenceException: element invisible")
        } catch (GebException ge) {
            log.warn(ge.getMessage())
        } catch (groovy.lang.MissingMethodException me) {
            log.warn(me.getMessage())
        } catch (geb.waiting.WaitTimeoutException we) {
            log.warn(we.getMessage())
        } catch (geb.error.RequiredPageContentNotPresent re) {
            log.warn(re.getMessage())
        } catch (org.openqa.selenium.ElementNotVisibleException ee) {
            log.warn(ee.getMessage())
        } catch (org.openqa.selenium.WebDriverException wde) {
            log.warn(wde.getMessage())
        } catch (Exception e) {
            log.warn("An error occur when we try to click element")
        }
    }

    /**
     * wait until element is displayed
     * @param driver
     * @param timeOut
     * @param strCheck
     * @param jsScript Ex: '''return $('title').text();'''
     * @return boolean
     */
    static boolean wait(ChromeDriver driver, int timeOut, String strCheck, String jsScript="") {
        int twoSecond = 2000
        String title = ""
        try {
            if (jsScript != null) {
                if (jsScript.isEmpty()) {
                    jsScript = '''return $('title').text();'''
                }
            }
            Thread.sleep(twoSecond)
            log.info("Waiting in ${timeOut}s ...")
            while (timeOut > 0) {
                title = driver.executeScript(jsScript)
                if (title != null) {
                    if (!title.isEmpty()) {
                        if (strCheck == title || title.endsWith(strCheck) || title.contains(strCheck)) {
                            log.info("Wait OK")
                            return true
                        }
                    }
                }
                timeOut = timeOut - 1
                Thread.sleep(twoSecond)
            }
            if (timeOut <= 0) {
                log.info("Wait time out")
                return false
            }
        } catch (AssertionError ae) {
            log.error(ae.getMessage())
        } catch (org.openqa.selenium.WebDriverException we) {
            log.error("WebDriver exception.")
        } catch (Exception e) {
            log.error(e.getMessage())
        }
        return false
    }

}