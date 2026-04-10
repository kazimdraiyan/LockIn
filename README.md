# LockIn

**LockIn** is a JavaFX-based collaborative study platform with posting, messaging, and voice communication features in one desktop app.

## Feature List

- Authentication (sign up, login, logout, token session)
- Profile
- Posting
- Commenting
- Direct messaging
- Voice calling
- Group chat
- Image and file attachments
- Theme switching

## Screenshots

[Placeholder]

## Demo Video

[Placeholder]

## Table of Contents

- [Quick Usage](#quick-usage)
- [Installation](#installation)
- [Tech Stack](#tech-stack)
- [Known Issues](#known-issues)
- [Future Plans](#future-plans)
- [What We Learnt from This Project](#what-we-learnt-from-this-project)
- [Contribution Guidelines](#contribution-guidelines)
- [Credits](#credits)
- [FAQ](#faq)

## Quick Usage

[//]: # (TODO: Add links to the zip file after GitHub release)
### Server
- Download [`lockin_server_v1.0.0.zip`](https://github.com/kazimdraiyan/LockIn/releases/download/v1.0.0/lockin_server_v1.0.0.zip) and extract it.
- Run `lockin_server_v1.0.0.exe`.
- Server should run at port 5000.

[//]: # (TODO: Add check this out link)
### Client
- Download [`lockin_client_v1.0.0.zip`](https://github.com/kazimdraiyan/LockIn/releases/download/v1.0.0/lockin_client_v1.0.0.zip) and extract it.
- Make sure the server is running on the _same device_. For running server and client on different devices, [see this](#running-server-and-clients-on-different-devices-advanced). 
- Run `lockin_client_v1.0.0.exe`.
- The app will try to connect server on localhost:5000 by default. To use a different address, see this.
- Create an account and use the app.

### Running server and clients on different devices (Advanced)

#### Same local network (same Wi-Fi)
- Start the server on the host machine.
- Get host IP by running `ipconfig` (IPv4 address) on the host device.
- [Use the IP in `server.txt`](#how-to-change-custom-server-address-in-servertxt) of the client device.
- No need to add or modify this file if you run the server and client on the same device.

#### Distant network (using ZeroTier)
TBD

### How to change custom server address in `server.txt`

The client reads `server.txt` from your user folder at:

- Windows: `%USERPROFILE%\.lockin\server.txt`
- Linux/macOS: `~/.lockin/server.txt`

Use this format:

```text
<server_host>
<server_tcp_port>
```

Example:

```text
192.168.0.105
5000
```

If `server.txt` is missing or invalid, the client falls back to:

- Host: `localhost`
- TCP Port: `5000`


## Installation

### Prerequisites

- Java JDK 21 (Temurin recommended)
- Gradle wrapper (already included in repo)

### Clone The Repo

```bash
git clone https://github.com/kazimdraiyan/LockIn.git
cd LockIn
```

### How to Run Server

The server entry point is `app.lockin.lockin.server.ServerManager`.

- Open the project in IntelliJ IDEA
- Run the `main` method in `ServerManager`
- The TCP server starts on port `5000` and UDP relay starts on port `5001`

### How to Run Client

The client entry point is `app.lockin.lockin.client.MyApplication`.

- Run the `main` method in `MyApplication`

## Folder Structure

```text
LockIn/
|- database/
|- src/main/java/app/lockin/lockin/
|  |- client/
|  |- common/
|  |- server/
|- src/main/resources/app/lockin/lockin/
|  |- css/
|  |- fxml/
|  |- icon/
|- build.gradle.kts
|- settings.gradle.kts
```

## Tech Stack

- Language: Java 21
- UI Framework: JavaFX 21.0.6
- Build Tool: Gradle 8.13
- Data format/storage: JSON files
- Networking: Java Socket (TCP) + DatagramSocket (UDP for voice relay)

## Known Issues

- Networking setup may require manual IP/port updates across machines

## Future Plans

- Video calling
- Better notifications and presence status

## What We Learnt from This Project

- Building full-stack desktop workflows with JavaFX and custom socket networking
- Designing request/response protocols for authentication, messaging, and posts
- Managing real-world networking constraints (LAN, tunneling, virtual networks)
- Coordinating UI and backend features in a collaborative team workflow

## Contribution Guidelines

- Fork the repository and create a feature branch
- Keep changes focused and consistent with current structure
- Test server and client flows before opening a PR
- Open a pull request with clear context and screenshots where relevant

## Credits

- [Shah Tahsif Sazzad (Ahon)](https://github.com/typhon13)
- [Kazi Md. Raiyan](https://github.com/kazimdraiyan)
- Supervisor: [Khaled Mahmud Shahriar](https://cse.buet.ac.bd/faculty/faculty_detail/khaledshahriar)

## FAQ

### Why does client fail to connect?

- Check whether the server is running
- Verify IP/port in `server.txt`
- Ensure firewall allows TCP `5000` and UDP `5001`

### Do I need `server.txt` for local development?

No. If `server.txt` does not exist, client uses `localhost:5000`.
