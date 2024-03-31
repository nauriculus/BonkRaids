package de.nauriculus.bonkraids.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.nauriculus.bonkraids.main.BonkRaids;

public class MySQLStatements {
	
	public static boolean tokenExists(String UUID) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TOKENNAME FROM tokens WHERE TOKENNAME='" + UUID + "'");
			if (rs.next()) {
				return rs.getString("TOKENNAME") != null;
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static boolean tokenAddressExists(String UUID) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TOKENADDRESS FROM tokens WHERE TOKENADDRESS='" + UUID + "'");
			if (rs.next()) {
				return rs.getString("TOKENADDRESS") != null;
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}

	public static boolean checkIfRaidIsActive(String UUID) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT ENABLED FROM missions WHERE MISSIONID='" + UUID + "'");
			if (rs.next()) {
				boolean enabled = rs.getBoolean("ENABLED");
				if(enabled) {
					return true;
				}
				else {
					return false;
				}
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	
	
	public static boolean raidIsAlreadyActive(String UUID) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT ENABLED FROM missions WHERE MISSIONID='" + UUID + "'");
			if (rs.next()) {
				if(rs.getBoolean("ENABLED") == true) {
					return true;
				}
				else {
					return false;
				}
				
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static boolean missionExists(String UUID) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT MISSIONID FROM missions WHERE MISSIONID='" + UUID + "'");
			if (rs.next()) {
				return rs.getString("MISSIONID") != null;
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static boolean twitterLinked(String UUID) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TWITTER FROM twitterAccounts WHERE DISCORD_ID='" + UUID + "'");
			if (rs.next()) {
				return rs.getString("TWITTER") != null;
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	

	
		public static String getGuildIdFromMission(String missions_id) {
			try {
				ResultSet rs = BonkRaids.mysql.query("SELECT GUILD FROM missions WHERE MISSIONID='" + missions_id + "'");
				if (rs.next()) {
					String s = rs.getString("GUILD");
					if (s != null)
						return s;
				}
			} catch (SQLException localSQLException) {
				return null;
			}
			return null;
			
		}
		

		public static String getRaidNameFromMission(String missions_id) {
			try {
				ResultSet rs = BonkRaids.mysql.query("SELECT RAIDNAME FROM missions WHERE MISSIONID='" + missions_id + "'");
				if (rs.next()) {
					String s = rs.getString("RAIDNAME");
					if (s != null)
						return s;
				}
			} catch (SQLException localSQLException) {
				return null;
			}
			return null;
			
		}

	public static void removeTwitterTag(String id) {
		try {
			List<Object> params1 = new ArrayList<Object>();
			params1.add(id);
			BonkRaids.mysql.update("DELETE FROM twitterAccounts WHERE DISCORD_ID=?", params1);
		} catch (Exception e) {
			return;
		}
	}

	

	public static void newTweetTimeline(Long id) {
		List<Object> params1 = new ArrayList<Object>();
		params1.add(id);
		BonkRaids.mysql.update("INSERT INTO tweetTimeline(ID) VALUES (?)", params1);
	}
	
	public static void newLinkedWallet(String owner, String wallet) {
		List<Object> params1 = new ArrayList<Object>();
		params1.add(owner);
		BonkRaids.mysql.update("INSERT INTO bonkRaidsWallets(OWNER) VALUES (?)", params1);
		
		List<Object> params2 = new ArrayList<Object>();
		params2.add(wallet);
		params2.add(owner);
		BonkRaids.mysql.update("UPDATE bonkRaidsWallets SET WALLET=? WHERE OWNER=?", params2);
	}

	public static void newPointWallet(String discord_id, Integer points) {
		List<Object> params1 = new ArrayList<Object>();
		params1.add(discord_id);
		BonkRaids.mysql.update("INSERT INTO points(DISCORD_ID) VALUES (?)", params1);

		List<Object> params2 = new ArrayList<Object>();
		params2.add(points);
		params2.add(discord_id);
		BonkRaids.mysql.update("UPDATE points SET POINTS=? WHERE DISCORD_ID=?", params2);
	}
	
	
	public static void newMission(String missionid, String twitterLink, String toFollow, double follow, double retweet, double like, String raidType, boolean enabled, int maxRaids, long raidend, String guild, String raidName, Long channel) {
		List<Object> params1 = new ArrayList<Object>();
		params1.add(missionid);
		BonkRaids.mysql.update("INSERT INTO missions(MISSIONID) VALUES (?)", params1);

		List<Object> params2 = new ArrayList<Object>();
		params2.add(twitterLink);
		params2.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET LINK=? WHERE MISSIONID=?", params2);
		
		List<Object> params3 = new ArrayList<Object>();
		params3.add(toFollow);
		params3.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET TOFOLLOW=? WHERE MISSIONID=?", params3);
		
		List<Object> params4 = new ArrayList<Object>();
		params4.add(follow);
		params4.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET FOLLOWAMOUNT=? WHERE MISSIONID=?", params4);
		
		List<Object> params5 = new ArrayList<Object>();
		params5.add(retweet);
		params5.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET RETWEETAMOUNT=? WHERE MISSIONID=?", params5);
		
		List<Object> params6 = new ArrayList<Object>();
		params6.add(like);
		params6.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET LIKEAMOUNT=? WHERE MISSIONID=?", params6);
		
		List<Object> params7 = new ArrayList<Object>();
		params7.add(raidType);
		params7.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET RAIDTYPE=? WHERE MISSIONID=?", params7);
		
		List<Object> params8 = new ArrayList<Object>();
		params8.add(enabled);
		params8.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET ENABLED=? WHERE MISSIONID=?", params8);
		
		List<Object> params9 = new ArrayList<Object>();
		params9.add(0);
		params9.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET RAIDS=? WHERE MISSIONID=?", params9);
		
		List<Object> params10 = new ArrayList<Object>();
		params10.add(maxRaids);
		params10.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET MAXRAIDS=? WHERE MISSIONID=?", params10);
		
		List<Object> params12 = new ArrayList<Object>();
		params12.add(raidend);
		params12.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET ENDING=? WHERE MISSIONID=?", params12);
		
		List<Object> params13 = new ArrayList<Object>();
		params13.add(raidName);
		params13.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET RAIDNAME=? WHERE MISSIONID=?", params13);
		
		List<Object> params14 = new ArrayList<Object>();
		params14.add(guild);
		params14.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET GUILD=? WHERE MISSIONID=?", params14);
		
		List<Object> params15 = new ArrayList<Object>();
		params15.add(channel);
		params15.add(missionid);
		BonkRaids.mysql.update("UPDATE missions SET CHANNEL=? WHERE MISSIONID=?", params15);
		
		
	}
	
	public static String getParticipants(String id) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT PARTICIPANTS FROM missions WHERE MISSIONID='" + id + "'");
			if (rs.next()) {
				String s = rs.getString("PARTICIPANTS");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getUserWallet(String id) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT WALLET FROM bonkRaidsWallets WHERE OWNER='" + id + "'");
			if (rs.next()) {
				String s = rs.getString("WALLET");
				if (s != null) {
					return s;
				}
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Long getChannelIdFromMission(String id) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT CHANNEL FROM missions WHERE MISSIONID='" + id + "'");
			if (rs.next()) {
				Long s = rs.getLong("CHANNEL");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static void updateParticipantsMissions(String list, String id) {
		BonkRaids.mysql.update("UPDATE missions SET PARTICIPANTS='" + list + "'WHERE MISSIONID='" + id + "'");
	}
	
	public static void newTwitter(String discord_id, String twitter) {
		List<Object> params1 = new ArrayList<Object>();
		params1.add(discord_id);
		BonkRaids.mysql.update("INSERT INTO twitterAccounts(DISCORD_ID) VALUES (?)", params1);

		List<Object> params2 = new ArrayList<Object>();
		params2.add(twitter);
		params2.add(discord_id);
		BonkRaids.mysql.update("UPDATE twitterAccounts SET TWITTER=? WHERE DISCORD_ID=?", params2);
	}
	
	public static void enableRaid(String id) {
		List<Object> params2 = new ArrayList<Object>();
		params2.add(true);
		params2.add(id);
		BonkRaids.mysql.update("UPDATE missions SET ENABLED=? WHERE MISSIONID=?", params2);
	}
	
	public static void updateCurrentRaids(String id) {
		
		Integer currentRaids = getCurrentRaids(id);
		List<Object> params2 = new ArrayList<Object>();
		params2.add(currentRaids + 1);
		params2.add(id);
		BonkRaids.mysql.update("UPDATE missions SET RAIDS=? WHERE MISSIONID=?", params2);
	}
	
	public static Integer getCurrentRaids(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT RAIDS FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				Integer s = rs.getInt("RAIDS");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Integer getMaxRaids(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT MAXRAIDS FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				Integer s = rs.getInt("MAXRAIDS");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getTwitterLinkFromMissionID(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT LINK FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("LINK");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getRaidType(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT RAIDTYPE FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("RAIDTYPE");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getToFollowFromMissionID(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TOFOLLOW FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("TOFOLLOW");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Double getMissionsReweetAmount(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT RETWEETAMOUNT FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				Double s = rs.getDouble("RETWEETAMOUNT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Double getMissionsFollowAmount(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT FOLLOWAMOUNT FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				Double s = rs.getDouble("FOLLOWAMOUNT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Double getMissionsLikeAmount(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT LIKEAMOUNT FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				Double s = rs.getDouble("LIKEAMOUNT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Double getMissionsCommentAmount(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT COMMENTAMOUNT FROM missions WHERE MISSIONID='" + uuid + "'");
			if (rs.next()) {
				Double s = rs.getDouble("COMMENTAMOUNT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String checkIfTwitterIsAlreadyLinkedToAnotherAccount(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TWITTER FROM twitterAccounts WHERE TWITTER='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("TWITTER");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getTwitterTag(String uuid) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TWITTER FROM twitterAccounts WHERE DISCORD_ID='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("TWITTER");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	
	public static Long getRaidEndingTimestamp(String id) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT ENDING FROM missions WHERE MISSIONID='" + id + "'");
			if (rs.next()) {
				Long s = rs.getLong("ENDING");
				return s;
				
			}
		} catch (SQLException sQLException) {
		}
		return 0l;
	}
	
	public static void deleteRaid(String id) {
		try {
			List<Object> params1 = new ArrayList<Object>();
			params1.add(id);
			BonkRaids.mysql.update("DELETE FROM missions WHERE MISSIONID=?", params1);
		} catch (Exception e) {
			return;
		}
	}
	
	public static String getTokenNameByAddress(String name) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TOKENNAME FROM tokens WHERE TOKENADDRESS='" + name + "'");
			if (rs.next()) {
				String s = rs.getString("TOKENNAME");
				return s;
			}
		} catch (SQLException sQLException) {
			return "ERROR";
		}
		return "ERROR";
	}
	
	public static String getTokenAddressFromName(String name) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT TOKENADDRESS FROM tokens WHERE TOKENNAME='" + name + "'");
			if (rs.next()) {
				String s = rs.getString("TOKENADDRESS");
				return s;
			}
		} catch (SQLException sQLException) {
			return "ERROR";
		}
		return "ERROR";
	}
	
	
	public static void stopMission(String id) {
		List<Object> params = new ArrayList<Object>();
		params.add(id);
		BonkRaids.mysql.update("UPDATE missions SET ENABLED=" + false + " WHERE MISSIONID=?", params);
	}
	
	public static String getParticipantsRaffle(String id) {
		try {
			ResultSet rs = BonkRaids.mysql.query("SELECT PARTICIPANTS FROM missions WHERE RAFFLE_ID='" + id + "'");
			if (rs.next()) {
				String s = rs.getString("PARTICIPANTS");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static ArrayList getAllRunningMissions() {
		try {
			ArrayList list = new ArrayList();
			boolean running = true;
			ResultSet rs = BonkRaids.mysql.query("SELECT MISSIONID FROM missions WHERE ENABLED=" + running);
			while (rs.next()) {
				String s = rs.getString("MISSIONID");
				list.add(s);
			}
			return list;
		} catch (SQLException localSQLException) {
		}
		return null;
	}
	
}
