package tests;

import utils.DBUtils;

public class DBConnectionTest {

	public static void main(String[] args) {
		String query = "select * from MOSLACEAdvisioryDB..tbl_ProductsCodesList where ProductId=2087";
		String productName = DBUtils.getSingleValue(query, "ProductName");
		System.out.println("Product from DB: " + productName);
	}
}
