package tests;

import utils.DBUtils;

public class DBMaintenanceTool {

	public static void main(String[] args) {
		DBUtils.cleanClientData();
	}
}
