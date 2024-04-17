# aex-strategy-listener 
This is a JAVA Spring Boot application, listening to all the CREATE, UPDATE and DELETE events from the Line planning UI Screen and processing them further.

## Libraries
* Spring-Kafka
* Http Client

## Getting Started

### Prerequisites
Make sure you have access to :

* [Vault](https://wmlink/hashicorp)
* [CCM2 Admin](https://wmlink/ccm2)
### How to run the program

First create below 4 files under /etc/secrets/ folder in your local machine and copy the content from their corresponding Vault path:

| S.No. | File Name                   | Vault Path                                                                     |
|-------|-----------------------------|--------------------------------------------------------------------------------|
| 1     | midasApi.authorization.txt  | /secret/apparel-precision-kitt/aex-clp-ap-listener/dev/midasApi.authorization  |
| 2     | ssl.truststore.password.txt | /secret/apparel-precision-kitt/aex-clp-ap-listener/dev/ssl.truststore.password |
| 3     | ssl.keystore.txt            | /secret/apparel-precision-kitt/aex-clp-ap-listener/dev/ssl.keystore            |
| 4     | ssl.truststore.txt          | /secret/apparel-precision-kitt/aex-clp-ap-listener/dev/ssl.truststore          |

*  Follow below steps to create these files in local
      1. Open terminal and login to root 
`sudo su` , now enter your walmart password and login
      2. Navigate to /etc/secrets folder `cd /etc/secrets/`
      3. Create an empty file first `touch <fileName>`
      4. Open the file in VI mode `vi <fileName`
      5. Cope the respective value from Vault and paste it , then save by entering `ESC -> :wq!`

*NOTE*: Follow above steps and create all the above file (step 1 & 2 are only for first time)

#### From kitt.yml find version of CCM and download that version of CCM folder 
To download CCM file go to https://wmlink/ccm2  -> aex-strategy-listener -> <Version mentioned in CCM> Download folder by clicking in work offline (envName = DEV)>

#### Now clone the project in your editor from github and in edit configuration add below value in VM Options
```
-Dspring.profiles.active=dev 
-Dccm.configs.dir=/Path/to/CCM/folder
```
#### Troubleshoot
frequently encountered errors and their fix we can add here