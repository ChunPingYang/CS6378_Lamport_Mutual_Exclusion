# CS 6378: Lamport-Mutual-Exclusion

## Requirements
1. Source code must be in the C/C++/Java programming language
2. The program must run on UTD lab machines (dc01, dc02, ...., dc45)

## Client-Server Model
In this project, you are expected to implement Lamport’s Mutual Exclusion Algorithm. Knowledge of threads and socket programming and its APIs in the language you choose is expected. Each process (server/client) must execute on a separate machine (dcxx).

## Description
Implement a solution that mimics a replicated file system. The file system consists of four text files: f1, f2, f3, and f4. All four files are replicated at three file servers. To achieve file replication, choose any three of the dcxx machines. Each of these machines will store a copy of all the files. Five clients, executing at different sites (and different from the three sites that replicate the files), may issue append to file requests for any of these files. The clients know the locations of all the other clients as well as the locations of all file servers where the files are replicated. All replicas of a file are consistent, to begin with. The desired operations are as follows:

- A client initiates at most one append to file at a time.
- The client may send a append to file REQUEST to any of the file replication sites (randomly selected) along with the text to be appended. In this case, the site must report a successful message to the client if all copies of the file, across the all sites, are updated consistently. Otherwise, the append should not happen at any site and the site must report a failure message to the client. We do not expect a failure to happen in this project unless a file is not replicated at exactly four different sites.
- On receiving the append to file REQUEST, the receiving site initiates a REQUEST to enter critical section, as per Lamport’s mutual exclusion algorithm. Obviously each REQUEST should result in a critical section execution regarding that particular file. In the critical section the text provided by the client must be appended to all copies of the file.
- Concurrent appends initiated by different clients to different files must be supported as such updates do not violate the mutual exclusion condition.
- Your program does NOT need to support the creation of new files or deletion of existing files. However, it must report an error if an attempt is made to append to a file that does not exist in the file system.
- All clients are started simultaneously.
- Each client loops through the following sequence of actions 100 times: (a) waits for a random amount of time between 0 to 1 second, (b) issues an append to file REQUEST, (c) waits for a successful or failure message. So, for this project you need to worry about concurrent read/write requests for the same file but issued by different clients. Your client should gracefully terminate when all 100 REQUESTS have been served (either successfully or unsuccessfully).

## How to Run the Project
Instead of configuration in UTD campus server, replace with locolhost to stimulate the project. In this project, there are three file directories representing three servers with four files for each one. There are two configuration files called "config.txt" and "ports.txt", which is used to set up the configuration. Here is the steps below:
1. Run class "ServerNode" three times individually, enter 0, 1, 2, respectively. One server waits for other two servers until they are active, and then connect each other. 
2. Run multiple instance for class "ClientNode", which represents multiple clients. Each client make 100 times requests to servers. Each time the program make a request to write to one of four files randomly in one of three servers randomly.
3.  Each server's output must be as follows: <br/>
client1 append 1 time on f2.txt on Server0 <br/>
client1 append 4 time on f2.txt on Server0 <br/>
client2 append 4 time on f2.txt on Server0 

