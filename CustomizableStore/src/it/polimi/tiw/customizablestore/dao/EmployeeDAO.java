package it.polimi.tiw.customizablestore.dao;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import it.polimi.tiw.customizablestore.beans.Option;
import it.polimi.tiw.customizablestore.beans.Product;
import it.polimi.tiw.customizablestore.beans.Quote;

public class EmployeeDAO {
	private Connection connection;
	private int id;
	
	public EmployeeDAO(Connection connection, int id) {
		this.connection = connection;
		this.id = id;
	}
	
	public List<Quote> findQuotes() throws SQLException {
		ArrayList<Quote> quotes = new ArrayList<Quote>();
		String query = "SELECT qt.*, GROUP_CONCAT(CONCAT(opt.idoption, ',', opt.code, ',', opt.name, ',', opt.type) order by opt.idoption separator ';') as options\n" + 
				"FROM (\n" + 
				"	SELECT \n" + 
				"    q.idquote,\n" + 
				"    q.submission_date,\n" + 
				"    q.price,\n" + 
				"    c.username as client,\n" + 
				"    e.username as employee,\n" + 
				"    p.image as product_image,\n" + 
				"    GROUP_CONCAT(CONCAT(p.idproduct, ',', p.code, ',', p.name) separator ';') as product\n" + 
				"FROM user e\n" + 
				"		INNER JOIN quote q\n" + 
				"        ON e.iduser = q.employee\n" + 
				"        INNER JOIN product p \n" + 
				"		ON q.product = p.idproduct\n" + 
				"        INNER JOIN user c\n" + 
				"		ON q.client = c.iduser\n" + 
				"         WHERE e.iduser = ?\n" + 
				"		GROUP BY q.idquote\n" + 
				") qt\n" + 
				"INNER JOIN quote_option qo\n" + 
				"ON qt.idquote = qo.quote_id\n" + 
				"INNER JOIN `option` opt\n" + 
				"ON qo.option_id = opt.idoption\n" + 
				"GROUP BY qt.idquote;";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, this.id);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Quote quote = new Quote();
					quote.setId(result.getInt("idquote"));					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");			
					quote.setSubmissionDate(result.getTimestamp("submission_date").toLocalDateTime().format(formatter));
					quote.setClient(result.getString("client"));
					quote.setEmployee(result.getString("employee"));
					quote.setPrice(result.getBigDecimal("price"));
					Product product = new Product();
					String[] parts = result.getString("product").split(",");
					product.setId(Integer.parseInt(parts[0]));
					product.setCode(parts[1]);
					product.setName(parts[2]);
					Blob blob = result.getBlob("product_image");
					byte[] imageByteArray = blob.getBytes(1l, (int)blob.length());
					product.setImage(Base64.getEncoder().encodeToString(imageByteArray));
					quote.setProduct(product);
					
					ArrayList<Option> options = new ArrayList<Option>();
					String[] optionList = result.getString("options").split(";");
					for (int i = 0; i < optionList.length; i++) {
						String[] optionString = optionList[i].split(",");
						Option op = new Option();
						op.setId(Integer.parseInt(optionString[0]));
						op.setCode(optionString[1]);
						op.setName(optionString[2]);
						op.setType(optionString[3]);
						options.add(op);
					}
					
					quote.setOptions(options);
					quotes.add(quote);
				}
			}
		}
		return quotes;
	}

	public List<Quote> findUnhandledQuotes() throws SQLException {
		ArrayList<Quote> quotes = new ArrayList<Quote>();
		String query = "SELECT qt.*, GROUP_CONCAT(CONCAT(opt.idoption, ',', opt.code, ',', opt.name, ',', opt.type) order by opt.idoption separator ';') as options\n" + 
				"FROM (\n" + 
				"	SELECT \n" + 
				"    q.idquote,\n" + 
				"    q.submission_date,\n" + 
				"    c.username as client,\n" + 
				"    p.image as product_image,\n" + 
				"    GROUP_CONCAT(CONCAT(p.idproduct, ',', p.code, ',', p.name) separator ';') as product\n" + 
				"FROM user c\n" + 
				"		INNER JOIN quote q\n" + 
				"        ON c.iduser = q.client\n" + 
				"        INNER JOIN product p \n" + 
				"		ON q.product = p.idproduct\n" + 
				"         WHERE q.employee IS NULL\n" + 
				"		GROUP BY q.idquote\n" + 
				") qt\n" + 
				"INNER JOIN quote_option qo\n" + 
				"ON qt.idquote = qo.quote_id\n" + 
				"INNER JOIN `option` opt\n" + 
				"ON qo.option_id = opt.idoption\n" + 
				"GROUP BY qt.idquote;";
		try (Statement statement = connection.createStatement();) {
			try (ResultSet result = statement.executeQuery(query);) {
				while (result.next()) {
					Quote quote = new Quote();
					quote.setId(result.getInt("idquote"));					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");			
					quote.setSubmissionDate(result.getTimestamp("submission_date").toLocalDateTime().format(formatter));
					quote.setClient(result.getString("client"));
					Product product = new Product();
					String[] parts = result.getString("product").split(",");
					product.setId(Integer.parseInt(parts[0]));
					product.setCode(parts[1]);
					product.setName(parts[2]);
					Blob blob = result.getBlob("product_image");
					byte[] imageByteArray = blob.getBytes(1l, (int)blob.length());
					product.setImage(Base64.getEncoder().encodeToString(imageByteArray));
					quote.setProduct(product);
					
					ArrayList<Option> options = new ArrayList<Option>();
					String[] optionList = result.getString("options").split(";");
					for (int i = 0; i < optionList.length; i++) {
						String[] optionString = optionList[i].split(",");
						Option op = new Option();
						op.setId(Integer.parseInt(optionString[0]));
						op.setCode(optionString[1]);
						op.setName(optionString[2]);
						op.setType(optionString[3]);
						options.add(op);
					}
					
					quote.setOptions(options);
					quotes.add(quote);
				}
			}
		}
		return quotes;
	}

	public Quote getQuoteDetails(int quoteId) throws SQLException {
		Quote quote = new Quote();
		String query = "SELECT qt.*, \n" + 
		"       Group_concat(Concat(opt.idoption, ',', opt.code, ',', opt.name, ',', \n" + 
		"       opt.type) \n" + 
		"       ORDER BY opt.idoption SEPARATOR ';') AS options \n" + 
		"FROM   (SELECT q.idquote, \n" + 
		"               q.submission_date, \n" + 
		"               c.username \n" + 
		"                      AS client, \n" + 
		"               p.image \n" + 
		"                      AS product_image, \n" + 
		"               Group_concat(Concat(p.idproduct, ',', p.code, ',', p.name) \n" + 
		"               SEPARATOR ';' \n" + 
		"                      ) AS \n" + 
		"               product \n" + 
		"        FROM   quote q \n" + 
		"               INNER JOIN user c \n" + 
		"                       ON c.iduser = q.client \n" + 
		"               INNER JOIN product p \n" + 
		"                       ON q.product = p.idproduct \n" + 
		"        WHERE  q.idquote = ? \n" + 
		"        GROUP  BY q.idquote) qt \n" + 
		"       INNER JOIN quote_option qo \n" + 
		"               ON qt.idquote = qo.quote_id \n" + 
		"       INNER JOIN `option` opt \n" + 
		"               ON qo.option_id = opt.idoption \n" + 
		"GROUP  BY qt.idquote";

		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, quoteId);
			try (ResultSet result = pStatement.executeQuery();) {
				while (result.next()) {
					quote.setId(result.getInt("idquote"));		
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");			
					quote.setSubmissionDate(result.getTimestamp("submission_date").toLocalDateTime().format(formatter));
					quote.setClient(result.getString("client"));
					Product product = new Product();
					String[] parts = result.getString("product").split(",");
					product.setId(Integer.parseInt(parts[0]));
					product.setCode(parts[1]);
					product.setName(parts[2]);
					Blob blob = result.getBlob("product_image");
					byte[] imageByteArray = blob.getBytes(1l, (int)blob.length());
					product.setImage(Base64.getEncoder().encodeToString(imageByteArray));
					quote.setProduct(product);
					
					ArrayList<Option> options = new ArrayList<Option>();
					String[] optionList = result.getString("options").split(";");
					for (int i = 0; i < optionList.length; i++) {
						String[] optionString = optionList[i].split(",");
						Option op = new Option();
						op.setId(Integer.parseInt(optionString[0]));
						op.setCode(optionString[1]);
						op.setName(optionString[2]);
						op.setType(optionString[3]);
						options.add(op);
					}
					quote.setOptions(options);
				}
			}
		}
		return quote;
	}

	public void priceQuote(int quoteId, BigDecimal price) throws SQLException {
		String query = "UPDATE quote\n" + 
		"SET employee = ?, price = ?\n" + 
		"WHERE quote.idquote = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, this.id);
			pstatement.setBigDecimal(2, price);
			pstatement.setInt(3, quoteId);
			pstatement.executeUpdate();
		}
	}
}
