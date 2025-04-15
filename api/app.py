from flask import Flask,request,jsonify
from thefuzz import process
from bs4 import BeautifulSoup
import requests

'''
GENERATE LIST OF VALID URLs
urls = []
soup = BeautifulSoup(requests.get('https://indianhelpline.com/sitemap.xml').text, 'html.parser')
for url in soup.findAll('url'):
    urls.append(url.findNext('loc').text.split('/')[-1])    
print(urls)
'''

urls = ['women-helpline', 'arunachal-pradesh', 'team', 'coach', 'andaman-and-nicobar-island', 'delhi-helpline-numbers', 'first-aid-training', 'nagpur', 'discount-deals', 'terms-and-conditions', 'uttar-pradesh', 'kota', 'rohtak', 'dermatologist', 'lab-booking', 'surat', 'special-child-councellor', 'ambulance-helpline', 'rajasthan', 'telangana', 'bangalore', 'gurugram', 'kerala', 'health-zone', 'odisha', 'travel', 'suicide-helpline', 'highway-numbers', 'agra', 'patna', 'health-guide', 'self-employment', 'learn', 'food-helpline', 'karnataka', 'meghalaya', 'tripura', 'gujrat', 'ambala', 'mizoram', 'manipur', 'jobs', 'ent', 'dentist', 'rewari', 'kolkata-helpline-numbers', 'indian-stuck-abroad', 'charitable-free-hospital-services', 'sikkim', 'jharkhand', 'hyderabad', 'delhitravel', 'uttarakhand', 'goa', 'indian-railways', 'andhra-pradesh', 'partners', 'birds-and-animal-helpline', 'meerut', 'nagaland', 'lucknow', 'yoga', 'psychologist', 'men-hepline', 'about-us', 'indore', 'know-thyself', 'ambala-city', 'important-websites', 'madhya-pradesh', 'kurukshetra', 'panipat', 'west-bengal', 'physio', 'de-addiction-helpline', 'ahemdabad', 'best-rehabs-in-india', 'web-made-easy', 'network', 'stressbuster', 'tamil-nadu', 'chandigarh', 'motivation', 'jaipur', 'mumbai-helpline-numbers', 'bihar', 'himachal-pradesh', 'faridabad', 'goa-travel', 'blood-banks', 'the-power-of-hugs-and-its-health-benefits', 'mumbaitravel', 'alerts', 'floods-disaster-helpline', 'pondicherry', 'ghaziabad', 'punjab', 'delhidoctors', 'doctor', 'disability-helpline', 'chatishgarh', 'world-wide-helpline', 'lakshadweep', 'delhi-hospital', 'consumer-complain', 'career', 'daman-and-diu', 'haryana', 'articles', 'privacypolicy', 'dietician', 'alwar', 'child-helpline', 'navi-mumbai', 'raipur', 'seniorhelpline', 'jammu-kashmir', 'noida', 'bhopal', 'lakdakh', 'indirapuram', 'karnal', 'pune', 'assam', 'maharashtra', 'important-numbers', 'chennai']

app = Flask(__name__)

def find_closest(url):
    return process.extractOne(url, urls)[0]

def process_text(text):
    text = text.split(' ')
    response = ['','']
    for word in text:
        if word.isdigit():
            response[1] = word
        elif any(char.isalpha() for char in word) and word.lower().strip() not in ['or','and']:
            response[0] += word + ' '
    return response

@app.route('/',methods=['POST'])
def hello_world():
    payload = request.get_json(force=True)
    url = find_closest(payload['location'])
    soup = BeautifulSoup(requests.get('https://indianhelpline.com/'+url).text, 'html.parser')
    returned_json = {}
    for para in soup.find_all('p'):
        response_text = process_text(para.text)
        if response_text[0] and response_text[1]:
            returned_json[response_text[0].strip()] = response_text[1]
    return jsonify(returned_json)