package it.polimi.tiw.customizablestore.dao;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import it.polimi.tiw.customizablestore.beans.Option;
import it.polimi.tiw.customizablestore.beans.Product;
import it.polimi.tiw.customizablestore.beans.Quote;

public class ClientDAO {
	private Connection connection;
	private int id;
	
	public ClientDAO(Connection connection, int id) {
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
				"    p.image as product_image,\n" + 
				"    GROUP_CONCAT(CONCAT(p.idproduct, ',', p.code, ',', p.name) separator ';') as product\n" + 
				"FROM user c\n" + 
				"		INNER JOIN quote q\n" + 
				"        ON c.iduser = q.client\n" + 
				"        INNER JOIN product p \n" + 
				"		ON q.product = p.idproduct\n" + 
				"         WHERE c.iduser = ?\n" + 
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

	public void createQuote(int productId, int[] optionIds) throws SQLException {
		String insertQuoteQuery = "INSERT INTO quote (submission_date, client, product) VALUES (NOW(), ?, ?)";

		String insertOptionsQuery = "INSERT INTO quote_option (quote_id, option_id) VALUES ";

		String quoteId = "";
		try (PreparedStatement pQuoteStatement = connection.prepareStatement(insertQuoteQuery, Statement.RETURN_GENERATED_KEYS);) {
			pQuoteStatement.setInt(1, this.id);
			pQuoteStatement.setInt(2, productId);
			pQuoteStatement.executeUpdate();

			
			ResultSet rs = pQuoteStatement.getGeneratedKeys();
			if (rs.next()){
				quoteId = rs.getString(1);
				String optionString = "";
				for (int i = 0; i < optionIds.length; i++) {
					optionString = optionString + "(" + quoteId + " , " + optionIds[i] + ")";
					if (i != optionIds.length -1) {
						optionString = optionString + ", ";
					}
				}
				try (Statement optionsStatement = connection.createStatement()) {
					System.out.println(insertOptionsQuery + optionString);
					optionsStatement.executeUpdate(insertOptionsQuery + optionString);
				}
			}
		}
	}
}
