import geb.spock.GebReportingSpec
import groovy.util.logging.*
import geb.driver.CachingDriverFactory
import org.openqa.selenium.WebDriverException
import spock.lang.*

@Log4j
class HotmailSpec extends GebReportingSpec {


    @Shared
            jutils = new JSONUtils()
    @Shared
            mailReader = new MailReader()

    def setup() {
        CachingDriverFactory.clearCache()
        browser.config.autoClearCookies = true
        driver.manage().window().maximize()
    }

    def cleanup() {
        try {
            driver.close()
        } catch (WebDriverException | Exception e) {}
    }

    @Unroll
    "Hotmail register"() {

        log.info("---------------------------------------CASE_ID: ${caseId}-----------------------------------------------")
        ArrayList<String> mail1 = mailReader.getMails(jutils.getConfig("MAIL_PATH_1"))
        ArrayList<String> mail2 = mailReader.getMails(jutils.getConfig("MAIL_PATH_2"))
        if (mail1.size() <= 0) {
            log.error("Please check list1mail.txt")
            assert false
        }
        if (mail2.size() <= 0) {
            log.error("Please check list2mail.txt")
            assert false
        }

        driver.switchTo().defaultContent()
        when:
        to SignUpPage
        log.info("Microsoft Account opened")
        Utils.screenshotFullPage(driver, "1.Input_info_before", caseId)

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
        Utils.selectByValue(username, mail1.get(0).split("@")[0], "User name")

        then: "Input password"
        Utils.selectByValue(passwd, passwordValue, "Password")

        then: "Confirm password"
        Utils.selectByValue(confirmPasswd, passwordValue, "Confirm password")
        Thread.sleep(3000)

        String errMsg = $("#iMembernameLiveError").text()
        if (errMsg != null) {
            log.warn(errMsg)
            int i = 1
            while ((errMsg.contains("already")) && (i < mail1.size())) {
                Utils.selectByValue(username, mail1.get(i).split("@")[0], "Try username")
                Utils.clickElement(firstName, "", true)
                Thread.sleep(3000)
                errMsg = $("#iMembernameLiveError").text()
                i++
            }
        }
        log.info("*****USERNAME registered: ${username.getAttribute("value")}")

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

//        then: "Input telephone number"
//        Utils.selectByValue(phoneNo, telephoneNoValue, "Phone number")

        then: "Input alternative email"
        Utils.selectByValue(altEmail, mail2.get(Utils.randomInt(mail2.size() - 1, 0)), "Alternative email")

        browser.report("captcha")
        Utils.cropImage(jutils.getConfig("CAPTCHA_LOCATION"))

        then: "Input captcha"
        Utils.selectByValue(captcha, Utils.getCaptchaText(), "")
        Utils.screenshotFullPage(driver, "2.Input_info_after", caseId)

        then: "Click submit button"
        Utils.clickElement(submitBtn, "Clicked submit button", true)
        Thread.sleep(30000)

        {Utils.getCookiesAsString(browser)}
        cleanup()
        where:
            caseId << jutils.get("CASE_ID")
            domainValue << jutils.get("DOMAIN")
            passwordValue << jutils.get("PASSWORD")
            countryCodeValue << jutils.get("COUNTRY_CODE")
            zipCodeValue << jutils.get("ZIP_CODE")
            genderValue << jutils.get("GENDER")
    }

}