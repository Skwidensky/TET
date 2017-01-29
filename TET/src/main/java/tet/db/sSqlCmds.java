package tet.db;

public class sSqlCmds {

	public static String useDb(String pDb) {
		return "use " + pDb + ";";
	}

	public static String createEyeTbl(String pName) {
		return "create table if not exists " + pName
				+ "_calculated (total_time FLOAT8, fpm FLOAT8, tot_fixations FLOAT8, time_btwn_fix FLOAT8, fix_len FLOAT8, smooth_dist FLOAT8, smooth_speed FLOAT8, pct_fixated FLOAT8, avg_sac_spd FLOAT8, avg_peak_sac_accel FLOAT8, fidg_l FLOAT8, fidg_r FLOAT8, avg_fidg FLOAT8, blinks FLOAT8, subject VARCHAR(32));";
	}

	public static String createRawTbl(String pName) {
		return "create table if not exists " + pName + "_raw (raw_sample VARCHAR(50));";
	}

	public static String insertEyeValues(String pTable, String pValues) {
		return "insert into " + pTable
				+ "_calculated (total_time, fpm, tot_fixations, time_btwn_fix, fix_len, smooth_dist, smooth_speed, pct_fixated, avg_sac_spd, avg_peak_sac_accel, fidg_l, fidg_r, avg_fidg, blinks) values "
				+ pValues + ";";
	}

	public static String insertTrainingValue(String pTable, String pValues) {
		return "insert into " + pTable
				+ "_calculated (total_time, fpm, tot_fixations, time_btwn_fix, fix_len, smooth_dist, smooth_speed, pct_fixated, avg_sac_spd, avg_peak_sac_accel, fidg_l, fidg_r, avg_fidg, blinks, subject) values "
				+ pValues + ";";
	}

	public static String insertRawOutputStrings(String pTable, String pValues) {
		return "insert into " + pTable + " (raw_sample) values " + pValues + ";";
	}

	public static String getListOfTables() {
		return "show tables;";
	}

	public static String truncateTbl(String pTable) {
		return "truncate table " + pTable + ";";
	}

	public static String dropTbl(String pTable) {
		return "drop table " + pTable + ";";
	}

	public static String getRaw(String pTable) {
		return "select * from " + pTable + "_raw;";
	}

}
