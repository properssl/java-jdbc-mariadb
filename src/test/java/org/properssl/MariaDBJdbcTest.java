package org.properssl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class MariaDBJdbcTest {
	String host = "localhost";
	int port = 13306;
	String database = "proper_ssl";
	String username = "proper_ssl";
	String password = "real_passwords_should_be_random";

	private Connection getConnection(Properties info) throws SQLException {
		String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
		info.setProperty("user", username);
		info.setProperty("password", password);
		return DriverManager.getConnection(url, info);
	}

	private void testConnect(Properties info, boolean sslExpected)
			throws SQLException {
		Connection conn = null;
		try {
			conn = getConnection(info);
			Statement stmt = conn.createStatement();

			// Test a query to make sure we're connected:
			ResultSet rs = stmt.executeQuery("SELECT 1");
			rs.next();
			Assert.assertEquals(1, rs.getInt(1));
			rs.close();

			// Check whether we're using SSL
			rs = stmt.executeQuery("SHOW STATUS LIKE 'ssl_cipher'");
			rs.next();
			String sslCipher = rs.getString(2);
			rs.close();
			boolean sslActual = sslCipher != null && sslCipher.length() > 0;
			Assert.assertEquals("SSL use does not match", sslExpected,
					sslActual);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Connect using no SSL. This is bad as passwords are sent over as
	 * plaintext.
	 */
	@Test
	public void connectNoSSL() throws SQLException {
		Properties info = new Properties();
		testConnect(info, false);
	}

	/**
	 * Connect using SSL without any server certificate validation. This is bad
	 * as this connection is vulnerable to a man in the middle attack.
	 */
	@Test
	public void connectSSLWithoutValidation() throws SQLException {
		Properties info = new Properties();
		info.setProperty("useSSL", "true");
		info.setProperty("trustServerCertificate", "true");
		testConnect(info, true);
	}

	/**
	 * Connect using SSL and attempt to validate the server's certificate but
	 * don't actually provide it. This connection attempt should *fail* as the
	 * client should reject the server.
	 */
	@Test(expected = SQLNonTransientConnectionException.class)
	public void connectSSLWithValidationNoCert() throws SQLException {
		Properties info = new Properties();
		info.setProperty("useSSL", "true");
		testConnect(info, true);
	}

	/**
	 * Connect using SSL and attempt to validate the server's certificate
	 * against the wrong pre shared certificate. This test uses a pre generated
	 * certificate that will *not* match the test MariaDB server (the
	 * certificate is for properssl.example.com).
	 * 
	 * This test should throw an exception as the client should reject the
	 * server since the certificate does not match.
	 * 
	 * @throws SQLException
	 */
	@Test(expected = SQLNonTransientConnectionException.class)
	public void connectSSLWithValidationWrongCert() throws SQLException,
			IOException {
		Properties info = new Properties();
		info.setProperty("useSSL", "true");
		info.setProperty("serverSslCert", "classpath:invalid-server.crt");
		testConnect(info, true);
	}

	/**
	 * Connect using SSL and attempt to validate the server's certificate
	 * against the proper pre shared certificate. Make sure to copy the server's
	 * certificate to src/test/resources prior to running this. Instructions are
	 * in the README file.
	 * 
	 * NOTE: This test will fail if you do not copy the newly generated server
	 * certificate before you run it.
	 * 
	 * NOTE: If you're connecting to a remote server that uses a self signed
	 * certificate this is how a connection should be made.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void connectSSLWithValidationProperCert() throws SQLException,
			IOException {
		Properties info = new Properties();
		info.setProperty("useSSL", "true");
		info.setProperty("serverSslCert", "classpath:server.crt");
		testConnect(info, true);
	}
}
