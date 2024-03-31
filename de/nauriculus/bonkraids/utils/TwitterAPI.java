package de.nauriculus.bonkraids.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPI {
	
	public static TwitterFactory tf;
	public static Twitter twitter;

	public static String BEARER_TOKEN = "";
	    
	 private static String createAuthHeader() {
	   return "Bearer " + BEARER_TOKEN;
	 }
	  
	public static boolean checkIfUserIsFollowing(String user, String toFollow) throws TwitterException {
		try {
			String followerScreenName = user; //user name
			String followeeScreenName = toFollow; //who should be followed

	        // Get the user objects for the follower and followee
	        User follower = twitter.showUser(followerScreenName);
	        User followee = twitter.showUser(followeeScreenName);

	        // Check if the follower is following the followee
	        boolean isFollowing = twitter.showFriendship(follower.getId(), followee.getId()).isSourceFollowingTarget();

	        if (isFollowing) {
	            return true;
	        } else {
	            return false;
	        }
		}
		catch(Exception e) 
		{
			return false;
		}
	}
	
	public static ArrayList getAllLikingUsers(Long tweetId) {
 

        // Set up the URL and the connection
        URL url = null;
		try {
			url = new URL("https://api.twitter.com/2/tweets/" + tweetId + "/liking_users");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Set the headers
        connection.setRequestProperty("Authorization", createAuthHeader());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        connection.setRequestProperty("Content-Type", "application/json");

        // Send the request
        connection.setDoOutput(true);
        int responseCode = 0;
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Check the response code
        if (responseCode == HttpURLConnection.HTTP_OK) {

            // Get the response body
            BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String inputLine;
            StringBuffer response = new StringBuffer();
            try {
				while ((inputLine = in.readLine()) != null) {
				    response.append(inputLine);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Print the response body
           
         
            JSONObject jsonResponse = new JSONObject(response.toString());
            System.out.println(jsonResponse);
            JSONArray data = jsonResponse.getJSONArray("data");

            ArrayList<String> usernames = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                JSONObject user = data.getJSONObject(i);
                usernames.add(user.getString("username"));
            }
            
            return usernames;


        } else {
            System.out.println("POST request failed with response code " + responseCode);
        }
		return null;

    }

	
	
	public static Long getTweetIdFromLink(String url) {
        try {
            URL tweetUrl = new URL(url);
            String path = tweetUrl.getPath();
            String[] pathSegments = path.split("/");
            
            for (int i = pathSegments.length - 1; i >= 0; i--) {
                if (!pathSegments[i].isEmpty()) {
                    try {
                        long tweetId = Long.parseLong(pathSegments[i]);
                        return tweetId;
                    } catch (NumberFormatException e) {
                        // Ignore and continue searching
                    }
                }
            }
            
            System.out.println("Invalid tweet link.");
            return 0L;
        } catch (Exception e) {
            System.out.println("Invalid tweet link.");
            return 0L;
        }
    }
	
	
	   public static ArrayList<String> getAllCommenter(Long tweetId) {
	        // Set up the initial URL and connection
	        String url = "https://api.twitter.com/2/tweets/search/recent?query=conversation_id:" + tweetId + "&tweet.fields=created_at&expansions=author_id&user.fields=name,username&max_results=50";
	        ArrayList<String> usernames = new ArrayList<>();

	        while (url != null) {
	            try {
	                // Set up the connection
	                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	                connection.setRequestMethod("GET");
	                connection.setRequestProperty("Authorization", createAuthHeader());
	                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
	                connection.setRequestProperty("Content-Type", "application/json");

	                // Send the request and check the response code
	                int responseCode = connection.getResponseCode();
	                if (responseCode == HttpURLConnection.HTTP_OK) {
	                    // Get the response body
	                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	                    String inputLine;
	                    StringBuffer response = new StringBuffer();
	                    while ((inputLine = in.readLine()) != null) {
	                        response.append(inputLine);
	                    }
	                    in.close();

	                    // Parse the JSON response and add the usernames to the list
	                    JSONObject jsonResponse = new JSONObject(response.toString());
	                    try {
	                    JSONObject includes = jsonResponse.getJSONObject("includes");
	                    JSONArray users = includes.getJSONArray("users");
	                    for (int i = 0; i < users.length(); i++) {
	                        String username = users.getJSONObject(i).getString("username");
	                        usernames.add(username);
	                    }

	                    // Check if there's a next token in the response and update the URL if so
	                    if (jsonResponse.has("meta") && jsonResponse.getJSONObject("meta").has("next_token")) {
	                        url = "https://api.twitter.com/2/tweets/search/recent?query=conversation_id:" + tweetId +
	                              "&tweet.fields=created_at&expansions=author_id&user.fields=name,username&max_results=100" +
	                              "&next_token=" + jsonResponse.getJSONObject("meta").getString("next_token");
	                    } else {
	                        url = null; // no more next token, stop looping
	                    }
	                    }
	                    catch(Exception e) {
	                    	System.out.println("Error when fetching tweet commenter " + e.getMessage());
	  	                    return usernames;
	                    }
	                } else {
	                    System.out.println("GET request failed with response code " + responseCode);
	                    return null;
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	                return null;
	            }
	        }

	        return usernames;
	    }
	

	
	public static ArrayList getAllRetweetingUsers(Long tweetId) {
        // Set up the URL and the connection
        URL url = null;
		try {
			url = new URL("https://api.twitter.com/2/tweets/"+ tweetId + "/retweeted_by");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Set the headers
        connection.setRequestProperty("Authorization", createAuthHeader());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        connection.setRequestProperty("Content-Type", "application/json");

        // Send the request
        connection.setDoOutput(true);
        int responseCode = 0;
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Check the response code
        if (responseCode == HttpURLConnection.HTTP_OK) {

            // Get the response body
            BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String inputLine;
            StringBuffer response = new StringBuffer();
            try {
				while ((inputLine = in.readLine()) != null) {
				    response.append(inputLine);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Print the response body
           
         
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray data = jsonResponse.getJSONArray("data");

            ArrayList<String> usernames = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                JSONObject user = data.getJSONObject(i);
                usernames.add(user.getString("username"));
            }
            
            return usernames;


        } else {
            System.out.println("POST request failed with response code " + responseCode);
        }
		return null;

    }

	
	  public static void start() {
		    ConfigurationBuilder cb = new ConfigurationBuilder();
		    cb.setDebugEnabled(true)
		      .setOAuthConsumerKey("")
		      .setOAuthConsumerSecret("")
		      .setOAuthAccessToken("")
		      .setOAuthAccessTokenSecret("");
		    tf = new TwitterFactory(cb.build());
		    twitter = tf.getInstance();
	  }
}
