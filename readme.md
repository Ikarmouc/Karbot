# <center>Karbot


<div style="text-align: center;"><img src="" alt="logo" style="zoom: 100%; " /> </div>

Discord Bot written in kotlin and kotlin coroutines using the [Kord Library](https://github.com/kordlib "Kord") 

The aim of this bot is to provide a multifunctional bot with multiple types of commands including : 



* Music commands 
* Administration commands 
* Weather commands
* ... and more in the future


## Installation

To set up the bot you need to have a .env file in the root directory of the executable with the following variables : 

```env
BOT_TOKEN=YOUR_BOT_TOKEN

# Api key for the Visual crossing API
WEATHER_API_KEY=YOUR_WEATHER_API_KEY

# Api key for VirusTotal API
VIRUS_TOTAL_API_KEY=YOUR_VIRUS_TOTAL_API_KEY

ASSETS_SERVER_URL=YOUR_ASSETS_SERVER_URL

NEWS_API_KEY=YOUR_NEWS_API_KEY

DEV_MODE=true

PATH_TO_CITIES_CSV=YOUR_PATH_TO_CITIES_CSV
```

you also need to have a worldcities.csv file in the root directory of the executable.


This bot is still in development and is not yet ready for production use.



## Road map 

- Complete voice commands ( playing music, listening in voice calls in order to perform voice recognition commands) : 

|  | Tasks                                                                          |
  |--|--------------------------------------------------------------------------------|
| ✔ | Basic player commands (play,pause,skip)                                        |
| x | Advanced player commands (settings, insert and queue system)                   |
| x | Auto logoff if the bot is alone in a voice channel                             |
| x | Saving audio configuration per server ( auto repeat function, voice listening) |
| x | Voice recognition features                                                     |
| x | Migration to kord-native voice                                                 |

  - Finish bot commands to manage a server with a system, here's a list of commands to add/finish : 
    
  |   | task                                                                    |
  |---|-------------------------------------------------------------------------|
  | ✔ | Role management in the server                                           |
  | x | User name modification                                                  |
  | x | Text and voice channel commands ( add/delete/update)                    |
  | x | Chat text moderation ( ban words, ads link or malicious link and files) |
  | x | Listing users and log about the user ( warn count, warn list...)        |

- Useful commands such as : 

|  | Task                                                                     |
|--|--------------------------------------------------------------------------|
| ✔ | Weather information with                                                 |
| x | Link and files security check (virusTotal)                               |
| x | response to messages containing a certain pattern with a custom response |
| x |                                                                          |




