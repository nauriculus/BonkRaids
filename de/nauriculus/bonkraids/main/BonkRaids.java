package de.nauriculus.bonkraids.main;

import java.awt.Color;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import de.nauriculus.bonkraids.listener.CommandListener;
import de.nauriculus.bonkraids.mysql.MySQL;
import de.nauriculus.bonkraids.mysql.MySQLStatements;
import de.nauriculus.bonkraids.utils.TwitterAPI;
import de.nauriculus.bonkraids.utils.WalletAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import twitter4j.TwitterException;

public class BonkRaids {

	public static JDA jda;
	public static MySQL mysql;

	public MySQL getMySQL() {
	  return mysql;
	}

	//Connect to the database and create tables if missing
	public static void connectMySQL() {
	   mysql = new MySQL(MySQL.HOST, MySQL.DATABASE, MySQL.USER, MySQL.PASSWORD);
	   mysql.update("CREATE TABLE IF NOT EXISTS missions(MISSIONID text, CHANNEL long, GUILD text, RAIDNAME text, PARTICIPANTS text, ENDING long, LINK text, TOFOLLOW text, FOLLOWAMOUNT double, RETWEETAMOUNT double , LIKEAMOUNT double, RAIDTYPE text, ENABLED boolean, RAIDS int, MAXRAIDS int)", new ArrayList<Object>());
	   mysql.update("CREATE TABLE IF NOT EXISTS twitterAccounts(DISCORD_ID text, TWITTER text)", new ArrayList<Object>());
	   mysql.update("CREATE TABLE IF NOT EXISTS tokens(TOKENNAME text, TOKENADDRESS text)", new ArrayList<Object>());
	}

	
	public static void main(String[] args) {
		try {
			connectMySQL(); // Connect to database
		} catch (Exception e) {
			System.out.println("" + e.getMessage());
			System.exit(1);
		}

		 JDABuilder builder = null;
		 builder = JDABuilder.createDefault(""); // Your Discord Bot Secret Key
		 builder.setStatus(OnlineStatus.ONLINE);
		 
		 builder.addEventListeners(new CommandListener());

		 
		 try {
			jda = builder.build(); // Start jda
		 } catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		
		 TwitterAPI.start();
		 //Raffles.start();
		 
		 try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } 
		 
		 List<CommandData> commandData = new ArrayList<>();
	     commandData.add(Commands.slash("unlink", "Unlink your twitter account"));
         
	     OptionData titleOption = new OptionData(OptionType.STRING, "title", "The mission's title", true);
	   
	     OptionData twitterHandleOption = new OptionData(OptionType.STRING, "followeetag", "The twitter account to follow", false);
	     OptionData twitterLinkOption = new OptionData(OptionType.STRING, "twitterlink", "The twitter link", true);
	  
	     OptionData channel = new OptionData(OptionType.CHANNEL, "channel", "The channel where the raid message gets posted", true);
	     
	     OptionData descOption = new OptionData(OptionType.STRING, "description", "description", true);
	     
	     OptionData ending = new OptionData(OptionType.STRING, "raidduration", "unix timestamp for the raiding duration", true).setAutoComplete(true);
	     OptionData retweetAmount = new OptionData(OptionType.STRING, "retweetamount", "retweet (SOL or SPL-TOKEN) amount", true);
	     OptionData likeAmount = new OptionData(OptionType.STRING, "likeamount", "(SOL or SPL-TOKEN) amount", true);
	     
	     OptionData raiderAmount = new OptionData(OptionType.INTEGER, "raiders", "The max amount of raiders you want", true);
	     OptionData raidType = new OptionData(OptionType.STRING, "rewardtype", "Raid-Type: SOL or SPL (TOKEN NAME REQUIRED)", true).setAutoComplete(true);
	     
	     OptionData followAmount = new OptionData(OptionType.STRING, "followamount", "follow (SOL or SPL-TOKEN) amount", false);
	   
		 commandData.add(Commands.slash("createraid", "Start a new raid").addOptions(titleOption,descOption, channel,ending, raiderAmount, raidType, twitterLinkOption, retweetAmount, likeAmount, twitterHandleOption, followAmount));
	     
		 OptionData acc = new OptionData(OptionType.STRING, "twittertag", "Your twitter account (tag) you want to link", true);
		 OptionData wallet = new OptionData(OptionType.STRING, "wallet", "Your Solana wallet you want to link", true);
		    
		 
		 commandData.add(Commands.slash("link", "Link your twitter account").addOptions(acc));
		 commandData.add(Commands.slash("wallet", "Link your Solana wallet").addOptions(wallet));
		  
		 commandData.add(Commands.slash("setup", "Setup the server's house wallet"));
		 
		 commandData.add(Commands.slash("housewallet", "Check the server's house wallet information"));
		 commandData.add(Commands.slash("help", "Gives you an overview of all commands of commands"));
		 
		 //Update commands
		 jda.updateCommands().addCommands(commandData).queue();
		     
		     try {
				Thread.sleep(5000);
			 } catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			 }
		     

	 		Timer timer111111 = new Timer();
	 		TimerTask hourlyTask111111 = new TimerTask() {
	 			@Override
	 			public void run() {
	 					ArrayList list = MySQLStatements.getAllRunningMissions();
	 					for (int i = 0; i < list.size(); i++) {
	 			 		    
	 						 try {
	 						 String id = (String) list.get(i);
	 			 		    
	 			 		     String participants = MySQLStatements.getParticipants(id);
	 			 		     List<String> myList = null;
	 			 		     if(participants == null) {
	 			 		    	 myList = new ArrayList<>();
	 			 		     }
	 			 		     
	 			 		     String guildId = MySQLStatements.getGuildIdFromMission(id);
	 			 		   
	 			 		     if(participants != null) {
	 				 		 
	 			 		     String replace = participants.replace("[","");
	 			 		     String replace1 = replace.replace("]","");
	 			 		     
	 			 		 
	 			 		     myList = new ArrayList<String>(Arrays.asList(replace1.split(",")));
	 			 		     }
	 			 		     
	 			 		     
	 			 		     Long s = MySQLStatements.getRaidEndingTimestamp(id);
	 			 		     Date time = new Date((long)s*1000);
	 			 			
	 			 		     int all = myList.size();
	 						 Date currentDate = new Date();
	 						 if (currentDate.after(time)) {
	 							    
	 							 	System.out.println("ending starting process...");
	 							 	int winners = MySQLStatements.getMaxRaids(id);

	 							    String raidName = MySQLStatements.getRaidNameFromMission(id);
	 							    EmbedBuilder builder = new EmbedBuilder();
	 							    builder.setTitle("**RAID RESULTS**");
	 							   
	 								builder.setColor(Color.yellow); 									
 									builder.setFooter("Powered by BonkRaids","https://cdn.discordapp.com/attachments/1161769361334341664/1224035490207105116/Neuefs_Projekt.png?ex=661c06e2&is=660991e2&hm=eaef5902fe54b94fc35a982fb3c315cac7710dccb8e2ed75986e6b6fc860c1f6&");
 									
	 							    builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
	 							    StringBuilder descriptionBuilder = new StringBuilder();
	 				
	 								String link = MySQLStatements.getTwitterLinkFromMissionID(id);
	 								String tofollow = MySQLStatements.getToFollowFromMissionID(id);
	 								
	 								Long tweetId = 0l;
	 								tweetId = TwitterAPI.getTweetIdFromLink(link);
	 								
	 								if(tweetId == 0l) {
	 									System.out.println("tweetid is invaild");
	 									return;
	 								}
	 								
	 							
	 								boolean disable = false;
	 								
	 								double totalRewarded = 0.0;
	 								
	 								if(all >= 1) {
	 									
	 								ArrayList likers;
	 								ArrayList retweeters;
	 								
	 								try {
	 								 likers = TwitterAPI.getAllLikingUsers(tweetId);
		 							 retweeters = TwitterAPI.getAllRetweetingUsers(tweetId);	
		 							 
		 							 disable = true;
	 								}

	 								catch(Exception e) {
	 									likers = TwitterAPI.getAllLikingUsers(tweetId);
			 							retweeters = TwitterAPI.getAllRetweetingUsers(tweetId);
	 								}
	 								
		 							double likeAmount = MySQLStatements.getMissionsLikeAmount(id);
		 							double retweetAmount = MySQLStatements.getMissionsReweetAmount(id);
		 							double followAmount = MySQLStatements.getMissionsFollowAmount(id);
		 							
		 							String raidType = MySQLStatements.getRaidType(id);
		 							//String tokenAddress = MySQLStatements.getTokenAddressFromName(raidType);
		 						
		 							String houseWallet = WalletAPI.getWalletWithoutPIN(guildId);
		 						
		 							int count = 0;
		 							for (String users: myList) {
	 							    	try {
		 								String user = users.replaceAll("\\s+", "");
		 								System.out.println("" + user);
		 								
		 								if(count == winners) {
		 									break;
		 								}
		 								
		 								count++;
		 								
	 							        boolean followed = false;
	 									boolean retweeted = false;
	 									boolean liked = false;
	 									
	 							        String userTag = MySQLStatements.getTwitterTag(user);
		 								
		 								System.out.println("" + retweeters.toString());
		 								System.out.println("" + likers.toString());
		 						
		 								
		 								if (!link.equalsIgnoreCase("None")) {
		 						            if (containsIgnoreCase(retweeters, userTag)) {
		 						                retweeted = true;
		 						            }

		 						            if (containsIgnoreCase(likers, userTag)) {
		 						                liked = true;
		 						            }
		 						        }
		 								
		 								if(!tofollow.equalsIgnoreCase("None")) {
		 								try {
		 									if(TwitterAPI.checkIfUserIsFollowing(userTag, tofollow)) {
		 										followed = true;
		 									}
		 								} catch (TwitterException e) {
		 									// TODO Auto-generated catch block
		 									e.printStackTrace();
		 									}
		 								}
		 								
		 								double rewarded = 0.0;
		 								if(liked) {
		 									rewarded = rewarded + likeAmount;
		 								}
		 									
		 								if(retweeted) {		
		 									rewarded = rewarded + retweetAmount;
		 								}
		 									
		 							    if(followed) {	
		 							    	rewarded = rewarded + followAmount;
		 							    }
		 							    
		 							   
		 							    totalRewarded = totalRewarded + rewarded;
	 							       
		 								if(rewarded <= 0.0 || rewarded == 0 || rewarded == 0.0) {
		 								  continue;
		 								}
		 								
		 							
		 								
		 								String wallet = MySQLStatements.getUserWallet(user);
		 								System.out.println("user wallet "+ wallet);
		 								
		 								if(rewarded > 0.0) {
		 									descriptionBuilder.append("\n<@").append(user).append(">");
		 									if(!raidType.equalsIgnoreCase("SOL")) {
		 										
		 										double tokenValue = rewarded;
		 										System.out.println(tokenValue);
		 										
		 										
		 										WalletAPI.sendSPL(houseWallet, wallet, "" + tokenValue, guildId);
		 									 }
		 								  }
	 							    	}catch(Exception e) {
	 							    		disable = false; 
	 							    	}
	 							    	
	 							    	disable = true;
	 							    	
	 							    	if(disable) {
		 									MySQLStatements.stopMission(id);
		 	 							    System.out.println("Raid with id: " + id + " has been stopped successfully.");
		 								  }
	 							       }
	 								}

	 								if(all >= 0) {
	 							    builder.setDescription("The raid " + raidName + " ended.");
	 							    
 									String raidType = MySQLStatements.getRaidType(id);
 									
 									if(raidType.equalsIgnoreCase("SOL")) {
 									
 		 								
	 							    builder.addField("Total rewarded", "» " + totalRewarded + " SOL", false);
	 							    
	 								}
 									else {
 		 							builder.addField("Total rewarded", "» " + totalRewarded + " " + raidType.toUpperCase() + "", false);
 									}
 									
 									
 									builder.addField("Total raiders", "» "+ all +"", false);
	 							    }
	 								
	 								if(all == 0) {
	 								   builder.setDescription("The raid " + raidName + " ended. No raiders were selected!");
	 								}
	 								
	 								Long channel = MySQLStatements.getChannelIdFromMission(id);
	 								TextChannel tc = jda.getTextChannelById(channel);
	 								tc.sendMessageEmbeds(builder.build()).queue();   
	 							    
	 							}
	 						 }
	 						 catch(Exception e) {
	 			    			  System.out.println("There was an issue posting the ending raid embed." + e.getMessage());
	 			    			  continue;
	 						 }
	 				}
	 		}
	 		};

	 	    timer111111.schedule(hourlyTask111111, 0l, 20000);	
	    
	}
	
	
	private static boolean containsIgnoreCase(ArrayList<String> list, String item) {
	    for (String listItem : list) {
	        if (listItem.equalsIgnoreCase(item)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
}