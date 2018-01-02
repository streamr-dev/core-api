package core

public class LoginTester2Spec extends LoginTesterSpec {

	public static String testerUsername = "tester2@streamr.com"
	public static String testerPassword = "tester2"

	@Override
	public String getTesterUsername() {
		return testerUsername
	}

	@Override
	public String getTesterPassword() {
		return testerPassword
	}
}
