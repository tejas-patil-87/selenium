package tests;

import java.nio.file.Paths;

import utils.DBUtils.DBToExcelUtil;

public class DBConnectionTest {

	public static void main(String[] args) {
//		String query = "select * from MOSLACEAdvisioryDB..tbl_ProductsCodesList where ProductId=2087";
//		String productName = DBUtils.getSingleValue(query, "ProductName");
//		System.out.println("Product from DB: " + productName);
		String excelPath = Paths.get(System.getProperty("user.dir"), "test-output", "client_data.xlsx").toString();

		DBToExcelUtil.writeClientDataToExcel(excelPath);

	}
}
