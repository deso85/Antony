
[license]: https://github.com/deso85/Antony/tree/master/LICENSE
[faq]: https://github.com/deso85/Antony/wiki
[version]: https://github.com/deso85/Antony/releases/latest
[license-shield]: https://img.shields.io/badge/license-EUPL%201.2-lightgrey
[faq-shield]: https://img.shields.io/badge/Wiki-FAQ-blue.svg
[version-shield]: https://img.shields.io/github/v/release/deso85/Antony
[ ![version-shield] ][version]
[ ![license-shield] ][license]
[ ![faq-shield] ][faq]

# Antony - Discord Bot
Antony is yet another Discord Bot which provides well-known functions as well as some specialized functions for our German ant keeping Discord Server. I decided to code it in Java as I have basic knowledge of this programming language. Thanks to [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)!

## Getting Started

To get started with **Antony**, follow these simple steps to set up and run the bot:

### 1. Set Up Your Discord Application
First, create a Discord bot application by navigating to the [Discord Developer Portal](https://discord.com/developers/applications). Once there:
- Click on *New Application* and give your bot a name.
- Under the *Bot* tab, click *Add Bot*.
- Copy the bot token—you’ll need it to connect Antony to your Discord server.

### 2. Clone the Repository
Clone the **Antony** repository to your local machine using the following command:
```bash
git clone https://github.com/deso85/Antony.git
```

### 3. Configure the Bot
Next, navigate to the `src/main/resources` folder and copy the properties template:
```bash
cp antony.properties.tpl antony.properties
```
Now, open the `antony.properties` file and:
- Replace the `bot.token` value with your Discord bot token (from step 1).
- Set the `bot.owner.id` (the user ID of the bot owner).
- Optionally, configure other settings such as database, commands, or other bot-specific configurations.

### 4. Configure Logging (Log4j)
The bot uses Log4j for logging configuration. To adjust logging behavior, modify the `log4j.properties` file in the `src/main/resources` directory. The default configuration should suffice for most use cases, but feel free to customize the log levels, formats, and output locations according to your preferences.

### 5. Build the Project
You can easily build the bot using Maven. From the project root directory, run:
```bash
mvn clean compile assembly:single
```
This command will compile the code and package it into a runnable JAR file located in the `target` folder.

### 6. Run the Bot
Once the build process is complete, start the bot by running the following command:
```bash
java -jar target/Antony.jar
```
This will launch **Antony** and connect it to your Discord server using the configuration set in the `antony.properties` file.

## Contributing
Contributions to **Antony** are welcome! If you encounter any bugs or have suggestions for improvements, here are the steps to get involved:

1. **Report Issues**: If you encounter a bug or have an idea for an enhancement, please open an issue on GitHub with a clear description. Screenshots and log outputs are appreciated.
2. **Fork the Repository**: Fork this repository to make changes, then clone your fork to your local machine.
3. **Create a Feature Branch**: When making substantial changes, create a new feature branch:
    ```bash
    git checkout -b feature/YourFeatureName
    ```
4. **Make Your Changes**: Implement your changes, ensuring that you write clear and concise commit messages.
5. **Test Your Code**: Before submitting your changes, test them to ensure everything works as expected.
6. **Open a Pull Request**: Push your changes to your fork and open a pull request on GitHub with a description of the changes.

Thank you for contributing to **Antony**!

## Libraries Used
Antony relies on the following libraries:

- **[JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)**: A powerful Java library for building Discord bots. JDA not only provides an interface to interact with the Discord API but also includes many built-in utilities and abstractions for handling events, commands, and bot management tasks, such as message processing, event listeners, and gateway connections.
- **[Jackson](https://github.com/FasterXML/jackson)**: A set of data-processing tools for Java, used for JSON binding and manipulation.
- **[Hibernate](https://hibernate.org/)**: A framework for mapping Java objects to database tables, used for persistence and data management.
- **[SQLite JDBC](https://github.com/xerial/sqlite-jdbc)**: A JDBC driver for SQLite, used for database connectivity.
- **[Log4j](https://logging.apache.org/log4j/)**: For logging functionality.
- **[Resteasy](https://resteasy.github.io/)**: A JAX-RS implementation, used for building RESTful web services.
  
Additional dependencies and versions can be found in the `pom.xml` file.
