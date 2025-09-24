# STAG - Simple Text Adventure Game Engine

A versatile, multiplayer text-based adventure game engine built in Java using Object-Oriented Programming principles. This game engine can run any text adventure game that conforms to the specified configuration format, supporting multiple simultaneous players through socket-based networking.

## 🎮 Features

### Core Gameplay
- **Multiplayer Support**: Multiple players can play simultaneously using different usernames
- **Dynamic Game Loading**: Games are loaded from configuration files (entities and actions)
- **Flexible Command Interpretation**: Natural language processing with case insensitivity and partial commands
- **Real-time Networking**: Socket-based client-server architecture

### Game Mechanics
- **Inventory System**: Players can collect, carry, and drop artifacts
- **Location-based Movement**: Navigate between interconnected game locations
- **Custom Actions**: Extensible action system defined through XML configuration
- **Health System**: Player health management with consequences for death
- **Entity Interactions**: Rich interactions with characters, furniture, and artifacts

### Built-in Commands
- **`look`** - Examine current location and its contents
- **`inventory` / `inv`** - List items in player's inventory
- **`get <item>`** - Pick up an artifact from current location
- **`drop <item>`** - Drop an item from inventory to current location
- **`goto <location>`** - Move to a connected location
- **`health`** - Check current health level

## 🏗️ Architecture

### Object-Oriented Design
The game engine is built using OOP principles with the following key components:

- **GameServer**: Main server class handling client connections and command processing
- **GameEntity**: Abstract base class for all game entities
- **Player**: Represents individual players with inventory and location
- **Location**: Game areas with paths, entities, and environmental details
- **GameAction**: Custom actions defined in configuration files

### Data Structures
- Uses Java Collections Framework (avoiding arrays/ArrayLists as per constraints)
- Entity management through unique identifier system
- Graph-based location connectivity
