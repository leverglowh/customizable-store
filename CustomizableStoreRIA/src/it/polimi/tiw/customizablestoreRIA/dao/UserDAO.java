package it.polimi.tiw.customizablestoreRIA.dao;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import it.polimi.tiw.customizablestoreRIA.beans.User;
import it.polimi.tiw.customizablestoreRIA.exceptions.UsernameExistsException;

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

	public User register(String username, String password, String email, String role) throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException, UsernameExistsException {
		con.setAutoCommit(false);
		Boolean isUsernameAvailable = checkUsername(username);
		if (!isUsernameAvailable) {
			throw new UsernameExistsException("Username is not available");
		}
		byte[] salt = SecureRandom.getSeed(SALT_LENGTH);
		byte[] generatedSecret = getEncryptedPassword(password, salt, ITERATION_NUM, KEY_LENGTH);
		
		String query = "INSERT INTO user (username, email, role, salt, password) VALUES (?, ?, ?, ?, ?)";
		String userId = "";
		User user = new User();
		try (PreparedStatement pstatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, role);
			pstatement.setBytes(4, salt);
			pstatement.setBytes(5, generatedSecret);
			System.out.println(pstatement.toString());
			pstatement.executeUpdate();

			ResultSet rs = pstatement.getGeneratedKeys();
			if (rs.next()){
				userId = rs.getString(1);
				String userQuery = "SELECT username, email, role from user where user.iduser = ?";
				try (PreparedStatement getUserStatement = con.prepareStatement(userQuery);) {
					getUserStatement.setInt(1, Integer.parseInt(userId));
					try (ResultSet result = getUserStatement.executeQuery();) {
						if (!result.isBeforeFirst()) // no results, user registration failed
						return null;
						else {
							con.commit();
							while(result.next()) {
								user.setId(Integer.parseInt(userId));
								user.setRole(result.getString("role"));
								user.setUsername(result.getString("username"));
								user.setEmail(result.getString("email"));
								return user;
							}
						}
					}
				}
			}
		}
		return user;
	}

	private Boolean checkUsername(String username) {
		String query = "SELECT username FROM user WHERE username = ?;";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				return !result.isBeforeFirst(); // no results, username is available
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
