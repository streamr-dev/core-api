

class LoginTester2Spec extends LoginTesterSpec {

	public static String testerUsername = "tester2@streamr.com"
	public static String testerPassword = "tester2"

	@Override
	String getTesterUsername() {
		return testerUsername
	}

	@Override
	String getTesterPassword() {
		return testerPassword
	}
}
