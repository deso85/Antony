
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

Antony is a Java-based Discord bot that provides common bot features as well as specialized commands for a German ant-keeping community. It is built on top of [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA).

---

## ✨ Features

- Standard Discord bot functionality
- Custom features tailored for an ant-keeping community
- Configurable via properties templates
- Database support using Hibernate + SQLite
- REST capabilities via Resteasy

---

## ✅ Prerequisites

Make sure you have the following installed on your system:

| Software | Minimum Version |
|---|---|
| Java (JDK) | 11 |
| Maven | 3.8+ |

> ⚠️ Ensure you have a Discord bot application set up in the [Discord Developer Portal](https://discord.com/developers/applications) and copy your bot token.

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/deso85/Antony.git
cd Antony
```

### 2. Create the configuration files

From the project root, copy the example configuration files:

```bash
cp src/main/resources/antony.properties.tpl src/main/resources/antony.properties
cp src/main/resources/log4j.properties.tpl src/main/resources/log4j.properties
```

On Windows PowerShell, use:

```powershell
Copy-Item src/main/resources/antony.properties.tpl src/main/resources/antony.properties
Copy-Item src/main/resources/log4j.properties.tpl src/main/resources/log4j.properties
```

Then edit the copied files and set at least the following values:

```properties
bot.token=YOUR_BOT_TOKEN_HERE
bot.owner.id=YOUR_USER_ID
```

### 3. Build the project

```bash
mvn clean compile assembly:single
```

### 4. Run the bot

After building, run the generated JAR from the target directory:

```bash
java -jar target/Antony-*-jar-with-dependencies.jar
```

> 💡 The bot uses Log4j for logging. You can adjust the log behavior in the copied log configuration file under src/main/resources.

---

## 📁 Project Structure

| Directory | Description |
|---|---|
| src/main/java | Main Java source files |
| src/main/resources | Config templates and resources |
| src/test/java | Unit tests |
| pom.xml | Maven build configuration |

---

## 📚 Built With

- [JDA](https://github.com/DV8FromTheWorld/JDA) – Java Discord API
- [Maven](https://maven.apache.org/) – Build tool
- [Hibernate](https://hibernate.org/) – ORM
- [Resteasy](https://resteasy.dev/) – REST framework
- [Log4j](https://logging.apache.org/log4j/) – Logging

---

## 📖 Documentation

For more details, check out the [FAQ & Wiki](https://github.com/deso85/Antony/wiki).

---

## 📜 License

This project is licensed under the European Union Public Licence 1.2 — see [LICENSE](https://github.com/deso85/Antony/tree/master/LICENSE) for details.

---

## 🙏 Acknowledgements

Thanks to the JDA developers and contributors for making Discord bot development in Java much easier.
