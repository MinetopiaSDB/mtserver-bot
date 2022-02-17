# MTSERVER Discord bot
De MTSERVER Discord bot is gemaakt om gemakkelijk en gratis subdomeinen aan te kunnen bieden aan de gebruikers van MinetopiaSDB. 

Door middel van het uitvoeren van een simpel commando kan de gebruiker een subdomein aanmaken onder de [mtserver.nl](https://mtserver.nl/) domein naam.

## Commands

Er zijn drie commands in deze Discord bot:
- `/createsubdomain`: Met dit command kan een gebruiker maximaal één subdomein aanmaken.
- `/deletesubdomain`: Met dit command kan een gebruiker zijn subdomein(en) verwijderen.
- `/listsubdomains`: Met dit command kan een gebruiker een lijst van zijn subdomein(en) zien.

## Hoe kan ik de bot zelf hosten?
Download de laatste versie van de bot [hier](https://github.com/MinetopiaSDB/mtserver-bot/releases). De MTSERVER Discord bot vereist Java 17.

Voordat je aan de slag kunt, moet je de bot token en MySQL inloggegevens instellen in het config.yml bestand. 
Je kunt hiervoor het config.example.yml bestand gebruiken. 

### Toevoegen domeinnamen
Om domeinnamen vanuit Cloudflare toe te voegen heb je een API-key nodig. 
Deze kun je op de [API Tokens](https://dash.cloudflare.com/profile/api-tokens) pagina van Cloudflare ophalen. Je hebt op deze pagina je 'Global API Key' nodig.

Je kunt deze Global API Key nu invullen op de plek van `AuthKey`. Vul gelijk het e-mailadres van jouw Cloudflare account in bij het `AuthEmail` veld van jouw config.yml.

De `ZoneId` kun je ophalen door jouw domeinnaam te openen in jouw Cloudflare dashboard. Het ZoneId is vervolgens te vinden aan de rechterkant van jouw scherm onder het kopje 'API'.

Je kunt meerdere domeinnamen toevoegen in jouw config.yml bestand, waarna gebruikers met de slash commands zelf kunnen kiezen onder welk domein ze hun subdomein aan willen maken. 
```yaml
Discord:
  BotToken: 'YOUR_BOT_TOKEN'
Database:
  host: 'localhost'
  port: 3306
  user: 'root'
  password: 'root'
  database: 'mtserverbot'
Domains:
  -
    ZoneId: ''
    Domain: 'mtserver.nl'
    AuthEmail: ''
    AuthKey: ''
#  -
#    ZoneId: ''
#    Domain: 'eenanderedomeinnaam.nl'
#    AuthEmail: ''
#    AuthKey: ''
```