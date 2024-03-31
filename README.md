<div align="center">
  <img src="https://media.discordapp.net/attachments/1161769361334341664/1224035490207105116/Neuefs_Projekt.png?ex=661c06e2&is=660991e2&hm=eaef5902fe54b94fc35a982fb3c315cac7710dccb8e2ed75986e6b6fc860c1f6&=&format=webp&quality=lossless" width="200" height="200" />
</div>

# BonkRaids

Get ready to bonk your way through tweets and earn rewards in the form of bonk tokens! 
 
BonkRaids is a discord bot designed to enhance Twitter raiding experience within Discord servers. This bot offers a wide range of features to streamline the process of creating and managing raids.

### JS API Endpoints

1. **GET /getWalletOnly**: Retrieves house wallet information associated with a UUID (Discord Guild ID).
2. **GET /sendspl**: Initiates a token transfer transaction (BONK) to a given raid wallet.

## Command Details

1. **Create Raid Command**
   - **Name:** `/createraid`
   - **Options:**
     - `title`: The mission's title (required)
     - `description`: Raid Description (required)
     - `channel`: The channel where the raid message gets posted (required)
     - `raidduration`: Unix timestamp for the raiding duration (required)
     - `raiders`: The max amount of raiders you want (required)
     - `rewardtype`: Raid-Type: BONK only for now (required)
     - `twitterlink`: The twitter link (required)
     - `retweetamount`: Retweet amount (required)
     - `likeamount`: amount (required)
     - `followeetag`: The twitter account to follow (optional)
     - `followamount`: Follow (SOL or SPL-TOKEN) amount (optional)

2. **Link Command**
   - **Name:** `/link`
   - **Description:** Link your twitter account
   - **Options:**
     - `twittertag`: Your twitter account (tag) you want to link (required)

3. **Wallet Command**
   - **Name:** `/wallet`
   - **Description:** Link your Solana wallet
   - **Options:**
     - `wallet`: Your Solana wallet you want to link (required)

4. **Setup Command**
   - **Name:** `/setup`
   - **Description:** Setup the server's house wallet

5. **Housewallet Command**
   - **Name:** `/housewallet`
   - **Description:** Check the server's house wallet information

6. **Help Command**
   - **Name:** `/help`
   - **Description:** Gives you an overview of all commands of commands

## Usage

1. **Installation**: Clone the repository and configure the bot with necessary credentials and settings, or just skip this step and invite the bot.
2. Invite the bot using this link: https://discord.com/oauth2/authorize?permissions=347200&scope=bot+applications.commands&client_id=1223216689307717632
3. **Deployment**: Ensure proper permissions for the bot.
4. **Interact**: Use the available commands to create raids, link accounts, manage wallets, and more.

*© 2024 BonkRaids Discord Bot. All rights reserved.*
