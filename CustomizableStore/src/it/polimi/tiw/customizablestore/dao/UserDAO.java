package it.polimi.tiw.customizablestore.dao;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import it.polimi.tiw.customizablestore.beans.User;

public class UserDAO {
	private Connection con;
	private static final int SALT_LENGTH = 8;
	private static final int ITERATION_NUM = 65536;
	private static final int KEY_LENGTH = 128;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkCredentials(String username, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		User user = new User();
		byte[] salt = null;
		
		String query = "SELECT iduser, role, username, email, salt, password FROM user WHERE username = ?;";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					salt = result.getBytes("salt");
					if (salt == null) return null;
					byte[] generatedSecret = getEncryptedPassword(password, salt, ITERATION_NUM, KEY_LENGTH);
					byte[] originalHash = result.getBytes("password");
					if (Arrays.equals(originalHash, generatedSecret)) {
						user.setId(result.getInt("iduser"));
						user.setRole(result.getString("role"));
						user.setUsername(result.getString("username"));
						user.setEmail(result.getString("email"));
						return user;
					}
				}
			}
		}
		return user;
	}

	public void updateCredentials(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException {
		byte[] salt = SecureRandom.getSeed(SALT_LENGTH);
		
		StringBuilder sb = new StringBuilder();
	    sb.append("[ ");
	    for (byte b : salt) {
	        sb.append(String.format("0x%02X ", b));
	    }
	    sb.append("]");
	    
	    System.out.println(sb.toString());
		byte[] generatedSecret = getEncryptedPassword(password, salt, ITERATION_NUM, KEY_LENGTH);
		StringBuilder sba = new StringBuilder();
	    sba.append("[ ");
	    for (byte b : generatedSecret) {
	        sba.append(String.format("0x%02X ", b));
	    }
	    sba.append("]");
		System.out.println(sba.toString());
		String query = "UPDATE user SET password=?, salt=? WHERE username = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setBytes(1, generatedSecret);
			pstatement.setBytes(2, salt);
			pstatement.setString(3, username);
			System.out.println(pstatement.toString());
			int result = pstatement.executeUpdate();
			System.out.println(result);
		}
	}
	
	private static byte[] getEncryptedPassword(
			String password,
            byte[] salt,
            int iterations,
            int keyLength
            ) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(
		    password.toCharArray(),
		    salt,
		    iterations,
		    keyLength * 8
		    );

		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

		return f.generateSecret(spec).getEncoded();
	}
}
