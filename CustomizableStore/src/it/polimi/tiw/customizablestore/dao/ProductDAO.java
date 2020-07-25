package it.polimi.tiw.customizablestore.dao;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import it.polimi.tiw.customizablestore.beans.Option;
import it.polimi.tiw.customizablestore.beans.Product;

public class ProductDAO {
    private Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Product> getProducts() throws SQLException {
        ArrayList<Product> products = new ArrayList<Product>();
        String query = 	"SELECT \n" + 
                        "  p.idproduct, \n" + 
                        "  p.code, \n" + 
                        "  p.name, \n" + 
                        "  p.image, \n" + 
                        "  GROUP_CONCAT(\n" + 
                        "    CONCAT(\n" + 
                        "      o.idoption, ',', o.code, ',', o.name, ',', o.type\n" + 
                        "    ) order by o.idoption separator ';'\n" + 
                        "  ) as options \n" + 
                        "FROM \n" + 
                        "  product p \n" + 
                        "  INNER JOIN product_option po ON p.idproduct = po.product_id \n" + 
                        "  INNER JOIN `option` o ON po.option_id = o.idoption \n" + 
                        "GROUP BY \n" + 
                        "  po.product_id";
        try (Statement statement = connection.createStatement();) {
            try (ResultSet result = statement.executeQuery(query);) {
                while (result.next()) {
                    Product product = new Product();
                    product.setId(result.getInt("idproduct"));
                    product.setName(result.getString("name"));
                    product.setCode(result.getString("code"));
                    Blob blob = result.getBlob("image");
					byte[] imageByteArray = blob.getBytes(1l, (int)blob.length());
					product.setImage(Base64.getEncoder().encodeToString(imageByteArray));

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

                    product.setOptions(options);
                    products.add(product);
                }
            }
        }
        return products;
    }
}
