
# XPlore

This project is a mobile application developed in Android Java as part of a college lab project. The application uses the Foursquare API to access key locations around the user within a 10km radius and displays them on the app with customized search options. Additionally, it employs a custom-made API built using Python Flask to scrape data from [Indian Helpline](https://indianhelpline.com/) and, based on the current location o the user, finds the closest match using fuzzy matching and provides emergency and local helpline numbers.

## Live Link

Find the Emergency Contact API here: 

```bash
https://xplore-vkzl.onrender.com
```

*NOTE: The API only accepts POST request and does not require any authentication or API Keys*


## Technology Stack
- Frontend: Android Java
- Backend: Flask
- APIs: Foursquare API, Custom Flask API
- Web-Scraper: BeautifulSoup
- Security: Bcrypt


## Features

- **Location Discovery:**
   - Utilizes the Foursquare API to find and display key locations within a 10km radius of the user's current location.
   - Locations are displayed in a `Java ListView` with custom selection options.
   - Viewing individual point of interest if also possible with fetching of comments associated with the place (if any) for enhancement of user experience. 

- **Emergency Helplines:**
   - Custom Python Flask API scrapes data from [Indian Helpline](https://indianhelpline.com/) and displays as a `Java Listview` on the landing page.
   - Finds the closest match on the sitemap using fuzzy matching and retrieves relevant emergency and local helpline numbers based on the user's current location.

- **User Authentication:**
   - Login feature with password hashing.
   - User data is securely stored in an SQLite3 database.
