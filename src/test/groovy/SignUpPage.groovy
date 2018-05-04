class SignUpPage extends geb.Page {

	static at = { assert title.contains("Microsoft") }
	
	static content = {

		firstName {$("input", id: "iFirstName")}

		lastName {$("input", id: "iLastName")}

		username {$("input", id: "imembernamelive")}

		domain (wait: false) {$("select", id: "idomain")}

		passwd {$("input", id: "iPwd")}

		confirmPasswd {$("input", id: "iRetypePwd")}

		country {$("select", id: "iCountry")}

		zipCode {$("input", id: "iZipCode")}

		birthMonth {$("select", id: "iBirthMonth")}

		birthDay {$("select", id: "iBirthDay")}

		birthYear {$("select", id: "iBirthYear")}

		gender {$("select", id: "iGender")}

		countryCode {$("select", id: "iSMSCountry")}

		phoneNo {$("input", id: "iPhone")}

		newMail {$("#livelinkctr a")}

		altEmail {$("#iAltEmail")}

		captcha {$("input.spHipNoClear")}

		submitBtn {$("#createbuttons input")}

	}

}
