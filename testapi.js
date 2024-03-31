const https = require('https');
const express = require('express');
const fs = require('fs');
const mysql = require('mysql2');
const { Keypair } = require('@solana/web3.js');
const nacl = require('tweetnacl');
const crypto = require('crypto');
const app = express();
const spl = require("@solana/spl-token");
const web3 = require("@solana/web3.js");

const options = {
   cert: fs.readFileSync('/etc/letsencrypt/live/certificate.crt'),
   ca: fs.readFileSync('/etc/letsencrypt/live/ca_bundle.crt'),
   key: fs.readFileSync('/etc/letsencrypt/live/private.key')
};

const connection = mysql.createConnection({
  host: 'localhost',
  user: '',
  port: '',
  password: '',
  database: ''
});
let connected = false;

function handleDisconnect() {

const query = 'SELECT * FROM testwallets LIMIT 1'; //This is used to maintain the connection over a long period without requests. Change this to any other table!
  connection.query(query, (err, result) => {
    if (err) throw err;
  });

  connection.connect((err) => {
    if (err) {
      console.error(`Error connecting to database: ${err.message}`);
      setTimeout(handleDisconnect, 2000);
      return;
    }
    connected = true;
    console.log(`MySQL connection state: ${connection.state}`);
  });

  connection.on('error', (err) => {
    console.error(`MySQL connection error: ${err.message}`);
    if (!connected) {
      handleDisconnect();
    }
  });
}

setInterval(handleDisconnect, 3600000);   

app.get('/getWalletOnly', async (req, res) => {

  const userUUID = req.query.uuid;
    if (!userUUID ) {
    res.status(500).send({ error: 'No uuid found' });
    return;
  }

      connection.query(`SELECT WALLET FROM testwallets WHERE USER_ID='${userUUID}'`, (error, results) => {
      if (error) {
      res.status(500).send({ error: `Error while fetching wallet: ${error}` });
      return;
      } else {
        if (results.length > 0) {
            console.log(`Wallet exists for user ID ${userUUID}`);
            results[0].CHAIN = "SOLANA";
            res.send({ results });
            return;
       } else {
        res.status(500).send({ error: 'No linked wallet found' });
        return;
            }
        }
        });
  });


  async function getOrCreateATAInfo(connection, owner, token) {
    const FAILED_TO_FIND_ACCOUNT = "Failed to find account";
    const INVALID_ACCOUNT_OWNER = "Invalid account owner";
    const associatedAddress = (await web3.PublicKey.findProgramAddress([
        owner.toBuffer(),
    spl.TOKEN_PROGRAM_ID.toBuffer(),
        token.publicKey.toBuffer(),
    ], spl.ASSOCIATED_TOKEN_PROGRAM_ID))[0];
    try {
        return await token.getAccountInfo(associatedAddress);
    }
    catch (err) {
       
       console.log("err3", err);
           
       
    }
  }

app.get('/getWallet', async (req, res) => {

    const secretPass = req.query.secret;
    	if (!secretPass ) {
	    res.status(500).send({ error: 'No token found' });
      return;
    }

    const userUUID = req.query.uuid;
    	if (!userUUID ) {
	    res.status(500).send({ error: 'No uuid found' });
      return;
    }

        connection.query(`SELECT WALLET FROM testwallets WHERE USER_ID='${userUUID}'`, (error, results) => {
        if (error) {
        res.status(500).send({ error: `Error while fetching wallet: ${error}` });
		    return;
        } else {
          if (results.length > 0) {
              console.log(`Wallet exists for user ID ${userUUID}`);
              results[0].CHAIN = "SOLANA";
   			      res.send({ results });
			        return;
         } else {

          const seed = nacl.randomBytes(32);
          const keyPair = Keypair.fromSeed(seed);
          const publicKey = keyPair.publicKey;
          const privateKey = keyPair.secretKey;
          const password = secretPass;

          // Encrypt wallet private key with the password hash as the encryption key
          const cipher = crypto.createCipher('aes256', password);
          let encrypted = cipher.update(privateKey, 'hex', 'hex');
          encrypted += cipher.final('hex');

          // Save encrypted wallet private key to file
          const walletJson = {
            privateKey: encrypted
          };
          fs.writeFileSync(`/home/testapi/${userUUID}.json`, JSON.stringify(walletJson));

         // Save wallet to database
         connection.query(`INSERT INTO testwallets (USER_ID, WALLET) VALUES ('${userUUID}', '${publicKey}')`, (error) => {
          if (error) {
              console.error(`Error while saving wallet to database: ${error}`);
              res.status(500).send({ error: `Error while saving wallet to database: ${error}` });
          } else {
              console.log(`Wallet saved to database for user ID ${userUUID}`);
              let response = {};
              response.results = [{}];
              response.results[0].WALLET = publicKey.toString();
              response.results[0].CHAIN = "SOLANA";
              res.send(response);                    
                      }
                                      });
                                  }
                              }
                          }); 
              });

              app.get('/sendspl', async (req, res) => {
                if (!req.query.receiver || !req.query.amount) {
                  return res.status(400).send({ error: 'Missing required field: wallet, amount or token' });
                }
        
                const houseWallet = req.query.houseWallet;
                const houseWalletID = req.query.houseWalletID;
                const receiver = req.query.receiver;
                const amount = req.query.amount;
                const secret = req.query.secret;

                if(secret !== "") {
                  res.send({ message: 'Invalid' });
                  return;
                }

                
                const parsedAmount = parseFloat(amount);
                if (isNaN(parsedAmount)) {
                  res.send({ message: 'Invalid SOL amount' });
                  return;
                }
        
                if(parsedAmount == 0 || parsedAmount == 0.0) {
                  res.send({ message: 'Invalid amount' });
                  return; 
                }
        
                if (!receiver) {
                  res.status(500).send({ error: 'No houseWallet found' });
                  return;
                }
            
                if (!houseWallet) {
                res.status(500).send({ error: 'No houseWallet found' });
                return;
              }

              if (!houseWalletID) {
                res.status(500).send({ error: 'No houseWallet found' });
                return;
              }
        
              fs.readFile(`/home/testapi/${houseWalletID}.json`, 'utf8', (err, decryptedWallet) => {
                if (err) {
                  res.status(500).send({ error: `Error reading wallet file: ${err}` });
                  return;
                }
              
                const wallet = JSON.parse(decryptedWallet);
        
              if (!wallet) {
                res.status(500).send({ error: 'Wallet not found' });
                return;
              }
        
              (async () => {
        
                const web3 = require("@solana/web3.js");
                // Connect to cluster
                
                 let decrypted;
                  try {
                    const privateKeyEncrypted = wallet.privateKey;
                    const decipher = crypto.createDecipher("aes256", "");
                    decrypted = decipher.update(privateKeyEncrypted, "hex", "hex");
                    decrypted += decipher.final("hex");
                  } catch (error) {
                    res.status(500).send({ error: `Error decrypting private key: Check your secret!` });
                    return;
                  }
        
            const seed = Buffer.from(decrypted, 'hex').slice(0, 32);
            const keyPair = Keypair.fromSeed(seed);
           
            const mint = new web3.PublicKey("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263");

            const connectionClient = new web3.Connection("https://rpc.hellomoon.io/", "confirmed");
      
            

          const tokenHouse = new spl.Token(connectionClient, mint, spl.TOKEN_PROGRAM_ID, keyPair.publicKey);
          const tokenUser = new spl.Token(connectionClient, mint, spl.TOKEN_PROGRAM_ID, new web3.PublicKey(houseWallet));
          const tokenAccount = (await getOrCreateATAInfo(connectionClient, new web3.PublicKey(receiver), tokenUser)).address.toBase58();
          const tokenAccountEscrow = (await getOrCreateATAInfo(connectionClient, keyPair.publicKey, tokenHouse)).address.toBase58();
          
          console.log("tokenaccount escrow: " + tokenAccountEscrow);
          console.log("user wallet token account: " + tokenAccount);
          const PRECISION = 5; 
          const AMOUNT_IN_BOKU = parsedAmount; 

          const AMOUNT_IN_LAMPORTS = AMOUNT_IN_BOKU * Math.pow(10, PRECISION);
      
          const addPriorityFee = web3.ComputeBudgetProgram.setComputeUnitPrice({
            microLamports: 25000, 
        });
         
          const transaction = new web3.Transaction().add(
              spl.Token.createTransferInstruction(
                  spl.TOKEN_PROGRAM_ID,
                  new web3.PublicKey(tokenAccountEscrow),
                  new web3.PublicKey(tokenAccount),
                  keyPair.publicKey,
                  [],
                  AMOUNT_IN_LAMPORTS
              ),
              addPriorityFee
          );

    

       

      
           let latestBlockhash = await connectionClient.getLatestBlockhash('confirmed');
           console.log(" ✅ - Fetched latest blockhash. Last Valid Height:", latestBlockhash.lastValidBlockHeight);

  
          transaction.feePayer = wallet.publicKey;
          transaction.recentBlockhash = latestBlockhash.blockhash;
   
   
          const txid = await connectionClient.sendTransaction(transaction, [keyPair],{ maxRetries: 15 });

          const confirmation = await connectionClient.confirmTransaction({
            signature: txid,
            blockhash: latestBlockhash.blockhash,
            lastValidBlockHeight: latestBlockhash.lastValidBlockHeight,
          });
          
          if (confirmation.value.err) {
            throw new Error("🚨 Transaction not confirmed.");
          }

          const txResult = await connectionClient.getTransaction(txid, {maxSupportedTransactionVersion: 0})
          console.log('🚀 Transaction Successfully Confirmed!', '\n', `https://solscan.io/tx/${txid}`);
          console.log(`Transaction Fee: ${txResult?.meta?.fee} Lamports`);

          res.send({ message: 'Transaction success: ', txid});
          })();
        });
      });  
     
      
    https.createServer(options, app).listen(4564, () => {
      console.log('HTTPS REST API listening on port');
    });