class GmailLoginPage extends geb.Page {

    static at = { assert title.contains("HostMail") }

    static content = {

        username {$("#Email")}

        password {$("#password")}

        next {$("#next")}

    }
}
