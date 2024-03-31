package de.nauriculus.bonkraids.listener;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.nauriculus.bonkraids.mysql.MySQLStatements;
import de.nauriculus.bonkraids.utils.TwitterAPI;
import de.nauriculus.bonkraids.utils.WalletAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CommandListener extends ListenerAdapter {
	  
	  private static String createAuthHeader() {
	     return "Bearer " + TwitterAPI.BEARER_TOKEN;
	  }
	  
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
	    OptionMapping raidDurationOption = event.getOption("raidduration");
	    OptionMapping raidTypeOption = event.getOption("rewardtype");
	  
	    ArrayList<String> tempList = new ArrayList<>();
	    tempList.clear();
	    
	    if (raidDurationOption != null) {
	        // Populate choices for raidduration
	    	tempList.clear();
	        long now = Instant.now().getEpochSecond();
	        
	        tempList.add("" + (now + 5 * 60) * 1000 + " | 5 Minutes"); // 5 minutes
	        tempList.add("" + (now + 30 * 60) + " | 30 Minutes"); // 30 minutes
	        tempList.add("" + (now + 60 * 60) + " | 1 Hour"); // 1 hour
	        tempList.add("" + (now + 3 * 60 * 60) + " | 3 Hours"); // 3 hours
	        tempList.add("" + (now + 5 * 60 * 60) + " | 5 Hours"); // 5 hours
	        tempList.add("" + (now + 24 * 60 * 60) + " | 24 Hours"); // 24 hours
	    }

	    if (raidTypeOption != null) {
	        // Populate choices for raidtype
	    	tempList.clear();
	        tempList.add("BONK"); 
	    }

	    event.replyChoiceStrings(tempList).queue();
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if(event.getInteraction() == null) {
			return;
		}
				
		if (event.getComponentId().contains("ConfirmRaid")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);
			
			String uuid = event.getComponentId();
			String[] parts = uuid.split("-");

			String id = parts[1]; // extract the second part of the split string
			String amount = parts[2]; // extract the third part of the split string
			
			if(!MySQLStatements.missionExists(id)) {
				hook.sendMessage("This raid was already canceled.").queue();
				return;
			}
			
			if(MySQLStatements.raidIsAlreadyActive(id)) {
				hook.sendMessage("This raid was already activated.").queue();
				return;
			}
			String raidType = MySQLStatements.getRaidType(id);
			
			
			if(!raidType.equalsIgnoreCase("SOL")) {
				String wallet = WalletAPI.getWalletWithoutPIN(event.getGuild().getId());
				Double neededAmountDouble = Double.parseDouble(amount);
					
					String payload = "{\"method\":\"getTokenAccountsByOwner\",\"jsonrpc\":\"2.0\",\"params\":[\"" + wallet + "\",{\"programId\":\"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\"},{\"encoding\":\"jsonParsed\",\"commitment\":\"processed\"}],\"id\":\"0e1a60c3-97ec-4c73-a40f-a339713276d7\"}";
					
					MediaType mediaType = MediaType.parse("application/json");
					RequestBody body = RequestBody.create(mediaType, payload);
					
					OkHttpClient client = new OkHttpClient().newBuilder()
						    .connectTimeout(1,TimeUnit.MINUTES)
						    .writeTimeout(1,TimeUnit.MINUTES)
						    .readTimeout(1,TimeUnit.MINUTES)
						    .build();
					 		
					Request request = new Request.Builder()
							.url("https://rpc.helius.xyz/?api-key=d0d35862-92d4-42be-a493-975136d562ed")
							   .header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
							
							 .addHeader("accept", "application/json, text/plain, */*")
							 .addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
							 .addHeader("content-type", "text/plain")
							 .addHeader("sec-ch-ua", "Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104")
							 .addHeader("sec-ch-ua-mobile", "?0")
							 .addHeader("sec-ch-ua-platform", "macOS")
							 .addHeader("sec-fetch-dest", "empty")
							 .addHeader("sec-fetch-mode", "cors")
							 .addHeader("sec-fetch-site", "same-site")
							 .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
							 .method("POST", body).build();
					Response response = null;
					try {
						response = client.newCall(request).execute();
					} catch (IOException e2) {
						return;
					}
					
					String s = null;
					try {
						s = response.body().string();
					
					} catch (IOException e2) {
						return;
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = null;
					try {
						jsonNode = objectMapper.readTree(s);
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						
					}

					boolean found = false;
					JsonNode valueNode = jsonNode.get("result").get("value");
					for (JsonNode accountNode : valueNode) {
					    JsonNode dataNode = accountNode.get("account").get("data");
					    JsonNode parsedNode = dataNode.get("parsed");
					    String mintAddress = parsedNode.get("info").get("mint").asText();
					    double uiAmount = parsedNode.get("info").get("tokenAmount").get("uiAmount").asDouble();

					    if(MySQLStatements.tokenAddressExists(mintAddress)) {
					    	String tokenName = MySQLStatements.getTokenNameByAddress(mintAddress);
					    	if(tokenName.equalsIgnoreCase(raidType)) {
					    		
					    		found = true;
					    		if(uiAmount >= neededAmountDouble) {
					    		MySQLStatements.enableRaid(id);
								hook.sendMessage("You successfully enabled this raid.").queue();
								return;
					    		}
					    		else {
					    			double missing = uiAmount - neededAmountDouble;
					    			hook.sendMessage("The " + raidType.toUpperCase() +" balance of the project's house wallet is insufficient. You are missing: " + missing + " tokens").queue();
									return;	
					    		}
					    	}
					    }
					    
					}
					
					if(!found) {
						hook.sendMessage("The " + raidType.toUpperCase() +" balance of the project's house wallet is insufficient.").queue();
						return;	
					}
					
					
				
				}
		}

		
		if (event.getComponentId().contains("CancelRaid")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);
			
			String uuid = event.getComponentId();
			String[] parts = uuid.split("-");

			String id = parts[1]; // extract the second part of the split string
			String channelID = parts[2]; // extract the third part of the split string
			
			if(MySQLStatements.raidIsAlreadyActive(id)) {
				hook.sendMessage("This raid was already activated and therefore can't be canceled anymore. Please remove the message if you really want to stop the raid.").queue();
				return;
			}
			if(MySQLStatements.missionExists(id)) {
				MySQLStatements.deleteRaid(id);
				
				// get the channel you want to delete messages from
				TextChannel channel = event.getGuild().getTextChannelById(channelID);

				try {// retrieve the last 2 messages in the channel
				List<Message> messages = channel.getHistory().retrievePast(2).complete();

				// delete the messages
				for (Message message : messages) {
				    message.delete().queue();
				}
				}
				catch(Exception e) {
					
				}
				
				hook.sendMessage("The raid was canceled successfully!").queue();
				return;
			}
			else {
				hook.sendMessage("This raid doesn't exist anymore.").queue();
				return;
			}
			
			
		}
		
		if(event.getComponentId().equalsIgnoreCase("Submit")) {
			event.deferReply(true).queue();
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);
			
			String uuid = event.getMessageId();
			
			if(!MySQLStatements.checkIfRaidIsActive(uuid)) {
				hook.sendMessage("Apologies, but this raid is not currently active or has already ended.").queue();
				return;
			}
			
			if(!MySQLStatements.twitterLinked(event.getUser().getId())) {
				hook.sendMessage("Prior to claiming rewards, kindly link your Twitter account by utilizing the /link command.").queue();
				return;
			}
			

			String exists = WalletAPI.getWalletWithoutPIN(event.getUser().getId());
			if(exists.equalsIgnoreCase("ERROR")) {
				hook.sendMessage("Prior to claiming rewards, kindly create a wallet using /create.").queue();
				return;
			}
			
			 String participants = MySQLStatements.getParticipants(uuid);

	 		 ArrayList<String> myList = null;
	 		 if(participants == null) {
				myList = new ArrayList<String>();  
				myList.add(event.getMember().getId());  
			   }
			   
			   if(participants != null) {
				
				if(participants.contains(event.getMember().getId())) {
					hook.sendMessage("You already joined this raid.").queue();
					return;
				}
		 		String replace = participants.replace("[","");
		 		String sss = replace.replace("]","");
		 		String part1 = sss.replaceAll("\\s+","");
		 		myList = new ArrayList<String>(Arrays.asList(part1.split(",")));
		 		myList.add(event.getMember().getId()); 
			   }
			   
			   
			   MySQLStatements.updateParticipantsMissions(myList.toString(), uuid);
			   hook.sendMessage("You successfully joined this raid. Make sure to complete all the tasks before the raid ends!").queue();
			   return;
		}
	}
	
	
	@Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		 if(event.isAcknowledged()) {
			 return;
		 }
		 
		 if(event.getInteraction() == null) {
			return;
		}
		 
		
		 
		 if (event.getName().equalsIgnoreCase("housewallet")) {
		      
	        	ExecutorService executorService = Executors.newSingleThreadExecutor();			
	        	executorService.execute(new Runnable() {
				  public void run() {
					  	event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
						InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
						hook.setEphemeral(true);
						
						String exists = WalletAPI.getWalletWithoutPIN(event.getGuild().getId());

						if(exists.equalsIgnoreCase("ERROR")) {
							hook.sendMessage("This server wasn't setup yet. Make sure to use /setup before!").queue();
							return;
						}
					
					
						String wallet = WalletAPI.getWalletWithoutPIN(event.getGuild().getId());

						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle(event.getGuild().getName().toUpperCase() + " HOUSE WALLET");
						builder.setDescription("Solana-Address: ``" + wallet + "``");	

						String payload = "{\"method\":\"getTokenAccountsByOwner\",\"jsonrpc\":\"2.0\",\"params\":[\"" + wallet + "\",{\"programId\":\"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\"},{\"encoding\":\"jsonParsed\",\"commitment\":\"processed\"}],\"id\":\"0e1a60c3-97ec-4c73-a40f-a339713276d7\"}";
						
						MediaType mediaType = MediaType.parse("application/json");
						RequestBody body = RequestBody.create(mediaType, payload);
						
						OkHttpClient client = new OkHttpClient().newBuilder()
							    .connectTimeout(1,TimeUnit.MINUTES)
							    .writeTimeout(1,TimeUnit.MINUTES)
							    .readTimeout(1,TimeUnit.MINUTES)
							    .build();
							    		
						Request request = new Request.Builder()
								.url("https://rpc.helius.xyz/?api-key=d0d35862-92d4-42be-a493-975136d562ed")
								   .header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
								
								 .addHeader("accept", "application/json, text/plain, */*")
								 .addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
								 .addHeader("content-type", "text/plain")
								 .addHeader("sec-ch-ua", "Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104")
								 .addHeader("sec-ch-ua-mobile", "?0")
								 .addHeader("sec-ch-ua-platform", "macOS")
								 .addHeader("sec-fetch-dest", "empty")
								 .addHeader("sec-fetch-mode", "cors")
								 .addHeader("sec-fetch-site", "same-site")
								 .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
								 .method("POST", body).build();
						Response response = null;
						try {
							response = client.newCall(request).execute();
						} catch (IOException e2) {
							return;
						}
						
						String s = null;
						try {
							s = response.body().string();
						
						} catch (IOException e2) {
							return;
						}

						
						
						ObjectMapper objectMapper = new ObjectMapper();
						JsonNode jsonNode = null;
						try {
							jsonNode = objectMapper.readTree(s);
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						JsonNode valueNode = jsonNode.get("result").get("value");
						for (JsonNode accountNode : valueNode) {
						    JsonNode dataNode = accountNode.get("account").get("data");
						    JsonNode parsedNode = dataNode.get("parsed");
						    String mintAddress = parsedNode.get("info").get("mint").asText();
						    double uiAmount = parsedNode.get("info").get("tokenAmount").get("uiAmount").asDouble();

						    if(MySQLStatements.tokenAddressExists(mintAddress)) {
						    	String tokenName = MySQLStatements.getTokenNameByAddress(mintAddress);
						    	builder.addField("Balance", "**" + uiAmount + "** " + tokenName.toUpperCase(), false);
						    }   
						}
						    
						
						builder.addField(":warning: Reminder", "The funds deposited into this wallet are not eligible for withdrawal, as they are reserved for the purpose of financing the user's participation in raid rewards.", false);
						builder.setColor(Color.white);
						builder.setFooter("Powered by BonkRaids","https://cdn.discordapp.com/attachments/1161769361334341664/1224035490207105116/Neuefs_Projekt.png?ex=661c06e2&is=660991e2&hm=eaef5902fe54b94fc35a982fb3c315cac7710dccb8e2ed75986e6b6fc860c1f6&");
						
						builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
						
						hook.sendMessageEmbeds(builder.build()).queue();
						
				  }
				});
				executorService.shutdown();
	     	 }
			 
			 if (event.getName().equalsIgnoreCase("setup")) {
			      
		        	ExecutorService executorService = Executors.newSingleThreadExecutor();			
		        	executorService.execute(new Runnable() {
					  public void run() {
					 
						event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
						InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
						hook.setEphemeral(true);
							
						if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
							hook.sendMessage("You don't have enough permissions to execute this command!").queue();
							return;
						}
						
						String exists = WalletAPI.getWalletWithoutPIN(event.getGuild().getId());
						
						if(!exists.equalsIgnoreCase("ERROR")) {
							hook.sendMessage("This server was already setup.").queue();
							return;
						}
						
						if(!exists.equalsIgnoreCase("EXISTS")) {
							String wallet = WalletAPI.getUserWallet("", event.getGuild().getId());
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle("WALLET CREATED");
							builder.setDescription(":white_check_mark: Great news! Your projects house wallet was created.");
						
							
							builder.addField("Solana Address", "``" + wallet + "``", false);
							
							builder.setColor(Color.white);
							builder.setFooter("Powered by BonkRaids","https://cdn.discordapp.com/attachments/1161769361334341664/1224035490207105116/Neuefs_Projekt.png?ex=661c06e2&is=660991e2&hm=eaef5902fe54b94fc35a982fb3c315cac7710dccb8e2ed75986e6b6fc860c1f6&");
							
							builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
							
							hook.sendMessageEmbeds(builder.build()).queue();
							return;
					    	 
						}
						
						
					  }
					});
					executorService.shutdown();
		     }
		    
		  
		 
		 if (event.getName().equalsIgnoreCase("unlink")) {
			 event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);

				if(!MySQLStatements.twitterLinked(event.getUser().getId())) {	
					hook.sendMessage("There is no twitter account linked currently. Please link one using /link").queue();
					return;
				}
				else {
					MySQLStatements.removeTwitterTag(event.getUser().getId());
					hook.sendMessage("Your old twitter account was unlinked.").queue();
					return;
				}
		 }
		 
		 if (event.getName().equalsIgnoreCase("wallet")) {
			 event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);
				
				OptionMapping wallet = event.getOption("wallet");
				
				if(wallet == null){
					hook.sendMessage("Wallet wasnt specified").queue();
					return;
				}
				
				String walletAddress = wallet.getAsString();
				
				MySQLStatements.newLinkedWallet(event.getUser().getId(), walletAddress);
				hook.sendMessage("Your wallet was changed to : ``" + walletAddress + "`` successfully.").queue();
				
		 }
		
		 
		 if (event.getName().equalsIgnoreCase("link")) {
			 event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);
				
				OptionMapping twitter = event.getOption("twittertag");
				
				if(twitter == null){
					hook.sendMessage("Tag wasnt specified").queue();
					return;
				}
				
				String twitterS = twitter.getAsString();
				
				if(twitterS.contains("http")) {
					hook.sendMessage("Please remove the link and only provide your twitter tag without any @!").queue();
					return;
				}
				if(twitterS.contains("https")) {
					hook.sendMessage("Please remove the link and only provide your twitter tag without any @!").queue();
					return;
				}
				if (twitterS.charAt(0) == '@') {
					hook.sendMessage("Please remove the @ character!").queue();
					return;
				}
				if(!MySQLStatements.twitterLinked(event.getUser().getId())) {	
					String tag = MySQLStatements.checkIfTwitterIsAlreadyLinkedToAnotherAccount(twitterS);
					if(tag != null) {
						hook.sendMessage("Another user linked this account already. Use a different one!").queue();
						return;
					}
					MySQLStatements.newTwitter(event.getUser().getId(), twitterS);
					hook.sendMessage("Your twitter: ``" + twitterS + "`` was linked successfully.").queue();
				}
				else {
					hook.sendMessage("Another account is already linked.").queue();
					return;
				}
		 }
		 
		 if (event.getName().equalsIgnoreCase("createraid")) {
				event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);
			
				if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
					hook.sendMessage("You don't have enough permissions to execute this command!").queue();
					return;
				}
				
				OptionMapping title = event.getOption("title");
				
				OptionMapping desc = event.getOption("description");
			
				OptionMapping twitterhandle = event.getOption("followeetag");
				OptionMapping twitterLink = event.getOption("twitterlink");
				OptionMapping raidendtimestamp = event.getOption("raidduration");
				
				
				OptionMapping likeAmount = event.getOption("likeamount");
				OptionMapping retweetAmount = event.getOption("retweetamount");
				OptionMapping followAmount = event.getOption("followamount");
				
				OptionMapping raiders = event.getOption("raiders");
				OptionMapping raidType = event.getOption("rewardtype");
				
				OptionMapping channel = event.getOption("channel");
				
				if(title == null){
				   hook.sendMessage("Amount wasnt specified").queue();
				   return;
				}
				
				
				if(channel == null){
				  hook.sendMessage("channel wasnt specified").queue();
				  return;
				}
			
				String twitterTag = "";
				if(twitterhandle != null) {
				   twitterTag = twitterhandle.getAsString();
				}
				else {
					twitterTag = "None";
				}
				
				Double likeAmountI = 0.0;
				Double retweetAmountI = 0.0;
				Double followAmountI = 0.0;
				Long raidEndTime;
				
				try {
					
					String raidEndTimeInt = raidendtimestamp.getAsString();
					String input = raidEndTimeInt;
					if (input.contains("|")) {
					    String[] parts = input.split("\\s*\\|\\s*");
					    long timestamp = Long.parseLong(parts[0]);
					    raidEndTime = (timestamp);
					} else {
					    // handle the case where '|' is not present in the input string
					    long timestamp = Long.parseLong(input);
					    raidEndTime = (timestamp);
					}
				}
				catch(Exception e) {
					hook.sendMessage("raidEndTime is invaild.").queue();
					return;
				}
				
				try {
					likeAmountI = Double.parseDouble(likeAmount.getAsString());
				}
				catch(Exception e) {
					hook.sendMessage("Like amount is invaild").queue();
					return;
				}
				
				try {
					retweetAmountI = Double.parseDouble(retweetAmount.getAsString());
				}
				catch(Exception e) {
					hook.sendMessage("Reweet amount is invaild").queue();
					return;
				}
				
				try {
					if(followAmount != null) {
						followAmountI = Double.parseDouble(followAmount.getAsString());
					}
				}
				catch(Exception e) {
				}
				
				if(likeAmountI < 0) {
					hook.sendMessage("The like amount is invaild").queue();
					return;
				}
				
				if(retweetAmountI < 0) {
					hook.sendMessage("The retweet amount is invaild").queue();
					return;
				}
				
				if(followAmountI != 0.0) {
				if((followAmountI < 0)) {
					hook.sendMessage("The follow amount is invaild").queue();
					return;
					}
				}
				
				if(!raidType.getAsString().equalsIgnoreCase("SOL")) {
				if(!MySQLStatements.tokenExists(raidType.getAsString())) {
					hook.sendMessage("Couldn't find this token inside our database!").queue();
					return;
				  }
				}
				
				//Limit to prevent abuse
				
				/*if(raidType.getAsString().equalsIgnoreCase("Bonk")) {
					if((followAmountI > 187)) {
						hook.sendMessage("limit protection!! amount is too high.").queue();
						return;
						}
					
					if((likeAmountI > 187)) {
						hook.sendMessage("limit protection!! amount is too high.").queue();
						return;
						}
					if((retweetAmountI > 187)) {
						hook.sendMessage("limit protection!! amount is too high.").queue();
						return;
						}
				}*/
				
				
				String tLink;
				if(twitterLink != null) {
					tLink = twitterLink.getAsString();
				}
				else {
					tLink = "None";
				}
			
				
				TextChannel tc = channel.getAsTextChannel();
				
				int maxRaids = raiders.getAsInt();
				
				
				double raidReward = 0.0;
			
				List<Button> buttons = new ArrayList<Button>();
				if(!tLink.equalsIgnoreCase("None")) {
					buttons.add(Button.link(tLink, "❤️"));
					buttons.add(Button.link(tLink, "🔁"));
					buttons.add(Button.link(tLink, "💬"));
					
					raidReward = raidReward + retweetAmountI + likeAmountI;
				}
				
				
				
				List<Button> followButton = new ArrayList<Button>();
				if(!twitterTag.equalsIgnoreCase("None")) {

					Button customButton = Button.link(tLink, "➕");
					buttons.add(customButton);

					raidReward = raidReward + followAmountI;
				}
				
				Button customButton = Button.success("Submit", "Submit");
				followButton.add(customButton);
				
				// raid.setDescription(desc.getAsString());	
				
				ActionRow row1 = ActionRow.of(buttons);
				ActionRow row2 = ActionRow.of(followButton);
				
				
				String tweetUrl = tLink; 
				
				
				Long tId = TwitterAPI.getTweetIdFromLink(tweetUrl);
				
				if(tId == 0L) {
				   hook.sendMessage("There was an error. The provided tweet is invaild. Please try again!").queue();
				   return;
				}
				
				EmbedBuilder tweet = new EmbedBuilder();
				
	            String url = "https://api.twitter.com/2/tweets/" + tId + "?expansions=author_id,attachments.media_keys&media.fields=preview_image_url,url,type&user.fields=profile_image_url";
	            Request request = new Request.Builder()
	                    .url(url)
	                    .addHeader("Authorization", createAuthHeader())
	                    .build();

	            OkHttpClient httpClient = new OkHttpClient();
	            Response response = null;
				try {
					response = httpClient.newCall(request).execute();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
	            if (response.isSuccessful()) {
	                ResponseBody responseBody = response.body();
	                
	                if (responseBody != null) {
	                	org.json.JSONObject json = null;
						try {
							json = new org.json.JSONObject(responseBody.string());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                
	                	org.json.JSONObject data = json.getJSONObject("data");

	                    String tweetText = data.getString("text");
	                 
	                    org.json.JSONObject author = json.getJSONObject("includes").getJSONArray("users").getJSONObject(0);
	                    String authorName = author.getString("username");
	                    
	                    String pUrl = author.getString("profile_image_url");
	                    System.out.println("pUrl: " + pUrl);
	                    tweet.setTitle(""+ title.getAsString());
	                    tweet.setDescription(desc.getAsString() + "\n\n" + tweetText.toString());
	                    tweet.setAuthor(authorName, pUrl);

	                    Double totalAmount = raidReward * maxRaids;
	    				
	    				double num = totalAmount;
	    				DecimalFormat df = new DecimalFormat("#.###");
	    				String result = df.format(num);
	    				
	    				tweet.addField("\n\nTOTAL REWARDS", "" + "" + result + " " + raidType.getAsString().toUpperCase(), false);
	    				
	    				Long endTime = 0l;
	    				
	    				if(String.valueOf(raidEndTime).length() > 10) {
	    					endTime = (raidEndTime / 1000);
	    					raidEndTime = (raidEndTime / 1000);
	    				}
	    				else {
	    					endTime = raidEndTime;
	    				}
	    			
	    				tweet.addField("END", "" + "<t:" + endTime + ":R>", false);
	    				
	                    if (data.has("attachments")) {
	                        org.json.JSONObject attachments = data.getJSONObject("attachments");
	                        if (attachments.has("media_keys")) {
	                            org.json.JSONArray mediaKeys = attachments.getJSONArray("media_keys");
	                            List<Object> mediaObjects = json.getJSONObject("includes").getJSONArray("media").toList();
	                            mediaObjects = mediaObjects.stream()
	                                    .map(obj -> new org.json.JSONObject((Map<String, Object>) obj))
	                                    .collect(Collectors.toList());

	                            for (int i = 0; i < mediaKeys.length(); i++) {
	                                org.json.JSONObject mediaObject = (org.json.JSONObject) mediaObjects.get(i);
	                                if (mediaObject != null) {
	                               
	                                    if (mediaObject.has("preview_image_url")) {
	                                        String previewImageUrl = mediaObject.getString("preview_image_url");
	                                        System.out.println("previewImageUrl: " + previewImageUrl);
	                                        tweet.setImage(previewImageUrl);
	                                        break;
	                                    }
	                             
	                                    else if (mediaObject.has("url")) {
	                                        String imageUrl = mediaObject.getString("url");
	                                        System.out.println("imageUrl: " + imageUrl);
	                                        tweet.setImage(imageUrl);
	                                        break; 
	                                    }
	                                }
	                            }
	                        }
	                    }
	            } else {
	                System.out.println(response.code() + " " + response.message());
	            }

	          
	        } else {
	            System.out.println("Invalid tweet link.");
	        }
	            

	            
	            tweet.setColor(Color.white);
	            tweet.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
	            tweet.setFooter("Powered by BonkRaids","https://cdn.discordapp.com/attachments/1161769361334341664/1224035490207105116/Neuefs_Projekt.png?ex=661c06e2&is=660991e2&hm=eaef5902fe54b94fc35a982fb3c315cac7710dccb8e2ed75986e6b6fc860c1f6&");
	    		
	            if(!raidType.getAsString().equalsIgnoreCase("SOL")) {
					if(!MySQLStatements.tokenExists(raidType.getAsString())) {
					 hook.sendMessage("You can only use BONK!").queue();
					 return;
				}
				
				Message raidMessage = tc.sendMessageEmbeds(tweet.build()).setActionRows(row1, row2).complete();
				if (raidMessage.getId() != null) {
						    
						    String messageIdString = raidMessage.getId();
						    if (twitterhandle != null) {
						        if (tLink.equalsIgnoreCase("None")) {
						            MySQLStatements.newMission(messageIdString, "None", twitterhandle.getAsString(), followAmountI, retweetAmountI, likeAmountI, raidType.getAsString(), false, maxRaids, raidEndTime, event.getGuild().getId(), title.getAsString(), event.getChannel().getIdLong());
						        } else {
						            MySQLStatements.newMission(messageIdString, tLink, twitterhandle.getAsString(), followAmountI, retweetAmountI, likeAmountI,raidType.getAsString(), false, maxRaids, raidEndTime, event.getGuild().getId(), title.getAsString(), event.getChannel().getIdLong());
						        }
						    } else {
						        MySQLStatements.newMission(messageIdString, tLink, "None", followAmountI, retweetAmountI, likeAmountI, raidType.getAsString(), false, maxRaids, raidEndTime, event.getGuild().getId(), title.getAsString(), event.getChannel().getIdLong());
						    }
						} else {
						    hook.sendMessage("There was an error sending the raid body. Please try again!").queue();
						    try {
								List<Message> messages = event.getChannel().getHistory().retrievePast(2).complete();

								// delete the messages
								for (Message message : messages) {
								    message.delete().queue();
								}
								}
								catch(Exception e) {
									
								}
						    return;
						}
						
						int numRaiders = raiders.getAsInt();
	
						double amountPerRaider = raidReward;
	
						// Calculate the total amount of BONK to be awarded in this raid
						double totalRewardAmount = numRaiders * amountPerRaider;
	
						double rewardAmountRequired = (totalRewardAmount);
	
						// Display the result
						
						EmbedBuilder deposit = new EmbedBuilder(); 
						deposit.setTitle("CONFIRM RAID");
						deposit.setDescription("To run this raid for " + numRaiders + " users, you need " + rewardAmountRequired + " " + raidType.getAsString()+ " inside the house wallet.");
						deposit.setColor(Color.white);
					
						String messageIdString = raidMessage.getId();
						List<Button> depositButtons = new ArrayList<Button>();
						Button confirm = Button.success("ConfirmRaid-" + messageIdString + "-" + rewardAmountRequired , "Confirm Raid");
						Button cancel = Button.danger("CancelRaid-" + messageIdString +"-" + tc.getId() , "Cancel Raid");
						depositButtons.add(confirm);
						depositButtons.add(cancel);
						
						ActionRow row = ActionRow.of(depositButtons);
				
						hook.sendMessageEmbeds(deposit.build()).addActionRows(row).queue();
						
						
						return;
						}
		 }
	
		
		if (event.getName().equalsIgnoreCase("help")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);
		
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("BONKRAIDS HELP");

			builder.setDescription("Here is a list of all commands from the BOKU Raiding tool");
			builder.addField("/link", "Links your twitter handle to your discord account for raids", false);
			builder.addField("/unlink", "Unlinks your twitter handle", false);
			builder.addField("/createraid (Admin)", "Creates a new raid", false);
			builder.addField("/setup (Admin)", "Creates a new housewallet for the project", false);
			builder.addField("/housewallet (Admin)", "Shows information about the housewallet of the project", false);
		
		
			builder.setColor(Color.white);
			builder.setFooter("Powered by BonkRaids","https://cdn.discordapp.com/attachments/1161769361334341664/1224035490207105116/Neuefs_Projekt.png?ex=661c06e2&is=660991e2&hm=eaef5902fe54b94fc35a982fb3c315cac7710dccb8e2ed75986e6b6fc860c1f6&");
				
			builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
		   
			
			hook.sendMessageEmbeds(builder.build()).queue();
			return;
			
			
			
		}
		
		
	}
}
