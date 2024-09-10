## This is the server code for my Astrotwin website!
Astrotwin is a web application where users can calculate their astrology chart and find celebrities who best match their chart!<br><br>
It has a number of features such as:
- Adjustable match variables for users to customize matches and emphasize certain chart placements
- A searchable celebrity database for users to compare their chart with a specific celebrities
- If a celebrity isn't in the database, users can insert their own celebrities!
<br><br>

<b>For more information about this project and how I created it check out the [About](https://astrotwin.live/about) section on astrotwin.live</b><br><br>
The frontend repo for Astrotwin can be found [here](https://github.com/laurenc206/astrotwin_client)<br><br>
If you would like to contact me or check out any of my other projects, go to my portfolio at [laurendev.com](https://laurendev.com) or email me at cavanaugh.lc@gmail.com<br>
<br>
## About
This is the server code for my Astrotwin website. It is written in Java and leverages the Springboot library to define API routes for the frontend to connect to and make queries to a MongoDB database storing pre-calculated celebrity charts.

In the background, the Astrotwin server runs Astrolog, a chart caluclation program necessary for performing the complex calculations to create birthcharts.

In the first and second version an Atlas service was required in order to retrieve location data necessary for these calculations. Astrotwin has since been updated to utilize the Google Location APIs and autocomplete to retrieve the location data used calculate user's charts. This has reduced the number of errors returned when a user's birth location was unable to be found by the Atlas Service and their chart was unable to be calculated.

I thought it would be fun if users were able to insert their own celebrities (if the birth information is avaliable). To implement this, I use a webscraper (located in the Service directory) to retrieve a celebrities birthdate information and then calculate their chart.

Matches are found by running a query on the database that uses variable planet quantifiers to rank matches. If a user adjusts any of the planet values in the modify match variables page, the frontend will trigger a new match query to be ran on the database so the matches are recalculated.

In summary, the server is responsible for doing all the calculations and performing the business logic necessary for Astrotwin to run, including connecting to other astrology programs and parsing the output, webscraping and connecting to the database to perform variable matching queries on the dynamic database containing celebrity charts. 
<br><br>
## Repo Index

### Controller:
Since User charts and Celebrity charts are handled differently there are different services dedicated to each. In this directory, you can find the API routes the frontend connects to to run the related service and retrieve data.<br>
- UserController
- CelebrityController

### Model:
This directory contains the data schema for the various entities used by the server including data recieved by the frontend, or internal server definitions used to calculate charts.
Data recieved by the frontend has 'Form' appended to the filename, as the data recieved is the result of a form object in the frontend:
- ContactForm
- MatchForm
- SearchForm
- UserForm
  
Other data models used internally are defined with the 'Model' appendage:
- AstrologModel
- AtlasModel
- CelebrityChartModel
- ChartNode
- MatchModel
- UserChartModel
- UserModel
- ZodiacModel

### Repositories:
Connections to the database can be found here.
- CelebChartsRepository
- CelebsRepository
- UserChartsRepository
- UsersRepository

### Service:
Here is where the calculations are done including running Astrolog, data parsing, webscraping and running match queries.
- **AtlasService:** Uses Astrologs location services to search for a location using string matching (depreciated since updating to Google Location APIs, where latitude and longitude can find more exact matches)
- **CelebService:** Webscrape celebrity birth data, connects to chart service and inserts celebrities into the database.
- **ChartService:** Creates a birthchart using the time and birth location. Time is washed so that timezones and daylight savings are taken into consideration. Location is matched exactly using latitude and longitude.
- **EmailService:** For the contact me page.
- **MatchService:** Run matching queries on the celebrity database.
- **UserService:** Calculate user's charts by running the chart service.

<br><br>
## To Start the Server

### Prerequisites:
You will first need to download and compile the Astrolog program. Astrotwin currently uses version 7.4. You will want to make sure that the server has the file path to the Astrolog program stored in a variable `ASTROLOG_FPATH` in the .env file.

The following code assumes you have Astrotwin_server and a directory called Astrolog in the root directory. The following directions are how I launched the server on an Amazon Linux EC2 instance but how you compile and provide the Astrolog directory will be machine dependent.
1. Create an Astrolog directory and run ``wget https://www.astrolog.org/ftp/ast75src.zip`` inside the directory to retrieve the Astrolog code.
2. Unzip Astrolog by running ``unzip ast75src.zip``
3. Install gcc and libX11 (the Linux EC2 instance doesn't come with these preinstalled) by running ``sudo yum install -y gcc-c++`` and ``sudo yum install libX11-deve``
4. Inside of the Astrolog directory, check out the MakeFile (remember this will be machine dependent) to compile Astrolog. Run ``make`` in the Astrolog directory. <br><br>

Next you will need to get the Astrotwin server code from git. Since EC2 instances don't come preinstalled with git, maven or java you will need to run the following in order to install these packages: <br>
  ``sudo yum install git -y`` <br>
  ``sudo yum install java-17-amazon-corretto-headless`` <br>
  ``sudo yum install maven`` <br><br>

Inside the .env file, you will need to configure the following:
 - **ASTROLOG_FPATH=** the filepath to Astrolog
 - Database Configurations <br><br>

You will also need to modify the `@CrossOrigin(origins = "astrotwin-client/client-url")` at the top of [CelebController](https://github.com/laurenc206/astrotwin_server/tree/main/src/main/java/dev/lauren/astrotwin/Controller/CelebController) and [UserController](https://github.com/laurenc206/astrotwin_server/tree/main/src/main/java/dev/lauren/astrotwin/Controller/UserController) with the url of astrotwin-client/client to bypass CORS and allow requests to go through. <br><br>

### To Run:

1. Compile AstroTwin by running ``maven clean install`` inside the AstroTwin directory containing the pom.xml
2. Change directory into the main directory containing the Astrolog directory and the astrotwin_server code from git.
   Run ``java -jar ./astrotwin_server/target/astrotwin-0.0.1-SNAPSHOT.jar`` to start the server.
