<div align="center">
  <img src="https://github.com/nauriculus/BonkRaids/assets/24634581/32fc1c10-77e8-4461-9b50-5c17a217f9a2" width="400" height="400" />
</div>

# BonkRaids

Get ready to bonk your way through tweets and earn rewards in the form of bonk tokens! 
 
BonkRaids is a discord bot designed to enhance Twitter raiding experience within Discord servers. This bot offers a wide range of features to streamline the process of creating and managing raids.

#Preview

### JS API Endpoints

1. **GET /getWalletOnly**: Retrieves house wallet information associated with a UUID (Discord Guild ID).
2. **GET /sendspl**: Initiates a token transfer transaction (BONK) to a given raid wallet.

## Command Details

**1. Create Raid Command**
   - **Name:** `/createraid`
   - **Options:**
     - `title`: The mission's title (required)
     - `description`: Raid Description (required)
     - `channel`: The channel where the raid message gets posted (required)
     - `raidduration`: Unix timestamp for the raiding duration (required)
     - `raiders`: The max amount of raiders you want (required)
     - `rewardtype`: Raid-Type: BONK only for now (required)
     - `twitterlink`: The twitter link (required)
     - `retweetamount`: Reward Retweet amount (required)
     - `likeamount`: Reward Like amount (required)
     - `followeetag`: The twitter account to follow (optional)
     - `followamount`: Reward Follow amount (optional)

<img width="403" alt="Bildschirmfoto 2024-03-31 um 23 48 39" src="https://github.com/nauriculus/BonkRaids/assets/24634581/f07cf532-b1ae-469a-935d-51748b52d6e1">

  <img width="409" alt="Bildschirmfoto 2024-03-30 um 13 33 55" src="https://github.com/nauriculus/BonkRaids/assets/24634581/2458f94b-7cbe-46b5-875c-c61b611d6ce8">

  <img width="328" alt="Bildschirmfoto 2024-04-01 um 10 25 24" src="https://github.com/nauriculus/BonkRaids/assets/24634581/73bebe8f-3b55-4e31-98f6-545194df0384">



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

<img width="534" alt="Bildschirmfoto 2024-04-01 um 10 07 59 (1)" src="https://github.com/nauriculus/BonkRaids/assets/24634581/5a2d5b88-90b8-4fff-89a9-c61217623bfa">

6. **Help Command**
   - **Name:** `/help`
   - **Description:** Gives you an overview of all commands of commands

## Usage

1. **Installation**: Clone the repository and configure the bot with necessary credentials and settings, or just skip this step and invite the bot.
2. Invite the bot using this link: https://discord.com/oauth2/authorize?permissions=347200&scope=bot+applications.commands&client_id=1223216689307717632
3. **Deployment**: Ensure proper permissions for the bot.
4. **Interact**: Use the available commands to create raids, link accounts, manage wallets, and more.

*© 2024 BonkRaids Discord Bot. All rights reserved.*
