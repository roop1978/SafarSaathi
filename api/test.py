from bs4 import BeautifulSoup
import requests

url = 'chennai'
soup = BeautifulSoup(requests.get('https://in.bookmyshow.com/explore/movies-'+url).text, 'html.parser')
print(soup.prettify())