import geb.spock.GebReportingSpec
import groovy.util.logging.*
import geb.driver.CachingDriverFactory
import org.openqa.selenium.WebDriverException
import spock.lang.*

import javax.imageio.ImageIO
import java.awt.image.BufferedImage


@Log4j
class HotmailSpec extends GebReportingSpec {


    @Shared
            jutils = new JSONUtils()

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

        driver.switchTo().defaultContent()
        when:
        to SignUpPage
        log.info("Microsoft Account opened")
        Utils.screenshotFullPage(driver, "1.Input_info_before", caseId)

        then: "Input first name"
        Utils.selectByValue(firstName, firstNameValue, "First name")

        then: "Input last name"
        Utils.selectByValue(lastName, lastNameValue, "Last name")

        then: "Click new mail"
        Utils.clickElement(newMail, "Clicked new mail", true)
        Thread.sleep(100)

        then: "Select domain"
        Utils.selectByValue(domain, domainValue, "Selected domain")

        then: "Input user name"
        Utils.selectByValue(username, usernameValues[0], "User name")

        then: "Input password"
        Utils.selectByValue(passwd, passwordValue, "Password")

        then: "Confirm password"
        Utils.selectByValue(confirmPasswd, passwordValue, "Confirm password")
        Thread.sleep(3000)

        String errMsg = $("#iMembernameLiveError").text()
        if (errMsg != null) {
            log.warn(errMsg)
            int i = 1
            while ((errMsg.contains("already")) && (i <= usernameValues.size())) {
                Utils.selectByValue(username, usernameValues[i], "Try username")
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
        Utils.selectByValue(birthMonth, birthMonthValue, "Birth month")

        then: "Select birth day"
        Utils.selectByValue(birthDay, birthDayValue, "Birth day")

        then: "Select birth year"
        Utils.selectByValue(birthYear, birthYearValue, "Birth year")

        then: "Select gender"
        Utils.selectByValue(gender, genderValue, "Gender")

        then: "Select country code"
        Utils.selectByValue(countryCode, countryCodeValue, "Country code")

//        then: "Input telephone number"
//        Utils.selectByValue(phoneNo, telephoneNoValue, "Phone number")

        then: "Input alternative email"
        Utils.selectByValue(altEmail, altEmailValue, "Alternative email")

        reportGroup("ABC")
        report("abcd")

        then: "Input captcha"
        Utils.selectByValue(captcha, "1234", "")
        Utils.screenshotFullPage(driver, "2.Input_info_after", caseId)
        Thread.sleep(1000000)

        then: "Click submit button"
        Utils.clickElement(submitBtn, "Clicked submit button", true)

        Thread.sleep(3000)
        driver.get("https://login.live.com/login.srf")
        Thread.sleep(5000)

        // Go to email to verify
        driver.get(altLoginService)
        when: "At host mail login page"
        at GmailLoginPage
        Utils.screenshotFullPage(driver, "3.Hostmail_login_page", caseId)
        log.info("At login page")

        then: "Input username"
        Utils.selectByValue(username, altEmailValue, "Gmail username")

        then: "Input password"
        Utils.selectByValue(password, passwordValue, "Gmail password")

        then: "Click login button"
        Utils.clickElement(next, "Clicked next button", true)
        Thread.sleep(5000)

        where:
            caseId << jutils.get("CASE_ID")
            firstNameValue << jutils.get("FIRST_NAME")
            lastNameValue << jutils.get("LAST_NAME")
            usernameValues << jutils.get("USER_NAME")
            domainValue << jutils.get("DOMAIN")
            passwordValue << jutils.get("PASSWORD")
            telephoneNoValue << jutils.get("TELEPHONE_NUMBER")
            countryCodeValue << jutils.get("COUNTRY_CODE")
            zipCodeValue << jutils.get("ZIP_CODE")
            genderValue << jutils.get("GENDER")
            birthMonthValue << jutils.get("BIRTH_MONTH")
            birthDayValue << jutils.get("BIRTH_DAY")
            birthYearValue << jutils.get("BIRTH_YEAR")
            altEmailValue << jutils.get("ALTERNATIVE_EMAIL")
            altLoginService << jutils.get("ALTERNATIVE_LOGIN_SERVICE")
    }

}