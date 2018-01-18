

class LoginTesterAdminSpec extends LoginTesterSpec {

	public static String testerUsername = "tester-admin@streamr.com"
	public static String testerPassword = "tester-adminTESTER-ADMIN"

	@Override
	String getTesterUsername() {
		return testerUsername
	}

	@Override
	String getTesterPassword() {
		return testerPassword
	}
}
