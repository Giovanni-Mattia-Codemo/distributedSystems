# Distributed Group Chat Application

## Overview
This project implements a distributed group chat application where users can create and delete rooms, post messages within rooms they are participating in, and receive messages in causal order. The application is designed to be fully distributed, meaning it operates without a centralized server, and highly available, ensuring users can use the chat even when temporarily disconnected from the network or during network failures.

## Features
1. **Room Creation and Deletion:** Users can create rooms and specify the set of participants at the time of creation. Rooms can also be deleted by users.
2. **Messaging:** Users can post new messages within rooms they are participating in. Messages within each room are delivered in causal order.
3. **Distributed Architecture:** The application operates in a fully distributed manner, without relying on any centralized server. Users exchange messages directly with each other.
4. **High Availability:** The application allows users to continue using the chat even during temporary network disconnections or failures.

## Usage
1. **Creating a Room:** Users can create a room and specify the participants by providing their usernames.  
<pre>/create [room_name] [participant1] [participant2] ...</pre>

2. **Deleting a Room:** Room creators can delete a room they created.
<pre>/delete [room_name]</pre>

3. **Sending Messages:** Users can send messages to rooms they are participating in.
<pre>/send [room_name] [message_content]</pre>

## Assumptions
- Clients are reliable but can join and leave the network at any time.
- Network failures and partitions may occur, but the application should continue to operate effectively.

## Project members
- Falcone Federico
- Giovanni M. Codemo

## References
- Distributed Systems course at Politecnico di Milano
