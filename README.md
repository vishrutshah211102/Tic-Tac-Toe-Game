# Tic-Tac-Toe Android Game

## Project Description

This Tic-Tac-Toe game is an Android-based application that allows users to play both single-player and multiplayer games. In single-player mode, users can challenge the AI at three difficulty levels: Easy, Medium, and Hard. The AI uses a minimax algorithm with alpha-beta pruning to make optimal moves, ensuring a challenging experience in higher difficulties.

In multiplayer mode, the app allows two players to connect over Bluetooth to play against each other. The game is designed with a simple, user-friendly interface that dynamically updates the board as players make moves. Additionally, game history is stored locally using SQLite, allowing users to view past games and track their performance.

## Features

- **Single-Player Mode**: Play against an AI with three difficulty levels (Easy, Medium, Hard).
- **Multiplayer Mode**: Connect two Android devices via Bluetooth and play against a friend.
- **Minimax Algorithm**: The AI in Hard mode never loses, making optimal moves using alpha-beta pruning.
- **Local Storage**: Past game results, including winner and difficulty level, are saved and can be viewed later.
- **Responsive UI**: Real-time updates to the game board, smooth gameplay transitions, and easy-to-navigate design.

## Prerequisites

- Android Studio installed on your computer.
- An Android device or emulator to test the application.
- Bluetooth enabled on Android devices for multiplayer mode.

## Installation Instructions

1. **Clone the Repository**:
   Open a terminal and clone the repository to your local machine:

2. **Open the Project in Android Studio**:
   - Launch **Android Studio**.
   - Open the project by selecting the appropriate directory.
   - Wait for the project to build and sync.

3. **Connect an Android Device or Use an Emulator**:
   - If you’re using an Android device, enable **Developer Mode** and **USB Debugging** on your phone, then connect it via USB.
   - Alternatively, set up an Android emulator in **Android Studio**.

4. **Run the App**:
   - In Android Studio, click the **Run** button.
   - Select your device or emulator and wait for the app to launch.

## Usage Instructions

### Single-Player Mode
1. Launch the app on your Android device.
2. On the main screen, tap **Start Game**.
3. Choose the AI difficulty level in the settings:
   - **Easy**: AI makes random moves.
   - **Medium**: AI mixes random moves with some optimal strategies.
   - **Hard**: AI uses the minimax algorithm with alpha-beta pruning and never loses.
4. Play against the AI by tapping on any square in the 3x3 grid.

### Multiplayer Mode (Bluetooth)
1. Both players must launch the app on their respective Android devices.
2. One player should tap **Create Room** to make their device discoverable.
3. The other player should tap **Join Room** and scan for nearby devices.
4. Once a connection is established, take turns tapping the game board to play.
5. The game will automatically update on both devices after each move.

### Viewing Past Games
1. From the main menu, tap **Past Games**.
2. A list of previously played games will be displayed, showing the date, winner, and difficulty level.
3. Scroll through the list to review past game results.

### Resetting the Game
- To reset a game during play, go to **Settings** and tap the **Reset Game** option.

## Troubleshooting

- **Bluetooth Connection Issues**: If you have trouble connecting devices over Bluetooth, ensure that both devices are discoverable and within range. Try restarting Bluetooth if necessary.
- **App Crashes**: If the app crashes or behaves unexpectedly, ensure that Android Studio is updated, and all necessary dependencies are installed.
