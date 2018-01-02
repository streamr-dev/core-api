package core

public class LoginTesterAdminSpec extends LoginTesterSpec {

	public static String testerUsername = "tester-admin@streamr.com"
	public static String testerPassword = "tester-adminTESTER-ADMIN"

	@Override
	public String getTesterUsername() {
		return testerUsername
	}

	@Override
	public String getTesterPassword() {
		return testerPassword
	}
}
