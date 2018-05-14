import geb.spock.GebReportingSpec
import groovy.util.logging.*
import geb.driver.CachingDriverFactory
import org.apache.log4j.Logger
import spock.lang.*

@Log4j
class HotmailSpec extends GebReportingSpec {


    @Shared
            jutils = new JSONUtils()
    @Shared
            mail1 = new MailReader().getMails(jutils.getConfig("MAIL_PATH_1"))
    @Shared
            mail2 = new MailReader().getMails(jutils.getConfig("MAIL_PATH_2"))

    private static final Logger logger = Logger.getLogger("ExternalAppLogger")

    def setup() {
        CachingDriverFactory.clearCache()
        browser.config.autoClearCookies = true
        driver.manage().window().maximize()
    }

    @Unroll
    "Hotmail register"() {

        log.info("---------------------------------------CASE_ID: -----------------------------------------------")
        if (mail1.size() <= 0) {
            log.error("Can not find any email for register")
            System.exit(0)
            assert false
        }
        if (mail2.size() <= 0) {
            log.error("Please check list2mail.txt")
            assert false
        }
        long waitTime = jutils.getConfig("SIGN_UP_WAIT") * 1000

        // Connect to VPN and change ip address
        Utils.execCmd("\"c:\\Program Files (x86)\\HMA! Pro VPN\\bin\\HMA! Pro VPN.exe\" -changeip")
        Thread.sleep(25000)

        driver.switchTo().defaultContent()
        when:
        to SignUpPage
        log.info("Microsoft Account opened")


        try {
            String winHandleBefore = driver.getWindowHandle()
            driver.executeScript('''return window.open("google.com", "_blank")''')
            for (String winHandle : driver.getWindowHandles()) {
                if (winHandle != winHandleBefore) {
                    driver.switchTo().window(winHandle)
                    driver.get("chrome-extension://lncaoejhfdpcafpkkcddpjnhnodcajfg/options.html")
                    Thread.sleep(2000)
                    driver.executeScript('''return $("#save").click();''')
                    sleep(1000)
                    driver.close()
                }
            }
            driver.switchTo().window(winHandleBefore)
        } catch (Exception e) {

        } catch (org.openqa.selenium.WebDriverException we) {
            log.error("WebDriver exception.")
        }


        then: "Input first name"
        Utils.selectByValue(firstName, Utils.getNameRandom(), "First name")

        then: "Input last name"
        Utils.selectByValue(lastName, Utils.getNameRandom(), "Last name")

        then: "Click new mail"
        Utils.clickElement(newMail, "Clicked new mail", true)
        Thread.sleep(100)

        then: "Select domain"
        Utils.selectByValue(domain, domainValue, "Selected domain")

        then: "Input user name"
        String[] mailInfos = mail1.get(0).split("@")
        if (mailInfos.size() > 1) {
            Utils.selectByValue(username, mail1.get(0).split("@")[0], "User name")
        } else {
            Utils.selectByValue(username, mail1.get(0), "User name")
        }

        then: "Input password"
        Utils.selectByValue(passwd, passwordValue, "Password")

        then: "Confirm password"
        Utils.selectByValue(confirmPasswd, passwordValue, "Confirm password")
        Thread.sleep(3000)

        String errMsg = $("#iMembernameLiveError").text()
        ArrayList<String> removeList = new ArrayList<String>()
        if (errMsg != null) {
            log.warn(errMsg)
            int i = 1
            while ((errMsg.contains("already")) && (i < mail1.size())) {
                removeList.add(mail1.get(i - 1))
                mailInfos = mail1.get(i).split("@")
                if (mailInfos.size() > 1) {
                    Utils.selectByValue(username, mail1.get(i).split("@")[0], "Try username")
                } else {
                    Utils.selectByValue(username, mail1.get(i), "Try username")
                }
                Utils.clickElement(firstName, "", true)
                Thread.sleep(3000)
                errMsg = $("#iMembernameLiveError").text()
                i++
            }
            if (i == mail1.size()) {
                log.info("All email invalid")
                System.exit(0)
            }
        }

        if (removeList.size() > 0) {
            mail1.removeAll(removeList)
        }
        String hotmail = username.getAttribute("value")
        log.info("*****USERNAME registered: ${hotmail}")

        then: "Select country"
        Utils.selectByValue(country, countryCodeValue, "Country")
        Thread.sleep(1000)

        then: "Input zip code"
        Utils.selectByValue(zipCode, zipCodeValue, "Zip code")

        then: "Select birth month"
        Utils.selectByValue(birthMonth, "${Utils.randomInt(11, 1)}", "Birth month")

        then: "Select birth day"
        Utils.selectByValue(birthDay, "${Utils.randomInt(28, 1)}", "Birth day")

        then: "Select birth year"
        Utils.selectByValue(birthYear, "${Utils.randomInt(1999, 1985)}", "Birth year")

        then: "Select gender"
        Utils.selectByValue(gender, genderValue, "Gender")

        then: "Select country code"
        Utils.selectByValue(countryCode, countryCodeValue, "Country code")

        then: "Input alternative email"
        Utils.selectByValue(altEmail, mail2.get(Utils.randomInt(mail2.size() - 1, 0)), "Alternative email")

        Utils.clickElement(submitBtn, "", true)
        browser.report("captcha")
        Utils.cropImage(jutils.getConfig("CAPTCHA_LOCATION"))

        then: "Input captcha"
        String captchaValue = Utils.getCaptchaText()
        String welcome
        try {
            if (!captchaValue.isEmpty()) {
                Utils.selectByValue(captcha, captchaValue, "")
                Utils.clickElement(submitBtn, "Clicked submit button", true)
                try {
                    Thread.currentThread().sleep(waitTime)
                    welcome = driver.executeScript('''return $("h2").text();''')
                } catch (InterruptedException e) {
                    log.error("Interrupted exception")
                } catch (Exception e) {
                    log.error("Waiting exception")
                } catch (org.openqa.selenium.WebDriverException we) {
                    log.error("WebDriver exception.")
                }

                log.info("WELCOME: ${welcome}")
                if (!welcome.isEmpty()) {
                    if (welcome.contains("Welcome")) {
                        logger.info("--------------------------------")
                        logger.info("Hotmail: ${hotmail}")
                        log.info(Utils.getCookiesAsString(browser))
                        logger.info(Utils.getCookiesAsString(browser))
                        logger.info("--------------------------------")
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage())
        } catch (org.openqa.selenium.WebDriverException we) {
            log.error("WebDriver exception.")
        }

        then: "Check captcha"
        try {
            String errorMsg
            try {
                errorMsg = driver.executeScript('''return $(".hipErrorText")[2].innerHTML;''')
            } catch (Exception e) {

            } catch (org.openqa.selenium.WebDriverException we) {
                log.error("WebDriver exception.")
            }
            if (errorMsg != null) {
                log.warn(errorMsg)
                int i = 0
                while (!errorMsg.isEmpty() && (i < 3)) {
                    Utils.clickElement(submitBtn, "", true)
                    browser.report("captcha")
                    Utils.cropImage(jutils.getConfig("CAPTCHA_LOCATION"))
                    Utils.selectByValue(captcha, Utils.getCaptchaText(), "")
                    Utils.clickElement(submitBtn, "Clicked submit button", true)
                    try {
                        Thread.currentThread().sleep(15000)
                        errorMsg = driver.executeScript('''return $(".hipErrorText")[2].innerHTML;''')
                    } catch (Exception e) {

                    } catch (org.openqa.selenium.WebDriverException we) {
                        log.error("WebDriver exception.")
                    }
                    if (!errorMsg.contains("didn't match the picture")) {
                        try {
                            Thread.currentThread().sleep(20000)
                            welcome = driver.executeScript('''return $("h2").text()''')
                        } catch (Exception e) {

                        }
                        if (!welcome.isEmpty()) {
                            if (welcome.contains("Welcome")) {
                                logger.info("--------------------------------")
                                logger.info("Hotmail: ${hotmail}")
                                log.info(Utils.getCookiesAsString(browser))
                                logger.info(Utils.getCookiesAsString(browser))
                                logger.info("--------------------------------")
                            }
                        }
                    }
                    i++
                }
                if (i == 3) {
                    log.error("Cannot input captcha")
                    assert false
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage())
        } catch (org.openqa.selenium.WebDriverException we) {
            log.error("WebDriver exception.")
        }

        driver.close()
        where:
            domainValue << jutils.get("DOMAIN")
            passwordValue << jutils.get("PASSWORD")
            countryCodeValue << jutils.get("COUNTRY_CODE")
            zipCodeValue << jutils.get("ZIP_CODE")
            genderValue << jutils.get("GENDER")
    }

}